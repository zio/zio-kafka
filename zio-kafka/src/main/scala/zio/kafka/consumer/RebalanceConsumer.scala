package zio.kafka.consumer

import org.apache.kafka.clients.consumer.{Consumer => JConsumer, OffsetAndMetadata}
import org.apache.kafka.common.TopicPartition
import zio.{Task, ZIO}

import scala.jdk.CollectionConverters._

/**
 * A subset of Consumer methods available during rebalances.
 */
trait RebalanceConsumer {
  def commit(offsets: Map[TopicPartition, OffsetAndMetadata]): Task[Unit]
}

object RebalanceConsumer {
  final case class Live(consumer: JConsumer[Array[Byte], Array[Byte]]) extends RebalanceConsumer {
    def commit(offsets: Map[TopicPartition, OffsetAndMetadata]): Task[Unit] =
      ZIO.attemptBlocking(consumer.commitSync(offsets.asJava))
  }
}
