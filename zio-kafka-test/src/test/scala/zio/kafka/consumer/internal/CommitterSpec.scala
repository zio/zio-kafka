package zio.kafka.consumer.internal

import org.apache.kafka.clients.consumer.OffsetAndMetadata
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.errors.RebalanceInProgressException
import zio.kafka.consumer.diagnostics.Diagnostics
import zio.test._
import zio.{ durationInt, Promise, Ref, ZIO }

import java.util.{ Map => JavaMap }
import scala.jdk.CollectionConverters.MapHasAsJava

object CommitterSpec extends ZIOSpecDefault {
  override def spec = suite("Committer")(
    test("signals that a new commit is available") {
      for {
        runtime         <- ZIO.runtime[Any]
        commitAvailable <- Promise.make[Nothing, Unit]
        committer <- LiveCommitter
                       .make(
                         10.seconds,
                         Diagnostics.NoOp,
                         new DummyMetrics,
                         onCommitAvailable = commitAvailable.succeed(()).unit,
                         sameThreadRuntime = runtime
                       )
        tp = new TopicPartition("topic", 0)
        _ <- committer.commit(Map(tp -> new OffsetAndMetadata(0))).forkScoped
        _ <- commitAvailable.await
      } yield assertCompletes
    },
    test("handles a successful commit by completing the commit effect") {
      for {
        runtime         <- ZIO.runtime[Any]
        commitAvailable <- Promise.make[Nothing, Unit]
        committer <- LiveCommitter.make(
                       10.seconds,
                       Diagnostics.NoOp,
                       new DummyMetrics,
                       onCommitAvailable = commitAvailable.succeed(()).unit,
                       sameThreadRuntime = runtime
                     )
        tp = new TopicPartition("topic", 0)
        commitFiber <- committer.commit(Map(tp -> new OffsetAndMetadata(0))).forkScoped
        _           <- commitAvailable.await
        _ <- committer.processQueuedCommits((offsets, callback) => ZIO.attempt(callback.onComplete(offsets, null)))
        _ <- commitFiber.join
      } yield assertCompletes
    },
    test("handles a failed commit by completing the commit effect with a failure") {
      for {
        runtime         <- ZIO.runtime[Any]
        commitAvailable <- Promise.make[Nothing, Unit]
        committer <- LiveCommitter.make(
                       10.seconds,
                       Diagnostics.NoOp,
                       new DummyMetrics,
                       onCommitAvailable = commitAvailable.succeed(()).unit,
                       sameThreadRuntime = runtime
                     )
        tp = new TopicPartition("topic", 0)
        commitFiber <- committer.commit(Map(tp -> new OffsetAndMetadata(0))).forkScoped
        _           <- commitAvailable.await
        _ <- committer.processQueuedCommits((offsets, callback) =>
               ZIO.attempt(callback.onComplete(offsets, new RuntimeException("Commit failed")))
             )
        result <- commitFiber.await
      } yield assertTrue(result.isFailure)
    },
    test("retries when rebalancing") {
      for {
        runtime         <- ZIO.runtime[Any]
        commitAvailable <- Promise.make[Nothing, Unit]
        committer <- LiveCommitter.make(
                       10.seconds,
                       Diagnostics.NoOp,
                       new DummyMetrics,
                       onCommitAvailable = commitAvailable.succeed(()).unit,
                       sameThreadRuntime = runtime
                     )
        tp = new TopicPartition("topic", 0)
        commitFiber <- committer.commit(Map(tp -> new OffsetAndMetadata(0))).forkScoped
        _           <- commitAvailable.await
        _ <- committer.processQueuedCommits((offsets, callback) =>
               ZIO.attempt(callback.onComplete(offsets, new RebalanceInProgressException("Rebalance in progress")))
             )
        _      <- committer.processQueuedCommits((offsets, callback) => ZIO.attempt(callback.onComplete(offsets, null)))
        result <- commitFiber.await
      } yield assertTrue(result.isSuccess)
    },
    test("adds 1 to the committed last offset") {
      for {
        runtime         <- ZIO.runtime[Any]
        commitAvailable <- Promise.make[Nothing, Unit]
        committer <- LiveCommitter.make(
                       10.seconds,
                       Diagnostics.NoOp,
                       new DummyMetrics,
                       onCommitAvailable = commitAvailable.succeed(()).unit,
                       sameThreadRuntime = runtime
                     )
        tp = new TopicPartition("topic", 0)
        _                <- committer.commit(Map(tp -> new OffsetAndMetadata(1))).forkScoped
        _                <- commitAvailable.await
        committedOffsets <- Promise.make[Nothing, JavaMap[TopicPartition, OffsetAndMetadata]]
        _ <- committer.processQueuedCommits((offsets, callback) =>
               committedOffsets.succeed(offsets) *> ZIO.attempt(callback.onComplete(offsets, null))
             )
        offsetsCommitted <- committedOffsets.await
      } yield assertTrue(
        offsetsCommitted == Map(tp -> new OffsetAndMetadata(2)).asJava
      )
    },
    test("batches commits from multiple partitions and offsets") {
      for {
        runtime          <- ZIO.runtime[Any]
        commitsAvailable <- Promise.make[Nothing, Unit]
        nrCommitsDone    <- Ref.make(0)
        committer <- LiveCommitter.make(
                       10.seconds,
                       Diagnostics.NoOp,
                       new DummyMetrics,
                       onCommitAvailable =
                         ZIO.whenZIO(nrCommitsDone.updateAndGet(_ + 1).map(_ == 3))(commitsAvailable.succeed(())).unit,
                       sameThreadRuntime = runtime
                     )
        tp  = new TopicPartition("topic", 0)
        tp2 = new TopicPartition("topic", 1)
        commitFiber1     <- committer.commit(Map(tp -> new OffsetAndMetadata(1))).forkScoped
        commitFiber2     <- committer.commit(Map(tp -> new OffsetAndMetadata(2))).forkScoped
        commitFiber3     <- committer.commit(Map(tp2 -> new OffsetAndMetadata(3))).forkScoped
        _                <- commitsAvailable.await
        committedOffsets <- Promise.make[Nothing, JavaMap[TopicPartition, OffsetAndMetadata]]
        _ <- committer.processQueuedCommits((offsets, callback) =>
               committedOffsets.succeed(offsets) *> ZIO.attempt(callback.onComplete(offsets, null))
             )
        _                <- commitFiber1.join zip commitFiber2.join zip commitFiber3.join
        offsetsCommitted <- committedOffsets.await
      } yield assertTrue(
        offsetsCommitted == Map(tp -> new OffsetAndMetadata(3), tp2 -> new OffsetAndMetadata(4)).asJava
      )
    },
    test("keeps track of pending commits") {
      for {
        runtime         <- ZIO.runtime[Any]
        commitAvailable <- Promise.make[Nothing, Unit]
        committer <- LiveCommitter.make(
                       10.seconds,
                       Diagnostics.NoOp,
                       new DummyMetrics,
                       onCommitAvailable = commitAvailable.succeed(()).unit,
                       sameThreadRuntime = runtime
                     )
        tp = new TopicPartition("topic", 0)
        commitFiber <- committer.commit(Map(tp -> new OffsetAndMetadata(0))).forkScoped
        _           <- commitAvailable.await
        _ <- committer.processQueuedCommits((offsets, callback) => ZIO.attempt(callback.onComplete(offsets, null)))
        pendingCommitsDuringCommit <- committer.pendingCommitCount
        _                          <- committer.cleanupPendingCommits
        pendingCommitsAfterCommit  <- committer.pendingCommitCount
        _                          <- commitFiber.join
      } yield assertTrue(pendingCommitsDuringCommit == 1 && pendingCommitsAfterCommit == 0)
    },
    test("keep track of committed offsets") {
      for {
        runtime         <- ZIO.runtime[Any]
        commitAvailable <- Promise.make[Nothing, Unit]
        committer <- LiveCommitter.make(
                       10.seconds,
                       Diagnostics.NoOp,
                       new DummyMetrics,
                       onCommitAvailable = commitAvailable.succeed(()).unit,
                       sameThreadRuntime = runtime
                     )
        tp = new TopicPartition("topic", 0)
        commitFiber <- committer.commit(Map(tp -> new OffsetAndMetadata(0))).forkScoped
        _           <- commitAvailable.await
        _ <- committer.processQueuedCommits((offsets, callback) => ZIO.attempt(callback.onComplete(offsets, null)))
        committedOffsets <- committer.getCommittedOffsets
        _                <- commitFiber.join
      } yield assertTrue(committedOffsets.offsets == Map(tp -> 0L))
    },
    test("clean committed offsets of no-longer assigned partitions") {
      for {
        runtime         <- ZIO.runtime[Any]
        commitAvailable <- Promise.make[Nothing, Unit]
        committer <- LiveCommitter.make(
                       10.seconds,
                       Diagnostics.NoOp,
                       new DummyMetrics,
                       onCommitAvailable = commitAvailable.succeed(()).unit,
                       sameThreadRuntime = runtime
                     )
        tp = new TopicPartition("topic", 0)
        commitFiber <- committer.commit(Map(tp -> new OffsetAndMetadata(0))).forkScoped
        _           <- commitAvailable.await
        _ <- committer.processQueuedCommits((offsets, callback) => ZIO.attempt(callback.onComplete(offsets, null)))
        _ <- committer.keepCommitsForPartitions(Set.empty)
        committedOffsets <- committer.getCommittedOffsets
        _                <- commitFiber.join
      } yield assertTrue(committedOffsets.offsets.isEmpty)
    }
  ) @@ TestAspect.withLiveClock @@ TestAspect.nonFlaky(100)
}
