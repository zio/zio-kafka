package zio.kafka.consumer.internal

import org.apache.kafka.clients.consumer._
import org.apache.kafka.common.TopicPartition
import zio.kafka.ZIOSpecDefaultSlf4j
import zio.kafka.consumer.internal.Committer.CommitOffsets
import zio.kafka.consumer.internal.ConsumerAccess.ByteArrayKafkaConsumer
import zio.kafka.consumer.internal.RebalanceCoordinator._
import zio.kafka.consumer.internal.Runloop.ByteArrayCommittableRecord
import zio.kafka.consumer.{ CommittableRecord, ConsumerSettings }
import zio.test._
import zio._
import zio.kafka.diagnostics.Diagnostics
import zio.stream.ZStream

import java.util.{ Map => JavaMap }
import scala.jdk.CollectionConverters._

object RebalanceCoordinatorSpec extends ZIOSpecDefaultSlf4j {
  type BinaryMockConsumer = MockConsumer[Array[Byte], Array[Byte]]

  private val mockMetrics = new ConsumerMetrics {
    override def observePoll(resumedCount: Int, pausedCount: Int, latency: zio.Duration, pollSize: Int): UIO[Unit] =
      ZIO.unit

    override def observeCommit(latency: zio.Duration): UIO[Unit]                                 = ZIO.unit
    override def observeAggregatedCommit(latency: zio.Duration, commitSize: NanoTime): UIO[Unit] = ZIO.unit
    override def observeRebalance(
      currentlyAssignedCount: Int,
      assignedCount: Int,
      revokedCount: Int,
      lostCount: Int
    ): UIO[Unit] = ZIO.unit
    override def observeRunloopMetrics(
      state: Runloop.State,
      commandQueueSize: Int,
      commitQueueSize: Int,
      pendingCommits: Int
    ): UIO[Unit] = ZIO.unit
    override def observePollAuthError(): UIO[Unit] = ZIO.unit
  }

