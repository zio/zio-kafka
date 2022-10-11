package zio.kafka.consumer
import org.apache.kafka.common.TopicPartition
import zio.kafka.consumer.internal.Runloop.ByteArrayCommittableRecord
import zio.kafka.serde.Deserializer
import zio.stream.{ Stream, Take, ZStream }
import zio.{ Chunk, Hub, NonEmptyChunk, Ref, Scope, ZIO }

/**
 * Allows consuming from multiple topics using one Consumer
 *
 * @param consumer
 * @param subscriptions
 * @param partitionAssignments
 */
class MultiConsumer(
  consumer: Consumer,
  subscriptions: Ref.Synchronized[Set[Subscription]],
  partitionAssignments: Hub[
    Take[Throwable, Chunk[(TopicPartition, ZStream[Any, Throwable, ByteArrayCommittableRecord])]]
  ]
) {
  // If subscriptions overlap, behavior is undefined. Kafka records will probably go randomly to one of the streams
  // Will throw when unable to union subscription with other subscriptions
  def partitionedStream[R, K, V](
    subscription: Subscription,
    keyDeserializer: Deserializer[R, K],
    valueDeserializer: Deserializer[R, V]
  ): ZStream[Any, Throwable, (TopicPartition, ZStream[R, Throwable, CommittableRecord[K, V]])] =
    partitionedAssignmentStream(subscription, keyDeserializer, valueDeserializer).flattenChunks

  def plainStream[R, K, V](
    subscription: Subscription,
    keyDeserializer: Deserializer[R, K],
    valueDeserializer: Deserializer[R, V],
    bufferSize: Int
  ): ZStream[R, Throwable, CommittableRecord[K, V]] =
    partitionedStream(subscription, keyDeserializer, valueDeserializer).flatMapPar(
      n = Int.MaxValue,
      bufferSize = bufferSize
    )(_._2)

  def partitionedAssignmentStream[R, K, V](
    subscription: Subscription,
    keyDeserializer: Deserializer[R, K],
    valueDeserializer: Deserializer[R, V]
  ): Stream[Throwable, Chunk[(TopicPartition, ZStream[R, Throwable, CommittableRecord[K, V]])]] =
    ZStream.unwrapScoped {
      for {
        _ <- subscriptions.getAndUpdateZIO { existingSubscriptions =>
               val newSubscriptions = NonEmptyChunk.fromIterable(subscription, existingSubscriptions)
               ZIO
                 .fromOption(Subscription.unionAll(newSubscriptions))
                 .orElseFail(InvalidSubscriptionUnion(newSubscriptions.toSeq))
                 .flatMap { union =>
                   consumer.subscribe(union).as(newSubscriptions.toSet)
                 }
             }
        stream <- ZStream.fromHubScoped(partitionAssignments)
      } yield stream
        .map(_.exit)
        .flattenExitOption
        .flattenChunks
        .map {
          _.collect {
            case (tp, partitionStream) if Subscription.subscriptionMatches(subscription, tp) =>
              tp -> partitionStream.mapChunksZIO(_.mapZIO(_.deserializeWith(keyDeserializer, valueDeserializer)))
          }
        }
    }
}
object MultiConsumer {
  def make: ZIO[Scope with Consumer, Nothing, MultiConsumer] = for {
    consumer      <- ZIO.service[Consumer]
    subscriptions <- Ref.Synchronized.make(Set.empty[Subscription])
    partitionAssignments <-
      consumer.partitionedAssignmentStream(Deserializer.byteArray, Deserializer.byteArray).toHub(32)
  } yield new MultiConsumer(consumer, subscriptions, partitionAssignments)

}
