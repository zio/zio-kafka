package zio.kafka.consumer

import org.apache.kafka.clients.consumer.ConsumerRebalanceListener
import org.apache.kafka.common.TopicPartition
import zio.{ Chunk, Runtime, Task, Unsafe, ZIO }

/**
 * ZIO wrapper around Kafka's `ConsumerRebalanceListener` to work with Scala collection types and ZIO effects.
 *
 * Note that the given ZIO effects are executed directly on the Kafka poll thread. Fork and shift to another executor
 * when this is not desired.
 */
final case class RebalanceListener(
  onAssigned: (Chunk[TopicPartition], RebalanceConsumer) => Task[Unit],
  onRevoked: (Chunk[TopicPartition], RebalanceConsumer) => Task[Unit],
  onLost: (Chunk[TopicPartition], RebalanceConsumer) => Task[Unit]
) {

  /**
   * Combine with another [[RebalanceListener]] and execute their actions sequentially
   */
  def ++(that: RebalanceListener): RebalanceListener =
    RebalanceListener(
      (assigned, consumer) => onAssigned(assigned, consumer) *> that.onAssigned(assigned, consumer),
      (revoked, consumer) => onRevoked(revoked, consumer) *> that.onRevoked(revoked, consumer),
      (lost, consumer) => onLost(lost, consumer) *> that.onLost(lost, consumer)
    )

  def toKafka(
    runtime: Runtime[Any],
    consumer: RebalanceConsumer
  ): ConsumerRebalanceListener =
    new ConsumerRebalanceListener {
      override def onPartitionsRevoked(
        partitions: java.util.Collection[TopicPartition]
      ): Unit = Unsafe.unsafe { implicit u =>
        runtime.unsafe
          .run(onRevoked(Chunk.fromJavaIterable(partitions), consumer))
          .getOrThrowFiberFailure()
        ()
      }

      override def onPartitionsAssigned(
        partitions: java.util.Collection[TopicPartition]
      ): Unit = Unsafe.unsafe { implicit u =>
        runtime.unsafe
          .run(onAssigned(Chunk.fromJavaIterable(partitions), consumer))
          .getOrThrowFiberFailure()
        ()
      }

      override def onPartitionsLost(
        partitions: java.util.Collection[TopicPartition]
      ): Unit = Unsafe.unsafe { implicit u =>
        runtime.unsafe
          .run(onLost(Chunk.fromJavaIterable(partitions), consumer))
          .getOrThrowFiberFailure()
        ()
      }
    }

}

object RebalanceListener {
  def apply(
    onAssigned: (Chunk[TopicPartition], RebalanceConsumer) => Task[Unit],
    onRevoked: (Chunk[TopicPartition], RebalanceConsumer) => Task[Unit]
  ): RebalanceListener =
    RebalanceListener(onAssigned, onRevoked, onRevoked)

  val noop: RebalanceListener = RebalanceListener(
    (_, _) => ZIO.unit,
    (_, _) => ZIO.unit,
    (_, _) => ZIO.unit
  )
}
