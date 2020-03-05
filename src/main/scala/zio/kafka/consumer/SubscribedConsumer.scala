package zio.kafka.consumer

import org.apache.kafka.common.TopicPartition
import zio.RIO
import zio.blocking.Blocking
import zio.clock.Clock
import zio.stream.{ ZStream, ZStreamChunk }

class SubscribedConsumer[R, K, V](
  private val underlying: RIO[Blocking, Consumer.Service[R, K, V]]
) {

  def partitionedStream: ZStream[
    Clock with Blocking,
    Throwable,
    (TopicPartition, ZStreamChunk[R, Throwable, CommittableRecord[K, V]])
  ] =
    ZStream.fromEffect(underlying).flatMap(_.partitionedStream)

  def plainStream: ZStreamChunk[R with Clock with Blocking, Throwable, CommittableRecord[K, V]] =
    ZStreamChunk(partitionedStream.flatMapPar(n = Int.MaxValue)(_._2.chunks))
}

class SubscribedConsumerFromEnvironment[R, K, V](
  private val underlying: RIO[Blocking with Consumer[R, K, V], Consumer.Service[R, K, V]]
) {

  def partitionedStream: ZStream[
    Clock with Blocking with Consumer[R, K, V],
    Throwable,
    (TopicPartition, ZStreamChunk[R, Throwable, CommittableRecord[K, V]])
  ] =
    ZStream.fromEffect(underlying).flatMap(_.partitionedStream)

  def plainStream: ZStreamChunk[R with Clock with Blocking with Consumer[R, K, V], Throwable, CommittableRecord[K, V]] =
    ZStreamChunk(partitionedStream.flatMapPar(n = Int.MaxValue)(_._2.chunks))
}
