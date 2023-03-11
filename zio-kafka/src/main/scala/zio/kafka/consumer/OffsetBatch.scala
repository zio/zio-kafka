package zio.kafka.consumer

import org.apache.kafka.clients.consumer.ConsumerGroupMetadata
import org.apache.kafka.common.TopicPartition
import zio.{ RIO, Schedule, Task, ZIO }

final case class OffsetBatch(
  offsets: Map[TopicPartition, Long],
  commitHandle: Map[TopicPartition, Long] => Task[Unit],
  consumerGroupMetadata: Option[ConsumerGroupMetadata]
) {
  def commit: Task[Unit] = commitHandle(offsets)

  /**
   * Attempts to commit and retries according to the given policy when the commit fails with a
   * RetriableCommitFailedException
   */
  def commitOrRetry[R](policy: Schedule[R, Throwable, Any]): RIO[R, Unit] =
    Offset.commitOrRetry(commit, policy)

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

object OffsetBatch {
  val empty: OffsetBatch = OffsetBatch(offsets = Map.empty, commitHandle = _ => ZIO.unit, None)

  def apply(offsets: Iterable[Offset]): OffsetBatch = offsets.foldLeft(empty)(_ merge _)
}
