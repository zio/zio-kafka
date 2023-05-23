package zio.kafka.consumer

import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.{ TopicPartition => JTopicPartition }
import zio.RIO
import zio.kafka.serde.Deserializer

final case class CommittableRecord[K, V](
  record: ConsumerRecord[K, V]
) {
  def deserializeWith[R, K1, V1](
    keyDeserializer: Deserializer[R, K1],
    valueDeserializer: Deserializer[R, V1]
  )(implicit ev1: K <:< Array[Byte], ev2: V <:< Array[Byte]): RIO[R, CommittableRecord[K1, V1]] =
    for {
      key   <- keyDeserializer.deserialize(record.topic(), record.headers(), record.key())
      value <- valueDeserializer.deserialize(record.topic(), record.headers(), record.value())
    } yield new CommittableRecord[K1, V1](
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

  def key: K          = record.key()
  def value: V        = record.value()
  def partition: Int  = record.partition()
  def timestamp: Long = record.timestamp()

  private[consumer] lazy val topicPartition: JTopicPartition = new JTopicPartition(record.topic(), record.partition())
  private[consumer] def offset: Long                         = record.offset()
}

object CommittableRecord {
  def apply[K, V](record: ConsumerRecord[K, V]): CommittableRecord[K, V] =
    new CommittableRecord(record = record)
}
