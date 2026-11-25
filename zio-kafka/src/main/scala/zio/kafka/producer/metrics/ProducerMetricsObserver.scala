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

  /**
   * Called when a produce call succeeded and all its records are acknowledged.
   *
   * @param latency
   *   the time from enqueueing the records (after serialization) until they were acknowledged
   * @param batchSize
   *   the number of records that were produced
   */
  def observeProduce(latency: Duration, batchSize: Int): UIO[Unit]

  /**
   * Called every 10 seconds with the number of record batches that are in the zio-kafka send queue.
   */
  def observeSendQueueSize(size: Int): UIO[Unit]

  /**
   * Called when a batch of records is taken from the send queue.
   *
   * @param latency
   *   the time the batch spent in the send queue, including the time waiting for capacity in the queue
   */
  def observeSendQueueTake(latency: Duration): UIO[Unit]

  /**
   * Called when sending records failed with an `AuthorizationException` or an `AuthenticationException`. It is not
   * called for a chunk of records in which sending also failed with another kind of exception.
   *
   * @param errorCount
   *   the number of records for which sending failed with such an exception
   */
  def observeSendAuthError(errorCount: Int): UIO[Unit]
}

/**
 * Provided [[ProducerMetricsObserver]] instances.
 */
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
