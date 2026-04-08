package zio.kafka.consumer.metrics

import zio._
import zio.kafka.consumer.metrics.ConsumerMetrics.{ CounterInfo, GaugeInfo, HistogramInfo }
import zio.metrics._

/**
 * A [[ConsumerMetricsObserver]] implementation that uses zio-metrics to collect observations.
 *
 * @param consumerMetrics
 *   the names, descriptions and histogram boundaries of all the metrics
 * @param metricLabels
 *   the metric labels that are added to each metric
 */
//noinspection ScalaWeakerAccess
final class ZioMetricsConsumerMetricsObserver(
  consumerMetrics: ConsumerMetrics,
  metricLabels: Set[MetricLabel]
) extends ConsumerMetricsObserver {
  import ZioMetricsConsumerMetricsObserver._

  /**
   * A [[ConsumerMetricsObserver]] implementation that uses zio-metrics to collect observations, using the default
   * metric names, descriptions and histogram boundaries.
   *
   * @param metricLabels
   *   the metric labels that are added to each metric
   */
  def this(metricLabels: Set[MetricLabel]) =
    this(ConsumerMetrics(), metricLabels)

  // -----------------------------------------------------
  //
  // Poll metrics
  //

  private val pollCounter: Metric.Counter[Int] =
    consumerMetrics.pollCounter.toZioMetric(metricLabels)

  private val partitionsResumedInLatestPollGauge: Metric.Gauge[Int] =
    consumerMetrics.partitionsResumedInLatestPollGauge.toZioMetric(metricLabels)

  private val partitionsPausedInLatestPollGauge: Metric.Gauge[Int] =
    consumerMetrics.partitionsPausedInLatestPollGauge.toZioMetric(metricLabels)

  private val pollLatencyHistogram: Metric.Histogram[Duration] =
    consumerMetrics.pollLatencyHistogram.toZioMetric(metricLabels)

  private val pollSizeHistogram: Metric.Histogram[Int] =
    consumerMetrics.pollSizeHistogram.toZioMetric(metricLabels)

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

  private val commitCounter: Metric.Counter[Int] =
    consumerMetrics.commitCounter.toZioMetric(metricLabels)

  private val commitLatencyHistogram: Metric.Histogram[Duration] =
    consumerMetrics.commitLatencyHistogram.toZioMetric(metricLabels)

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

  private val aggregatedCommitCounter: Metric.Counter[Int] =
    consumerMetrics.aggregatedCommitCounter.toZioMetric(metricLabels)

  private val aggregatedCommitLatencyHistogram: Metric.Histogram[Duration] =
    consumerMetrics.aggregatedCommitLatencyHistogram.toZioMetric(metricLabels)

  // Note: the metric is an approximation because the first commit to a partition is not included.
  private val aggregatedCommitSizeHistogram: Metric.Histogram[Long] =
    consumerMetrics.aggregatedCommitSizeHistogram.toZioMetric(metricLabels)

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
    consumerMetrics.rebalanceCounter.toZioMetric(metricLabels)

  private val partitionsCurrentlyAssignedGauge: Metric.Gauge[Int] =
    consumerMetrics.partitionsCurrentlyAssignedGauge.toZioMetric(metricLabels)

  private val partitionsAssignedCounter =
    consumerMetrics.partitionsAssignedCounter.toZioMetric(metricLabels)

  private val partitionsRevokedCounter =
    consumerMetrics.partitionsRevokedCounter.toZioMetric(metricLabels)

  private val partitionsLostCounter =
    consumerMetrics.partitionsLostCounter.toZioMetric(metricLabels)

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

  private val pendingRequestsHistogram: Metric.Histogram[Int] =
    consumerMetrics.pendingRequestsHistogram.toZioMetric(metricLabels)

  private val pendingCommitsHistogram: Metric.Histogram[Int] =
    consumerMetrics.pendingCommitsHistogram.toZioMetric(metricLabels)

  private val queueSizeHistogram: Metric.Histogram[Int] =
    consumerMetrics.queueSizeHistogram.toZioMetric(metricLabels)

  private val queuePollsHistogram: Metric.Histogram[Int] =
    consumerMetrics.queuePollsHistogram.toZioMetric(metricLabels)

  private val allQueueSizeHistogram: Metric.Histogram[Int] =
    consumerMetrics.allQueueSizeHistogram.toZioMetric(metricLabels)

  private val subscriptionStateGauge: Metric.Gauge[Int] =
    consumerMetrics.subscriptionStateGauge.toZioMetric(metricLabels)

  private val commandQueueSizeHistogram: Metric.Histogram[Int] =
    consumerMetrics.commandQueueSizeHistogram.toZioMetric(metricLabels)

  private val commitQueueSizeHistogram: Metric.Histogram[Int] =
    consumerMetrics.commitQueueSizeHistogram.toZioMetric(metricLabels)

  override def observeRunloopMetrics(state: ConsumerMetricsObserver.ConsumerState): UIO[Unit] =
    for {
      _ <- ZIO.foreachDiscard(state.perPartitionOutstandingPolls)(queuePollsHistogram.update(_))
      _ <- ZIO.foreachDiscard(state.perPartitionQueueSizes)(qs => queueSizeHistogram.update(qs))
      _ <- allQueueSizeHistogram.update(state.totalQueueSize)
      _ <- pendingRequestsHistogram.update(state.pendingRequestCount)
      _ <- pendingCommitsHistogram.update(state.pendingCommitCount)
      _ <- subscriptionStateGauge.set(if (state.isSubscribed) 1 else 0)
      _ <- commandQueueSizeHistogram.update(state.commandQueueSize)
      _ <- commitQueueSizeHistogram.update(state.commitQueueSize)
    } yield ()

  // -----------------------------------------------------
  //
  // Poll auth error metrics
  //

  private val pollAuthErrorCounter: Metric.Counter[Int] =
    consumerMetrics.pollAuthErrorCounter.toZioMetric(metricLabels)

  def observePollAuthError(): UIO[Unit] =
    pollAuthErrorCounter.increment

}

