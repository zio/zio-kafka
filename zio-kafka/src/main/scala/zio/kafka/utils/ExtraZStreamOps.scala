package zio.kafka.utils

import zio._
import zio.stream._

object ExtraZStreamOps {

  // noinspection SimplifyWhenInspection
  implicit class ZStreamOps[R, E, A](val stream: ZStream[R, E, A]) extends AnyVal {

    type Interruptor[E1 >: E] = Promise[E1, Unit]
    type StartTimer           = UIO[Unit]
    type ResetTimer           = UIO[Unit]

    private def timer[E1 >: E](
      e: => E1
    )(after: Duration): URIO[Scope, (Interruptor[E1], StartTimer, ResetTimer)] =
      for {
        scope                  <- ZIO.scope
        now                    <- Clock.instant
        lastChunkReceivedAtRef <- Ref.Synchronized.make(now)
        p                      <- Promise.make[E1, Unit]
        afterAsMillis = after.toMillis
        failPromiseIfNeeded = Clock.instant.flatMap { now =>
                                lastChunkReceivedAtRef.updateZIO { lastChunkReceivedAt =>
                                  val deadline         = lastChunkReceivedAt.plusMillis(afterAsMillis)
                                  val deadlineIsPassed = deadline.isBefore(now)

                                  if (deadlineIsPassed) p.fail(e).as(lastChunkReceivedAt)
                                  else ZIO.succeed(now)
                                }
                              }
        startBackgroundInterruptorIfNot <-
          failPromiseIfNeeded
            .repeat(Schedule.fixed(1.second))
            .forkIn(scope)
            .once
        resetTimer = failPromiseIfNeeded
      } yield (p, startBackgroundInterruptorIfNot, resetTimer)

    /**
     * Fails the stream with given error if it is not consumed (pulled) from, for some duration.
     *
     * Also see [[zio.stream.ZStream#timeoutFail]] for failing the stream doesn't _produce_ a value.
     */
    def consumeTimeoutFail[E1 >: E](e: => E1)(after: Duration): ZStream[R, E1, A] =
      // For every incoming chunk a timer is started. When the chunk is consumed, the timer is stopped by interrupting
      // it. When the timer completes, the stream gets interrupted.
      ZStream.unwrapScoped {
        for {
          (p, startTimer, resetTimer) <- timer(e)(after)
        } yield stream
          .interruptWhen(p)
          .mapChunksZIO(data => startTimer *> resetTimer.as(data))
      }
  }

}
