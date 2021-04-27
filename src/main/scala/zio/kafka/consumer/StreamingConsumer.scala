package zio.kafka.consumer

import org.apache.kafka.common.TopicPartition
import zio.blocking.Blocking
import zio.clock.Clock
import zio.kafka.consumer.Consumer.{ offsetBatches, OffsetRetrieval }
import zio.kafka.consumer.diagnostics.Diagnostics
import zio.kafka.consumer.internal.{ ConsumerAccess, Runloop }
import zio.kafka.serde.Deserializer
import zio.stream.ZStream
import zio._
import zio.duration._
import zio.kafka.consumer.internal.Runloop.Mode

import scala.jdk.CollectionConverters._

object streaming {

  type StreamingConsumer = Has[StreamingConsumer.Service]

  object StreamingConsumer {
    def live: ZLayer[Has[Diagnostics], Nothing, StreamingConsumer] =
      ZLayer.fromService[Diagnostics, Service](diagnostics => make(diagnostics))

    def make(
      diagnostics: Diagnostics = Diagnostics.NoOp
    ): Service =
      Live(diagnostics)

    trait Service {

      /**
       * Create a stream with messages on the subscribed topic-partitions by topic-partition
       *
       * The top-level stream will emit new topic-partition streams for each topic-partition that is assigned
       * to this consumer. This is subject to consumer rebalancing, unless a manual subscription
       * was made. When rebalancing occurs, new topic-partition streams may be emitted and existing
       * streams may be completed.
       *
       * All streams can be completed by calling [[stopConsumption]].
       * */
      def partitionedStream[R, K, V](
        settings: ConsumerSettings,
        subscription: Subscription,
        keyDeserializer: Deserializer[R, K],
        valueDeserializer: Deserializer[R, V]
      ): ZStream[
        Clock with Blocking,
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
        settings: ConsumerSettings,
        subscription: Subscription,
        keyDeserializer: Deserializer[R, K],
        valueDeserializer: Deserializer[R, V],
        outputBuffer: Int = 4
      ): ZStream[R with Clock with Blocking, Throwable, CommittableRecord[K, V]]

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
      ): ZIO[R with R1 with Blocking with Clock, Throwable, Unit]
    }

