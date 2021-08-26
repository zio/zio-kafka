package zio.kafka.producer

import org.apache.kafka.clients.producer.{ ProducerRecord, RecordMetadata }
import zio.{ IO, RIO, RefM, ZIO }
import zio.kafka.serde.Serializer

final private case class TransactionState(
  abortScheduled: Boolean = false
)

final private[producer] class Transaction(
  private val producer: Producer,
  private[producer] val state: RefM[TransactionState]
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

  def abort: IO[Nothing, Unit] = state.set(TransactionState(abortScheduled = true))
}
