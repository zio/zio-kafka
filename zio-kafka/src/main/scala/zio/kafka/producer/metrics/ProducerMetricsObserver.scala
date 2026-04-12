package zio.kafka.producer.metrics

import zio._

/**
 * Implementations of this trait are responsible for observing producer metrics and collect the measurements. The
 * different methods are invoked from different places in the producer.
 *
 * These methods hold up the producer; they should return quickly or else throughput of the producer will be affected.
 *
 * Users can provide a custom implementation via [[zio.kafka.producer.ProducerSettings.withMetricsObserver]].
 *
 * WARNING: this is an UNSTABLE API and may change in an incompatible way without notice in any zio-kafka version.
 */
trait ProducerMetricsObserver {
  def observeProduce(latency: Duration, batchSize: Int): UIO[Unit]
  def observeSendQueueSize(size: Int): UIO[Unit]
  def observeSendQueueTake(latency: Duration): UIO[Unit]
  def observeSendAuthError(errorCount: Int): UIO[Unit]
}

object ProducerMetricsObserver {

  /**
   * A metrics observer that does nothing.
   */
  object NoOp extends ProducerMetricsObserver {
    override def observeProduce(latency: Duration, batchSize: Int): UIO[Unit] = ZIO.unit
    override def observeSendQueueSize(size: Int): UIO[Unit]                   = ZIO.unit
    override def observeSendQueueTake(latency: Duration): UIO[Unit]           = ZIO.unit
    override def observeSendAuthError(errorCount: Int): UIO[Unit]             = ZIO.unit
  }

}
