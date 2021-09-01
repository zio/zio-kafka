package zio.kafka.producer

import org.apache.kafka.clients.producer.{ ProducerRecord, RecordMetadata }
import zio.kafka.producer.TransactionalProducer.UserInitiatedAbort
import zio.{ Cause, IO, RIO, RefM, ZIO }
import zio.kafka.serde.Serializer

final private[producer] class Transaction(
  private val producer: Producer
) {
  def produce[R, K, V](
    topic: String,
    key: K,
    value: V,
    keySerializer: Serializer[R, K],
    valueSerializer: Serializer[R, V]
  ): RIO[R, RecordMetadata] =
    produce(new ProducerRecord[K, V](topic, key, value), keySerializer, valueSerializer)

  def produce[R, K, V](
    producerRecord: ProducerRecord[K, V],
    keySerializer: Serializer[R, K],
    valueSerializer: Serializer[R, V]
  ): RIO[R, RecordMetadata] =
    producer.produce[R, K, V](producerRecord, keySerializer, valueSerializer)

  def abort: IO[TransactionalProducer.UserInitiatedAbort.type, Nothing] =
    ZIO.haltWith(t => Cause.traced(Cause.fail(UserInitiatedAbort), t()))
}
