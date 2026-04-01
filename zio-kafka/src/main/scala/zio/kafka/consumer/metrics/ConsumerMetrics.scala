package zio.kafka.consumer.metrics

import zio.{ Chunk, Duration }
import zio.metrics.MetricKeyType
import zio.metrics.MetricKeyType.Histogram

/**
 * A description of all metrics that can be observed by the zio-kafka consumer.
 *
 * This class serves two purposes:
 *   - change specific histogram boundaries, metric name or description by providing a subclass of this class
 *     (with some overrides) to the default [[ZioMetricsConsumerMetricsObserver]],
 *   - as a repository for the default metric names/descriptions/boundaries for alternative metrics observers
 *     implemented with Micrometer, Dropwizard, etc.
 *
 * WARNING: this is an UNSTABLE API and may change in an incompatible way without notice in any zio-kafka version.
 */
class ConsumerMetrics {
  import ConsumerMetrics._

  // -----------------------------------------------------
  //
  // Poll metrics
  //

  // 0.01,0.03,0.08,0.21,0.55,1.49,4.04,10.97,29.81,81.04 in seconds
  // 10,30,80,210,550,1490,4040,10970,29810,81040 in milliseconds
  def pollLatencyBoundaries: Histogram.Boundaries =
    MetricKeyType.Histogram.Boundaries.fromChunk(
      Chunk.iterate(0.01, 10)(_ * Math.E).map(d => Math.ceil(d * 100.0) / 100.0)
    )

  // 1,3,8,21,55,149,404,1097,2981,8104
  def pollSizeBoundaries: Histogram.Boundaries =
    MetricKeyType.Histogram.Boundaries.fromChunk(Chunk.iterate(1.0, 10)(_ * Math.E).map(Math.ceil))

  def pollCounter: CounterInfo =
    CounterInfo("ziokafka_consumer_polls", "The number of polls.")

  def partitionsResumedInLatestPollGauge: GaugeInfo[Int] = GaugeInfo[Int](
    "ziokafka_consumer_partitions_resumed_in_latest_poll",
    "The number of partitions resumed in the latest poll call."
  )

  def partitionsPausedInLatestPollGauge: GaugeInfo[Int] = GaugeInfo[Int](
    "ziokafka_consumer_partitions_paused_in_latest_poll",
    "The number of partitions paused in the latest poll call (because of backpressure)."
  )

  def pollLatencyHistogram: HistogramInfo[Duration] = HistogramInfo[Duration](
    "ziokafka_consumer_poll_latency_seconds",
    "The duration of a single poll in seconds.",
    pollLatencyBoundaries
  )

  def pollSizeHistogram: HistogramInfo[Int] = HistogramInfo[Int](
    "ziokafka_consumer_poll_size",
    "The number of records fetched by a single poll.",
    pollSizeBoundaries
  )

  // -----------------------------------------------------
  //
  // Commit metrics
  //

  // 0.01,0.03,0.08,0.21,0.55,1.49,4.04,10.97,29.81,81.04 in seconds
  // 10,30,80,210,550,1490,4040,10970,29810,81040 in milliseconds
  def commitLatencyBoundaries: Histogram.Boundaries =
    MetricKeyType.Histogram.Boundaries.fromChunk(
      Chunk.iterate(0.01, 10)(_ * Math.E).map(d => Math.ceil(d * 100.0) / 100.0)
    )

  def commitCounter: CounterInfo =
    CounterInfo("ziokafka_consumer_commits", "The number of commits.")

  def commitLatencyHistogram: HistogramInfo[Duration] = HistogramInfo[Duration](
    "ziokafka_consumer_commit_latency_seconds",
    "The duration of a commit in seconds.",
    commitLatencyBoundaries
  )

  // -----------------------------------------------------
  //
  // Aggregated commit metrics
  //
  // Each runloop cycle zio-kafka aggregates all commit requests into a single aggregated commit.
  //

  // 0.01,0.03,0.08,0.21,0.55,1.49,4.04,10.97,29.81,81.04 in seconds
  // 10,30,80,210,550,1490,4040,10970,29810,81040 in milliseconds
  def aggregatedCommitLatencyBoundaries: Histogram.Boundaries =
    MetricKeyType.Histogram.Boundaries.fromChunk(
      Chunk.iterate(0.01, 10)(_ * Math.E).map(d => Math.ceil(d * 100.0) / 100.0)
    )

  // 1,3,8,21,55,149,404,1097,2981,8104
  def aggregatedCommitSizeBoundaries: Histogram.Boundaries =
    MetricKeyType.Histogram.Boundaries.fromChunk(Chunk.iterate(1.0, 10)(_ * Math.E).map(Math.ceil))

  def aggregatedCommitCounter: CounterInfo =
    CounterInfo("ziokafka_consumer_aggregated_commits", "The number of aggregated commits.")

  def aggregatedCommitLatencyHistogram: HistogramInfo[Duration] = HistogramInfo[Duration](
    "ziokafka_consumer_aggregated_commit_latency_seconds",
    "The duration of an aggregated commit in seconds.",
    aggregatedCommitLatencyBoundaries
  )

