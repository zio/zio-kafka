package zio.kafka.consumer.internal

import org.apache.kafka.common.TopicPartition
import zio.kafka.consumer.Offset
import zio.kafka.consumer.diagnostics.{ DiagnosticEvent, Diagnostics }
import zio.kafka.consumer.internal.Runloop.ByteArrayCommittableRecord
import zio.stream.{ Take, ZStream }
import zio.{ Chunk, LogAnnotation, Promise, Queue, Ref, UIO, ZIO }

private[internal] final class PartitionStreamControl private (
  val tp: TopicPartition,
  stream: ZStream[Any, Throwable, ByteArrayCommittableRecord],
  val lastOffset: Ref[Option[Offset]],
  dataQueue: Queue[Take[Throwable, ByteArrayCommittableRecord]],
  startedPromise: Promise[Nothing, Unit],
  endedPromise: Promise[Nothing, Unit],
  completedPromise: Promise[Nothing, Unit],
  interruptPromise: Promise[Throwable, Unit]
) {

  private var pollResumedHistory: PollHistory = PollHistory.Empty

  private val logAnnotate = ZIO.logAnnotate(
    LogAnnotation("topic", tp.topic()),
    LogAnnotation("partition", tp.partition().toString)
  )

  /** Offer new data for the stream to process. */
  def offerRecords(data: Chunk[ByteArrayCommittableRecord]): ZIO[Any, Nothing, Unit] =
    data.lastOption.fold(ZIO.unit)(last => lastOffset.set(Some(last.offset))) *>
      dataQueue.offer(Take.chunk(data)).unit

  /** To be invoked when the partition was lost. */
  def lost(): UIO[Boolean] =
    interruptPromise.fail(new RuntimeException(s"Partition ${tp.toString} was lost"))

  /** To be invoked when the partition was revoked or otherwise needs to be ended. */
  def end(): ZIO[Any, Nothing, Unit] =
    logAnnotate {
      ZIO.logTrace(s"Partition ${tp.toString} ending") *>
        ZIO
          .whenZIO(endedPromise.succeed(())) {
            dataQueue.offer(Take.end)
          }
          .unit
    }

  /** Returns true when the stream accepts new data. */
  def acceptsData: ZIO[Any, Nothing, Boolean] =
    for {
      ended       <- endedPromise.isDone
      completed   <- completedPromise.isDone
      interrupted <- interruptPromise.isDone
    } yield !(ended || completed || interrupted)

  /** Returns true when the stream is done (or when it didn't even start). */
  def isCompletedAfterStart: ZIO[Any, Nothing, Boolean] =
    for {
      started   <- startedPromise.isDone
      completed <- completedPromise.isDone
    } yield !started || completed

  def lasOffsetIsIn(committedOffsets: Map[TopicPartition, Long]): ZIO[Any, Nothing, Boolean] =
    lastOffset.get.map(_.forall(offset => committedOffsets.get(offset.topicPartition).exists(_ >= offset.offset))).tap {
      result =>
        for {
          lo <- lastOffset.get
          _ <- ZIO.logDebug(
                 s"${tp.partition()} lastOffset: ${lo.map(_.offset.toString).getOrElse("-")} " +
                   s"in committedOffsets: ${committedOffsets.get(tp).map(_.toString).getOrElse("-")} " +
                   s"==> $result"
               )
        } yield ()
    }

  val tpStream: (TopicPartition, ZStream[Any, Throwable, ByteArrayCommittableRecord]) =
    (tp, stream)

  def optimisticResume: Boolean = pollResumedHistory.optimisticResume

  /**
   * Add a poll event to the poll history.
   *
   * Warning: this method is not multi-thread safe.
   *
   * @param resumed
   *   true when this stream was resumed before the poll, false when it was paused
   */
  def addPollHistory(resumed: Boolean): Unit =
    pollResumedHistory = pollResumedHistory.addPollHistory(resumed)

}

private[internal] object PartitionStreamControl {

  def newPartitionStream(
    tp: TopicPartition,
    commandQueue: Queue[RunloopCommand],
    diagnostics: Diagnostics
  ): ZIO[Any, Nothing, PartitionStreamControl] =
    for {
      _                   <- ZIO.logTrace(s"Creating partition stream ${tp.toString}")
      startedPromise      <- Promise.make[Nothing, Unit]
      endedPromise        <- Promise.make[Nothing, Unit]
      completedPromise    <- Promise.make[Nothing, Unit]
      interruptionPromise <- Promise.make[Throwable, Unit]
      lastOffset          <- Ref.make[Option[Offset]](None)
      dataQueue           <- Queue.unbounded[Take[Throwable, ByteArrayCommittableRecord]]
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
                 ZStream.fromZIO(startedPromise.succeed(())) *>
                 ZStream.repeatZIOChunk {
                   // First try to take all records that are available right now.
                   // When no data is available, request more data and await its arrival.
                   dataQueue.takeAll.flatMap(data => if (data.isEmpty) requestAndAwaitData else ZIO.succeed(data))
                 }.flattenTake
                   .interruptWhen(interruptionPromise)
    } yield new PartitionStreamControl(
      tp,
      stream,
      lastOffset,
      dataQueue,
      startedPromise,
      endedPromise,
      completedPromise,
      interruptionPromise
    )

}
