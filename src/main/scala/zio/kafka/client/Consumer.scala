package zio.kafka.client

import org.apache.kafka.common.serialization.Serde
import org.apache.kafka.common.TopicPartition
import zio.blocking.Blocking
import zio._
import zio.stream._
import zio.clock.Clock
import zio.console._

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

  /**
   * Execute an effect for each record and commit the offset after processing
   *
   * This method is the easiest way of processing messages on a Kafka topic.
   *
   * Messages on a single partition are processed sequentially, while the processing of
   * multiple partitions happens in parallel.
   *
   * Offsets are batched and committed after execution of the effect.
   *
   * The effect should not fail. Failures should be handled by retries or ignoring the
   * error to skip the Kafka message.
   *
   * Usage example:
   *
   * @param settings Settings for creating a [[Consumer]]
   * @param subscription Topic subscription parameters
   * @param f Function that returns the effect to execute for each message. It is passed the key and value
   * @tparam K Type of keys (an implicit [[Serde]] should be in scope)
   * @tparam V Type of values (an implicit [[Serde]] should be in scope)
   * @return Effect that completes with a unit value only when interrupted. May fail when the [[Consumer]] fails.
   *
   *         TODO guarantee that all processed effects get a commit
   */
  def consumeM[R, K: Serde, V: Serde](
    subscription: Subscription,
    settings: ConsumerSettings
  )(f: (K, V) => ZIO[R, Nothing, Unit]): ZIO[R with Clock with Blocking with Console, Throwable, Unit] =
    ZStream
      .managed(Consumer.make[K, V](settings))
      .flatMap { consumer =>
        ZStream
          .fromEffect(consumer.subscribe(subscription))
          .flatMap { _ =>
            consumer.partitioned.flatMapPar(Int.MaxValue, outputBuffer = settings.perPartitionChunkPrefetch) {
              case (partition @ _, partitionStream) =>
                partitionStream.mapM {
                  case CommittableRecord(record, offset) =>
                    f(record.key(), record.value()).as(offset)
                }.flattenChunks
            }
          }
      }
      .aggregate(ZSink.foldLeft[Offset, OffsetBatch](OffsetBatch.empty)(_ merge _))
      .mapM(_.commit)
      .tap(_ => putStrLn(s"Commit done"))
      .run(ZSink.drain)
}
