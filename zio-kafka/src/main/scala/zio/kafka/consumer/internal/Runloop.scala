package zio.kafka.consumer.internal

import org.apache.kafka.clients.consumer._
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.errors.RebalanceInProgressException
import zio._
import zio.kafka.consumer.Consumer.{ CommitTimeout, OffsetRetrieval }
import zio.kafka.consumer._
import zio.kafka.consumer.diagnostics.DiagnosticEvent.Finalization
import zio.kafka.consumer.diagnostics.{ DiagnosticEvent, Diagnostics }
import zio.kafka.consumer.fetch.FetchStrategy
import zio.kafka.consumer.internal.ConsumerAccess.ByteArrayKafkaConsumer
import zio.kafka.consumer.internal.Runloop._
import zio.kafka.consumer.internal.RunloopAccess.PartitionAssignment
import zio.stream._

import java.util
import java.util.{ Map => JavaMap }
import scala.collection.mutable
import scala.jdk.CollectionConverters._

//noinspection SimplifyWhenInspection,SimplifyUnlessInspection
private[consumer] final class Runloop private (
  runtime: Runtime[Any],
  hasGroupId: Boolean,
  consumer: ConsumerAccess,
  pollTimeout: Duration,
  maxPollInterval: Duration,
  commitTimeout: Duration,
  commandQueue: Queue[RunloopCommand],
  lastRebalanceEvent: Ref.Synchronized[Runloop.RebalanceEvent],
  partitionsHub: Hub[Take[Throwable, PartitionAssignment]],
  diagnostics: Diagnostics,
  offsetRetrieval: OffsetRetrieval,
  userRebalanceListener: RebalanceListener,
  restartStreamsOnRebalancing: Boolean,
  currentStateRef: Ref[State],
  fetchStrategy: FetchStrategy
) {

  private def newPartitionStream(tp: TopicPartition): UIO[PartitionStreamControl] =
    PartitionStreamControl.newPartitionStream(tp, commandQueue, diagnostics, maxPollInterval)

  def stopConsumption: UIO[Unit] =
    ZIO.logDebug("stopConsumption called") *>
      commandQueue.offer(RunloopCommand.StopAllStreams).unit

  private[consumer] def shutdown: UIO[Unit] =
    ZIO.logDebug(s"Shutting down runloop initiated") *>
      commandQueue
        .offerAll(
          Chunk(
            RunloopCommand.RemoveAllSubscriptions,
            RunloopCommand.StopAllStreams,
            RunloopCommand.StopRunloop
          )
        )
        .unit

  private[internal] def addSubscription(subscription: Subscription): IO[InvalidSubscriptionUnion, Unit] =
    for {
      _       <- ZIO.logDebug(s"Add subscription $subscription")
      promise <- Promise.make[InvalidSubscriptionUnion, Unit]
      _       <- commandQueue.offer(RunloopCommand.AddSubscription(subscription, promise))
      _       <- ZIO.logDebug(s"Waiting for subscription $subscription")
      _       <- promise.await
      _       <- ZIO.logDebug(s"Done for subscription $subscription")
    } yield ()

  private[internal] def removeSubscription(subscription: Subscription): UIO[Unit] =
    commandQueue.offer(RunloopCommand.RemoveSubscription(subscription)).unit

  private val rebalanceListener: RebalanceListener = {
    val recordRebalanceRebalancingListener = RebalanceListener(
      onAssigned = (assignedTps, _) =>
        for {
          _              <- ZIO.logDebug(s"${assignedTps.size} partitions are assigned")
          _              <- diagnostics.emit(DiagnosticEvent.Rebalance.Assigned(assignedTps))
          rebalanceEvent <- lastRebalanceEvent.get
          state          <- currentStateRef.get
          streamsToEnd = if (restartStreamsOnRebalancing && !rebalanceEvent.wasInvoked) state.assignedStreams
                         else Chunk.empty
          _ <- ZIO.foreachDiscard(streamsToEnd)(_.end)
          _ <- lastRebalanceEvent.set(rebalanceEvent.onAssigned(assignedTps, endedStreams = streamsToEnd))
          _ <- ZIO.logTrace("onAssigned done")
        } yield (),
      onRevoked = (revokedTps, _) =>
        for {
          _              <- ZIO.logDebug(s"${revokedTps.size} partitions are revoked")
          _              <- diagnostics.emit(DiagnosticEvent.Rebalance.Revoked(revokedTps))
          rebalanceEvent <- lastRebalanceEvent.get
          state          <- currentStateRef.get
          streamsToEnd = if (restartStreamsOnRebalancing && !rebalanceEvent.wasInvoked) state.assignedStreams
                         else state.assignedStreams.filter(control => revokedTps.contains(control.tp))
          _ <- ZIO.foreachDiscard(streamsToEnd)(_.end)
          _ <- lastRebalanceEvent.set(rebalanceEvent.onRevoked(revokedTps, endedStreams = streamsToEnd))
          _ <- ZIO.logTrace("onRevoked done")
        } yield (),
      onLost = (lostTps, _) =>
        for {
          _              <- ZIO.logDebug(s"${lostTps.size} partitions are lost")
          _              <- diagnostics.emit(DiagnosticEvent.Rebalance.Lost(lostTps))
          rebalanceEvent <- lastRebalanceEvent.get
          state          <- currentStateRef.get
          (lostStreams, remainingStreams) = state.assignedStreams.partition(control => lostTps.contains(control.tp))
          _ <- ZIO.foreachDiscard(lostStreams)(_.lost)
          streamsToEnd = if (restartStreamsOnRebalancing && !rebalanceEvent.wasInvoked) remainingStreams
                         else Chunk.empty
          _ <- ZIO.foreachDiscard(streamsToEnd)(_.end)
          _ <- lastRebalanceEvent.set(rebalanceEvent.onLost(lostTps, endedStreams = streamsToEnd))
          _ <- ZIO.logTrace(s"onLost done")
        } yield ()
    )

    recordRebalanceRebalancingListener ++ userRebalanceListener
  }

  /** This is the implementation behind the user facing api `Offset.commit`. */
  private val commit: Map[TopicPartition, OffsetAndMetadata] => Task[Unit] =
    offsets =>
      for {
        p <- Promise.make[Throwable, Unit]
        _ <- commandQueue.offer(RunloopCommand.Commit(offsets, p)).unit
        _ <- diagnostics.emit(DiagnosticEvent.Commit.Started(offsets))
        _ <- p.await.timeoutFail(CommitTimeout)(commitTimeout)
      } yield ()

  /** Merge commits and prepare parameters for calling `consumer.commitAsync`. */
  private def asyncCommitParameters(
    commits: Chunk[RunloopCommand.Commit]
  ): (JavaMap[TopicPartition, OffsetAndMetadata], OffsetCommitCallback, Throwable => UIO[Unit]) = {
    val offsets = commits
      .foldLeft(mutable.Map.empty[TopicPartition, OffsetAndMetadata]) { case (acc, commit) =>
        commit.offsets.foreach { case (tp, offset) =>
          acc += (tp -> acc
            .get(tp)
            .map(current => if (current.offset() > offset.offset()) current else offset)
            .getOrElse(offset))
        }
        acc
      }
      .toMap
    val offsetsWithMetaData = offsets.map { case (tp, offset) =>
      tp -> new OffsetAndMetadata(offset.offset + 1, offset.leaderEpoch, offset.metadata)
    }
    val cont      = (e: Exit[Throwable, Unit]) => ZIO.foreachDiscard(commits)(_.cont.done(e))
    val onSuccess = cont(Exit.unit) <* diagnostics.emit(DiagnosticEvent.Commit.Success(offsetsWithMetaData))
    val onFailure: Throwable => UIO[Unit] = {
      case _: RebalanceInProgressException =>
        for {
          _ <- ZIO.logDebug(s"Rebalance in progress, commit for offsets $offsets will be retried")
          _ <- commandQueue.offerAll(commits)
        } yield ()
      case err: Throwable =>
        cont(Exit.fail(err)) <* diagnostics.emit(DiagnosticEvent.Commit.Failure(offsetsWithMetaData, err))
    }
    val callback =
      new OffsetCommitCallback {
        override def onComplete(offsets: util.Map[TopicPartition, OffsetAndMetadata], exception: Exception): Unit =
          Unsafe.unsafe { implicit u =>
            runtime.unsafe.run {
              if (exception eq null) onSuccess else onFailure(exception)
            }
              .getOrThrowFiberFailure()
          }
      }
    (offsetsWithMetaData.asJava, callback, onFailure)
  }

  private def handleCommits(state: State, commits: Chunk[RunloopCommand.Commit]): UIO[State] =
    if (commits.isEmpty) {
      ZIO.succeed(state)
    } else {
      val (offsets, callback, onFailure) = asyncCommitParameters(commits)
      val newState                       = state.addCommits(commits)
      consumer.runloopAccess { c =>
        // We don't wait for the completion of the commit here, because it
        // will only complete once we poll again.
        ZIO.attempt(c.commitAsync(offsets, callback))
      }
        .catchAll(onFailure)
        .as(newState)
    }

  /**
   * Does all needed to end revoked partitions:
   *   1. Complete the revoked assigned streams 2. Remove from the list of pending requests
   * @return
   *   New pending requests, new active assigned streams
   */
  private def endRevokedPartitions(
    pendingRequests: Chunk[RunloopCommand.Request],
    assignedStreams: Chunk[PartitionStreamControl],
    isRevoked: TopicPartition => Boolean
  ): UIO[Runloop.RevokeResult] = {
    val (revokedStreams, newAssignedStreams) =
      assignedStreams.partition(control => isRevoked(control.tp))

    ZIO
      .foreachDiscard(revokedStreams)(_.end)
      .as(
        Runloop.RevokeResult(
          pendingRequests = pendingRequests.filter(req => !isRevoked(req.tp)),
          assignedStreams = newAssignedStreams
        )
      )
  }

  /**
   * Offer records retrieved from poll() call to the streams.
   *
   * @return
   *   Remaining pending requests
   */
  private def offerRecordsToStreams(
    partitionStreams: Chunk[PartitionStreamControl],
    pendingRequests: Chunk[RunloopCommand.Request],
    ignoreRecordsForTps: Set[TopicPartition],
    polledRecords: ConsumerRecords[Array[Byte], Array[Byte]]
  ): UIO[Runloop.FulfillResult] = {
    type Record = CommittableRecord[Array[Byte], Array[Byte]]

    // The most efficient way to get the records from [[ConsumerRecords]] per
    // topic-partition, is by first getting the set of topic-partitions, and
    // then requesting the records per topic-partition.
    val tps           = polledRecords.partitions().asScala.toSet -- ignoreRecordsForTps
    val fulfillResult = Runloop.FulfillResult(pendingRequests = pendingRequests.filter(req => !tps.contains(req.tp)))
    val streams =
      if (tps.isEmpty) Chunk.empty else partitionStreams.filter(streamControl => tps.contains(streamControl.tp))

    if (streams.isEmpty) ZIO.succeed(fulfillResult)
    else {
      for {
        consumerGroupMetadata <- getConsumerGroupMetadataIfAny
        _ <- ZIO.foreachParDiscard(streams) { streamControl =>
               val tp      = streamControl.tp
               val records = polledRecords.records(tp)
               if (records.isEmpty) ZIO.unit
               else {
                 val builder  = ChunkBuilder.make[Record](records.size())
                 val iterator = records.iterator()
                 while (iterator.hasNext) {
                   val consumerRecord = iterator.next()
                   builder +=
                     CommittableRecord[Array[Byte], Array[Byte]](
                       record = consumerRecord,
                       commitHandle = commit,
                       consumerGroupMetadata = consumerGroupMetadata
                     )
                 }
                 streamControl.offerRecords(builder.result())
               }
             }
      } yield fulfillResult
    }
  }

  private val getConsumerGroupMetadataIfAny: UIO[Option[ConsumerGroupMetadata]] =
    if (hasGroupId) consumer.runloopAccess(c => ZIO.attempt(c.groupMetadata())).fold(_ => None, Some(_))
    else ZIO.none

  private def doSeekForNewPartitions(c: ByteArrayKafkaConsumer, tps: Set[TopicPartition]): Task[Set[TopicPartition]] =
    offsetRetrieval match {
      case OffsetRetrieval.Auto(_) => ZIO.succeed(Set.empty)
      case OffsetRetrieval.Manual(getOffsets) =>
        if (tps.isEmpty) ZIO.succeed(Set.empty)
        else
          getOffsets(tps)
            .flatMap(offsets => ZIO.attempt(offsets.foreach { case (tp, offset) => c.seek(tp, offset) }))
            .as(tps)
    }

  /**
   * Pause partitions for which there is no demand and resume those for which there is now demand.
   */
  private def resumeAndPausePartitions(
    c: ByteArrayKafkaConsumer,
    assignment: Set[TopicPartition],
    requestedPartitions: Set[TopicPartition]
  ): Unit = {
    val toResume = assignment intersect requestedPartitions
    val toPause  = assignment -- requestedPartitions

    if (toResume.nonEmpty) c.resume(toResume.asJava)
    if (toPause.nonEmpty) c.pause(toPause.asJava)
  }

  private def handlePoll(state: State): Task[State] =
    for {
      partitionsToFetch <- fetchStrategy.selectPartitionsToFetch(state.assignedStreams)
      _ <- ZIO.logDebug(
             s"Starting poll with ${state.pendingRequests.size} pending requests and" +
               s" ${state.pendingCommits.size} pending commits," +
               s" resuming ${partitionsToFetch} partitions"
           )
      _ <- currentStateRef.set(state)
      pollResult <-
        consumer.runloopAccess { c =>
          ZIO.suspend {
            val prevAssigned = c.assignment().asScala.toSet

            resumeAndPausePartitions(c, prevAssigned, partitionsToFetch)

            val polledRecords = {
              val records = c.poll(pollTimeout)
              if (records eq null) ConsumerRecords.empty[Array[Byte], Array[Byte]]() else records
            }

            diagnostics.emit {
              val providedTps         = polledRecords.partitions().asScala.toSet
              val requestedPartitions = state.pendingRequests.map(_.tp).toSet

              DiagnosticEvent.Poll(
                tpRequested = requestedPartitions,
                tpWithData = providedTps,
                tpWithoutData = requestedPartitions -- providedTps
              )
            } *>
              lastRebalanceEvent.getAndSet(RebalanceEvent.None).flatMap {
                case RebalanceEvent(false, _, _, _, _) =>
                  // The fast track, rebalance listener was not invoked:
                  //   no assignment changes, only new records.
                  ZIO.succeed(
                    PollResult(
                      records = polledRecords,
                      ignoreRecordsForTps = Set.empty,
                      pendingRequests = state.pendingRequests,
                      assignedStreams = state.assignedStreams
                    )
                  )

                case RebalanceEvent(true, assignedTps, revokedTps, lostTps, endedStreams) =>
                  // The slow track, the rebalance listener was invoked:
                  //   some partitions were assigned, revoked or lost,
                  //   some streams have ended.

                  val currentAssigned = c.assignment().asScala.toSet
                  val endedTps        = endedStreams.map(_.tp).toSet
                  for {
                    ignoreRecordsForTps <- doSeekForNewPartitions(c, assignedTps)

                    // The topic partitions that need a new stream are:
                    //  1. Those that are freshly assigned
                    //  2. Those that are still assigned but were ended in the rebalance listener because
                    //     of `restartStreamsOnRebalancing` being true
                    startingTps = assignedTps ++ (currentAssigned intersect endedTps)

                    startingStreams <-
                      ZIO.foreach(Chunk.fromIterable(startingTps))(newPartitionStream).tap { newStreams =>
                        ZIO.logDebug(s"Offering partition assignment $startingTps") *>
                          partitionsHub.publish(
                            Take.chunk(newStreams.map(_.tpStream))
                          )
                      }

                    updatedAssignedStreams =
                      state.assignedStreams.filter(s => !endedTps.contains(s.tp)) ++ startingStreams

                    // Remove pending requests for all streams that ended:
                    //  1. streams that were ended because the partition was lost
                    //  2. streams that were ended because the partition was revoked
                    //  3. streams that were ended because of `restartStreamsOnRebalancing` being true
                    updatedPendingRequests =
                      state.pendingRequests.filter { pendingRequest =>
                        val tp = pendingRequest.tp
                        !(lostTps.contains(tp) || revokedTps.contains(tp) || endedStreams.exists(_.tp == tp))
                      }
                  } yield Runloop.PollResult(
                    records = polledRecords,
                    ignoreRecordsForTps = ignoreRecordsForTps,
                    pendingRequests = updatedPendingRequests,
                    assignedStreams = updatedAssignedStreams
                  )
              }
          }
        }
      fulfillResult <- offerRecordsToStreams(
                         pollResult.assignedStreams,
                         pollResult.pendingRequests,
                         pollResult.ignoreRecordsForTps,
                         pollResult.records
                       )
      updatedPendingCommits <- ZIO.filter(state.pendingCommits)(_.isPending)
      _                     <- checkStreamPollInterval(pollResult.assignedStreams)
    } yield state.copy(
      pendingRequests = fulfillResult.pendingRequests,
      pendingCommits = updatedPendingCommits,
      assignedStreams = pollResult.assignedStreams
    )

  /**
   * Check each stream to see if it exceeded its poll interval. If so, halt it. In addition, if any stream has exceeded
   * its poll interval, shutdown the consumer.
   */
  private def checkStreamPollInterval(streams: Chunk[PartitionStreamControl]): ZIO[Any, Nothing, Unit] =
    for {
      now <- Clock.nanoTime
      anyExceeded <- ZIO.foldLeft(streams)(false) { case (acc, stream) =>
                       stream
                         .maxPollIntervalExceeded(now)
                         .tap(exceeded => if (exceeded) stream.halt else ZIO.unit)
                         .map(acc || _)
                     }
      _ <- shutdown.when(anyExceeded)
    } yield ()

  private def handleCommand(state: State, cmd: RunloopCommand.StreamCommand): Task[State] = {
    def doChangeSubscription(newSubscriptionState: SubscriptionState): Task[State] =
      applyNewSubscriptionState(newSubscriptionState).flatMap { newAssignedStreams =>
        val newState = state.copy(
          assignedStreams = state.assignedStreams ++ newAssignedStreams,
          subscriptionState = newSubscriptionState
        )
        if (newSubscriptionState.isSubscribed) ZIO.succeed(newState)
        else
          // End all streams and pending requests
          endRevokedPartitions(
            newState.pendingRequests,
            newState.assignedStreams,
            isRevoked = _ => true
          ).map { revokeResult =>
            newState.copy(
              pendingRequests = revokeResult.pendingRequests,
              assignedStreams = revokeResult.assignedStreams
            )
          }
      }

    cmd match {
      case req: RunloopCommand.Request => ZIO.succeed(state.addRequest(req))
      case cmd @ RunloopCommand.AddSubscription(newSubscription, _) =>
        state.subscriptionState match {
          case SubscriptionState.NotSubscribed =>
            val newSubState =
              SubscriptionState.Subscribed(subscriptions = Set(newSubscription), union = newSubscription)
            cmd.succeed *> doChangeSubscription(newSubState)
          case SubscriptionState.Subscribed(existingSubscriptions, _) =>
            val subs = NonEmptyChunk.fromIterable(newSubscription, existingSubscriptions)

            Subscription.unionAll(subs) match {
              case None => cmd.fail(InvalidSubscriptionUnion(subs)).as(state)
              case Some(union) =>
                val newSubState =
                  SubscriptionState.Subscribed(
                    subscriptions = existingSubscriptions + newSubscription,
                    union = union
                  )
                cmd.succeed *> doChangeSubscription(newSubState)
            }
        }
      case RunloopCommand.RemoveSubscription(subscription) =>
        state.subscriptionState match {
          case SubscriptionState.NotSubscribed => ZIO.succeed(state)
          case SubscriptionState.Subscribed(existingSubscriptions, _) =>
            val newUnion: Option[(Subscription, NonEmptyChunk[Subscription])] =
              NonEmptyChunk
                .fromIterableOption(existingSubscriptions - subscription)
                .flatMap(subs => Subscription.unionAll(subs).map(_ -> subs))

            newUnion match {
              case Some((union, newSubscriptions)) =>
                val newSubState =
                  SubscriptionState.Subscribed(subscriptions = newSubscriptions.toSet, union = union)
                doChangeSubscription(newSubState)
              case None =>
                ZIO.logDebug(s"Unsubscribing kafka consumer") *>
                  doChangeSubscription(SubscriptionState.NotSubscribed)
            }
        }
      case RunloopCommand.RemoveAllSubscriptions => doChangeSubscription(SubscriptionState.NotSubscribed)
      case RunloopCommand.StopAllStreams =>
        for {
          _ <- ZIO.logDebug("Stop all streams initiated")
          _ <- ZIO.foreachDiscard(state.assignedStreams)(_.end)
          _ <- partitionsHub.publish(Take.end)
          _ <- ZIO.logDebug("Stop all streams done")
        } yield state.copy(pendingRequests = Chunk.empty)
    }
  }

  private def applyNewSubscriptionState(
    newSubscriptionState: SubscriptionState
  ): Task[Chunk[PartitionStreamControl]] =
    consumer.runloopAccess { c =>
      newSubscriptionState match {
        case SubscriptionState.NotSubscribed =>
          ZIO
            .attempt(c.unsubscribe())
            .as(Chunk.empty)
        case SubscriptionState.Subscribed(_, Subscription.Pattern(pattern)) =>
          val rc = RebalanceConsumer.Live(c)
          ZIO
            .attempt(c.subscribe(pattern.pattern, rebalanceListener.toKafka(runtime, rc)))
            .as(Chunk.empty)
        case SubscriptionState.Subscribed(_, Subscription.Topics(topics)) =>
          val rc = RebalanceConsumer.Live(c)
          ZIO
            .attempt(c.subscribe(topics.asJava, rebalanceListener.toKafka(runtime, rc)))
            .as(Chunk.empty)
        case SubscriptionState.Subscribed(_, Subscription.Manual(topicPartitions)) =>
          // For manual subscriptions we have to do some manual work before starting the run loop
          for {
            _                <- ZIO.attempt(c.assign(topicPartitions.asJava))
            _                <- doSeekForNewPartitions(c, topicPartitions)
            partitionStreams <- ZIO.foreach(Chunk.fromIterable(topicPartitions))(newPartitionStream)
            _                <- partitionsHub.publish(Take.chunk(partitionStreams.map(_.tpStream)))
          } yield partitionStreams
      }
    }

  /**
   * Poll behavior:
   *   - Run until the StopRunloop command is received
   *   - Process all currently queued Commits
   *   - Process all currently queued commands
   *   - Poll the Kafka broker
   *   - After command handling and after polling, determine whether the runloop should continue:
   *     - Poll only when subscribed (leads to exceptions from the Apache Kafka Consumer if not)
   *     - Poll continuously when there are (still) unfulfilled requests or pending commits
   *     - Poll periodically when we are subscribed but do not have assigned streams yet. This happens after
   *       initialization and rebalancing
   */
  private def run(initialState: State): ZIO[Scope, Throwable, Any] = {
    import Runloop.StreamOps

    ZStream
      .fromQueue(commandQueue)
      .takeWhile(_ != RunloopCommand.StopRunloop)
      .runFoldChunksDiscardZIO(initialState) { (state, commands) =>
        for {
          _ <- ZIO.logDebug(s"Processing ${commands.size} commands: ${commands.mkString(",")}")
          commitCommands = commands.collect { case cmd: RunloopCommand.Commit => cmd }
          stateAfterCommits <- handleCommits(state, commitCommands)
          streamCommands = commands.collect { case cmd: RunloopCommand.StreamCommand => cmd }
          stateAfterCommands <- ZIO.foldLeft(streamCommands)(stateAfterCommits)(handleCommand)

          updatedStateAfterPoll <- if (stateAfterCommands.shouldPoll) handlePoll(stateAfterCommands)
                                   else ZIO.succeed(stateAfterCommands)
          // Immediately poll again, after processing all new queued commands
          _ <- if (updatedStateAfterPoll.shouldPoll) commandQueue.offer(RunloopCommand.Poll) else ZIO.unit
        } yield updatedStateAfterPoll
      }
      .tapErrorCause(cause => ZIO.logErrorCause("Error in Runloop", cause))
      .onError(cause => partitionsHub.offer(Take.failCause(cause)))
  }
}

