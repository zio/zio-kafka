package zio.kafka.consumer

import org.apache.kafka.clients.consumer.{ ConsumerRecord, KafkaConsumer, OffsetAndMetadata, OffsetAndTimestamp }
import org.apache.kafka.common._
import zio._
import zio.kafka.consumer.diagnostics.DiagnosticEvent.Finalization
import zio.kafka.consumer.diagnostics.Diagnostics
import zio.kafka.consumer.internal.{ ConsumerAccess, RunloopAccess }
import zio.kafka.serde.{ Deserializer, Serde }
import zio.kafka.utils.SslHelper
import zio.stream._

import scala.jdk.CollectionConverters._
import scala.util.control.NoStackTrace

trait Consumer {

  /**
   * Returns the topic-partitions that this consumer is currently assigned.
   *
   * This is subject to consumer rebalancing, unless using a manual subscription.
   */
  def assignment: Task[Set[TopicPartition]]

  def beginningOffsets(
    partitions: Set[TopicPartition],
    timeout: Duration = Duration.Infinity
  ): Task[Map[TopicPartition, Long]]

  def endOffsets(
    partitions: Set[TopicPartition],
    timeout: Duration = Duration.Infinity
  ): Task[Map[TopicPartition, Long]]

  /**
   * Retrieve the last committed offset for the given topic-partitions
   */
  def committed(
    partitions: Set[TopicPartition],
    timeout: Duration = Duration.Infinity
  ): Task[Map[TopicPartition, Option[OffsetAndMetadata]]]

  def listTopics(timeout: Duration = Duration.Infinity): Task[Map[String, List[PartitionInfo]]]

  /**
   * Create a stream that emits chunks whenever new partitions are assigned to this consumer.
   *
   * The top-level stream will emit chunks whenever the consumer rebalances, unless a manual subscription was made. When
   * rebalancing occurs, new topic-partition streams may be emitted and existing streams may be completed.
   *
   * All streams can be completed by calling [[stopConsumption]].
   *
   * Multiple subscriptions on one Consumer are supported, as long as the subscriptions are of the same type (topics,
   * patterns, manual). Each subscription will only receive messages from the topic-partitions that match the
   * subscription. When subscriptions overlap, kafka records will be divided over the overlapping subscriptions
   * non-deterministically.
   *
   * On completion of the stream, the consumer is unsubscribed. In case of multiple subscriptions, the total consumer
   * subscription is changed to exclude this subscription.
   */
  def partitionedAssignmentStream[R, K, V](
    subscription: Subscription,
    keyDeserializer: Deserializer[R, K],
    valueDeserializer: Deserializer[R, V]
  ): Stream[Throwable, Chunk[(TopicPartition, ZStream[R, Throwable, CommittableRecord[K, V]])]]

  /**
   * Create a stream with messages on the subscribed topic-partitions by topic-partition
   *
   * The top-level stream will emit new topic-partition streams for each topic-partition that is assigned to this
   * consumer. This is subject to consumer rebalancing, unless a manual subscription was made. When rebalancing occurs,
   * new topic-partition streams may be emitted and existing streams may be completed.
   *
   * All streams can be completed by calling [[stopConsumption]].
   *
   * Multiple subscriptions on one Consumer are supported, as long as the subscriptions are of the same type (topics,
   * patterns, manual). Each subscription will only receive messages from the topic-partitions that match the
   * subscription. When subscriptions overlap, kafka records will be divided over the overlapping subscriptions
   * non-deterministically.
   *
   * On completion of the stream, the consumer is unsubscribed. In case of multiple subscriptions, the total consumer
   * subscription is changed to exclude this subscription.
   */
  def partitionedStream[R, K, V](
    subscription: Subscription,
    keyDeserializer: Deserializer[R, K],
    valueDeserializer: Deserializer[R, V]
  ): Stream[Throwable, (TopicPartition, ZStream[R, Throwable, CommittableRecord[K, V]])]

  /**
   * Create a stream with all messages on the subscribed topic-partitions
   *
   * The stream will emit messages from all topic-partitions interleaved. Per-partition record order is guaranteed, but
   * the topic-partition interleaving is non-deterministic.
   *
   * Up to `bufferSize` chunks may be buffered in memory by this operator.
   *
   * The stream can be completed by calling [[stopConsumption]].
   *
   * Multiple subscriptions on one Consumer are supported, as long as the subscriptions are of the same type (topics,
   * patterns, manual). Each subscription will only receive messages from the topic-partitions that match the
   * subscription. When subscriptions overlap, kafka records will be divided over the overlapping subscriptions
   * non-deterministically.
   *
   * On completion of the stream, the consumer is unsubscribed. In case of multiple subscriptions, the total consumer
   * subscription is changed to exclude this subscription.
   */
  def plainStream[R, K, V](
    subscription: Subscription,
    keyDeserializer: Deserializer[R, K],
    valueDeserializer: Deserializer[R, V],
    bufferSize: Int = 4
  ): ZStream[R, Throwable, CommittableRecord[K, V]]

