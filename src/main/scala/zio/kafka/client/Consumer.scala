package zio.kafka.client

import org.apache.kafka.clients.consumer.OffsetAndTimestamp
import org.apache.kafka.common.{ PartitionInfo, TopicPartition }
import zio._
import zio.blocking.Blocking
import zio.clock.Clock
import zio.duration._
import zio.kafka.client.serde.Deserializer
import zio.kafka.client.diagnostics.Diagnostics
import zio.kafka.client.internal.{ ConsumerAccess, Runloop }
import zio.stream._

import scala.collection.JavaConverters._

class Consumer private (
  private val consumer: ConsumerAccess,
  private val settings: ConsumerSettings,
  private val runloop: Runloop
) {
  self =>

  /**
   * Returns the topic-partitions that this consumer is currently assigned.
   *
   * This is subject to consumer rebalancing, unless using a manual subscription.
   */
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
    consumer.withConsumer { eo =>
      val offs = eo.endOffsets(partitions.asJava, timeout.asJava)
      offs.asScala.mapValues(_.longValue()).toMap
    }

  /**
   * Stops consumption of data, drains buffered records, and ends the attached
   * streams while still serving commit requests.
   */
  def stopConsumption: UIO[Unit] =
    runloop.deps.gracefulShutdown

  def listTopics(timeout: Duration = Duration.Infinity): BlockingTask[Map[String, List[PartitionInfo]]] =
    consumer.withConsumer(_.listTopics(timeout.asJava).asScala.mapValues(_.asScala.toList).toMap)

  def offsetsForTimes(
    timestamps: Map[TopicPartition, Long],
    timeout: Duration = Duration.Infinity
  ): BlockingTask[Map[TopicPartition, OffsetAndTimestamp]] =
    consumer.withConsumer(_.offsetsForTimes(timestamps.mapValues(Long.box).asJava, timeout.asJava).asScala.toMap)

  /**
   * Create a stream with messages on the subscribed topic-partitions by topic-partition
   *
   * The top-level stream will emit new topic-partition streams for each topic-partition that is assigned
   * to this consumer. This is subject to consumer rebalancing, unless a manual subscription
   * was made. When rebalancing occurs, new topic-partition streams may be emitted and existing
   * streams may be completed.
   *
   * All streams can be completed by calling [[stopConsumption]].
   *
   * @param keyDeserializer Deserializer for the record keys
   * @param valueDeserializer Deserializer for the record values
   * @tparam R Environment required by the serializers
   * @tparam K Type of record keys
   * @tparam V Type of record values
   * @return
   */
  def partitionedStream[R, K, V](
    keyDeserializer: Deserializer[R, K],
    valueDeserializer: Deserializer[R, V]
  ): ZStream[
    Clock with Blocking,
    Throwable,
    (TopicPartition, ZStreamChunk[R, Throwable, CommittableRecord[K, V]])
  ] =
    ZStream
      .fromQueue(runloop.deps.partitions)
      .unTake
      .map {
        case (tp, partition) =>
          val partitionStream =
            if (settings.perPartitionChunkPrefetch <= 0) partition
            else ZStreamChunk(partition.chunks.buffer(settings.perPartitionChunkPrefetch))

          tp -> partitionStream.mapM(_.deserializeWith(keyDeserializer, valueDeserializer))
      }

  def partitionsFor(topic: String, timeout: Duration = Duration.Infinity): BlockingTask[List[PartitionInfo]] =
    consumer.withConsumer(_.partitionsFor(topic, timeout.asJava).asScala.toList)

  def position(partition: TopicPartition, timeout: Duration = Duration.Infinity): BlockingTask[Long] =
    consumer.withConsumer(_.position(partition, timeout.asJava))

  /**
   * Create a stream with all messages on the subscribed topic-partitions
   *
   * The stream will emit messages from all topic-partitions interleaved. Per-partition
   * record order is guaranteed, but the topic-partition interleaving is non-deterministic.
   *
   * The stream can be completed by calling [[stopConsumption]].
   *
   * @param keyDeserializer Deserializer for the record keys
   * @param valueDeserializer Deserializer for the record values
   * @tparam R Environment required by the serializers
   * @tparam K Type of record keys
   * @tparam V Type of record values
   * @return
   */
  def plainStream[R, K, V](
    keyDeserializer: Deserializer[R, K],
    valueDeserializer: Deserializer[R, V]
  ): ZStreamChunk[R with Clock with Blocking, Throwable, CommittableRecord[K, V]] =
    ZStreamChunk(
      partitionedStream[R, K, V](keyDeserializer, valueDeserializer)
        .flatMapPar(n = Int.MaxValue, outputBuffer = 1)(_._2.chunks)
    )

  def seek(partition: TopicPartition, offset: Long): BlockingTask[Unit] =
    consumer.withConsumer(_.seek(partition, offset))

  def seekToBeginning(partitions: Set[TopicPartition]): BlockingTask[Unit] =
    consumer.withConsumer(_.seekToBeginning(partitions.asJava))

  def seekToEnd(partitions: Set[TopicPartition]): BlockingTask[Unit] =
    consumer.withConsumer(_.seekToEnd(partitions.asJava))

  def subscribe(subscription: Subscription): BlockingTask[Unit] =
    consumer.withConsumer { c =>
      subscription match {
        case Subscription.Pattern(pattern) => c.subscribe(pattern.pattern, runloop.deps.rebalanceListener)
        case Subscription.Topics(topics)   => c.subscribe(topics.asJava, runloop.deps.rebalanceListener)
      }
    }

  def subscribeAnd(subscription: Subscription): SubscribedConsumer =
    new SubscribedConsumer(subscribe(subscription).as(self))

  def subscription: BlockingTask[Set[String]] =
    consumer.withConsumer(_.subscription().asScala.toSet)

  def unsubscribe: BlockingTask[Unit] =
    consumer.withConsumer(_.unsubscribe())
}

