package zio.kafka.consumer
import zio.UIO
import zio.stream.ZStream

/**
 * Allows graceful shutdown of a stream, where no more records are being fetched but the in-flight records can continue
 * to be processed and their offsets committed.
 *
 * As long as this object is in scope, the Kafka consumer remains subscribed.
 *
 * @tparam S
 *   Type of the stream returned from [[stream]]
 */
trait SubscriptionStreamControl[S <: ZStream[_, _, _]] {

  /**
   * The stream of partitions / records for this subscription
   */
  def stream: S

  /**
   * Stop fetching records for the subscribed topic-partitions and end the associated streams, while allowing commits to
   * proceed (consumer remains subscribed)
   */
  def stop: UIO[Unit]
}

object SubscriptionStreamControl {
  def apply[S <: ZStream[_, _, _]](stream0: S, stop0: UIO[Unit]): SubscriptionStreamControl[S] =
    new SubscriptionStreamControl[S] {
      override def stream: S       = stream0
      override def stop: UIO[Unit] = stop0
    }
}
