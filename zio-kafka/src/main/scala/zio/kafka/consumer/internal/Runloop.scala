package zio.kafka.consumer.internal

import org.apache.kafka.clients.consumer._
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.errors.{ AuthenticationException, AuthorizationException }
import zio._
import zio.kafka.consumer.Consumer.OffsetRetrieval
import zio.kafka.consumer._
import zio.kafka.consumer.diagnostics.DiagnosticEvent.{ Finalization, Rebalance }
import zio.kafka.consumer.diagnostics.{ DiagnosticEvent, Diagnostics }
import zio.kafka.consumer.internal.ConsumerAccess.ByteArrayKafkaConsumer
import zio.kafka.consumer.internal.RebalanceCoordinator.RebalanceEvent
import zio.kafka.consumer.internal.Runloop._
import zio.kafka.consumer.internal.RunloopAccess.PartitionAssignment
import zio.stream._

import scala.jdk.CollectionConverters._

//noinspection SimplifyWhenInspection,SimplifyUnlessInspection
private[consumer] final class Runloop private (
  settings: ConsumerSettings,
  topLevelExecutor: Executor,
  sameThreadRuntime: Runtime[Any],
  consumer: ConsumerAccess,
  commandQueue: Queue[RunloopCommand],
  partitionsHub: Hub[Take[Throwable, PartitionAssignment]],
  diagnostics: Diagnostics,
  maxStreamPullInterval: Duration,
  currentStateRef: Ref[State],
  rebalanceCoordinator: RebalanceCoordinator,
  consumerMetrics: ConsumerMetrics,
  committer: Committer
) {
  private def newPartitionStream(tp: TopicPartition): UIO[PartitionStreamControl] =
    PartitionStreamControl.newPartitionStream(
      tp,
      commandQueue.offer(RunloopCommand.Request(tp)).unit,
      diagnostics,
      maxStreamPullInterval
    )

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

  private def makeRebalanceListener: ConsumerRebalanceListener = {
    // Here we just want to avoid any executor shift if the user provided listener is the noop listener.
    val userRebalanceListener =
      settings.rebalanceListener match {
        case RebalanceListener.noop => RebalanceListener.noop
        case _                      => settings.rebalanceListener.runOnExecutor(topLevelExecutor)
      }

    RebalanceListener.toKafka(rebalanceCoordinator.toRebalanceListener ++ userRebalanceListener, sameThreadRuntime)
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
    polledRecords: ConsumerRecords[Array[Byte], Array[Byte]],
    consumerGroupMetadata: Option[ConsumerGroupMetadata]
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
        _ <- ZIO.foreachParDiscard(streams) { streamControl =>
               val tp      = streamControl.tp
               val records = polledRecords.records(tp)
               if (records.isEmpty) {
                 streamControl.offerRecords(Chunk.empty)
               } else {
                 val builder  = ChunkBuilder.make[Record](records.size())
                 val iterator = records.iterator()
                 while (iterator.hasNext) {
                   val consumerRecord = iterator.next()
                   builder +=
                     CommittableRecord[Array[Byte], Array[Byte]](
                       record = consumerRecord,
                       commitHandle = committer.commit,
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
    if (settings.hasGroupId)
      consumer.rebalanceListenerAccess(c => ZIO.attempt(c.groupMetadata())).fold(_ => None, Some(_))
    else ZIO.none

  /** @return the topic-partitions for which received records should be ignored */
  private def doSeekForNewPartitions(c: ByteArrayKafkaConsumer, tps: Set[TopicPartition]): Task[Set[TopicPartition]] =
    settings.offsetRetrieval match {
      case OffsetRetrieval.Auto(_) => ZIO.succeed(Set.empty)
      case OffsetRetrieval.Manual(getOffsets, _) =>
        if (tps.isEmpty) ZIO.succeed(Set.empty)
        else
          getOffsets(tps).flatMap { offsets =>
            ZIO
              .attempt(offsets.foreach { case (tp, offset) => c.seek(tp, offset) })
              .as(offsets.keySet)
          }
    }

  /**
   * Pause partitions for which there is no demand and resume those for which there is now demand.
   */
  private def resumeAndPausePartitions(
    c: ByteArrayKafkaConsumer,
    requestedPartitions: Set[TopicPartition]
  ): Task[(Int, Int)] = ZIO.attempt {
    val assignment = c.assignment().asScala.toSet
    val toResume   = assignment intersect requestedPartitions
    val toPause    = assignment -- requestedPartitions

    if (toResume.nonEmpty) c.resume(toResume.asJava)
    if (toPause.nonEmpty) c.pause(toPause.asJava)

    (toResume.size, toPause.size)
  }

  private def doPoll(c: ByteArrayKafkaConsumer): Task[ConsumerRecords[Array[Byte], Array[Byte]]] =
    ZIO.attempt {
      val recordsOrNull = c.poll(settings.pollTimeout)
      if (recordsOrNull eq null) ConsumerRecords.empty[Array[Byte], Array[Byte]]()
      else recordsOrNull
    }
      // Recover from spurious auth failures:
      .retry(
        Schedule.recurWhileZIO[Any, Throwable] {
          case _: AuthorizationException | _: AuthenticationException =>
            consumerMetrics.observePollAuthError().as(true)
          case _ => ZIO.succeed(false)
        } &&
          settings.authErrorRetrySchedule
      )

  private def handlePoll(state: State): Task[State] = {
    for {
      partitionsToFetch  <- settings.fetchStrategy.selectPartitionsToFetch(state.assignedStreams)
      pendingCommitCount <- committer.pendingCommitCount
      _ <- ZIO.logDebug(
             s"Starting poll with ${state.pendingRequests.size} pending requests and" +
               s" ${pendingCommitCount} pending commits," +
               s" resuming $partitionsToFetch partitions"
           )
      _ <- currentStateRef.set(state)
      pollResult <-
        consumer.runloopAccess { c =>
          for {
            resumeAndPauseCounts <- resumeAndPausePartitions(c, partitionsToFetch)
            (toResumeCount, toPauseCount) = resumeAndPauseCounts

            pullDurationAndRecords <- doPoll(c).timed
            (pollDuration, polledRecords) = pullDurationAndRecords

            _ <- consumerMetrics.observePoll(toResumeCount, toPauseCount, pollDuration, polledRecords.count()) *>
                   diagnostics.emit {
                     val providedTps         = polledRecords.partitions().asScala.toSet
                     val requestedPartitions = state.pendingRequests.map(_.tp).toSet

                     DiagnosticEvent.Poll(
                       tpRequested = requestedPartitions,
                       tpWithData = providedTps,
                       tpWithoutData = requestedPartitions -- providedTps
                     )
                   }
            pollresult <- rebalanceCoordinator.getAndResetLastEvent.flatMap {
                            case RebalanceEvent(false, _, _, _, _) =>
                              // The fast track, rebalance listener was not invoked:
                              //   no assignment changes, no new commits, only new records.
                              ZIO.succeed(
                                PollResult(
                                  records = polledRecords,
                                  ignoreRecordsForTps = Set.empty,
                                  pendingRequests = state.pendingRequests,
                                  assignedStreams = state.assignedStreams,
                                  consumerGroupMetadata = None
                                )
                              )

                            case RebalanceEvent(true, assignedTps, revokedTps, lostTps, endedStreams) =>
                              // The slow track, the rebalance listener was invoked:
                              //   some partitions were assigned, revoked or lost,
                              //   some streams have ended.

                              val currentAssigned = c.assignment().asScala.toSet
                              val endedTps        = endedStreams.map(_.tp).toSet
                              for {
                                ignoreRecordsForTps   <- doSeekForNewPartitions(c, assignedTps)
                                consumerGroupMetadata <- getConsumerGroupMetadataIfAny

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
                                    !(lostTps.contains(tp) || revokedTps.contains(tp) || endedStreams
                                      .exists(_.tp == tp))
                                  }

                                // Remove committed offsets for partitions that are no longer assigned:
                                // NOTE: the type annotation is needed to keep the IntelliJ compiler happy.
                                _ <-
                                  committer.keepCommitsForPartitions(updatedAssignedStreams.map(_.tp).toSet): Task[Unit]

                                _ <- consumerMetrics.observeRebalance(
                                       currentAssigned.size,
                                       assignedTps.size,
                                       revokedTps.size,
                                       lostTps.size
                                     )
                                _ <- diagnostics.emit(
                                       Rebalance(
                                         revoked = revokedTps,
                                         assigned = assignedTps,
                                         lost = lostTps,
                                         ended = endedStreams.map(_.tp).toSet
                                       )
                                     )
                                // Ensure that all assigned partitions have a stream and no streams are present for unassigned streams
                                _ <-
                                  ZIO
                                    .logWarning(
                                      s"Not all assigned partitions have a (single) stream or vice versa. Assigned: ${currentAssigned.mkString(",")}, streams: ${updatedAssignedStreams.map(_.tp).mkString(",")}"
                                    )
                                    .when(
                                      currentAssigned != updatedAssignedStreams
                                        .map(_.tp)
                                        .toSet || currentAssigned.size != updatedAssignedStreams.size
                                    )
                              } yield Runloop.PollResult(
                                records = polledRecords,
                                ignoreRecordsForTps = ignoreRecordsForTps,
                                pendingRequests = updatedPendingRequests,
                                assignedStreams = updatedAssignedStreams,
                                consumerGroupMetadata = consumerGroupMetadata
                              )
                          }
          } yield pollresult
        }
      fulfillResult <- offerRecordsToStreams(
                         pollResult.assignedStreams,
                         pollResult.pendingRequests,
                         pollResult.ignoreRecordsForTps,
                         pollResult.records,
                         pollResult.consumerGroupMetadata
                       )
      _ <- committer.cleanupPendingCommits
      _ <- checkStreamPullInterval(pollResult.assignedStreams)
    } yield state.copy(
      pendingRequests = fulfillResult.pendingRequests,
      assignedStreams = pollResult.assignedStreams
    )
  }

  /**
   * Check each stream to see if it exceeded its pull interval. If so, halt it. In addition, if any stream has exceeded
   * its pull interval, shutdown the consumer.
   */
  private def checkStreamPullInterval(streams: Chunk[PartitionStreamControl]): ZIO[Any, Nothing, Unit] = {
    def logShutdown(stream: PartitionStreamControl): ZIO[Any, Nothing, Unit] =
      ZIO.logError(
        s"Stream for ${stream.tp} has not pulled chunks for more than $maxStreamPullInterval, shutting down. " +
          "Use ConsumerSettings.withMaxPollInterval or .withMaxStreamPullInterval to set a longer interval when " +
          "processing a batch of records needs more time."
      )

    for {
      now <- Clock.nanoTime
      anyExceeded <- ZIO.foldLeft(streams)(false) { case (acc, stream) =>
                       stream
                         .maxStreamPullIntervalExceeded(now)
                         .tap(ZIO.when(_)(logShutdown(stream)))
                         .tap(exceeded => if (exceeded) stream.halt else ZIO.unit)
                         .map(acc || _)
                     }
      _ <- shutdown.when(anyExceeded)
    } yield ()
  }

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
      case req: RunloopCommand.Request =>
        // Ignore request from streams that were ended or lost.
        ZIO.succeed(
          if (state.assignedStreams.exists(_.tp == req.tp)) state.addRequest(req)
          else state
        )
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
          val rebalanceListener = makeRebalanceListener
          ZIO
            .attempt(c.subscribe(pattern.pattern, rebalanceListener))
            .as(Chunk.empty)
        case SubscriptionState.Subscribed(_, Subscription.Topics(topics)) =>
          val rebalanceListener = makeRebalanceListener
          ZIO
            .attempt(c.subscribe(topics.asJava, rebalanceListener))
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
   *
   * Note that this method is executed on a dedicated single-thread Executor
   */
  private def run(initialState: State): ZIO[Scope, Throwable, Any] = {
    import Runloop.StreamOps

    ZStream
      .fromQueue(commandQueue)
      .takeWhile(_ != RunloopCommand.StopRunloop)
      .runFoldChunksDiscardZIO(initialState) { (state, commands) =>
        for {
          _ <- ZIO.logDebug(s"Processing ${commands.size} commands: ${commands.mkString(",")}")
          _ <- consumer.runloopAccess(committer.processQueuedCommits(_))
          streamCommands = commands.collect { case cmd: RunloopCommand.StreamCommand => cmd }
          stateAfterCommands <- ZIO.foldLeft(streamCommands)(state)(handleCommand)

          updatedStateAfterPoll <- shouldPoll(stateAfterCommands).flatMap {
                                     case true  => handlePoll(stateAfterCommands)
                                     case false => ZIO.succeed(stateAfterCommands)
                                   }
          // Immediately poll again, after processing all new queued commands
          _ <- ZIO.whenZIO(shouldPoll(updatedStateAfterPoll))(commandQueue.offer(RunloopCommand.Poll))
          // Save the current state for other parts of Runloop (read-only, for metrics only)
          _ <- currentStateRef.set(updatedStateAfterPoll)
        } yield updatedStateAfterPoll
      }
      .tapErrorCause(cause => ZIO.logErrorCause("Error in Runloop", cause))
      .onError(cause => partitionsHub.offer(Take.failCause(cause)))
  }

  def shouldPoll(state: State): UIO[Boolean] =
    committer.pendingCommitCount.map { pendingCommitCount =>
      state.subscriptionState.isSubscribed && (state.pendingRequests.nonEmpty || pendingCommitCount > 0 || state.assignedStreams.isEmpty)
    }

  private def observeRunloopMetrics(runloopMetricsSchedule: Schedule[Any, Unit, Long]): ZIO[Any, Nothing, Unit] = {
    val observe = for {
      currentState       <- currentStateRef.get
      commandQueueSize   <- commandQueue.size
      commitQueueSize    <- committer.queueSize
      pendingCommitCount <- committer.pendingCommitCount
      _ <- consumerMetrics
             .observeRunloopMetrics(currentState, commandQueueSize, commitQueueSize, pendingCommitCount)
    } yield ()

    observe
      .repeat(runloopMetricsSchedule)
      .unit
  }

  def registerExternalCommits(offsetBatch: OffsetBatch): Task[Unit] =
    committer.registerExternalCommits(offsetBatch.offsets)
}

object Runloop {
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
    assignedStreams: Chunk[PartitionStreamControl],
    consumerGroupMetadata: Option[ConsumerGroupMetadata]
  )
  private final case class RevokeResult(
    pendingRequests: Chunk[RunloopCommand.Request],
    assignedStreams: Chunk[PartitionStreamControl]
  )
  private final case class FulfillResult(
    pendingRequests: Chunk[RunloopCommand.Request]
  )

  private[consumer] def make(
    settings: ConsumerSettings,
    maxStreamPullInterval: Duration,
    maxRebalanceDuration: Duration,
    diagnostics: Diagnostics,
    consumer: ConsumerAccess,
    partitionsHub: Hub[Take[Throwable, PartitionAssignment]]
  ): URIO[Scope, Runloop] =
    for {
      _                  <- ZIO.addFinalizer(diagnostics.emit(Finalization.RunloopFinalized))
      commandQueue       <- ZIO.acquireRelease(Queue.unbounded[RunloopCommand])(_.shutdown)
      lastRebalanceEvent <- Ref.Synchronized.make[RebalanceEvent](RebalanceEvent.None)
      initialState = State.initial
      currentStateRef   <- Ref.make(initialState)
      sameThreadRuntime <- ZIO.runtime[Any].provideLayer(SameThreadRuntimeLayer)
      executor          <- ZIO.executor
      metrics = new ZioConsumerMetrics(settings.metricLabels)
      committer <- LiveCommitter
                     .make(
                       settings.commitTimeout,
                       diagnostics,
                       metrics,
                       commandQueue.offer(RunloopCommand.CommitAvailable).unit
                     )
      rebalanceCoordinator = new RebalanceCoordinator(
                               lastRebalanceEvent,
                               settings,
                               consumer,
                               maxRebalanceDuration,
                               currentStateRef.get.map(_.assignedStreams),
                               committer
                             )
      runloop = new Runloop(
                  settings = settings,
                  topLevelExecutor = executor,
                  sameThreadRuntime = sameThreadRuntime,
                  consumer = consumer,
                  commandQueue = commandQueue,
                  partitionsHub = partitionsHub,
                  diagnostics = diagnostics,
                  maxStreamPullInterval = maxStreamPullInterval,
                  currentStateRef = currentStateRef,
                  consumerMetrics = metrics,
                  rebalanceCoordinator = rebalanceCoordinator,
                  committer = committer
                )
      _ <- ZIO.logDebug("Starting Runloop")

      _ <- runloop.observeRunloopMetrics(settings.runloopMetricsSchedule).forkScoped

      // Run the entire loop on a dedicated thread to avoid executor shifts

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

  private[internal] final case class State(
    pendingRequests: Chunk[RunloopCommand.Request],
    assignedStreams: Chunk[PartitionStreamControl],
    subscriptionState: SubscriptionState
  ) {
    def addRequest(r: RunloopCommand.Request): State = copy(pendingRequests = pendingRequests :+ r)
  }

  private object State {
    val initial: State = State(
      pendingRequests = Chunk.empty,
      assignedStreams = Chunk.empty,
      subscriptionState = SubscriptionState.NotSubscribed
    )
  }

}
