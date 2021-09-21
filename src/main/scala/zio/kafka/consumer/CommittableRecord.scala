package zio.kafka.consumer

import org.apache.kafka.clients.consumer.{ConsumerGroupMetadata, ConsumerRecord}
import org.apache.kafka.common.TopicPartition
import zio.{RIO, Task}
import zio.kafka.serde.Deserializer

final case class CommittableRecord[K, V](record: ConsumerRecord[K, V], offset: Offset) {
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
        ConsumerRecord.NULL_CHECKSUM, // Checksum is deprecated
        record.serializedKeySize(),
        record.serializedValueSize(),
        key,
        value,
        record.headers()
      )
    )

  def key: K          = record.key
  def value: V        = record.value()
  def partition: Int  = record.partition()
  def timestamp: Long = record.timestamp()
}

object CommittableRecord {
  def apply[K, V](
    record: ConsumerRecord[K, V],
    commitHandle: Map[TopicPartition, Long] => Task[Unit],
    consumerGroupMetadata: ConsumerGroupMetadata
  ): CommittableRecord[K, V] =
    CommittableRecord(
      record,
      OffsetImpl(new TopicPartition(record.topic(), record.partition()), record.offset(), commitHandle, consumerGroupMetadata)
    )
}