  /**
   * Stops consumption of data, drains buffered records, and ends the attached streams while still serving commit
   * requests.
   */
  def stopConsumption: UIO[Unit]

  /**
   * See [[Consumer.consumeWith]].
   */
  def consumeWith[R: EnvironmentTag, R1: EnvironmentTag, K, V](
    subscription: Subscription,
    keyDeserializer: Deserializer[R, K],
    valueDeserializer: Deserializer[R, V],
    commitRetryPolicy: Schedule[Any, Any, Any] = Schedule.exponential(1.second) && Schedule.recurs(3)
  )(
    f: ConsumerRecord[K, V] => URIO[R1, Unit]
  ): ZIO[R & R1, Throwable, Unit]

  /**
   * Look up the offsets for the given partitions by timestamp. The returned offset for each partition is the earliest
   * offset whose timestamp is greater than or equal to the given timestamp in the corresponding partition.
   *
   * The consumer does not have to be assigned the partitions. If no messages exist yet for a partition, it will not
   * exist in the returned map.
   */
  def offsetsForTimes(
    timestamps: Map[TopicPartition, Long],
    timeout: Duration = Duration.Infinity
  ): Task[Map[TopicPartition, OffsetAndTimestamp]]

  def partitionsFor(topic: String, timeout: Duration = Duration.Infinity): Task[List[PartitionInfo]]

  def position(partition: TopicPartition, timeout: Duration = Duration.Infinity): Task[Long]

  def subscription: Task[Set[String]]

  /**
   * Expose internal consumer metrics
   */
  def metrics: Task[Map[MetricName, Metric]]
}

object Consumer {
  case object RunloopTimeout extends RuntimeException("Timeout in Runloop") with NoStackTrace
  case object CommitTimeout  extends RuntimeException("Commit timeout") with NoStackTrace

  val offsetBatches: ZSink[Any, Nothing, Offset, Nothing, OffsetBatch] =
    ZSink.foldLeft[Offset, OffsetBatch](OffsetBatch.empty)(_ add _)

  def live: RLayer[ConsumerSettings & Diagnostics, Consumer] =
    ZLayer.scoped {
      for {
        settings    <- ZIO.service[ConsumerSettings]
        diagnostics <- ZIO.service[Diagnostics]
        consumer    <- make(settings, diagnostics)
      } yield consumer
    }

  def make(
    settings: ConsumerSettings,
    diagnostics: Diagnostics = Diagnostics.NoOp
  ): ZIO[Scope, Throwable, Consumer] =
    for {
      _              <- ZIO.addFinalizer(diagnostics.emit(Finalization.ConsumerFinalized))
      _              <- SslHelper.validateEndpoint(settings.bootstrapServers, settings.properties)
      consumerAccess <- ConsumerAccess.make(settings)
      runloopAccess  <- RunloopAccess.make(settings, consumerAccess, diagnostics)
    } yield new ConsumerLive(consumerAccess, runloopAccess)

  /**
   * Create a zio-kafka Consumer from an org.apache.kafka KafkaConsumer
   *
   * You are responsible for creating and closing the KafkaConsumer
   */
  def fromJavaConsumer(
    javaConsumer: KafkaConsumer[Array[Byte], Array[Byte]],
    settings: ConsumerSettings,
    diagnostics: Diagnostics = Diagnostics.NoOp
  ): ZIO[Scope, Throwable, Consumer] =
    for {
      _              <- ZIO.addFinalizer(diagnostics.emit(Finalization.ConsumerFinalized))
      consumerAccess <- ConsumerAccess.make(javaConsumer)
      runloopAccess  <- RunloopAccess.make(settings, consumerAccess, diagnostics)
    } yield new ConsumerLive(consumerAccess, runloopAccess)

  /**
   * Accessor method
   */
  def assignment: RIO[Consumer, Set[TopicPartition]] =
    ZIO.serviceWithZIO(_.assignment)

  /**
   * Accessor method
   */
  def beginningOffsets(
    partitions: Set[TopicPartition],
    timeout: Duration = Duration.Infinity
  ): RIO[Consumer, Map[TopicPartition, Long]] =
    ZIO.serviceWithZIO(_.beginningOffsets(partitions, timeout))

