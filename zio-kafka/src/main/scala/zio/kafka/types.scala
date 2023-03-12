package zio.kafka

import org.apache.kafka.clients.consumer.ConsumerGroupMetadata
import org.apache.kafka.common.TopicPartition
import zio.kafka.consumer.Offset
import zio.prelude.Subtype

object types {

  final case class TransactionalOffsetBatch(
    offsetBatch: OffsetBatch,
    consumerGroupMetadata: () => Option[ConsumerGroupMetadata]
  ) {
    @inline def isEmpty: Boolean = offsetBatch.isEmpty
  }
  object TransactionalOffsetBatch {
    val empty: TransactionalOffsetBatch = TransactionalOffsetBatch(OffsetBatch.empty, () => None)
  }

  private[zio] object OffsetBatch extends Subtype[Map[TopicPartition, Offset]] {
    def empty: OffsetBatch                           = OffsetBatch(Map.empty)
    def single(offset: Offset): OffsetBatch          = OffsetBatch(Map(offset.topicPartition -> offset))
    def from(offsets: Iterable[Offset]): OffsetBatch = offsets.foldLeft(OffsetBatch.empty)(_.add(_))

    implicit final class RichOffsetBatch(private val self: OffsetBatch) extends AnyVal {
      def add(offset: Offset): OffsetBatch = {
        val tp = offset.topicPartition
        OffsetBatch(
          self + (tp -> self.get(tp).fold(ifEmpty = offset)(_ max offset))
        )
      }

      def merge(others: OffsetBatch): OffsetBatch = {
        val newOffsets = Map.newBuilder[TopicPartition, Offset]
        newOffsets ++= self
        others.foreach { case (tp, offset) =>
          newOffsets += tp -> self.get(tp).fold(ifEmpty = offset)(_ max offset)
        }
        OffsetBatch(newOffsets.result())
      }
    }
  }
  type OffsetBatch = OffsetBatch.Type

}
