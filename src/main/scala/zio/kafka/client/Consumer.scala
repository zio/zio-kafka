package zio.kafka.client

import org.apache.kafka.common.serialization.Serde
import org.apache.kafka.common.TopicPartition
import zio.blocking.Blocking
import zio._
import zio.duration._
import zio.stream._
import zio.clock.Clock

import scala.collection.JavaConverters._
import org.apache.kafka.common.PartitionInfo
import org.apache.kafka.clients.consumer.OffsetAndTimestamp

class Consumer[K, V] private (
  private val consumer: ConsumerAccess[K, V],
  private val settings: ConsumerSettings,
  private val runloop: Runloop[K, V]
) {
  def assignment: BlockingTask[Set[TopicPartition]] =
    consumer.withConsumer(_.assignment().asScala.toSet)

  def beginningOffsets(
    partitions: Set[TopicPartition],
    timeout: Duration = Duration.Infinity
  ): BlockingTask[Map[TopicPartition, Long]] =
    consumer.withConsumer(_.beginningOffsets(partitions.asJava, timeout.asJava).asScala.mapValues(_.longValue()).toMap)

  def endOffsets(
    partitions: Set[TopicPartition],
    timeout: Duration = Duration.Infinity
  ): BlockingTask[Map[TopicPartition, Long]] =
    consumer.withConsumer(_.endOffsets(partitions.asJava, timeout.asJava).asScala.mapValues(_.longValue()).toMap)

  def listTopics(timeout: Duration = Duration.Infinity): BlockingTask[Map[String, List[PartitionInfo]]] =
    consumer.withConsumer(_.listTopics(timeout.asJava).asScala.mapValues(_.asScala.toList).toMap)

  def offsetsForTimes(
    timestamps: Map[TopicPartition, Long],
    timeout: Duration = Duration.Infinity
  ): BlockingTask[Map[TopicPartition, OffsetAndTimestamp]] =
    consumer.withConsumer(_.offsetsForTimes(timestamps.mapValues(Long.box).asJava, timeout.asJava).asScala.toMap)

  def partitioned
    : ZStream[Clock with Blocking, Throwable, (TopicPartition, ZStreamChunk[Any, Throwable, CommittableRecord[K, V]])] =
    ZStream
      .fromQueue(runloop.deps.partitions)
      .unTake
      .map {
        case (tp, partition) =>
          val partitionStream =
            if (settings.perPartitionChunkPrefetch <= 0) partition
            else ZStreamChunk(partition.chunks.buffer(settings.perPartitionChunkPrefetch))

          tp -> partitionStream
      }

  def partitionsFor(topic: String, timeout: Duration = Duration.Infinity): BlockingTask[List[PartitionInfo]] =
    consumer.withConsumer(_.partitionsFor(topic, timeout.asJava).asScala.toList)

  def position(partition: TopicPartition, timeout: Duration = Duration.Infinity): BlockingTask[Long] =
    consumer.withConsumer(_.position(partition, timeout.asJava))

  def plain: ZStreamChunk[Clock with Blocking, Throwable, CommittableRecord[K, V]] =
    ZStreamChunk(partitioned.flatMapPar(Int.MaxValue)(_._2.chunks))

  def seek(partition: TopicPartition, offset: Long): BlockingTask[Unit] =
    consumer.withConsumer(_.seek(partition, offset))

  def seekToBeginning(partitions: Set[TopicPartition]): BlockingTask[Unit] =
    consumer.withConsumer(_.seekToBeginning(partitions.asJava))

  def seekToEnd(partitions: Set[TopicPartition]): BlockingTask[Unit] =
    consumer.withConsumer(_.seekToEnd(partitions.asJava))

  def subscribe(subscription: Subscription) =
    consumer.withConsumer { c =>
      subscription match {
        case Subscription.Pattern(pattern) => c.subscribe(pattern.pattern, runloop.deps.rebalanceListener)
        case Subscription.Topics(topics)   => c.subscribe(topics.asJava, runloop.deps.rebalanceListener)
      }
    }

  def subscription: BlockingTask[Set[String]] =
    consumer.withConsumer(_.subscription().asScala.toSet)

  def unsubscribe: BlockingTask[Unit] =
    consumer.withConsumer(_.unsubscribe())
}

object Consumer {
  def make[K: Serde, V: Serde](settings: ConsumerSettings): ZManaged[Clock with Blocking, Throwable, Consumer[K, V]] =
    for {
      wrapper <- ConsumerAccess.make[K, V](settings)
      deps <- Runloop.Deps.make(
               wrapper,
               settings.pollInterval,
               settings.pollTimeout
             )
      runloop <- Runloop(deps)
    } yield new Consumer(wrapper, settings, runloop)
}
