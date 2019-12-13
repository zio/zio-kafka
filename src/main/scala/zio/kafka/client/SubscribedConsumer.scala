package zio.kafka.client

import org.apache.kafka.common.TopicPartition
import zio.blocking.Blocking
import zio.clock.Clock
import zio.kafka.client.serde.Deserializer
import zio.stream.{ ZStream, ZStreamChunk }

class SubscribedConsumer(private val underlying: BlockingTask[Consumer]) {

  def partitionedStream[R, K, V](
    keyDeserializer: Deserializer[R, K],
    valueDeserializer: Deserializer[R, V]
  ): ZStream[Clock with Blocking, Throwable, (TopicPartition, ZStreamChunk[R, Throwable, CommittableRecord[K, V]])] =
    ZStream.fromEffect(underlying).flatMap(_.partitionedStream(keyDeserializer, valueDeserializer))

  def plainStream[R, K, V](
    keyDeserializer: Deserializer[R, K],
    valueDeserializer: Deserializer[R, V]
  ): ZStreamChunk[R with Clock with Blocking, Throwable, CommittableRecord[K, V]] =
    ZStreamChunk(
      partitionedStream[R, K, V](keyDeserializer, valueDeserializer).flatMapPar(n = Int.MaxValue)(
        _._2.chunks
      )
    )

}
