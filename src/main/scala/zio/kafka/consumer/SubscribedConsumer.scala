package zio.kafka.consumer

import org.apache.kafka.common.TopicPartition
import zio.RIO
import zio.blocking.Blocking
import zio.clock.Clock
import zio.stream.ZStream
import zio.kafka.serde.Deserializer

class SubscribedConsumer(
  private val underlying: RIO[Blocking, Consumer.Service]
) {

  def partitionedStream[R, K, V](keyDeserializer: Deserializer[R, K], valueDeserializer: Deserializer[R, V]): ZStream[
    Clock with Blocking,
    Throwable,
    (TopicPartition, ZStream[R, Throwable, CommittableRecord[K, V]])
  ] = ???

  def plainStream[R, K, V](
    keyDeserializer: Deserializer[R, K],
    valueDeserializer: Deserializer[R, V],
    outputBuffer: Int = 4
  ): ZStream[R with Clock with Blocking, Throwable, CommittableRecord[K, V]] = ???
}

class SubscribedConsumerFromEnvironment(
  private val underlying: RIO[Blocking with Consumer, Consumer.Service]
) {

  def partitionedStream[R, K, V](keyDeserializer: Deserializer[R, K], valueDeserializer: Deserializer[R, V]): ZStream[
    Clock with Blocking with Consumer,
    Throwable,
    (TopicPartition, ZStream[R, Throwable, CommittableRecord[K, V]])
  ] = ???

  def plainStream[R, K, V](
    keyDeserializer: Deserializer[R, K],
    valueDeserializer: Deserializer[R, V],
    outputBuffer: Int = 4
  ): ZStream[R with Clock with Blocking with Consumer, Throwable, CommittableRecord[K, V]] = ???
}
