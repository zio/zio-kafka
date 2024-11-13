package zio.kafka.consumer.internal
import org.apache.kafka.clients.consumer.{OffsetAndMetadata, OffsetCommitCallback}
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.errors.RebalanceInProgressException
import zio.kafka.consumer.Consumer.CommitTimeout
import zio.kafka.consumer.diagnostics.{DiagnosticEvent, Diagnostics}
import zio.kafka.consumer.internal.Committer.CommitOffsets
import zio.kafka.consumer.internal.LiveCommitter.Commit
import zio.{Chunk, Duration, Exit, Promise, Queue, Ref, Runtime, Scope, Task, UIO, Unsafe, ZIO, durationLong}

import java.util
import java.util.{Map => JavaMap}
import scala.collection.mutable
import scala.jdk.CollectionConverters._

private[consumer] final class LiveCommitter(
  commitQueue: Queue[Commit],
  commitTimeout: Duration,
  diagnostics: Diagnostics,
  consumerMetrics: ConsumerMetrics,
  onCommitAvailable: UIO[Unit],
  committedOffsetsRef: Ref[CommitOffsets],
  sameThreadRuntime: Runtime[Any],
  pendingCommits: Ref[Chunk[Commit]]
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
   * Takes commits from the queue, commits them and adds them to pending commits
   *
   * If the queue is empty, nothing happens, unless executeOnEmpty is true.
   *
   * @param consumer
   *   Consumer with exclusive access
   * @param executeOnEmpty
   *   Execute commitAsync() even if there are no commits
   */
  override def processQueuedCommits(
    commitAsync: (JavaMap[TopicPartition, OffsetAndMetadata], OffsetCommitCallback) => Task[Unit],
    executeOnEmpty: Boolean = false
  ): Task[Unit] = for {
    commits <- commitQueue.takeAll
    _       <- ZIO.logDebug(s"Processing ${commits.size} commits")
    _ <- ZIO.unless(commits.isEmpty && !executeOnEmpty) {
           val (offsets, callback, onFailure) = asyncCommitParameters(commits)
           pendingCommits.update(_ ++ commits) *>
             // We don't wait for the completion of the commit here, because it
             // will only complete once we poll again.
             commitAsync(offsets, callback)
               .catchAll(onFailure)
         }
  } yield ()

  /** Merge commits and prepare parameters for calling `consumer.commitAsync`. */
  private def asyncCommitParameters(
    commits: Chunk[Commit]
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
          _ <- onCommitAvailable
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

  override def queueSize: UIO[Int] = commitQueue.size

  override def pendingCommitCount: UIO[Int] = pendingCommits.get.map(_.size)

  override def getPendingCommits: UIO[CommitOffsets] =
    pendingCommits.get.map(CommitOffsets.empty.addCommits(_)._2)

  override def updatePendingCommitsAfterPoll: UIO[Unit] =
    pendingCommits.get.flatMap(ZIO.filter(_)(_.isPending)).flatMap(pendingCommits.set)

  override def pruneCommittedOffsets(assignedPartitions: Set[TopicPartition]): UIO[Unit] =
    committedOffsetsRef.update(_.keepPartitions(assignedPartitions))

  override def getCommittedOffsets: UIO[CommitOffsets] = committedOffsetsRef.get
}

private[internal] object LiveCommitter {
  def make(
    commitTimeout: Duration,
    diagnostics: Diagnostics,
    consumerMetrics: ConsumerMetrics,
    onCommitAvailable: UIO[Unit],
    sameThreadRuntime: Runtime[Any]
  ): ZIO[Scope, Nothing, LiveCommitter] = for {
    pendingCommits      <- Ref.make(Chunk.empty[Commit])
    commitQueue         <- ZIO.acquireRelease(Queue.unbounded[Commit])(_.shutdown)
    committedOffsetsRef <- Ref.make(CommitOffsets.empty)
  } yield new LiveCommitter(
    commitQueue,
    commitTimeout,
    diagnostics,
    consumerMetrics,
    onCommitAvailable,
    committedOffsetsRef,
    sameThreadRuntime,
    pendingCommits
  )

  private[internal] final case class Commit(
    createdAt: NanoTime,
    offsets: Map[TopicPartition, OffsetAndMetadata],
    cont: Promise[Throwable, Unit]
  ) {
    @inline def isDone: UIO[Boolean]    = cont.isDone
    @inline def isPending: UIO[Boolean] = isDone.negate
  }

}
