package zio.kafka.consumer.internal

import org.apache.kafka.common.TopicPartition
import zio._
import zio.kafka.consumer.internal.Runloop.ByteArrayCommittableRecord
import zio.stream.Take

private[internal] case class PartitionStreamControl(
  topicPartition: TopicPartition,
  drainQueue: Queue[Take[Nothing, ByteArrayCommittableRecord]],
  streamCompleted: Promise[Nothing, Unit]
) {

  def finishWith(remaining: Chunk[ByteArrayCommittableRecord]): ZIO[Any, Nothing, Unit] =
    ZIO.logAnnotate(
      LogAnnotation("topic", topicPartition.topic()),
      LogAnnotation("partition", topicPartition.partition().toString)
    ) {
      for {
        _ <- drainQueue.offer(Take.chunk(remaining))
        _ <- drainQueue.offer(Take.end)
      } yield ()
    }

  def completeStream: UIO[Unit] =
    ZIO.logAnnotate(
      LogAnnotation("topic", topicPartition.topic()),
      LogAnnotation("partition", topicPartition.partition().toString)
    ) {
      ZIO.logDebug(s"Marked completion of partition stream") *> streamCompleted.succeed(()).unit
    }
}