  /**
   * Accessor method
   */
  def committed(
    partitions: Set[TopicPartition],
    timeout: Duration = Duration.Infinity
  ): RIO[Consumer, Map[TopicPartition, Option[OffsetAndMetadata]]] =
    ZIO.serviceWithZIO(_.committed(partitions, timeout))

  /**
   * Accessor method
   */
  def endOffsets(
    partitions: Set[TopicPartition],
    timeout: Duration = Duration.Infinity
  ): RIO[Consumer, Map[TopicPartition, Long]] =
    ZIO.serviceWithZIO(_.endOffsets(partitions, timeout))

  /**
   * Accessor method
   */
  def listTopics(
    timeout: Duration = Duration.Infinity
  ): RIO[Consumer, Map[String, List[PartitionInfo]]] =
    ZIO.serviceWithZIO(_.listTopics(timeout))

  /**
   * Accessor method
   */
  def partitionedAssignmentStream[R, K, V](
    subscription: Subscription,
    keyDeserializer: Deserializer[R, K],
    valueDeserializer: Deserializer[R, V]
  ): ZStream[Consumer, Throwable, Chunk[(TopicPartition, ZStream[R, Throwable, CommittableRecord[K, V]])]] =
    ZStream.serviceWithStream[Consumer](_.partitionedAssignmentStream(subscription, keyDeserializer, valueDeserializer))

  /**
   * Accessor method
   */
  def partitionedStream[R, K, V](
    subscription: Subscription,
    keyDeserializer: Deserializer[R, K],
    valueDeserializer: Deserializer[R, V]
  ): ZStream[
    Consumer,
    Throwable,
    (TopicPartition, ZStream[R, Throwable, CommittableRecord[K, V]])
  ] =
    ZStream.serviceWithStream[Consumer](_.partitionedStream(subscription, keyDeserializer, valueDeserializer))

  /**
   * Accessor method
   */
  def plainStream[R, K, V](
    subscription: Subscription,
    keyDeserializer: Deserializer[R, K],
    valueDeserializer: Deserializer[R, V],
    bufferSize: Int = 4
  ): ZStream[R & Consumer, Throwable, CommittableRecord[K, V]] =
    ZStream.serviceWithStream[Consumer](
      _.plainStream(subscription, keyDeserializer, valueDeserializer, bufferSize)
    )

  /**
   * Accessor method
   */
  def stopConsumption: RIO[Consumer, Unit] =
    ZIO.serviceWithZIO(_.stopConsumption)

  /**
   * Execute an effect for each record and commit the offset after processing
   *
   * This method is the easiest way of processing messages on a Kafka topic.
   *
   * Messages on a single partition are processed sequentially, while the processing of multiple partitions happens in
   * parallel.
   *
   * Offsets are committed after execution of the effect. They are batched when a commit action is in progress to avoid
   * backpressuring the stream. When commits fail due to a
   * org.apache.kafka.clients.consumer.RetriableCommitFailedException they are retried according to commitRetryPolicy
   *
   * The effect should absorb any failures. Failures should be handled by retries or ignoring the error, which will
   * result in the Kafka message being skipped.
   *
   * Messages are processed with 'at least once' consistency: it is not guaranteed that every message that is processed
   * by the effect has a corresponding offset commit before stream termination.
   *
   * Usage example:
   *
   * {{{
   * val settings: ConsumerSettings = ???
   * val subscription = Subscription.Topics(Set("my-kafka-topic"))
   *
   * val consumerIO = Consumer.consumeWith(settings, subscription, Serdes.string, Serdes.string) { record =>
   *   // Process the received record here
   *   putStrLn(s"Received record: \${record.key()}: \${record.value()}")
   * }
   * }}}
   *
   * @param settings
   *   Settings for creating a [[Consumer]]
   * @param subscription
   *   Topic subscription parameters
   * @param keyDeserializer
   *   Deserializer for the key of the messages
   * @param valueDeserializer
   *   Deserializer for the value of the messages
   * @param commitRetryPolicy
   *   Retry commits that failed due to a RetriableCommitFailedException according to this schedule
   * @param f
   *   Function that returns the effect to execute for each message. It is passed the
   *   [[org.apache.kafka.clients.consumer.ConsumerRecord]].
   * @tparam R
   *   Environment for the consuming effect
   * @tparam R1
   *   Environment for the deserializers
   * @tparam K
   *   Type of keys (an implicit `Deserializer` should be in scope)
   * @tparam V
   *   Type of values (an implicit `Deserializer` should be in scope)
   * @return
   *   Effect that completes with a unit value only when interrupted. May fail when the [[Consumer]] fails.
   */
  def consumeWith[R: EnvironmentTag, R1: EnvironmentTag, K, V](
    settings: ConsumerSettings,
    subscription: Subscription,
    keyDeserializer: Deserializer[R, K],
    valueDeserializer: Deserializer[R, V],
    commitRetryPolicy: Schedule[Any, Any, Any] = Schedule.exponential(1.second) && Schedule.recurs(3)
  )(f: ConsumerRecord[K, V] => URIO[R1, Unit]): RIO[R & R1, Unit] =
    ZIO.scoped[R & R1] {
      Consumer
        .make(settings)
        .flatMap(_.consumeWith[R, R1, K, V](subscription, keyDeserializer, valueDeserializer, commitRetryPolicy)(f))
    }

