package zio.kafka.consumer.metrics

import zio._

/**
 * Implementations of this trait are responsible for observing consumer metrics and collect the measurements. The
 * different methods are invoked from different places in the consumer.
 *
 * These methods hold up the consumer; they should return quickly or else throughput of the consumer will be affected.
 *
 * Users can provide a custom implementation via [[zio.kafka.consumer.ConsumerSettings.withMetricsObserver]].
 *
 * WARNING: this is an UNSTABLE API and may change in an incompatible way without notice in any zio-kafka version.
 */
trait ConsumerMetricsObserver {
  def observePoll(resumedCount: Int, pausedCount: Int, latency: Duration, pollSize: Int): UIO[Unit]
  def observeCommit(latency: Duration): UIO[Unit]
  def observeAggregatedCommit(latency: Duration, commitSize: Long): UIO[Unit]
  def observeRebalance(currentlyAssignedCount: Int, assignedCount: Int, revokedCount: Int, lostCount: Int): UIO[Unit]
  def observeRunloopMetrics(state: ConsumerMetricsObserver.ConsumerState): UIO[Unit]
  def observePollAuthError: UIO[Unit]
}

object ConsumerMetricsObserver {

  /**
   * A metrics observer that does nothing.
   */
  object NoOp extends ConsumerMetricsObserver {
    override def observePoll(
      resumedCount: Int,
      pausedCount: Int,
      latency: zio.Duration,
      pollSize: Int
    ): UIO[Unit] = ZIO.unit
    override def observeCommit(latency: zio.Duration): UIO[Unit]                             = ZIO.unit
    override def observeAggregatedCommit(latency: zio.Duration, commitSize: Long): UIO[Unit] = ZIO.unit
    override def observeRebalance(
      currentlyAssignedCount: Int,
      assignedCount: Int,
      revokedCount: Int,
      lostCount: Int
    ): UIO[Unit] = ZIO.unit
    override def observeRunloopMetrics(state: ConsumerState): UIO[Unit] = ZIO.unit
    override def observePollAuthError: UIO[Unit]                        = ZIO.unit
  }

  final case class ConsumerState(
    pendingRequestCount: Int,
    assignedPartitionCount: Int,
    perPartitionQueueSizes: Chunk[Int],
    perPartitionOutstandingPolls: Chunk[Int],
    isSubscribed: Boolean,
    commandQueueSize: Int,
    commitQueueSize: Int,
    pendingCommitCount: Int
  ) {
    val totalQueueSize: Int = perPartitionQueueSizes.sum
  }

}
