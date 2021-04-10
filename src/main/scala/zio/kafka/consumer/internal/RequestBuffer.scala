package zio.kafka.consumer.internal

import zio._
import zio.stream.{ UStream, ZStream }

/**
 * A queue-like construct for buffering requests. Allows to take all of the
 * contents at once, but also block when there's nothing to take (unlike zio.Queue
 * which immediately returns an empty list).
 *
 * Will be replaced once zio.Queue is enhanced with a `takeUpTo` method.
 */
private[internal] class RequestBuffer(ref: Ref[RequestBuffer.State]) {
  import RequestBuffer.State._

  def offer(request: Runloop.Request): UIO[Any] =
    ref.modify {
      case Shutdown      => ZIO.interrupt      -> Shutdown
      case Empty(notify) => notify.succeed(()) -> Filled(List(request))
      case Filled(items) => UIO.unit           -> Filled(request :: items)
    }.flatten

  def takeAll: UIO[List[Runloop.Request]] =
    Promise
      .make[Nothing, Unit]
      .flatMap { p =>
        ref.modify {
          case Shutdown          => ZIO.interrupt -> Shutdown
          case s @ Empty(notify) => (notify.await *> takeAll, s)
          case Filled(items)     => (UIO.succeed(items.reverse), Empty(p))
        }
      }
      .flatten

  def stream: UStream[List[Runloop.Request]] =
    ZStream.repeatEffectOption {
      takeAll.catchAllCause(cause => if (cause.interrupted) ZIO.fail(None) else ZIO.halt(cause))
    }

  def shutdown: UIO[Any] =
    ref.set(Shutdown)
}

private[internal] object RequestBuffer {
  sealed trait State
  object State {
    case object Shutdown                                 extends State
    case class Empty(notifyFill: Promise[Nothing, Unit]) extends State
    case class Filled(items: List[Runloop.Request])      extends State
  }

  def make: UIO[RequestBuffer] =
    Promise.make[Nothing, Unit].flatMap(p => Ref.make(State.Empty(p): State).map(new RequestBuffer(_)))
}