    case class Live(
      private val diagnostics: Diagnostics = Diagnostics.NoOp
    ) extends Service {

      /**
       * Create a stream with messages on the subscribed topic-partitions by topic-partition
       *
       * The top-level stream will emit new topic-partition streams for each topic-partition that is assigned
       * to this consumer. This is subject to consumer rebalancing, unless a manual subscription
       * was made. When rebalancing occurs, new topic-partition streams may be emitted and existing
       * streams may be completed.
       *
       * All streams can be completed by calling [[stopConsumption]].
       * */
      override def partitionedStream[R, K, V](
        settings: ConsumerSettings,
        subscription: Subscription,
        keyDeserializer: Deserializer[R, K],
        valueDeserializer: Deserializer[R, V]
      ): ZStream[Clock with Blocking, Throwable, (TopicPartition, ZStream[R, Throwable, CommittableRecord[K, V]])] = {
        val configureDeserializers =
          ZStream.fromEffect {
            keyDeserializer.configure(settings.driverSettings, isKey = true) *>
              valueDeserializer.configure(settings.driverSettings, isKey = false)
          }

        val runLoopWithConsumerAccessStream = ZStream.managed(
          for {
            consumerAccess <- ConsumerAccess.make(settings)
            runloop <- Runloop(
                        consumerAccess,
                        settings.pollInterval,
                        settings.pollTimeout,
                        diagnostics,
                        settings.offsetRetrieval,
                        Mode.PartitionStream
                      )
          } yield (runloop, consumerAccess)
        )

        configureDeserializers *>
          runLoopWithConsumerAccessStream.flatMap {
            case (runLoop, consumerAccess) =>
              ZStream.fromEffect(subscribe(consumerAccess, settings, subscription, runLoop, false)) *>
                ZStream
                  .fromQueue(runLoop.partitions)
                  .flattenExitOption
                  .map {
                    case (tp, partition) =>
                      val partitionStream =
                        if (settings.perPartitionChunkPrefetch <= 0) partition
                        else partition.buffer(settings.perPartitionChunkPrefetch)

                      tp -> partitionStream.mapChunksM(_.mapM(_.deserializeWith(keyDeserializer, valueDeserializer)))
                  }
          }

      }

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
      override def plainStream[R, K, V](
        settings: ConsumerSettings,
        subscription: Subscription,
        keyDeserializer: Deserializer[R, K],
        valueDeserializer: Deserializer[R, V],
        outputBuffer: Int
      ): ZStream[R with Clock with Blocking, Throwable, CommittableRecord[K, V]] = {
        val configureDeserializers =
          ZStream.fromEffect {
            keyDeserializer.configure(settings.driverSettings, isKey = true) *>
              valueDeserializer.configure(settings.driverSettings, isKey = false)
          }

        val runLoopWithConsumerAccessStream = ZStream.managed(
          for {
            consumerAccess <- ConsumerAccess.make(settings)
            runloop <- Runloop(
                        consumerAccess,
                        settings.pollInterval,
                        settings.pollTimeout,
                        diagnostics,
                        settings.offsetRetrieval,
                        Mode.PlainStream
                      )
          } yield (runloop, consumerAccess)
        )

        for {
          _                         <- configureDeserializers
          (runLoop, consumerAccess) <- runLoopWithConsumerAccessStream
          _                         <- ZStream.fromEffect(subscribe(consumerAccess, settings, subscription, runLoop, true))
          stream                    = runLoop.newPlainStream().mapChunksM(_.mapM(_.deserializeWith(keyDeserializer, valueDeserializer)))
          record                    <- (if (outputBuffer <= 0) stream else stream.buffer(outputBuffer))
        } yield record
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
        plainStream(settings, subscription, keyDeserializer, valueDeserializer)
          .mapChunksM(_.mapM {
            case CommittableRecord(record, offset) =>
              f(record.key(), record.value()).as(offset)
          })
          .aggregateAsync(offsetBatches)
          .mapM(_.commitOrRetry(commitRetryPolicy))
          .runDrain

      private def subscribe(
        consumerAccess: ConsumerAccess,
        settings: ConsumerSettings,
        subscription: Subscription,
        runloop: Runloop,
        isPlain: Boolean
      ) =
        ZIO.runtime[Any].flatMap { runtime =>
          consumerAccess.withConsumerM { c =>
            subscription match {
              case Subscription.Pattern(pattern) =>
                ZIO(c.subscribe(pattern.pattern, runloop.rebalanceListener.toKafka(runtime)))
              case Subscription.Topics(topics) =>
                ZIO(c.subscribe(topics.asJava, runloop.rebalanceListener.toKafka(runtime)))

              // For manual subscriptions we have to do some manual work before starting the run loop
              case Subscription.Manual(topicPartitions) =>
                ZIO(c.assign(topicPartitions.asJava)) *>
                  ZIO.foreach_(topicPartitions)(runloop.newPartitionStream).unless(isPlain) *> {
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
    }

  }
  def partitionedStream[R, K, V](
    settings: ConsumerSettings,
    subscription: Subscription,
    keyDeserializer: Deserializer[R, K],
    valueDeserializer: Deserializer[R, V]
  ): ZStream[
    R with Clock with Blocking with StreamingConsumer,
    Throwable,
    (TopicPartition, ZStream[R, Throwable, CommittableRecord[K, V]])
  ] =
    ZStream.accessStream(
      _.get[StreamingConsumer.Service].partitionedStream(settings, subscription, keyDeserializer, valueDeserializer)
    )

  def plainStream[R, K, V](
    settings: ConsumerSettings,
    subscription: Subscription,
    keyDeserializer: Deserializer[R, K],
    valueDeserializer: Deserializer[R, V],
    outputBuffer: Int = 4
  ): ZStream[R with Clock with Blocking with StreamingConsumer, Throwable, CommittableRecord[K, V]] =
    ZStream.accessStream(
      _.get[StreamingConsumer.Service]
        .plainStream(settings, subscription, keyDeserializer, valueDeserializer, outputBuffer)
    )

  def consumeWith[R, R1: Tag, K: Tag, V: Tag](
    settings: ConsumerSettings,
    subscription: Subscription,
    keyDeserializer: Deserializer[R1, K],
    valueDeserializer: Deserializer[R1, V],
    commitRetryPolicy: Schedule[Clock, Any, Any] = Schedule.exponential(1.second) && Schedule.recurs(3)
  )(
    f: (K, V) => ZIO[R, Nothing, Unit]
  ): ZIO[R with R1 with Blocking with Clock with StreamingConsumer, Throwable, Unit] =
    ZIO.accessM(
      _.get[StreamingConsumer.Service]
        .consumeWith(settings, subscription, keyDeserializer, valueDeserializer, commitRetryPolicy)(f)
    )
}
