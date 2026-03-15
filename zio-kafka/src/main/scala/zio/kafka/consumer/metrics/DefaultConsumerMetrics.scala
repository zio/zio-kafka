package zio.kafka.consumer.metrics

import zio._
import zio.metrics.MetricKeyType.Histogram
import zio.metrics._

/**
 * A [[ConsumerMetrics]] implementation that uses zio-metrics for measuring.
 *
 * @param metricLabels
 *   the metric labels that are added to each metric
 */
//noinspection ScalaWeakerAccess
private[consumer] class DefaultConsumerMetrics(metricLabels: Set[MetricLabel]) extends ConsumerMetrics {

  // -----------------------------------------------------
  //
  // Poll metrics
  //

  // 0.01,0.03,0.08,0.21,0.55,1.49,4.04,10.97,29.81,81.04 in seconds
  // 10,30,80,210,550,1490,4040,10970,29810,81040 in milliseconds
  protected val pollLatencyBoundaries: Histogram.Boundaries =
    MetricKeyType.Histogram.Boundaries.fromChunk(
      Chunk.iterate(0.01, 10)(_ * Math.E).map(d => Math.ceil(d * 100.0) / 100.0)
    )

  // 1,3,8,21,55,149,404,1097,2981,8104
  protected val pollSizeBoundaries: Histogram.Boundaries =
    MetricKeyType.Histogram.Boundaries.fromChunk(Chunk.iterate(1.0, 10)(_ * Math.E).map(Math.ceil))

  private val pollCounter: Metric.Counter[Int] =
    Metric
      .counterInt("ziokafka_consumer_polls", "The number of polls.")
      .tagged(metricLabels)

  private val partitionsResumedInLatestPollGauge: Metric.Gauge[Int] =
    Metric
      .gauge(
        "ziokafka_consumer_partitions_resumed_in_latest_poll",
        "The number of partitions resumed in the latest poll call."
      )
      .contramap[Int](_.toDouble)
      .tagged(metricLabels)

  private val partitionsPausedInLatestPollGauge: Metric.Gauge[Int] =
    Metric
      .gauge(
        "ziokafka_consumer_partitions_paused_in_latest_poll",
        "The number of partitions paused in the latest poll call (because of backpressure)."
      )
      .contramap[Int](_.toDouble)
      .tagged(metricLabels)

  private val pollLatencyHistogram: Metric.Histogram[Duration] =
    Metric
      .histogram(
        "ziokafka_consumer_poll_latency_seconds",
        "The duration of a single poll in seconds.",
        pollLatencyBoundaries
      )
      .contramap[Duration](_.toNanos.toDouble / 1e9)
      .tagged(metricLabels)

  private val pollSizeHistogram: Metric.Histogram[Int] =
    Metric
      .histogram(
        "ziokafka_consumer_poll_size",
        "The number of records fetched by a single poll.",
        pollSizeBoundaries
      )
      .contramap[Int](_.toDouble)
      .tagged(metricLabels)

  override def observePoll(resumedCount: Int, pausedCount: Int, latency: Duration, pollSize: Int): UIO[Unit] =
    for {
      _ <- pollCounter.increment
      _ <- partitionsResumedInLatestPollGauge.update(resumedCount)
      _ <- partitionsPausedInLatestPollGauge.update(pausedCount)
      _ <- pollLatencyHistogram.update(latency)
      _ <- pollSizeHistogram.update(pollSize)
    } yield ()

  // -----------------------------------------------------
  //
  // Commit metrics
  //

  // 0.01,0.03,0.08,0.21,0.55,1.49,4.04,10.97,29.81,81.04 in seconds
  // 10,30,80,210,550,1490,4040,10970,29810,81040 in milliseconds
  protected val commitLatencyBoundaries: Histogram.Boundaries =
    MetricKeyType.Histogram.Boundaries.fromChunk(
      Chunk.iterate(0.01, 10)(_ * Math.E).map(d => Math.ceil(d * 100.0) / 100.0)
    )

  private val commitCounter: Metric.Counter[Int] =
    Metric
      .counterInt("ziokafka_consumer_commits", "The number of commits.")
      .tagged(metricLabels)

  private val commitLatencyHistogram: Metric.Histogram[Duration] =
    Metric
      .histogram(
        "ziokafka_consumer_commit_latency_seconds",
        "The duration of a commit in seconds.",
        commitLatencyBoundaries
      )
      .contramap[Duration](_.toNanos.toDouble / 1e9)
      .tagged(metricLabels)

  override def observeCommit(latency: zio.Duration): UIO[Unit] =
    for {
      _ <- commitCounter.increment
      _ <- commitLatencyHistogram.update(latency)
    } yield ()

  // -----------------------------------------------------
  //
  // Aggregated commit metrics
  //
  // Each runloop cycle zio-kafka aggregates all commit requests into a single aggregated commit.
  //

  // 0.01,0.03,0.08,0.21,0.55,1.49,4.04,10.97,29.81,81.04 in seconds
  // 10,30,80,210,550,1490,4040,10970,29810,81040 in milliseconds
  protected val aggregatedCommitLatencyBoundaries: Histogram.Boundaries =
    MetricKeyType.Histogram.Boundaries.fromChunk(
      Chunk.iterate(0.01, 10)(_ * Math.E).map(d => Math.ceil(d * 100.0) / 100.0)
    )

  // 1,3,8,21,55,149,404,1097,2981,8104
  protected val aggregatedCommitSizeBoundaries: Histogram.Boundaries =
    MetricKeyType.Histogram.Boundaries.fromChunk(Chunk.iterate(1.0, 10)(_ * Math.E).map(Math.ceil))

  private val aggregatedCommitCounter: Metric.Counter[Int] =
    Metric
      .counterInt("ziokafka_consumer_aggregated_commits", "The number of aggregated commits.")
      .tagged(metricLabels)

  private val aggregatedCommitLatencyHistogram: Metric.Histogram[Duration] =
    Metric
      .histogram(
        "ziokafka_consumer_aggregated_commit_latency_seconds",
        "The duration of an aggregated commit in seconds.",
        aggregatedCommitLatencyBoundaries
      )
      .contramap[Duration](_.toNanos.toDouble / 1e9)
      .tagged(metricLabels)

  // Note: the metric is an approximation because the first commit to a partition is not included.
  private val aggregatedCommitSizeHistogram: Metric.Histogram[Long] =
    Metric
      .histogram(
        "ziokafka_consumer_aggregated_commit_size",
        "An approximation of the number of records (offsets) per aggregated commit.",
        aggregatedCommitSizeBoundaries
      )
      .contramap[Long](_.toDouble)
      .tagged(metricLabels)

  override def observeAggregatedCommit(latency: Duration, commitSize: Long): UIO[Unit] =
    for {
      _ <- aggregatedCommitCounter.increment
      _ <- aggregatedCommitLatencyHistogram.update(latency)
      _ <- aggregatedCommitSizeHistogram.update(commitSize)
    } yield ()

  // -----------------------------------------------------
  //
  // Rebalance metrics
  //

  private val rebalanceCounter: Metric.Counter[Int] =
    Metric
      .counterInt("ziokafka_consumer_rebalances", "The number of rebalances.")
      .tagged(metricLabels)

  private val partitionsCurrentlyAssignedGauge: Metric.Gauge[Int] =
    Metric
      .gauge(
        "ziokafka_consumer_partitions_currently_assigned",
        "The number of partitions currently assigned to the consumer."
      )
      .contramap[Int](_.toDouble)
      .tagged(metricLabels)

  private def partitionsToStateCounter(state: String): Metric.Counter[Int] =
    Metric
      .counterInt(
        s"ziokafka_consumer_partitions_$state",
        s"The number of partitions $state to the consumer."
      )
      .tagged(metricLabels)

  private val partitionsAssignedCounter = partitionsToStateCounter("assigned")
  private val partitionsRevokedCounter  = partitionsToStateCounter("revoked")
  private val partitionsLostCounter     = partitionsToStateCounter("lost")

  override def observeRebalance(
    currentlyAssignedCount: Int,
    assignedCount: Int,
    revokedCount: Int,
    lostCount: Int
  ): UIO[Unit] =
    for {
      _ <- rebalanceCounter.increment
      _ <- partitionsCurrentlyAssignedGauge.update(currentlyAssignedCount)
      _ <- partitionsAssignedCounter.incrementBy(assignedCount)
      _ <- partitionsRevokedCounter.incrementBy(revokedCount)
      _ <- partitionsLostCounter.incrementBy(lostCount)
    } yield ()

  // -----------------------------------------------------
  //
  // Runloop metrics
  //

  // 0,1,3,8,21,55,149,404,1097,2981
  protected val streamCountBoundaries: Histogram.Boundaries =
    MetricKeyType.Histogram.Boundaries.fromChunk(Chunk(0.0) ++ Chunk.iterate(1.0, 9)(_ * Math.E).map(Math.ceil))

  // 0,100,272,739,2009,5460,14842,40343,109664,298096
  protected val streamSizeBoundaries: Histogram.Boundaries =
    MetricKeyType.Histogram.Boundaries.fromChunk(Chunk(0.0) ++ Chunk.iterate(100.0, 9)(_ * Math.E).map(Math.ceil))

  protected val queuePollSizeBoundaries: Histogram.Boundaries =
    MetricKeyType.Histogram.Boundaries.fromChunk(Chunk[Double](0, 1, 2, 3, 4, 5, 6, 7, 8, 9))

  private val pendingRequestsHistogram: Metric.Histogram[Int] =
    Metric
      .histogram(
        "ziokafka_consumer_pending_requests",
        "The number of partitions that ran out of records (the queue is empty).",
        streamCountBoundaries
      )
      .contramap[Int](_.toDouble)
      .tagged(metricLabels)

  private val pendingCommitsHistogram: Metric.Histogram[Int] =
    Metric
      .histogram(
        "ziokafka_consumer_pending_commits",
        "The number of commits that are awaiting completion.",
        streamCountBoundaries
      )
      .contramap[Int](_.toDouble)
      .tagged(metricLabels)

  private val queueSizeHistogram: Metric.Histogram[Int] =
    Metric
      .histogram(
        "ziokafka_consumer_queue_size",
        "The number of records queued for a partition.",
        streamSizeBoundaries
      )
      .contramap[Int](_.toDouble)
      .tagged(metricLabels)

  private val queuePollsHistogram: Metric.Histogram[Int] =
    Metric
      .histogram(
        "ziokafka_consumer_queue_polls",
        "The number of polls during which records are idling in a queue.",
        queuePollSizeBoundaries
      )
      .contramap[Int](_.toDouble)
      .tagged(metricLabels)

  private val allQueueSizeHistogram: Metric.Histogram[Int] =
    Metric
      .histogram(
        "ziokafka_consumer_all_queue_size",
        "The total number of records queued for all partitions.",
        streamSizeBoundaries
      )
      .contramap[Int](_.toDouble)
      .tagged(metricLabels)

  private val subscriptionStateGauge: Metric.Gauge[Double] =
    Metric
      .gauge(
        "ziokafka_consumer_subscription_state",
        "Whether the consumer is subscribed (1) or not (0)."
      )
      .tagged(metricLabels)

  // 0,1,3,8,21,55,149,404,1097,2981
  protected val commandAndCommitQueueSizeBoundaries: Histogram.Boundaries =
    MetricKeyType.Histogram.Boundaries.fromChunk(Chunk(0.0) ++ Chunk.iterate(1.0, 9)(_ * Math.E).map(Math.ceil))

  private val commandQueueSizeHistogram: Metric.Histogram[Int] =
    Metric
      .histogram(
        "ziokafka_consumer_command_queue_size",
        "The number of commands queued in the consumer.",
        commandAndCommitQueueSizeBoundaries
      )
      .contramap[Int](_.toDouble)
      .tagged(metricLabels)

  private val commitQueueSizeHistogram: Metric.Histogram[Int] =
    Metric
      .histogram(
        "ziokafka_consumer_commit_queue_size",
        "The number of commits queued in the consumer.",
        commandAndCommitQueueSizeBoundaries
      )
      .contramap[Int](_.toDouble)
      .tagged(metricLabels)

  override def observeRunloopMetrics(state: ConsumerMetrics.ConsumerState): UIO[Unit] =
    for {
      _ <- ZIO.foreachDiscard(state.perPartitionOutstandingPolls)(queuePollsHistogram.update(_))
      _ <- ZIO.foreachDiscard(state.perPartitionQueueSizes)(qs => queueSizeHistogram.update(qs))
      _ <- allQueueSizeHistogram.update(state.totalQueueSize)
      _ <- pendingRequestsHistogram.update(state.pendingRequestCount)
      _ <- pendingCommitsHistogram.update(state.pendingCommitCount)
      _ <- subscriptionStateGauge.set(if (state.isSubscribed) 1.0 else 0.0)
      _ <- commandQueueSizeHistogram.update(state.commandQueueSize)
      _ <- commitQueueSizeHistogram.update(state.commitQueueSize)
    } yield ()

  // -----------------------------------------------------
  //
  // Poll auth error metrics
  //

  private val pollAuthErrorCounter: Metric.Counter[Int] =
    Metric
      .counterInt(
        "ziokafka_consumer_poll_auth_errors",
        "The number of polls that ended with an authentication or authorization error."
      )
      .tagged(metricLabels)

  def observePollAuthError(): UIO[Unit] =
    pollAuthErrorCounter.increment

}