  def spec: Spec[TestEnvironment with Scope, Throwable] =
    suite("RunloopRebalanceListener")(
      test("should track assigned, revoked and lost partitions") {
        for {
          lastEvent <- Ref.Synchronized.make(RebalanceCoordinator.RebalanceEvent.None)
          consumer = new BinaryMockConsumer("latest") {}
          tp       = new TopicPartition("topic", 0)
          tp2      = new TopicPartition("topic", 1)
          tp3      = new TopicPartition("topic", 2)
          tp4      = new TopicPartition("topic", 3)
          listener <- makeCoordinator(lastEvent, consumer, rebalanceSafeCommits = false)
          _        <- listener.toRebalanceListener.onAssigned(Set(tp))
          _        <- listener.toRebalanceListener.onAssigned(Set(tp4))
          _        <- listener.toRebalanceListener.onRevoked(Set(tp2))
          _        <- listener.toRebalanceListener.onLost(Set(tp3))
          event    <- lastEvent.get
        } yield {
          def matches(
            rebalanceCallback: RebalanceCallback,
            assigned: Set[TopicPartition],
            revoked: Set[TopicPartition],
            lost: Set[TopicPartition]
          ): TestResult =
            assertTrue(
              rebalanceCallback.assignedTps == assigned,
              rebalanceCallback.revokedTps == revoked,
              rebalanceCallback.lostTps == lost
            )
          assertTrue(
            event.wasInvoked,
            event.rebalanceCallbacks.size == 4
          ) &&
          matches(event.rebalanceCallbacks(0), Set(tp), Set.empty, Set.empty) &&
          matches(event.rebalanceCallbacks(1), Set(tp4), Set.empty, Set.empty) &&
          matches(event.rebalanceCallbacks(2), Set.empty, Set(tp2), Set.empty) &&
          matches(event.rebalanceCallbacks(3), Set.empty, Set.empty, Set(tp3))
        }
      },
      test("should end streams for revoked and lost partitions") {
        for {
          lastEvent <- Ref.Synchronized.make(RebalanceCoordinator.RebalanceEvent.None)
          consumer = new BinaryMockConsumer("latest") {}
          tp       = new TopicPartition("topic", 0)
          tp2      = new TopicPartition("topic", 1)
          tp3      = new TopicPartition("topic", 2)
          assignedStreams <- ZIO.foreach(Chunk(tp, tp2, tp3))(makeStreamControl)
          listener        <- makeCoordinator(lastEvent, consumer, rebalanceSafeCommits = true, assignedStreams)
          _               <- listener.toRebalanceListener.onAssigned(Set(tp))
          _               <- listener.toRebalanceListener.onRevoked(Set(tp2))
          _               <- listener.toRebalanceListener.onLost(Set(tp3))
          event           <- lastEvent.get
          endedStreamTps = event.rebalanceCallbacks.flatMap(_.endedStreams).map(_.tp).toSet
        } yield assertTrue(endedStreamTps == Set(tp2, tp3))
      },
      suite("rebalanceSafeCommits")(
        test("holds up rebalance until the last pulled offset is committed") {
          // Outline of this test:
          // - Create a stream and offer it 3 records immediately
          // - Run the stream in the background, sleep for each record, then commit
          //   Total commit time: _at least_ 3*50ms = 150ms
          // - Create a coordinator with rebalanceSafeCommits enabled
          // - Wait until all 3 records are pulled by the stream
          // - Revoke the partition
          // - Assert that onRevoked takes its time, waiting for all 3 commits to happen
          //   but not so long that the maxRebalanceDuration triggered
          for {
            lastEvent            <- Ref.Synchronized.make(RebalanceCoordinator.RebalanceEvent.None)
            lastCommittedOffsets <- Ref.make(Map.empty[TopicPartition, Long])
            runtime              <- ZIO.runtime[Any]
            consumer = new CommitTrackingMockConsumer(runtime, lastCommittedOffsets)
            tp       = new TopicPartition("topic", 0)
            streamControl <- makeStreamControl(tp)
            recordCount = 3
            records     = createTestRecords(tp, recordCount)
            _ <- streamControl.offerRecords(records)

            recordsPulledByStream <- Promise.make[Nothing, Unit]
            committer             <- LiveCommitter.make(10.seconds, Diagnostics.NoOp, mockMetrics, ZIO.unit)
            _                     <- streamControl.stream
                   .completePromiseWhenOffsetSeen(recordCount.toLong, recordsPulledByStream)
                   .tap(_ => ZIO.sleep(50.millis))
                   .commitEachRecord(committer)
                   .runDrain
                   .forkScoped

            coordinator <- makeCoordinator(
                             lastEvent,
                             consumer,
                             rebalanceSafeCommits = true,
                             assignedStreams = Chunk(streamControl),
                             committer = committer,
                             maxRebalanceDuration = 10.seconds
                           )
            _              <- recordsPulledByStream.await
            revokeDuration <- coordinator.toRebalanceListener.onRevoked(Set(tp)).timed.map(_._1)

            committed <- lastCommittedOffsets.get
            lastOffset = committed.get(tp)
          } yield assertTrue(
            lastOffset.contains(recordCount.toLong),
            revokeDuration.toMillis > 150,
            revokeDuration.toMillis < 6000
          )
        },
        test("maximizes time to hold up rebalance when committing the last pulled offset takes too long") {
          // Outline of this test:
          // - Create a stream and offer it 3 records immediately
          // - Run the stream in the background, don't do any commits
          // - Create a coordinator with rebalanceSafeCommits enabled
          // - Wait until all 3 records are pulled by the stream
          // - Revoke the partition
          // - Assert that onRevoked takes its time, waiting for the configured max rebalance duration
          for {
            lastEvent <- Ref.Synchronized.make(RebalanceCoordinator.RebalanceEvent.None)
            consumer = new BinaryMockConsumer("latest") {}
            tp       = new TopicPartition("topic", 0)
            streamControl <- makeStreamControl(tp)
            recordCount = 3
            records     = createTestRecords(tp, recordCount)
            _ <- streamControl.offerRecords(records)

            recordsPulledByStream <- Promise.make[Nothing, Unit]
            _                     <- streamControl.stream
                   .completePromiseWhenOffsetSeen(recordCount.toLong, recordsPulledByStream)
                   .runDrain
                   .forkScoped

            coordinator <- makeCoordinator(
                             lastEvent,
                             consumer,
                             rebalanceSafeCommits = true,
                             assignedStreams = Chunk(streamControl),
                             commitTimeout = 1.second,
                             maxRebalanceDuration = 2.second
                           )
            _              <- recordsPulledByStream.await
            revokeDuration <- coordinator.toRebalanceListener.onRevoked(Set(tp)).timed.map(_._1)
          } yield assertTrue(
            // Actual max rebalance time is: maxRebalanceDuration - commitTimeout = 1 second
            revokeDuration.toMillis >= 1000
          )
        }
      ),
      suite("without rebalanceSafeCommits")(
        test("does not hold up rebalance even though last pulled offset is not committed") {
          // Outline of this test:
          // - Create a stream and offer it 3 records immediately
          // - Run the stream in the background, don't do any commits, sleep for each record to make sure it runs
          //   for a while
          // - Create a coordinator with rebalanceSafeCommits disabled
          // - Wait until all 3 records are pulled by the stream
          // - Revoke the partition
          // - Assert that onRevoked completes immediately, meaning that it does not wait for the stream to commit
          for {
            lastEvent <- Ref.Synchronized.make(RebalanceCoordinator.RebalanceEvent.None)
            consumer = new BinaryMockConsumer("latest") {}
            tp       = new TopicPartition("topic", 0)
            streamControl <- makeStreamControl(tp)
            recordCount = 3
            records     = createTestRecords(tp, recordCount)
            _ <- streamControl.offerRecords(records)

            // Make the stream slow by sleeping for each record
            recordsPulledByStream <- Promise.make[Nothing, Unit]
            _                     <- streamControl.stream
                   .completePromiseWhenOffsetSeen(recordCount.toLong, recordsPulledByStream)
                   .tap(_ => ZIO.sleep(500.millis))
                   .runDrain
                   .forkScoped

            coordinator <- makeCoordinator(
                             lastEvent,
                             consumer,
                             rebalanceSafeCommits = false,
                             assignedStreams = Chunk(streamControl)
                           )
            _              <- recordsPulledByStream.await
            revokeDuration <- coordinator.toRebalanceListener.onRevoked(Set(tp)).timed.map(_._1)
          } yield assertTrue(revokeDuration.toMillis < 1500)
        }
      )
    ) @@ TestAspect.withLiveClock