object Consumer {
  val offsetBatches: ZSink[Any, Nothing, Nothing, Offset, OffsetBatch] =
    ZSink.foldLeft[Offset, OffsetBatch](OffsetBatch.empty)(_ merge _)

  def make(
    settings: ConsumerSettings,
    diagnostics: Diagnostics = Diagnostics.NoOp
  ): ZManaged[Clock with Blocking, Throwable, Consumer] =
    for {
      wrapper <- ConsumerAccess.make(settings)
      deps <- Runloop.Deps.make(
               wrapper,
               settings.pollInterval,
               settings.pollTimeout,
               diagnostics
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
   * Offsets are committed after execution of the effect. They are batched when a commit action is in progress
   * to avoid backpressuring the stream.
   *
   * The effect should must absorb any failures. Failures should be handled by retries or ignoring the
   * error, which will result in the Kafka message being skipped.
   *
   * Messages are processed with 'at least once' consistency: it is not guaranteed that every message
   * that is processed by the effect has a corresponding offset commit before stream termination.
   *
   * Usage example:
   *
   * {{{
   * val settings: ConsumerSettings = ???
   * val subscription = Subscription.Topics(Set("my-kafka-topic"))
   *
   * val consumerIO = Consumer.consumeWith(settings, subscription, Serdes.string, Serdes.string) { case (key, value) =>
   *   // Process the received record here
   *   putStrLn(s"Received record: \${key}: \${value}")
   * }
   * }}}
   *
   * @param settings Settings for creating a [[Consumer]]
   * @param subscription Topic subscription parameters
   * @param keyDeserializer Deserializer for the key of the messages
   * @param valueDeserializer Deserializer for the value of the messages
   * @param f Function that returns the effect to execute for each message. It is passed the key and value
   * @tparam R Environment
   * @tparam K Type of keys (an implicit `Deserializer` should be in scope)
   * @tparam V Type of values (an implicit `Deserializer` should be in scope)
   * @return Effect that completes with a unit value only when interrupted. May fail when the [[Consumer]] fails.
   */
  def consumeWith[R, R1, K, V](
    settings: ConsumerSettings,
    subscription: Subscription,
    keyDeserializer: Deserializer[R1, K],
    valueDeserializer: Deserializer[R1, V]
  )(
    f: (K, V) => ZIO[R, Nothing, Unit]
  ): ZIO[R with R1 with Blocking with Clock, Throwable, Unit] =
    ZStream
      .managed(Consumer.make(settings))
      .flatMap { consumer =>
        ZStream
          .fromEffect(consumer.subscribe(subscription))
          .flatMap { _ =>
            consumer
              .partitionedStream[R1, K, V](keyDeserializer, valueDeserializer)
              .flatMapPar(Int.MaxValue, outputBuffer = settings.perPartitionChunkPrefetch) {
                case (_, partitionStream) =>
                  partitionStream.mapM {
                    case CommittableRecord(record, offset) =>
                      f(record.key(), record.value()).as(offset)
                  }.flattenChunks
              }
          }
      }
      .aggregateAsync(offsetBatches)
      .mapM(_.commit)
      .runDrain
}
