package zio.kafka.consumer

import org.apache.kafka.clients.consumer.{ ConsumerGroupMetadata, OffsetAndMetadata }
import org.apache.kafka.common.TopicPartition

/**
 * Supertype for a single offset or a batch of offsets for multiple topic-partitions
 */
sealed trait CommittableOffset {
  def offsetsAsMap: Map[TopicPartition, OffsetAndMetadata]
  def consumerGroupMetadata: Option[ConsumerGroupMetadata]
}

sealed trait OffsetBatch extends CommittableOffset {
  def add(offset: CommittableOffset): OffsetBatch
}

object OffsetBatch {
  val empty: OffsetBatch = EmptyOffsetBatch

  def apply(offsets: Iterable[Offset]): OffsetBatch = offsets.foldLeft(empty)(_ add _)
}

private final case class OffsetBatchImpl(
  offsetsAsMap: Map[TopicPartition, OffsetAndMetadata],
  consumerGroupMetadata: Option[ConsumerGroupMetadata]
) extends OffsetBatch {
  def add(offset: CommittableOffset): OffsetBatch =
    offset match {
      case batch: OffsetBatch => merge(batch)
      case offset: Offset =>
        val maxOffsetAndMetadata = offsetsAsMap.get(offset.topicPartition) match {
          case Some(existing) if existing.offset > offset.offset => existing
          case _                                                 => offset.asJavaOffsetAndMetadata
        }

        copy(
          offsetsAsMap = offsetsAsMap + (offset.topicPartition -> maxOffsetAndMetadata)
        )
    }

  private def merge(otherOffsets: OffsetBatch): OffsetBatch = {
    val newOffsets = Map.newBuilder[TopicPartition, OffsetAndMetadata]
    newOffsets ++= offsetsAsMap
    otherOffsets.offsetsAsMap.foreach { case (tp, offset) =>
      val laterOffset = offsetsAsMap.get(tp) match {
        case Some(existing) => if (existing.offset < offset.offset) offset else existing
        case None           => offset
      }
      newOffsets += tp -> laterOffset
    }

    copy(offsetsAsMap = newOffsets.result())
  }
}

case object EmptyOffsetBatch extends OffsetBatch {
  override val offsetsAsMap: Map[TopicPartition, OffsetAndMetadata] = Map.empty

  override def add(offset: CommittableOffset): OffsetBatch = offset match {
    case batch: OffsetBatch => batch
    case o: Offset => OffsetBatchImpl(Map(o.topicPartition -> o.asJavaOffsetAndMetadata), consumerGroupMetadata)
  }

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

final case class OffsetImpl(
  topic: String,
  partition: Int,
  offset: Long,
  consumerGroupMetadata: Option[ConsumerGroupMetadata],
  metadata: Option[String] = None
) extends Offset {
  def batch: OffsetBatch = OffsetBatchImpl(offsetsAsMap, consumerGroupMetadata)
  override def offsetsAsMap: Map[TopicPartition, OffsetAndMetadata] = Map(topicPartition -> asJavaOffsetAndMetadata)
  def withMetadata(metadata: String): Offset                        = copy(metadata = Some(metadata))
}
