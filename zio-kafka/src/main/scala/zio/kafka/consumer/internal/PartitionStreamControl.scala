package zio.kafka.consumer.internal

import org.apache.kafka.common.TopicPartition
import zio.kafka.consumer.diagnostics.{ DiagnosticEvent, Diagnostics }
import zio.kafka.consumer.internal.PartitionStreamControl.QueueInfo
import zio.kafka.consumer.internal.Runloop.ByteArrayCommittableRecord
import zio.stream.{ Take, ZStream }
import zio.{ Chunk, Clock, Duration, LogAnnotation, Promise, Queue, Ref, UIO, ZIO }

import java.util.concurrent.TimeoutException
import scala.util.control.NoStackTrace

final class PartitionStreamControl private (
  val tp: TopicPartition,
  stream: ZStream[Any, Throwable, ByteArrayCommittableRecord],
  dataQueue: Queue[Take[Throwable, ByteArrayCommittableRecord]],
  interruptionPromise: Promise[Throwable, Unit],
  completedPromise: Promise[Nothing, Unit],
  queueInfoRef: Ref[QueueInfo],
  maxPollInterval: Duration
) {
  private val maxPollIntervalNanos = maxPollInterval.toNanos

  private val logAnnotate = ZIO.logAnnotate(
    LogAnnotation("topic", tp.topic()),
    LogAnnotation("partition", tp.partition().toString)
  )

  /** Offer new data for the stream to process. */
  private[internal] def offerRecords(data: Chunk[ByteArrayCommittableRecord]): ZIO[Any, Nothing, Unit] =
    for {
      now <- Clock.nanoTime
      newPullDeadline = now + maxPollIntervalNanos
      _ <- queueInfoRef.update(_.withOffer(newPullDeadline, data.size))
      _ <- dataQueue.offer(Take.chunk(data))
    } yield ()

  def queueSize: UIO[Int] = queueInfoRef.get.map(_.size)

  /**
   * @return
   *   `true` when the stream has data available, but none has been pulled for more than `maxPollInterval` (since data
   *   became available), `false` otherwise
   */
  private[internal] def maxPollIntervalExceeded: UIO[Boolean] =
    for {
      now       <- Clock.nanoTime
      queueInfo <- queueInfoRef.get
    } yield queueInfo.deadlineExceeded(now)

  /** To be invoked when the partition was lost. */
  private[internal] def lost: UIO[Boolean] =
    interruptionPromise.fail(new RuntimeException(s"Partition ${tp.toString} was lost"))

  /** To be invoked when the stream is no longer processing. */
  private[internal] def halt: UIO[Boolean] = {
    val timeOutMessage = s"No records were polled for more than $maxPollInterval for topic partition $tp. " +
      "Use ConsumerSettings.withMaxPollInterval to set a longer interval if processing a batch of records " +
      "needs more time."
    val consumeTimeout = new TimeoutException(timeOutMessage) with NoStackTrace
    interruptionPromise.fail(consumeTimeout)
  }

  /** To be invoked when the partition was revoked or otherwise needs to be ended. */
  private[internal] def end: ZIO[Any, Nothing, Unit] =
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

  private type NanoTime = Long

  private[internal] def newPartitionStream(
    tp: TopicPartition,
    commandQueue: Queue[RunloopCommand],
    diagnostics: Diagnostics,
    maxPollInterval: Duration
  ): UIO[PartitionStreamControl] = {
    val maxPollIntervalNanos = maxPollInterval.toNanos

    def registerPull(queueInfo: Ref[QueueInfo], recordCount: Int): UIO[Unit] =
      for {
        now <- Clock.nanoTime
        newPullDeadline = now + maxPollIntervalNanos
        _ <- queueInfo.update(_.withPull(newPullDeadline, recordCount))
      } yield ()

    for {
      _                   <- ZIO.logDebug(s"Creating partition stream ${tp.toString}")
      interruptionPromise <- Promise.make[Throwable, Unit]
      completedPromise    <- Promise.make[Nothing, Unit]
      dataQueue           <- Queue.unbounded[Take[Throwable, ByteArrayCommittableRecord]]
      now                 <- Clock.nanoTime
      queueInfo           <- Ref.make(QueueInfo(now, 0))
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
                   .chunksWith(_.tap(records => registerPull(queueInfo, records.size)))
                   .interruptWhen(interruptionPromise)
    } yield new PartitionStreamControl(
      tp,
      stream,
      dataQueue,
      interruptionPromise,
      completedPromise,
      queueInfo,
      maxPollInterval
    )
  }

  // The `pullDeadline` is only relevant when `size > 0`. We initialize `pullDeadline` as soon as size goes above 0.
  // (Note that theoretically `size` can go below 0 when the update operations are reordered.)
  private final case class QueueInfo(pullDeadline: NanoTime, size: Int) {
    def withOffer(newPullDeadline: NanoTime, recordCount: Int): QueueInfo =
      QueueInfo(if (size <= 0) newPullDeadline else pullDeadline, size + recordCount)

    def withPull(newPullDeadline: NanoTime, recordCount: Int): QueueInfo =
      QueueInfo(newPullDeadline, size - recordCount)

    def deadlineExceeded(now: NanoTime): Boolean =
      size > 0 && pullDeadline <= now
  }

}
