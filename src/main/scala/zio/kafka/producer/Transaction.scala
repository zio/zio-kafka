package zio.kafka.producer

import org.apache.kafka.clients.producer.{ ProducerRecord, RecordMetadata }
import zio.kafka.producer.TransactionalProducer.UserInitiatedAbort
import zio.kafka.serde.Serializer
import zio.{ Cause, IO, RIO, ZIO }

final class Transaction private[producer] (
  private val producer: Producer
  // TODO: check state is not reachable from tests otherwise create accessor
) { // TODO: we can achieve higher typesafety by returning a zstate instead of a zio to forbid some compositions at typelevel
  // for example no more produce after abort
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
