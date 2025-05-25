package zio.kafka.consumer

import org.apache.kafka.clients.consumer.{
  Consumer => JConsumer,
  ConsumerRecord,
  OffsetAndMetadata,
  OffsetAndTimestamp
}
import org.apache.kafka.common._
import zio._
import zio.kafka.consumer.diagnostics.DiagnosticEvent
import zio.kafka.consumer.internal.{ ConsumerAccess, RunloopAccess }
import zio.kafka.diagnostics.Diagnostics
import zio.kafka.diagnostics.internal.ConcurrentDiagnostics
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

  /**
   * The settings used to create this consumer.
   */
  val consumerSettings: ConsumerSettings

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
   * Like [[plainStream]] but returns an object to control the stream.
   *
   * The Scope provided to the returned effect controls the lifetime of the subscription. The subscription is
   * unsubscribed when the scope is ended. Calling [[StreamControl.end]] stops fetching data for the subscription
   * partitions but will not unsubscribe until the Scope is ended.
   *
   * See the docs at https://zio.dev/zio-kafka/consuming-kafka-topics-using-zio-streams for more information.
   *
   * WARNING: this is an EXPERIMENTAL API and may disappear or change in an incompatible way without notice in any
   * zio-kafka version.
   */
  def plainStreamWithControl[R, K, V](
    subscription: Subscription,
    keyDeserializer: Deserializer[R, K],
    valueDeserializer: Deserializer[R, V],
    bufferSize: Int = 4
  ): ZIO[Scope with R, Throwable, StreamControl[R, Throwable, CommittableRecord[K, V]]]

  /**
   * Like [[partitionedStream]] but returns an object to control the stream.
   *
   * The Scope provided to the returned effect controls the lifetime of the subscription. The subscription is
   * unsubscribed when the scope is ended. Calling [[StreamControl.end]] stops fetching data for the subscription
   * partitions but will not unsubscribe until the Scope is ended.
   *
   * See the docs at https://zio.dev/zio-kafka/consuming-kafka-topics-using-zio-streams for more information.
   *
   * WARNING: this is an EXPERIMENTAL API and may disappear or change in an incompatible way without notice in any
   * zio-kafka version.
   */
  def partitionedStreamWithControl[R, K, V](
    subscription: Subscription,
    keyDeserializer: Deserializer[R, K],
    valueDeserializer: Deserializer[R, V]
  ): ZIO[
    Scope with R,
    Throwable,
    StreamControl[R, Throwable, (TopicPartition, ZStream[R, Throwable, CommittableRecord[K, V]])]
  ]

  /**
   * Like [[partitionedAssignmentStream]] but returns an object to control the stream.
   *
   * The Scope provided to the returned effect controls the lifetime of the subscription. The subscription is
   * unsubscribed when the scope is ended. Calling [[StreamControl.end]] stops fetching data for the subscription
   * partitions but will not unsubscribe until the Scope is ended.
   *
   * See the docs at https://zio.dev/zio-kafka/consuming-kafka-topics-using-zio-streams for more information.
   *
   * WARNING: this is an EXPERIMENTAL API and may disappear or change in an incompatible way without notice in any
   * zio-kafka version.
   */
  def partitionedAssignmentStreamWithControl[R, K, V](
    subscription: Subscription,
    keyDeserializer: Deserializer[R, K],
    valueDeserializer: Deserializer[R, V]
  ): ZIO[Scope, Throwable, StreamControl[
    R,
    Throwable,
    Chunk[(TopicPartition, ZStream[R, Throwable, CommittableRecord[K, V]])]
  ]]

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

  /**
   * Register a commit that was done externally, that is, not by this consumer.
   *
   * This method is useful when you want to use rebalance-safe-commits, but you are not committing to the Kafka brokers,
   * but to some external system, for example a relational database.
   *
   * When this consumer is used in combination with a [[zio.kafka.producer.TransactionalProducer]], the transactional
   * producer calls this method when the transaction is committed.
   *
   * See also [[zio.kafka.consumer.ConsumerSettings.withRebalanceSafeCommits]].
   */
  def registerExternalCommits(offsetBatch: OffsetBatch): Task[Unit]
}

object Consumer {

  /** A callback for consumer diagnostic events. */
  type ConsumerDiagnostics = zio.kafka.diagnostics.Diagnostics[DiagnosticEvent]

  /** A diagnostics implementation that does nothing. */
  val NoDiagnostics: ConsumerDiagnostics = zio.kafka.diagnostics.Diagnostics.NoOp

  case object CommitTimeout extends RuntimeException("Commit timeout") with NoStackTrace

