package zio.kafka.client

import org.apache.kafka.common.TopicPartition
import zio.clock.Clock
import zio.{ Schedule, Task, ZIO }

sealed trait OffsetBatch {
  def offsets: Map[TopicPartition, Long]
  def commit: Task[Unit]
  def merge(offset: Offset): OffsetBatch
  def merge(offsets: OffsetBatch): OffsetBatch

  /**
   * Attempts to commit and retries according to the given policy when the commit fails
   * with a RetriableCommitFailedException
   */
  def commitOrRetry[R, B](policy: Schedule[R, Throwable, B]): ZIO[R with Clock, Throwable, Unit] =
    Offset.commitOrRetry(commit, policy)
}

object OffsetBatch {
  val empty: OffsetBatch = EmptyOffsetBatch

  def apply(offsets: Iterable[Offset]): OffsetBatch = offsets.foldLeft(empty)(_ merge _)
}

final case class OffsetBatchImpl(
  offsets: Map[TopicPartition, Long],
  commitHandle: Map[TopicPartition, Long] => Task[Unit]
) extends OffsetBatch {
  val commit: Task[Unit] = commitHandle(offsets)

  def merge(offset: Offset) =
    copy(
      offsets = offsets + (offset.topicPartition -> (offsets
        .getOrElse(offset.topicPartition, -1L) max offset.offset))
    )

  def merge(otherOffsets: OffsetBatch) = {
    val newOffsets = Map.newBuilder[TopicPartition, Long]
    newOffsets ++= offsets
    otherOffsets.offsets.foreach {
      case (tp, offset) =>
        val existing = offsets.getOrElse(tp, -1L)
        if (existing < offset)
          newOffsets += tp -> offset
    }

    copy(offsets = newOffsets.result())
  }
}

case object EmptyOffsetBatch extends OffsetBatch {
  val offsets: Map[TopicPartition, Long]       = Map()
  val commit                                   = Task.unit
  def merge(offset: Offset): OffsetBatch       = offset.batch
  def merge(offsets: OffsetBatch): OffsetBatch = offsets
}
