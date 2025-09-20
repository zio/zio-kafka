package zio.kafka.consumer.internal

import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.TopicPartition
import zio._
import zio.kafka.consumer.CommittableRecord
import zio.kafka.consumer.internal.Runloop.ByteArrayCommittableRecord
import zio.kafka.diagnostics.Diagnostics
import zio.test._

import java.util.concurrent.TimeoutException

object PartitionStreamControlSpec extends ZIOSpecDefault {
  override def spec: Spec[Any, Throwable] = suite("PartitionStreamControl")(
    suite("Queue info")(
      test("offerRecords updates queue size correctly") {
        for {
          control <- createTestControl
          records = createTestRecords(3)
          _    <- control.offerRecords(records)
          size <- control.queueSize
        } yield assertTrue(size == 3)
      },
      test("empty offerRecords updates outstandingPolls") {
        for {
          control <- createTestControl
          _       <- control.offerRecords(Chunk.empty)
          _       <- control.offerRecords(Chunk.empty)
          polls   <- control.outstandingPolls
        } yield assertTrue(polls == 2)
      },
      test("multiple offers accumulate correctly") {
        for {
          control <- createTestControl
          _       <- control.offerRecords(createTestRecords(2))
          _       <- control.offerRecords(createTestRecords(3))
          size    <- control.queueSize
        } yield assertTrue(size == 5)
      }
    ),
    // Stream Control Tests
    suite("Stream control")(
      test("offered records end up in the stream") {
        for {
          control <- createTestControl
          records = createTestRecords(3)
          _ <- control.offerRecords(records)
          stream = control.stream
          pulledRecords <- stream.take(3).runCollect
        } yield assertTrue(records == pulledRecords)
      },
      test("end discards records and ends the stream") {
        for {
          control       <- createTestControl
          _             <- control.offerRecords(createTestRecords(3))
          _             <- control.end
          pulledRecords <- control.stream.runCollect
          hasEnded      <- control.hasEnded
        } yield assertTrue(pulledRecords.isEmpty && hasEnded)
      },
      test("offering records after end will fail") {
        for {
          control <- createTestControl
          records = createTestRecords(3)
          _    <- control.offerRecords(records)
          _    <- control.end
          exit <- control.offerRecords(records).exit
        } yield assertTrue(exit.isFailure)
      },
      test("halt completes the stream with a TimeoutException") {
        for {
          control <- createTestControl
          _       <- control.halt
          result  <- control.stream.runCollect.exit
        } yield assertTrue(result.isFailure, result.causeOrNull.squash.isInstanceOf[TimeoutException])
      },
      test("finalizing the stream will set isCompleted") {
        for {
          control            <- createTestControl
          initialIsCompleted <- control.isCompleted
          records = createTestRecords(3)
          _                <- control.offerRecords(records)
          _                <- control.end
          _                <- control.stream.runCollect
          finalIsCompleted <- control.isCompleted
        } yield assertTrue(!initialIsCompleted, finalIsCompleted)
      },
      test("pulling from the stream will request data") {
        ZIO.scoped {
          for {
            requested <- Promise.make[Nothing, Unit]
            control   <- createTestControlWithRequestData(requested.succeed(()))
            _         <- control.stream.runCollect.forkScoped
            _         <- requested.await
          } yield assertCompletes
        }
      },
      test("pulling from the stream when there are records in the queue will request additional data") {
        ZIO.scoped {
          for {
            requested <- Promise.make[Nothing, Unit]
            control   <- createTestControlWithRequestData(requested.succeed(()))
            records = createTestRecords(3)
            _ <- control.offerRecords(records)
            _ <- control.stream.runCollect.forkScoped
            _ <- requested.await
          } yield assertCompletes
        }
      }
    ),
    suite("Poll deadline")(
      test("maxPollIntervalExceeded returns false initially") {
        for {
          control  <- createTestControl
          now      <- Clock.nanoTime
          exceeded <- control.maxStreamPullIntervalExceeded(now)
        } yield assertTrue(!exceeded)
      },
      test("maxPollIntervalExceeded returns true after timeout") {
        for {
          control <- createTestControl
          _       <- control.offerRecords(createTestRecords(1))
          now     <- Clock.nanoTime
          futureTime = now + Duration.fromSeconds(31).toNanos
          exceeded <- control.maxStreamPullIntervalExceeded(futureTime)
        } yield assertTrue(exceeded)
      }
    ),
    suite("Offset Tracking")(
      test("lastPulledOffset updates correctly after each pull") {
        for {
          control <- createTestControl
          records = createTestRecords(6)
          _ <- control.offerRecords(records.take(3))
          offerNextBatch = control.offerRecords(records.slice(3, 6))

          offsetsAfterChunks <- Ref.make(Chunk.empty[Option[Long]])
          _ <- {
            def updateLastPulledOffsets =
              control.lastPulledOffset.flatMap(offset => offsetsAfterChunks.update(_ :+ offset.map(_.offset)))

            updateLastPulledOffsets *> control.stream
              .mapChunksZIO(updateLastPulledOffsets *> offerNextBatch.as(_))
              .take(6)
              .runCollect
          }
          lastPulledOffsets <- offsetsAfterChunks.get
        } yield assertTrue(lastPulledOffsets == Chunk(None, Some(3L), Some(6L)))
      }
    )
  ) @@ TestAspect.withLiveClock @@ TestAspect.timeout(1.minute)

  private def createTestControl: ZIO[Any, Nothing, PartitionStreamControl] =
    createTestControlWithRequestData(ZIO.unit)

  private def createTestControlWithRequestData(requestData: UIO[Any]): ZIO[Any, Nothing, PartitionStreamControl] = {
    val tp          = new TopicPartition("test-topic", 0)
    val diagnostics = Diagnostics.NoOp
    PartitionStreamControl.newPartitionStream(
      tp,
      requestData.unit,
      diagnostics,
      Duration.fromSeconds(30)
    )
  }

  private def createTestRecords(count: Int): Chunk[ByteArrayCommittableRecord] =
    Chunk.fromIterable(
      (1 to count).map(i =>
        CommittableRecord(
          record = new ConsumerRecord[Array[Byte], Array[Byte]](
            "test-topic",
            0,
            i.toLong,
            Array[Byte](),
            Array[Byte]()
          ),
          commitHandle = _ => ZIO.unit,
          consumerGroupMetadata = None
        )
      )
    )
}
