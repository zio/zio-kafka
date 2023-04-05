package zio.kafka.consumer.internal

import org.apache.kafka.clients.consumer._
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.errors.RebalanceInProgressException
import zio._
import zio.kafka.consumer.Consumer.{ OffsetRetrieval, RunloopTimeout }
import zio.kafka.consumer.diagnostics.{ DiagnosticEvent, Diagnostics }
import zio.kafka.consumer.internal.ConsumerAccess.ByteArrayKafkaConsumer
import zio.kafka.consumer.internal.Runloop._
import zio.kafka.consumer.{ CommittableRecord, RebalanceConsumer, RebalanceListener, Subscription }
import zio.stream._

import java.util
import java.util.{ Map => JavaMap }
import scala.collection.mutable
import scala.jdk.CollectionConverters._

/**
 * Runloop is the heart of the zio-kafka consumer.
 *
 * ## Stream management
 *
 *   - When a partition gets assigned manually or by the broker, a new stream is started.
 *   - When a partition is revoked by the broker, the stream is ended.
 *   - When a partition is reported as lost, the stream is interrupted.
 *
 * ## Fetching data
 *
 *   - Streams that needs data request this via a [[Request]] command to the command-queue.
 *   - Partitions for which no data is needed are paused. This backpressure prevents unnecessary buffering of data.
 *
 * ## Poll-loop
 *
 * The poll-loop continuously polls the broker for new data. Since polling is also needed for learning about partition
 * assignment changes, or for completing commits, polling also continuous when no partitions are assigned, or when there
 * are pending commits.
 *
 * When all streams stop processing, polling stops so that the broker can detect that this Kafka client is stalled.
 *
 * ## Rebalance listener
 *
 * The rebalance listener runs during a poll to the broker. It is used to track changes to partition assignments.
 * Partitions can be assigned, revoked or lost.
 *
 * When a partition is revoked, the stream that handles it will be ended (signal the stream that no more data will be
 * available). Even though there is no more data, the stream still needs to complete processing the messages it already
 * got.
 *
 * ### Rebalance listener - Commit-loop
 *
 * When `rebalanceSafeCommits` is `true`, we wait for a revoked stream to commit offsets from within the rebalance
 * listener callback. This gives the program a chance to commit offsets before its partition is given to another
 * consumer.
 *
 * While the rebalance listener is waiting, new commits must still be send to the broker. In addition we need to
 * continue polling the broker so that we know that earlier commits completed. For both we use commitAsync (in the
 * second case with an empty map of offsets). This forms the commit-loop.
 *
 * The commit-loop ends when the offsets have been committed or a time out occured.
 *
 * ## The command-queue and the commit-queue
 *
 * TODO: Move this document to a central place.
 */
