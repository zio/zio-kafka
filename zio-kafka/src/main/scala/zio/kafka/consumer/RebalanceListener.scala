package zio.kafka.consumer

import org.apache.kafka.clients.consumer.ConsumerRebalanceListener
import org.apache.kafka.common.TopicPartition
import zio.{ Executor, Runtime, Task, Unsafe, ZIO }

import scala.jdk.CollectionConverters._

/**
 * ZIO wrapper around Kafka's `ConsumerRebalanceListener` to work with Scala collection types and ZIO effects.
 */
final case class RebalanceListener(
  onAssigned: Set[TopicPartition] => Task[Unit],
  onRevoked: Set[TopicPartition] => Task[Unit],
  onLost: Set[TopicPartition] => Task[Unit]
) {

  /**
   * Combine with another [[RebalanceListener]] and execute their actions sequentially
   */
  def ++(that: RebalanceListener): RebalanceListener =
    RebalanceListener(
      assigned => onAssigned(assigned) *> that.onAssigned(assigned),
      revoked => onRevoked(revoked) *> that.onRevoked(revoked),
      lost => onLost(lost) *> that.onLost(lost)
    )

  def runOnExecutor(executor: Executor): RebalanceListener = RebalanceListener(
    assigned => onAssigned(assigned).onExecutor(executor),
    revoked => onRevoked(revoked).onExecutor(executor),
    lost => onLost(lost).onExecutor(executor)
  )

}

object RebalanceListener {
  def apply(
    onAssigned: Set[TopicPartition] => Task[Unit],
    onRevoked: Set[TopicPartition] => Task[Unit]
  ): RebalanceListener =
    RebalanceListener(onAssigned, onRevoked, onRevoked)

  val noop: RebalanceListener = RebalanceListener(
    _ => ZIO.unit,
    _ => ZIO.unit,
    _ => ZIO.unit
  )

  private[kafka] def toKafka(
    rebalanceListener: RebalanceListener,
    runtime: Runtime[Any]
  ): ConsumerRebalanceListener =
    new ConsumerRebalanceListener {
      override def onPartitionsRevoked(
        partitions: java.util.Collection[TopicPartition]
      ): Unit = Unsafe.unsafe { implicit u =>
        runtime.unsafe
          .run(rebalanceListener.onRevoked(partitions.asScala.toSet))
          .getOrThrowFiberFailure()
        ()
      }

      override def onPartitionsAssigned(
        partitions: java.util.Collection[TopicPartition]
      ): Unit = Unsafe.unsafe { implicit u =>
        runtime.unsafe
          .run(rebalanceListener.onAssigned(partitions.asScala.toSet))
          .getOrThrowFiberFailure()
        ()
      }

      override def onPartitionsLost(
        partitions: java.util.Collection[TopicPartition]
      ): Unit = Unsafe.unsafe { implicit u =>
        runtime.unsafe
          .run(rebalanceListener.onLost(partitions.asScala.toSet))
          .getOrThrowFiberFailure()
        ()
      }
    }
}
