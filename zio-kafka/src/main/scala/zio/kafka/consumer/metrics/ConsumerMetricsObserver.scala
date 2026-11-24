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

  /**
   * Called after every poll of the Kafka consumer.
   *
   * @param resumedCount
   *   the number of partitions that were resumed (allowed to fetch) for this poll
   * @param pausedCount
   *   the number of partitions that were paused for this poll
   * @param latency
   *   the duration of the poll
   * @param pollSize
   *   the number of records that the poll returned
   */
  def observePoll(resumedCount: Int, pausedCount: Int, latency: Duration, pollSize: Int): UIO[Unit]

  /**
   * Called when a commit requested by the user (for example with `Offset#commit`) completed successfully. It is not
   * called for a commit that fails or times out.
   *
   * @param latency
   *   the time between the request and the completion of the commit
   */
  def observeCommit(latency: Duration): UIO[Unit]

  /**
   * Called when a commit to the Kafka broker completed successfully. The consumer combines the commits that are waiting
   * into one aggregated commit.
   *
   * @param latency
   *   the duration of the aggregated commit
   * @param commitSize
   *   an estimate of the total increase of the committed offsets. The first commit to a partition by this consumer adds
   *   0.
   */
  def observeAggregatedCommit(latency: Duration, commitSize: Long): UIO[Unit]

  /**
   * Called when a poll changed the partition assignment.
   *
   * @param currentlyAssignedCount
   *   the number of partitions assigned to the consumer after the change
   * @param assignedCount
   *   the number of newly assigned partitions
   * @param revokedCount
   *   the number of revoked partitions
   * @param lostCount
   *   the number of lost partitions
   */
  def observeRebalance(currentlyAssignedCount: Int, assignedCount: Int, revokedCount: Int, lostCount: Int): UIO[Unit]

  /**
   * Called repeatedly, on the schedule set with [[zio.kafka.consumer.ConsumerSettings.withRunloopMetricsSchedule]],
   * with a snapshot of the state of the consumer.
   */
  def observeRunloopMetrics(state: ConsumerMetricsObserver.ConsumerState): UIO[Unit]

  /**
   * Called every time a poll fails with an `org.apache.kafka.common.errors.AuthorizationException` or an
   * `org.apache.kafka.common.errors.AuthenticationException`. The poll is then retried according to
   * [[zio.kafka.consumer.ConsumerSettings.withAuthErrorRetrySchedule]].
   */
  def observePollAuthError: UIO[Unit]
}

/** Implementations of [[ConsumerMetricsObserver]]. */
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

  /**
   * A snapshot of the state of the consumer.
   *
   * @param pendingRequestCount
   *   the number of partitions that ran out of records and requested more
   * @param assignedPartitionCount
   *   the number of partitions with a stream
   * @param perPartitionQueueSizes
   *   for each partition stream, the number of records that are queued
   * @param perPartitionOutstandingPolls
   *   for each partition stream, the number of polls during which its queued records were not pulled
   * @param isSubscribed
   *   whether the consumer is subscribed
   * @param commandQueueSize
   *   the number of commands that are queued in the consumer
   * @param commitQueueSize
   *   the number of commits that are queued in the consumer
   * @param pendingCommitCount
   *   the number of commits that are awaiting completion
   */
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

    /** The total number of records that are queued for all partitions. */
    val totalQueueSize: Int = perPartitionQueueSizes.sum
  }

}
