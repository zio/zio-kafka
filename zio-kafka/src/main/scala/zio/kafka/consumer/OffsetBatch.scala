package zio.kafka.consumer

import org.apache.kafka.clients.consumer.{ ConsumerGroupMetadata, OffsetAndMetadata }
import org.apache.kafka.common.TopicPartition
import zio._

import scala.math.Ordering.Implicits._

/**
 * A set of offsets, one per topic-partition, that is committed in a single call.
 *
 * When offsets of the same partition are added, the one with the highest next offset is kept, together with its
 * metadata. Collect the offsets of processed records into a batch and commit the batch, instead of committing each
 * offset.
 *
 * {{{
 * import zio._
 * import zio.stream._
 *
 * def commitInBatches(offsets: ZStream[Any, Throwable, Offset]): Task[Unit] =
 *   offsets
 *     .aggregateAsyncWithin(Consumer.collectOffsets, Schedule.fixed(100.millis))
 *     .mapZIO(_.commit)
 *     .runDrain
 * }}}
 *
 * @see
 *   [[https://zio.dev/zio-kafka/consumer/ Consumer]]
 */
sealed trait OffsetBatch {

  /** The committable next offsets. */
  def nextOffsets: Map[TopicPartition, OffsetAndMetadata]

  /** Commit the next offsets. */
  def commit: Task[Unit]

  /**
   * Returns a new batch that also contains the given offset. When this batch already has an offset for the same
   * partition, the one with the highest next offset is kept.
   */
  def add(offset: Offset): OffsetBatch

  /**
   * Returns a new batch that contains the offsets of this batch and of `offsets`. For each partition the offset with
   * the highest next offset is kept.
   */
  def merge(offsets: OffsetBatch): OffsetBatch

  /**
   * The metadata of the consumer group that consumed the offsets, see [[Offset.consumerGroupMetadata]]. It is `None`
   * for an empty batch.
   */
  def consumerGroupMetadata: Option[ConsumerGroupMetadata]

  /**
   * Attempts to commit and retries according to the given policy when the commit fails with a
   * RetriableCommitFailedException or commit timeout.
   */
  def commitOrRetry[R](policy: Schedule[R, Throwable, Any]): RIO[R, Unit] =
    Offset.commitOrRetry(commit, policy)
}

/** Constructors for [[OffsetBatch]]. */
object OffsetBatch {

  /** A batch without offsets. Committing it does nothing. */
  val empty: OffsetBatch = EmptyOffsetBatch

  /** A batch that contains only the given offset. */
  def apply(offset: Offset): OffsetBatch = empty.add(offset)

  /** A batch that contains the given offsets. For each partition the offset with the highest next offset is kept. */
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
      case Some(existing) => offset.nextOffset.max(existing)
      case _              => offset.nextOffset
    }
    copy(nextOffsets = nextOffsets + (offset.topicPartition -> laterOffset))
  }

  override def merge(otherOffsets: OffsetBatch): OffsetBatch = {
    val newOffsets = Map.newBuilder[TopicPartition, OffsetAndMetadata]
    newOffsets ++= nextOffsets
    otherOffsets.nextOffsets.foreach { case (tp, offset) =>
      val laterOffset = nextOffsets.get(tp) match {
        case Some(existing) => offset.max(existing)
        case None           => offset
      }
      newOffsets += tp -> laterOffset
    }
    copy(nextOffsets = newOffsets.result())
  }
}

/** The [[OffsetBatch]] without offsets, also available as `OffsetBatch.empty`. */
case object EmptyOffsetBatch extends OffsetBatch {
  override val nextOffsets: Map[TopicPartition, OffsetAndMetadata]  = Map.empty
  override val commit: Task[Unit]                                   = ZIO.unit
  override def add(offset: Offset): OffsetBatch                     = offset.batch
  override def merge(offsets: OffsetBatch): OffsetBatch             = offsets
  override def consumerGroupMetadata: Option[ConsumerGroupMetadata] = None
}
