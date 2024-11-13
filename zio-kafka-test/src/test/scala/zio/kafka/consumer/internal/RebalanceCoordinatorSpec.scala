package zio.kafka.consumer.internal

import org.apache.kafka.clients.consumer._
import org.apache.kafka.common.TopicPartition
import zio.kafka.ZIOSpecDefaultSlf4j
import zio.kafka.consumer.diagnostics.Diagnostics
import zio.kafka.consumer.internal.LiveCommitter.{ Commit, CommitOffsets }
import zio.kafka.consumer.internal.RebalanceCoordinator.RebalanceEvent
import zio.kafka.consumer.internal.Runloop.ByteArrayCommittableRecord
import zio.kafka.consumer.{ CommittableRecord, ConsumerSettings }
import zio.test._
import zio.{ durationInt, Chunk, Promise, Ref, Scope, Semaphore, Task, UIO, ZIO }

import java.util

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

  def spec = suite("RunloopRebalanceListener")(
    test("should track assigned, revoked and lost partitions") {
      for {
        lastEvent <- Ref.make(RebalanceCoordinator.RebalanceEvent.None)
        consumer = new BinaryMockConsumer(OffsetResetStrategy.LATEST) {}
        tp       = new TopicPartition("topic", 0)
        tp2      = new TopicPartition("topic", 1)
        tp3      = new TopicPartition("topic", 2)
        tp4      = new TopicPartition("topic", 3)
        listener <- makeCoordinator(lastEvent, consumer)
        _        <- listener.toRebalanceListener.onAssigned(Set(tp))
        _        <- listener.toRebalanceListener.onAssigned(Set(tp4))
        _        <- listener.toRebalanceListener.onRevoked(Set(tp2))
        _        <- listener.toRebalanceListener.onLost(Set(tp3))
        event    <- lastEvent.get
      } yield assertTrue(
        event.wasInvoked && event.assignedTps == Set(tp, tp4) && event.revokedTps == Set(tp2) && event.lostTps == Set(
          tp3
        )
      )
    },
    test("should end streams for revoked and lost partitions") {
      for {
        lastEvent <- Ref.make(RebalanceCoordinator.RebalanceEvent.None)
        consumer = new BinaryMockConsumer(OffsetResetStrategy.LATEST) {}
        tp       = new TopicPartition("topic", 0)
        tp2      = new TopicPartition("topic", 1)
        tp3      = new TopicPartition("topic", 2)
        assignedStreams <- ZIO.foreach(Chunk(tp, tp2, tp3))(makeStreamControl)
        listener        <- makeCoordinator(lastEvent, consumer, assignedStreams = assignedStreams)
        _               <- listener.toRebalanceListener.onAssigned(Set(tp))
        _               <- listener.toRebalanceListener.onRevoked(Set(tp2))
        _               <- listener.toRebalanceListener.onLost(Set(tp3))
        event           <- lastEvent.get
        // Lost and end partition's stream should be ended
        _ <- assignedStreams(1).stream.runDrain
        _ <- assignedStreams(2).stream.runDrain
      } yield assertTrue(event.endedStreams.map(_.tp).toSet == Set(tp2, tp3))
    },
    suite("rebalanceSafeCommits")(
      test("should wait for the last pulled offset to commit") {
        for {
          lastEvent <- Ref.make(RebalanceCoordinator.RebalanceEvent.None)
          // Mock consumer that does not complete commit callbacks until commitSync is called
          consumer = new BinaryMockConsumer(OffsetResetStrategy.LATEST) {
                       var callback: OffsetCommitCallback                       = null
                       var offsets: util.Map[TopicPartition, OffsetAndMetadata] = null

                       override def commitAsync(
                         offsets: util.Map[TopicPartition, OffsetAndMetadata],
                         callback: OffsetCommitCallback
                       ): Unit = {
                         // Do nothing during rebalancing
                         if (callback != null) callback.onComplete(offsets, null)
                         if (!offsets.isEmpty) {
                           this.callback = callback
                           this.offsets = offsets
                         }
                       }

                       override def commitSync(): Unit =
                         callback.onComplete(offsets, null)
                     }
          tp = new TopicPartition("topic", 0)
          streamControl <- makeStreamControl(tp)
          records = createTestRecords(3)
          recordsPulled <- Promise.make[Nothing, Unit]
          _             <- streamControl.offerRecords(records)
          runtime       <- ZIO.runtime[Any]
          committer     <- LiveCommitter.make(10.seconds, Diagnostics.NoOp, mockMetrics, ZIO.unit, runtime)

          streamDrain <-
            streamControl.stream
              .tap(_ => recordsPulled.succeed(()))
              .tap(record =>
                committer
                  .commit(
                    Map(
                      new TopicPartition("topic", record.partition) -> new OffsetAndMetadata(record.offset.offset, null)
                    )
                  )
                  .debug(s"Done commit for ${record.offset.offset}")
              )
              .runDrain
              .forkScoped
          listener <-
            makeCoordinator(
              lastEvent,
              consumer,
              assignedStreams = Chunk(streamControl),
              rebalanceSafeCommits = true,
              committer = committer
            )
          _ <- listener.toRebalanceListener.onRevoked(Set(tp))
          _ <- streamDrain.join
        } yield assertCompletes
      },
      // TODO something with driving commits during waiting
      test("should continue if waiting for the stream to continue has timed out") {
        for {
          lastEvent <- Ref.make(RebalanceCoordinator.RebalanceEvent.None)
          consumer = new BinaryMockConsumer(OffsetResetStrategy.LATEST) {}
          tp       = new TopicPartition("topic", 0)
          streamControl <- makeStreamControl(tp)
          records = createTestRecords(3)
          recordsPulled    <- Promise.make[Nothing, Unit]
          _                <- streamControl.offerRecords(records)
          committedOffsets <- Ref.make(CommitOffsets.empty)
          done             <- Promise.make[Throwable, Unit]
          committer = new MockCommitter {
                        override val commit =
                          offsets =>
                            committedOffsets
                              .update(_.addCommits(Chunk(Commit(0L, offsets, done)))._2)
                        override def getCommittedOffsets = committedOffsets.get
                      }
          streamDrain <-
            streamControl.stream
              .tap(_ => recordsPulled.succeed(()))
              .runDrain
              .forkScoped
          listener <-
            makeCoordinator(
              lastEvent,
              consumer,
              assignedStreams = Chunk(streamControl),
              rebalanceSafeCommits = true,
              committer = committer
            )
          _ <- listener.toRebalanceListener.onRevoked(Set(tp))
          _ <- streamDrain.join
        } yield assertCompletes
      }
    )
    // TODO something with driving commits during waiting
  ) @@ TestAspect.withLiveClock

  private def makeStreamControl(tp: TopicPartition): UIO[PartitionStreamControl] =
    PartitionStreamControl.newPartitionStream(tp, ZIO.unit, Diagnostics.NoOp, 30.seconds)

  private def makeCoordinator(
    lastEvent: Ref[RebalanceEvent],
    mockConsumer: BinaryMockConsumer,
    assignedStreams: Chunk[PartitionStreamControl] = Chunk.empty,
    committer: Committer = new MockCommitter {},
    settings: ConsumerSettings = ConsumerSettings(List("")).withCommitTimeout(1.second),
    rebalanceSafeCommits: Boolean = false
  ): ZIO[Scope, Throwable, RebalanceCoordinator] =
    Semaphore.make(1).map(new ConsumerAccess(mockConsumer, _)).map { consumerAccess =>
      new RebalanceCoordinator(
        lastEvent,
        settings.withRebalanceSafeCommits(rebalanceSafeCommits),
        consumerAccess,
        5.seconds,
        ZIO.succeed(assignedStreams),
        committer
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

abstract class MockCommitter extends Committer {
  override val commit: Map[TopicPartition, OffsetAndMetadata] => Task[Unit] = _ => ZIO.unit

  override def processQueuedCommits(
    commitAsync: (java.util.Map[TopicPartition, OffsetAndMetadata], OffsetCommitCallback) => zio.Task[Unit],
    executeOnEmpty: Boolean
  ): zio.Task[Unit] = ZIO.unit
  override def queueSize: UIO[Int]                      = ZIO.succeed(0)
  override def pendingCommitCount: UIO[Int]             = ZIO.succeed(0)
  override def getPendingCommits: UIO[CommitOffsets]    = ZIO.succeed(CommitOffsets.empty)
  override def updatePendingCommitsAfterPoll: UIO[Unit] = ZIO.unit
  override def pruneCommittedOffsets(assignedPartitions: Set[TopicPartition]): UIO[Unit] = ZIO.unit
  override def getCommittedOffsets: UIO[CommitOffsets] = ZIO.succeed(CommitOffsets.empty)
}
