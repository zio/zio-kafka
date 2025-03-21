package zio.kafka.consumer.internal
import org.apache.kafka.clients.consumer.{ OffsetAndMetadata, OffsetCommitCallback }
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.errors.RebalanceInProgressException
import zio.kafka.consumer.Consumer.{ CommitTimeout, ConsumerDiagnostics }
import zio.kafka.consumer.internal.Committer.CommitOffsets
import zio.kafka.consumer.internal.ConsumerAccess.ByteArrayKafkaConsumer
import zio.kafka.consumer.internal.LiveCommitter.Commit
import zio._
import zio.kafka.consumer.diagnostics.DiagnosticEvent

import java.util.{ Map => JavaMap }
import scala.collection.mutable
import scala.jdk.CollectionConverters._

private[consumer] final class LiveCommitter(
  commitQueue: Queue[Commit],
  commitTimeout: Duration,
  diagnostics: ConsumerDiagnostics,
  consumerMetrics: ConsumerMetrics,
  onCommitAvailable: UIO[Unit],
  committedOffsetsRef: Ref[CommitOffsets],
  pendingCommits: Ref.Synchronized[Chunk[Commit]]
) extends Committer {

  override val registerExternalCommits: Map[
    TopicPartition,
    OffsetAndMetadata
  ] => Task[Unit] = offsets =>
    committedOffsetsRef.modify {
      // The continuation promise can be `null` because this commit is not actually handled by the consumer.
      _.addCommits(Chunk(Commit(java.lang.System.nanoTime(), offsets, null)))
    }.unit

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
    consumer: ByteArrayKafkaConsumer,
    executeOnEmpty: Boolean = false
  ): Task[Unit] = for {
    commits <- commitQueue.takeAll
    _       <- ZIO.logDebug(s"Processing ${commits.size} commits")
    _ <- ZIO.when(commits.nonEmpty || executeOnEmpty) {
           val offsets = mergeCommitOffsets(commits)
           val offsetsWithMetaData = offsets.map { case (tp, offset) =>
             tp -> new OffsetAndMetadata(offset.offset + 1, offset.leaderEpoch, offset.metadata)
           }

           for {
             _         <- pendingCommits.update(_ ++ commits)
             startTime <- ZIO.clockWith(_.nanoTime)
             _ <- commitAsyncZIO(
                    consumer,
                    offsetsWithMetaData,
                    doOnComplete = handleCommitCompletion(commits, offsetsWithMetaData, startTime, _)
                  )
             // We don't wait for the completion of the commit here, because it will only complete once we poll again.
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

  private def handleCommitCompletion(
    commits: Chunk[Commit],
    offsets: Map[TopicPartition, OffsetAndMetadata],
    startTime: NanoTime,
    commitResults: Either[Exception, Map[TopicPartition, OffsetAndMetadata]]
  ): UIO[Unit] =
    ZIO
      .from(commitResults)
      .zipLeft(
        for {
          endTime <- ZIO.clockWith(_.nanoTime)
          latency = (endTime - startTime).nanoseconds
          offsetIncrease <- committedOffsetsRef.modify(_.addCommits(commits))
          _              <- consumerMetrics.observeAggregatedCommit(latency, offsetIncrease).when(commits.nonEmpty)
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
      .ignore

  /**
   * Wrapper that converts KafkaConsumer#commitAsync to ZIO
   *
   * @return
   *   Task whose completion indicates completion of the commitAsync call.
   */
  private def commitAsyncZIO(
    consumer: ByteArrayKafkaConsumer,
    offsets: Map[TopicPartition, OffsetAndMetadata],
    doOnComplete: Either[Exception, Map[TopicPartition, OffsetAndMetadata]] => UIO[Unit]
  ): Task[Unit] =
    for {
      runtime <- ZIO.runtime[Any]
      _ <- ZIO.attempt {
             consumer.commitAsync(
               offsets.asJava,
               new OffsetCommitCallback {
                 override def onComplete(
                   offsets: JavaMap[TopicPartition, OffsetAndMetadata],
                   exception: Exception
                 ): Unit =
                   Unsafe.unsafe { implicit unsafe =>
                     runtime.unsafe.run {
                       if (exception == null) doOnComplete(Right(offsets.asScala.toMap))
                       else doOnComplete(Left(exception))
                     }.getOrThrowFiberFailure()
                   }
               }
             )
           }
    } yield ()

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
    diagnostics: ConsumerDiagnostics,
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
