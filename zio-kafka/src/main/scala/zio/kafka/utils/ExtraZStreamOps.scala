package zio.kafka.utils

import zio._
import zio.stream._

object ExtraZStreamOps {

  implicit final class ZStreamOps[R, E, A](private val stream: ZStream[R, E, A]) extends AnyVal {

    /**
     * Fails the stream with given error if it is not consumed (pulled) from, for some duration.
     *
     * Note: since the wrapped stream only starts when pulling starts, we never time out waiting for the first pull
     *
     * Also see [[zio.stream.ZStream.timeoutFail]] for failing the stream doesn't _produce_ a value.
     */
    def consumeTimeoutFail[E1 >: E](e: => E1)(after: Duration): ZStream[R, E1, A] =
      ZStream.unwrap(
        for {
          streamStart <- Clock.nanoTime
          pullEndRef  <- Ref.make(streamStart)
        } yield {
          val pull: ZIO[Scope with R, Nothing, ZIO[R, Option[E1], Chunk[A]]] =
            stream.toPull.map { pull =>
              for {
                pullEnd    <- pullEndRef.get
                now        <- Clock.nanoTime
                pullResult <- if ((now - pullEnd).nanos < after) pull else ZIO.fail(Some(e))
                _          <- Clock.nanoTime.flatMap(now => pullEndRef.set(now).unit)
              } yield pullResult
            }
          ZStream.fromPull[R, E1, A](pull)
        }
      )
  }

}
