package zio.kafka.utils

import zio._
import zio.stream._

import java.time.Instant
import scala.math.Ordered.orderingToOrdered

object ExtraZStreamOps {
  private sealed trait State
  private final case class WaitingForProducer(producerDone: Promise[Nothing, Unit]) extends State
  private final case class WaitingForConsumer(deadline: Instant)                    extends State
  private case object ConsumerTimedOut                                              extends State

  implicit final class ZStreamOps[R, E, A](private val stream: ZStream[R, E, A]) extends AnyVal {

    /**
     * Fails the stream with given error if it is not consumed (pulled) from, for some duration.
     *
     * Will also run workflow `onTimeout` when the timeout occurs.
     *
     * Note: since the wrapped stream only starts when pulling starts, we never time out waiting for the first pull
     *
     * Also see [[zio.stream.ZStream.timeoutFail]] for failing the stream doesn't _produce_ a value.
     */
    // noinspection SimplifySleepInspection
    def consumeTimeoutFail[E1 >: E](
      e: => E1
    )(after: Duration)(onTimeout: ZIO[R, Nothing, Any])(implicit trace: Trace): ZStream[R, E1, A] =
      ZStream.unwrapScoped[R] {
        def deadlineChecker(
          stateRef: Ref[State],
          release: ZIO[R, Nothing, Any]
        ): ZIO[R, Nothing, Any] =
          stateRef.get.flatMap {
            case WaitingForProducer(producerDone) =>
              producerDone.await *> deadlineChecker(stateRef, release)
            case WaitingForConsumer(deadline) =>
              Clock.instant.flatMap { now =>
                if (now >= deadline) stateRef.set(ConsumerTimedOut) *> release.uninterruptible
                else ZIO.sleep(Duration.fromInterval(now, deadline)) *> deadlineChecker(stateRef, release)
              }
            case ConsumerTimedOut => ZIO.unit
          }

        for {
          producerDone <- Promise.make[Nothing, Unit]
          stateRef     <- Ref.make[State](WaitingForProducer(producerDone))
          p            <- Promise.make[E1, Nothing]
          _            <- deadlineChecker(stateRef, p.fail(e) *> onTimeout).interruptible.forkScoped
        } yield {
          val timedOutState = (ZIO.fail(e), ConsumerTimedOut)
          val illegalState  = (ZIO.die(new IllegalStateException()), ConsumerTimedOut)

          val producerDone: ZIO[Any, E1, Unit] =
            Clock.instant.flatMap { now =>
              stateRef.modify {
                case WaitingForProducer(producerDone) =>
                  (producerDone.succeed(()), WaitingForConsumer(now.plus(after)))
                case ConsumerTimedOut      => timedOutState
                case WaitingForConsumer(_) => illegalState
              }.flatten.unit
            }

          val consumerDone: ZIO[Any, E1, Unit] =
            Promise.make[Nothing, Unit].flatMap { producerDone =>
              stateRef.modify {
                case WaitingForConsumer(_) => (ZIO.unit, WaitingForProducer(producerDone))
                case ConsumerTimedOut      => timedOutState
                case WaitingForProducer(_) => illegalState
              }.flatten.unit
            }

          lazy val channel: ZChannel[Any, E, Chunk[A], Any, E1, Chunk[A], Any] =
            ZChannel.readWithCause(
              in =>
                ZChannel.fromZIO(producerDone) *>
                  ZChannel.write(in) *>
                  ZChannel.fromZIO(consumerDone) *>
                  channel,
              err => ZChannel.refailCause(err),
              done => ZChannel.succeedNow(done)
            )

          ZStream.fromChannel(stream.channel.pipeTo(channel)).interruptWhen(p)
        }
      }
  }
}
