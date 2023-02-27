package zio.kafka.consumer.diagnostics

import org.apache.kafka.clients.consumer.OffsetAndMetadata
import org.apache.kafka.common.TopicPartition
import zio.kafka.consumer.internal.Runloop.Command

sealed trait DiagnosticEvent
object DiagnosticEvent {

  /**
   * Stable diagnostic events are events we consider stable so that you can use them in your apps if you want to.
   */
  sealed trait StableDiagnosticEvent extends DiagnosticEvent

  /**
   * Internal diagnostic events are events we advise you not to use/rely on in your apps. We may change/remove them at
   * any time based on our internal needs.
   */
  sealed trait InternalDiagnosticEvent extends DiagnosticEvent

  final case class Poll(
    tpRequested: Set[TopicPartition],
    tpWithData: Set[TopicPartition],
    tpWithoutData: Set[TopicPartition]
  ) extends DiagnosticEvent
  final case class Request(partition: TopicPartition) extends StableDiagnosticEvent

  sealed trait Commit extends StableDiagnosticEvent
  object Commit {
    final case class Started(offsets: Map[TopicPartition, Long])                                extends Commit
    final case class Success(offsets: Map[TopicPartition, OffsetAndMetadata])                   extends Commit
    final case class Failure(offsets: Map[TopicPartition, OffsetAndMetadata], cause: Throwable) extends Commit
  }

  sealed trait Rebalance extends StableDiagnosticEvent
  object Rebalance {
    final case class Revoked(partitions: Set[TopicPartition])  extends Rebalance
    final case class Assigned(partitions: Set[TopicPartition]) extends Rebalance
    final case class Lost(partitions: Set[TopicPartition])     extends Rebalance
  }

  final case class RunloopEvent(command: Command) extends InternalDiagnosticEvent
}
