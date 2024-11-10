package zio.kafka.consumer.internal
import org.apache.kafka.clients.consumer.{ OffsetAndMetadata, OffsetCommitCallback }
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.errors.RebalanceInProgressException
import zio.kafka.consumer.Consumer.CommitTimeout
import zio.kafka.consumer.ConsumerSettings
import zio.kafka.consumer.diagnostics.{ DiagnosticEvent, Diagnostics }
import zio.kafka.consumer.internal.Committer.{ Commit, CommitOffsets }
import zio.kafka.consumer.internal.ConsumerAccess.ByteArrayKafkaConsumer
import zio.{ durationLong, Chunk, Exit, Promise, Queue, Ref, Runtime, Scope, Task, UIO, Unsafe, ZIO }

import java.lang.Math.max
import java.util
import java.util.{ Map => JavaMap }
import scala.collection.mutable
import scala.jdk.CollectionConverters._

private[consumer] final class Committer(
  commitQueue: Queue[Commit],
  settings: ConsumerSettings,
  diagnostics: Diagnostics,
  consumerMetrics: ConsumerMetrics,
  onCommitAvailable: UIO[Unit],
  committedOffsetsRef: Ref[CommitOffsets],
  sameThreadRuntime: Runtime[Any],
  pendingCommits: Ref[Chunk[Commit]] // TODO make Commit internal
) {
  private val commitTimeout = settings.commitTimeout

  /** This is the implementation behind the user facing api `Offset.commit`. */
  val commit: Map[TopicPartition, OffsetAndMetadata] => Task[Unit] =
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
  def handleNewCommits(consumer: ByteArrayKafkaConsumer, executeOnEmpty: Boolean = false): Task[Unit] = for {
    commits <- commitQueue.takeAll
    _       <- ZIO.logDebug(s"Processing ${commits.size} commits")
    _ <- ZIO.unless(commits.isEmpty && !executeOnEmpty) {
           val (offsets, callback, onFailure) = asyncCommitParameters(commits)
           pendingCommits.update(_ ++ commits) *>
             // We don't wait for the completion of the commit here, because it
             // will only complete once we poll again.
             ZIO
               .attempt(consumer.commitAsync(offsets, callback))
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

  def pendingCommitCount: UIO[Int] = pendingCommits.get.map(_.size)

  def queueSize: UIO[Int] = commitQueue.size

  def updatePendingCommitsAfterPoll: UIO[Unit] =
    pendingCommits.get.flatMap(ZIO.filter(_)(_.isPending)).flatMap(pendingCommits.set)

  def pruneCommittedOffsets(assignedPartitions: Set[TopicPartition]): UIO[Unit] =
    committedOffsetsRef.update(_.keepPartitions(assignedPartitions))

  def getCommittedOffsets: UIO[CommitOffsets] = committedOffsetsRef.get

  def getPendingCommits: UIO[CommitOffsets] =
    pendingCommits.get.map(CommitOffsets.empty.addCommits(_)._2)
}

private[internal] object Committer {
  def make(
    settings: ConsumerSettings,
    diagnostics: Diagnostics,
    consumerMetrics: ConsumerMetrics,
    onCommitAvailable: UIO[Unit],
    sameThreadRuntime: Runtime[Any]
  ): ZIO[Any with Scope, Nothing, Committer] = for {
    pendingCommits      <- Ref.make(Chunk.empty[Commit])
    commitQueue         <- ZIO.acquireRelease(Queue.unbounded[Commit])(_.shutdown)
    committedOffsetsRef <- Ref.make(CommitOffsets.empty)
  } yield new Committer(
    commitQueue,
    settings,
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

  // package private for unit testing
  private[internal] final case class CommitOffsets(offsets: Map[TopicPartition, Long]) {

    /** Returns an estimate of the total offset increase, and a new `CommitOffsets` with the given offsets added. */
    def addCommits(c: Chunk[Commit]): (Long, CommitOffsets) = {
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
