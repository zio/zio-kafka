package zio.kafka.consumer.internal

import org.apache.kafka.clients.consumer.{ MockConsumer, OffsetAndMetadata, OffsetCommitCallback, OffsetResetStrategy }
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.errors.RebalanceInProgressException
import zio.kafka.consumer.diagnostics.Diagnostics
import zio.test._
import zio._

import java.util.{ Map => JavaMap }
import scala.jdk.CollectionConverters.{ MapHasAsJava, MapHasAsScala }

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
        consumer    <- createMockConsumer(offsets => ZIO.succeed(offsets))
        _           <- committer.processQueuedCommits(consumer)
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
        consumer    <- createMockConsumer(_ => ZIO.fail(new RuntimeException("Commit failed")))
        _           <- committer.processQueuedCommits(consumer)
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
        callCount   <- Ref.make(0)
        consumer <-
          createMockConsumer { offsets =>
            callCount.updateAndGet(_ + 1).flatMap { count =>
              if (count == 1) {
                ZIO.fail(new RebalanceInProgressException("Rebalance in progress"))
              } else {
                ZIO.succeed(offsets)
              }
            }
          }
        _ <- committer.processQueuedCommits(consumer)
        _ <- commitAvailable.take
        _ <- committer.processQueuedCommits(consumer)
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
        consumer         <- createMockConsumer(offsets => committedOffsets.succeed(offsets.asJava).as(offsets))
        _                <- committer.processQueuedCommits(consumer)
        offsetsCommitted <- committedOffsets.await
      } yield assertTrue(
        offsetsCommitted == Map(tp -> new OffsetAndMetadata(2)).asJava
      )
    },
    test("batches commits from multiple partitions and offsets") {
      for {
        commitsAvailable <- Promise.make[Nothing, Unit]
        nrCommitsDone    <- Ref.make(0)
        committer <- LiveCommitter.make(
                       10.seconds,
                       Diagnostics.NoOp,
                       new DummyMetrics,
                       onCommitAvailable =
                         ZIO.whenZIO(nrCommitsDone.updateAndGet(_ + 1).map(_ == 3))(commitsAvailable.succeed(())).unit
                     )
        tp  = new TopicPartition("topic", 0)
        tp2 = new TopicPartition("topic", 1)
        commitFiber1     <- committer.commit(Map(tp -> new OffsetAndMetadata(1))).forkScoped
        commitFiber2     <- committer.commit(Map(tp -> new OffsetAndMetadata(2))).forkScoped
        commitFiber3     <- committer.commit(Map(tp2 -> new OffsetAndMetadata(3))).forkScoped
        _                <- commitsAvailable.await
        committedOffsets <- Promise.make[Nothing, JavaMap[TopicPartition, OffsetAndMetadata]]
        consumer         <- createMockConsumer(offsets => committedOffsets.succeed(offsets.asJava).as(offsets))
        _                <- committer.processQueuedCommits(consumer)
        _                <- commitFiber1.join zip commitFiber2.join zip commitFiber3.join
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
        consumer                   <- createMockConsumer(offsets => ZIO.succeed(offsets))
        _                          <- committer.processQueuedCommits(consumer)
        pendingCommitsDuringCommit <- committer.pendingCommitCount
        _                          <- commitFiber.join
        _                          <- committer.cleanupPendingCommits
        pendingCommitsAfterCommit  <- committer.pendingCommitCount
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
        consumer         <- createMockConsumer(offsets => ZIO.succeed(offsets))
        _                <- committer.processQueuedCommits(consumer)
        _                <- commitFiber.join
        committedOffsets <- committer.getCommittedOffsets
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
        consumer         <- createMockConsumer(offsets => ZIO.succeed(offsets))
        _                <- committer.processQueuedCommits(consumer)
        _                <- commitFiber.join
        _                <- committer.keepCommitsForPartitions(Set.empty)
        committedOffsets <- committer.getCommittedOffsets
      } yield assertTrue(committedOffsets.offsets.isEmpty)
    }
  ) @@ TestAspect.withLiveClock @@ TestAspect.nonFlaky(100)

  private def createMockConsumer(
    onCommitAsync: Map[TopicPartition, OffsetAndMetadata] => Task[Map[TopicPartition, OffsetAndMetadata]]
  ): ZIO[Any, Nothing, MockConsumer[Array[Byte], Array[Byte]]] =
    ZIO.runtime[Any].map { runtime =>
      new MockConsumer[Array[Byte], Array[Byte]](OffsetResetStrategy.LATEST) {
        override def commitAsync(
          offsets: JavaMap[TopicPartition, OffsetAndMetadata],
          callback: OffsetCommitCallback
        ): Unit =
          Unsafe.unsafe { implicit unsafe =>
            runtime.unsafe
              .run(
                onCommitAsync(offsets.asScala.toMap)
                  .tapBoth(
                    e => ZIO.attempt(callback.onComplete(offsets, e.asInstanceOf[Exception])),
                    offsets => ZIO.attempt(callback.onComplete(offsets.asJava, null))
                  )
                  .ignore
              )
              .getOrThrowFiberFailure()
          }

      }
    }
}
