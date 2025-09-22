package zio.kafka.consumer.internal

import org.apache.kafka.common.TopicPartition
import zio.kafka.consumer.Consumer.ConsumerDiagnostics
import zio.kafka.consumer.Offset
import zio.kafka.consumer.internal.PartitionStreamControl.{ EndOfStream, QueueInfo }
import zio.kafka.consumer.internal.Runloop.ByteArrayCommittableRecord
import zio.stream.ZStream
import zio._
import zio.kafka.consumer.diagnostics.DiagnosticEvent

import java.util.concurrent.TimeoutException
import scala.util.control.NoStackTrace

abstract class PartitionStream {
  def tp: TopicPartition
  def queueSize: UIO[Int]
}

/**
 * Provides control and information over a stream that consumes from a partition.
 *
 * @param tp
 *   topic and partition
 * @param stream
 *   the stream
 * @param dataQueue
 *   the queue the stream reads records from, each element is a chunk of records that were offered together, and
 *   correspond to the records fetched in a single poll for this partition, `PartitionStreamControl.EndOfStream` is used
 *   to indicate the last element.
 * @param interruptionPromise
 *   a promise that when completed stops the stream
 * @param completedPromise
 *   the last pulled offset (if any). The promise completes when the stream completed.
 * @param queueInfoRef
 *   used to track the stream's pull deadline, its queue size, and last pulled offset
 * @param maxStreamPullInterval
 *   see [[zio.kafka.consumer.ConsumerSettings.withMaxStreamPullInterval()]]
 */
final class PartitionStreamControl private (
  val tp: TopicPartition,
  val stream: ZStream[Any, Throwable, ByteArrayCommittableRecord],
  dataQueue: Queue[Chunk[ByteArrayCommittableRecord]],
  interruptionPromise: Promise[Throwable, Nothing],
  val completedPromise: Promise[Nothing, Option[Offset]],
  hasEndedRef: Ref[Boolean],
  queueInfoRef: Ref[QueueInfo],
  maxStreamPullInterval: Duration
) extends PartitionStream {
  private val maxStreamPullIntervalNanos = maxStreamPullInterval.toNanos

  private val logAnnotate = ZIO.logAnnotate(
    LogAnnotation("topic", tp.topic()),
    LogAnnotation("partition", tp.partition().toString)
  )

  /** Offer new data for the stream to process. Should be called on every poll, also when `data.isEmpty` */
  private[internal] def offerRecords(data: Chunk[ByteArrayCommittableRecord]): UIO[Unit] =
    ZIO.ifZIO(hasEnded)(
      onTrue = ZIO.dieMessage("Partition stream has already ended, cannot offer more records"),
      onFalse = if (data.isEmpty) {
        queueInfoRef.update(_.withEmptyPoll)
      } else {
        for {
          now <- Clock.nanoTime
          newPullDeadline = now + maxStreamPullIntervalNanos
          _ <- queueInfoRef.update(_.withOffer(newPullDeadline, data.size))
          _ <- dataQueue.offer(data)
        } yield ()
      }
    )

  def queueSize: UIO[Int] = queueInfoRef.get.map(_.size)

  def lastPulledOffset: UIO[Option[Offset]] = queueInfoRef.get.map(_.lastPulledOffset)

  /**
   * @return
   *   the number of polls there are records idling in the queue. It is increased on every poll (when the queue is
   *   nonEmpty) and reset to 0 when the stream pulls the records
   */
  def outstandingPolls: UIO[Int] = queueInfoRef.get.map(_.outstandingPolls)

  /**
   * @param now
   *   the time as given by `Clock.nanoTime`
   * @return
   *   `true` when the stream has data available, but none has been pulled for more than `maxPollInterval` (since data
   *   became available), `false` otherwise
   */
  private[internal] def maxStreamPullIntervalExceeded(now: NanoTime): UIO[Boolean] =
    queueInfoRef.get.map(_.deadlineExceeded(now))

  /** To be invoked when the stream is no longer processing. */
  private[internal] def halt: UIO[Unit] = {
    val timeOutMessage = s"No records were pulled for more than $maxStreamPullInterval for topic partition $tp."
    val consumeTimeout = new TimeoutException(timeOutMessage) with NoStackTrace
    interruptionPromise.fail(consumeTimeout).unit
  }

  /** To be invoked when the partition was revoked, lost or otherwise needs to be ended. */
  private[internal] def end: ZIO[Any, Nothing, Unit] =
    logAnnotate {
      ZIO.logDebug(s"Partition ${tp.toString} ending") *>
        hasEndedRef.set(true) *>
        dataQueue.offer(EndOfStream).unit
    }

  /** Returns true when the stream is done. */
  private[internal] def isCompleted: ZIO[Any, Nothing, Boolean] =
    completedPromise.isDone

  /**
   * Returns true when the stream is running (the finalizer has not yet run). The stream is still considered running
   * after `end` is called but before the stream has finalized
   */
  private[internal] def isRunning: ZIO[Any, Nothing, Boolean] =
    isCompleted.negate

  /**
   * Returns true after `end` has been called.
   */
  private[internal] def hasEnded: ZIO[Any, Nothing, Boolean] =
    hasEndedRef.get

  private[internal] val tpStream: (TopicPartition, ZStream[Any, Throwable, ByteArrayCommittableRecord]) =
    (tp, stream)
}

