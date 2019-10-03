package zio.kafka.client

import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.TopicPartition
import zio.{ RIO, Task }
import zio.kafka.client.serde.Deserializer

final case class CommittableRecord[K, V](record: ConsumerRecord[K, V], offset: Offset)
object CommittableRecord {
  def apply[K, V](
    record: ConsumerRecord[K, V],
    commitHandle: Map[TopicPartition, Long] => Task[Unit]
  ): CommittableRecord[K, V] =
    CommittableRecord(
      record,
      OffsetImpl(new TopicPartition(record.topic(), record.partition()), record.offset(), commitHandle)
    )

  def deserialize[R, K, V](
    record: CommittableRecord[Array[Byte], Array[Byte]],
    keyDeserializer: Deserializer[R, K],
    valueDeserializer: Deserializer[R, V]
  ): RIO[R, CommittableRecord[K, V]] =
    for {
      key   <- keyDeserializer.deserialize(record.record.key())
      value <- valueDeserializer.deserialize(record.record.value())
    } yield {
      val r = record.record
      record.copy(
        record = new ConsumerRecord[K, V](
          r.topic(),
          r.partition(),
          r.offset(),
          r.timestamp(),
          r.timestampType(),
          ConsumerRecord.NULL_CHECKSUM, // Checksum is deprecated
          r.serializedKeySize(),
          r.serializedValueSize(),
          key,
          value,
          r.headers()
        )
      )
    }
}
