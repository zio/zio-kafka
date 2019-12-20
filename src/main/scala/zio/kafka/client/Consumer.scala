package zio.kafka.client

import org.apache.kafka.clients.consumer.{
  ConsumerRebalanceListener,
  ConsumerRecords,
  OffsetAndMetadata,
  OffsetAndTimestamp
}
import org.apache.kafka.common.{ PartitionInfo, TopicPartition }
import zio._
import zio.blocking.Blocking
import zio.clock.Clock
import zio.duration._
import zio.kafka.client.serde.Deserializer
import zio.kafka.client.diagnostics.Diagnostics
import zio.kafka.client.internal.Runloop.Deps
import zio.kafka.client.internal.{ ConsumerAccess, Runloop }
import zio.stream._

import scala.jdk.CollectionConverters._
import scala.collection.compat._

/**
 * ZIO wrapper around the kafka Consumer
 *
 * Provides thread-safety and proper handling of blocking operations
 *
 * See [[ConsumerStream]] for more high-level usage of the consumer
 */
class Consumer private[zio] (
  private[zio] val consumer: ConsumerAccess
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
    consumer.withConsumer(
      _.beginningOffsets(partitions.asJava, timeout.asJava).asScala.view.mapValues(_.longValue()).toMap
    )

  /**
   * Retrieve the last committed offset for the given topic-partitions
   */
  def committed(
    partitions: Set[TopicPartition],
    timeout: Duration = Duration.Infinity
  ): BlockingTask[Map[TopicPartition, Option[OffsetAndMetadata]]] =
    consumer.withConsumer(
      _.committed(partitions.asJava, timeout.asJava).asScala.toMap.view.mapValues(Option.apply).toMap
    )

  def endOffsets(
    partitions: Set[TopicPartition],
    timeout: Duration = Duration.Infinity
  ): BlockingTask[Map[TopicPartition, Long]] =
    consumer.withConsumer { eo =>
      val offs = eo.endOffsets(partitions.asJava, timeout.asJava)
      offs.asScala.view.mapValues(_.longValue()).toMap
    }

  def listTopics(timeout: Duration = Duration.Infinity): BlockingTask[Map[String, List[PartitionInfo]]] =
    consumer.withConsumer(_.listTopics(timeout.asJava).asScala.view.mapValues(_.asScala.toList).toMap)

  def offsetsForTimes(
    timestamps: Map[TopicPartition, Long],
    timeout: Duration = Duration.Infinity
  ): BlockingTask[Map[TopicPartition, OffsetAndTimestamp]] =
    consumer.withConsumer(
      _.offsetsForTimes(timestamps.view.mapValues(Long.box).toMap.asJava, timeout.asJava).asScala.toMap
    )

  def partitionsFor(topic: String, timeout: Duration = Duration.Infinity): BlockingTask[List[PartitionInfo]] =
    consumer.withConsumer(_.partitionsFor(topic, timeout.asJava).asScala.toList)

  def poll(timeout: Duration): BlockingTask[ConsumerRecords[Array[Byte], Array[Byte]]] =
    consumer.withConsumer(_.poll(timeout.asJava))

  def position(partition: TopicPartition, timeout: Duration = Duration.Infinity): BlockingTask[Long] =
    consumer.withConsumer(_.position(partition, timeout.asJava))

  def seek(partition: TopicPartition, offset: Long): BlockingTask[Unit] =
    consumer.withConsumer(_.seek(partition, offset))

  def seekToBeginning(partitions: Set[TopicPartition]): BlockingTask[Unit] =
    consumer.withConsumer(_.seekToBeginning(partitions.asJava))

  def seekToEnd(partitions: Set[TopicPartition]): BlockingTask[Unit] =
    consumer.withConsumer(_.seekToEnd(partitions.asJava))

  def subscribe(subscription: Subscription, rebalanceListener: ConsumerRebalanceListener): BlockingTask[Unit] =
    consumer.withConsumerM { c =>
      subscription match {
        case Subscription.Pattern(pattern)        => ZIO(c.subscribe(pattern.pattern, rebalanceListener))
        case Subscription.Topics(topics)          => ZIO(c.subscribe(topics.asJava, rebalanceListener))
        case Subscription.Manual(topicPartitions) => ZIO(c.assign(topicPartitions.asJava))
      }
    }

  def subscription: BlockingTask[Set[String]] =
    consumer.withConsumer(_.subscription().asScala.toSet)

  def unsubscribe: BlockingTask[Unit] =
    consumer.withConsumer(_.unsubscribe())
}

