package zio.kafka.consumer.fetch

import org.apache.kafka.common.TopicPartition
import zio.kafka.ZIOSpecDefaultSlf4j
import zio.kafka.consumer.internal.PartitionStream
import zio.test.{ assertTrue, Spec, TestEnvironment }
import zio._

object QueueSizeBasedFetchStrategySpec extends ZIOSpecDefaultSlf4j {

  private val partitionPreFetchBufferLimit = 50
  private val fetchStrategy                = QueueSizeBasedFetchStrategy(partitionPreFetchBufferLimit)

  private val tp10 = new TopicPartition("topic1", 0)
  private val tp11 = new TopicPartition("topic1", 1)
  private val tp20 = new TopicPartition("topic2", 0)

  override def spec: Spec[TestEnvironment with Scope, Any] =
    suite("QueueSizeBasedFetchStrategySpec")(
      test("stream with queue size above limit is paused") {
        val streams = Chunk(newStream(tp10, currentQueueSize = partitionPreFetchBufferLimit + 1))
        for {
          result <- fetchStrategy.selectPartitionsToFetch(streams)
        } yield assertTrue(result.isEmpty)
      },
      test("stream with queue size equal to limit may resume") {
        val streams = Chunk(newStream(tp10, currentQueueSize = partitionPreFetchBufferLimit))
        for {
          result <- fetchStrategy.selectPartitionsToFetch(streams)
        } yield assertTrue(result == Set(tp10))
      },
      test("stream with queue size below limit may resume") {
        val streams = Chunk(newStream(tp10, currentQueueSize = partitionPreFetchBufferLimit - 1))
        for {
          result <- fetchStrategy.selectPartitionsToFetch(streams)
        } yield assertTrue(result == Set(tp10))
      },
      test("some streams may resume") {
        val streams = Chunk(
          newStream(tp10, currentQueueSize = 10),
          newStream(tp11, currentQueueSize = partitionPreFetchBufferLimit - 1),
          newStream(tp20, currentQueueSize = 100)
        )
        for {
          result <- fetchStrategy.selectPartitionsToFetch(streams)
        } yield assertTrue(result == Set(tp10, tp11))
      }
    )

  private def newStream(topicPartition: TopicPartition, currentQueueSize: Int): PartitionStream =
    new PartitionStream {
      override def tp: TopicPartition                         = topicPartition
      override def queueSize(implicit trace: Trace): UIO[Int] = ZIO.succeed(currentQueueSize)
    }
}
