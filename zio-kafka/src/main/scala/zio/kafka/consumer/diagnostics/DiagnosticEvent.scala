package zio.kafka.consumer.diagnostics

import org.apache.kafka.clients.consumer.OffsetAndMetadata
import org.apache.kafka.common.TopicPartition
import zio.Chunk

sealed trait DiagnosticEvent
object DiagnosticEvent {

  final case class Poll(
    tpRequested: Chunk[TopicPartition],
    tpWithData: Chunk[TopicPartition],
    tpWithoutData: Chunk[TopicPartition]
  ) extends DiagnosticEvent
  final case class Request(partition: TopicPartition) extends DiagnosticEvent

  sealed trait Commit extends DiagnosticEvent
  object Commit {
    final case class Started(offsets: Map[TopicPartition, Long])                                extends Commit
    final case class Success(offsets: Map[TopicPartition, OffsetAndMetadata])                   extends Commit
    final case class Failure(offsets: Map[TopicPartition, OffsetAndMetadata], cause: Throwable) extends Commit
  }

  sealed trait Rebalance extends DiagnosticEvent
  object Rebalance {
    final case class Revoked(partitions: Chunk[TopicPartition])  extends Rebalance
    final case class Assigned(partitions: Chunk[TopicPartition]) extends Rebalance
    final case class Lost(partitions: Chunk[TopicPartition])     extends Rebalance
  }

}
