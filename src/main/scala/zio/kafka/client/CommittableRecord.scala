package zio.kafka.client

import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.TopicPartition
import zio.Task
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

  def deserialize[K: Deserializer, V: Deserializer](
    record: CommittableRecord[Array[Byte], Array[Byte]]
  ): Task[CommittableRecord[K, V]] =
    for {
      key   <- implicitly[Deserializer[K]].deserialize(record.record.key())
      value <- implicitly[Deserializer[V]].deserialize(record.record.value())
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