object ZioMetricsConsumerMetricsObserver {

  private implicit class CounterInfoToMetric(val counterInfo: CounterInfo) {
    def toZioMetric(metricLabels: Set[MetricLabel]): Metric.Counter[Int] =
      Metric
        .counterInt(counterInfo.name, counterInfo.description)
        .tagged(metricLabels)
  }

  private implicit class GaugeIntInfoToMetric(val gaugeInfo: GaugeInfo[Int]) {
    def toZioMetric(metricLabels: Set[MetricLabel]): Metric.Gauge[Int] =
      Metric
        .gauge(gaugeInfo.name, gaugeInfo.description)
        .contramap[Int](_.toDouble)
        .tagged(metricLabels)
  }

  private implicit class HistogramDurationInfoToMetric(val histogramInfo: HistogramInfo[Duration]) {
    def toZioMetric(metricLabels: Set[MetricLabel]): Metric.Histogram[Duration] =
      Metric
        .histogram(histogramInfo.name, histogramInfo.description, histogramInfo.boundaries)
        .contramap[Duration](_.toNanos.toDouble / 1e9)
        .tagged(metricLabels)
  }

  private implicit class HistogramIntInfoToMetric(val histogramInfo: HistogramInfo[Int]) {
    def toZioMetric(metricLabels: Set[MetricLabel]): Metric.Histogram[Int] =
      Metric
        .histogram(histogramInfo.name, histogramInfo.description, histogramInfo.boundaries)
        .contramap[Int](_.toDouble)
        .tagged(metricLabels)
  }

  private implicit class HistogramLongInfoToMetric(val histogramInfo: HistogramInfo[Long]) {
    def toZioMetric(metricLabels: Set[MetricLabel]): Metric.Histogram[Long] =
      Metric
        .histogram(histogramInfo.name, histogramInfo.description, histogramInfo.boundaries)
        .contramap[Long](_.toDouble)
        .tagged(metricLabels)
  }

}
