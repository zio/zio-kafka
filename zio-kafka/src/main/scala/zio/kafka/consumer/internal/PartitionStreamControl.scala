package zio.kafka.consumer.internal

import org.apache.kafka.common.TopicPartition
import zio.kafka.consumer.internal.Runloop.ByteArrayCommittableRecord
import zio.stream.{ Take, ZStream }
import zio.{ Chunk, LogAnnotation, Promise, Queue, Ref, UIO, ZIO }

private[internal] final class PartitionStreamControl private (
  val tp: TopicPartition,
  stream: ZStream[Any, Throwable, ByteArrayCommittableRecord],
  dataQueue: Queue[Take[Throwable, ByteArrayCommittableRecord]],
  interruptPromise: Promise[Throwable, Unit],
  completedPromise: Promise[Nothing, Unit],
  queueSize: Ref[Int]
) {

  private val logAnnotate = ZIO.logAnnotate(
    LogAnnotation("topic", tp.topic()),
    LogAnnotation("partition", tp.partition().toString)
  )

  /** Offer new data for the stream to process. */
  def offerRecords(data: Chunk[ByteArrayCommittableRecord]): ZIO[Any, Nothing, Unit] =
    queueSize.update(_ + data.size) *> dataQueue.offer(Take.chunk(data)).unit

  def getQueueSize: UIO[Int] = queueSize.get

  /** To be invoked when the partition was lost. */
  def lost(): UIO[Boolean] =
    interruptPromise.fail(new RuntimeException(s"Partition ${tp.toString} was lost"))

  /** To be invoked when the partition was revoked or otherwise needs to be ended. */
  def end(): ZIO[Any, Nothing, Unit] =
    logAnnotate {
      ZIO.logTrace(s"Partition ${tp.toString} ending") *>
        dataQueue.offer(Take.end).unit
    }

  /** Returns true when the stream is done. */
  def isCompleted: ZIO[Any, Nothing, Boolean] =
    completedPromise.isDone

  /** Returns true when the stream is running. */
  def isRunning: ZIO[Any, Nothing, Boolean] =
    isCompleted.negate

  /** Wait till the stream is done. */
  def awaitCompleted(): ZIO[Any, Nothing, Unit] =
    completedPromise.await

  val tpStream: (TopicPartition, ZStream[Any, Throwable, ByteArrayCommittableRecord]) =
    (tp, stream)
}

private[internal] object PartitionStreamControl {

  def newPartitionStream(
    tp: TopicPartition
  ): ZIO[Any, Nothing, PartitionStreamControl] =
    for {
      _                   <- ZIO.logTrace(s"Creating partition stream ${tp.toString}")
      interruptionPromise <- Promise.make[Throwable, Unit]
      completedPromise    <- Promise.make[Nothing, Unit]
      dataQueue           <- Queue.unbounded[Take[Throwable, ByteArrayCommittableRecord]]
      queueSize           <- Ref.make(0)

      stream = ZStream.logAnnotate(
                 LogAnnotation("topic", tp.topic()),
                 LogAnnotation("partition", tp.partition().toString)
               ) *>
                 ZStream.finalizer(
                   completedPromise.succeed(()) <*
                     ZIO.logDebug(s"Partition stream ${tp.toString} has ended")
                 ) *>
                 ZStream
                   .fromQueue(dataQueue)
                   .flattenTake
                   .chunksWith(_.tap(records => queueSize.update(_ - records.size)))
                   .interruptWhen(interruptionPromise)
    } yield new PartitionStreamControl(tp, stream, dataQueue, interruptionPromise, completedPromise, queueSize)

}
