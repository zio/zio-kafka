package zio.kafka.consumer

import org.apache.kafka.common.TopicPartition
import zio.kafka.utils.PendingCommit
import zio.{Chunk, Promise, Ref, Schedule, Scope, Task, UIO, URIO, ZIO}

trait Committer {
  def commit(records: Chunk[CommittableRecord[_, _]]): UIO[PendingCommit[Throwable, Unit]]
}

//noinspection ConvertExpressionToSAM
object Committer {
  private val emptyState: (Map[TopicPartition, Long], List[Promise[Throwable, Unit]]) =
    (Map.empty[TopicPartition, Long], List.empty[Promise[Throwable, Unit]])

  val unit: Committer =
    new Committer {
      override def commit(records: Chunk[CommittableRecord[_, _]]): UIO[PendingCommit[Throwable, Unit]] =
        ZIO.succeed(PendingCommit.unit.asInstanceOf[PendingCommit[Throwable, Unit]])
    }

  private[zio] def fromSchedule[R](
    commitSchedule: Schedule[R, Any, Any],
    commit: Map[TopicPartition, Long] => Task[Unit],
    scope: Scope
  ): URIO[R, Committer] =
    for {
      acc <- Ref.Synchronized.make(emptyState)
      _ <- acc.updateZIO { case data @ (offsets, promises) =>
             if (offsets.isEmpty) ZIO.succeed(data)
             else
               commit(offsets)
                 .foldZIO(
                   e => ZIO.foreachDiscard(promises)(_.fail(e)),
                   _ => ZIO.foreachDiscard(promises)(_.succeed(()))
                 )
                 .as(emptyState)
           }
             .schedule(commitSchedule)
             .forkIn(scope)
    } yield new Committer {
      override def commit(records: Chunk[CommittableRecord[_, _]]): UIO[PendingCommit[Throwable, Unit]] =
        for {
          p <- Promise.make[Throwable, Unit]
          newOffsets = records.map(record => record.topicPartition -> record.record.offset())
          _ <- acc.update { case (offsets, promises) => (offsets ++ newOffsets, promises :+ p) }
        } yield PendingCommit(p)
    }
}
