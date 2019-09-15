package zio.kafka.client

import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.TopicPartition
import zio.Task

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
}
