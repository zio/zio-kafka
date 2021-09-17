package zio.kafka.consumer

import org.apache.kafka.clients.consumer.RetriableCommitFailedException
import org.apache.kafka.common.TopicPartition
import zio.{ RIO, Schedule, Task }
import zio.clock.Clock

sealed trait Offset {
  def topicPartition: TopicPartition
  def offset: Long
  def commit: Task[Unit]
  def batch: OffsetBatch
  def consumerGroupId: String

  /**
   * Attempts to commit and retries according to the given policy when the commit fails
   * with a RetriableCommitFailedException
   */
  def commitOrRetry[R](policy: Schedule[R, Throwable, Any]): RIO[R with Clock, Unit] =
    Offset.commitOrRetry(commit, policy)
}

object Offset {
  private[consumer] def commitOrRetry[R, B](
    commit: Task[Unit],
    policy: Schedule[R, Throwable, B]
  ): RIO[R with Clock, Unit] =
    commit.retry(
      Schedule.recurWhile[Throwable] {
        case _: RetriableCommitFailedException => true
        case _                                 => false
      } && policy
    )
}

private final case class OffsetImpl(
  topicPartition: TopicPartition,
  offset: Long,
  commitHandle: Map[TopicPartition, Long] => Task[Unit],
  consumerGroupId: String
) extends Offset {
  def commit: Task[Unit] = commitHandle(Map(topicPartition -> offset))
  def batch: OffsetBatch = OffsetBatchImpl(Map(topicPartition -> offset), commitHandle, consumerGroupId)
}
