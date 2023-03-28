package zio.kafka.consumer

import org.apache.kafka.clients.consumer.{ ConsumerGroupMetadata, ConsumerRecord }
import org.apache.kafka.common.TopicPartition
import zio.kafka.serde.Deserializer
import zio.{ RIO, Task }

final case class CommittableRecord[K, V](
  record: ConsumerRecord[K, V],
  private val commitHandle: Map[TopicPartition, Long] => Task[Unit],
  private val consumerGroupMetadata: Option[ConsumerGroupMetadata]
) {
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

  def key: K          = record.key
  def value: V        = record.value()
  def partition: Int  = record.partition()
  def timestamp: Long = record.timestamp()

  def offset: Offset =
    OffsetImpl(
      topic = record.topic(),
      partition = record.partition(),
      offset = record.offset(),
      commitHandle = commitHandle,
      consumerGroupMetadata = consumerGroupMetadata
    )
}

object CommittableRecord {
  def apply[K, V](
    record: ConsumerRecord[K, V],
    commitHandle: Map[TopicPartition, Long] => Task[Unit],
    consumerGroupMetadata: Option[ConsumerGroupMetadata]
  ): CommittableRecord[K, V] =
    new CommittableRecord(
      record = record,
      commitHandle = commitHandle,
      consumerGroupMetadata = consumerGroupMetadata
    )
}
