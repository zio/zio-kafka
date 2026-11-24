package zio.kafka.metrics

/**
 * A metric descriptor.
 *
 * Should provide enough information to allow constructing a metric in the used metrics library (e.g. zio-metrics).
 *
 * WARNING: this is an UNSTABLE API and may change in an incompatible way without notice in any zio-kafka version.
 */
sealed trait MetricInfo {

  /** The name of the metric. */
  def name: String

  /** A description of the metric. */
  def description: String
}

/**
 * The kinds of metric descriptors.
 */
object MetricInfo {

  /** A descriptor of a counter metric. */
  final case class Counter(name: String, description: String) extends MetricInfo

  /** A descriptor of a gauge metric that holds values of type `A`. */
  final case class Gauge[A](name: String, description: String) extends MetricInfo

  // Full path avoids shadowing zio.metrics.MetricKeyType.Histogram inside this object
  /**
   * A descriptor of a histogram metric that observes values of type `A`.
   *
   * @param boundaries
   *   the boundaries of the histogram's buckets
   */
  final case class Histogram[A](
    name: String,
    description: String,
    boundaries: zio.metrics.MetricKeyType.Histogram.Boundaries
  ) extends MetricInfo
}
