package zio.kafka.consumer.internal
import org.apache.kafka.common.TopicPartition
import zio.kafka.consumer.internal.ConsumerAccess.ByteArrayKafkaConsumer
import zio.kafka.consumer.internal.RunloopRebalanceListener.{
  EndOffsetCommitPending,
  EndOffsetCommitted,
  EndOffsetNotCommitted,
  RebalanceEvent,
  StreamCompletionStatus
}
import zio.kafka.consumer.{ ConsumerSettings, RebalanceListener }
import zio.stream.ZStream
import zio.{ durationInt, Chunk, Duration, Ref, Task, UIO, ZIO }

/**
 * The Runloop's RebalanceListener gets notified of partitions that are assigned, revoked and lost
 *
 * Because this happens during the call to `poll()`, we communicate any results to the Runloop via a `Ref`
 *
 * When rebalanceSafeCommits is enabled, we await completion of all revoked partitions' streams and their commits before
 * continuing.
 */
private[internal] class RunloopRebalanceListener(
  lastRebalanceEvent: Ref.Synchronized[RebalanceEvent],
  settings: ConsumerSettings,
  consumer: ConsumerAccess,
  maxRebalanceDuration: Duration,
  getCurrentAssignedStreams: UIO[Chunk[PartitionStreamControl]],
  committer: Committer
) {
  private val commitTimeoutNanos = settings.commitTimeout.toNanos

  private val restartStreamsOnRebalancing = settings.restartStreamOnRebalancing
  private val rebalanceSafeCommits        = settings.rebalanceSafeCommits
  private val commitTimeout               = settings.commitTimeout

  // All code in this block is called from the rebalance listener and therefore runs on the same-thread-runtime. This
  // is because the Java kafka client requires us to invoke the consumer from the same thread that invoked the
  // rebalance listener.
  // Unfortunately the same-thread-runtime does not work for all ZIO operations. For example, `ZIO.timeout`,
  // `ZStream.repeat`, `Promise.await` on non-completed promises, and any other ZIO operation that shifts the work to
  // another thread cannot be used.

  // Time between polling the commit queue from the rebalance listener when `rebalanceSafeCommits` is enabled.
  private val commitQueuePollInterval = 100.millis

  // End streams from the rebalance listener.
  // When `rebalanceSafeCommits` is enabled, wait for consumed offsets to be committed.
  private def endStreams(streamsToEnd: Chunk[PartitionStreamControl]): Task[Any] =
    ZIO.unless(streamsToEnd.isEmpty) {
      for {
        _ <- ZIO.foreachDiscard(streamsToEnd)(_.end)
        _ <- consumer.rebalanceListenerAccess(doAwaitStreamCommits(_, streamsToEnd)).when(rebalanceSafeCommits)
      } yield ()
    }

  private def doAwaitStreamCommits(
    consumer: ByteArrayKafkaConsumer,
    streamsToEnd: Chunk[PartitionStreamControl]
  ): Task[Unit] = {
    val deadline = java.lang.System.nanoTime() + maxRebalanceDuration.toNanos - commitTimeoutNanos

    def timeToDeadlineMillis(): Long = (java.lang.System.nanoTime() - deadline) / 1000000L

    def completionStatusesAsString(completionStatuses: Chunk[StreamCompletionStatus]): String =
      "Revoked partitions: " + completionStatuses.map(_.toString).mkString("; ")

    def getStreamCompletionStatuses: UIO[Chunk[StreamCompletionStatus]] =
      for {
        committedOffsets        <- committer.getCommittedOffsets
        allPendingCommitOffsets <- committer.getPendingCommits.map(_.offsets.toSeq) // TODO toSeq efficient enough?
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
        completionStatuses <- getStreamCompletionStatuses
        _                  <- logStreamCompletionStatuses(completionStatuses)
      } yield ()

    def endingStreamsCompletedAndCommitsExist: UIO[Boolean] =
      for {
        completionStatuses <- getStreamCompletionStatuses
        _                  <- logStreamCompletionStatuses(completionStatuses)
      } yield completionStatuses.forall { status =>
        // A stream is complete when it never got any records, or when it committed the offset of the last consumed record
        status.lastPulledOffset.isEmpty || (status.streamEnded && status.endOffsetCommitStatus != EndOffsetNotCommitted)
      }

    def logFinalStreamCompletionStatuses(completed: Boolean): UIO[Unit] =
      if (completed)
        ZIO.logInfo("Continuing rebalance, all offsets of consumed records in the revoked partitions were committed.")
      else
        for {
          completionStatuses <- getStreamCompletionStatuses
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
    // (TODO is the pending part still true? Code did not reflect that)
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
      completed <-
        ZStream
          .fromZIO(blockingSleep(commitQueuePollInterval))
          .forever
          // Even if there is nothing to commit, continue to drive communication with the broker
          // so that commits can complete and the streams can make progress, by setting
          // executeOnEmpty = true
          .tap(_ => committer.handleNewCommits(consumer, executeOnEmpty = true))
          .takeWhile(_ => java.lang.System.nanoTime() <= deadline)
          .mapZIO(_ => endingStreamsCompletedAndCommitsExist)
          .takeUntil(completed => completed)
          .runLast
          .map(_.getOrElse(false))
      _ <- logFinalStreamCompletionStatuses(completed)
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

  def toRebalanceListener: RebalanceListener = RebalanceListener(
    onAssigned = assignedTps =>
      for {
        rebalanceEvent <- lastRebalanceEvent.get
        _ <- ZIO.logDebug {
               val sameRebalance = if (rebalanceEvent.wasInvoked) " in same rebalance" else ""
               s"${assignedTps.size} partitions are assigned$sameRebalance"
             }
        assignedStreams <- getCurrentAssignedStreams
        streamsToEnd = if (restartStreamsOnRebalancing && !rebalanceEvent.wasInvoked) assignedStreams
                       else Chunk.empty
        _ <- endStreams(streamsToEnd)
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
        assignedStreams <- getCurrentAssignedStreams
        streamsToEnd = if (restartStreamsOnRebalancing && !rebalanceEvent.wasInvoked) assignedStreams
                       else assignedStreams.filter(control => revokedTps.contains(control.tp))
        _ <- endStreams(streamsToEnd)
        _ <- lastRebalanceEvent.set(rebalanceEvent.onRevoked(revokedTps, endedStreams = streamsToEnd))
        _ <- ZIO.logTrace("onRevoked done")
      } yield (),
    onLost = lostTps =>
      for {
        _               <- ZIO.logDebug(s"${lostTps.size} partitions are lost")
        rebalanceEvent  <- lastRebalanceEvent.get
        assignedStreams <- getCurrentAssignedStreams
        lostStreams = assignedStreams.filter(control => lostTps.contains(control.tp))
        _ <- ZIO.foreachDiscard(lostStreams)(_.lost)
        _ <- lastRebalanceEvent.set(rebalanceEvent.onLost(lostTps, lostStreams))
        _ <- ZIO.logTrace(s"onLost done")
      } yield ()
  )
}

private[internal] object RunloopRebalanceListener {

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
      s"$tp: " +
        s"${if (streamEnded) "stream ended" else "stream is running"}, " +
        s"last pulled offset=${lastPulledOffset.getOrElse("none")}, " +
        endOffsetCommitStatus
  }

  final case class RebalanceEvent(
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

  object RebalanceEvent {
    val None: RebalanceEvent =
      RebalanceEvent(wasInvoked = false, Set.empty, Set.empty, Set.empty, Chunk.empty)
  }
}
