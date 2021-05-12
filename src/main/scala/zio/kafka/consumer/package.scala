package zio.kafka

import org.apache.kafka.clients.consumer.{ OffsetAndMetadata, OffsetAndTimestamp }
import org.apache.kafka.common.{ Metric, MetricName, PartitionInfo, TopicPartition }
import zio._
import zio.blocking.Blocking
import zio.clock.Clock
import zio.duration._
import zio.kafka.serde.Deserializer
import zio.kafka.consumer.diagnostics.Diagnostics
import zio.kafka.consumer.internal.{ ConsumerAccess, Runloop }
import zio.stream._

import scala.collection.compat._
import scala.jdk.CollectionConverters._

package object consumer {
  type Consumer = Has[Consumer.Service]

  object Consumer {
    trait Service {

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
       * Retrieve the last committed offset for the given topic-partitions
       */
      def committed(
        partitions: Set[TopicPartition],
        timeout: Duration = Duration.Infinity
      ): Task[Map[TopicPartition, Option[OffsetAndMetadata]]]

      def endOffsets(
        partitions: Set[TopicPartition],
        timeout: Duration = Duration.Infinity
      ): Task[Map[TopicPartition, Long]]

      def listTopics(timeout: Duration = Duration.Infinity): Task[Map[String, List[PartitionInfo]]]

      /**
       * Create a stream with messages on the subscribed topic-partitions by topic-partition
       *
       * The top-level stream will emit new topic-partition streams for each topic-partition that is assigned
       * to this consumer. This is subject to consumer rebalancing, unless a manual subscription
       * was made. When rebalancing occurs, new topic-partition streams may be emitted and existing
       * streams may be completed.
       *
       * All streams can be completed by calling [[stopConsumption]].
     **/
      def partitionedStream[R, K, V](
        keyDeserializer: Deserializer[R, K],
        valueDeserializer: Deserializer[R, V]
      ): ZStream[
        Clock,
        Throwable,
        (TopicPartition, ZStream[R, Throwable, CommittableRecord[K, V]])
      ]

      /**
       * Create a stream with all messages on the subscribed topic-partitions
       *
       * The stream will emit messages from all topic-partitions interleaved. Per-partition
       * record order is guaranteed, but the topic-partition interleaving is non-deterministic.
       *
       * Up to `outputBuffer` chunks may be buffered in memory by this operator.
       *
       * The stream can be completed by calling [[stopConsumption]].
       */
      def plainStream[R, K, V](
        keyDeserializer: Deserializer[R, K],
        valueDeserializer: Deserializer[R, V],
        outputBuffer: Int = 4
      ): ZStream[R with Clock, Throwable, CommittableRecord[K, V]]

      /**
       * Stops consumption of data, drains buffered records, and ends the attached
       * streams while still serving commit requests.
       */
      def stopConsumption: UIO[Unit]

      /**
       * See [[Consumer.consumeWith]].
       */
      def consumeWith[R, RC, K, V](
        subscription: Subscription,
        keyDeserializer: Deserializer[R, K],
        valueDeserializer: Deserializer[R, V],
        commitRetryPolicy: Schedule[Clock, Any, Any] = Schedule.exponential(1.second) && Schedule.recurs(3)
      )(
        f: (K, V) => URIO[RC, Unit]
      ): ZIO[R with RC with Clock, Throwable, Unit]

      def subscribe(subscription: Subscription): Task[Unit]

      def unsubscribe: Task[Unit]

      /**
       * Look up the offsets for the given partitions by timestamp. The returned offset for each partition is the
       * earliest offset whose timestamp is greater than or equal to the given timestamp in the corresponding partition.
       *
       * The consumer does not have to be assigned the partitions.
       * If no messages exist yet for a partition, it will not exist in the returned map.
       */
      def offsetsForTimes(
        timestamps: Map[TopicPartition, Long],
        timeout: Duration = Duration.Infinity
      ): Task[Map[TopicPartition, OffsetAndTimestamp]]

      def partitionsFor(topic: String, timeout: Duration = Duration.Infinity): Task[List[PartitionInfo]]

      def position(partition: TopicPartition, timeout: Duration = Duration.Infinity): Task[Long]

      def subscribeAnd(subscription: Subscription): SubscribedConsumer

      def subscription: Task[Set[String]]

      /**
       * Expose internal consumer metrics
       */
      def metrics: Task[Map[MetricName, Metric]]
    }

    final case class Live(
      private val consumer: ConsumerAccess,
      private val settings: ConsumerSettings,
      private val runloop: Runloop
    ) extends Service {

      override def assignment: Task[Set[TopicPartition]] =
        consumer.withConsumer(_.assignment().asScala.toSet)

      override def beginningOffsets(
        partitions: Set[TopicPartition],
        timeout: Duration = Duration.Infinity
      ): Task[Map[TopicPartition, Long]] =
        consumer.withConsumer(
          _.beginningOffsets(partitions.asJava, timeout.asJava).asScala.view.mapValues(_.longValue()).toMap
        )

      override def committed(
        partitions: Set[TopicPartition],
        timeout: Duration = Duration.Infinity
      ): Task[Map[TopicPartition, Option[OffsetAndMetadata]]] =
        consumer.withConsumer(
          _.committed(partitions.asJava, timeout.asJava).asScala.toMap.view.mapValues(Option.apply).toMap
        )

      override def endOffsets(
        partitions: Set[TopicPartition],
        timeout: Duration = Duration.Infinity
      ): Task[Map[TopicPartition, Long]] =
        consumer.withConsumer { eo =>
          val offs = eo.endOffsets(partitions.asJava, timeout.asJava)
          offs.asScala.view.mapValues(_.longValue()).toMap
        }

      /**
       * Stops consumption of data, drains buffered records, and ends the attached
       * streams while still serving commit requests.
       */
      override def stopConsumption: UIO[Unit] =
        runloop.gracefulShutdown

      override def listTopics(timeout: Duration = Duration.Infinity): Task[Map[String, List[PartitionInfo]]] =
        consumer.withConsumer(_.listTopics(timeout.asJava).asScala.view.mapValues(_.asScala.toList).toMap)

      override def offsetsForTimes(
        timestamps: Map[TopicPartition, Long],
        timeout: Duration = Duration.Infinity
      ): Task[Map[TopicPartition, OffsetAndTimestamp]] =
        consumer.withConsumer(
          _.offsetsForTimes(timestamps.view.mapValues(Long.box).toMap.asJava, timeout.asJava).asScala.toMap
          // If a partition doesn't exist yet, the map will have 'null' as entry.
          // It's more idiomatic scala to then simply not have that map entry.
            .filter(_._2 != null)
        )

      override def partitionedStream[R, K, V](
        keyDeserializer: Deserializer[R, K],
        valueDeserializer: Deserializer[R, V]
      ): ZStream[
        Clock,
        Throwable,
        (TopicPartition, ZStream[R, Throwable, CommittableRecord[K, V]])
      ] = {
        val configureDeserializers =
          ZStream.fromEffect {
            keyDeserializer.configure(settings.driverSettings, isKey = true) *>
              valueDeserializer.configure(settings.driverSettings, isKey = false)
          }

        configureDeserializers *>
          ZStream
            .fromQueue(runloop.partitions)
            .flattenExitOption
            .map {
              case (tp, partition) =>
                val partitionStream =
                  if (settings.perPartitionChunkPrefetch <= 0) partition
                  else partition.buffer(settings.perPartitionChunkPrefetch)

                tp -> partitionStream.mapChunksM(_.mapM(_.deserializeWith(keyDeserializer, valueDeserializer)))
            }
      }

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

      override def plainStream[R, K, V](
        keyDeserializer: Deserializer[R, K],
        valueDeserializer: Deserializer[R, V],
        outputBuffer: Int
      ): ZStream[R with Clock, Throwable, CommittableRecord[K, V]] =
        partitionedStream(keyDeserializer, valueDeserializer).flatMapPar(n = Int.MaxValue, outputBuffer = outputBuffer)(
          _._2
        )

      override def subscribeAnd(subscription: Subscription): SubscribedConsumer =
        new SubscribedConsumer(subscribe(subscription).as(this))

      override def subscription: Task[Set[String]] =
        consumer.withConsumer(_.subscription().asScala.toSet)

      override def consumeWith[R, RC, K, V](
        subscription: Subscription,
        keyDeserializer: Deserializer[R, K],
        valueDeserializer: Deserializer[R, V],
        commitRetryPolicy: Schedule[Clock, Any, Any] = Schedule.exponential(1.second) && Schedule.recurs(3)
      )(
        f: (K, V) => URIO[RC, Unit]
      ): ZIO[R with RC with Clock, Throwable, Unit] =
        ZStream
          .fromEffect(subscribe(subscription))
          .flatMap { _ =>
            partitionedStream(keyDeserializer, valueDeserializer)
              .flatMapPar(Int.MaxValue, outputBuffer = settings.perPartitionChunkPrefetch) {
                case (_, partitionStream) =>
                  partitionStream.mapChunksM(_.mapM {
                    case CommittableRecord(record, offset) =>
                      f(record.key(), record.value()).as(offset)
                  })
              }
          }
          .aggregateAsync(offsetBatches)
          .mapM(_.commitOrRetry(commitRetryPolicy))
          .runDrain

      override def subscribe(subscription: Subscription): Task[Unit] =
        ZIO.runtime[Any].flatMap { runtime =>
          consumer.withConsumerM { c =>
            subscription match {
              case Subscription.Pattern(pattern) =>
                ZIO(c.subscribe(pattern.pattern, runloop.rebalanceListener.toKafka(runtime)))
              case Subscription.Topics(topics) =>
                ZIO(c.subscribe(topics.asJava, runloop.rebalanceListener.toKafka(runtime)))

              // For manual subscriptions we have to do some manual work before starting the run loop
              case Subscription.Manual(topicPartitions) =>
                ZIO(c.assign(topicPartitions.asJava)) *>
                  ZIO.foreach_(topicPartitions)(runloop.newPartitionStream) *> {
                  settings.offsetRetrieval match {
                    case OffsetRetrieval.Manual(getOffsets) =>
                      getOffsets(topicPartitions).flatMap { offsets =>
                        ZIO.foreach_(offsets) { case (tp, offset) => ZIO(c.seek(tp, offset)) }
                      }
                    case OffsetRetrieval.Auto(_) => ZIO.unit
                  }
                }
            }
          }
        }

      override def unsubscribe: Task[Unit] =
        consumer.withConsumer(_.unsubscribe())

      override def metrics: Task[Map[MetricName, Metric]] =
        consumer.withConsumer(_.metrics().asScala.toMap)
    }

    val offsetBatches: ZTransducer[Any, Nothing, Offset, OffsetBatch] =
      ZTransducer.foldLeft[Offset, OffsetBatch](OffsetBatch.empty)(_ merge _)

    def live: ZLayer[Clock with Blocking with Has[ConsumerSettings] with Has[Diagnostics], Throwable, Consumer] =
      ZLayer.fromServicesManaged[ConsumerSettings, Diagnostics, Clock with Blocking, Throwable, Service] {
        (settings, diagnostics) => make(settings, diagnostics)
      }

    def make(
      settings: ConsumerSettings,
      diagnostics: Diagnostics = Diagnostics.NoOp
    ): ZManaged[Clock with Blocking, Throwable, Service] =
      for {
        wrapper <- ConsumerAccess.make(settings)
        runloop <- Runloop(
                    wrapper,
                    settings.pollInterval,
                    settings.pollTimeout,
                    diagnostics,
                    settings.offsetRetrieval
                  )
      } yield Live(wrapper, settings, runloop)

    def withConsumerService[R, A](
      r: Service => RIO[R, A]
    ): RIO[R with Consumer, A] =
      ZIO.accessM(env => r(env.get[Service]))

    /**
     * Accessor method for [[Service.assignment]]
     */
    def assignment: RIO[Consumer, Set[TopicPartition]] =
      withConsumerService(_.assignment)

    /**
     * Accessor method for [[Service.beginningOffsets]]
     */
    def beginningOffsets(
      partitions: Set[TopicPartition],
      timeout: Duration = Duration.Infinity
    ): RIO[Consumer, Map[TopicPartition, Long]] =
      withConsumerService(_.beginningOffsets(partitions, timeout))

    /**
     * Accessor method for [[Service.committed]]
     */
    def committed(
      partitions: Set[TopicPartition],
      timeout: Duration = Duration.Infinity
    ): RIO[Blocking with Consumer, Map[TopicPartition, Option[OffsetAndMetadata]]] =
      withConsumerService(_.committed(partitions, timeout))

    /**
     * Accessor method for [[Service.endOffsets]]
     */
    def endOffsets(
      partitions: Set[TopicPartition],
      timeout: Duration = Duration.Infinity
    ): RIO[Blocking with Consumer, Map[TopicPartition, Long]] =
      withConsumerService(_.endOffsets(partitions, timeout))

    /**
     * Accessor method for [[Service.listTopics]]
     */
    def listTopics(
      timeout: Duration = Duration.Infinity
    ): RIO[Blocking with Consumer, Map[String, List[PartitionInfo]]] =
      withConsumerService(_.listTopics(timeout))

    /**
     * Accessor method for [[Service.partitionedStream]]
     */
    def partitionedStream[R: Tag, K: Tag, V: Tag](
      keyDeserializer: Deserializer[R, K],
      valueDeserializer: Deserializer[R, V]
    ): ZStream[
      Consumer with Clock with Blocking,
      Throwable,
      (TopicPartition, ZStream[R, Throwable, CommittableRecord[K, V]])
    ] =
      ZStream.accessStream(_.get[Service].partitionedStream(keyDeserializer, valueDeserializer))

    /**
     * Accessor method for [[Service.plainStream]]
     */
    def plainStream[R: Tag, K: Tag, V: Tag](
      keyDeserializer: Deserializer[R, K],
      valueDeserializer: Deserializer[R, V],
      outputBuffer: Int = 4
    ): ZStream[R with Consumer with Clock with Blocking, Throwable, CommittableRecord[K, V]] =
      ZStream.accessStream(_.get[Service].plainStream(keyDeserializer, valueDeserializer, outputBuffer))

    /**
     * Accessor method for [[Service.stopConsumption]]
     */
    def stopConsumption: RIO[Consumer, Unit] =
      ZIO.accessM(_.get[Service].stopConsumption)

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
     * @param commitRetryPolicy Retry commits that failed due to a RetriableCommitFailedException according to this schedule
     * @param f Function that returns the effect to execute for each message. It is passed the key and value
     * @tparam R Environment for the consuming effect
     * @tparam R1 Environment for the deserializers
     * @tparam K Type of keys (an implicit `Deserializer` should be in scope)
     * @tparam V Type of values (an implicit `Deserializer` should be in scope)
     * @return Effect that completes with a unit value only when interrupted. May fail when the [[Consumer]] fails.
     */
    def consumeWith[R, R1: Tag, K: Tag, V: Tag](
      settings: ConsumerSettings,
      subscription: Subscription,
      keyDeserializer: Deserializer[R1, K],
      valueDeserializer: Deserializer[R1, V],
      commitRetryPolicy: Schedule[Clock, Any, Any] = Schedule.exponential(1.second) && Schedule.recurs(3)
    )(
      f: (K, V) => ZIO[R, Nothing, Unit]
    ): ZIO[R with R1 with Blocking with Clock, Throwable, Unit] =
      Consumer
        .make(settings)
        .use(_.consumeWith(subscription, keyDeserializer, valueDeserializer, commitRetryPolicy)(f))

    /**
     * Accessor method for [[Service.subscribe]]
     */
    def subscribe(subscription: Subscription): RIO[Consumer, Unit] =
      withConsumerService(_.subscribe(subscription))

    /**
     * Accessor method for [[Service.unsubscribe]]
     */
    def unsubscribe: RIO[Consumer, Unit] =
      withConsumerService(_.unsubscribe)

    /**
     * Accessor method for [[Service.offsetsForTimes]]
     */
    def offsetsForTimes(
      timestamps: Map[TopicPartition, Long],
      timeout: Duration = Duration.Infinity
    ): RIO[Consumer, Map[TopicPartition, OffsetAndTimestamp]] =
      withConsumerService(_.offsetsForTimes(timestamps, timeout))

    /**
     * Accessor method for [[Service.partitionsFor]]
     */
    def partitionsFor(
      topic: String,
      timeout: Duration = Duration.Infinity
    ): RIO[Consumer, List[PartitionInfo]] =
      withConsumerService(_.partitionsFor(topic, timeout))

    /**
     * Accessor method for [[Service.position]]
     */
    def position(
      partition: TopicPartition,
      timeout: Duration = Duration.Infinity
    ): RIO[Consumer, Long] =
      withConsumerService(_.position(partition, timeout))

    /**
     * Accessor method for [[Service.subscribeAnd]]
     */
    def subscribeAnd(
      subscription: Subscription
    ): SubscribedConsumerFromEnvironment =
      new SubscribedConsumerFromEnvironment(
        ZIO.accessM { env =>
          val consumer = env.get[Service]
          consumer.subscribe(subscription).as(consumer)
        }
      )

    /**
     * Accessor method for [[Service.subscription]]
     */
    def subscription: RIO[Consumer, Set[String]] =
      withConsumerService(_.subscription)

    /**
     * Accessor method for [[Service.metrics]]
     */
    def metrics: RIO[Consumer, Map[MetricName, Metric]] =
      withConsumerService(_.metrics)

    sealed trait OffsetRetrieval

    object OffsetRetrieval {
      final case class Auto(reset: AutoOffsetStrategy = AutoOffsetStrategy.Latest) extends OffsetRetrieval
      final case class Manual(getOffsets: Set[TopicPartition] => Task[Map[TopicPartition, Long]])
          extends OffsetRetrieval
    }

    sealed trait AutoOffsetStrategy { self =>
      def toConfig = self match {
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

}
