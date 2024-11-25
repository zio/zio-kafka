package zio.kafka.consumer.internal
import org.apache.kafka.clients.consumer.OffsetAndMetadata
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.errors.RebalanceInProgressException
import zio.kafka.consumer.Consumer.CommitTimeout
import zio.kafka.consumer.diagnostics.{ DiagnosticEvent, Diagnostics }
import zio.kafka.consumer.internal.Committer.CommitOffsets
import zio.kafka.consumer.internal.LiveCommitter.Commit
import zio.{ durationLong, Cause, Chunk, Duration, Exit, Promise, Queue, Ref, Scope, Task, UIO, ZIO }

import scala.collection.mutable

private[consumer] final class LiveCommitter(
  commitQueue: Queue[Commit],
  commitTimeout: Duration,
  diagnostics: Diagnostics,
  consumerMetrics: ConsumerMetrics,
  onCommitAvailable: UIO[Unit],
  committedOffsetsRef: Ref[CommitOffsets],
  pendingCommits: Ref.Synchronized[Chunk[Commit]]
) extends Committer {

  /** This is the implementation behind the user facing api `Offset.commit`. */
  override val commit: Map[TopicPartition, OffsetAndMetadata] => Task[Unit] =
    offsets =>
      for {
        p <- Promise.make[Throwable, Unit]
        startTime = java.lang.System.nanoTime()
        _ <- commitQueue.offer(Commit(startTime, offsets, p))
        _ <- onCommitAvailable
        _ <- diagnostics.emit(DiagnosticEvent.Commit.Started(offsets))
        _ <- p.await.timeoutFail(CommitTimeout)(commitTimeout)
        endTime = java.lang.System.nanoTime()
        latency = (endTime - startTime).nanoseconds
        _ <- consumerMetrics.observeCommit(latency)
      } yield ()

  /**
   * WARNING: this method is used during a rebalance from the same-thread-runtime. This restricts what ZIO operations
   * may be used. Please see [[RebalanceCoordinator]] for more information.
   */
  override def processQueuedCommits(
    commitAsync: Map[TopicPartition, OffsetAndMetadata] => Task[Task[Map[TopicPartition, OffsetAndMetadata]]],
    executeOnEmpty: Boolean = false
  ): Task[Unit] = for {
    commits <- commitQueue.takeAll
    _       <- ZIO.logDebug(s"Processing ${commits.size} commits")
    _ <- ZIO.unless(commits.isEmpty && !executeOnEmpty) {
           val offsets = mergeCommitOffsets(commits)
           val offsetsWithMetaData = offsets.map { case (tp, offset) =>
             tp -> new OffsetAndMetadata(offset.offset + 1, offset.leaderEpoch, offset.metadata)
           }

           for {
             _                <- pendingCommits.update(_ ++ commits)
             startTime        <- ZIO.clockWith(_.nanoTime)
             getCommitResults <- commitAsync(offsetsWithMetaData)
             _ <- getCommitResults
                    .zipLeft(
                      for {
                        endTime <- ZIO.clockWith(_.nanoTime)
                        latency = (endTime - startTime).nanoseconds
                        offsetIncrease <- committedOffsetsRef.modify(_.addCommits(commits))
                        _ <- consumerMetrics.observeAggregatedCommit(latency, offsetIncrease).when(commits.nonEmpty)
                      } yield ()
                    )
                    .zipLeft(ZIO.foreachDiscard(commits)(_.cont.done(Exit.unit)))
                    .tap(offsetsWithMetaData => diagnostics.emit(DiagnosticEvent.Commit.Success(offsetsWithMetaData)))
                    .catchAllCause {
                      case Cause.Fail(_: RebalanceInProgressException, _) =>
                        for {
                          _ <- ZIO.logDebug(s"Rebalance in progress, commit for offsets $offsets will be retried")
                          _ <- commitQueue.offerAll(commits)
                          _ <- onCommitAvailable
                        } yield ()
                      case c =>
                        ZIO.foreachDiscard(commits)(_.cont.done(Exit.fail(c.squash))) <* diagnostics.emit(
                          DiagnosticEvent.Commit.Failure(offsets, c.squash)
                        )
                    }
                    // We don't wait for the completion of the commit here, because it
                    // will only complete once we poll again.
                    .forkDaemon
           } yield ()
         }
  } yield ()

  private def mergeCommitOffsets(commits: Chunk[Commit]): Map[TopicPartition, OffsetAndMetadata] =
    commits
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

  override def queueSize: UIO[Int] = commitQueue.size

  override def pendingCommitCount: UIO[Int] = pendingCommits.get.map(_.size)

  override def getPendingCommits: UIO[CommitOffsets] =
    pendingCommits.get.map(CommitOffsets.empty.addCommits(_)._2)

  override def cleanupPendingCommits: UIO[Unit] =
    pendingCommits.updateZIO(_.filterZIO(_.isPending))

  override def keepCommitsForPartitions(assignedPartitions: Set[TopicPartition]): UIO[Unit] =
    committedOffsetsRef.update(_.keepPartitions(assignedPartitions))

  override def getCommittedOffsets: UIO[CommitOffsets] = committedOffsetsRef.get
}

private[internal] object LiveCommitter {
  def make(
    commitTimeout: Duration,
    diagnostics: Diagnostics,
    consumerMetrics: ConsumerMetrics,
    onCommitAvailable: UIO[Unit]
  ): ZIO[Scope, Nothing, LiveCommitter] = for {
    pendingCommits      <- Ref.Synchronized.make(Chunk.empty[Commit])
    commitQueue         <- ZIO.acquireRelease(Queue.unbounded[Commit])(_.shutdown)
    committedOffsetsRef <- Ref.make(CommitOffsets.empty)
  } yield new LiveCommitter(
    commitQueue,
    commitTimeout,
    diagnostics,
    consumerMetrics,
    onCommitAvailable,
    committedOffsetsRef,
    pendingCommits
  )

  private[internal] final case class Commit(
    createdAt: NanoTime,
    offsets: Map[TopicPartition, OffsetAndMetadata],
    cont: Promise[Throwable, Unit]
  ) {
    @inline def isPending: UIO[Boolean] = cont.isDone.negate
  }

}
