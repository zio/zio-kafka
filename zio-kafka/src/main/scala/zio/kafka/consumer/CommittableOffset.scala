package zio.kafka.consumer

import org.apache.kafka.clients.consumer.{ ConsumerGroupMetadata, OffsetAndMetadata }
import org.apache.kafka.common.TopicPartition

trait CommittableOffset {
  def offsets: Map[TopicPartition, OffsetAndMetadata]
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
  offsets: Map[TopicPartition, OffsetAndMetadata],
  consumerGroupMetadata: Option[ConsumerGroupMetadata]
) extends OffsetBatch {
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
  def withMetadata(metadata: String): Offset

  private[consumer] def metadata: Option[String]
  private[consumer] def asJavaOffsetAndMetadata: OffsetAndMetadata = new OffsetAndMetadata(offset, metadata.orNull)

  final lazy val topicPartition: TopicPartition = new TopicPartition(topic, partition)
}

object Offset {}

private final case class OffsetImpl(
  topic: String,
  partition: Int,
  offset: Long,
  consumerGroupMetadata: Option[ConsumerGroupMetadata],
  metadata: Option[String] = None
) extends Offset {
  override def batch: OffsetBatch                              = OffsetBatchImpl(offsets, consumerGroupMetadata)
  override def offsets: Map[TopicPartition, OffsetAndMetadata] = Map(topicPartition -> asJavaOffsetAndMetadata)
  def withMetadata(metadata: String): OffsetImpl               = copy(metadata = Some(metadata))
}
