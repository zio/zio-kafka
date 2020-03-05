package zio.kafka.client

import org.apache.kafka.common.TopicPartition
import zio.blocking.Blocking
import zio.clock.Clock
import zio.kafka.client.Consumer.Service
import zio.stream.{ ZStream, ZStreamChunk }

class SubscribedConsumer[R, K, V](private val underlying: BlockingTask[Service[R, K, V]]) {

  def partitionedStream
    : ZStream[Clock with Blocking, Throwable, (TopicPartition, ZStreamChunk[R, Throwable, CommittableRecord[K, V]])] =
    ZStream.fromEffect(underlying).flatMap(_.partitionedStream)

  def plainStream: ZStreamChunk[R with Clock with Blocking, Throwable, CommittableRecord[K, V]] =
    ZStreamChunk(partitionedStream.flatMapPar(n = Int.MaxValue)(_._2.chunks))
}
