package zio.kafka.utils

import zio._
import zio.stream._

object ExtraZStreamOps {

  implicit class ZStreamOps[R, E, A](val stream: ZStream[R, E, A]) extends AnyVal {

    /**
     * Fails the stream with given error if it is not consumed (pulled) from, for some duration.
     *
     * Also see [[zio.stream.ZStream#timeoutFail]] for failing the stream doesn't _produce_ a value.
     */
    def consumeTimeoutFail[E1 >: E](e: => E1)(after: Duration): ZStream[R, E1, A] =
      // For every incoming chunk a timer is started. When the chunk is consumed, the timer is stopped by interrupting
      // it. When the timer completes, the stream gets interrupted.
      stream.via(
        ZPipeline.unwrapScoped(
          for {
            scope <- ZIO.scope
            p     <- Promise.make[E1, Unit]
          } yield {
            val timer = p.fail(e).delay(after).forkScoped.provideEnvironment(ZEnvironment[Scope](scope))
            def loop: ZChannel[Any, ZNothing, Chunk[A], Any, E, Chunk[A], Unit] =
              ZChannel.readWithCause(
                (in: Chunk[A]) =>
                  ZChannel.fromZIO(timer).flatMap { t =>
                    ZChannel.write(in) *> ZChannel.fromZIO(t.interrupt) *> loop
                  },
                (cause: Cause[ZNothing]) => ZChannel.refailCause(cause),
                (_: Any) => ZChannel.unit
              )
            ZPipeline.fromChannel(loop.interruptWhen(p))
          }
        )
      )
  }

}