  val offsetBatches: ZSink[Any, Nothing, Offset, Nothing, OffsetBatch] =
    ZSink.foldLeft[Offset, OffsetBatch](OffsetBatch.empty)(_ add _)

  def live: RLayer[ConsumerSettings, Consumer] =
    ZLayer.scoped {
      for {
        settings <- ZIO.service[ConsumerSettings]
        consumer <- make(settings)
      } yield consumer
    }

  /**
   * A new consumer.
   */
  def make(settings: ConsumerSettings): ZIO[Scope, Throwable, Consumer] =
    for {
      wrappedDiagnostics <- makeConcurrentDiagnostics(settings.diagnostics)
      _                  <- SslHelper.validateEndpoint(settings.driverSettings)
      consumerAccess     <- ConsumerAccess.make(settings)
      runloopAccess      <- RunloopAccess.make(settings, consumerAccess, wrappedDiagnostics)
    } yield new ConsumerLive(consumerAccess, settings, runloopAccess)

  /**
   * Create a zio-kafka [[Consumer]] from an `org.apache.kafka KafkaConsumer`.
   *
   * You are responsible for all of the following:
   *   - creating and closing the `KafkaConsumer`,
   *   - making sure `auto.commit` is disabled,
   *   - creating `access` as a fair semaphore with a single permit,
   *   - acquire a permit from `access` before using the consumer, and release it afterward,
   *   - not using the following consumer methods: `subscribe`, `unsubscribe`, `assign`, `poll`, `commit*`, `seek`,
   *     `pause`, `resume`, and `enforceRebalance`,
   *   - keeping the consumer config given to the java consumer in sync with the properties in `settings` (for example
   *     by constructing `settings` with `ConsumerSettings(bootstrapServers).withProperties(config)`).
   *
   * Any deviation of these rules is likely to cause hard to track errors.
   *
   * Semaphore `access` is shared between you and the zio-kafka consumer. Use it as short as possible; while you hold a
   * permit the zio-kafka consumer is blocked.
   *
   * @param javaConsumer
   *   Consumer
   * @param settings
   *   Settings
   * @param access
   *   A Semaphore with 1 permit.
   */
  def fromJavaConsumerWithPermit(
    javaConsumer: JConsumer[Array[Byte], Array[Byte]],
    settings: ConsumerSettings,
    access: Semaphore
  ): ZIO[Scope, Throwable, Consumer] =
    for {
      wrappedDiagnostics <- makeConcurrentDiagnostics(settings.diagnostics)
      consumerAccess = new ConsumerAccess(javaConsumer, access)
      runloopAccess <- RunloopAccess.make(settings, consumerAccess, wrappedDiagnostics)
    } yield new ConsumerLive(consumerAccess, settings, runloopAccess)

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

  /** See ConsumerSettings.withOffsetRetrieval. */
  sealed trait OffsetRetrieval
  object OffsetRetrieval {
    final case class Auto(reset: AutoOffsetStrategy = AutoOffsetStrategy.Latest) extends OffsetRetrieval
    final case class Manual(
      getOffsets: Set[TopicPartition] => Task[Map[TopicPartition, Long]],
      defaultStrategy: AutoOffsetStrategy = AutoOffsetStrategy.Latest
    ) extends OffsetRetrieval
  }

  sealed trait AutoOffsetStrategy { self =>
    final def toConfig: String = self match {
      case AutoOffsetStrategy.Earliest => "earliest"
      case AutoOffsetStrategy.Latest   => "latest"
      case AutoOffsetStrategy.None     => "none"
    }
  }

  /** See ConsumerSettings.withOffsetRetrieval. */
  object AutoOffsetStrategy {
    case object Earliest extends AutoOffsetStrategy
    case object Latest   extends AutoOffsetStrategy
    case object None     extends AutoOffsetStrategy
  }

