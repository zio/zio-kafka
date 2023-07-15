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
    )(after: Duration): URIO[Scope, (Interruptor[E1], ResetTimer)] = {
      def getNow: UIO[Long] = Clock.currentTime(ChronoUnit.MILLIS)

      for {
        scope                  <- ZIO.environment[Scope]
        now                    <- getNow
        lastChunkReceivedAtRef <- Ref.make(now)
        started                <- Ref.Synchronized.make(false)
        p                      <- Promise.make[E1, Unit]
        afterAsMillis = after.toMillis
        tick =
          (getNow zip lastChunkReceivedAtRef.get).flatMap { case (now, lastExecution) =>
            val deadline = lastExecution + afterAsMillis
            if (deadline < now) p.fail(e) else ZIO.unit
          }
            .repeat(Schedule.fixed(1.second))
        startTimer = started.updateSomeZIO { case false => tick.forkScoped.provideEnvironment(scope).as(true) }
        resetTimer = startTimer *> getNow.flatMap(lastChunkReceivedAtRef.set(_))
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
      ZStream.unwrapScoped {
        for {
          (p, resetTimer) <- timer(e)(after)
        } yield stream
          .viaFunction(_.interruptWhen(p))
          .mapChunksZIO(data => resetTimer *> ZIO.succeed(data))
      }
  }

}
