package zio.kafka.consumer.internal

import org.apache.kafka.common.TopicPartition
import zio._
import zio.kafka.consumer.Subscription

sealed trait RunloopCommand
object RunloopCommand {

  /** Used for internal control of the runloop. */
  sealed trait Control extends RunloopCommand

  /** Used for internal control of the stream of data from Kafka. */
  sealed trait StreamCommand extends RunloopCommand

  /** Used as a signal that another poll is needed. */
  case object Poll extends Control

  case object StopRunloop    extends Control
  case object StopAllStreams extends StreamCommand

  final case class Commit(offsets: Map[TopicPartition, Long], cont: Promise[Throwable, Unit]) extends StreamCommand {
    @inline def isDone: UIO[Boolean] = cont.isDone

    @inline def isPending: UIO[Boolean] = isDone.negate
  }

  /** Used by a stream to request more records. */
  final case class Request(tp: TopicPartition) extends StreamCommand

  final case class ChangeSubscription(
    subscription: Option[Subscription],
    cont: Promise[Throwable, Unit]
  ) extends StreamCommand {
    @inline def succeed: UIO[Boolean] = cont.succeed(())

    @inline def fail(throwable: Throwable): UIO[Boolean] = cont.fail(throwable)
  }
}
