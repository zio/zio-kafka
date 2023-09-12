package zio.kafka

import org.apache.kafka.clients.producer.ProducerRecord
import zio.RIO
import zio.kafka.serde.Serializer

package object producer {
  type ByteRecord = ProducerRecord[Array[Byte], Array[Byte]]

  def serialize[R, K, V](
    r: ProducerRecord[K, V],
    keySerializer: Serializer[R, K],
    valueSerializer: Serializer[R, V]
  ): RIO[R, ByteRecord] =
    for {
      key   <- keySerializer.serialize(r.topic, r.headers, r.key())
      value <- valueSerializer.serialize(r.topic, r.headers, r.value())
    } yield new ProducerRecord(r.topic, r.partition(), r.timestamp(), key, value, r.headers)
}
