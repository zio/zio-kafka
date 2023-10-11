package zio.kafka.consumer

import org.apache.kafka.clients.consumer.{ConsumerGroupMetadata, OffsetAndMetadata, RetriableCommitFailedException}
import org.apache.kafka.common.TopicPartition
import zio.{RIO, Schedule, Task}

sealed trait Offset {
  def topic: String
  def partition: Int
  def offset: Long
  def commit: Task[Unit]
  def batch: OffsetBatch
  def consumerGroupMetadata: Option[ConsumerGroupMetadata]

  private[consumer] def metadata: Option[String]
  def withMetadata(metadata: String): Offset

  /**
   * Attempts to commit and retries according to the given policy when the commit fails with a
   * RetriableCommitFailedException
   */
  final def commitOrRetry[R](policy: Schedule[R, Throwable, Any]): RIO[R, Unit] =
    Offset.commitOrRetry(commit, policy)

  final lazy val topicPartition: TopicPartition = new TopicPartition(topic, partition)

  private[consumer] def asJavaOffsetAndMetadata: OffsetAndMetadata = new OffsetAndMetadata(offset, metadata.orNull)
}

object Offset {
  private[consumer] def commitOrRetry[R, B](
    commit: Task[Unit],
    policy: Schedule[R, Throwable, B]
  ): RIO[R, Unit] =
    commit.retry(
      Schedule.recurWhile[Throwable] {
        case _: RetriableCommitFailedException => true
        case _                                 => false
      } && policy
    )
}

private final case class OffsetImpl(
  topic: String,
  partition: Int,
  offset: Long,
  commitHandle: Map[TopicPartition, Long] => Task[Unit],
  consumerGroupMetadata: Option[ConsumerGroupMetadata],
  private[consumer] val metadata: Option[String] = None
) extends Offset {
  def commit: Task[Unit] = commitHandle(Map(topicPartition -> offset))
  def batch: OffsetBatch = OffsetBatchImpl(Map(topicPartition -> offset), commitHandle, consumerGroupMetadata)

  override def withMetadata(metadata: String): Offset = copy(metadata = Some(metadata))
}
