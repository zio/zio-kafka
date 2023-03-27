package zio.kafka.consumer.internal

import org.apache.kafka.common.TopicPartition
import zio.kafka.consumer.diagnostics.{ DiagnosticEvent, Diagnostics }
import zio.kafka.consumer.internal.Runloop.Command.Request
import zio.kafka.consumer.internal.Runloop.{ ByteArrayCommittableRecord, Command }
import zio.stream.{ Take, ZStream }
import zio.{ Chunk, LogAnnotation, Promise, Queue, UIO, ZIO }

private[internal] final class PartitionStreamControl private (
  val tp: TopicPartition,
  stream: ZStream[Any, Throwable, ByteArrayCommittableRecord],
  dataQueue: Queue[Take[Throwable, ByteArrayCommittableRecord]],
  interruptPromise: Promise[Throwable, Unit],
  completedPromise: Promise[Nothing, Unit]
) {

  private val logAnnotate = ZIO.logAnnotate(
    LogAnnotation("topic", tp.topic()),
    LogAnnotation("partition", tp.partition().toString)
  )

  /** Offer new data for the stream to process. */
  def offerRecords(data: Chunk[ByteArrayCommittableRecord]): ZIO[Any, Nothing, Unit] =
    dataQueue.offer(Take.chunk(data)).unit

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
    tp: TopicPartition,
    commandQueue: Queue[Command],
    diagnostics: Diagnostics
  ): ZIO[Any, Nothing, PartitionStreamControl] =
    for {
      _                   <- ZIO.logTrace(s"Creating partition stream ${tp.toString}")
      interruptionPromise <- Promise.make[Throwable, Unit]
      completedPromise    <- Promise.make[Nothing, Unit]
      dataQueue           <- Queue.unbounded[Take[Throwable, ByteArrayCommittableRecord]]
      requestAndAwaitData =
        for {
          _     <- commandQueue.offer(Request(tp))
          _     <- diagnostics.emitIfEnabled(DiagnosticEvent.Request(tp))
          taken <- dataQueue.takeBetween(1, Int.MaxValue)
        } yield taken

      stream = ZStream.logAnnotate(
                 LogAnnotation("topic", tp.topic()),
                 LogAnnotation("partition", tp.partition().toString)
               ) *>
                 ZStream.finalizer(
                   completedPromise.succeed(()) <*
                     ZIO.logDebug(s"Partition stream ${tp.toString} has ended")
                 ) *>
                 ZStream.repeatZIOChunk {
                   // First try to take all records that are available right now.
                   // When no data is available, request more data and await its arrival.
                   dataQueue.takeAll.flatMap(data => if (data.isEmpty) requestAndAwaitData else ZIO.succeed(data))
                 }.flattenTake
                   .interruptWhen(interruptionPromise)
    } yield new PartitionStreamControl(tp, stream, dataQueue, interruptionPromise, completedPromise)

}
