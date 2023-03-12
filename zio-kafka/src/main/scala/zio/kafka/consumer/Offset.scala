package zio.kafka.consumer

import org.apache.kafka.clients.consumer.RetriableCommitFailedException
import org.apache.kafka.common.TopicPartition
import zio.kafka.consumer.diagnostics.{ DiagnosticEvent, Diagnostics }
import zio.kafka.consumer.internal.Runloop.Command
import zio.kafka.types.OffsetBatch
import zio.{ Promise, Queue, RIO, Schedule, Task }

final case class Offset(
  topic: String,
  partition: Int,
  offset: Long
) {
  private[zio] def max(that: Offset): Offset = {
    val a = this.offset
    val b = that.offset
    if (a >= b) this else that
  }

  private[zio] def topicPartition: TopicPartition = new TopicPartition(topic, partition)
}

object Offset {
  def commit(commandQueue: Queue[Command], diagnostics: Diagnostics)(offset: Offset): Task[Unit] =
    commitBatch(commandQueue, diagnostics)(OffsetBatch.single(offset))

  def commitBatch(commandQueue: Queue[Command], diagnostics: Diagnostics)(
    offsets: OffsetBatch
  ): Task[Unit] =
    for {
      p <- Promise.make[Throwable, Unit]
      offs = offsets.map { case (key, offset) => key -> offset.offset }
      _ <- commandQueue.offer(Command.Commit(offs, p)).unit
      _ <- diagnostics.emitIfEnabled(DiagnosticEvent.Commit.Started(offs))
      _ <- p.await
    } yield ()

  /**
   * Attempts to commit and retries according to the given policy when the commit fails with a
   * RetriableCommitFailedException
   */
  def commitOrRetry[R](commandQueue: Queue[Command], diagnostics: Diagnostics)(
    offsets: Offset
  )(policy: Schedule[R, Throwable, Any]): RIO[R, Unit] =
    commitBatchOrRetry(commandQueue, diagnostics)(OffsetBatch.single(offsets))(policy)

  /**
   * Attempts to commit and retries according to the given policy when the commit fails with a
   * RetriableCommitFailedException
   */
  def commitBatchOrRetry[R](commandQueue: Queue[Command], diagnostics: Diagnostics)(
    offsets: OffsetBatch
  )(policy: Schedule[R, Throwable, Any]): RIO[R, Unit] =
    commitBatch(commandQueue, diagnostics)(offsets)
      .retry(
        Schedule.recurWhile[Throwable] {
          case _: RetriableCommitFailedException => true
          case _                                 => false
        } && policy
      )

}
