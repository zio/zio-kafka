package zio.kafka.consumer

import org.apache.kafka.common.TopicPartition
import zio.kafka.serde.Deserializer
import zio.stream.{ Stream, ZStream }
import zio.{ Chunk, RIO, Task }

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
      .fromZIO(underlying)
      .flatMap(
        _.partitionedAssignmentStream(keyDeserializer, valueDeserializer)
      )

  def partitionedStream[R, K, V](keyDeserializer: Deserializer[R, K], valueDeserializer: Deserializer[R, V]): Stream[
    Throwable,
    (TopicPartition, ZStream[R, Throwable, CommittableRecord[K, V]])
  ] =
    ZStream.fromZIO(underlying).flatMap(_.partitionedStream(keyDeserializer, valueDeserializer))

  def plainStream[R, K, V](
    keyDeserializer: Deserializer[R, K],
    valueDeserializer: Deserializer[R, V],
    bufferSize: Int = 4
  ): ZStream[R, Throwable, CommittableRecord[K, V]] =
    partitionedStream(keyDeserializer, valueDeserializer).flatMapPar(n = Int.MaxValue, bufferSize = bufferSize)(
      _._2
    )
}

class SubscribedConsumerFromEnvironment(
  private val underlying: RIO[Consumer, Consumer]
) {

  def partitionedAssignmentStream[R, K, V](
    keyDeserializer: Deserializer[R, K],
    valueDeserializer: Deserializer[R, V]
  ): ZStream[Consumer, Throwable, Chunk[
    (TopicPartition, ZStream[R, Throwable, CommittableRecord[K, V]])
  ]] =
    ZStream
      .fromZIO(underlying)
      .flatMap(
        _.partitionedAssignmentStream(keyDeserializer, valueDeserializer)
      )

  def partitionedStream[R, K, V](keyDeserializer: Deserializer[R, K], valueDeserializer: Deserializer[R, V]): ZStream[
    Consumer,
    Throwable,
    (TopicPartition, ZStream[R, Throwable, CommittableRecord[K, V]])
  ] =
    ZStream.fromZIO(underlying).flatMap(_.partitionedStream(keyDeserializer, valueDeserializer))

  def plainStream[R, K, V](
    keyDeserializer: Deserializer[R, K],
    valueDeserializer: Deserializer[R, V],
    bufferSize: Int = 4
  ): ZStream[R with Consumer, Throwable, CommittableRecord[K, V]] =
    partitionedStream(keyDeserializer, valueDeserializer).flatMapPar(n = Int.MaxValue, bufferSize = bufferSize)(
      _._2
    )
}
