package zio.kafka.consumer

import org.apache.kafka.clients.consumer.{ Consumer => JConsumer, OffsetAndMetadata, OffsetCommitCallback }
import org.apache.kafka.common.TopicPartition
import zio.{ Task, ZIO }

import scala.jdk.CollectionConverters._

/**
 * A subset of Consumer methods available during rebalances.
 */
trait RebalanceConsumer {
  def commit(offsets: Map[TopicPartition, OffsetAndMetadata]): Task[Unit]
  def commitAsync(offsets: Map[TopicPartition, OffsetAndMetadata], callback: OffsetCommitCallback): Task[Unit]
}

object RebalanceConsumer {
  final case class Live(consumer: JConsumer[Array[Byte], Array[Byte]]) extends RebalanceConsumer {
    def commit(offsets: Map[TopicPartition, OffsetAndMetadata]): Task[Unit] =
      ZIO.attemptBlocking(consumer.commitSync(offsets.asJava))
    def commitAsync(offsets: Map[TopicPartition, OffsetAndMetadata], callback: OffsetCommitCallback): Task[Unit] =
      ZIO.attempt(consumer.commitAsync(offsets.asJava, callback))
  }
}