  /**
   * Takes a StreamControl for some stream and runs the given ZIO workflow on that stream such that, when interrupted,
   * stops fetching records and gracefully waits for the ZIO workflow to complete.
   *
   * This is useful for running streams from within your application's Main class, such that streams are cleanly stopped
   * when the application is shutdown (for example by your container runtime).
   *
   * WARNING: this is an EXPERIMENTAL API and may disappear or change in an incompatible way without notice in any
   * zio-kafka version.
   *
   * @param streamControl
   *   Result of one of the Consumer's methods returning a [[StreamControl]]
   * @param shutdownTimeout
   *   Timeout for the workflow to complete after initiating the graceful shutdown
   * @param withStream
   *   Takes the stream as input and returns a ZIO workflow that processes the stream. As in most programs the given
   *   workflow runs until an external interruption, the result value (Any type) is meaningless. `withStream` is
   *   typically something like:
   *   {{{
   *     stream => stream.mapZIO(record => ZIO.debug(record))
   *                     .mapZIO(record => record.offset.commit)
   *   }}}
   */
  def runWithGracefulShutdown[R1, R2, E, A](
    control: StreamControl[R1, E, A],
    shutdownTimeout: Duration
  )(
    withStream: ZStream[R1, E, A] => ZIO[R2 & Scope, E, Any]
  ): ZIO[R1 & R2, E, Any] =
    ZIO.scoped[R1 & R2] {
      for {
        fib <-
          withStream(control.stream)
            .onInterrupt(
              ZIO.logError("withStream in runWithGracefulShutdown was interrupted, this should not happen")
            )
            .tapErrorCause(cause => ZIO.logErrorCause("Error in withStream fiber in runWithGracefulShutdown", cause))
            .forkScoped
        result <-
          fib.join.onInterrupt(
            control.end *>
              fib.join
                .timeout(shutdownTimeout)
                .someOrElseZIO(
                  ZIO.logError(
                    "Timeout waiting for `withStream` to shut down gracefully. Not all in-flight records may have been processed."
                  )
                )
                .tapErrorCause(cause => ZIO.logErrorCause("Stream failed while awaiting its graceful shutdown", cause))
                .ignore
          )
      } yield result
    }

  private def makeConcurrentDiagnostics(diagnostics: ConsumerDiagnostics): ZIO[Scope, Nothing, ConsumerDiagnostics] =
    if (diagnostics == Diagnostics.NoOp) ZIO.succeed(diagnostics)
    else ConcurrentDiagnostics.make(diagnostics, DiagnosticEvent.ConsumerFinalized)
}

private[consumer] final class ConsumerLive private[consumer] (
  consumer: ConsumerAccess,
  val consumerSettings: ConsumerSettings,
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
        streamControl <- runloopAccess.subscribe(subscription)
        stream = streamControl.stream
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

  override def partitionedAssignmentStreamWithControl[R, K, V](
    subscription: Subscription,
    keyDeserializer: Deserializer[R, K],
    valueDeserializer: Deserializer[R, V]
  ): ZIO[Scope, Throwable, StreamControl[
    R,
    Throwable,
    Chunk[(TopicPartition, ZStream[R, Throwable, CommittableRecord[K, V]])]
  ]] = {
    val onlyByteArraySerdes: Boolean = (keyDeserializer eq Serde.byteArray) && (valueDeserializer eq Serde.byteArray)
    for {
      streamControl <- runloopAccess.subscribe(subscription)
    } yield streamControl.map(
      _.map(_.exit).flattenExitOption.map {
        _.collect {
          case (tp, partitionStream) if Subscription.subscriptionMatches(subscription, tp) =>
            val stream: ZStream[R, Throwable, CommittableRecord[K, V]] =
              if (onlyByteArraySerdes)
                partitionStream.asInstanceOf[ZStream[R, Throwable, CommittableRecord[K, V]]]
              else partitionStream.mapChunksZIO(_.mapZIO(_.deserializeWith(keyDeserializer, valueDeserializer)))

            tp -> stream
        }
      }
    )
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

  override def plainStreamWithControl[R, K, V](
    subscription: Subscription,
    keyDeserializer: Deserializer[R, K],
    valueDeserializer: Deserializer[R, V],
    bufferSize: Int = 4
  ): ZIO[Scope with R, Throwable, StreamControl[R, Throwable, CommittableRecord[K, V]]] =
    partitionedStreamWithControl(subscription, keyDeserializer, valueDeserializer).map(
      _.map(
        _.flatMapPar(
          n = Int.MaxValue,
          bufferSize = bufferSize
        )(_._2)
      )
    )

  override def partitionedStreamWithControl[R, K, V](
    subscription: Subscription,
    keyDeserializer: Deserializer[R, K],
    valueDeserializer: Deserializer[R, V]
  ): ZIO[
    Scope with R,
    Throwable,
    StreamControl[R, Throwable, (TopicPartition, ZStream[R, Throwable, CommittableRecord[K, V]])]
  ] = partitionedAssignmentStreamWithControl(subscription, keyDeserializer, valueDeserializer)
    .map(_.map(_.flattenChunks))

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

  override def registerExternalCommits(externallyCommittedOffsets: OffsetBatch): Task[Unit] =
    runloopAccess.registerExternalCommits(externallyCommittedOffsets)

}
