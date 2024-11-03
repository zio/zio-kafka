package zio.kafka.consumer

import org.apache.kafka.clients.consumer.{ ConsumerGroupMetadata, OffsetAndMetadata, RetriableCommitFailedException }
import org.apache.kafka.common.TopicPartition
import zio.kafka.consumer.Consumer.CommitError
import zio.{ IO, Schedule, ZIO }

sealed trait Offset {

  def topic: String
  def partition: Int
  def offset: Long
  def commit: IO[CommitError, Unit]
  def batch: OffsetBatch
  def consumerGroupMetadata: Option[ConsumerGroupMetadata]
  def withMetadata(metadata: String): Offset

  private[consumer] def metadata: Option[String]
  private[consumer] def asJavaOffsetAndMetadata: OffsetAndMetadata = new OffsetAndMetadata(offset, metadata.orNull)

  /**
   * Attempts to commit and retries according to the given policy when the commit fails with a
   * RetriableCommitFailedException
   */
  final def commitOrRetry[R](policy: Schedule[R, CommitError, Any]): ZIO[R, CommitError, Unit] =
    Offset.commitOrRetry(commit, policy)

  final lazy val topicPartition: TopicPartition = new TopicPartition(topic, partition)
}

object Offset {
  private[consumer] def commitOrRetry[R, B](
    commit: IO[CommitError, Unit],
    policy: Schedule[R, CommitError, B]
  ): ZIO[R, CommitError, Unit] =
    commit.retry(
      Schedule.recurWhile[CommitError] {
        case _: RetriableCommitFailedException  => true
        case Consumer.CommitError.CommitTimeout => true
        case _                                  => false
      } && policy
    )
}

private final case class OffsetImpl(
  topic: String,
  partition: Int,
  offset: Long,
  commitHandle: Map[TopicPartition, OffsetAndMetadata] => IO[CommitError, Unit],
  consumerGroupMetadata: Option[ConsumerGroupMetadata],
  metadata: Option[String] = None
) extends Offset {
  def commit: IO[CommitError, Unit] = commitHandle(Map(topicPartition -> asJavaOffsetAndMetadata))
  def batch: OffsetBatch = OffsetBatchImpl(
    Map(topicPartition -> asJavaOffsetAndMetadata),
    commitHandle,
    consumerGroupMetadata
  )
  def withMetadata(metadata: String): OffsetImpl = copy(metadata = Some(metadata))
}
