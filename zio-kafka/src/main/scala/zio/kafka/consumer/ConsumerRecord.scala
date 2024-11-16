package zio.kafka.consumer

import org.apache.kafka.clients.consumer.{ ConsumerGroupMetadata, ConsumerRecord => JConsumerRecord }
import zio.RIO
import zio.kafka.serde.Deserializer

final case class ConsumerRecord[K, V](
  record: JConsumerRecord[K, V],
  private val consumerGroupMetadata: Option[ConsumerGroupMetadata]
) {
  def deserializeWith[R, K1, V1](
    keyDeserializer: Deserializer[R, K1],
    valueDeserializer: Deserializer[R, V1]
  )(implicit ev1: K <:< Array[Byte], ev2: V <:< Array[Byte]): RIO[R, ConsumerRecord[K1, V1]] =
    for {
      key   <- keyDeserializer.deserialize(record.topic(), record.headers(), record.key())
      value <- valueDeserializer.deserialize(record.topic(), record.headers(), record.value())
    } yield copy(
      record = new JConsumerRecord[K1, V1](
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
      consumerGroupMetadata = consumerGroupMetadata,
      metadata = None
    )
}

object ConsumerRecord {
  def apply[K, V](
    record: JConsumerRecord[K, V],
    consumerGroupMetadata: Option[ConsumerGroupMetadata]
  ): ConsumerRecord[K, V] =
    new ConsumerRecord(
      record = record,
      consumerGroupMetadata = consumerGroupMetadata
    )
}
