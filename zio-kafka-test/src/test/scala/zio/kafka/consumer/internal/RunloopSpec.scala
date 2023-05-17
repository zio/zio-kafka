package zio.kafka.consumer.internal

import org.apache.kafka.common.TopicPartition
import zio.kafka.consumer.internal.Runloop.ByteArrayCommittableRecord
import zio.stream.{ Take, ZStream }
import zio.test._
import zio.{ Chunk, Promise, Queue, Scope, Task }

import java.util
import scala.jdk.CollectionConverters._

object RunloopSpec extends ZIOSpecDefault {

  override def spec: Spec[TestEnvironment with Scope, Any] = suite("RunloopSpec")(
    suite("resumeAndPausePartitions")(
      test("resumes partitions with requests, pauses the rest") {
        val consumer     = new PauseResumeMockKafkaConsumer
        val processingTp = newTopicPartition(2)
        val requestedTp  = newTopicPartition(1)
        for {
          processingStream <- newStream(processingTp)
          requestingStream <- newStream(requestedTp)
        } yield {
          Runloop.resumeAndPausePartitions(
            consumer,
            requestedPartitions = Set(requestedTp),
            streams = Chunk(processingStream, requestingStream)
          )
          assertTrue(
            consumer.pausedTps == Seq(processingTp),
            processingStream.pollResumedHistory.asBitString == "0",
            consumer.resumedTps == Seq(requestedTp),
            requestingStream.pollResumedHistory.asBitString == "1"
          )
        }
      },
      test("no failures for no streams") {
        val consumer = new PauseResumeMockKafkaConsumer
        Runloop.resumeAndPausePartitions(consumer, Set.empty, Chunk.empty)
        assertTrue(
          consumer.pausedTps.isEmpty,
          consumer.resumedTps.isEmpty
        )
      },
      test("optimistically resumes a partition") {
        val consumer     = new PauseResumeMockKafkaConsumer
        val processingTp = newTopicPartition(1)
        for {
          // `"1" * 5` is a pattern that causes optimistic resume
          processingStream <- newStream(processingTp, "1" * 5)
        } yield {
          Runloop.resumeAndPausePartitions(
            consumer,
            requestedPartitions = Set.empty,
            streams = Chunk(processingStream)
          )
          assertTrue(
            consumer.pausedTps == Seq.empty,
            consumer.resumedTps == Seq(processingTp),
            processingStream.pollResumedHistory.asBitString == "1" * 6
          )
        }
      },
      test("force pauses a long resumed partition with request") {
        val consumer     = new PauseResumeMockKafkaConsumer
        val requestedTp1 = newTopicPartition(1)
        val requestedTp2 = newTopicPartition(2)
        for {
          requestingStream1 <- newStream(requestedTp1)
          requestingStream2 <- newStream(requestedTp2, "1" * 8)
        } yield {
          Runloop.resumeAndPausePartitions(
            consumer,
            requestedPartitions = Set(requestedTp1, requestedTp2),
            streams = Chunk(requestingStream1, requestingStream2)
          )
          assertTrue(
            consumer.pausedTps == Seq(requestedTp2),
            requestingStream2.pollResumedHistory.asBitString == "1" * 8 + "0",
            consumer.resumedTps == Seq(requestedTp1),
            requestingStream1.pollResumedHistory.asBitString == "1"
          )
        }
      },
      test("force pauses a long resumed partition with optimistic resume") {
        val consumer     = new PauseResumeMockKafkaConsumer
        val requestedTp  = newTopicPartition(1)
        val processingTp = newTopicPartition(2)
        for {
          requestingStream <- newStream(requestedTp)
          // `"1" * 5` is a pattern that causes optimistic resume
          processingStream <- newStream(processingTp, "1" * 5)
        } yield {
          Runloop.resumeAndPausePartitions(
            consumer,
            requestedPartitions = Set(requestedTp),
            streams = Chunk(requestingStream, processingStream)
          )
          assertTrue(
            consumer.pausedTps == Seq(processingTp),
            processingStream.pollResumedHistory.asBitString == "1" * 5 + "0",
            consumer.resumedTps == Seq(requestedTp),
            requestingStream.pollResumedHistory.asBitString == "1"
          )
        }
      }
    )
  )

  private class PauseResumeMockKafkaConsumer extends ProxyKafkaConsumer {
    var pausedTps: Seq[TopicPartition]  = Seq.empty
    var resumedTps: Seq[TopicPartition] = Seq.empty

    override def pause(tps: util.Collection[TopicPartition]): Unit =
      this.pausedTps = tps.asScala.toSeq

    override def resume(tps: util.Collection[TopicPartition]): Unit =
      this.resumedTps = tps.asScala.toSeq
  }

  private def newTopicPartition(partition: Int) =
    new TopicPartition("t1", partition)

  private def newStream(tp: TopicPartition, pollHistory: String = "0"): Task[PartitionStreamControl] =
    for {
      dummyQueue          <- Queue.bounded[Take[Throwable, ByteArrayCommittableRecord]](1)
      interruptionPromise <- Promise.make[Throwable, Unit]
      completedPromise    <- Promise.make[Nothing, Unit]
    } yield {
      val stream = new PartitionStreamControl(
        tp,
        ZStream.empty,
        dummyQueue,
        interruptionPromise,
        completedPromise
      )
      stream.pollResumedHistory = pollHistory.toPollHistory
      stream
    }

}
