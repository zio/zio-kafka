package zio.kafka.consumer

import org.apache.kafka.clients.consumer.{ ConsumerGroupMetadata, ConsumerRecord, OffsetAndMetadata }
import org.apache.kafka.common.TopicPartition
import zio.kafka.serde.Deserializer
import zio.{ RIO, Task }

/**
 * A record consumed from Kafka, together with what is needed to commit its offset.
 *
 * The streams of a [[Consumer]] emit committable records. Read the data with `key`, `value`, `partition` and
 * `timestamp` (or from `record`), and commit the record's [[Offset]] after the record is processed.
 *
 * {{{
 * import zio._
 * import zio.kafka.serde.Serde
 *
 * def process(consumer: Consumer): Task[Unit] =
 *   consumer
 *     .plainStream(Subscription.topics("orders"), Serde.string, Serde.string)
 *     .mapZIOChunked(record => ZIO.logInfo(record.key + ": " + record.value).as(record.offset))
 *     .aggregateAsyncWithin(Consumer.collectOffsets, Schedule.fixed(100.millis))
 *     .mapZIO(_.commit)
 *     .runDrain
 * }}}
 *
 * @param record
 *   the consumed Kafka record
 * @param nextOffset
 *   the offset to commit for this record. Normally this is the offset of the record plus 1. For the last record that a
 *   poll returned for a partition it can be higher, when the broker has control records after that record.
 * @see
 *   [[https://zio.dev/zio-kafka/consumer/ Consumer]]
 */
final case class CommittableRecord[K, V](
  record: ConsumerRecord[K, V],
  nextOffset: OffsetAndMetadata,
  private val commitHandle: Map[TopicPartition, OffsetAndMetadata] => Task[Unit],
  private val consumerGroupMetadata: Option[ConsumerGroupMetadata]
) {
  /**
   * Deserializes the key and the value of this record, which must be byte arrays, with the given deserializers.
   *
   * Returns a new record with the deserialized key and value. The topic, partition, offset, timestamp and headers of
   * the record, and the next offset, stay the same. The effect fails when one of the deserializers fails.
   */
  def deserializeWith[R, K1, V1](
    keyDeserializer: Deserializer[R, K1],
    valueDeserializer: Deserializer[R, V1]
  )(implicit ev1: K <:< Array[Byte], ev2: V <:< Array[Byte]): RIO[R, CommittableRecord[K1, V1]] =
    for {
      key   <- keyDeserializer.deserialize(record.topic(), record.headers(), record.key())
      value <- valueDeserializer.deserialize(record.topic(), record.headers(), record.value())
    } yield copy(
      record = new ConsumerRecord[K1, V1](
        record.topic(),
        record.partition(),
        record.offset(),
        record.timestamp(),
        record.timestampType(),
        record.serializedKeySize(),
        record.serializedValueSize(),
        key,
        value,
        record.headers(),
        record.leaderEpoch()
      )
    )

  /** The key of the record. */
  def key: K = record.key

  /** The value of the record. */
  def value: V = record.value()

  /** The partition that the record was consumed from. */
  def partition: Int = record.partition()

  /** The timestamp of the record in milliseconds, or -1 when the record has no timestamp. */
  def timestamp: Long = record.timestamp()

  /** The [[Offset]] of this record. Commit it after the record is processed. */
  def offset: Offset =
    OffsetImpl(
      topic = record.topic(),
      partition = record.partition(),
      offset = record.offset(),
      nextOffset = nextOffset,
      commitHandle = commitHandle,
      consumerGroupMetadata = consumerGroupMetadata
    )
}

/** Constructors for [[CommittableRecord]]. */
object CommittableRecord {

  /**
   * Creates a committable record.
   *
   * @param commitHandle
   *   called with the offsets to commit when the [[Offset]] of this record (or an [[OffsetBatch]] that contains it) is
   *   committed
   * @param consumerGroupMetadata
   *   the metadata of the consumer group, see [[Offset.consumerGroupMetadata]]
   */
  def apply[K, V](
    record: ConsumerRecord[K, V],
    nextOffset: OffsetAndMetadata,
    commitHandle: Map[TopicPartition, OffsetAndMetadata] => Task[Unit],
    consumerGroupMetadata: Option[ConsumerGroupMetadata]
  ): CommittableRecord[K, V] =
    new CommittableRecord(
      record = record,
      nextOffset = nextOffset,
      commitHandle = commitHandle,
      consumerGroupMetadata = consumerGroupMetadata
    )
}
