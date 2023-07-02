package zio.kafka.consumer.internal

import org.apache.kafka.common.TopicPartition
import zio.kafka.consumer.diagnostics.{ DiagnosticEvent, Diagnostics }
import zio.kafka.consumer.internal.Runloop.ByteArrayCommittableRecord
import zio.stream.{ Take, ZStream }
import zio.{ Chunk, Duration, LogAnnotation, Promise, Queue, Ref, UIO, ZIO }

import scala.concurrent.TimeoutException

final class PartitionStreamControl private (
  val tp: TopicPartition,
  stream: ZStream[Any, Throwable, ByteArrayCommittableRecord],
  dataQueue: Queue[Take[Throwable, ByteArrayCommittableRecord]],
  interruptionPromise: Promise[Throwable, Unit],
  completedPromise: Promise[Nothing, Unit],
  queueSizeRef: Ref[Int]
) {

  private val logAnnotate = ZIO.logAnnotate(
    LogAnnotation("topic", tp.topic()),
    LogAnnotation("partition", tp.partition().toString)
  )

  /** Offer new data for the stream to process. */
  private[internal] def offerRecords(data: Chunk[ByteArrayCommittableRecord]): ZIO[Any, Nothing, Unit] =
    queueSizeRef.update(_ + data.size) *> dataQueue.offer(Take.chunk(data)).unit

  def queueSize: UIO[Int] = queueSizeRef.get

  /** To be invoked when the partition was lost. */
  private[internal] def lost(): UIO[Boolean] =
    interruptionPromise.fail(new RuntimeException(s"Partition ${tp.toString} was lost"))

  /** To be invoked when the partition was revoked or otherwise needs to be ended. */
  private[internal] def end(): ZIO[Any, Nothing, Unit] =
    logAnnotate {
      ZIO.logDebug(s"Partition ${tp.toString} ending") *>
        dataQueue.offer(Take.end).unit
    }

  /** Returns true when the stream is done. */
  private[internal] def isCompleted: ZIO[Any, Nothing, Boolean] =
    completedPromise.isDone

  /** Returns true when the stream is running. */
  private[internal] def isRunning: ZIO[Any, Nothing, Boolean] =
    isCompleted.negate

  private[internal] val tpStream: (TopicPartition, ZStream[Any, Throwable, ByteArrayCommittableRecord]) =
    (tp, stream)
}

object PartitionStreamControl {

  private[internal] def newPartitionStream(
    tp: TopicPartition,
    commandQueue: Queue[RunloopCommand],
    diagnostics: Diagnostics,
    maxPollInterval: Duration
  ): UIO[PartitionStreamControl] =
    for {
      _                   <- ZIO.logDebug(s"Creating partition stream ${tp.toString}")
      interruptionPromise <- Promise.make[Throwable, Unit]
      completedPromise    <- Promise.make[Nothing, Unit]
      dataQueue           <- Queue.unbounded[Take[Throwable, ByteArrayCommittableRecord]]
      queueSize           <- Ref.make(0)
      requestAndAwaitData =
        for {
          _     <- commandQueue.offer(RunloopCommand.Request(tp))
          _     <- diagnostics.emit(DiagnosticEvent.Request(tp))
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
                   .chunksWith(_.tap(records => queueSize.update(_ - records.size)))
                   .interruptWhen(interruptionPromise)
                   .pollTimeoutFail(
                     new TimeoutException(
                       s"No records were polled for more than $maxPollInterval, aborting the stream. Set kafka configuration 'max.poll.interval.ms' to a higher value if processing a batch of records needs more time."
                     )
                   )(maxPollInterval)
    } yield new PartitionStreamControl(tp, stream, dataQueue, interruptionPromise, completedPromise, queueSize)

  implicit private class ZStreamOps[R, E, A](val stream: ZStream[R, E, A]) {
    /**
     * Fails the stream with given error if it is not polled for a value after d duration.
     *
     * See [[zio.stream.ZStream#timeoutFail]] for failing the stream doesn't produce a value.
     */
    def pollTimeoutFail[E1 >: E](e: E1)(timeout: Duration): ZStream[R, E1, A] =
      ZStream.unwrap(
        for {
          timeoutReached <- Promise.make[E1, A]
          queue          <- Queue.bounded[Unit](1)
        } yield {
          val timeoutStream = ZStream
            .fromQueue(queue)
            .timeoutFail(e)(timeout)
            .tapError(timeoutReached.fail)
          val pollStream = stream.chunks
            .tap(_ => queue.offer(()).unit)
            .flattenChunks
            .interruptWhen(timeoutReached)
          timeoutStream *> pollStream
        }
      )
  }

}