// Disable zio-intellij's inspection `SimplifyWhenInspection` because its suggestion is not
// equivalent performance-wise.
//noinspection SimplifyWhenInspection
private[consumer] final class Runloop private (
  sameThreadRuntime: Runtime[Any],
  hasGroupId: Boolean,
  consumer: ConsumerAccess,
  pollTimeout: Duration,
  runloopTimeout: Duration,
  commandQueue: Queue[RunloopCommand],
  commitQueue: Queue[Commit],
  rebalanceListenerEvent: Ref[RebalanceEvent],
  val partitionsQueue: Queue[Take[Throwable, (TopicPartition, Stream[Throwable, ByteArrayCommittableRecord])]],
  diagnostics: Diagnostics,
  offsetRetrieval: OffsetRetrieval,
  userRebalanceListener: RebalanceListener,
  restartStreamsOnRebalancing: Boolean,
  rebalanceSafeCommits: Boolean,
  currentStateRef: Ref[State]
) {

  private def newPartitionStream(tp: TopicPartition): UIO[PartitionStreamControl] =
    PartitionStreamControl.newPartitionStream(tp, commandQueue, diagnostics)

  def stopConsumption: UIO[Unit] =
    commandQueue.offer(RunloopCommand.StopAllStreams).unit

  def changeSubscription(
    subscription: Option[Subscription]
  ): Task[Unit] =
    Promise
      .make[Throwable, Unit]
      .flatMap { cont =>
        commandQueue.offer(RunloopCommand.ChangeSubscription(subscription, cont)) *>
          cont.await
      }
      .unit
      .uninterruptible

  private val rebalanceListener: RebalanceListener = {
    val emitDiagnostics = RebalanceListener(
      (assigned, _) => diagnostics.emit(DiagnosticEvent.Rebalance.Assigned(assigned)),
      (revoked, _) => diagnostics.emit(DiagnosticEvent.Rebalance.Revoked(revoked)),
      (lost, _) => diagnostics.emit(DiagnosticEvent.Rebalance.Lost(lost))
    )

    val endRevokedStreamsRebalancingListener = RebalanceListener(
      onAssigned = (assigned, _) =>
        for {
          _              <- ZIO.logDebug(s"${assigned.size} partitions are assigned")
          rebalanceEvent <- rebalanceListenerEvent.get
          state          <- currentStateRef.get
          streamsToEnd = if (restartStreamsOnRebalancing && !rebalanceEvent.wasInvoked) state.assignedStreams
                         else Chunk.empty
          pendingCommits <- consumer.rebalanceListenerAccess { consumer =>
                              endStreams(state, consumer, streamsToEnd, rebalanceSafeCommits)
                            }
          _ <- rebalanceListenerEvent.set(rebalanceEvent.onAssigned(assigned, pendingCommits, streamsToEnd))
          _ <- ZIO.logTrace("onAssigned done")
        } yield (),
      onRevoked = (revokedTps, _) =>
        for {
          _              <- ZIO.logDebug(s"${revokedTps.size} partitions are revoked")
          rebalanceEvent <- rebalanceListenerEvent.get
          state          <- currentStateRef.get
          streamsToEnd = if (restartStreamsOnRebalancing && !rebalanceEvent.wasInvoked) state.assignedStreams
                         else state.assignedStreams.filter(control => revokedTps.contains(control.tp))
          pendingCommits <- consumer.rebalanceListenerAccess { consumer =>
                              endStreams(state, consumer, streamsToEnd, rebalanceSafeCommits)
                            }
          _ <- rebalanceListenerEvent.set(rebalanceEvent.onRevokedOrLost(pendingCommits, streamsToEnd))
          _ <- ZIO.logTrace("onRevoked done")
        } yield (),
      onLost = (lostTps, _) =>
        for {
          _              <- ZIO.logDebug(s"${lostTps.size} partitions are lost")
          rebalanceEvent <- rebalanceListenerEvent.get
          state          <- currentStateRef.get
          (lostStreams, remainingStreams) = state.assignedStreams.partition(control => lostTps.contains(control.tp))
          _ <- ZIO.foreachDiscard(lostStreams)(_.lost())
          streamsToEnd = if (restartStreamsOnRebalancing && !rebalanceEvent.wasInvoked) remainingStreams
                         else Chunk.empty
          pendingCommits <- consumer.rebalanceListenerAccess { consumer =>
                              endStreams(state, consumer, streamsToEnd, rebalanceSafeCommits)
                            }
          _ <- rebalanceListenerEvent.update(_.onRevokedOrLost(pendingCommits, streamsToEnd))
          _ <- ZIO.logTrace(s"onLost done")
        } yield ()
    )

    emitDiagnostics ++ endRevokedStreamsRebalancingListener ++ userRebalanceListener
  }

  /**
   * Ends streams, optionally waiting for consumed offsets to be committed.
   *
   * @return
   *   all commits that were created while waiting
   */
  private def endStreams(
    state: State,
    consumer: ByteArrayKafkaConsumer,
    streamsToEnd: Chunk[PartitionStreamControl],
    awaitStreamCommits: Boolean
  ): Task[Chunk[Commit]] =
    if (streamsToEnd.nonEmpty) {
      for {
        _ <- ZIO.foreachDiscard(streamsToEnd)(_.end())
        pendingCommits <- if (awaitStreamCommits) doAwaitStreamCommits(state, consumer, streamsToEnd)
                          else ZIO.succeed(Chunk.empty)
      } yield pendingCommits
    } else {
      ZIO.succeed(Chunk.empty)
    }

  private def doAwaitStreamCommits(
    state: State,
    consumer: ByteArrayKafkaConsumer,
    streamsToEnd: Chunk[PartitionStreamControl]
  ): Task[Chunk[Commit]] =
    // When the queue is empty we still need to call commit (with 0 offsets) so that we poll
    // the broker and earlier commits can complete.
    // We cannot use ZStream.fromQueue because that will emit nothing when the queue is empty.
    ZStream
      .fromZIO(commitQueue.takeAll)
      .tap(handleCommitsDuringWait(consumer))
      .forever
      // TODO: remove following delay (perhaps replace `forever` with a repeat)
      // .tap(_ => ZIO.sleep(100.millis))
      .scan(Chunk.empty[Commit])(_ ++ _)
      .tap(_ => ZIO.logTrace(s"Waiting for ${streamsToEnd.size} streams to end"))
      .takeUntilZIO { commits =>
        val sentCommits = state.pendingCommits ++ commits
        val sentOffsets = state.committedOffsets ++ sentCommits
          .map(_.offsets)
          .foldLeft(Map.empty[TopicPartition, Long])(_ ++ _)
        for {
          _ <-
            ZIO.logDebug(
              s"state.committedOffsets: ${state.committedOffsets}\nstate.pendingCommits: ${state.pendingCommits}\ncommits: $commits"
            )
          allCommitted <- ZIO.forall(streamsToEnd)(_.lasOffsetIsIn(sentOffsets))
        } yield allCommitted
      }
      .tap(_ => ZIO.attempt(consumer.commitSync(Map.empty[TopicPartition, OffsetAndMetadata].asJava, runloopTimeout)))
      .run(ZSink.last)
      .map(_.getOrElse(Chunk.empty))
      // .timeoutFail(new RuntimeException("Timeout waiting for streams to end"))(runloopTimeout)
      .ensuring {
        ZIO.logInfo(s"Done waiting for ${streamsToEnd.size} streams to end")
      }
      .interruptible

  /**
   * Handle commits while waiting for streams to end.
   *
   * We need to ensure the streams can end. This is only possible if the commits these streams started complete. The
   * commits complete when the callback is invoked. The callback is invoked when the underlying consumer polls the
   * broker. This can be achieved by invoking `commitAsync`. Even when we pass no offsets, the broker will be polled and
   * callbacks will be called.
   */
  private def handleCommitsDuringWait(
    consumer: ByteArrayKafkaConsumer
  )(commits: Chunk[Commit]): UIO[Unit] = {
    val (offsets, callback, onFailure) = asyncCommitParameters(commits)
    // Note, as described above, we always call commit, even when offsets is empty.
    ZIO.logDebug(s"Async commit of ${offsets.size} offsets for ${commits.size} commits") *>
      ZIO.attempt {
        if (commits.nonEmpty) consumer.commitAsync(offsets, callback)
        else ()
      }
        .catchAll(onFailure)
//    (
//        if (commits.nonEmpty)
//          ZIO
//            .attempt(consumer.commitAsync(offsets, callback))
//            .catchAll(onFailure)
//        else {
//          ZIO.logDebug(s"Calling sync commit") *>
//            ZIO.attempt {
//              consumer.commitSync(java.util.Collections.emptyMap[TopicPartition, OffsetAndMetadata]())
//            }.orDie.unit <*
//            ZIO.logDebug(s"Done calling sync commit")
//        }
//      )
  }

  private def handleCommits(state: State, commits: Chunk[Commit]): UIO[State] =
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

  private def asyncCommitParameters(
    commits: Chunk[Commit]
  ): (JavaMap[TopicPartition, OffsetAndMetadata], OffsetCommitCallback, Throwable => UIO[Unit]) = {
    val offsets = commits
      .foldLeft(mutable.Map.empty[TopicPartition, Long]) { case (acc, commit) =>
        commit.offsets.foreach { case (tp, offset) =>
          acc += (tp -> acc.get(tp).map(_ max offset).getOrElse(offset))
        }
        acc
      }
      .toMap
    val offsetsWithMetaData = offsets.map { case (tp, offset) => tp -> new OffsetAndMetadata(offset + 1) }
    val cont                = (e: Exit[Throwable, Unit]) => ZIO.foreachDiscard(commits)(_.cont.done(e))
    val onSuccess = cont(Exit.unit) <* diagnostics.emit(DiagnosticEvent.Commit.Success(offsetsWithMetaData))
    val onFailure: Throwable => UIO[Unit] = {
      case _: RebalanceInProgressException =>
        for {
          _ <- ZIO.logDebug(s"Rebalance in progress, commit for offsets $offsets will be retried")
          _ <- commitQueue.offerAll(commits)
          _ <- commandQueue.offer(RunloopCommand.CommitAvailable)
        } yield ()
      case err: Throwable =>
        cont(Exit.fail(err)) <* diagnostics.emit(DiagnosticEvent.Commit.Failure(offsetsWithMetaData, err))
    }
    val callback =
      new OffsetCommitCallback {
        override def onComplete(offsets: util.Map[TopicPartition, OffsetAndMetadata], exception: Exception): Unit = {
          if (exception ne null) {
            println(
              s"onComplete callback: ${exception.getMessage}"
            )
          } else {
            println(
              s"onComplete callback: ${commits
                  .map(c => s"${c.offsets.map { case (tp, offset) => s"${tp.partition()}-$offset" }.mkString(", ")}")}"
            )
          }
          Unsafe.unsafe { implicit u =>
            sameThreadRuntime.unsafe
              .run(if (exception eq null) onSuccess else onFailure(exception))
              .getOrThrowFiberFailure()
          }
        }
      }
    (offsetsWithMetaData.asJava, callback, onFailure)
  }

  /** This is the implementation behind the user facing api `Offset.commit`. */
  private val commit: Map[TopicPartition, Long] => Task[Unit] =
    offsets =>
      for {
        p <- Promise.make[Throwable, Unit]
        _ <- commitQueue.offer(Commit(offsets, p))
        _ <- commandQueue.offer(RunloopCommand.CommitAvailable)
        _ <- diagnostics.emit(DiagnosticEvent.Commit.Started(offsets))
        _ <- p.await
      } yield ()

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
        committableRecords = {
          val acc             = ChunkBuilder.make[(PartitionStreamControl, Chunk[Record])](streams.size)
          val streamsIterator = streams.iterator
          while (streamsIterator.hasNext) {
            val streamControl = streamsIterator.next()
            val tp            = streamControl.tp
            val records       = polledRecords.records(tp)
            if (!records.isEmpty) {
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
              acc += (streamControl -> builder.result())
            }
          }
          acc.result()
        }
        _ <- ZIO
               .foreachDiscard(committableRecords) { case (streamControl, records) =>
                 streamControl.offerRecords(records)
               }
      } yield fulfillResult
    }
  }

  private def getConsumerGroupMetadataIfAny: UIO[Option[ConsumerGroupMetadata]] =
    if (hasGroupId) consumer.runloopAccess(c => ZIO.attempt(c.groupMetadata())).fold(_ => None, Some(_))
    else ZIO.none

  private def doSeekForNewPartitions(c: ByteArrayKafkaConsumer, tps: Set[TopicPartition]): Task[Set[TopicPartition]] =
    offsetRetrieval match {
      case OffsetRetrieval.Manual(getOffsets) =>
        getOffsets(tps)
          .tap(offsets => ZIO.foreachDiscard(offsets) { case (tp, offset) => ZIO.attempt(c.seek(tp, offset)) })
          .when(tps.nonEmpty)
          .as(tps)

      case OffsetRetrieval.Auto(_) =>
        ZIO.succeed(Set.empty)
    }

  /**
   * Pause partitions for which there is no demand and resume those for which there is now demand. Optimistically resume
   * partitions that are likely to need more data in the next poll.
   */
  private def resumeAndPausePartitions(
    c: ByteArrayKafkaConsumer,
    requestedPartitions: Set[TopicPartition],
    streams: Chunk[PartitionStreamControl]
  ): Unit = {
    val resumeTps = new java.util.ArrayList[TopicPartition](streams.size)
    val pauseTps  = new java.util.ArrayList[TopicPartition](streams.size)
    streams.foreach { stream =>
      val tp       = stream.tp
      val toResume = requestedPartitions.contains(tp) || stream.optimisticResume
      if (toResume) resumeTps.add(tp) else pauseTps.add(tp)
      stream.addPollHistory(toResume)
    }
    if (!resumeTps.isEmpty) c.resume(resumeTps)
    if (!pauseTps.isEmpty) c.pause(pauseTps)
  }

  private def handlePoll(state: State): Task[State] =
    for {
      _ <-
        ZIO.logTrace(
          s"Starting poll with ${state.pendingRequests.size} pending requests and ${state.pendingCommits.size} pending commits"
        )
      _ <- currentStateRef.set(state)
      _ <- rebalanceListenerEvent.set(RebalanceEvent.None)
      pollResult <-
        consumer.runloopAccess { c =>
          ZIO.suspend {
            val requestedPartitions = state.pendingRequests.map(_.tp).toSet

            resumeAndPausePartitions(c, requestedPartitions, state.assignedStreams)

            val polledRecords = {
              val records = c.poll(pollTimeout)
              if (records eq null) ConsumerRecords.empty[Array[Byte], Array[Byte]]() else records
            }

            rebalanceListenerEvent.get.flatMap {
              case RebalanceEvent(false, _, _, _) =>
                // The fast track: rebalance listener was not invoked, no changes, only new records.
                ZIO.succeed(Runloop.PollResult(polledRecords))

              case RebalanceEvent(true, newlyAssigned, pendingCommits, endedStreams) =>
                // Some partitions were revoked, lost or assigned,
                // some new commits might have been initiated,
                // some streams might have been ended.

                // When `restartStreamsOnRebalancing == true`,
                // all already assigned streams were ended.
                // Therefore, _all_ currently assigned tps are starting,
                // either because they are restarting, or because they
                // are new.
                val startingTps =
                  if (restartStreamsOnRebalancing) c.assignment().asScala.toSet
                  else newlyAssigned

                for {
                  ignoreRecordsForTps <- doSeekForNewPartitions(c, newlyAssigned)

                  _ <- diagnostics.emit {
                         val providedTps = polledRecords.partitions().asScala.toSet
                         DiagnosticEvent.Poll(
                           tpRequested = requestedPartitions,
                           tpWithData = providedTps,
                           tpWithoutData = requestedPartitions -- providedTps
                         )
                       }

                } yield Runloop.PollResult(
                  newCommits = pendingCommits,
                  startingTps = startingTps,
                  records = polledRecords,
                  ignoreRecordsForTps = ignoreRecordsForTps,
                  endedStreams = endedStreams
                )
            }
          }
        }
      startingStreams <-
        if (pollResult.startingTps.isEmpty) {
          ZIO.succeed(Chunk.empty[PartitionStreamControl])
        } else {
          ZIO
            .foreach(Chunk.fromIterable(pollResult.startingTps))(newPartitionStream)
            .tap { newStreams =>
              ZIO.logTrace(s"Offering partition assignment ${pollResult.startingTps}") *>
                partitionsQueue.offer(Take.chunk(Chunk.fromIterable(newStreams.map(_.tpStream))))
            }
        }
      runningStreams <- ZIO.filter(state.assignedStreams diff pollResult.endedStreams)(_.acceptsData)
      updatedStreams = runningStreams ++ startingStreams
      updatedPendingRequests = {
        val streamTps = updatedStreams.map(_.tp).toSet
        state.pendingRequests.filter(req => streamTps.contains(req.tp))
      }
      fulfillResult <- offerRecordsToStreams(
                         updatedStreams,
                         updatedPendingRequests,
                         pollResult.ignoreRecordsForTps,
                         pollResult.records
                       )
      (updatedPendingCommits, updatedCommittedOffsets) <- updateCommits(
                                                            state.pendingCommits ++ pollResult.newCommits,
                                                            state.committedOffsets
                                                          )
    } yield State(
      pendingRequests = fulfillResult.pendingRequests,
      pendingCommits = updatedPendingCommits,
      committedOffsets = updatedCommittedOffsets -- pollResult.endedStreams.map(_.tp),
      assignedStreams = updatedStreams,
      subscription = state.subscription
    )

  private def updateCommits(
    pendingCommits: Chunk[Commit],
    committedOffsets: Map[TopicPartition, Long]
  ): ZIO[Any, Nothing, (Chunk[Commit], Map[TopicPartition, Long])] =
    ZIO.foreach(pendingCommits)(commit => commit.isDone.map(commit -> _)).map { commitsWithDone =>
      val (doneCommits, updatedPendingCommits) = commitsWithDone.partitionMap { case (c, done) =>
        if (done) Left(c) else Right(c)
      }
      val updatedCommittedOffsets = committedOffsets ++ doneCommits.flatMap(_.offsets)
      (updatedPendingCommits, updatedCommittedOffsets)
    }

  private def handleCommand(state: State, cmd: RunloopCommand.StreamControl): Task[State] =
    cmd match {
      case req: RunloopCommand.Request =>
        ZIO.succeed(state.addRequest(req))
      case cmd @ RunloopCommand.ChangeSubscription(subscription, _) =>
        handleChangeSubscription(state, subscription).map { newAssignedStreams =>
          if (subscription.isDefined) {
            state.copy(
              assignedStreams = state.assignedStreams ++ newAssignedStreams,
              subscription = subscription
            )
          } else {
            state.copy(
              pendingRequests = Chunk.empty,
              pendingCommits = Chunk.empty,
              assignedStreams = Chunk.empty,
              subscription = None
            )
          }
        }
          .tapBoth(e => cmd.fail(e), _ => cmd.succeed)
          .uninterruptible
      case RunloopCommand.StopAllStreams =>
        // End all streams. Since we're waiting for the stream to end, there should be no pending commits.
        for {
          _ <- ZIO.logDebug("Graceful shutdown")
          _ <- consumer.runloopAccess { c =>
            endStreams(state, c, state.assignedStreams, awaitStreamCommits = false)
          }
          _ <- partitionsQueue.offer(Take.end)
          _ <- ZIO.logTrace("Graceful shutdown done")
        } yield state.copy(
          pendingRequests = Chunk.empty,
          pendingCommits = Chunk.empty,
          assignedStreams = Chunk.empty
        )
    }

  /**
   * @return
   *   any created streams
   */
  private def handleChangeSubscription(
    state: State,
    newSubscription: Option[Subscription]
  ): Task[Chunk[PartitionStreamControl]] =
    consumer.runloopAccess { c =>
      newSubscription match {
        case None =>
          // We assume that the invoker of this method will clear the state. This allows us to
          // ignore whatever happens to the state while in unsubscribe (the callbacks will be called).
          for {
            _ <- ZIO.logDebug(s"Unsubscribing, storing state: $state")
            _ <- currentStateRef.set(state)
            _ <- ZIO.attempt(c.unsubscribe())
            _ <- ZIO.logTrace("Unsubscribing done")
          } yield Chunk.empty
        case Some(Subscription.Pattern(pattern)) =>
          val rc = RebalanceConsumer.Live(c)
          ZIO
            .attempt(c.subscribe(pattern.pattern, rebalanceListener.toKafka(sameThreadRuntime, rc)))
            .as(Chunk.empty)
        case Some(Subscription.Topics(topics)) =>
          val rc = RebalanceConsumer.Live(c)
          ZIO
            .attempt(c.subscribe(topics.asJava, rebalanceListener.toKafka(sameThreadRuntime, rc)))
            .as(Chunk.empty)
        case Some(Subscription.Manual(topicPartitions)) =>
          // For manual subscriptions we have to do some manual work before starting the run loop
          for {
            _ <- ZIO.attempt(c.assign(topicPartitions.asJava))
            _ <- offsetRetrieval match {
                   case OffsetRetrieval.Manual(getOffsets) =>
                     getOffsets(topicPartitions).flatMap { offsets =>
                       ZIO.foreachDiscard(offsets) { case (tp, offset) => ZIO.attempt(c.seek(tp, offset)) }
                     }
                   case OffsetRetrieval.Auto(_) => ZIO.unit
                 }
            partitionStreams <- ZIO.foreach(Chunk.fromIterable(topicPartitions))(newPartitionStream)
            _                <- partitionsQueue.offer(Take.chunk(partitionStreams.map(_.tpStream)))
          } yield partitionStreams
      }
    }

  /**
   * Poll behavior:
   *   - Run until stop is set to true
   *   - Process commands as soon as they are queued, unless in the middle of polling
   *   - Process all currently queued commands before polling instead of one by one
   *   - Immediately after polling, if there are available commands, process them instead of waiting until some periodic
   *     trigger
   *   - Process the commitQueue (otherwise it would fill up when there are no rebalances)
   *   - Poll only when subscribed (leads to exceptions from the Apache Kafka Consumer if not)
   *   - Poll continuously when there are (still) unfulfilled requests or pending commits
   *   - Poll periodically when we are subscribed but do not have assigned streams yet. This happens after
   *     initialization and rebalancing
   */
  def run: ZIO[Scope, Throwable, Any] = {
    import Runloop.StreamOps

    ZStream
      .fromQueue(commandQueue)
      .timeoutFail[Throwable](RunloopTimeout)(runloopTimeout)
      .takeWhile(_ != RunloopCommand.StopRunloop)
      .runFoldChunksDiscardZIO(State.initial) { (state, commands) =>
        for {
          commits <- commitQueue.takeAll
          _ <- ZIO.logTrace(s"Processing ${commits.size} commits, ${commands.size} commands: ${commands.mkString(",")}")
          stateAfterCommits  <- handleCommits(state, commits)
          streamCommands = commands.collect { case cmd: RunloopCommand.StreamControl => cmd }
          stateAfterCommands <- ZIO.foldLeft(streamCommands)(stateAfterCommits)(handleCommand)

          updatedStateAfterPoll <- if (stateAfterCommands.shouldPoll) handlePoll(stateAfterCommands)
                                   else ZIO.succeed(stateAfterCommands)
          // Immediately poll again, after processing all new queued commands
          _ <- commandQueue.offer(RunloopCommand.Poll).when(updatedStateAfterPoll.shouldPoll)
        } yield updatedStateAfterPoll
      }
      .tapErrorCause(cause => ZIO.logErrorCause("Error in Runloop", cause))
      .onError(cause => partitionsQueue.offer(Take.failCause(cause)))
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

  // Internal parameters, should not be necessary to tune
  private val CommandQueueSize = 1024
  private val CommitQueueSize  = 1024

  private final case class PollResult(
    newCommits: Chunk[Commit],
    startingTps: Set[TopicPartition],
    records: ConsumerRecords[Array[Byte], Array[Byte]],
    ignoreRecordsForTps: Set[TopicPartition],
    endedStreams: Chunk[PartitionStreamControl]
  )
  private object PollResult {
    def apply(records: ConsumerRecords[Array[Byte], Array[Byte]]): PollResult =
      PollResult(
        newCommits = Chunk.empty,
        startingTps = Set.empty,
        records = records,
        ignoreRecordsForTps = Set.empty,
        endedStreams = Chunk.empty
      )
  }

  private final case class FulfillResult(
    pendingRequests: Chunk[RunloopCommand.Request]
  )

  private final case class RebalanceEvent(
    wasInvoked: Boolean,
    newlyAssigned: Set[TopicPartition],
    pendingCommits: Chunk[Commit],
    endedStreams: Chunk[PartitionStreamControl]
  ) {
    def onAssigned(
      assigned: Set[TopicPartition],
      commits: Chunk[Commit],
      streamsToEnd: Chunk[PartitionStreamControl]
    ): RebalanceEvent =
      RebalanceEvent(
        wasInvoked = true,
        newlyAssigned = newlyAssigned ++ assigned,
        pendingCommits = pendingCommits ++ commits,
        endedStreams = endedStreams ++ streamsToEnd
      )
    def onRevokedOrLost(
      commits: Chunk[Commit],
      streamsToEnd: Chunk[PartitionStreamControl]
    ): RebalanceEvent =
      copy(
        wasInvoked = true,
        pendingCommits = pendingCommits ++ commits,
        endedStreams = endedStreams ++ streamsToEnd
      )
  }

  private object RebalanceEvent {
    val None: RebalanceEvent = RebalanceEvent(wasInvoked = false, Set.empty, Chunk.empty, Chunk.empty)
  }

  final case class Commit(
                           offsets: Map[TopicPartition, Long],
                           cont: Promise[Throwable, Unit]
                         ) {
    @inline def isDone: UIO[Boolean] = cont.isDone
    @inline def isPending: UIO[Boolean] = isDone.negate
  }

  def make(
    hasGroupId: Boolean,
    consumer: ConsumerAccess,
    pollTimeout: Duration,
    diagnostics: Diagnostics,
    offsetRetrieval: OffsetRetrieval,
    userRebalanceListener: RebalanceListener,
    restartStreamsOnRebalancing: Boolean,
    rebalanceSafeCommits: Boolean,
    runloopTimeout: Duration
  ): ZIO[Scope, Throwable, Runloop] =
    for {
      commandQueue       <- ZIO.acquireRelease(Queue.bounded[RunloopCommand](CommandQueueSize))(_.shutdown)
      commitQueue            <- ZIO.acquireRelease(Queue.bounded[Runloop.Commit](CommitQueueSize))(_.shutdown)
      rebalanceListenerEvent <- Ref.make[RebalanceEvent](RebalanceEvent.None)
      partitions <- ZIO.acquireRelease(
                      Queue
                        .unbounded[
                          Take[Throwable, (TopicPartition, Stream[Throwable, ByteArrayCommittableRecord])]
                        ]
                    )(_.shutdown)
      currentStateRef   <- Ref.make(State.initial)
      sameThreadRuntime <- ZIO.runtime[Any].provideLayer(SameThreadRuntimeLayer)
      runloop = new Runloop(
                  sameThreadRuntime = sameThreadRuntime,
                  hasGroupId = hasGroupId,
                  consumer = consumer,
                  pollTimeout = pollTimeout,
                  runloopTimeout = runloopTimeout,
                  commandQueue = commandQueue,
                  commitQueue = commitQueue,
                  rebalanceListenerEvent = rebalanceListenerEvent,
                  partitionsQueue = partitions,
                  diagnostics = diagnostics,
                  offsetRetrieval = offsetRetrieval,
                  userRebalanceListener = userRebalanceListener,
                  restartStreamsOnRebalancing = restartStreamsOnRebalancing,
                  rebalanceSafeCommits = rebalanceSafeCommits,
                  currentStateRef = currentStateRef
                )
      _ <- ZIO.logDebug("Starting Runloop")

      // Run the entire loop on the a dedicated thread to avoid executor shifts
      executor <- RunloopExecutor.newInstance
      fib      <- ZIO.onExecutor(executor)(runloop.run).forkScoped

      _ <- ZIO.addFinalizer(
             ZIO.logTrace("Shutting down Runloop") *>
               commandQueue.offer(RunloopCommand.StopAllStreams) *>
               commandQueue.offer(RunloopCommand.StopRunloop) *>
               fib.join.orDie <*
               ZIO.logDebug("Shut down Runloop")
           )
    } yield runloop
}

private[internal] final case class State(
  pendingRequests: Chunk[RunloopCommand.Request],
  pendingCommits: Chunk[Commit],
  committedOffsets: Map[TopicPartition, Long],
  assignedStreams: Chunk[PartitionStreamControl],
  subscription: Option[Subscription]
) {
  def addCommits(c: Chunk[Commit]): State = copy(pendingCommits = pendingCommits ++ c)
  def addRequest(r: RunloopCommand.Request): State = copy(pendingRequests = pendingRequests :+ r)

  def isSubscribed: Boolean = subscription.isDefined

  def shouldPoll: Boolean =
    isSubscribed && (pendingRequests.nonEmpty || pendingCommits.nonEmpty || assignedStreams.isEmpty)
}

object State {
  val initial: State = State(
    pendingRequests = Chunk.empty,
    pendingCommits = Chunk.empty,
    committedOffsets = Map.empty,
    assignedStreams = Chunk.empty,
    subscription = None
  )
}
