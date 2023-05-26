package zio.kafka.consumer.internal

import org.apache.kafka.common.TopicPartition
import zio._
import zio.kafka.consumer.Subscription

sealed trait RunloopCommand
object RunloopCommand {

  /** Used for internal control of the runloop. */
  sealed trait Control       extends RunloopCommand
  sealed trait StreamCommand extends RunloopCommand

  /** Used as a signal that another poll is needed. */
  case object Poll extends Control

  /** Equivalent of `continue` in a loop */
  case object Next extends Control

  case object StopRunloop    extends Control
  case object StopAllStreams extends StreamCommand

  final case class Commit(offsets: Map[TopicPartition, Long], cont: Promise[Throwable, Unit]) extends StreamCommand {
    @inline def isDone: UIO[Boolean]    = cont.isDone
    @inline def isPending: UIO[Boolean] = isDone.negate
  }

  /** Used by a stream to request more records. */
  final case class Request(tp: TopicPartition) extends StreamCommand

  final case class AddSubscription(subscription: Subscription)    extends StreamCommand
  final case class RemoveSubscription(subscription: Subscription) extends StreamCommand
  case object StopSubscription                                    extends StreamCommand
}
