package zio.kafka.client

import org.apache.kafka.common.serialization.Serde
import org.apache.kafka.common.TopicPartition
import zio.blocking.Blocking
import zio._
import zio.stream._
import zio.clock.Clock

import scala.collection.JavaConverters._

class Consumer[K, V] private (
  private val consumer: ConsumerAccess[K, V],
  private val settings: ConsumerSettings,
  private val runloop: Runloop[K, V]
) {
  def partitioned
    : ZStream[Clock with Blocking, Throwable, (TopicPartition, ZStreamChunk[Any, Throwable, CommittableRecord[K, V]])] =
    ZStream
      .fromQueue(runloop.deps.partitions)
      .unTake
      .map {
        case (tp, partition) =>
          tp -> ZStreamChunk(partition.chunks.buffer(settings.perPartitionChunkPrefetch))
      }

  def plain: ZStreamChunk[Clock with Blocking, Throwable, CommittableRecord[K, V]] =
    ZStreamChunk(partitioned.flatMapPar(Int.MaxValue)(_._2.chunks))

  def subscribe(subscription: Subscription) =
    consumer.withConsumer { c =>
      subscription match {
        case Subscription.Pattern(pattern) => c.subscribe(pattern.pattern, runloop.deps.rebalanceListener)
        case Subscription.Topics(topics)   => c.subscribe(topics.asJava, runloop.deps.rebalanceListener)
      }
    }
}

object Consumer {
  def make[K: Serde, V: Serde](settings: ConsumerSettings): ZManaged[Clock with Blocking, Throwable, Consumer[K, V]] =
    for {
      wrapper <- ConsumerAccess.make[K, V](settings)
      deps <- Runloop.deps(
               wrapper,
               settings.pollInterval,
               settings.pollTimeout
             )
      runloop <- Runloop(deps)
    } yield new Consumer(wrapper, settings, runloop)
}
