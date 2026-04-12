package zio.kafka.consumer.metrics

import zio.{ Chunk, Duration }
import zio.metrics.MetricKeyType
import zio.metrics.MetricKeyType.Histogram
import zio.kafka.consumer.metrics.ConsumerMetrics._
import zio.kafka.metrics.MetricInfo

/**
 * A description of all metrics that can be observed by the zio-kafka consumer.
 *
 * The purpose of this class is to be a repository of metrics names/descriptions/boundaries, which can optionally be
 * updated by the user.
 *
 * Instances of this class can be provided to implementations of [[ConsumerMetricsObserver]]. This can be either the
 * built-in [[ZioMetricsConsumerMetricsObserver]] or an alternative metrics observer implemented with Micrometer,
 * Dropwizard, etc.
 *
 * Activate a custom [[ConsumerMetricsObserver]] with [[ConsumerSettings.withMetricsObserver]].
 *
 * WARNING: this is an UNSTABLE API and may change in an incompatible way without notice in any zio-kafka version.
 */
final case class ConsumerMetrics(
  // -----------------------------------------------------
  //
  // Poll metrics
  //

  pollCounter: MetricInfo.Counter = MetricInfo.Counter("ziokafka_consumer_polls", "The number of polls."),
  partitionsResumedInLatestPollGauge: MetricInfo.Gauge[Int] = MetricInfo.Gauge[Int](
    "ziokafka_consumer_partitions_resumed_in_latest_poll",
    "The number of partitions resumed in the latest poll call."
  ),
  partitionsPausedInLatestPollGauge: MetricInfo.Gauge[Int] = MetricInfo.Gauge[Int](
    "ziokafka_consumer_partitions_paused_in_latest_poll",
    "The number of partitions paused in the latest poll call (because of backpressure)."
  ),
  pollLatencyHistogram: MetricInfo.Histogram[Duration] = MetricInfo.Histogram[Duration](
    "ziokafka_consumer_poll_latency_seconds",
    "The duration of a single poll in seconds.",
    defaultLatencyBoundaries
  ),
  pollSizeHistogram: MetricInfo.Histogram[Int] = MetricInfo.Histogram[Int](
    "ziokafka_consumer_poll_size",
    "The number of records fetched by a single poll.",
    defaultCountBoundaries
  ),

  // -----------------------------------------------------
  //
  // Commit metrics
  //

  commitCounter: MetricInfo.Counter = MetricInfo.Counter("ziokafka_consumer_commits", "The number of commits."),
  commitLatencyHistogram: MetricInfo.Histogram[Duration] = MetricInfo.Histogram[Duration](
    "ziokafka_consumer_commit_latency_seconds",
    "The duration of a commit in seconds.",
    defaultLatencyBoundaries
  ),

  // -----------------------------------------------------
  //
  // Aggregated commit metrics
  //
  // Each runloop cycle zio-kafka aggregates all commit requests into a single aggregated commit.
  //

  aggregatedCommitCounter: MetricInfo.Counter =
    MetricInfo.Counter("ziokafka_consumer_aggregated_commits", "The number of aggregated commits."),
  aggregatedCommitLatencyHistogram: MetricInfo.Histogram[Duration] = MetricInfo.Histogram[Duration](
    "ziokafka_consumer_aggregated_commit_latency_seconds",
    "The duration of an aggregated commit in seconds.",
    defaultLatencyBoundaries
  ),
  // Note: the metric is an approximation because the first commit to a partition is not included.
  aggregatedCommitSizeHistogram: MetricInfo.Histogram[Long] = MetricInfo.Histogram[Long](
    "ziokafka_consumer_aggregated_commit_size",
    "An approximation of the number of records (offsets) per aggregated commit.",
    defaultCountBoundaries
  ),

  // -----------------------------------------------------
  //
  // Rebalance metrics
  //

  rebalanceCounter: MetricInfo.Counter =
    MetricInfo.Counter("ziokafka_consumer_rebalances", "The number of rebalances."),
  partitionsCurrentlyAssignedGauge: MetricInfo.Gauge[Int] = MetricInfo.Gauge[Int](
    "ziokafka_consumer_partitions_currently_assigned",
    "The number of partitions currently assigned to the consumer."
  ),
  partitionsAssignedCounter: MetricInfo.Counter = partitionsToStateCounter("assigned"),
  partitionsRevokedCounter: MetricInfo.Counter = partitionsToStateCounter("revoked"),
  partitionsLostCounter: MetricInfo.Counter = partitionsToStateCounter("lost"),

  // -----------------------------------------------------
  //
  // Runloop metrics
  //

  pendingRequestsHistogram: MetricInfo.Histogram[Int] = MetricInfo.Histogram[Int](
    "ziokafka_consumer_pending_requests",
    "The number of partitions that ran out of records (the queue is empty).",
    defaultStreamCountBoundaries
  ),
  pendingCommitsHistogram: MetricInfo.Histogram[Int] = MetricInfo.Histogram[Int](
    "ziokafka_consumer_pending_commits",
    "The number of commits that are awaiting completion.",
    defaultStreamCountBoundaries
  ),
  queueSizeHistogram: MetricInfo.Histogram[Int] = MetricInfo.Histogram[Int](
    "ziokafka_consumer_queue_size",
    "The number of records queued for a partition.",
    defaultStreamSizeBoundaries
  ),
  queuePollsHistogram: MetricInfo.Histogram[Int] = MetricInfo.Histogram[Int](
    "ziokafka_consumer_queue_polls",
    "The number of polls during which records are idling in a queue.",
    defaultQueuePollSizeBoundaries
  ),
  allQueueSizeHistogram: MetricInfo.Histogram[Int] = MetricInfo.Histogram[Int](
    "ziokafka_consumer_all_queue_size",
    "The total number of records queued for all partitions.",
    defaultStreamSizeBoundaries
  ),
  subscriptionStateGauge: MetricInfo.Gauge[Int] = MetricInfo.Gauge[Int](
    "ziokafka_consumer_subscription_state",
    "Whether the consumer is subscribed (1) or not (0)."
  ),
  commandQueueSizeHistogram: MetricInfo.Histogram[Int] = MetricInfo.Histogram[Int](
    "ziokafka_consumer_command_queue_size",
    "The number of commands queued in the consumer.",
    defaultCommandAndCommitQueueSizeBoundaries
  ),
  commitQueueSizeHistogram: MetricInfo.Histogram[Int] = MetricInfo.Histogram[Int](
    "ziokafka_consumer_commit_queue_size",
    "The number of commits queued in the consumer.",
    defaultCommandAndCommitQueueSizeBoundaries
  ),

  // -----------------------------------------------------
  //
  // Poll auth error metrics
  //

  pollAuthErrorCounter: MetricInfo.Counter = MetricInfo.Counter(
    "ziokafka_consumer_poll_auth_errors",
    "The number of polls that ended with an authentication or authorization error."
  )
)

