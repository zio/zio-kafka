package zio.kafka.client

import zio.test.mock.Method
import zio.test.mock.Mockable
import zio.test.mock.Mock

import org.apache.kafka.clients.consumer.{ OffsetAndMetadata, OffsetAndTimestamp }
import org.apache.kafka.common.{ PartitionInfo, TopicPartition }
import zio._
import zio.blocking.Blocking
import zio.clock.Clock
import zio.duration._
import zio.kafka.client.serde.Deserializer
import zio.stream._

trait ConsumerMock extends Consumer {
  val consumer: ConsumerMock.Service
}

object ConsumerMock {
  trait Service extends Consumer.Service

  object assignment       extends Method[ConsumerMock, Unit, Set[TopicPartition]]
  object beginningOffsets extends Method[ConsumerMock, (Set[TopicPartition], Duration), Map[TopicPartition, Long]]
  object committed
      extends Method[ConsumerMock, (Set[TopicPartition], Duration), Map[TopicPartition, Option[OffsetAndMetadata]]]
  object endOffsets      extends Method[ConsumerMock, (Set[TopicPartition], Duration), Map[TopicPartition, Long]]
  object stopConsumption extends Method[ConsumerMock, Unit, Unit]
  object listTopics      extends Method[ConsumerMock, Duration, Map[String, List[PartitionInfo]]]
  object offsetsForTimes
      extends Method[ConsumerMock, (Map[TopicPartition, Long], Duration), Map[TopicPartition, OffsetAndTimestamp]]
  object partitionsFor   extends Method[ConsumerMock, (String, Duration), List[PartitionInfo]]
  object position        extends Method[ConsumerMock, (TopicPartition, Duration), Long]
  object seek            extends Method[ConsumerMock, (TopicPartition, Long), Unit]
  object seekToBeginning extends Method[ConsumerMock, Set[TopicPartition], Unit]
  object seekToEnd       extends Method[ConsumerMock, Set[TopicPartition], Unit]
  object subscribe       extends Method[ConsumerMock, Subscription, Unit]
  object subscription    extends Method[ConsumerMock, Unit, Set[String]]
  object unsubscribe     extends Method[ConsumerMock, Unit, Unit]

  implicit val mockable: Mockable[ConsumerMock] = (mock: Mock) =>
    new ConsumerMock {
      val consumer = new Service {
        def assignment: BlockingTask[Set[TopicPartition]] = mock(ConsumerMock.assignment)
        def beginningOffsets(
          partitions: Set[TopicPartition],
          timeout: Duration
        ): BlockingTask[Map[TopicPartition, Long]] = mock(ConsumerMock.beginningOffsets, partitions, timeout)
        def committed(
          partitions: Set[TopicPartition],
          timeout: Duration
        ): BlockingTask[Map[TopicPartition, Option[OffsetAndMetadata]]] =
          mock(ConsumerMock.committed, partitions, timeout)
        def endOffsets(
          partitions: Set[TopicPartition],
          timeout: Duration
        ): BlockingTask[Map[TopicPartition, Long]] = mock(ConsumerMock.endOffsets, partitions, timeout)
        def stopConsumption: UIO[Unit]             = mock(ConsumerMock.stopConsumption)
        def listTopics(timeout: Duration): BlockingTask[Map[String, List[PartitionInfo]]] =
          mock(ConsumerMock.listTopics, timeout)
        def offsetsForTimes(
          timestamps: Map[TopicPartition, Long],
          timeout: Duration
        ): BlockingTask[Map[TopicPartition, OffsetAndTimestamp]] =
          mock(ConsumerMock.offsetsForTimes, timestamps, timeout)
        def partitionedStream[R, K, V](
          keyDeserializer: Deserializer[R, K],
          valueDeserializer: Deserializer[R, V]
        ): ZStream[
          Clock with Blocking,
          Throwable,
          (TopicPartition, ZStreamChunk[R, Throwable, CommittableRecord[K, V]])
        ] = ???
        def partitionsFor(topic: String, timeout: Duration): BlockingTask[List[PartitionInfo]] =
          mock(ConsumerMock.partitionsFor, topic, timeout)
        def position(partition: TopicPartition, timeout: Duration): BlockingTask[Long] =
          mock(ConsumerMock.position, partition, timeout)
        def plainStream[R, K, V](
          keyDeserializer: Deserializer[R, K],
          valueDeserializer: Deserializer[R, V]
        ): ZStreamChunk[R with Clock with Blocking, Throwable, CommittableRecord[K, V]] = ???
        def seek(partition: TopicPartition, offset: Long): BlockingTask[Unit] =
          mock(ConsumerMock.seek, partition, offset)
        def seekToBeginning(partitions: Set[TopicPartition]): BlockingTask[Unit] =
          mock(ConsumerMock.seekToBeginning, partitions)
        def seekToEnd(partitions: Set[TopicPartition]): BlockingTask[Unit] = mock(ConsumerMock.seekToEnd, partitions)
        def subscribe(subscription: Subscription): BlockingTask[Unit]      = mock(ConsumerMock.subscribe, subscription)
        def subscribeAnd(subscription: Subscription): SubscribedConsumer   = ???
        def subscription(): BlockingTask[Set[String]]                      = mock(ConsumerMock.subscription)
        def unsubscribe(): BlockingTask[Unit]                              = mock(ConsumerMock.unsubscribe)
      }
    }
}
