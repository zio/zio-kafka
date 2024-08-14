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

  /**
   * The partitions that were involved in a rebalance.
   *
   * Note: when a partition was assigned and immediately revoked/lost, it will occur in multiple sets.
   *
   * @param revoked
   *   the partitions that were revoked during the rebalance
   * @param assigned
   *   the partitions that were assigned during the rebalance
   * @param lost
   *   the partitions that were lost during the rebalance
   * @param ended
   *   the partition streams that were ended during the rebalance, a partition stream can be ended because it was
   *   revoked/lost or because `restartStreamsOnRebalancing` is used
   */
  final case class Rebalance(
    revoked: Set[TopicPartition],
    assigned: Set[TopicPartition],
    lost: Set[TopicPartition],
    ended: Set[TopicPartition]
  ) extends DiagnosticEvent

  sealed trait Finalization extends DiagnosticEvent
  object Finalization {
    case object SubscriptionFinalized extends Finalization
    case object RunloopFinalized      extends Finalization
    case object ConsumerFinalized     extends Finalization
  }

}
