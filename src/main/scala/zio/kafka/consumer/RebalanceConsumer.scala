package zio.kafka.consumer

import org.apache.kafka.common.TopicPartition
import org.apache.kafka.clients.consumer.OffsetAndMetadata

import zio.{ Task, ZIO }
import org.apache.kafka.clients.consumer.KafkaConsumer
import scala.jdk.CollectionConverters._

/**
 * A subset of Consumer methods available during rebalances.
 */
trait RebalanceConsumer {
  def commit(offsets: Map[TopicPartition, OffsetAndMetadata]): Task[Unit]
}

object RebalanceConsumer {
  case class Live(consumer: KafkaConsumer[Array[Byte], Array[Byte]]) extends RebalanceConsumer {
    def commit(offsets: Map[TopicPartition, OffsetAndMetadata]): Task[Unit] =
      ZIO.attemptBlocking(consumer.commitSync(offsets.asJava))
  }
}
