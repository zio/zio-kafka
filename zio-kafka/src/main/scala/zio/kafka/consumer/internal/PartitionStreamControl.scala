package zio.kafka.consumer.internal

import org.apache.kafka.common.TopicPartition
import zio.kafka.consumer.diagnostics.{ DiagnosticEvent, Diagnostics }
import zio.kafka.consumer.internal.Runloop.Command.Request
import zio.{ Chunk, LogAnnotation, Promise, Queue, UIO, ZIO }
import zio.kafka.consumer.internal.Runloop.{ ByteArrayCommittableRecord, Command }
import zio.stream.{ Take, ZStream }

private[internal] class PartitionStreamControl private (
  val tp: TopicPartition,
  val stream: ZStream[Any, Throwable, ByteArrayCommittableRecord],
  dataQueue: Queue[Take[Throwable, ByteArrayCommittableRecord]],
  interruptPromise: Promise[Throwable, Unit],
  completedPromise: Promise[Throwable, Unit]
) {

  /** Offer new data for the stream to process. */
  def offerRecords(data: Chunk[ByteArrayCommittableRecord]): ZIO[Any, Nothing, Unit] =
    dataQueue.offer(Take.chunk(data)).unit

  /** To be invoked when the partition was lost. */
  def lost(): UIO[Boolean] =
    interruptPromise.fail(new RuntimeException(s"Partition ${tp.toString} was lost"))

  /** To be invoked when the partition was revoked or otherwise needs to be ended. */
  def end(): ZIO[Any, Nothing, Unit] =
    ZIO.logTrace(s"Partition ${tp.toString} ending") *>
      dataQueue.offer(Take.end).unit

  /** To be invoked when the partition was revoked or otherwise needs to be ended, after the last data is processed. */
  def endWith(data: Chunk[ByteArrayCommittableRecord]): ZIO[Any, Nothing, Unit] =
    ZIO.logTrace(s"Partition ${tp.toString} ending after ${data.size} records") *> {
      if (data.isEmpty) {
        dataQueue.offer(Take.end).unit
      } else {
        dataQueue.offerAll(List(Take.chunk(data), Take.end)).unit
      }
    }

  /** Returns true when the stream is done. */
  def isCompleted: ZIO[Any, Nothing, Boolean] =
    completedPromise.isDone

  /** Wait till the stream is done. */
  def awaitCompleted(): ZIO[Any, Throwable, Unit] =
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
      completedPromise    <- Promise.make[Throwable, Unit]
      dataQueue           <- Queue.unbounded[Take[Throwable, ByteArrayCommittableRecord]]
      stream = ZStream.logAnnotate(
                 LogAnnotation("topic", tp.topic()),
                 LogAnnotation("partition", tp.partition().toString)
               ) *>
                 ZStream.finalizer(
                   completedPromise.succeed(()) *>
                     // TODO: I'd like to shutdown the queue here, however, for some reason runloop is still interacting with it during shutdown
                     // dataQueue.shutdown <*
                     ZIO.logDebug(s"Partition stream ${tp.toString} has ended")
                 ) *>
                 ZStream
                   .paginateChunkZIO(false) { completed =>
                     if (completed) ZIO.succeed((Chunk.empty, None))
                     else {
                       // First try to take all records that are available right now.
                       // When no data is available, request more data and await its arrival.
                       dataQueue.takeAll
                         .filterOrElse(_.nonEmpty) {
                           for {
                             _     <- commandQueue.offer(Request(tp, dataQueue)).unit
                             _     <- diagnostics.emitIfEnabled(DiagnosticEvent.Request(tp))
                             taken <- dataQueue.takeBetween(1, Int.MaxValue)
                           } yield taken
                         }
                         // Extract the success values and potential end-of-stream value
                         .map { takes =>
                           val (okTakes, endTakes) = takes.splitWhere(!_.isSuccess)
                           val records = okTakes
                             .map(
                               _.fold(
                                 end = Chunk.empty[ByteArrayCommittableRecord],
                                 error = _ => Chunk.empty[ByteArrayCommittableRecord],
                                 value = identity
                               )
                             )
                             .foldLeft(Chunk.empty[ByteArrayCommittableRecord])(_ ++ _)
                           val isComplete = endTakes.nonEmpty
                           (records, Some(isComplete))
                         }
                     }
                   }
                   .interruptWhen(interruptionPromise)
    } yield new PartitionStreamControl(tp, stream, dataQueue, interruptionPromise, completedPromise)

}