private[consumer] object Runloop {
  private implicit final class StreamOps[R, E, A](private val stream: ZStream[R, E, A]) extends AnyVal {

    /**
     * Inlined, simplified and specialized for our needs version of [[ZSink.foldChunksZIO]]
     *
     * Code initially inspired by the implementation of [[ZStream.runFoldZIO]] with everything we don't need removed and
     * with chunking added
     */
    def runFoldChunksDiscardZIO[R1 <: R, E1 >: E, S](s: S)(f: (S, Chunk[A]) => ZIO[R1, E1, S]): ZIO[R1, E1, Unit] = {
      def reader(s: S): ZChannel[R1, E1, Chunk[A], Any, E1, Nothing, Unit] =
        ZChannel.readWithCause(
          (in: Chunk[A]) => ZChannel.fromZIO(f(s, in)).flatMap(reader),
          (err: Cause[E1]) => ZChannel.refailCause(err),
          (_: Any) => ZChannel.unit
        )

      stream.run(ZSink.fromChannel(reader(s)))
    }
  }

  type ByteArrayCommittableRecord = CommittableRecord[Array[Byte], Array[Byte]]

  private final case class PollResult(
    records: ConsumerRecords[Array[Byte], Array[Byte]],
    ignoreRecordsForTps: Set[TopicPartition],
    pendingRequests: Chunk[RunloopCommand.Request],
    assignedStreams: Chunk[PartitionStreamControl]
  )
  private final case class RevokeResult(
    pendingRequests: Chunk[RunloopCommand.Request],
    assignedStreams: Chunk[PartitionStreamControl]
  )
  private final case class FulfillResult(
    pendingRequests: Chunk[RunloopCommand.Request]
  )

  private final case class RebalanceEvent(
    wasInvoked: Boolean,
    assignedTps: Set[TopicPartition],
    revokedTps: Set[TopicPartition],
    lostTps: Set[TopicPartition],
    endedStreams: Chunk[PartitionStreamControl]
  ) {
    def onAssigned(assigned: Set[TopicPartition], endedStreams: Chunk[PartitionStreamControl]): RebalanceEvent =
      copy(
        wasInvoked = true,
        assignedTps = assignedTps ++ assigned,
        endedStreams = this.endedStreams ++ endedStreams
      )

    def onRevoked(revoked: Set[TopicPartition], endedStreams: Chunk[PartitionStreamControl]): RebalanceEvent =
      copy(
        wasInvoked = true,
        revokedTps = revokedTps ++ revoked,
        endedStreams = this.endedStreams ++ endedStreams
      )

    def onLost(lost: Set[TopicPartition], endedStreams: Chunk[PartitionStreamControl]): RebalanceEvent =
      copy(
        wasInvoked = true,
        lostTps = lostTps ++ lost,
        endedStreams = this.endedStreams ++ endedStreams
      )
  }

  private object RebalanceEvent {
    val None: RebalanceEvent = RebalanceEvent(wasInvoked = false, Set.empty, Set.empty, Set.empty, Chunk.empty)
  }

  def make(
    hasGroupId: Boolean,
    consumer: ConsumerAccess,
    pollTimeout: Duration,
    maxPollInterval: Duration,
    commitTimeout: Duration,
    diagnostics: Diagnostics,
    offsetRetrieval: OffsetRetrieval,
    userRebalanceListener: RebalanceListener,
    restartStreamsOnRebalancing: Boolean,
    partitionsHub: Hub[Take[Throwable, PartitionAssignment]],
    fetchStrategy: FetchStrategy
  ): URIO[Scope, Runloop] =
    for {
      _                  <- ZIO.addFinalizer(diagnostics.emit(Finalization.RunloopFinalized))
      commandQueue       <- ZIO.acquireRelease(Queue.unbounded[RunloopCommand])(_.shutdown)
      lastRebalanceEvent <- Ref.Synchronized.make[Runloop.RebalanceEvent](Runloop.RebalanceEvent.None)
      initialState = State.initial
      currentStateRef <- Ref.make(initialState)
      runtime         <- ZIO.runtime[Any]
      runloop = new Runloop(
                  runtime = runtime,
                  hasGroupId = hasGroupId,
                  consumer = consumer,
                  pollTimeout = pollTimeout,
                  maxPollInterval = maxPollInterval,
                  commitTimeout = commitTimeout,
                  commandQueue = commandQueue,
                  lastRebalanceEvent = lastRebalanceEvent,
                  partitionsHub = partitionsHub,
                  diagnostics = diagnostics,
                  offsetRetrieval = offsetRetrieval,
                  userRebalanceListener = userRebalanceListener,
                  restartStreamsOnRebalancing = restartStreamsOnRebalancing,
                  currentStateRef = currentStateRef,
                  fetchStrategy = fetchStrategy
                )
      _ <- ZIO.logDebug("Starting Runloop")

      // Run the entire loop on the a dedicated thread to avoid executor shifts
      executor <- RunloopExecutor.newInstance
      fiber    <- ZIO.onExecutor(executor)(runloop.run(initialState)).forkScoped
      waitForRunloopStop = fiber.join.orDie

      _ <- ZIO.addFinalizer(
             ZIO.logDebug("Shutting down Runloop") *>
               runloop.shutdown *>
               waitForRunloopStop <*
               ZIO.logDebug("Shut down Runloop")
           )
    } yield runloop

  private final case class State(
    pendingRequests: Chunk[RunloopCommand.Request],
    pendingCommits: Chunk[RunloopCommand.Commit],
    assignedStreams: Chunk[PartitionStreamControl],
    subscriptionState: SubscriptionState
  ) {
    def addCommits(c: Chunk[RunloopCommand.Commit]): State = copy(pendingCommits = pendingCommits ++ c)
    def addRequest(r: RunloopCommand.Request): State       = copy(pendingRequests = pendingRequests :+ r)

    def shouldPoll: Boolean =
      subscriptionState.isSubscribed && (pendingRequests.nonEmpty || pendingCommits.nonEmpty || assignedStreams.isEmpty)
  }

  private object State {
    val initial: State = State(
      pendingRequests = Chunk.empty,
      pendingCommits = Chunk.empty,
      assignedStreams = Chunk.empty,
      subscriptionState = SubscriptionState.NotSubscribed
    )
  }
}