  private def makeStreamControl(tp: TopicPartition): UIO[PartitionStreamControl] =
    PartitionStreamControl.newPartitionStream(tp, ZIO.unit, Diagnostics.NoOp, 30.seconds)

  private def makeCoordinator(
    lastEvent: Ref.Synchronized[RebalanceEvent],
    mockConsumer: BinaryMockConsumer,
    rebalanceSafeCommits: Boolean,
    assignedStreams: Chunk[PartitionStreamControl] = Chunk.empty,
    committer: Committer = new MockCommitter {},
    settings: ConsumerSettings = ConsumerSettings(List("")),
    commitTimeout: Duration = 1.second,
    maxRebalanceDuration: Duration = 3.seconds
  ): ZIO[Scope, Throwable, RebalanceCoordinator] =
    Semaphore
      .make(1)
      .map(new ConsumerAccess(mockConsumer, _))
      .map { consumerAccess =>
        new RebalanceCoordinator(
          lastEvent,
          settings
            .withRebalanceSafeCommits(rebalanceSafeCommits)
            .withCommitTimeout(commitTimeout)
            .withMaxRebalanceDuration(maxRebalanceDuration),
          consumerAccess,
          maxRebalanceDuration,
          ZIO.succeed(assignedStreams),
          committer
        )
      }

