package zio.kafka.utils

import zio._
import zio.stream._

import java.time.temporal.ChronoUnit

object ExtraZStreamOps {

  // noinspection SimplifyWhenInspection
  implicit class ZStreamOps[R, E, A](val stream: ZStream[R, E, A]) extends AnyVal {

    type Interruptor[E1 >: E] = Promise[E1, Unit]
    type ResetTimer           = UIO[Unit]

    private def timer[E1 >: E](
      e: => E1
    )(after: Duration): UIO[(Interruptor[E1], ResetTimer)] = {
      def getNow: UIO[Long] = Clock.currentTime(ChronoUnit.MILLIS)

      for {
        now                    <- getNow
        lastChunkReceivedAtRef <- Ref.Synchronized.make(now)
        p                      <- Promise.make[E1, Unit]
        afterAsMillis = after.toMillis
        failIfNeeded = (now: Long) =>
                         lastChunkReceivedAtRef.updateZIO { lastChunkReceivedAt =>
                           val deadline         = lastChunkReceivedAt + afterAsMillis
                           val deadlineIsPassed = deadline < now

                           if (deadlineIsPassed) p.fail(e).as(lastChunkReceivedAt)
                           else ZIO.succeed(now)
                         }
        resetTimer = getNow.flatMap(now => failIfNeeded(now)).unit
      } yield (p, resetTimer)
    }

    /**
     * Fails the stream with given error if it is not consumed (pulled) from, for some duration.
     *
     * Also see [[zio.stream.ZStream#timeoutFail]] for failing the stream doesn't _produce_ a value.
     */
    def consumeTimeoutFail[E1 >: E](e: => E1)(after: Duration): ZStream[R, E1, A] =
      // For every incoming chunk a timer is started. When the chunk is consumed, the timer is stopped by interrupting
      // it. When the timer completes, the stream gets interrupted.
      ZStream.unwrap {
        for {
          (p, resetTimer) <- timer(e)(after)
        } yield stream
          .interruptWhen(p)
          .mapChunksZIO(resetTimer.as(_))
      }
  }

}
