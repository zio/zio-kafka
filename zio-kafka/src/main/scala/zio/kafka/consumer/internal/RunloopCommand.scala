package zio.kafka.consumer.internal

import org.apache.kafka.common.TopicPartition
import zio._
import zio.kafka.consumer.{ InvalidSubscriptionUnion, Subscription }

sealed trait RunloopCommand
object RunloopCommand {

  /** Used for internal control of the runloop. */
  sealed trait Control extends RunloopCommand

  /** Used for internal control of the stream of data from Kafka. */
  sealed trait StreamCommand extends RunloopCommand

  /** Used as a signal that another poll is needed. */
  case object Poll extends Control

  /** Used as a signal to the poll-loop that commits are available in the commit-queue. */
  case object CommitAvailable extends Control

  case object StopRunloop    extends Control
  case object StopAllStreams extends StreamCommand

  /** Used by a stream to request more records. */
  final case class Request(tp: TopicPartition) extends StreamCommand

  final case class AddSubscription(subscription: Subscription, cont: Promise[InvalidSubscriptionUnion, Unit])
      extends StreamCommand {
    @inline def succeed(implicit trace: Trace): UIO[Unit]                           = cont.succeed(()).unit
    @inline def fail(e: InvalidSubscriptionUnion)(implicit trace: Trace): UIO[Unit] = cont.fail(e).unit
  }
  final case class RemoveSubscription(subscription: Subscription, cont: Promise[Throwable, Unit]) extends StreamCommand

  final case class EndStreamsBySubscription(subscription: Subscription, cont: Promise[Nothing, Unit])
      extends StreamCommand {
    @inline def succeed(implicit trace: Trace): UIO[Unit] = cont.succeed(()).unit
  }

  case object RemoveAllSubscriptions extends StreamCommand
}
