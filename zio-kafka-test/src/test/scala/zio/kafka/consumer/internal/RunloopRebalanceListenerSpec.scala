package zio.kafka.consumer.internal

import org.apache.kafka.clients.consumer._
import org.apache.kafka.common.TopicPartition
import zio.kafka.ZIOSpecDefaultSlf4j
import zio.kafka.consumer.diagnostics.Diagnostics
import zio.kafka.consumer.internal.LiveCommitter.{ Commit, CommitOffsets }
import zio.kafka.consumer.internal.Runloop.ByteArrayCommittableRecord
import zio.kafka.consumer.{ CommittableRecord, ConsumerSettings }
import zio.test._
import zio.{ durationInt, Chunk, Promise, Ref, Task, UIO, ZIO }

/**
 * Runloop should:
 *   - Track the partitions that were assigned, revoked and lost in multiple invocations
 *   - rebalanceSafeCommits mode
 *     - Await stream completion and end offset commit
 *     - End streams when partitions are assigned
 */

object RunloopRebalanceListenerSpec extends ZIOSpecDefaultSlf4j {
  type BinaryMockConsumer = MockConsumer[Array[Byte], Array[Byte]]

  def spec = suite("RunloopRebalanceListener")(
    test("should track assigned, revoked and lost partitions") {
      for {
        lastEvent <- Ref.make(RunloopRebalanceListener.RebalanceEvent.None)
        consumer = new BinaryMockConsumer(OffsetResetStrategy.LATEST) {}
        tp       = new TopicPartition("topic", 0)
        tp2      = new TopicPartition("topic", 1)
        tp3      = new TopicPartition("topic", 2)
        tp4      = new TopicPartition("topic", 3)
        listener <- makeListener(lastEvent, consumer)
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
        lastEvent <- Ref.make(RunloopRebalanceListener.RebalanceEvent.None)
        consumer = new BinaryMockConsumer(OffsetResetStrategy.LATEST) {}
        tp       = new TopicPartition("topic", 0)
        tp2      = new TopicPartition("topic", 1)
        tp3      = new TopicPartition("topic", 2)
        assignedStreams <- ZIO.foreach(Chunk(tp, tp2, tp3))(makeStreamControl)
        listener        <- makeListener(lastEvent, consumer, assignedStreams = assignedStreams)
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
          lastEvent <- Ref.make(RunloopRebalanceListener.RebalanceEvent.None)
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
              .tap(record =>
                committer.commit(
                  Map(
                    new TopicPartition("topic", record.partition) -> new OffsetAndMetadata(record.offset.offset, null)
                  )
                )
              )
              .runDrain
              .forkScoped
          listener <-
            makeListener(
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
//      TODO test("should wait until timeout")
      // TODO something with driving commits during waiting
    )
  ) @@ TestAspect.withLiveClock

  private def makeStreamControl(tp: TopicPartition): UIO[PartitionStreamControl] =
    PartitionStreamControl.newPartitionStream(tp, ZIO.unit, Diagnostics.NoOp, 30.seconds)

  private def makeListener(
    lastEvent: Ref[RunloopRebalanceListener.RebalanceEvent],
    mockConsumer: BinaryMockConsumer,
    assignedStreams: Chunk[PartitionStreamControl] = Chunk.empty,
    committer: Committer = new MockCommitter {},
    settings: ConsumerSettings = ConsumerSettings(List("")),
    rebalanceSafeCommits: Boolean = false
  ) =
    ConsumerAccess.make(mockConsumer).map { consumerAccess =>
      new RunloopRebalanceListener(
        lastEvent,
        settings.withRebalanceSafeCommits(rebalanceSafeCommits),
        consumerAccess,
        60.seconds,
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