object PartitionStreamControl {

  private val EndOfStream: Chunk[Nothing] = Chunk.empty

  private[internal] def newPartitionStream(
    tp: TopicPartition,
    requestData: UIO[Unit],
    diagnostics: ConsumerDiagnostics,
    maxStreamPullInterval: Duration
  ): UIO[PartitionStreamControl] = {
    val maxStreamPullIntervalNanos = maxStreamPullInterval.toNanos

    def registerPull(queueInfo: Ref[QueueInfo], records: Chunk[ByteArrayCommittableRecord]): UIO[Unit] =
      for {
        now <- Clock.nanoTime
        newPullDeadline = now + maxStreamPullIntervalNanos
        _ <- queueInfo.update(_.withPull(newPullDeadline, records))
      } yield ()

    for {
      _                   <- ZIO.logDebug(s"Creating partition stream ${tp.toString}")
      interruptionPromise <- Promise.make[Throwable, Nothing]
      completedPromise    <- Promise.make[Nothing, Option[Offset]]
      dataQueue           <- Queue.unbounded[Chunk[ByteArrayCommittableRecord]]
      now                 <- Clock.nanoTime
      queueInfo           <- Ref.make(QueueInfo(now, 0, None, 0))
      hasEndedRef         <- Ref.make(false)
      requestAndAwaitData =
        for {
          _     <- requestData
          _     <- diagnostics.emit(DiagnosticEvent.Request(tp))
          taken <- dataQueue.take.raceFirst(interruptionPromise.await)
        } yield taken

      stream = ZStream.logAnnotate(
                 LogAnnotation("topic", tp.topic()),
                 LogAnnotation("partition", tp.partition().toString)
               ) *>
                 ZStream.finalizer {
                   for {
                     qi <- queueInfo.get
                     _  <- completedPromise.succeed(qi.lastPulledOffset)
                     _  <- ZIO.logDebug(s"Partition stream ${tp.toString} has ended")
                   } yield ()
                 } *>
                 ZStream.repeatZIO {
                   // First try to take a chunk of records that are available right now.
                   // When no data is available, request more data and await its arrival.
                   dataQueue.poll.flatMap {
                     case None        => requestAndAwaitData
                     case Some(taken) => Exit.succeed(taken)
                   }
                 }.takeUntil(_ eq EndOfStream)
                   // Due to https://github.com/zio/zio/issues/8515 we cannot use Zstream.interruptWhen.
                   .tap(_ => interruptionPromise.await.whenZIO(interruptionPromise.isDone))
                   // When stream.end was invoked, some chunks of records may be in transit, filter those out.
                   .filterZIO(_ => hasEndedRef.get.negate)
                   .tap(registerPull(queueInfo, _))
                   .flattenChunks
    } yield new PartitionStreamControl(
      tp,
      stream,
      dataQueue,
      interruptionPromise,
      completedPromise,
      hasEndedRef,
      queueInfo,
      maxStreamPullInterval
    )
  }

  // The `pullDeadline` is only relevant when `size > 0`. We initialize `pullDeadline` as soon as size goes above 0.
  // (Note that theoretically `size` can go below 0 when the update operations are reordered.)
  private final case class QueueInfo(
    pullDeadline: NanoTime,
    size: Int,
    lastPulledOffset: Option[Offset],
    outstandingPolls: Int
  ) {
    // To be called when a poll resulted in 0 records.
    def withEmptyPoll: QueueInfo =
      copy(outstandingPolls = outstandingPolls + 1)

    // To be called when a poll resulted in >0 records.
    def withOffer(newPullDeadline: NanoTime, recordCount: Int): QueueInfo =
      QueueInfo(
        pullDeadline = if (size <= 0) newPullDeadline else pullDeadline,
        size = size + recordCount,
        lastPulledOffset = lastPulledOffset,
        outstandingPolls = outstandingPolls + 1
      )

    def withPull(newPullDeadline: NanoTime, records: Chunk[ByteArrayCommittableRecord]): QueueInfo =
      QueueInfo(
        pullDeadline = newPullDeadline,
        size = size - records.size,
        lastPulledOffset = records.lastOption.map(_.offset).orElse(lastPulledOffset),
        outstandingPolls = 0
      )

    def deadlineExceeded(now: NanoTime): Boolean =
      size > 0 && pullDeadline <= now
  }

}
