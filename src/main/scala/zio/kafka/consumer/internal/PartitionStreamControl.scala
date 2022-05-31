package zio.kafka.consumer.internal

import zio._
import zio.kafka.consumer.internal.Runloop.ByteArrayCommittableRecord
import zio.stream.Take

case class PartitionStreamControl(
  interrupt: Promise[Throwable, Unit],
  drainQueue: Queue[Take[Nothing, ByteArrayCommittableRecord]]
) {

  def finishWith(remaining: Chunk[ByteArrayCommittableRecord]): ZIO[Any, Nothing, Unit] =
    for {
      _ <- drainQueue.offer(Take.chunk(remaining))
      _ <- drainQueue.offer(Take.end)
      _ <- interrupt.succeed(())
    } yield ()
}
