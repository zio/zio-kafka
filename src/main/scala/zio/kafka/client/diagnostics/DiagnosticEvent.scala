package zio.kafka.client.diagnostics

import org.apache.kafka.clients.consumer.OffsetAndMetadata
import org.apache.kafka.common.TopicPartition

sealed trait DiagnosticEvent
object DiagnosticEvent {
  case class Poll(tpRequested: Set[TopicPartition], tpWithData: Set[TopicPartition], tpWithoutData: Set[TopicPartition])
      extends DiagnosticEvent
  case class Request(partition: TopicPartition) extends DiagnosticEvent

  sealed trait Commit extends DiagnosticEvent
  object Commit {
    case class Started(offsets: Map[TopicPartition, Long])              extends Commit
    case class Success(offsets: Map[TopicPartition, OffsetAndMetadata]) extends Commit
    case class Failure(offsets: Map[TopicPartition, OffsetAndMetadata]) extends Commit
  }

  sealed trait Rebalance extends DiagnosticEvent
  object Rebalance {
    case class Revoked(partitions: Set[TopicPartition])  extends Rebalance
    case class Assigned(partitions: Set[TopicPartition]) extends Rebalance
  }
}
