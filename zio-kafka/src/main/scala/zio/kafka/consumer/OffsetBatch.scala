package zio.kafka.consumer

import org.apache.kafka.common.TopicPartition

import scala.collection.mutable

final case class OffsetBatch(offsets: Map[TopicPartition, Long]) {

  def nonEmpty: Boolean = offsets.nonEmpty

  def add(record: CommittableRecord[_, _]): OffsetBatch = {
    val tp = record.topicPartition
    val newOffset =
      offsets.get(tp) match {
        case None                => record.offset
        case Some(previousTuple) => math.max(previousTuple, record.offset)
      }

    copy(offsets = offsets + (tp -> newOffset))
  }

  @deprecated("Use add(Offset) instead", "2.1.4")
  def merge(record: CommittableRecord[_, _]): OffsetBatch = add(record)

  def merge(otherOffsetBatch: OffsetBatch): OffsetBatch = {
    val newOffsets = Map.newBuilder[TopicPartition, Long]
    newOffsets ++= offsets
    otherOffsetBatch.offsets.foreach { case tuple @ (tp, offset) =>
      offsets.get(tp) match {
        case None => newOffsets += tuple
        case Some(existing) =>
          if (existing < offset) {
            newOffsets += tuple
          }
      }
    }

    copy(offsets = newOffsets.result())
  }
}

object OffsetBatch {
  val empty: OffsetBatch = OffsetBatch(offsets = Map.empty)

  def apply(record: CommittableRecord[_, _]): OffsetBatch =
    OffsetBatch(offsets = Map(record.topicPartition -> record.offset))

  def apply(records: Iterable[CommittableRecord[_, _]]): OffsetBatch = {
    val offsets = new mutable.HashMap[TopicPartition, Long]
    offsets.sizeHint(records.size)

    records.foreach { r =>
      val tp = r.topicPartition
      val newOffset =
        offsets.get(tp) match {
          case None                 => r.offset
          case Some(previousOffset) => math.max(previousOffset, r.offset)
        }

      offsets += tp -> newOffset
    }

    OffsetBatch(offsets = offsets.toMap)
  }
}
