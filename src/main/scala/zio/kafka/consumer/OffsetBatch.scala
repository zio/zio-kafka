package zio.kafka.consumer

import org.apache.kafka.clients.consumer.ConsumerGroupMetadata
import org.apache.kafka.common.TopicPartition
import zio.{ RIO, Schedule, Task }
import zio.clock.Clock

sealed trait OffsetBatch {
  def offsets: Map[TopicPartition, Long]
  def commit: Task[Unit]
  def merge(offset: Offset): OffsetBatch
  def merge(offsets: OffsetBatch): OffsetBatch
  def consumerGroupMetadata: ConsumerGroupMetadata

  /**
   * Attempts to commit and retries according to the given policy when the commit fails with a
   * RetriableCommitFailedException
   */
  def commitOrRetry[R](policy: Schedule[R, Throwable, Any]): RIO[R with Clock, Unit] =
    Offset.commitOrRetry(commit, policy)
}

object OffsetBatch {
  val empty: OffsetBatch = EmptyOffsetBatch

  def apply(offsets: Iterable[Offset]): OffsetBatch = offsets.foldLeft(empty)(_ merge _)
}

private final case class OffsetBatchImpl(
  offsets: Map[TopicPartition, Long],
  commitHandle: Map[TopicPartition, Long] => Task[Unit],
  consumerGroupMetadata: ConsumerGroupMetadata
) extends OffsetBatch {
  def commit: Task[Unit] = commitHandle(offsets)

  def merge(offset: Offset): OffsetBatch =
    copy(
      offsets = offsets + (offset.topicPartition -> (offsets
        .getOrElse(offset.topicPartition, -1L) max offset.offset))
    )

  def merge(otherOffsets: OffsetBatch): OffsetBatch = {
    val newOffsets = Map.newBuilder[TopicPartition, Long]
    newOffsets ++= offsets
    otherOffsets.offsets.foreach { case (tp, offset) =>
      val existing = offsets.getOrElse(tp, -1L)
      if (existing < offset)
        newOffsets += tp -> offset
    }

    copy(offsets = newOffsets.result())
  }
}

case object EmptyOffsetBatch extends OffsetBatch {
  val offsets: Map[TopicPartition, Long]           = Map()
  val commit: Task[Unit]                           = Task.unit
  def merge(offset: Offset): OffsetBatch           = offset.batch
  def merge(offsets: OffsetBatch): OffsetBatch     = offsets
  def consumerGroupMetadata: ConsumerGroupMetadata = new ConsumerGroupMetadata("")
}
