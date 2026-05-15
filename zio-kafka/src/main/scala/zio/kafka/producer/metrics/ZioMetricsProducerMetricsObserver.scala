package zio.kafka.producer.metrics

import zio._
import zio.kafka.metrics.internal.MetricInfoToZioMetric._
import zio.metrics._

/**
 * A [[ProducerMetricsObserver]] implementation that uses zio-metrics to collect observations.
 *
 * @param producerMetrics
 *   the names, descriptions and histogram boundaries of all the metrics
 * @param metricLabels
 *   the metric labels that are added to each metric
 */
//noinspection ScalaWeakerAccess
final class ZioMetricsProducerMetricsObserver(
  producerMetrics: ProducerMetrics,
  metricLabels: Set[MetricLabel]
) extends ProducerMetricsObserver {

  /**
   * A [[ProducerMetricsObserver]] implementation that uses zio-metrics to collect observations, using the default
   * metric names, descriptions and histogram boundaries.
   *
   * @param metricLabels
   *   the metric labels that are added to each metric
   */
  def this(metricLabels: Set[MetricLabel]) =
    this(ProducerMetrics(), metricLabels)

  // -----------------------------------------------------
  //
  // Produce metrics
  //

  private val produceCallsCounter: Metric.Counter[Long] =
    producerMetrics.produceCallsCounter.toZioMetric(metricLabels)

  private val produceLatencyHistogram: Metric.Histogram[Duration] =
    producerMetrics.produceLatencyHistogram.toZioMetric(metricLabels)

  private val produceRecordsCounter: Metric.Counter[Long] =
    producerMetrics.produceRecordsCounter.toZioMetric(metricLabels)

  private val produceBatchSizeHistogram: Metric.Histogram[Int] =
    producerMetrics.produceBatchSizeHistogram.toZioMetric(metricLabels)

  override def observeProduce(latency: Duration, batchSize: Int): UIO[Unit] =
    for {
      _ <- produceCallsCounter.increment
      _ <- produceLatencyHistogram.update(latency)
      _ <- produceRecordsCounter.incrementBy(batchSize.toLong)
      _ <- produceBatchSizeHistogram.update(batchSize)
    } yield ()

  // -----------------------------------------------------
  //
  // Queue metrics
  //

  private val sendQueueSizeHistogram: Metric.Histogram[Int] =
    producerMetrics.sendQueueSizeHistogram.toZioMetric(metricLabels)

  private val sendQueueLatencyHistogram: Metric.Histogram[Duration] =
    producerMetrics.sendQueueLatencyHistogram.toZioMetric(metricLabels)

  override def observeSendQueueSize(size: Int): UIO[Unit] =
    sendQueueSizeHistogram.update(size)

  override def observeSendQueueTake(latency: Duration): UIO[Unit] =
    sendQueueLatencyHistogram.update(latency)

  // -----------------------------------------------------
  //
  // Auth error metrics
  //

  private val sendAuthErrorCounter: Metric.Counter[Long] =
    producerMetrics.sendAuthErrorCounter.toZioMetric(metricLabels)

  override def observeSendAuthError(errorCount: Int): UIO[Unit] =
    sendAuthErrorCounter.incrementBy(errorCount.toLong)

}
