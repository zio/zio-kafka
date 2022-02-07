package zio.kafka.consumer

import org.apache.kafka.common.TopicPartition
import zio.{ Chunk, Has, RIO, Task }
import zio.stream.{ Stream, ZStream }
import zio.kafka.serde.Deserializer

class SubscribedConsumer(
  private val underlying: Task[Consumer]
) {

  def partitionedAssignmentStream[R, K, V](
    keyDeserializer: Deserializer[R, K],
    valueDeserializer: Deserializer[R, V]
  ): Stream[Throwable, Chunk[
    (TopicPartition, ZStream[R, Throwable, CommittableRecord[K, V]])
  ]] =
    ZStream
      .fromEffect(underlying)
      .flatMap(
        _.partitionedAssignmentStream(keyDeserializer, valueDeserializer)
      )

  def partitionedStream[R, K, V](keyDeserializer: Deserializer[R, K], valueDeserializer: Deserializer[R, V]): Stream[
    Throwable,
    (TopicPartition, ZStream[R, Throwable, CommittableRecord[K, V]])
  ] =
    ZStream.fromEffect(underlying).flatMap(_.partitionedStream(keyDeserializer, valueDeserializer))

  def plainStream[R, K, V](
    keyDeserializer: Deserializer[R, K],
    valueDeserializer: Deserializer[R, V],
    outputBuffer: Int = 4
  ): ZStream[R, Throwable, CommittableRecord[K, V]] =
    partitionedStream(keyDeserializer, valueDeserializer).flatMapPar(n = Int.MaxValue, outputBuffer = outputBuffer)(
      _._2
    )
}

class SubscribedConsumerFromEnvironment(
  private val underlying: RIO[Has[Consumer], Consumer]
) {

  def partitionedStream[R, K, V](keyDeserializer: Deserializer[R, K], valueDeserializer: Deserializer[R, V]): ZStream[
    Has[Consumer],
    Throwable,
    (TopicPartition, ZStream[R, Throwable, CommittableRecord[K, V]])
  ] =
    ZStream.fromEffect(underlying).flatMap(_.partitionedStream(keyDeserializer, valueDeserializer))

  def plainStream[R, K, V](
    keyDeserializer: Deserializer[R, K],
    valueDeserializer: Deserializer[R, V],
    outputBuffer: Int = 4
  ): ZStream[R with Has[Consumer], Throwable, CommittableRecord[K, V]] =
    partitionedStream(keyDeserializer, valueDeserializer).flatMapPar(n = Int.MaxValue, outputBuffer = outputBuffer)(
      _._2
    )
}
