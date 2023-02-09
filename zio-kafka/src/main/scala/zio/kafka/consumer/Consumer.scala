package zio.kafka.consumer

import org.apache.kafka.clients.consumer.{ OffsetAndMetadata, OffsetAndTimestamp }
import org.apache.kafka.common.{ Metric, MetricName, PartitionInfo, TopicPartition }
import zio._
import zio.kafka.serde.Deserializer
import zio.kafka.consumer.diagnostics.Diagnostics
import zio.kafka.consumer.internal.{ ConsumerAccess, Runloop }
import zio.stream.ZStream.Pull
import zio.stream._

import scala.jdk.CollectionConverters._

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
   * Create a stream that emits chunks whenever new partitions are assigned to this consumer.
   *
   * The top-level stream will emit chunks whenever the consumer rebalances, unless a manual subscription was made. When
   * rebalancing occurs, new topic-partition streams may be emitted and existing streams may be completed.
   *
   * All streams can be completed by calling [[stopConsumption]].
   */
  def partitionedAssignmentStream[R, K, V](
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
   */
  def partitionedStream[R, K, V](
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
   */
  def plainStream[R, K, V](
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
  def consumeWith[R: Tag, R1: Tag, K, V](
    subscription: Subscription,
    keyDeserializer: Deserializer[R, K],
    valueDeserializer: Deserializer[R, V],
    commitRetryPolicy: Schedule[Any, Any, Any] = Schedule.exponential(1.second) && Schedule.recurs(3)
  )(
    f: (K, V) => URIO[R1, Unit]
  ): ZIO[R & R1, Throwable, Unit]

  def subscribe(subscription: Subscription): Task[Unit]

  def unsubscribe: Task[Unit]

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

  def subscribeAnd(subscription: Subscription): SubscribedConsumer

  def subscription: Task[Set[String]]

  /**
   * Expose internal consumer metrics
   */
  def metrics: Task[Map[MetricName, Metric]]
}

object Consumer {

  private final case class Live(
    private val consumer: ConsumerAccess,
    private val settings: ConsumerSettings,
    private val runloop: Runloop
  ) extends Consumer {

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

    override def committed(
      partitions: Set[TopicPartition],
      timeout: Duration = Duration.Infinity
    ): Task[Map[TopicPartition, Option[OffsetAndMetadata]]] =
      consumer.withConsumer(
        _.committed(partitions.asJava, timeout.asJava).asScala.map { case (k, v) => k -> Option(v) }.toMap
      )

    override def endOffsets(
      partitions: Set[TopicPartition],
      timeout: Duration = Duration.Infinity
    ): Task[Map[TopicPartition, Long]] =
      consumer.withConsumer { eo =>
        val offs = eo.endOffsets(partitions.asJava, timeout.asJava)
        offs.asScala.map { case (k, v) => k -> v.longValue() }.toMap
      }

    /**
     * Stops consumption of data, drains buffered records, and ends the attached streams while still serving commit
     * requests.
     */
    override def stopConsumption: UIO[Unit] =
      runloop.gracefulShutdown

    override def listTopics(timeout: Duration = Duration.Infinity): Task[Map[String, List[PartitionInfo]]] =
      consumer.withConsumer(_.listTopics(timeout.asJava).asScala.map { case (k, v) => k -> v.asScala.toList }.toMap)

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

    override def partitionedAssignmentStream[R, K, V](
      keyDeserializer: Deserializer[R, K],
      valueDeserializer: Deserializer[R, V]
    ): Stream[Throwable, Chunk[(TopicPartition, ZStream[R, Throwable, CommittableRecord[K, V]])]] = {
      val partitions = runloop.partitions
      val stream =
        ZStream.repeatZIOChunkOption {
          partitions
            .takeBetween(1, ZStream.DefaultChunkSize)
            .flatMap { chunk =>
              ZIO.foreach(chunk) { take =>
                take.fold(
                  partitions.shutdown.as(Take.end),
                  cause => ZIO.succeed(Take.failCause(cause)),
                  chunk => ZIO.succeed(Take.chunk(chunk))
                )
              }
            }
            .catchAllCause((c: Cause[Nothing]) =>
              partitions.isShutdown.flatMap { down =>
                if (down && c.isInterrupted) Pull.end
                else Pull.failCause(c)
              }
            )
        }

      stream.map(_.exit).flattenExitOption.map {
        _.map { case (tp, partition) =>
          val partitionStream =
            if (settings.perPartitionChunkPrefetch <= 0) partition
            else partition.bufferChunks(settings.perPartitionChunkPrefetch)

          tp -> partitionStream.mapChunksZIO(_.mapZIO(_.deserializeWith(keyDeserializer, valueDeserializer)))
        }
      }
    }

    override def partitionedStream[R, K, V](
      keyDeserializer: Deserializer[R, K],
      valueDeserializer: Deserializer[R, V]
    ): ZStream[
      Any,
      Throwable,
      (TopicPartition, ZStream[R, Throwable, CommittableRecord[K, V]])
    ] = partitionedAssignmentStream(keyDeserializer, valueDeserializer).flattenChunks

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
      bufferSize: Int
    ): ZStream[R, Throwable, CommittableRecord[K, V]] =
      partitionedStream(keyDeserializer, valueDeserializer).flatMapPar(n = Int.MaxValue, bufferSize = bufferSize)(
        _._2
      )

    override def subscribeAnd(subscription: Subscription): SubscribedConsumer =
      new SubscribedConsumer(subscribe(subscription).as(this))

    override def subscription: Task[Set[String]] =
      consumer.withConsumer(_.subscription().asScala.toSet)

    override def consumeWith[R: Tag, R1: Tag, K, V](
      subscription: Subscription,
      keyDeserializer: Deserializer[R, K],
      valueDeserializer: Deserializer[R, V],
      commitRetryPolicy: Schedule[Any, Any, Any] = Schedule.exponential(1.second) && Schedule.recurs(3)
    )(
      f: (K, V) => URIO[R1, Unit]
    ): ZIO[R & R1, Throwable, Unit] =
      for {
        r <- ZIO.environment[R & R1]
        _ <- ZStream
               .fromZIO(subscribe(subscription))
               .flatMap { _ =>
                 partitionedStream(keyDeserializer, valueDeserializer)
                   .flatMapPar(Int.MaxValue, bufferSize = settings.perPartitionChunkPrefetch) {
                     case (_, partitionStream) =>
                       partitionStream.mapChunksZIO(_.mapZIO { case CommittableRecord(record, offset) =>
                         f(record.key(), record.value()).as(offset)
                       })
                   }
               }
               .provideEnvironment(r)
               .aggregateAsync(offsetBatches)
               .mapZIO(_.commitOrRetry(commitRetryPolicy))
               .runDrain
      } yield ()

    override def subscribe(subscription: Subscription): Task[Unit] =
      ZIO.runtime[Any].flatMap { runtime =>
        consumer.withConsumerM { c =>
          val rc = RebalanceConsumer.Live(c)

          subscription match {
            case Subscription.Pattern(pattern) =>
              ZIO.attempt(c.subscribe(pattern.pattern, runloop.rebalanceListener.toKafka(runtime, rc)))
            case Subscription.Topics(topics) =>
              ZIO.attempt(c.subscribe(topics.asJava, runloop.rebalanceListener.toKafka(runtime, rc)))

            // For manual subscriptions we have to do some manual work before starting the run loop
            case Subscription.Manual(topicPartitions) =>
              ZIO.attempt(c.assign(topicPartitions.asJava)) *>
                ZIO.foreach(topicPartitions)(runloop.newPartitionStream).flatMap { partitionStreams =>
                  runloop.partitions.offer(
                    Take.chunk(
                      Chunk.fromIterable(partitionStreams.map { case (tp, _, stream) =>
                        tp -> stream
                      })
                    )
                  )
                } *> {
                  settings.offsetRetrieval match {
                    case OffsetRetrieval.Manual(getOffsets) =>
                      getOffsets(topicPartitions).flatMap { offsets =>
                        ZIO.foreachDiscard(offsets) { case (tp, offset) => ZIO.attempt(c.seek(tp, offset)) }
                      }
                    case OffsetRetrieval.Auto(_) => ZIO.unit
                  }
                }
          }
        }
      } *> runloop.markSubscribed

    override def unsubscribe: Task[Unit] =
      runloop.markUnsubscribed *> consumer.withConsumer(_.unsubscribe())

    override def metrics: Task[Map[MetricName, Metric]] =
      consumer.withConsumer(_.metrics().asScala.toMap)
  }

  val offsetBatches: ZSink[Any, Nothing, Offset, Nothing, OffsetBatch] =
    ZSink.foldLeft[Offset, OffsetBatch](OffsetBatch.empty)(_ merge _)

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
      wrapper <- ConsumerAccess.make(settings)
      runloop <- Runloop(
                   settings.hasGroupId,
                   wrapper,
                   settings.pollInterval,
                   settings.pollTimeout,
                   diagnostics,
                   settings.offsetRetrieval,
                   settings.rebalanceListener,
                   settings.restartStreamOnRebalancing
                 )
    } yield Live(wrapper, settings, runloop)

  /**
   * Accessor method for [[Consumer.assignment]]
   */
  def assignment: RIO[Consumer, Set[TopicPartition]] =
    ZIO.serviceWithZIO(_.assignment)

  /**
   * Accessor method for [[Consumer.beginningOffsets]]
   */
  def beginningOffsets(
    partitions: Set[TopicPartition],
    timeout: Duration = Duration.Infinity
  ): RIO[Consumer, Map[TopicPartition, Long]] =
    ZIO.serviceWithZIO(_.beginningOffsets(partitions, timeout))

  /**
   * Accessor method for [[Consumer.committed]]
   */
  def committed(
    partitions: Set[TopicPartition],
    timeout: Duration = Duration.Infinity
  ): RIO[Consumer, Map[TopicPartition, Option[OffsetAndMetadata]]] =
    ZIO.serviceWithZIO(_.committed(partitions, timeout))

  /**
   * Accessor method for [[Consumer.endOffsets]]
   */
  def endOffsets(
    partitions: Set[TopicPartition],
    timeout: Duration = Duration.Infinity
  ): RIO[Consumer, Map[TopicPartition, Long]] =
    ZIO.serviceWithZIO(_.endOffsets(partitions, timeout))

  /**
   * Accessor method for [[Consumer.listTopics]]
   */
  def listTopics(
    timeout: Duration = Duration.Infinity
  ): RIO[Consumer, Map[String, List[PartitionInfo]]] =
    ZIO.serviceWithZIO(_.listTopics(timeout))

  /**
   * Accessor method for [[Consumer.partitionedStream]]
   */
  def partitionedStream[R, K, V](
    keyDeserializer: Deserializer[R, K],
    valueDeserializer: Deserializer[R, V]
  ): ZStream[
    Consumer,
    Throwable,
    (TopicPartition, ZStream[R, Throwable, CommittableRecord[K, V]])
  ] =
    ZStream.environmentWithStream(_.get[Consumer].partitionedStream(keyDeserializer, valueDeserializer))

  /**
   * Accessor method for [[Consumer.plainStream]]
   */
  def plainStream[R, K, V](
    keyDeserializer: Deserializer[R, K],
    valueDeserializer: Deserializer[R, V],
    bufferSize: Int = 4
  ): ZStream[R & Consumer, Throwable, CommittableRecord[K, V]] =
    ZStream.environmentWithStream(_.get[Consumer].plainStream(keyDeserializer, valueDeserializer, bufferSize))

  /**
   * Accessor method for [[Consumer.stopConsumption]]
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
   * val consumerIO = Consumer.consumeWith(settings, subscription, Serdes.string, Serdes.string) { case (key, value) =>
   *   // Process the received record here
   *   putStrLn(s"Received record: \${key}: \${value}")
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
   *   Function that returns the effect to execute for each message. It is passed the key and value
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
  def consumeWith[R: Tag, R1: Tag, K, V](
    settings: ConsumerSettings,
    subscription: Subscription,
    keyDeserializer: Deserializer[R, K],
    valueDeserializer: Deserializer[R, V],
    commitRetryPolicy: Schedule[Any, Any, Any] = Schedule.exponential(1.second) && Schedule.recurs(3)
  )(f: (K, V) => URIO[R1, Unit]): RIO[R & R1, Unit] =
    ZIO.scoped[R & R1] {
      Consumer
        .make(settings)
        .flatMap(_.consumeWith[R, R1, K, V](subscription, keyDeserializer, valueDeserializer, commitRetryPolicy)(f))
    }

  /**
   * Accessor method for [[Consumer.subscribe]]
   */
  def subscribe(subscription: Subscription): RIO[Consumer, Unit] =
    ZIO.serviceWithZIO(_.subscribe(subscription))

  /**
   * Accessor method for [[Consumer.unsubscribe]]
   */
  def unsubscribe: RIO[Consumer, Unit] =
    ZIO.serviceWithZIO(_.unsubscribe)

  /**
   * Accessor method for [[Consumer.offsetsForTimes]]
   */
  def offsetsForTimes(
    timestamps: Map[TopicPartition, Long],
    timeout: Duration = Duration.Infinity
  ): RIO[Consumer, Map[TopicPartition, OffsetAndTimestamp]] =
    ZIO.serviceWithZIO(_.offsetsForTimes(timestamps, timeout))

  /**
   * Accessor method for [[Consumer.partitionsFor]]
   */
  def partitionsFor(
    topic: String,
    timeout: Duration = Duration.Infinity
  ): RIO[Consumer, List[PartitionInfo]] =
    ZIO.serviceWithZIO(_.partitionsFor(topic, timeout))

  /**
   * Accessor method for [[Consumer.position]]
   */
  def position(
    partition: TopicPartition,
    timeout: Duration = Duration.Infinity
  ): RIO[Consumer, Long] =
    ZIO.serviceWithZIO(_.position(partition, timeout))

  /**
   * Accessor method for [[Consumer.subscribeAnd]]
   */
  def subscribeAnd(
    subscription: Subscription
  ): SubscribedConsumerFromEnvironment =
    new SubscribedConsumerFromEnvironment(
      ZIO.environmentWithZIO { env =>
        val consumer = env.get[Consumer]
        consumer.subscribe(subscription).as(consumer)
      }
    )

  /**
   * Accessor method for [[Consumer.subscription]]
   */
  def subscription: RIO[Consumer, Set[String]] =
    ZIO.serviceWithZIO(_.subscription)

  /**
   * Accessor method for [[Consumer.metrics]]
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
