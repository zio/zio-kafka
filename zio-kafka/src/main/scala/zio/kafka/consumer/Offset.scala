package zio.kafka.consumer

import org.apache.kafka.clients.consumer.{ ConsumerGroupMetadata, OffsetAndMetadata, RetriableCommitFailedException }
import org.apache.kafka.common.TopicPartition
import zio._

import java.util.{ Optional => JOption }

/**
 * The offset of a consumed record.
 */
trait Offset {

  /** The topic of the consumed record. */
  def topic: String

  /** The partition of the consumed record. */
  def partition: Int

  /** The leader-epoch in which the record was consumed. */
  def leaderEpoch: JOption[Integer]

  /** The consumed record's offset. */
  def offset: Long

  /** The committable next offset. */
  def nextOffset: OffsetAndMetadata

  /** Commit the next offset. */
  def commit: Task[Unit]

  /** An [[OffsetBatch]] that contains only this offset. */
  def batch: OffsetBatch

  /**
   * The metadata of the consumer group that consumed the record.
   *
   * It is read from the Kafka consumer after each rebalance. It is `None` when the consumer has no `group.id`. It is
   * also `None` for a consumer that has only used [[Subscription.Manual]] subscriptions, because no rebalance happens
   * for them. A [[zio.kafka.producer.TransactionalProducer]] needs it to commit offsets in a transaction.
   */
  def consumerGroupMetadata: Option[ConsumerGroupMetadata]

  /** Add metadata to the commit. */
  def withMetadata(metadata: String): Offset

  /**
   * Attempts to commit and retries according to the given policy when the commit fails with a
   * RetriableCommitFailedException or commit timeout.
   */
  final def commitOrRetry[R](policy: Schedule[R, Throwable, Any]): RIO[R, Unit] =
    Offset.commitOrRetry(commit, policy)

  /** The topic and the partition of the consumed record as a `TopicPartition`. */
  final lazy val topicPartition: TopicPartition = new TopicPartition(topic, partition)
}

/** Companion of [[Offset]]. */
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
