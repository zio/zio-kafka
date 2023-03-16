package zio.kafka.consumer

import org.apache.kafka.clients.consumer.ConsumerGroupMetadata
import org.apache.kafka.common.TopicPartition
import zio.{ RIO, Schedule, Task, ZIO }

sealed trait OffsetBatch {
  def offsets: Map[TopicPartition, Long]
  def commit: Task[Unit]
  def add(offset: Offset): OffsetBatch
  @deprecated("Use add(Offset) instead", "2.1.4")
  def merge(offset: Offset): OffsetBatch
  def merge(offsets: OffsetBatch): OffsetBatch
  def consumerGroupMetadata: Option[ConsumerGroupMetadata]

  /**
   * Attempts to commit and retries according to the given policy when the commit fails with a
   * RetriableCommitFailedException
   */
  def commitOrRetry[R](policy: Schedule[R, Throwable, Any]): RIO[R, Unit] =
    Offset.commitOrRetry(commit, policy)
}

object OffsetBatch {
  val empty: OffsetBatch = EmptyOffsetBatch

  def apply(offsets: Iterable[Offset]): OffsetBatch = offsets.foldLeft(empty)(_ add _)
}

private final case class OffsetBatchImpl(
  offsets: Map[TopicPartition, Long],
  commitHandle: Map[TopicPartition, Long] => Task[Unit],
  consumerGroupMetadata: Option[ConsumerGroupMetadata]
) extends OffsetBatch {
  override def commit: Task[Unit] = commitHandle(offsets)

  override def add(offset: Offset): OffsetBatch =
    copy(
      offsets = offsets + (offset.topicPartition -> (offsets
        .getOrElse(offset.topicPartition, -1L) max offset.offset))
    )

  override def merge(offset: Offset): OffsetBatch = add(offset)

  override def merge(otherOffsets: OffsetBatch): OffsetBatch = {
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
  override val offsets: Map[TopicPartition, Long]                   = Map.empty
  override val commit: Task[Unit]                                   = ZIO.unit
  override def add(offset: Offset): OffsetBatch                     = offset.batch
  override def merge(offset: Offset): OffsetBatch                   = add(offset)
  override def merge(offsets: OffsetBatch): OffsetBatch             = offsets
  override def consumerGroupMetadata: Option[ConsumerGroupMetadata] = None
}
