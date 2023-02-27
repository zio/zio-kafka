package zio.kafka.consumer.internal

import zio._
import zio.kafka.consumer.internal.Runloop.ByteArrayCommittableRecord
import zio.stream.Take

private[internal] case class PartitionStreamControl(
  interrupt: Promise[Throwable, Unit],
  drainQueue: Queue[Take[Nothing, ByteArrayCommittableRecord]]
) {

  def finishWith(remaining: Chunk[ByteArrayCommittableRecord]): ZIO[Any, Nothing, Unit] =
    for {
      _ <- if (remaining.isEmpty) drainQueue.offer(Take.end)
           else drainQueue.offerAll(List(Take.chunk(remaining), Take.end))
      _ <- interrupt.succeed(())
    } yield ()
}