object Consumer {
  def make(
    settings: ConsumerSettings
  ): ZManaged[Clock with Blocking, Throwable, Consumer] =
    ConsumerAccess
      .make(settings)
      .map(new Consumer(_))
}

object ConsumerStream {

  /**
   * Create a stream with all messages on the subscribed topic-partitions
   *
   * The stream will emit messages from all topic-partitions interleaved. Per-partition
   * record order is guaranteed, but the topic-partition interleaving is non-deterministic.
   *
   * The stream can be completed by calling [[stopConsumption]].
   *
   * @param settings Settings for creating a [[Consumer]]
   * @param subscription Topic subscription parameters
   * @param keyDeserializer Deserializer for the record keys
   * @param valueDeserializer Deserializer for the record values
   * @tparam R Environment required by the serializers
   * @tparam K Type of record keys
   * @tparam V Type of record values
   * @return
   */
  def plain[R, K, V](
    settings: ConsumerSettings,
    subscription: Subscription,
    keyDeserializer: Deserializer[R, K],
    valueDeserializer: Deserializer[R, V],
    offsetRetrieval: OffsetRetrieval = OffsetRetrieval.Auto(),
    diagnostics: Diagnostics = Diagnostics.NoOp
  ): ZManaged[Clock with Blocking, Throwable, (Control, ZStreamChunk[R, Throwable, CommittableRecord[K, V]])] =
    partitioned[R, K, V](settings, subscription, keyDeserializer, valueDeserializer, offsetRetrieval, diagnostics).map {
      case (control, stream) =>
        (control, ZStreamChunk(stream.flatMapPar(n = Int.MaxValue, outputBuffer = 1)(_._2.chunks)))
    }

  trait Control {
    def stopConsumption: Task[Unit]
  }

  /**
   * Create a stream with messages on the subscribed topic-partitions by topic-partition
   *
   * The top-level stream will emit new topic-partition streams for each topic-partition that is assigned
   * to this consumer. This is subject to consumer rebalancing, unless a manual subscription
   * was made. When rebalancing occurs, new topic-partition streams may be emitted and existing
   * streams may be completed.
   *
   * All streams can be completed by calling [[Control.stopConsumption]].
   *
   * @param settings Settings for creating a [[Consumer]]
   * @param subscription Topic subscription parameters
   * @param keyDeserializer Deserializer for the record keys
   * @param valueDeserializer Deserializer for the record values
   * @tparam R Environment required by the serializers
   * @tparam K Type of record keys
   * @tparam V Type of record values
   * @return
   */
  def partitioned[R, K, V](
    settings: ConsumerSettings,
    subscription: Subscription,
    keyDeserializer: Deserializer[R, K],
    valueDeserializer: Deserializer[R, V],
    offsetRetrieval: OffsetRetrieval = OffsetRetrieval.Auto(),
    diagnostics: Diagnostics = Diagnostics.NoOp
  ): ZManaged[
    Clock with Blocking,
    Throwable,
    (Control, ZStream[Any, Throwable, (TopicPartition, ZStreamChunk[R, Throwable, CommittableRecord[K, V]])])
  ] = {
    // For manual subscriptions we have to do some manual work before starting the run loop
    def subscribe(consumer: Consumer, deps: Deps, runtime: Runtime[Blocking]) =
      consumer.subscribe(subscription, deps.rebalanceListener.toConsumerRebalanceListener(runtime)) *>
        (subscription match {
          // TODO this is a bit duplicate to Runloop
          case Subscription.Manual(topicPartitions) =>
            ZIO.foreach_(topicPartitions)(deps.newPartitionStream) *> {
              offsetRetrieval match {
                case OffsetRetrieval.Auto(_) => ZIO.unit
                case OffsetRetrieval.Manual(getOffsets) =>
                  getOffsets(topicPartitions).flatMap { offsets =>
                    consumer.consumer.withConsumerM { c =>
                      ZIO.traverse(offsets) { case (tp, offset) => ZIO(c.seek(tp, offset)) }
                    }
                  }
              }
            }
          case _ => ZIO.unit
        })

    for {
      wrapper  <- ConsumerAccess.make(settings)
      consumer = new Consumer(wrapper)
      deps <- Runloop.Deps.make(
               wrapper,
               settings.pollInterval,
               settings.pollTimeout,
               diagnostics,
               offsetRetrieval
             )
      _       <- ZIO.runtime[Blocking].flatMap(subscribe(consumer, deps, _)).toManaged_
      runloop <- Runloop(deps)
      stream = ZStream
        .fromQueue(runloop.deps.partitions)
        .unTake
        .map {
          case (tp, partition) =>
            val partitionStream =
              if (settings.perPartitionChunkPrefetch <= 0) partition
              else ZStreamChunk(partition.chunks.buffer(settings.perPartitionChunkPrefetch))

            tp -> partitionStream.mapM(_.deserializeWith(keyDeserializer, valueDeserializer))
        }
        .ensuringFirst(runloop.deps.gracefulShutdown)
      control = new Control {
        override def stopConsumption: Task[Unit] = runloop.deps.gracefulShutdown
      }
    } yield (control, stream)
  }

