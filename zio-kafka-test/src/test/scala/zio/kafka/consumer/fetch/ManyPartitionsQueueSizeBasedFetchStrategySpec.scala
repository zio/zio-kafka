package zio.kafka.consumer.fetch

import org.apache.kafka.common.TopicPartition
import zio.kafka.ZIOSpecDefaultSlf4j
import zio.kafka.consumer.internal.PartitionStream
import zio.test.{ assertTrue, Spec, TestEnvironment }
import zio.{ Chunk, Scope, UIO, ZIO }

object ManyPartitionsQueueSizeBasedFetchStrategySpec extends ZIOSpecDefaultSlf4j {

  private val maxPartitionQueueSize = 50
  private val fetchStrategy = ManyPartitionsQueueSizeBasedFetchStrategy(
    maxPartitionQueueSize,
    maxTotalQueueSize = 80
  )

  private val tp10 = new TopicPartition("topic1", 0)
  private val tp11 = new TopicPartition("topic1", 1)
  private val tp20 = new TopicPartition("topic2", 0)
  private val tp21 = new TopicPartition("topic2", 1)
  private val tp22 = new TopicPartition("topic2", 2)

  override def spec: Spec[TestEnvironment with Scope, Any] =
    suite("ManyPartitionsQueueSizeBasedFetchStrategySpec")(
      test("stream with queue size above maxSize is paused") {
        val streams = Chunk(newStream(tp10, currentQueueSize = 100))
        for {
          result <- fetchStrategy.selectPartitionsToFetch(streams)
        } yield assertTrue(result.isEmpty)
      },
      test("stream with queue size below maxSize may resume when less-equal global max") {
        val streams = Chunk(newStream(tp10, currentQueueSize = 10))
        for {
          result <- fetchStrategy.selectPartitionsToFetch(streams)
        } yield assertTrue(result == Set(tp10))
      },
      test("all streams with queue size less-equal maxSize may resume when total is less-equal global max") {
        val streams = Chunk(
          newStream(tp10, currentQueueSize = maxPartitionQueueSize),
          newStream(tp11, currentQueueSize = 10),
          newStream(tp20, currentQueueSize = 10),
          newStream(tp21, currentQueueSize = 10)
        )
        for {
          result <- fetchStrategy.selectPartitionsToFetch(streams)
        } yield assertTrue(result == Set(tp10, tp11, tp20, tp21))
      },
      test("not all streams with queue size less-equal maxSize may resume when total is less-equal global max") {
        val streams = Chunk(
          newStream(tp10, currentQueueSize = 40),
          newStream(tp11, currentQueueSize = 40),
          newStream(tp20, currentQueueSize = 40),
          newStream(tp21, currentQueueSize = 40)
        )
        for {
          result <- fetchStrategy.selectPartitionsToFetch(streams)
        } yield assertTrue(result.size == 2)
      },
      test("all streams with queue size less-equal maxSize may resume eventually") {
        val streams = Chunk(
          newStream(tp10, currentQueueSize = 60),
          newStream(tp11, currentQueueSize = 60),
          newStream(tp20, currentQueueSize = 40),
          newStream(tp21, currentQueueSize = 40),
          newStream(tp22, currentQueueSize = 40)
        )
        for {
          result1 <- fetchStrategy.selectPartitionsToFetch(streams)
          result2 <- fetchStrategy.selectPartitionsToFetch(streams)
          result3 <- fetchStrategy.selectPartitionsToFetch(streams)
          result4 <- fetchStrategy.selectPartitionsToFetch(streams)
          result5 <- fetchStrategy.selectPartitionsToFetch(streams)
          results = Chunk(result1, result2, result3, result4, result5)
        } yield assertTrue(
          // Only partitions from topic 2 are selected (since 40 <= 50):
          results.forall(_.forall(_.topic() == "topic2")),
          // 2 partitions are selected every time (since 2*40 <= 80):
          results.forall(_.size == 2),
          // All partitions from topic 2 are selected eventually:
          results.flatten.toSet == Set(tp20, tp21, tp22)
        )
      },
      test("different streams may resume every time") {
        val streams = Chunk(
          newStream(tp10, currentQueueSize = 25),
          newStream(tp11, currentQueueSize = 25),
          newStream(tp20, currentQueueSize = 25),
          newStream(tp21, currentQueueSize = 25),
          newStream(tp22, currentQueueSize = 25)
        )
        for {
          result1 <- fetchStrategy.selectPartitionsToFetch(streams)
          result2 <- fetchStrategy.selectPartitionsToFetch(streams)
          result3 <- fetchStrategy.selectPartitionsToFetch(streams)
          result4 <- fetchStrategy.selectPartitionsToFetch(streams)
          result5 <- fetchStrategy.selectPartitionsToFetch(streams)
          results = Chunk(result1, result2, result3, result4, result5)
        } yield assertTrue(
          // All partitions are selected eventually (since 25 <= 50):
          results.flatten.toSet.size == 5,
          // 3 partitions are selected every time (since 3*25 <= 80):
          results.forall(_.size == 3),
          // In at least 3 different combinations:
          results.combinations(2).count {
            case Chunk(resultA, resultB) => resultA != resultB
            case _                       => false // can not happen
          } >= 3
        )
      }
    )

  private def newStream(topicPartition: TopicPartition, currentQueueSize: Int): PartitionStream =
    new PartitionStream {
      override def tp: TopicPartition  = topicPartition
      override def queueSize: UIO[Int] = ZIO.succeed(currentQueueSize)
    }
}
