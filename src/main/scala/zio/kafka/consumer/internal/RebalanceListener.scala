package zio.kafka.consumer.internal
import org.apache.kafka.clients.consumer.ConsumerRebalanceListener
import org.apache.kafka.common.TopicPartition
import zio.{ Runtime, Task }
import scala.jdk.CollectionConverters._

/**
 * ZIO wrapper around Kafka's [[ConsumerRebalanceListener]] to work with Scala collection types and ZIO effects
 */
private[consumer] final case class RebalanceListener(
  onAssigned: Set[TopicPartition] => Task[Unit],
  onRevoked: Set[TopicPartition] => Task[Unit]
) {

  /**
   * Combine with another [[RebalanceListener]] and execute their actions sequentially
   */
  def ++(that: RebalanceListener) =
    RebalanceListener(
      assigned => onAssigned(assigned) *> that.onAssigned(assigned),
      revoked => onRevoked(revoked) *> that.onRevoked(revoked)
    )

  def toKafka(runtime: Runtime[Any]): ConsumerRebalanceListener =
    new ConsumerRebalanceListener {
      override def onPartitionsRevoked(partitions: java.util.Collection[TopicPartition]): Unit = {
        runtime.unsafeRun(onRevoked(partitions.asScala.toSet))
        ()
      }

      override def onPartitionsAssigned(partitions: java.util.Collection[TopicPartition]): Unit = {
        runtime.unsafeRun(onAssigned(partitions.asScala.toSet))
        ()
      }
    }

}

object RebalanceListener {
  def onRevoked(action: Set[TopicPartition] => Task[Unit]): RebalanceListener =
    RebalanceListener(_ => Task.unit, action)
}
