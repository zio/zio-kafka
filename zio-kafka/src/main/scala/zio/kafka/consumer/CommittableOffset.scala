package zio.kafka.consumer

import org.apache.kafka.clients.consumer.ConsumerGroupMetadata
import org.apache.kafka.common.TopicPartition

trait CommittableOffset {
  def offsets: Map[TopicPartition, Long]
}

// TODO remove the trait and just keep the implementation..?
sealed trait OffsetBatch extends CommittableOffset {
  def add(offset: Offset): OffsetBatch
  @deprecated("Use add(Offset) instead", "2.1.4")
  def merge(offset: Offset): OffsetBatch
  def merge(offsets: OffsetBatch): OffsetBatch
  def consumerGroupMetadata: Option[ConsumerGroupMetadata]
}

object OffsetBatch {
  val empty: OffsetBatch = EmptyOffsetBatch

  def apply(offsets: Iterable[Offset]): OffsetBatch = offsets.foldLeft(empty)(_ add _)
}

private final case class OffsetBatchImpl(
  offsets: Map[TopicPartition, Long],
  consumerGroupMetadata: Option[ConsumerGroupMetadata]
) extends OffsetBatch {
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
  override def add(offset: Offset): OffsetBatch                     = offset.batch
  override def merge(offset: Offset): OffsetBatch                   = add(offset)
  override def merge(offsets: OffsetBatch): OffsetBatch             = offsets
  override def consumerGroupMetadata: Option[ConsumerGroupMetadata] = None
}

sealed trait Offset extends CommittableOffset {
  def topic: String
  def partition: Int
  def offset: Long
  def batch: OffsetBatch
  def consumerGroupMetadata: Option[ConsumerGroupMetadata]

  final lazy val topicPartition: TopicPartition = new TopicPartition(topic, partition)
}

object Offset {}

private final case class OffsetImpl(
  topic: String,
  partition: Int,
  offset: Long,
  consumerGroupMetadata: Option[ConsumerGroupMetadata]
) extends Offset {
  override def batch: OffsetBatch                 = OffsetBatchImpl(offsets, consumerGroupMetadata)
  override def offsets: Map[TopicPartition, Long] = Map(topicPartition -> offset)
}