  /**
   * Accessor method
   */
  def offsetsForTimes(
    timestamps: Map[TopicPartition, Long],
    timeout: Duration = Duration.Infinity
  ): RIO[Consumer, Map[TopicPartition, OffsetAndTimestamp]] =
    ZIO.serviceWithZIO(_.offsetsForTimes(timestamps, timeout))

  /**
   * Accessor method
   */
  def partitionsFor(
    topic: String,
    timeout: Duration = Duration.Infinity
  ): RIO[Consumer, List[PartitionInfo]] =
    ZIO.serviceWithZIO(_.partitionsFor(topic, timeout))

  /**
   * Accessor method
   */
  def position(
    partition: TopicPartition,
    timeout: Duration = Duration.Infinity
  ): RIO[Consumer, Long] =
    ZIO.serviceWithZIO(_.position(partition, timeout))

  /**
   * Accessor method
   */
  def subscription: RIO[Consumer, Set[String]] =
    ZIO.serviceWithZIO(_.subscription)

  /**
   * Accessor method
   */
  def metrics: RIO[Consumer, Map[MetricName, Metric]] =
    ZIO.serviceWithZIO(_.metrics)

  sealed trait OffsetRetrieval
  object OffsetRetrieval {
    final case class Auto(reset: AutoOffsetStrategy = AutoOffsetStrategy.Latest)                extends OffsetRetrieval
    final case class Manual(getOffsets: Set[TopicPartition] => Task[Map[TopicPartition, Long]]) extends OffsetRetrieval
  }

  sealed trait AutoOffsetStrategy { self =>
    final def toConfig: String = self match {
      case AutoOffsetStrategy.Earliest => "earliest"
      case AutoOffsetStrategy.Latest   => "latest"
      case AutoOffsetStrategy.None     => "none"
    }
  }

  object AutoOffsetStrategy {
    case object Earliest extends AutoOffsetStrategy
    case object Latest   extends AutoOffsetStrategy
    case object None     extends AutoOffsetStrategy
  }
}

