package zio.kafka.consumer

import org.apache.kafka.clients.consumer.{ ConsumerGroupMetadata, OffsetAndMetadata }
import org.apache.kafka.common.TopicPartition
import zio._

import scala.math.Ordering.Implicits._

sealed trait OffsetBatch {

  /** The committable next offsets. */
  def nextOffsets: Map[TopicPartition, OffsetAndMetadata]

  /** Commit the next offsets. */
  def commit: Task[Unit]

  def add(offset: Offset): OffsetBatch
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

private object OffsetBatchImpl {
  implicit val offsetAndMetadataOrdering: Ordering[OffsetAndMetadata] =
    Ordering.by(_.offset())
}

private final case class OffsetBatchImpl(
  nextOffsets: Map[TopicPartition, OffsetAndMetadata],
  commitHandle: Map[TopicPartition, OffsetAndMetadata] => Task[Unit],
  consumerGroupMetadata: Option[ConsumerGroupMetadata]
) extends OffsetBatch {
  import OffsetBatchImpl._

  override def commit: Task[Unit] = commitHandle(nextOffsets)

  override def add(offset: Offset): OffsetBatch = {
    val laterOffset = nextOffsets.get(offset.topicPartition) match {
      case Some(existing) => existing.max(offset.nextOffset)
      case _              => offset.nextOffset
    }
    copy(nextOffsets = nextOffsets + (offset.topicPartition -> laterOffset))
  }

  override def merge(otherOffsets: OffsetBatch): OffsetBatch = {
    val newOffsets = Map.newBuilder[TopicPartition, OffsetAndMetadata]
    newOffsets ++= nextOffsets
    otherOffsets.nextOffsets.foreach { case (tp, offset) =>
      val laterOffset = nextOffsets.get(tp) match {
        case Some(existing) => existing.max(offset)
        case None           => offset
      }
      newOffsets += tp -> laterOffset
    }
    copy(nextOffsets = newOffsets.result())
  }
}

case object EmptyOffsetBatch extends OffsetBatch {
  override val nextOffsets: Map[TopicPartition, OffsetAndMetadata]  = Map.empty
  override val commit: Task[Unit]                                   = ZIO.unit
  override def add(offset: Offset): OffsetBatch                     = offset.batch
  override def merge(offsets: OffsetBatch): OffsetBatch             = offsets
  override def consumerGroupMetadata: Option[ConsumerGroupMetadata] = None
}
