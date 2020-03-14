package zio.kafka

import org.apache.kafka.clients.consumer.{ OffsetAndMetadata, OffsetAndTimestamp }
import org.apache.kafka.common.{ PartitionInfo, TopicPartition }
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
  type Consumer[R, K, V] = Has[Consumer.Service[R, K, V]]

  object Consumer {
    trait Service[R, K, V] {

      /**
       * Returns the topic-partitions that this consumer is currently assigned.
       *
       * This is subject to consumer rebalancing, unless using a manual subscription.
       */
      def assignment: RIO[Blocking, Set[TopicPartition]]

      def beginningOffsets(
        partitions: Set[TopicPartition],
        timeout: Duration = Duration.Infinity
      ): RIO[Blocking, Map[TopicPartition, Long]]

      /**
       * Retrieve the last committed offset for the given topic-partitions
       */
      def committed(
        partitions: Set[TopicPartition],
        timeout: Duration = Duration.Infinity
      ): RIO[Blocking, Map[TopicPartition, Option[OffsetAndMetadata]]]

      def endOffsets(
        partitions: Set[TopicPartition],
        timeout: Duration = Duration.Infinity
      ): RIO[Blocking, Map[TopicPartition, Long]]

      def listTopics(timeout: Duration = Duration.Infinity): RIO[Blocking, Map[String, List[PartitionInfo]]]

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
      def partitionedStream: ZStream[
        Clock with Blocking,
        Throwable,
        (TopicPartition, ZStreamChunk[R, Throwable, CommittableRecord[K, V]])
      ]

      /**
       * Create a stream with all messages on the subscribed topic-partitions
       *
       * The stream will emit messages from all topic-partitions interleaved. Per-partition
       * record order is guaranteed, but the topic-partition interleaving is non-deterministic.
       *
       * The stream can be completed by calling [[stopConsumption]].
       */
      def plainStream: ZStreamChunk[R with Clock with Blocking, Throwable, CommittableRecord[K, V]]

      /**
       * Stops consumption of data, drains buffered records, and ends the attached
       * streams while still serving commit requests.
       */
      def stopConsumption: UIO[Unit]

      /**
       * See [[Consumer.consumeWith]].
       */
      def consumeWith[RC](
        subscription: Subscription,
        commitRetryPolicy: Schedule[Clock, Any, Any] = Schedule.exponential(1.second) && Schedule.recurs(3)
      )(
        f: (K, V) => URIO[RC, Unit]
      ): ZIO[R with RC with Blocking with Clock, Throwable, Unit]

      def subscribe(subscription: Subscription): RIO[Blocking, Unit]

      def unsubscribe: RIO[Blocking, Unit]

      def offsetsForTimes(
        timestamps: Map[TopicPartition, Long],
        timeout: Duration = Duration.Infinity
      ): RIO[Blocking, Map[TopicPartition, OffsetAndTimestamp]]

      def partitionsFor(topic: String, timeout: Duration = Duration.Infinity): RIO[Blocking, List[PartitionInfo]]

      def position(partition: TopicPartition, timeout: Duration = Duration.Infinity): RIO[Blocking, Long]

      def subscribeAnd(subscription: Subscription): SubscribedConsumer[R, K, V]

      def subscription: RIO[Blocking, Set[String]]
    }

    final case class Live[R, K, V](
      private val consumer: ConsumerAccess,
      private val settings: ConsumerSettings,
      private val runloop: Runloop,
      keyDeserializer: Deserializer[R, K],
      valueDeserializer: Deserializer[R, V]
    ) extends Service[R, K, V] {

      override def assignment: RIO[Blocking, Set[TopicPartition]] =
        consumer.withConsumer(_.assignment().asScala.toSet)

      override def beginningOffsets(
        partitions: Set[TopicPartition],
        timeout: Duration = Duration.Infinity
      ): RIO[Blocking, Map[TopicPartition, Long]] =
        consumer.withConsumer(
          _.beginningOffsets(partitions.asJava, timeout.asJava).asScala.view.mapValues(_.longValue()).toMap
        )

      override def committed(
        partitions: Set[TopicPartition],
        timeout: Duration = Duration.Infinity
      ): RIO[Blocking, Map[TopicPartition, Option[OffsetAndMetadata]]] =
        consumer.withConsumer(
          _.committed(partitions.asJava, timeout.asJava).asScala.toMap.view.mapValues(Option.apply).toMap
        )

      override def endOffsets(
        partitions: Set[TopicPartition],
        timeout: Duration = Duration.Infinity
      ): RIO[Blocking, Map[TopicPartition, Long]] =
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

      override def listTopics(timeout: Duration = Duration.Infinity): RIO[Blocking, Map[String, List[PartitionInfo]]] =
        consumer.withConsumer(_.listTopics(timeout.asJava).asScala.view.mapValues(_.asScala.toList).toMap)

      override def offsetsForTimes(
        timestamps: Map[TopicPartition, Long],
        timeout: Duration = Duration.Infinity
      ): RIO[Blocking, Map[TopicPartition, OffsetAndTimestamp]] =
        consumer.withConsumer(
          _.offsetsForTimes(timestamps.view.mapValues(Long.box).toMap.asJava, timeout.asJava).asScala.toMap
        )

      override def partitionedStream: ZStream[
        Clock with Blocking,
        Throwable,
        (TopicPartition, ZStreamChunk[R, Throwable, CommittableRecord[K, V]])
      ] =
        ZStream
          .fromQueue(runloop.partitions)
          .unTake
          .map {
            case (tp, partition) =>
              val partitionStream =
                if (settings.perPartitionChunkPrefetch <= 0) partition
                else ZStreamChunk(partition.chunks.buffer(settings.perPartitionChunkPrefetch))

              tp -> partitionStream.mapM(_.deserializeWith(keyDeserializer, valueDeserializer))
          }

      override def partitionsFor(
        topic: String,
        timeout: Duration = Duration.Infinity
      ): RIO[Blocking, List[PartitionInfo]] =
        consumer.withConsumer(_.partitionsFor(topic, timeout.asJava).asScala.toList)

      override def position(partition: TopicPartition, timeout: Duration = Duration.Infinity): RIO[Blocking, Long] =
        consumer.withConsumer(_.position(partition, timeout.asJava))

      override def plainStream: ZStreamChunk[R with Clock with Blocking, Throwable, CommittableRecord[K, V]] =
        ZStreamChunk(partitionedStream.flatMapPar(n = Int.MaxValue)(_._2.chunks))

      override def subscribeAnd(subscription: Subscription): SubscribedConsumer[R, K, V] =
        new SubscribedConsumer(subscribe(subscription).as(this))

      override def subscription: RIO[Blocking, Set[String]] =
        consumer.withConsumer(_.subscription().asScala.toSet)

      override def consumeWith[RC](
        subscription: Subscription,
        commitRetryPolicy: Schedule[Clock, Any, Any] = Schedule.exponential(1.second) && Schedule.recurs(3)
      )(
        f: (K, V) => URIO[RC, Unit]
      ): ZIO[R with RC with Blocking with Clock, Throwable, Unit] =
        ZStream
          .fromEffect(subscribe(subscription))
          .flatMap { _ =>
            partitionedStream
              .flatMapPar(Int.MaxValue, outputBuffer = settings.perPartitionChunkPrefetch) {
                case (_, partitionStream) =>
                  partitionStream.mapM {
                    case CommittableRecord(record, offset) =>
                      f(record.key(), record.value()).as(offset)
                  }.flattenChunks
              }
          }
          .aggregateAsync(offsetBatches)
          .mapM(_.commitOrRetry(commitRetryPolicy))
          .runDrain

      override def subscribe(subscription: Subscription): RIO[Blocking, Unit] =
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

      override def unsubscribe: RIO[Blocking, Unit] =
        consumer.withConsumer(_.unsubscribe())
    }

    val offsetBatches: ZSink[Any, Nothing, Nothing, Offset, OffsetBatch] =
      ZSink.foldLeft[Offset, OffsetBatch](OffsetBatch.empty)(_ merge _)

    def live[R: Tagged, K: Tagged, V: Tagged]: ZLayer[Clock with Blocking with Has[Deserializer[R, K]] with Has[
      Deserializer[R, V]
    ] with Has[ConsumerSettings] with Has[
      Diagnostics
    ], Throwable, Consumer[R, K, V]] =
      ZLayer.fromManaged {
        ZManaged
          .accessManaged[Clock with Blocking with Has[Deserializer[R, K]] with Has[Deserializer[R, V]] with Has[
            ConsumerSettings
          ] with Has[
            Diagnostics
          ]] { env =>
            val settings = env.get[ConsumerSettings]
            for {
              wrapper <- ConsumerAccess.make(settings)
              runloop <- Runloop(
                          wrapper,
                          settings.pollInterval,
                          settings.pollTimeout,
                          env.get[Diagnostics],
                          settings.offsetRetrieval
                        )
            } yield Live(wrapper, settings, runloop, env.get[Deserializer[R, K]], env.get[Deserializer[R, V]])
          }
      }

    def make[R: Tagged, K: Tagged, V: Tagged](
      settings: ConsumerSettings,
      keyDeserializer: Deserializer[R, K],
      valueDeserializer: Deserializer[R, V],
      diagnostics: Diagnostics = Diagnostics.NoOp
    ): ZLayer[Clock with Blocking, Throwable, Consumer[R, K, V]] =
      ((ZLayer.requires[Clock] ++ ZLayer
        .requires[Blocking] ++ ZLayer.succeed(settings) ++ ZLayer.succeed(diagnostics) ++ ZLayer.succeed(
        keyDeserializer
      ) ++ ZLayer
        .succeed(
          valueDeserializer
        )) >>> live[R, K, V])

    def withConsumerService[R: Tagged, RC, K: Tagged, V: Tagged, A](
      r: Service[R, K, V] => RIO[R with RC with Blocking, A]
    ): RIO[R with RC with Blocking with Consumer[R, K, V], A] =
      ZIO.accessM(env => r(env.get[Service[R, K, V]]))

    /**
     * Accessor method for [[Service.assignment]]
     */
    def assignment[R: Tagged, K: Tagged, V: Tagged]: RIO[R with Blocking with Consumer[R, K, V], Set[TopicPartition]] =
      withConsumerService[R, Any, K, V, Set[TopicPartition]](_.assignment)

    /**
     * Accessor method for [[Service.beginningOffsets]]
     */
    def beginningOffsets[R: Tagged, K: Tagged, V: Tagged](
      partitions: Set[TopicPartition],
      timeout: Duration = Duration.Infinity
    ): RIO[R with Blocking with Consumer[R, K, V], Map[TopicPartition, Long]] =
      withConsumerService[R, Any, K, V, Map[TopicPartition, Long]](_.beginningOffsets(partitions, timeout))

    /**
     * Accessor method for [[Service.committed]]
     */
    def committed[R: Tagged, K: Tagged, V: Tagged](
      partitions: Set[TopicPartition],
      timeout: Duration = Duration.Infinity
    ): RIO[R with Blocking with Consumer[R, K, V], Map[TopicPartition, Option[OffsetAndMetadata]]] =
      withConsumerService[R, Any, K, V, Map[TopicPartition, Option[OffsetAndMetadata]]](
        _.committed(partitions, timeout)
      )

    /**
     * Accessor method for [[Service.endOffsets]]
     */
    def endOffsets[R: Tagged, K: Tagged, V: Tagged](
      partitions: Set[TopicPartition],
      timeout: Duration = Duration.Infinity
    ): RIO[R with Blocking with Consumer[R, K, V], Map[TopicPartition, Long]] =
      withConsumerService[R, Any, K, V, Map[TopicPartition, Long]](_.endOffsets(partitions, timeout))

    /**
     * Accessor method for [[Service.listTopics]]
     */
    def listTopics[R: Tagged, K: Tagged, V: Tagged](
      timeout: Duration = Duration.Infinity
    ): RIO[R with Blocking with Consumer[R, K, V], Map[String, List[PartitionInfo]]] =
      withConsumerService[R, Any, K, V, Map[String, List[PartitionInfo]]](_.listTopics(timeout))

    /**
     * Accessor method for [[Service.partitionedStream]]
     */
    def partitionedStream[R: Tagged, K: Tagged, V: Tagged]: ZStream[
      Consumer[R, K, V] with Clock with Blocking,
      Throwable,
      (TopicPartition, ZStreamChunk[R, Throwable, CommittableRecord[K, V]])
    ] =
      ZStream.accessStream(_.get[Service[R, K, V]].partitionedStream)

    /**
     * Accessor method for [[Service.plainStream]]
     */
    def plainStream[R: Tagged, K: Tagged, V: Tagged]
      : ZStreamChunk[R with Consumer[R, K, V] with Clock with Blocking, Throwable, CommittableRecord[K, V]] =
      ZStreamChunk(ZStream.accessStream(_.get[Service[R, K, V]].plainStream.chunks))

    /**
     * Accessor method for [[Service.stopConsumption]]
     */
    def stopConsumption[R: Tagged, K: Tagged, V: Tagged]: RIO[Consumer[R, K, V], Unit] =
      ZIO.accessM(_.get[Service[R, K, V]].stopConsumption)

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
    def consumeWith[R, R1: Tagged, K: Tagged, V: Tagged](
      settings: ConsumerSettings,
      subscription: Subscription,
      keyDeserializer: Deserializer[R1, K],
      valueDeserializer: Deserializer[R1, V],
      commitRetryPolicy: Schedule[Clock, Any, Any] = Schedule.exponential(1.second) && Schedule.recurs(3)
    )(
      f: (K, V) => ZIO[R, Nothing, Unit]
    ): ZIO[R with R1 with Blocking with Clock, Throwable, Unit] =
      Consumer
        .make(settings, keyDeserializer, valueDeserializer)
        .build
        .map(_.get[Service[R1, K, V]])
        .use(_.consumeWith(subscription, commitRetryPolicy)(f))

    /**
     * Accessor method for [[Service.subscribe]]
     */
    def subscribe[R: Tagged, K: Tagged, V: Tagged](
      subscription: Subscription
    ): RIO[R with Blocking with Consumer[R, K, V], Unit] =
      withConsumerService[R, Any, K, V, Unit](_.subscribe(subscription))

    /**
     * Accessor method for [[Service.unsubscribe]]
     */
    def unsubscribe[R: Tagged, K: Tagged, V: Tagged]: RIO[R with Blocking with Consumer[R, K, V], Unit] =
      withConsumerService[R, Any, K, V, Unit](_.unsubscribe)

    /**
     * Accessor method for [[Service.offsetsForTimes]]
     */
    def offsetsForTimes[R: Tagged, K: Tagged, V: Tagged](
      timestamps: Map[TopicPartition, Long],
      timeout: Duration = Duration.Infinity
    ): RIO[R with Blocking with Consumer[R, K, V], Map[TopicPartition, OffsetAndTimestamp]] =
      withConsumerService[R, Any, K, V, Map[TopicPartition, OffsetAndTimestamp]](_.offsetsForTimes(timestamps, timeout))

    /**
     * Accessor method for [[Service.partitionsFor]]
     */
    def partitionsFor[R: Tagged, K: Tagged, V: Tagged](
      topic: String,
      timeout: Duration = Duration.Infinity
    ): RIO[R with Blocking with Consumer[R, K, V], List[PartitionInfo]] =
      withConsumerService[R, Any, K, V, List[PartitionInfo]](_.partitionsFor(topic, timeout))

    /**
     * Accessor method for [[Service.position]]
     */
    def position[R: Tagged, K: Tagged, V: Tagged](
      partition: TopicPartition,
      timeout: Duration = Duration.Infinity
    ): RIO[R with Blocking with Consumer[R, K, V], Long] =
      withConsumerService[R, Any, K, V, Long](_.position(partition, timeout))

    /**
     * Accessor method for [[Service.subscribeAnd]]
     */
    def subscribeAnd[R: Tagged, K: Tagged, V: Tagged](
      subscription: Subscription
    ): SubscribedConsumerFromEnvironment[R, K, V] =
      new SubscribedConsumerFromEnvironment(
        ZIO.accessM { env =>
          val consumer = env.get[Service[R, K, V]]
          consumer.subscribe(subscription).as(consumer)
        }
      )

    /**
     * Accessor method for [[Service.subscription]]
     */
    def subscription[R: Tagged, K: Tagged, V: Tagged]: RIO[R with Blocking with Consumer[R, K, V], Set[String]] =
      withConsumerService[R, Any, K, V, Set[String]](_.subscription)

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
