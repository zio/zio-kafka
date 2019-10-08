package zio.kafka.client

import org.apache.kafka.common.TopicPartition
import zio.UIO

final case class RebalanceListener(
  onPartitionAssigned: Set[TopicPartition] => UIO[Unit],
  onPartitionRevoked: Set[TopicPartition] => UIO[Unit]
)

object RebalanceListener {
  val None = RebalanceListener(_ => UIO.succeed(()), _ => UIO.succeed(()))
}
