package zio.kafka.producer.internal

import zio.metrics.MetricKeyType.Histogram
import zio.metrics._
import zio._

/**
 * Implementations of this trait are responsible for measuring all Producer metrics. The different methods are invoked
 * from different places in the Producer.
 *
 * WARNING: This is an INTERNAL API and may change in an incompatible way, or disappear, without notice, in any
 * zio-kafka version.
 */
private[producer] trait ProducerMetrics {
  def observeProduce(latency: Duration, batchSize: Int): UIO[Unit]
  def observeSendQueueSize(size: Int): UIO[Unit]
  def observeSendQueueTake(latency: Duration): UIO[Unit]
  def observeSendAuthError(errorCount: Int): UIO[Unit]
}

/**
 * A `ProducerMetrics` that uses zio-metrics for measuring.
 *
 * Sub-classes are allowed to override the Histogram boundaries.
 *
 * WARNING: This is an INTERNAL API and may change in an incompatible way, or disappear, without notice, in any
 * zio-kafka version.
 *
 * @param metricLabels
 *   the metric labels that are added to each metric
 */
//noinspection ScalaWeakerAccess
private[producer] class ZioProducerMetrics(metricLabels: Set[MetricLabel]) extends ProducerMetrics {

  // -----------------------------------------------------
  //
  // Produce metrics
  //

  // 0.01,0.03,0.08,0.21,0.55,1.49,4.04,10.97,29.81,81.04 in seconds
  // 10,30,80,210,550,1490,4040,10970,29810,81040 in milliseconds
  protected val produceLatencyBoundaries: Histogram.Boundaries =
    MetricKeyType.Histogram.Boundaries.fromChunk(
      Chunk.iterate(0.01, 10)(_ * Math.E).map(d => Math.ceil(d * 100.0) / 100.0)
    )

  // 1,3,8,21,55,149,404,1097,2981,8104
  protected val batchSizeBoundaries: Histogram.Boundaries =
    MetricKeyType.Histogram.Boundaries.fromChunk(Chunk.iterate(1.0, 10)(_ * Math.E).map(Math.ceil))

  private val produceCounter: Metric.Counter[Long] =
    Metric
      .counter("ziokafka_producer_calls", "The number of times a produce method is invoked.")
      .tagged(metricLabels)

  private val produceLatencyHistogram: Metric.Histogram[Duration] =
    Metric
      .histogram(
        "ziokafka_producer_latency_seconds",
        "The duration of a single produce, from after serialization to acknowledged, in seconds.",
        produceLatencyBoundaries
      )
      .contramap[Duration](_.toNanos.toDouble / 1e9)
      .tagged(metricLabels)

  private val produceRecordCounter: Metric.Counter[Long] =
    Metric
      .counter("ziokafka_producer_records", "The number of records produced.")
      .tagged(metricLabels)

  private val produceBatchSizeHistogram: Metric.Histogram[Int] =
    Metric
      .histogram(
        "ziokafka_producer_batch_size",
        "The number of records per produce call.",
        batchSizeBoundaries
      )
      .contramap[Int](_.toDouble)
      .tagged(metricLabels)

  override def observeProduce(latency: Duration, batchSize: Int): UIO[Unit] =
    for {
      _ <- produceCounter.increment
      _ <- produceLatencyHistogram.update(latency)
      _ <- produceRecordCounter.incrementBy(batchSize.toLong)
      _ <- produceBatchSizeHistogram.update(batchSize)
    } yield ()

  // -----------------------------------------------------
  //
  // Queue metrics
  //

  // 0,10,28,74,201,546,1485,4035,10967,29810
  protected val sendQueueSizeBoundaries: Histogram.Boundaries =
    MetricKeyType.Histogram.Boundaries.fromChunk(Chunk(0.0) ++ Chunk.iterate(10.0, 9)(_ * Math.E).map(Math.ceil))

  // 0.001, 0.003, 0.008, 0.021, 0.055, 0.149, 0.404, 1.097, 2.981, 8.104 in seconds
  // 1, 3, 8, 21, 55, 149, 404, 1097, 2981, 8104 in milliseconds
  protected val queueLatencyBoundaries: Histogram.Boundaries =
    MetricKeyType.Histogram.Boundaries.fromChunk(
      Chunk.iterate(0.001, 10)(_ * Math.E).map(d => Math.ceil(d * 1000.0) / 1000.0)
    )

  private val sendQueueSizeHistogram: Metric.Histogram[Int] =
    Metric
      .histogram(
        "ziokafka_producer_send_queue_size",
        "The number of records in the zio-kafka send queue.",
        sendQueueSizeBoundaries
      )
      .contramap[Int](_.toDouble)
      .tagged(metricLabels)

  private val sendQueueLatencyHistogram: Metric.Histogram[Duration] =
    Metric
      .histogram(
        "ziokafka_producer_send_queue_latency_seconds",
        "Time in send queue, including waiting for capacity, in seconds.",
        queueLatencyBoundaries
      )
      .contramap[Duration](_.toNanos.toDouble / 1e9)
      .tagged(metricLabels)

  override def observeSendQueueSize(size: Int): UIO[Unit] =
    sendQueueSizeHistogram.update(size)

  override def observeSendQueueTake(latency: Duration): UIO[Unit] =
    sendQueueLatencyHistogram.update(latency)

  // -----------------------------------------------------
  //
  // Auth error metrics
  //

  private val sendAuthErrorCounter: Metric.Counter[Int] =
    Metric
      .counterInt(
        "ziokafka_producer_auth_errors",
        "The number of record sends that resulted in an authentication or authorization error."
      )
      .tagged(metricLabels)

  override def observeSendAuthError(errorCount: Int): UIO[Unit] =
    sendAuthErrorCounter.incrementBy(errorCount)

}
