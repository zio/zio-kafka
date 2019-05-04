package zio.kafka.client

import org.apache.kafka.common.TopicPartition

sealed trait Rebalance
object Rebalance {
  case class Assign(partitions: List[TopicPartition]) extends Rebalance
  case class Revoke(partitions: List[TopicPartition]) extends Rebalance
}