private[consumer] final class ConsumerLive private[consumer] (
  consumer: ConsumerAccess,
  runloopAccess: RunloopAccess
) extends Consumer {
  import Consumer._

  override def assignment: Task[Set[TopicPartition]] =
    consumer.withConsumer(_.assignment().asScala.toSet)

  override def beginningOffsets(
    partitions: Set[TopicPartition],
    timeout: Duration = Duration.Infinity
  ): Task[Map[TopicPartition, Long]] =
    consumer.withConsumer(
      _.beginningOffsets(partitions.asJava, timeout.asJava).asScala.map { case (tp, l) =>
        tp -> l.longValue()
      }.toMap
    )

  override def endOffsets(
    partitions: Set[TopicPartition],
    timeout: Duration = Duration.Infinity
  ): Task[Map[TopicPartition, Long]] =
    consumer.withConsumer { eo =>
      val offs = eo.endOffsets(partitions.asJava, timeout.asJava)
      offs.asScala.map { case (k, v) => k -> v.longValue() }.toMap
    }

  override def committed(
    partitions: Set[TopicPartition],
    timeout: Duration = Duration.Infinity
  ): Task[Map[TopicPartition, Option[OffsetAndMetadata]]] =
    consumer.withConsumer(
      _.committed(partitions.asJava, timeout.asJava).asScala.map { case (k, v) => k -> Option(v) }.toMap
    )

  override def listTopics(timeout: Duration = Duration.Infinity): Task[Map[String, List[PartitionInfo]]] =
    consumer.withConsumer(_.listTopics(timeout.asJava).asScala.map { case (k, v) => k -> v.asScala.toList }.toMap)

  override def partitionedAssignmentStream[R, K, V](
    subscription: Subscription,
    keyDeserializer: Deserializer[R, K],
    valueDeserializer: Deserializer[R, V]
  ): Stream[Throwable, Chunk[(TopicPartition, ZStream[R, Throwable, CommittableRecord[K, V]])]] = {
    val onlyByteArraySerdes: Boolean = (keyDeserializer eq Serde.byteArray) && (valueDeserializer eq Serde.byteArray)

    ZStream.unwrapScoped {
      for {
        stream <- runloopAccess.subscribe(subscription)
      } yield stream
        .map(_.exit)
        .flattenExitOption
        .map {
          _.collect {
            case (tp, partitionStream) if Subscription.subscriptionMatches(subscription, tp) =>
              val stream: ZStream[R, Throwable, CommittableRecord[K, V]] =
                if (onlyByteArraySerdes)
                  partitionStream.asInstanceOf[ZStream[R, Throwable, CommittableRecord[K, V]]]
                else partitionStream.mapChunksZIO(_.mapZIO(_.deserializeWith(keyDeserializer, valueDeserializer)))

              tp -> stream
          }
        }
    }
  }

  override def partitionedStream[R, K, V](
    subscription: Subscription,
    keyDeserializer: Deserializer[R, K],
    valueDeserializer: Deserializer[R, V]
  ): ZStream[Any, Throwable, (TopicPartition, ZStream[R, Throwable, CommittableRecord[K, V]])] =
    partitionedAssignmentStream(subscription, keyDeserializer, valueDeserializer).flattenChunks

  override def plainStream[R, K, V](
    subscription: Subscription,
    keyDeserializer: Deserializer[R, K],
    valueDeserializer: Deserializer[R, V],
    bufferSize: Int
  ): ZStream[R, Throwable, CommittableRecord[K, V]] =
    partitionedStream(subscription, keyDeserializer, valueDeserializer).flatMapPar(
      n = Int.MaxValue,
      bufferSize = bufferSize
    )(_._2)

  override def stopConsumption: UIO[Unit] =
    ZIO.logDebug("stopConsumption called") *>
      runloopAccess.stopConsumption

  override def consumeWith[R: EnvironmentTag, R1: EnvironmentTag, K, V](
    subscription: Subscription,
    keyDeserializer: Deserializer[R, K],
    valueDeserializer: Deserializer[R, V],
    commitRetryPolicy: Schedule[Any, Any, Any] = Schedule.exponential(1.second) && Schedule.recurs(3)
  )(
    f: ConsumerRecord[K, V] => URIO[R1, Unit]
  ): ZIO[R & R1, Throwable, Unit] =
    for {
      r <- ZIO.environment[R & R1]
      _ <- partitionedStream(subscription, keyDeserializer, valueDeserializer)
             .flatMapPar(Int.MaxValue) { case (_, partitionStream) =>
               partitionStream.mapChunksZIO(_.mapZIO((c: CommittableRecord[K, V]) => f(c.record).as(c.offset)))
             }
             .provideEnvironment(r)
             .aggregateAsync(offsetBatches)
             .mapZIO(_.commitOrRetry(commitRetryPolicy))
             .runDrain
    } yield ()

  override def offsetsForTimes(
    timestamps: Map[TopicPartition, Long],
    timeout: Duration = Duration.Infinity
  ): Task[Map[TopicPartition, OffsetAndTimestamp]] =
    consumer.withConsumer(
      _.offsetsForTimes(timestamps.map { case (k, v) => k -> Long.box(v) }.asJava, timeout.asJava).asScala.toMap
        // If a partition doesn't exist yet, the map will have 'null' as entry.
        // It's more idiomatic scala to then simply not have that map entry.
        .filter(_._2 != null)
    )

  override def partitionsFor(
    topic: String,
    timeout: Duration = Duration.Infinity
  ): Task[List[PartitionInfo]] =
    consumer.withConsumer { c =>
      val partitions = c.partitionsFor(topic, timeout.asJava)
      if (partitions eq null) List.empty else partitions.asScala.toList
    }

  override def position(partition: TopicPartition, timeout: Duration = Duration.Infinity): Task[Long] =
    consumer.withConsumer(_.position(partition, timeout.asJava))

  override def subscription: Task[Set[String]] =
    consumer.withConsumer(_.subscription().asScala.toSet)

  override def metrics: Task[Map[MetricName, Metric]] =
    consumer.withConsumer(_.metrics().asScala.toMap)

}
