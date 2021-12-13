package zio.kafka.consumer

import org.apache.kafka.clients.consumer.ConsumerRebalanceListener
import org.apache.kafka.common.TopicPartition
import zio.{ Runtime, Task, UIO }
import scala.jdk.CollectionConverters._

/**
 * ZIO wrapper around Kafka's [[ConsumerRebalanceListener]] to work with Scala collection types and ZIO effects
 */
final case class RebalanceListener(
  onAssigned: (Set[TopicPartition], RebalanceConsumer) => Task[Unit],
  onRevoked: (Set[TopicPartition], RebalanceConsumer) => Task[Unit]
) {

  /**
   * Combine with another [[RebalanceListener]] and execute their actions sequentially
   */
  def ++(that: RebalanceListener): RebalanceListener =
    RebalanceListener(
      (assigned, consumer) => onAssigned(assigned, consumer) *> that.onAssigned(assigned, consumer),
      (revoked, consumer) => onRevoked(revoked, consumer) *> that.onRevoked(revoked, consumer)
    )

  def toKafka(runtime: Runtime[Any], consumer: RebalanceConsumer): ConsumerRebalanceListener =
    new ConsumerRebalanceListener {
      override def onPartitionsRevoked(partitions: java.util.Collection[TopicPartition]): Unit = {
        runtime.unsafeRun(onRevoked(partitions.asScala.toSet, consumer))
        ()
      }

      override def onPartitionsAssigned(partitions: java.util.Collection[TopicPartition]): Unit = {
        runtime.unsafeRun(onAssigned(partitions.asScala.toSet, consumer))
        ()
      }
    }

}

object RebalanceListener {
  val noop = RebalanceListener((_, _) => UIO.unit, (_, _) => UIO.unit)
}
