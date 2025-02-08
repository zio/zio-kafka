package zio.kafka.consumer

import org.apache.kafka.clients.consumer.OffsetAndMetadata
import org.apache.kafka.common.TopicPartition

sealed trait ConsumerDiagnosticEvent

object ConsumerDiagnosticEvent {

  final case class Poll(
    tpRequested: Set[TopicPartition],
    tpWithData: Set[TopicPartition],
    tpWithoutData: Set[TopicPartition]
  ) extends ConsumerDiagnosticEvent

  final case class Request(partition: TopicPartition) extends ConsumerDiagnosticEvent

  sealed abstract class Commit extends ConsumerDiagnosticEvent
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
  ) extends ConsumerDiagnosticEvent

  case object SubscriptionFinalized extends ConsumerDiagnosticEvent
  case object RunloopFinalized      extends ConsumerDiagnosticEvent
  case object ConsumerFinalized     extends ConsumerDiagnosticEvent

}
