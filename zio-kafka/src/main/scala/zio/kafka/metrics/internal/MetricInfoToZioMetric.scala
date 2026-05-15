package zio.kafka.metrics.internal

import zio.Duration
import zio.kafka.metrics.MetricInfo
import zio.metrics.{ Metric, MetricLabel }

/**
 * Convert a [[MetricInfo]] to the appropriate ZIO metric with `metricInfo.toZioMetric`.
 *
 * Note: this is an internal API.
 */
object MetricInfoToZioMetric {

  implicit final class CounterInfoToMetric(private val counterInfo: MetricInfo.Counter) extends AnyVal {
    def toZioMetric(metricLabels: Set[MetricLabel]): Metric.Counter[Long] =
      Metric
        .counter(counterInfo.name, counterInfo.description)
        .tagged(metricLabels)
  }

  implicit final class GaugeIntInfoToMetric(private val gaugeInfo: MetricInfo.Gauge[Int]) extends AnyVal {
    def toZioMetric(metricLabels: Set[MetricLabel]): Metric.Gauge[Int] =
      Metric
        .gauge(gaugeInfo.name, gaugeInfo.description)
        .contramap[Int](_.toDouble)
        .tagged(metricLabels)
  }

  /** Converts duration to seconds. */
  implicit final class HistogramDurationInfoToMetric(private val histogramInfo: MetricInfo.Histogram[Duration])
      extends AnyVal {
    def toZioMetric(metricLabels: Set[MetricLabel]): Metric.Histogram[Duration] =
      Metric
        .histogram(histogramInfo.name, histogramInfo.description, histogramInfo.boundaries)
        .contramap[Duration](_.toNanos.toDouble / 1e9)
        .tagged(metricLabels)
  }

  implicit final class HistogramIntInfoToMetric(private val histogramInfo: MetricInfo.Histogram[Int]) extends AnyVal {
    def toZioMetric(metricLabels: Set[MetricLabel]): Metric.Histogram[Int] =
      Metric
        .histogram(histogramInfo.name, histogramInfo.description, histogramInfo.boundaries)
        .contramap[Int](_.toDouble)
        .tagged(metricLabels)
  }

  implicit final class HistogramLongInfoToMetric(private val histogramInfo: MetricInfo.Histogram[Long]) extends AnyVal {
    def toZioMetric(metricLabels: Set[MetricLabel]): Metric.Histogram[Long] =
      Metric
        .histogram(histogramInfo.name, histogramInfo.description, histogramInfo.boundaries)
        .contramap[Long](_.toDouble)
        .tagged(metricLabels)
  }

}
