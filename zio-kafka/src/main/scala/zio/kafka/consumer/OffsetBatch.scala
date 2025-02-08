package zio.kafka.consumer

import org.apache.kafka.clients.consumer.{ ConsumerGroupMetadata, OffsetAndMetadata }
import org.apache.kafka.common.TopicPartition
import zio._

sealed trait OffsetBatch {
  def offsets: Map[TopicPartition, OffsetAndMetadata]
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

  def apply(offset: Offset): OffsetBatch = empty.add(offset)

  def apply(offsets: Iterable[Offset]): OffsetBatch = offsets.foldLeft(empty)(_ add _)
}

private final case class OffsetBatchImpl(
  offsets: Map[TopicPartition, OffsetAndMetadata],
  commitHandle: Map[TopicPartition, OffsetAndMetadata] => Task[Unit],
  consumerGroupMetadata: Option[ConsumerGroupMetadata]
) extends OffsetBatch {
  override def commit: Task[Unit] = commitHandle(offsets)

  override def add(offset: Offset): OffsetBatch = {
    val maxOffsetAndMetadata = offsets.get(offset.topicPartition) match {
      case Some(existing) if existing.offset > offset.offset => existing
      case _                                                 => offset.asJavaOffsetAndMetadata
    }

    copy(
      offsets = offsets + (offset.topicPartition -> maxOffsetAndMetadata)
    )
  }

  override def merge(offset: Offset): OffsetBatch = add(offset)

  override def merge(otherOffsets: OffsetBatch): OffsetBatch = {
    val newOffsets = Map.newBuilder[TopicPartition, OffsetAndMetadata]
    newOffsets ++= offsets
    otherOffsets.offsets.foreach { case (tp, offset) =>
      val laterOffset = offsets.get(tp) match {
        case Some(existing) => if (existing.offset < offset.offset) offset else existing
        case None           => offset
      }
      newOffsets += tp -> laterOffset
    }

    copy(offsets = newOffsets.result())
  }
}

case object EmptyOffsetBatch extends OffsetBatch {
  override val offsets: Map[TopicPartition, OffsetAndMetadata]      = Map.empty
  override val commit: Task[Unit]                                   = ZIO.unit
  override def add(offset: Offset): OffsetBatch                     = offset.batch
  override def merge(offset: Offset): OffsetBatch                   = add(offset)
  override def merge(offsets: OffsetBatch): OffsetBatch             = offsets
  override def consumerGroupMetadata: Option[ConsumerGroupMetadata] = None
}
