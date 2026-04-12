package zio.kafka.producer.metrics

import zio.{ Chunk, Duration }
import zio.metrics.MetricKeyType
import zio.metrics.MetricKeyType.Histogram
import zio.kafka.metrics.MetricInfo

/**
 * A description of all metrics that can be observed by the zio-kafka producer.
 *
 * The purpose of this class is to be a repository of metrics names/descriptions/boundaries, which can optionally be
 * updated by the user.
 *
 * Instances of this class can be provided to implementations of [[ProducerMetricsObserver]]. This can be either the
 * built-in [[ZioMetricsProducerMetricsObserver]] or an alternative metrics observer implemented with Micrometer,
 * Dropwizard, etc.
 *
 * Activate a custom [[ProducerMetricsObserver]] with [[zio.kafka.producer.ProducerSettings.withMetricsObserver]].
 *
 * WARNING: this is an UNSTABLE API and may change in an incompatible way without notice in any zio-kafka version.
 */
final case class ProducerMetrics(
  // -----------------------------------------------------
  //
  // Produce metrics
  //

  produceCallsCounter: MetricInfo.Counter = MetricInfo.Counter(
    "ziokafka_producer_calls",
    "The number of times a produce method is invoked."
  ),
  produceLatencyHistogram: MetricInfo.Histogram[Duration] = MetricInfo.Histogram[Duration](
    "ziokafka_producer_latency_seconds",
    "The duration of a single produce, from after serialization to acknowledged, in seconds.",
    ProducerMetrics.defaultProduceLatencyBoundaries
  ),
  produceRecordsCounter: MetricInfo.Counter = MetricInfo.Counter(
    "ziokafka_producer_records",
    "The number of records produced."
  ),
  produceBatchSizeHistogram: MetricInfo.Histogram[Int] = MetricInfo.Histogram[Int](
    "ziokafka_producer_batch_size",
    "The number of records per produce call.",
    ProducerMetrics.defaultBatchSizeBoundaries
  ),

  // -----------------------------------------------------
  //
  // Queue metrics
  //

  sendQueueSizeHistogram: MetricInfo.Histogram[Int] = MetricInfo.Histogram[Int](
    "ziokafka_producer_send_queue_size",
    "The number of records in the zio-kafka send queue.",
    ProducerMetrics.defaultSendQueueSizeBoundaries
  ),
  sendQueueLatencyHistogram: MetricInfo.Histogram[Duration] = MetricInfo.Histogram[Duration](
    "ziokafka_producer_send_queue_latency_seconds",
    "Time in send queue, including waiting for capacity, in seconds.",
    ProducerMetrics.defaultQueueLatencyBoundaries
  ),

  // -----------------------------------------------------
  //
  // Auth error metrics
  //

  sendAuthErrorCounter: MetricInfo.Counter = MetricInfo.Counter(
    "ziokafka_producer_auth_errors",
    "The number of record sends that resulted in an authentication or authorization error."
  )
)

//noinspection ScalaWeakerAccess
object ProducerMetrics {

  // ----- Default boundaries -----

  // 0.01,0.03,0.08,0.21,0.55,1.49,4.04,10.97,29.81,81.04 in seconds
  // 10,30,80,210,550,1490,4040,10970,29810,81040 in milliseconds
  def defaultProduceLatencyBoundaries: Histogram.Boundaries =
    MetricKeyType.Histogram.Boundaries.fromChunk(
      Chunk.iterate(0.01, 10)(_ * Math.E).map(d => Math.ceil(d * 100.0) / 100.0)
    )

  // 1,3,8,21,55,149,404,1097,2981,8104
  def defaultBatchSizeBoundaries: Histogram.Boundaries =
    MetricKeyType.Histogram.Boundaries.fromChunk(Chunk.iterate(1.0, 10)(_ * Math.E).map(Math.ceil))

  // 0,10,28,74,201,546,1485,4035,10967,29810
  def defaultSendQueueSizeBoundaries: Histogram.Boundaries =
    MetricKeyType.Histogram.Boundaries.fromChunk(Chunk(0.0) ++ Chunk.iterate(10.0, 9)(_ * Math.E).map(Math.ceil))

  // 0.001,0.003,0.008,0.021,0.055,0.149,0.404,1.097,2.981,8.104 in seconds
  // 1,3,8,21,55,149,404,1097,2981,8104 in milliseconds
  def defaultQueueLatencyBoundaries: Histogram.Boundaries =
    MetricKeyType.Histogram.Boundaries.fromChunk(
      Chunk.iterate(0.001, 10)(_ * Math.E).map(d => Math.ceil(d * 1000.0) / 1000.0)
    )

}
