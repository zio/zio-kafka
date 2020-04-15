package zio.kafka.consumer

import org.apache.kafka.common.TopicPartition
import zio.RIO
import zio.blocking.Blocking
import zio.clock.Clock
import zio.stream.{ ZStream, ZStreamChunk }
import zio.kafka.serde.Deserializer

class SubscribedConsumer(
  private val underlying: RIO[Blocking, Consumer.Service]
) {

  def partitionedStream[R, K, V](keyDeserializer: Deserializer[R, K], valueDeserializer: Deserializer[R, V]): ZStream[
    Clock with Blocking,
    Throwable,
    (TopicPartition, ZStreamChunk[R, Throwable, CommittableRecord[K, V]])
  ] =
    ZStream.fromEffect(underlying).flatMap(_.partitionedStream(keyDeserializer, valueDeserializer))

  def plainStream[R, K, V](
    keyDeserializer: Deserializer[R, K],
    valueDeserializer: Deserializer[R, V]
  ): ZStreamChunk[R with Clock with Blocking, Throwable, CommittableRecord[K, V]] =
    ZStreamChunk(partitionedStream(keyDeserializer, valueDeserializer).flatMapPar(n = Int.MaxValue)(_._2.chunks))
}

class SubscribedConsumerFromEnvironment(
  private val underlying: RIO[Blocking with Consumer, Consumer.Service]
) {

  def partitionedStream[R, K, V](keyDeserializer: Deserializer[R, K], valueDeserializer: Deserializer[R, V]): ZStream[
    Clock with Blocking with Consumer,
    Throwable,
    (TopicPartition, ZStreamChunk[R, Throwable, CommittableRecord[K, V]])
  ] =
    ZStream.fromEffect(underlying).flatMap(_.partitionedStream(keyDeserializer, valueDeserializer))

  def plainStream[R, K, V](
    keyDeserializer: Deserializer[R, K],
    valueDeserializer: Deserializer[R, V]
  ): ZStreamChunk[R with Clock with Blocking with Consumer, Throwable, CommittableRecord[K, V]] =
    ZStreamChunk(partitionedStream(keyDeserializer, valueDeserializer).flatMapPar(n = Int.MaxValue)(_._2.chunks))
}