  /**
   * Execute an effect for each record and commit the offset after processing
   *
   * This method is the easiest way of processing messages on a Kafka topic.
   *
   * Messages on a single partition are processed sequentially, while the processing of
   * multiple partitions happens in parallel.
   *
   * Offsets are committed after execution of the effect. They are batched when a commit action is in progress
   * to avoid backpressuring the stream. When commits fail due to a org.apache.kafka.clients.consumer.RetriableCommitFailedException they are
   * retried according to commitRetryPolicy
   *
   * The effect should absorb any failures. Failures should be handled by retries or ignoring the
   * error, which will result in the Kafka message being skipped.
   *
   * Messages are processed with 'at least once' consistency: it is not guaranteed that every message
   * that is processed by the effect has a corresponding offset commit before stream termination.
   * TODO is that still true..? Or can we guarantee it with graceful shutdown?
   *
   * Usage example:
   *
   * TODO UPDATE
   * {{{
   * val settings: ConsumerSettings = ???
   * val subscription = Subscription.Topics(Set("my-kafka-topic"))
   *
   * val consumerFiber = Consumer.consumeWith(settings, subscription, Serdes.string, Serdes.string) { case (key, value) =>
   *   // Process the received record here
   *   putStrLn(s"Received record: \${key}: \${value}")
   * }
   * }}}
   *
   * @param settings Settings for creating a [[Consumer]]
   * @param subscription Topic subscription parameters
   * @param keyDeserializer Deserializer for the key of the messages
   * @param valueDeserializer Deserializer for the value of the messages
   * @param commitRetryPolicy Retry commits that failed due to a RetriableCommitFailedException according to this schedule
   * @param f Function that returns the effect to execute for each message. It is passed the key and value
   * @tparam R Environment for the consuming effect
   * @tparam R1 Environment for the deserializers
   * @tparam K Type of keys (an implicit `Deserializer` should be in scope)
   * @tparam V Type of values (an implicit `Deserializer` should be in scope)
   * @return Fiber Effect that completes with a unit value only when interrupted. May fail when the [[Consumer]] fails.
   */
  def consumeWith[R, R1, K, V](
    settings: ConsumerSettings,
    subscription: Subscription,
    keyDeserializer: Deserializer[R1, K],
    valueDeserializer: Deserializer[R1, V],
    offsetRetrieval: OffsetRetrieval = OffsetRetrieval.Auto(),
    commitRetryPolicy: Schedule[Clock, Any, Any] = Schedule.exponential(1.second) && Schedule.recurs(3)
  )(f: (K, V) => ZIO[R, Nothing, Unit]): ZIO[R with R1 with Clock with Blocking, Throwable, Unit] =
    partitioned[R1, K, V](settings, subscription, keyDeserializer, valueDeserializer, offsetRetrieval).use {
      case (_, stream) =>
        stream
          .flatMapPar(Int.MaxValue, outputBuffer = settings.perPartitionChunkPrefetch) {
            case (_, partitionStream) =>
              partitionStream.mapM {
                case CommittableRecord(record, offset) =>
                  f(record.key(), record.value()).as(offset)
              }.flattenChunks
          }
          .aggregateAsync(offsetBatches)
          .mapM(_.commitOrRetry(commitRetryPolicy))
          .runDrain
    }

  sealed trait OffsetRetrieval

  object OffsetRetrieval {
    // TODO process the strategy in the consumer settings be included here?
    final case class Auto(reset: AutoOffsetStrategy = AutoOffsetStrategy.Latest)                extends OffsetRetrieval
    final case class Manual(getOffsets: Set[TopicPartition] => Task[Map[TopicPartition, Long]]) extends OffsetRetrieval
  }

  sealed trait AutoOffsetStrategy

  object AutoOffsetStrategy {
    case object Earliest extends AutoOffsetStrategy
    case object Latest   extends AutoOffsetStrategy
    case object None     extends AutoOffsetStrategy
  }

  val offsetBatches: ZSink[Any, Nothing, Nothing, Offset, OffsetBatch] =
    ZSink.foldLeft[Offset, OffsetBatch](OffsetBatch.empty)(_ merge _)
}
