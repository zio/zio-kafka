package zio.kafka.client

import org.apache.kafka.common.TopicPartition
import zio.Task

sealed trait Offset {
  def topicPartition: TopicPartition
  def offset: Long
  def commit: Task[Unit]
  def batch: OffsetBatch
}

private final case class OffsetImpl(
  topicPartition: TopicPartition,
  offset: Long,
  commitHandle: Map[TopicPartition, Long] => Task[Unit]
) extends Offset {
  val commit: Task[Unit] = commitHandle(Map(topicPartition    -> offset))
  val batch: OffsetBatch = OffsetBatchImpl(Map(topicPartition -> offset), commitHandle)
}
