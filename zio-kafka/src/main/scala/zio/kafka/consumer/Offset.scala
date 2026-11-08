package zio.kafka.consumer

import org.apache.kafka.clients.consumer.{ ConsumerGroupMetadata, OffsetAndMetadata, RetriableCommitFailedException }
import org.apache.kafka.common.TopicPartition
import zio._

import java.util.{ Optional => JOption }

/**
 * The offset of a consumed record.
 */
trait Offset {

  def topic: String
  def partition: Int

  /** The leader-epoch in which the record was consumed. */
  def leaderEpoch: JOption[Integer]

  /** The consumed record's offset. */
  def offset: Long

  /** The committable next offset. */
  def nextOffset: OffsetAndMetadata

  /** Commit the next offset. */
  def commit: Task[Unit]

  def batch: OffsetBatch
  def consumerGroupMetadata: Option[ConsumerGroupMetadata]

  /** Add metadata to the commit. */
  def withMetadata(metadata: String): Offset

  /**
   * Attempts to commit and retries according to the given policy when the commit fails with a
   * RetriableCommitFailedException
   */
  final def commitOrRetry[R](policy: Schedule[R, Throwable, Any]): RIO[R, Unit] =
    Offset.commitOrRetry(commit, policy)

  final lazy val topicPartition: TopicPartition = new TopicPartition(topic, partition)
}

object Offset {
  private[consumer] def commitOrRetry[R, B](
    commit: Task[Unit],
    policy: Schedule[R, Throwable, B]
  ): RIO[R, Unit] =
    commit.retry(
      Schedule.recurWhile[Throwable] {
        case _: RetriableCommitFailedException => true
        case Consumer.CommitTimeout            => true
        case _                                 => false
      } && policy
    )
}

private final case class OffsetImpl(
  topic: String,
  partition: Int,
  offset: Long,
  nextOffset: OffsetAndMetadata,
  commitHandle: Map[TopicPartition, OffsetAndMetadata] => Task[Unit],
  consumerGroupMetadata: Option[ConsumerGroupMetadata]
) extends Offset {
  def leaderEpoch: JOption[Integer] = nextOffset.leaderEpoch()
  def commit: Task[Unit]            = commitHandle(Map(topicPartition -> nextOffset))
  def batch: OffsetBatch = OffsetBatchImpl(
    Map(topicPartition -> nextOffset),
    commitHandle,
    consumerGroupMetadata
  )
  def withMetadata(metadata: String): OffsetImpl =
    copy(nextOffset = new OffsetAndMetadata(nextOffset.offset(), nextOffset.leaderEpoch(), metadata))

}