//noinspection ScalaWeakerAccess
object ConsumerMetrics {

  // ----- Default boundaries -----

  // 0.01,0.03,0.08,0.21,0.55,1.49,4.04,10.97,29.81,81.04 in seconds
  // 10,30,80,210,550,1490,4040,10970,29810,81040 in milliseconds
  def defaultLatencyBoundaries: Histogram.Boundaries =
    MetricKeyType.Histogram.Boundaries.fromChunk(
      Chunk.iterate(0.01, 10)(_ * Math.E).map(d => Math.ceil(d * 100.0) / 100.0)
    )

  // 1,3,8,21,55,149,404,1097,2981,8104
  def defaultCountBoundaries: Histogram.Boundaries =
    MetricKeyType.Histogram.Boundaries.fromChunk(Chunk.iterate(1.0, 10)(_ * Math.E).map(Math.ceil))

  // 0,1,3,8,21,55,149,404,1097,2981
  def defaultStreamCountBoundaries: Histogram.Boundaries =
    MetricKeyType.Histogram.Boundaries.fromChunk(Chunk(0.0) ++ Chunk.iterate(1.0, 9)(_ * Math.E).map(Math.ceil))

  // 0,100,272,739,2009,5460,14842,40343,109664,298096
  def defaultStreamSizeBoundaries: Histogram.Boundaries =
    MetricKeyType.Histogram.Boundaries.fromChunk(Chunk(0.0) ++ Chunk.iterate(100.0, 9)(_ * Math.E).map(Math.ceil))

  // 0,1,2,3,4,5,6,7,8,9
  def defaultQueuePollSizeBoundaries: Histogram.Boundaries =
    MetricKeyType.Histogram.Boundaries.fromChunk(Chunk[Double](0, 1, 2, 3, 4, 5, 6, 7, 8, 9))

  // 0,1,3,8,21,55,149,404,1097,2981
  def defaultCommandAndCommitQueueSizeBoundaries: Histogram.Boundaries =
    defaultStreamCountBoundaries

  private def partitionsToStateCounter(state: String): MetricInfo.Counter = MetricInfo.Counter(
    s"ziokafka_consumer_partitions_$state",
    s"The number of partitions $state to the consumer."
  )

}