  private def createTestRecords(tp: TopicPartition, count: Int): Chunk[ByteArrayCommittableRecord] =
    Chunk.fromIterable(
      (1 to count).map(i =>
        CommittableRecord(
          record = new ConsumerRecord[Array[Byte], Array[Byte]](
            tp.topic(),
            tp.partition(),
            i.toLong,
            Array[Byte](),
            Array[Byte]()
          ),
          commitHandle = _ => ZIO.unit,
          consumerGroupMetadata = None
        )
      )
    )

  private implicit class RecordStreamOps(private val s: ZStream[Any, Throwable, ByteArrayCommittableRecord])
      extends AnyVal {
    def completePromiseWhenOffsetSeen(
      offsetToSee: Long,
      offsetSeenPromise: Promise[Nothing, Unit]
    ): ZStream[Any, Throwable, ByteArrayCommittableRecord] =
      s.tapChunksZIO(chunk => offsetSeenPromise.succeed(()).when(chunk.map(_.offset.offset).max >= offsetToSee))

    def commitEachRecord(committer: Committer): ZStream[Any, Throwable, ByteArrayCommittableRecord] =
      s.tap { record =>
        committer.commit {
          Map(
            new TopicPartition(record.offset.topic, record.offset.partition) ->
              new OffsetAndMetadata(record.offset.offset)
          )
        }
      }

  }

  private implicit class ZStreamTapChunks[R, E, A](private val s: ZStream[R, E, A]) extends AnyVal {
    def tapChunksZIO[R1 <: R, E1 >: E](f: Chunk[A] => ZIO[R1, E1, Any])(implicit trace: Trace): ZStream[R1, E1, A] =
      s.mapChunksZIO(chunk => f(chunk).as(chunk))
  }

}

abstract private class MockCommitter extends Committer {
  override def commit(offsets: Map[TopicPartition, OffsetAndMetadata]): Task[Unit]                  = ZIO.unit
  override def registerExternalCommits(offsets: Map[TopicPartition, OffsetAndMetadata]): Task[Unit] = ZIO.unit

  override def processQueuedCommits(consumer: ByteArrayKafkaConsumer, executeOnEmpty: Boolean): Task[Unit] = ZIO.unit

  override def queueSize: UIO[Int]                   = ZIO.succeed(0)
  override def pendingCommitCount: UIO[Int]          = ZIO.succeed(0)
  override def getPendingCommits: UIO[CommitOffsets] = ZIO.succeed(CommitOffsets.empty)
  override def cleanupPendingCommits: UIO[Unit]      = ZIO.unit
  override def keepCommitsForPartitions(assignedPartitions: Set[TopicPartition]): UIO[Unit] = ZIO.unit
  override def getCommittedOffsets: UIO[CommitOffsets] = ZIO.succeed(CommitOffsets.empty)
}

private class CommitTrackingMockConsumer(
  runtime: Runtime[Any],
  lastCommittedOffsets: Ref[Map[TopicPartition, Long]]
) extends MockConsumer[Array[Byte], Array[Byte]]("latest") {
  override def commitAsync(
    offsets: JavaMap[TopicPartition, OffsetAndMetadata],
    callback: OffsetCommitCallback
  ): Unit = {
    // The calls without offsets are not interesting, ignore those.
    if (!offsets.isEmpty) {
      // Subtract 1 to go back from 'next record to read' to 'offset of last committed record'
      val scalaOffsets = offsets.asScala.view.mapValues(_.offset() - 1L).toMap
      Unsafe.unsafe { implicit u =>
        runtime.unsafe.run(lastCommittedOffsets.set(scalaOffsets)).getOrThrow()
      }
    }
    super.commitAsync(offsets, callback)
  }
}
