package zio.kafka.utils

import zio._
import zio.stream._

object ExtraZStreamOps {

  implicit class ZStreamOps[R, E, A](val stream: ZStream[R, E, A]) {

    /**
     * Fails the stream with given error if it is not consumed (pulled) from for a value after d duration.
     *
     * Also see [[zio.stream.ZStream#timeoutFail]] for failing the stream doesn't _produce_ a value.
     */
    def consumeTimeoutFail[E1 >: E](e: => E1)(after: Duration): ZStream[R, E1, A] =
      stream.via(
        ZPipeline.unwrapScoped(
          for {
            scope <- ZIO.scope
            p     <- Promise.make[E1, Unit]
            timer = p.fail(e).delay(after).forkScoped.provideEnvironment(ZEnvironment[Scope](scope))
            initialTimer <- timer
          } yield {
            def loop(timerFiber: Fiber[Nothing, Boolean]): ZChannel[Any, ZNothing, Chunk[A], Any, E, Chunk[A], Unit] =
              ZChannel.readWithCause(
                (in: Chunk[A]) => ZChannel.write(in) *> ZChannel.unwrap(timerFiber.interrupt *> timer.map(loop)),
                (cause: Cause[ZNothing]) => ZChannel.refailCause(cause),
                (_: Any) => ZChannel.unit
              )
            ZPipeline.fromChannel(loop(initialTimer).interruptWhen(p))
          }
        )
      )
  }

}
