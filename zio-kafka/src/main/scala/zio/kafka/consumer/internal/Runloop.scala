package zio.kafka.consumer.internal

import org.apache.kafka.clients.consumer._
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.errors.{ AuthenticationException, AuthorizationException, RebalanceInProgressException }
import zio._
import zio.kafka.consumer.Consumer.{ CommitTimeout, OffsetRetrieval }
import zio.kafka.consumer._
import zio.kafka.consumer.diagnostics.DiagnosticEvent.{ Finalization, Rebalance }
import zio.kafka.consumer.diagnostics.{ DiagnosticEvent, Diagnostics }
import zio.kafka.consumer.internal.ConsumerAccess.ByteArrayKafkaConsumer
import zio.kafka.consumer.internal.Runloop._
import zio.kafka.consumer.internal.RunloopAccess.PartitionAssignment
import zio.stream._

import java.lang.Math.max
import java.util
import java.util.{ Map => JavaMap }
import scala.collection.mutable
import scala.jdk.CollectionConverters._

//noinspection SimplifyWhenInspection,SimplifyUnlessInspection
private[consumer] final class Runloop private (
  settings: ConsumerSettings,
  topLevelExecutor: Executor,
  sameThreadRuntime: Runtime[Any],
  consumer: ConsumerAccess,
  commitQueue: Queue[Commit],
  commandQueue: Queue[RunloopCommand],
  lastRebalanceEvent: Ref.Synchronized[Runloop.RebalanceEvent],
  partitionsHub: Hub[Take[Throwable, PartitionAssignment]],
  diagnostics: Diagnostics,
  maxStreamPullInterval: Duration,
  maxRebalanceDuration: Duration,
  currentStateRef: Ref[State],
  committedOffsetsRef: Ref[CommitOffsets]
) {
  private val commitTimeout      = settings.commitTimeout
  private val commitTimeoutNanos = settings.commitTimeout.toNanos

  private val restartStreamsOnRebalancing = settings.restartStreamOnRebalancing
  private val rebalanceSafeCommits        = settings.rebalanceSafeCommits

  private val consumerMetrics = new ZioConsumerMetrics(settings.metricLabels)

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
    // All code in this block is called from the rebalance listener and therefore runs on the same-thread-runtime. This
    // is because the Java kafka client requires us to invoke the consumer from the same thread that invoked the
    // rebalance listener.
    // Unfortunately the same-thread-runtime does not work for all ZIO operations. For example, `ZIO.timeout`,
    // `ZStream.repeat`, `Promise.await` on non-completed promises, and any other ZIO operation that shifts the work to
    // another thread cannot be used.

    // Time between polling the commit queue from the rebalance listener when `rebalanceSafeCommits` is enabled.
    val commitQueuePollInterval = 100.millis

    // End streams from the rebalance listener.
    // When `rebalanceSafeCommits` is enabled, wait for consumed offsets to be committed.
    def endStreams(state: State, streamsToEnd: Chunk[PartitionStreamControl]): Task[Unit] =
      if (streamsToEnd.isEmpty) ZIO.unit
      else {
        for {
          _ <- ZIO.foreachDiscard(streamsToEnd)(_.end)
          _ <- if (rebalanceSafeCommits)
                 consumer.rebalanceListenerAccess(doAwaitStreamCommits(_, state, streamsToEnd))
               else ZIO.unit
        } yield ()
      }

    def doAwaitStreamCommits(
      consumer: ByteArrayKafkaConsumer,
      state: State,
      streamsToEnd: Chunk[PartitionStreamControl]
    ): Task[Unit] = {
      val deadline = java.lang.System.nanoTime() + maxRebalanceDuration.toNanos - commitTimeoutNanos

      def timeToDeadlineMillis(): Long = (deadline - java.lang.System.nanoTime()) / 1000000L

      val endingTps = streamsToEnd.map(_.tp).toSet

      def commitsOfEndingStreams(commits: Chunk[Runloop.Commit]): Chunk[Runloop.Commit] =
        commits.filter(commit => (commit.offsets.keySet intersect endingTps).nonEmpty)

      lazy val previousPendingCommits: Chunk[Commit] =
        commitsOfEndingStreams(state.pendingCommits)

      def commitAsync(commits: Chunk[Commit]): UIO[Unit] =
        if (commits.nonEmpty) {
          val (offsets, callback, onFailure) = asyncCommitParameters(commits)
          ZIO.logDebug(s"Async commit of ${offsets.size} offsets for ${commits.size} commits") *>
            ZIO.attempt(consumer.commitAsync(offsets, callback)).catchAll(onFailure)
        } else {
          // Continue to drive communication with the broker so that commits can complete and the streams can
          // make progress.
          ZIO.attempt(consumer.commitAsync(java.util.Collections.emptyMap(), null)).orDie
        }

      sealed trait EndOffsetCommitStatus
      case object EndOffsetNotCommitted  extends EndOffsetCommitStatus { override def toString = "not committed"  }
      case object EndOffsetCommitPending extends EndOffsetCommitStatus { override def toString = "commit pending" }
      case object EndOffsetCommitted     extends EndOffsetCommitStatus { override def toString = "committed"      }

      final case class StreamCompletionStatus(
        tp: TopicPartition,
        streamEnded: Boolean,
        lastPulledOffset: Option[Long],
        endOffsetCommitStatus: EndOffsetCommitStatus
      ) {
        override def toString: String =
          s"${tp}: " +
            s"${if (streamEnded) "stream ended" else "stream is running"}, " +
            s"last pulled offset=${lastPulledOffset.getOrElse("none")}, " +
            endOffsetCommitStatus
      }

      def completionStatusesAsString(completionStatuses: Chunk[StreamCompletionStatus]): String =
        "Revoked partitions: " + completionStatuses.map(_.toString).mkString("; ")

      def getStreamCompletionStatuses(newCommits: Chunk[Commit]): UIO[Chunk[StreamCompletionStatus]] =
        for {
          committedOffsets <- committedOffsetsRef.get
          allPendingCommitOffsets =
            (previousPendingCommits ++ commitsOfEndingStreams(newCommits)).flatMap(_.offsets).map {
              case (tp, offsetAndMetadata) => (tp, offsetAndMetadata.offset())
            }
          streamResults <-
            ZIO.foreach(streamsToEnd) { stream =>
              for {
                isDone           <- stream.completedPromise.isDone
                lastPulledOffset <- stream.lastPulledOffset
                endOffset        <- if (isDone) stream.completedPromise.await else ZIO.none

                endOffsetCommitStatus =
                  endOffset match {
                    case Some(endOffset) if committedOffsets.contains(stream.tp, endOffset.offset) =>
                      EndOffsetCommitted
                    case Some(endOffset) if allPendingCommitOffsets.contains((stream.tp, endOffset.offset)) =>
                      EndOffsetCommitPending
                    case _ => EndOffsetNotCommitted
                  }
              } yield StreamCompletionStatus(stream.tp, isDone, lastPulledOffset.map(_.offset), endOffsetCommitStatus)
            }
        } yield streamResults

      @inline
      def logStreamCompletionStatuses(completionStatuses: Chunk[StreamCompletionStatus]): UIO[Unit] = {
        val statusStrings = completionStatusesAsString(completionStatuses)
        ZIO.logInfo(
          s"Delaying rebalance until ${streamsToEnd.size} streams (of revoked partitions) have committed " +
            s"the offsets of the records they consumed. Deadline in ${timeToDeadlineMillis()}ms. $statusStrings"
        )
      }

      def logInitialStreamCompletionStatuses: UIO[Unit] =
        for {
          completionStatuses <- getStreamCompletionStatuses(newCommits = Chunk.empty)
          _                  <- logStreamCompletionStatuses(completionStatuses)
        } yield ()

      def endingStreamsCompletedAndCommitsExist(newCommits: Chunk[Commit]): UIO[Boolean] =
        for {
          completionStatuses <- getStreamCompletionStatuses(newCommits)
          _                  <- logStreamCompletionStatuses(completionStatuses)
        } yield completionStatuses.forall { status =>
          // A stream is complete when it never got any records, or when it committed the offset of the last consumed record
          status.lastPulledOffset.isEmpty || (status.streamEnded && status.endOffsetCommitStatus != EndOffsetNotCommitted)
        }

      def logFinalStreamCompletionStatuses(completed: Boolean, newCommits: Chunk[Commit]): UIO[Unit] =
        if (completed)
          ZIO.logInfo("Continuing rebalance, all offsets of consumed records in the revoked partitions were committed.")
        else
          for {
            completionStatuses <- getStreamCompletionStatuses(newCommits)
            statusStrings = completionStatusesAsString(completionStatuses)
            _ <-
              ZIO.logWarning(
                s"Exceeded deadline waiting for streams (of revoked partitions) to commit the offsets of " +
                  s"the records they consumed; the rebalance will continue. " +
                  s"This might cause another consumer to process some records again. $statusStrings"
              )
          } yield ()

      def commitSync: Task[Unit] =
        ZIO.attempt(consumer.commitSync(java.util.Collections.emptyMap(), commitTimeout))

      // Outline:
      // - Every `commitQueuePollInterval` until the deadline has been reached:
      //   - Get all commits from the commit queue.
      //   - Start an async commit for these commits.
      // - Collect all these new (pending) commits.
      // - repeat the above until:
      //   - All streams that were ended have completed their work, and
      //   - we have seen a completed or pending commit for all end-offsets.
      //     An end-offset of a stream is the offset of the last record given to that stream.
      // - Do a single sync commit without any offsets, this has the side-effect of blocking until all
      //   preceding async commits are complete (this requires kafka-client 3.6.0 or later).
      //   Because all commits created here (including those from non-ending streams) are now complete, we do not
      //   have to add them to the pending commits of the runloop state.
      //
      // Note, we cannot use ZStream.fromQueue because that will emit nothing when the queue is empty.
      // Instead, we poll the queue in a loop.
      for {
        _ <- logInitialStreamCompletionStatuses
        completedAndCommits <-
          ZStream
            .fromZIO(blockingSleep(commitQueuePollInterval) *> commitQueue.takeAll)
            .tap(commitAsync)
            .forever
            .takeWhile(_ => java.lang.System.nanoTime() <= deadline)
            .scan(Chunk.empty[Runloop.Commit])(_ ++ _)
            .mapZIO(commits => endingStreamsCompletedAndCommitsExist(commits).map((_, commits)))
            .takeUntil { case (completed, _) => completed }
            .runLast
            .map(_.getOrElse((false, Chunk.empty)))
        _ <- logFinalStreamCompletionStatuses(completedAndCommits._1, completedAndCommits._2)
        _ <- commitSync
        _ <- ZIO.logDebug(s"Done waiting for ${streamsToEnd.size} streams to end")
      } yield ()
    }

    // During a poll, the java kafka client might call each method of the rebalance listener 0 or 1 times.
    // We do not know the order in which the call-back methods are invoked.
    //
    // Ref `lastRebalanceEvent` is used to track what happens during the poll. Just before the poll the
    // `RebalanceEvent.None` is stored. Then during the poll, inside each method of the rebalance listener,
    // the ref is updated.
    //
    // Each method:
    // - emits a diagnostic event
    // - determines if this is the first method invoked during this poll (`rebalanceEvent.wasInvoked`) to
    //   make sure that the `restartStreamsOnRebalancing` feature is applied only once per poll
    // - ends streams that need to be ended
    // - updates `lastRebalanceEvent`
    //
    val recordRebalanceRebalancingListener = RebalanceListener(
      onAssigned = assignedTps =>
        for {
          rebalanceEvent <- lastRebalanceEvent.get
          _ <- ZIO.logDebug {
                 val sameRebalance = if (rebalanceEvent.wasInvoked) " in same rebalance" else ""
                 s"${assignedTps.size} partitions are assigned$sameRebalance"
               }
          state <- currentStateRef.get
          streamsToEnd = if (restartStreamsOnRebalancing && !rebalanceEvent.wasInvoked) state.assignedStreams
                         else Chunk.empty
          _ <- endStreams(state, streamsToEnd)
          _ <- lastRebalanceEvent.set(rebalanceEvent.onAssigned(assignedTps, endedStreams = streamsToEnd))
          _ <- ZIO.logTrace("onAssigned done")
        } yield (),
      onRevoked = revokedTps =>
        for {
          rebalanceEvent <- lastRebalanceEvent.get
          _ <- ZIO.logDebug {
                 val sameRebalance = if (rebalanceEvent.wasInvoked) " in same rebalance" else ""
                 s"${revokedTps.size} partitions are revoked$sameRebalance"
               }
          state <- currentStateRef.get
          streamsToEnd = if (restartStreamsOnRebalancing && !rebalanceEvent.wasInvoked) state.assignedStreams
                         else state.assignedStreams.filter(control => revokedTps.contains(control.tp))
          _ <- endStreams(state, streamsToEnd)
          _ <- lastRebalanceEvent.set(rebalanceEvent.onRevoked(revokedTps, endedStreams = streamsToEnd))
          _ <- ZIO.logTrace("onRevoked done")
        } yield (),
      onLost = lostTps =>
        for {
          _              <- ZIO.logDebug(s"${lostTps.size} partitions are lost")
          rebalanceEvent <- lastRebalanceEvent.get
          state          <- currentStateRef.get
          lostStreams = state.assignedStreams.filter(control => lostTps.contains(control.tp))
          _ <- ZIO.foreachDiscard(lostStreams)(_.lost)
          _ <- lastRebalanceEvent.set(rebalanceEvent.onLost(lostTps, lostStreams))
          _ <- ZIO.logTrace(s"onLost done")
        } yield ()
    )

    // Here we just want to avoid any executor shift if the user provided listener is the noop listener.
    val userRebalanceListener =
      settings.rebalanceListener match {
        case RebalanceListener.noop => RebalanceListener.noop
        case _                      => settings.rebalanceListener.runOnExecutor(topLevelExecutor)
      }

    RebalanceListener.toKafka(recordRebalanceRebalancingListener ++ userRebalanceListener, sameThreadRuntime)
  }

  /** This is the implementation behind the user facing api `Offset.commit`. */
  private val commit: Map[TopicPartition, OffsetAndMetadata] => Task[Unit] =
    offsets =>
      for {
        p <- Promise.make[Throwable, Unit]
        startTime = java.lang.System.nanoTime()
        _ <- commitQueue.offer(Runloop.Commit(java.lang.System.nanoTime(), offsets, p))
        _ <- commandQueue.offer(RunloopCommand.CommitAvailable)
        _ <- diagnostics.emit(DiagnosticEvent.Commit.Started(offsets))
        _ <- p.await.timeoutFail(CommitTimeout)(commitTimeout)
        endTime = java.lang.System.nanoTime()
        latency = (endTime - startTime).nanoseconds
        _ <- consumerMetrics.observeCommit(latency)
      } yield ()

  /** Merge commits and prepare parameters for calling `consumer.commitAsync`. */
  private def asyncCommitParameters(
    commits: Chunk[Runloop.Commit]
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
    val cont = (e: Exit[Throwable, Unit]) => ZIO.foreachDiscard(commits)(_.cont.done(e))
    // We assume the commit is started immediately after returning from this method.
    val startTime = java.lang.System.nanoTime()
    val onSuccess = {
      val endTime = java.lang.System.nanoTime()
      val latency = (endTime - startTime).nanoseconds
      for {
        offsetIncrease <- committedOffsetsRef.modify(_.addCommits(commits))
        _              <- consumerMetrics.observeAggregatedCommit(latency, offsetIncrease).when(commits.nonEmpty)
        result         <- cont(Exit.unit)
        _              <- diagnostics.emit(DiagnosticEvent.Commit.Success(offsetsWithMetaData))
      } yield result
    }
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
        override def onComplete(offsets: util.Map[TopicPartition, OffsetAndMetadata], exception: Exception): Unit =
          Unsafe.unsafe { implicit u =>
            sameThreadRuntime.unsafe.run {
              if (exception eq null) onSuccess else onFailure(exception)
            }
              .getOrThrowFiberFailure()
          }
      }
    (offsetsWithMetaData.asJava, callback, onFailure)
  }

  private def handleCommits(state: State, commits: Chunk[Runloop.Commit]): UIO[State] =
    if (commits.isEmpty) {
      ZIO.succeed(state)
    } else {
      val (offsets, callback, onFailure) = asyncCommitParameters(commits)
      val newState                       = state.addPendingCommits(commits)
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
    if (settings.hasGroupId) consumer.runloopAccess(c => ZIO.attempt(c.groupMetadata())).fold(_ => None, Some(_))
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
      partitionsToFetch <- settings.fetchStrategy.selectPartitionsToFetch(state.assignedStreams)
      _ <- ZIO.logDebug(
             s"Starting poll with ${state.pendingRequests.size} pending requests and" +
               s" ${state.pendingCommits.size} pending commits," +
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
            pollresult <- lastRebalanceEvent.getAndSet(RebalanceEvent.None).flatMap {
                            case RebalanceEvent(false, _, _, _, _) =>
                              // The fast track, rebalance listener was not invoked:
                              //   no assignment changes, no new commits, only new records.
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
                                    !(lostTps.contains(tp) || revokedTps.contains(tp) || endedStreams
                                      .exists(_.tp == tp))
                                  }

                                // Remove committed offsets for partitions that are no longer assigned:
                                // NOTE: the type annotation is needed to keep the IntelliJ compiler happy.
                                _ <-
                                  committedOffsetsRef
                                    .update(_.keepPartitions(updatedAssignedStreams.map(_.tp).toSet)): Task[Unit]

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
                                assignedStreams = updatedAssignedStreams
                              )
                          }
          } yield pollresult
        }
      fulfillResult <- offerRecordsToStreams(
                         pollResult.assignedStreams,
                         pollResult.pendingRequests,
                         pollResult.ignoreRecordsForTps,
                         pollResult.records
                       )
      updatedPendingCommits <- ZIO.filter(state.pendingCommits)(_.isPending)
      _                     <- checkStreamPullInterval(pollResult.assignedStreams)
    } yield state.copy(
      pendingRequests = fulfillResult.pendingRequests,
      pendingCommits = updatedPendingCommits,
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
   */
  private def run(initialState: State): ZIO[Scope, Throwable, Any] = {
    import Runloop.StreamOps

    ZStream
      .fromQueue(commandQueue)
      .takeWhile(_ != RunloopCommand.StopRunloop)
      .runFoldChunksDiscardZIO(initialState) { (state, commands) =>
        for {
          commitCommands <- commitQueue.takeAll
          _ <- ZIO.logDebug(
                 s"Processing ${commitCommands.size} commits," +
                   s" ${commands.size} commands: ${commands.mkString(",")}"
               )
          stateAfterCommits <- handleCommits(state, commitCommands)
          streamCommands = commands.collect { case cmd: RunloopCommand.StreamCommand => cmd }
          stateAfterCommands <- ZIO.foldLeft(streamCommands)(stateAfterCommits)(handleCommand)

          updatedStateAfterPoll <- if (stateAfterCommands.shouldPoll) handlePoll(stateAfterCommands)
                                   else ZIO.succeed(stateAfterCommands)
          // Immediately poll again, after processing all new queued commands
          _ <- if (updatedStateAfterPoll.shouldPoll) commandQueue.offer(RunloopCommand.Poll) else ZIO.unit
          // Save the current state for other parts of Runloop (read-only, for metrics only)
          _ <- currentStateRef.set(updatedStateAfterPoll)
        } yield updatedStateAfterPoll
      }
      .tapErrorCause(cause => ZIO.logErrorCause("Error in Runloop", cause))
      .onError(cause => partitionsHub.offer(Take.failCause(cause)))
  }

  private def observeRunloopMetrics(runloopMetricsSchedule: Schedule[Any, Unit, Long]): ZIO[Any, Nothing, Unit] = {
    val observe = for {
      currentState     <- currentStateRef.get
      commandQueueSize <- commandQueue.size
      commitQueueSize  <- commitQueue.size
      _ <- consumerMetrics
             .observeRunloopMetrics(currentState, commandQueueSize, commitQueueSize)
    } yield ()

    observe
      .repeat(runloopMetricsSchedule)
      .unit
  }
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
    def onAssigned(
      assigned: Set[TopicPartition],
      endedStreams: Chunk[PartitionStreamControl]
    ): RebalanceEvent =
      copy(
        wasInvoked = true,
        assignedTps = assignedTps ++ assigned,
        endedStreams = this.endedStreams ++ endedStreams
      )

    def onRevoked(
      revoked: Set[TopicPartition],
      endedStreams: Chunk[PartitionStreamControl]
    ): RebalanceEvent =
      copy(
        wasInvoked = true,
        assignedTps = assignedTps -- revoked,
        revokedTps = revokedTps ++ revoked,
        endedStreams = this.endedStreams ++ endedStreams
      )

    def onLost(lost: Set[TopicPartition], endedStreams: Chunk[PartitionStreamControl]): RebalanceEvent =
      copy(
        wasInvoked = true,
        assignedTps = assignedTps -- lost,
        lostTps = lostTps ++ lost,
        endedStreams = this.endedStreams ++ endedStreams
      )
  }

  private object RebalanceEvent {
    val None: RebalanceEvent =
      RebalanceEvent(wasInvoked = false, Set.empty, Set.empty, Set.empty, Chunk.empty)
  }

  private[internal] final case class Commit(
    createdAt: NanoTime,
    offsets: Map[TopicPartition, OffsetAndMetadata],
    cont: Promise[Throwable, Unit]
  ) {
    @inline def isDone: UIO[Boolean]    = cont.isDone
    @inline def isPending: UIO[Boolean] = isDone.negate
  }

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
      commitQueue        <- ZIO.acquireRelease(Queue.bounded[Runloop.Commit](settings.commitQueueSize))(_.shutdown)
      commandQueue       <- ZIO.acquireRelease(Queue.bounded[RunloopCommand](settings.commandQueueSize))(_.shutdown)
      lastRebalanceEvent <- Ref.Synchronized.make[Runloop.RebalanceEvent](Runloop.RebalanceEvent.None)
      initialState = State.initial
      currentStateRef     <- Ref.make(initialState)
      committedOffsetsRef <- Ref.make(CommitOffsets.empty)
      sameThreadRuntime   <- ZIO.runtime[Any].provideLayer(SameThreadRuntimeLayer)
      executor            <- ZIO.executor
      runloop = new Runloop(
                  settings = settings,
                  topLevelExecutor = executor,
                  sameThreadRuntime = sameThreadRuntime,
                  consumer = consumer,
                  commitQueue = commitQueue,
                  commandQueue = commandQueue,
                  lastRebalanceEvent = lastRebalanceEvent,
                  partitionsHub = partitionsHub,
                  diagnostics = diagnostics,
                  maxStreamPullInterval = maxStreamPullInterval,
                  maxRebalanceDuration = maxRebalanceDuration,
                  currentStateRef = currentStateRef,
                  committedOffsetsRef = committedOffsetsRef
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
    pendingCommits: Chunk[Runloop.Commit],
    assignedStreams: Chunk[PartitionStreamControl],
    subscriptionState: SubscriptionState
  ) {
    def addPendingCommits(c: Chunk[Runloop.Commit]): State = copy(pendingCommits = pendingCommits ++ c)
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

  // package private for unit testing
  private[internal] final case class CommitOffsets(offsets: Map[TopicPartition, Long]) {

    /** Returns an estimate of the total offset increase, and a new `CommitOffsets` with the given offsets added. */
    def addCommits(c: Chunk[Runloop.Commit]): (Long, CommitOffsets) = {
      val updatedOffsets = mutable.Map.empty[TopicPartition, Long]
      updatedOffsets.sizeHint(offsets.size)
      updatedOffsets ++= offsets
      var offsetIncrease = 0L
      c.foreach { commit =>
        commit.offsets.foreach { case (tp, offsetAndMeta) =>
          val offset = offsetAndMeta.offset()
          val maxOffset = updatedOffsets.get(tp) match {
            case Some(existingOffset) =>
              offsetIncrease += max(0L, offset - existingOffset)
              max(existingOffset, offset)
            case None =>
              // This partition was not committed to from this consumer yet. Therefore we do not know the offset
              // increase. A good estimate would be the poll size for this consumer, another okayish estimate is 0.
              // Lets go with the simplest for now: ```offsetIncrease += 0```
              offset
          }
          updatedOffsets += tp -> maxOffset
        }
      }
      (offsetIncrease, CommitOffsets(offsets = updatedOffsets.toMap))
    }

    def keepPartitions(tps: Set[TopicPartition]): CommitOffsets =
      CommitOffsets(offsets.filter { case (tp, _) => tps.contains(tp) })

    def contains(tp: TopicPartition, offset: Long): Boolean =
      offsets.get(tp).exists(_ >= offset)
  }

  private[internal] object CommitOffsets {
    val empty: CommitOffsets = CommitOffsets(Map.empty)
  }
}
