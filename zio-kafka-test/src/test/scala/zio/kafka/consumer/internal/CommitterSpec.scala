package zio.kafka.consumer.internal

import org.apache.kafka.clients.consumer.OffsetAndMetadata
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.errors.RebalanceInProgressException
import zio.kafka.consumer.diagnostics.Diagnostics
import zio.test._
import zio.{ durationInt, Promise, Queue, ZIO }

import java.util.{ Map => JavaMap }
import scala.jdk.CollectionConverters.MapHasAsJava

object CommitterSpec extends ZIOSpecDefault {
  override def spec = suite("Committer")(
    test("signals that a new commit is available") {
      for {
        commitAvailable <- Promise.make[Nothing, Unit]
        committer <- LiveCommitter
                       .make(
                         10.seconds,
                         Diagnostics.NoOp,
                         new DummyMetrics,
                         onCommitAvailable = commitAvailable.succeed(()).unit
                       )
        tp = new TopicPartition("topic", 0)
        _ <- committer.commit(Map(tp -> new OffsetAndMetadata(0))).forkScoped
        _ <- commitAvailable.await
      } yield assertCompletes
    },
    test("handles a successful commit by completing the commit effect") {
      for {
        commitAvailable <- Promise.make[Nothing, Unit]
        committer <- LiveCommitter.make(
                       10.seconds,
                       Diagnostics.NoOp,
                       new DummyMetrics,
                       onCommitAvailable = commitAvailable.succeed(()).unit
                     )
        tp = new TopicPartition("topic", 0)
        commitFiber <- committer.commit(Map(tp -> new OffsetAndMetadata(0))).forkScoped
        _           <- commitAvailable.await
        _           <- committer.processQueuedCommits(offsets => ZIO.succeed(offsets))
        _           <- commitFiber.join
      } yield assertCompletes
    },
    test("handles a failed commit by completing the commit effect with a failure") {
      for {
        commitAvailable <- Promise.make[Nothing, Unit]
        committer <- LiveCommitter.make(
                       10.seconds,
                       Diagnostics.NoOp,
                       new DummyMetrics,
                       onCommitAvailable = commitAvailable.succeed(()).unit
                     )
        tp = new TopicPartition("topic", 0)
        commitFiber <- committer.commit(Map(tp -> new OffsetAndMetadata(0))).forkScoped
        _           <- commitAvailable.await
        _           <- committer.processQueuedCommits(_ => ZIO.fail(new RuntimeException("Commit failed")))
        result      <- commitFiber.await
      } yield assertTrue(result.isFailure)
    },
    test("retries when rebalancing") {
      for {
        commitAvailable <- Queue.bounded[Unit](1)
        committer <- LiveCommitter.make(
                       10.seconds,
                       Diagnostics.NoOp,
                       new DummyMetrics,
                       onCommitAvailable = commitAvailable.offer(()).unit
                     )
        tp = new TopicPartition("topic", 0)
        commitFiber <- committer.commit(Map(tp -> new OffsetAndMetadata(0))).forkScoped
        _           <- commitAvailable.take
        _ <-
          committer.processQueuedCommits(_ => ZIO.fail(new RebalanceInProgressException("Rebalance in progress")))
        _ <- commitAvailable.take
        _ <- committer.processQueuedCommits(offsets => ZIO.succeed(offsets))
        _ <- commitFiber.join
      } yield assertCompletes
    },
    test("adds 1 to the committed last offset") {
      for {
        commitAvailable <- Promise.make[Nothing, Unit]
        committer <- LiveCommitter.make(
                       10.seconds,
                       Diagnostics.NoOp,
                       new DummyMetrics,
                       onCommitAvailable = commitAvailable.succeed(()).unit
                     )
        tp = new TopicPartition("topic", 0)
        _                <- committer.commit(Map(tp -> new OffsetAndMetadata(1))).forkScoped
        _                <- commitAvailable.await
        committedOffsets <- Promise.make[Nothing, JavaMap[TopicPartition, OffsetAndMetadata]]
        _ <- committer.processQueuedCommits(offsets => committedOffsets.succeed(offsets.asJava).as(offsets))
        offsetsCommitted <- committedOffsets.await
      } yield assertTrue(
        offsetsCommitted == Map(tp -> new OffsetAndMetadata(2)).asJava
      )
    },
    test("batches commits from multiple partitions and offsets") {
      for {
        commitAvailable <- Promise.make[Nothing, Unit]
        committer <- LiveCommitter.make(
                       10.seconds,
                       Diagnostics.NoOp,
                       new DummyMetrics,
                       onCommitAvailable = commitAvailable.succeed(()).unit
                     )
        tp  = new TopicPartition("topic", 0)
        tp2 = new TopicPartition("topic", 1)
        commitFiber1     <- committer.commit(Map(tp -> new OffsetAndMetadata(1))).forkScoped
        commitFiber2     <- committer.commit(Map(tp -> new OffsetAndMetadata(2))).forkScoped
        commitFiber3     <- committer.commit(Map(tp2 -> new OffsetAndMetadata(3))).forkScoped
        _                <- commitAvailable.await
        committedOffsets <- Promise.make[Nothing, JavaMap[TopicPartition, OffsetAndMetadata]]
        _ <- committer.processQueuedCommits(offsets => committedOffsets.succeed(offsets.asJava).as(offsets))
        _ <- commitFiber1.join zip commitFiber2.join zip commitFiber3.join
        offsetsCommitted <- committedOffsets.await
      } yield assertTrue(
        offsetsCommitted == Map(tp -> new OffsetAndMetadata(3), tp2 -> new OffsetAndMetadata(4)).asJava
      )
    },
    test("keeps track of pending commits") {
      for {
        commitAvailable <- Promise.make[Nothing, Unit]
        committer <- LiveCommitter.make(
                       10.seconds,
                       Diagnostics.NoOp,
                       new DummyMetrics,
                       onCommitAvailable = commitAvailable.succeed(()).unit
                     )
        tp = new TopicPartition("topic", 0)
        commitFiber                <- committer.commit(Map(tp -> new OffsetAndMetadata(0))).forkScoped
        _                          <- commitAvailable.await
        _                          <- committer.processQueuedCommits(ZIO.succeed(_))
        pendingCommitsDuringCommit <- committer.pendingCommitCount
        _                          <- committer.cleanupPendingCommits
        pendingCommitsAfterCommit  <- committer.pendingCommitCount
        _                          <- commitFiber.join
      } yield assertTrue(pendingCommitsDuringCommit == 1 && pendingCommitsAfterCommit == 0)
    },
    test("keep track of committed offsets") {
      for {
        commitAvailable <- Promise.make[Nothing, Unit]
        committer <- LiveCommitter.make(
                       10.seconds,
                       Diagnostics.NoOp,
                       new DummyMetrics,
                       onCommitAvailable = commitAvailable.succeed(()).unit
                     )
        tp = new TopicPartition("topic", 0)
        commitFiber      <- committer.commit(Map(tp -> new OffsetAndMetadata(0))).forkScoped
        _                <- commitAvailable.await
        _                <- committer.processQueuedCommits(ZIO.succeed(_))
        committedOffsets <- committer.getCommittedOffsets
        _                <- commitFiber.join
      } yield assertTrue(committedOffsets.offsets == Map(tp -> 0L))
    },
    test("clean committed offsets of no-longer assigned partitions") {
      for {
        commitAvailable <- Promise.make[Nothing, Unit]
        committer <- LiveCommitter.make(
                       10.seconds,
                       Diagnostics.NoOp,
                       new DummyMetrics,
                       onCommitAvailable = commitAvailable.succeed(()).unit
                     )
        tp = new TopicPartition("topic", 0)
        commitFiber      <- committer.commit(Map(tp -> new OffsetAndMetadata(0))).forkScoped
        _                <- commitAvailable.await
        _                <- committer.processQueuedCommits(ZIO.succeed(_))
        _                <- committer.keepCommitsForPartitions(Set.empty)
        committedOffsets <- committer.getCommittedOffsets
        _                <- commitFiber.join
      } yield assertTrue(committedOffsets.offsets.isEmpty)
    }
  ) @@ TestAspect.withLiveClock
}
