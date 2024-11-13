package zio.kafka.consumer.internal

import org.apache.kafka.clients.consumer.{ OffsetAndMetadata, OffsetCommitCallback }
import org.apache.kafka.common.TopicPartition
import zio.kafka.consumer.internal.Committer.CommitOffsets
import zio.kafka.consumer.internal.LiveCommitter.Commit
import zio.{ Chunk, Task, UIO }

import java.lang.Math.max
import java.util.{ Map => JavaMap }
import scala.collection.mutable

private[internal] trait Committer {
  val commit: Map[TopicPartition, OffsetAndMetadata] => Task[Unit]

  /**
   * Takes commits from the queue, commits them and adds them to pending commits
   *
   * If the queue is empty, nothing happens, unless executeOnEmpty is true.
   *
   * @param commitAsync
   *   Function 'commitAsync' on the KafkaConsumer. This is isolated from the whole KafkaConsumer for testing purposes.
   *   The caller should ensure exclusive access to the KafkaConsumer.
   * @param executeOnEmpty
   *   Execute commitAsync() even if there are no commits
   */
  def processQueuedCommits(
    commitAsync: (JavaMap[TopicPartition, OffsetAndMetadata], OffsetCommitCallback) => Task[Unit],
    executeOnEmpty: Boolean = false
  ): Task[Unit]

  def queueSize: UIO[Int]

  def pendingCommitCount: UIO[Int]

  def getPendingCommits: UIO[CommitOffsets]

  def updatePendingCommitsAfterPoll: UIO[Unit]

  def pruneCommittedOffsets(assignedPartitions: Set[TopicPartition]): UIO[Unit]

  def getCommittedOffsets: UIO[CommitOffsets]
}

private[internal] object Committer {
  final case class CommitOffsets(offsets: Map[TopicPartition, Long]) {

    /** Returns an estimate of the total offset increase, and a new `CommitOffsets` with the given offsets added. */
    def addCommits(c: Chunk[Commit]): (Long, CommitOffsets) = {
      val updatedOffsets = mutable.Map.empty[TopicPartition, Long]
      updatedOffsets.sizeHint(offsets.size)
      updatedOffsets ++= offsets
      var offsetIncrease = 0L
      c.foreach { commit =>
        commit.offsets.foreach { case (tp, offsetAndMeta) =>
          val offset = offsetAndMeta.offset()
          val maxOffset = updatedOffsets.get(tp) match {
            case Some(existingOffset) =>
              offsetIncrease += max(0L, offset - existingOffset)
              max(existingOffset, offset)
            case None =>
              // This partition was not committed to from this consumer yet. Therefore we do not know the offset
              // increase. A good estimate would be the poll size for this consumer, another okayish estimate is 0.
              // Lets go with the simplest for now: ```offsetIncrease += 0```
              offset
          }
          updatedOffsets += tp -> maxOffset
        }
      }
      (offsetIncrease, CommitOffsets(offsets = updatedOffsets.toMap))
    }

    def keepPartitions(tps: Set[TopicPartition]): CommitOffsets =
      CommitOffsets(offsets.filter { case (tp, _) => tps.contains(tp) })

    def contains(tp: TopicPartition, offset: Long): Boolean =
      offsets.get(tp).exists(_ >= offset)

    def get(tp: TopicPartition): Option[Long] = offsets.get(tp)
  }

  private[internal] object CommitOffsets {
    val empty: CommitOffsets = CommitOffsets(Map.empty)
  }

}
