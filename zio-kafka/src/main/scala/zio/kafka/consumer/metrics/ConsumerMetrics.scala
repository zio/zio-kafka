package zio.kafka.consumer.metrics

import zio._

/**
 * Implementations of this trait are responsible for measuring all consumer metrics. The different methods are invoked
 * from different places in the consumer.
 *
 * Users can provide a custom implementation via [[zio.kafka.consumer.ConsumerSettings.withConsumerMetrics]].
 */
trait ConsumerMetrics {
  def observePoll(resumedCount: Int, pausedCount: Int, latency: Duration, pollSize: Int): UIO[Unit]
  def observeCommit(latency: Duration): UIO[Unit]
  def observeAggregatedCommit(latency: Duration, commitSize: Long): UIO[Unit]
  def observeRebalance(currentlyAssignedCount: Int, assignedCount: Int, revokedCount: Int, lostCount: Int): UIO[Unit]
  def observeRunloopMetrics(state: ConsumerMetrics.ConsumerState): UIO[Unit]
  def observePollAuthError(): UIO[Unit]
}

object ConsumerMetrics {

  object NoOp extends ConsumerMetrics {
    override def observePoll(
      resumedCount: RuntimeFlags,
      pausedCount: RuntimeFlags,
      latency: zio.Duration,
      pollSize: RuntimeFlags
    ): UIO[Unit] = ZIO.unit
    override def observeCommit(latency: zio.Duration): UIO[Unit]                             = ZIO.unit
    override def observeAggregatedCommit(latency: zio.Duration, commitSize: Long): UIO[Unit] = ZIO.unit
    override def observeRebalance(
      currentlyAssignedCount: RuntimeFlags,
      assignedCount: RuntimeFlags,
      revokedCount: RuntimeFlags,
      lostCount: RuntimeFlags
    ): UIO[Unit] = ZIO.unit
    override def observeRunloopMetrics(state: ConsumerState): UIO[Unit] = ZIO.unit
    override def observePollAuthError(): UIO[Unit]                      = ZIO.unit
  }

  final case class ConsumerState(
    pendingRequestCount: Int,
    assignedPartitionCount: Int,
    perPartitionQueueSizes: Chunk[Int],
    perPartitionOutstandingPolls: Chunk[Int],
    totalQueueSize: Int,
    isSubscribed: Boolean,
    commandQueueSize: Int,
    commitQueueSize: Int,
    pendingCommitCount: Int
  )
}
