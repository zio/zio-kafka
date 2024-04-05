package zio.kafka.consumer
import zio.UIO

/**
 * Allows graceful shutdown of a stream, where no more records are being fetched but the in-flight records can continue
 * to be processed and their offsets committed.
 *
 * As long as this object is in scope, the Kafka consumer remains subscribed.
 */
private[consumer] trait SubscriptionStreamControl {

  /**
   * Stop fetching records for the subscribed topic-partitions and end the associated streams, while allowing commits to
   * proceed (consumer remains subscribed)
   */
  def stop: UIO[Unit]
}

private[consumer] object SubscriptionStreamControl {
  def apply(stop0: UIO[Unit]): SubscriptionStreamControl =
    new SubscriptionStreamControl {
      override def stop: UIO[Unit] = stop0
    }
}
