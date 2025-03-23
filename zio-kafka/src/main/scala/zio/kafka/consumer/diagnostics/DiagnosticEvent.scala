package zio.kafka.consumer.diagnostics

import org.apache.kafka.clients.consumer.OffsetAndMetadata
import org.apache.kafka.common.TopicPartition

sealed trait DiagnosticEvent

object DiagnosticEvent {

  final case class Poll(
    tpRequested: Set[TopicPartition],
    tpWithData: Set[TopicPartition],
    tpWithoutData: Set[TopicPartition]
  ) extends DiagnosticEvent

  final case class Request(partition: TopicPartition) extends DiagnosticEvent

  sealed trait Commit extends DiagnosticEvent
  object Commit {
    final case class Started(offsets: Map[TopicPartition, OffsetAndMetadata])                   extends Commit
    final case class Success(offsets: Map[TopicPartition, OffsetAndMetadata])                   extends Commit
    final case class Failure(offsets: Map[TopicPartition, OffsetAndMetadata], cause: Throwable) extends Commit
  }

  final case class Rebalance(
    revoked: Set[TopicPartition],
    assigned: Set[TopicPartition],
    lost: Set[TopicPartition],
    ended: Set[TopicPartition]
  ) extends DiagnosticEvent

  case object SubscriptionFinalized extends DiagnosticEvent
  case object RunloopFinalized      extends DiagnosticEvent
  case object ConsumerFinalized     extends DiagnosticEvent

  @deprecated("Use zio.kafka.consumer.diagnostics.DiagnosticEvent instead", "2.12.0")
  sealed trait Finalization extends DiagnosticEvent

  object Finalization {
    @deprecated("Use zio.kafka.consumer.diagnostics.DiagnosticEvent.SubscriptionFinalized instead", "2.12.0")
    val SubscriptionFinalized: DiagnosticEvent.SubscriptionFinalized.type = DiagnosticEvent.SubscriptionFinalized
    @deprecated("Use zio.kafka.consumer.diagnostics.DiagnosticEvent.RunloopFinalized instead", "2.12.0")
    val RunloopFinalized: DiagnosticEvent.RunloopFinalized.type = DiagnosticEvent.RunloopFinalized
    @deprecated("Use zio.kafka.consumer.diagnostics.DiagnosticEvent.ConsumerFinalized instead", "2.12.0")
    val ConsumerFinalized: DiagnosticEvent.ConsumerFinalized.type = DiagnosticEvent.ConsumerFinalized
  }

}
