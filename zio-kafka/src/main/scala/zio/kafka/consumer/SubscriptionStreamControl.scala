package zio.kafka.consumer
import zio.UIO
import zio.stream.ZStream

/**
 * Allows graceful shutdown of a stream, where no more records are being fetched but the in-flight records can continue
 * to be processed and their offsets committed.
 *
 * @param stream
 *   The stream of partitions / records for this subscription
 * @param stop
 *   Stop fetching records for the subscribed topic-partitions and end the associated streams, while allowing commits to
 *   proceed (consumer remains subscribed)
 */
final private[consumer] case class SubscriptionStreamControl[S <: ZStream[_, _, _]](stream: S, stop: UIO[Unit])
