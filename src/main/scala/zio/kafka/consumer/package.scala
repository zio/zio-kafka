package zio.kafka

import org.apache.kafka.clients.consumer.{ OffsetAndMetadata, OffsetAndTimestamp }
import org.apache.kafka.common.{ Metric, MetricName, PartitionInfo, TopicPartition }
import zio._
import zio.blocking.Blocking
import zio.clock.Clock
import zio.duration._
import zio.kafka.consumer.diagnostics.Diagnostics
import zio.kafka.consumer.internal.{ ConsumerAccess, Runloop }
import zio.kafka.serde.Deserializer
import zio.stream._

import scala.jdk.CollectionConverters._

package object consumer {
  type Consumer = Has[Consumer.Service]

  object Consumer {
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
      } yield Live(wrapper, settings)

    /**
     * Accessor method for [[Service.assignment]]
     */
    def assignment: RIO[Blocking with Consumer, Set[TopicPartition]] =
      withConsumerService(_.assignment)

    def withConsumerService[R, A](
      r: Service => RIO[R with Blocking, A]
    ): RIO[R with Blocking with Consumer, A] =
      ZIO.accessM(env => r(env.get[Service]))

    /**
     * Accessor method for [[Service.beginningOffsets]]
     */
    def beginningOffsets(
      partitions: Set[TopicPartition],
      timeout: Duration = Duration.Infinity
    ): RIO[Blocking with Consumer, Map[TopicPartition, Long]] =
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
     * Accessor method for [[Service.unsubscribe]]
     */
    def unsubscribe: RIO[Blocking with Consumer, Unit] =
      withConsumerService(_.unsubscribe)

    /**
     * Accessor method for [[Service.offsetsForTimes]]
     */
    def offsetsForTimes(
      timestamps: Map[TopicPartition, Long],
      timeout: Duration = Duration.Infinity
    ): RIO[Blocking with Consumer, Map[TopicPartition, OffsetAndTimestamp]] =
      withConsumerService(_.offsetsForTimes(timestamps, timeout))

    /**
     * Accessor method for [[Service.partitionsFor]]
     */
    def partitionsFor(
      topic: String,
      timeout: Duration = Duration.Infinity
    ): RIO[Blocking with Consumer, List[PartitionInfo]] =
      withConsumerService(_.partitionsFor(topic, timeout))

    /**
     * Accessor method for [[Service.position]]
     */
    def position(
      partition: TopicPartition,
      timeout: Duration = Duration.Infinity
    ): RIO[Blocking with Consumer, Long] =
      withConsumerService(_.position(partition, timeout))

    /**
     * Accessor method for [[Service.subscription]]
     */
    def subscription: RIO[Blocking with Consumer, Set[String]] =
      withConsumerService(_.subscription)

    /**
     * Accessor method for [[Service.metrics]]
     */
    def metrics: RIO[Blocking with Consumer, Map[MetricName, Metric]] =
      withConsumerService(_.metrics)

    sealed trait OffsetRetrieval

    sealed trait AutoOffsetStrategy { self =>
      def toConfig = self match {
        case AutoOffsetStrategy.Earliest => "earliest"
        case AutoOffsetStrategy.Latest   => "latest"
        case AutoOffsetStrategy.None     => "none"
      }
    }

    trait Service {

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

      def unsubscribe: RIO[Blocking, Unit]

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
      ): RIO[Blocking, Map[TopicPartition, OffsetAndTimestamp]]

      def partitionsFor(topic: String, timeout: Duration = Duration.Infinity): RIO[Blocking, List[PartitionInfo]]

      def position(partition: TopicPartition, timeout: Duration = Duration.Infinity): RIO[Blocking, Long]

      def subscription: RIO[Blocking, Set[String]]

      /**
       * Expose internal consumer metrics
       */
      def metrics: RIO[Blocking, Map[MetricName, Metric]]
    }

    final case class Live(
      private val consumer: ConsumerAccess,
      private val settings: ConsumerSettings
    ) extends Service {

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

      override def listTopics(timeout: Duration = Duration.Infinity): RIO[Blocking, Map[String, List[PartitionInfo]]] =
        consumer.withConsumer(_.listTopics(timeout.asJava).asScala.view.mapValues(_.asScala.toList).toMap)

      override def offsetsForTimes(
        timestamps: Map[TopicPartition, Long],
        timeout: Duration = Duration.Infinity
      ): RIO[Blocking, Map[TopicPartition, OffsetAndTimestamp]] =
        consumer.withConsumer(
          _.offsetsForTimes(timestamps.view.mapValues(Long.box).toMap.asJava, timeout.asJava).asScala.toMap
          // If a partition doesn't exist yet, the map will have 'null' as entry.
          // It's more idiomatic scala to then simply not have that map entry.
            .filter(_._2 != null)
        )

      override def partitionsFor(
        topic: String,
        timeout: Duration = Duration.Infinity
      ): RIO[Blocking, List[PartitionInfo]] =
        consumer.withConsumer { c =>
          val partitions = c.partitionsFor(topic, timeout.asJava)
          if (partitions eq null) List.empty else partitions.asScala.toList
        }

      override def position(partition: TopicPartition, timeout: Duration = Duration.Infinity): RIO[Blocking, Long] =
        consumer.withConsumer(_.position(partition, timeout.asJava))

      override def subscription: RIO[Blocking, Set[String]] =
        consumer.withConsumer(_.subscription().asScala.toSet)

      override def unsubscribe: RIO[Blocking, Unit] =
        consumer.withConsumer(_.unsubscribe())

      override def metrics: RIO[Blocking, Map[MetricName, Metric]] =
        consumer.withConsumer(_.metrics().asScala.toMap)
    }

    object OffsetRetrieval {
      final case class Auto(reset: AutoOffsetStrategy = AutoOffsetStrategy.Latest) extends OffsetRetrieval
      final case class Manual(getOffsets: Set[TopicPartition] => Task[Map[TopicPartition, Long]])
          extends OffsetRetrieval
    }

    object AutoOffsetStrategy {
      case object Earliest extends AutoOffsetStrategy
      case object Latest   extends AutoOffsetStrategy
      case object None     extends AutoOffsetStrategy
    }
  }

}
