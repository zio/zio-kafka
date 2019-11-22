package zio.kafka.client

import org.apache.kafka.clients.consumer.RetriableCommitFailedException
import org.apache.kafka.common.TopicPartition
import zio.clock.Clock
import zio.{ Schedule, Task, ZIO }

sealed trait Offset {
  def topicPartition: TopicPartition
  def offset: Long
  def commit: Task[Unit]
  def batch: OffsetBatch

  /**
   * Attempts to commit and retries according to the given policy when the commit fails
   * with a RetriableCommitFailedException
   */
  def commitOrRetry[R, B](policy: Schedule[R, Throwable, B]): ZIO[R with Clock, Throwable, Unit] =
    Offset.commitOrRetry(commit, policy)
}

object Offset {
  private[client] def commitOrRetry[R, B](
    commit: Task[Unit],
    policy: Schedule[R, Throwable, B]
  ): ZIO[R with Clock, Throwable, Unit] =
    commit.retry(
      Schedule.doWhile[Throwable]({
        case _: RetriableCommitFailedException => true
        case _                                 => false
      }) && policy
    )
}

private final case class OffsetImpl(
  topicPartition: TopicPartition,
  offset: Long,
  commitHandle: Map[TopicPartition, Long] => Task[Unit]
) extends Offset {
  val commit: Task[Unit] = commitHandle(Map(topicPartition    -> offset))
  val batch: OffsetBatch = OffsetBatchImpl(Map(topicPartition -> offset), commitHandle)
}
