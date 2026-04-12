package zio.kafka.producer.metrics

import zio._
import zio.kafka.producer.metrics.ProducerMetrics.{ CounterInfo, HistogramInfo }
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
  import ZioMetricsProducerMetricsObserver._

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

  private val produceCallsCounter: Metric.Counter[Int] =
    producerMetrics.produceCallsCounter.toZioMetric(metricLabels)

  private val produceLatencyHistogram: Metric.Histogram[Duration] =
    producerMetrics.produceLatencyHistogram.toZioMetric(metricLabels)

  private val produceRecordsCounter: Metric.Counter[Int] =
    producerMetrics.produceRecordsCounter.toZioMetric(metricLabels)

  private val produceBatchSizeHistogram: Metric.Histogram[Int] =
    producerMetrics.produceBatchSizeHistogram.toZioMetric(metricLabels)

  override def observeProduce(latency: Duration, batchSize: Int): UIO[Unit] =
    for {
      _ <- produceCallsCounter.increment
      _ <- produceLatencyHistogram.update(latency)
      _ <- produceRecordsCounter.incrementBy(batchSize)
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

  private val sendAuthErrorCounter: Metric.Counter[Int] =
    producerMetrics.sendAuthErrorCounter.toZioMetric(metricLabels)

  override def observeSendAuthError(errorCount: Int): UIO[Unit] =
    sendAuthErrorCounter.incrementBy(errorCount)

}

object ZioMetricsProducerMetricsObserver {

  private implicit final class CounterInfoToMetric(private val counterInfo: CounterInfo) extends AnyVal {
    def toZioMetric(metricLabels: Set[MetricLabel]): Metric.Counter[Int] =
      Metric
        .counterInt(counterInfo.name, counterInfo.description)
        .tagged(metricLabels)
  }

  private implicit final class HistogramDurationInfoToMetric(private val histogramInfo: HistogramInfo[Duration])
      extends AnyVal {
    def toZioMetric(metricLabels: Set[MetricLabel]): Metric.Histogram[Duration] =
      Metric
        .histogram(histogramInfo.name, histogramInfo.description, histogramInfo.boundaries)
        .contramap[Duration](_.toNanos.toDouble / 1e9)
        .tagged(metricLabels)
  }

  private implicit final class HistogramIntInfoToMetric(private val histogramInfo: HistogramInfo[Int]) extends AnyVal {
    def toZioMetric(metricLabels: Set[MetricLabel]): Metric.Histogram[Int] =
      Metric
        .histogram(histogramInfo.name, histogramInfo.description, histogramInfo.boundaries)
        .contramap[Int](_.toDouble)
        .tagged(metricLabels)
  }

}
