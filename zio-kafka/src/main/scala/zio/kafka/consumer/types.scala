package zio.kafka.consumer

import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.TopicPartition
import zio.kafka.serde.Deserializer
import zio.prelude.Subtype
import zio.{ Chunk, RIO }
import scala.collection.immutable.Map

object types {

  @inline
  private[zio] def topicPartition(record: ConsumerRecord[_, _]): TopicPartition =
    new TopicPartition(record.topic(), record.partition())

  private[zio] def deserializeWith[R, K, V](
    keyDeserializer: Deserializer[R, K],
    valueDeserializer: Deserializer[R, V]
  )(record: ConsumerRecord[Array[Byte], Array[Byte]]): RIO[R, ConsumerRecord[K, V]] =
    for {
      key   <- keyDeserializer.deserialize(record.topic(), record.headers(), record.key())
      value <- valueDeserializer.deserialize(record.topic(), record.headers(), record.value())
    } yield new ConsumerRecord[K, V](
      record.topic(),
      record.partition(),
      record.offset(),
      record.timestamp(),
      record.timestampType(),
      record.serializedKeySize(),
      record.serializedValueSize(),
      key,
      value,
      record.headers(),
      record.leaderEpoch()
    )

  type Offset = Offset.Type
  object Offset extends Subtype[(TopicPartition, Long)] {
    def from(record: ConsumerRecord[_, _]): Offset =
      Offset((topicPartition(record), record.offset()))
  }

  type OffsetBatch = OffsetBatch.Type
  object OffsetBatch extends Subtype[Map[TopicPartition, Long]] {
    def empty: OffsetBatch = OffsetBatch(Map.empty)

    def from(record: ConsumerRecord[_, _]): OffsetBatch =
      OffsetBatch(Map(topicPartition(record) -> record.offset()))

    def from(records: Chunk[ConsumerRecord[_, _]]): OffsetBatch =
      OffsetBatch.wrap(records.map(record => topicPartition(record) -> record.offset()).toMap)

    implicit final class OffsetBatchOps(private val self: OffsetBatch) extends AnyVal {
      def merge(other: OffsetBatch): OffsetBatch = {
        val newOffsets = Map.newBuilder[TopicPartition, Long]
        newOffsets ++= self
        other.foreach { case (tp, offset) =>
          val existing = self.getOrElse(tp, -1L)
          if (existing < offset) {
            newOffsets += tp -> offset
          }
        }
        OffsetBatch(newOffsets.result())
      }

      def add(offset: Offset): OffsetBatch = {
        val (tp, offsetValue) = offset
        val newOffset         = self.getOrElse(tp, -1L) max offsetValue
        OffsetBatch(self + (tp -> newOffset))
      }
    }
  }

}
