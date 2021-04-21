package zio.kafka.consumer

import org.apache.kafka.common.TopicPartition
import zio.blocking.Blocking
import zio.clock.Clock
import zio.kafka.consumer.Consumer.OffsetRetrieval
import zio.kafka.consumer.diagnostics.Diagnostics
import zio.kafka.consumer.internal.{ ConsumerAccess, Runloop }
import zio.kafka.serde.Deserializer
import zio.stream.ZStream
import zio.{ Has, ZIO, ZLayer }

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
                        settings.offsetRetrieval
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
                        settings.offsetRetrieval
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
}