  /** Note: the metric is an approximation because the first commit to a partition is not included. */
  def aggregatedCommitSizeHistogram: HistogramInfo[Long] = HistogramInfo[Long](
    "ziokafka_consumer_aggregated_commit_size",
    "An approximation of the number of records (offsets) per aggregated commit.",
    aggregatedCommitSizeBoundaries
  )

  // -----------------------------------------------------
  //
  // Rebalance metrics
  //

  def rebalanceCounter: CounterInfo =
    CounterInfo("ziokafka_consumer_rebalances", "The number of rebalances.")

  def partitionsCurrentlyAssignedGauge: GaugeInfo[Int] = GaugeInfo[Int](
    "ziokafka_consumer_partitions_currently_assigned",
    "The number of partitions currently assigned to the consumer."
  )

  protected def partitionsToStateCounter(state: String): CounterInfo = CounterInfo(
    s"ziokafka_consumer_partitions_$state",
    s"The number of partitions $state to the consumer."
  )

  def partitionsAssignedCounter: CounterInfo = partitionsToStateCounter("assigned")
  def partitionsRevokedCounter: CounterInfo  = partitionsToStateCounter("revoked")
  def partitionsLostCounter: CounterInfo     = partitionsToStateCounter("lost")

  // -----------------------------------------------------
  //
  // Runloop metrics
  //

  // 0,1,3,8,21,55,149,404,1097,2981
  def streamCountBoundaries: Histogram.Boundaries =
    MetricKeyType.Histogram.Boundaries.fromChunk(Chunk(0.0) ++ Chunk.iterate(1.0, 9)(_ * Math.E).map(Math.ceil))

  // 0,100,272,739,2009,5460,14842,40343,109664,298096
  def streamSizeBoundaries: Histogram.Boundaries =
    MetricKeyType.Histogram.Boundaries.fromChunk(Chunk(0.0) ++ Chunk.iterate(100.0, 9)(_ * Math.E).map(Math.ceil))

  def queuePollSizeBoundaries: Histogram.Boundaries =
    MetricKeyType.Histogram.Boundaries.fromChunk(Chunk[Double](0, 1, 2, 3, 4, 5, 6, 7, 8, 9))

  def pendingRequestsHistogram: HistogramInfo[Int] = HistogramInfo[Int](
    "ziokafka_consumer_pending_requests",
    "The number of partitions that ran out of records (the queue is empty).",
    streamCountBoundaries
  )

  def pendingCommitsHistogram: HistogramInfo[Int] = HistogramInfo[Int](
    "ziokafka_consumer_pending_commits",
    "The number of commits that are awaiting completion.",
    streamCountBoundaries
  )

  def queueSizeHistogram: HistogramInfo[Int] = HistogramInfo[Int](
    "ziokafka_consumer_queue_size",
    "The number of records queued for a partition.",
    streamSizeBoundaries
  )

  def queuePollsHistogram: HistogramInfo[Int] = HistogramInfo[Int](
    "ziokafka_consumer_queue_polls",
    "The number of polls during which records are idling in a queue.",
    queuePollSizeBoundaries
  )

  def allQueueSizeHistogram: HistogramInfo[Int] = HistogramInfo[Int](
    "ziokafka_consumer_all_queue_size",
    "The total number of records queued for all partitions.",
    streamSizeBoundaries
  )

  def subscriptionStateGauge: GaugeInfo[Int] = GaugeInfo[Int](
    "ziokafka_consumer_subscription_state",
    "Whether the consumer is subscribed (1) or not (0)."
  )

  // 0,1,3,8,21,55,149,404,1097,2981
  def commandAndCommitQueueSizeBoundaries: Histogram.Boundaries =
    MetricKeyType.Histogram.Boundaries.fromChunk(Chunk(0.0) ++ Chunk.iterate(1.0, 9)(_ * Math.E).map(Math.ceil))

  def commandQueueSizeHistogram: HistogramInfo[Int] = HistogramInfo[Int](
    "ziokafka_consumer_command_queue_size",
    "The number of commands queued in the consumer.",
    commandAndCommitQueueSizeBoundaries
  )

  def commitQueueSizeHistogram: HistogramInfo[Int] = HistogramInfo[Int](
    "ziokafka_consumer_commit_queue_size",
    "The number of commits queued in the consumer.",
    commandAndCommitQueueSizeBoundaries
  )

  // -----------------------------------------------------
  //
  // Poll auth error metrics
  //

  def pollAuthErrorCounter: CounterInfo = CounterInfo(
    "ziokafka_consumer_poll_auth_errors",
    "The number of polls that ended with an authentication or authorization error."
  )

}

object ConsumerMetrics {

  final case class CounterInfo(name: String, description: String)
  final case class GaugeInfo[A](name: String, description: String)
  final case class HistogramInfo[A](name: String, description: String, boundaries: Histogram.Boundaries)

}
