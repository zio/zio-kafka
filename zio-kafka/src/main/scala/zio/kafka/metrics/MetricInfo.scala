package zio.kafka.metrics

/**
 * Shared metric descriptor types used by consumer and producer metrics.
 *
 * WARNING: this is an UNSTABLE API and may change in an incompatible way without notice in any zio-kafka version.
 */
sealed trait MetricInfo {
  def name: String
  def description: String
}

object MetricInfo {
  final case class Counter(name: String, description: String)  extends MetricInfo
  final case class Gauge[A](name: String, description: String) extends MetricInfo
  // Full path avoids shadowing zio.metrics.MetricKeyType.Histogram inside this object
  final case class Histogram[A](
    name: String,
    description: String,
    boundaries: zio.metrics.MetricKeyType.Histogram.Boundaries
  ) extends MetricInfo
}
