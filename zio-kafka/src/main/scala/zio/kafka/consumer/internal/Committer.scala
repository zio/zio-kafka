package zio.kafka.consumer.internal

import org.apache.kafka.clients.consumer.{ OffsetAndMetadata, OffsetCommitCallback }
import org.apache.kafka.common.TopicPartition
import zio.kafka.consumer.internal.LiveCommitter.CommitOffsets
import zio.{ Task, UIO }

import java.util.{ Map => JavaMap }

trait Committer {
  val commit: Map[TopicPartition, OffsetAndMetadata] => Task[Unit]

  /**
   * Takes commits from the queue, commits them and adds them to pending commits
   *
   * If the queue is empty, nothing happens, unless executeOnEmpty is true.
   *
   * @param consumer
   *   Consumer with exclusive access
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
