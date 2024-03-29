package zio.kafka.consumer
import zio.UIO

/**
 * Allows graceful shutdown of a stream, where no more records are being fetched but the in-flight records can continue
 * to be processed and their offsets committed.
 *
 * As long as this object is in scope, the Kafka consumer remains subscribed.
 * @tparam StreamType
 *   Type of the stream returned from [[stream]]
 */
trait SubscriptionStreamControl[StreamType] {

  /**
   * The stream of partitions / records for this subscription
   */
  def stream: StreamType

  /**
   * Stop fetching records for the subscribed topic-partitions and end the associated streams, while allowing commits to
   * proceed (consumer remains subscribed)
   */
  def stop: UIO[Unit]

}
object SubscriptionStreamControl {
  def apply[T](stream0: T, stop0: UIO[Unit]): SubscriptionStreamControl[T] = new SubscriptionStreamControl[T] {
    override def stream: T       = stream0
    override def stop: UIO[Unit] = stop0
  }
}
