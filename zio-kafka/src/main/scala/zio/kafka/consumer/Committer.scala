package zio.kafka.consumer

import org.apache.kafka.clients.consumer.ConsumerRecord
import zio.kafka.consumer.types.OffsetBatch
import zio.kafka.utils.PendingCommit
import zio.{ Chunk, Promise, Ref, Schedule, Scope, Task, UIO, URIO, ZIO }

trait Committer {
  def commit(offsetBatch: OffsetBatch): UIO[PendingCommit[Throwable, Unit]]

  final def commit(records: Chunk[ConsumerRecord[_, _]]): UIO[PendingCommit[Throwable, Unit]] =
    commit(OffsetBatch.from(records))

  final def commitAndAwait(records: Chunk[ConsumerRecord[_, _]]): Task[Unit] =
    commit(records).flatMap(_.awaitCommit)

  final def commitAndForget(records: Chunk[ConsumerRecord[_, _]]): UIO[Unit] =
    commit(records).unit
}

//noinspection ConvertExpressionToSAM
object Committer {
  private val emptyState: (OffsetBatch, List[Promise[Throwable, Unit]]) =
    (OffsetBatch.empty, List.empty[Promise[Throwable, Unit]])

  val unit: Committer =
    new Committer {
      override def commit(offsetBatch: OffsetBatch): UIO[PendingCommit[Throwable, Unit]] =
        ZIO.succeed(PendingCommit.unit.asInstanceOf[PendingCommit[Throwable, Unit]])
    }

  private[zio] def fromSchedule[R](
    commitSchedule: Schedule[R, Any, Any],
    commit: OffsetBatch => Task[Unit],
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
      override def commit(offsetBatch: OffsetBatch): UIO[PendingCommit[Throwable, Unit]] =
        for {
          p <- Promise.make[Throwable, Unit]
          _ <- acc.update { case (offsets, promises) => (offsets merge offsetBatch, promises :+ p) }
        } yield PendingCommit(p)
    }
}
