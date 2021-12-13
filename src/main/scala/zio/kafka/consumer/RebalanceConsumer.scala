package zio.kafka.consumer

import org.apache.kafka.common.TopicPartition
import org.apache.kafka.clients.consumer.OffsetAndMetadata

import zio.Task
import org.apache.kafka.clients.consumer.KafkaConsumer
import scala.jdk.CollectionConverters._

/**
 * A subset of Consumer methods available during rebalances.
 */
trait RebalanceConsumer {
  def commit(offsets: Map[TopicPartition, OffsetAndMetadata]): Task[Unit]
}

object RebalanceConsumer {
  case class Live(blocking: zio.blocking.Blocking.Service, consumer: KafkaConsumer[Array[Byte], Array[Byte]])
      extends RebalanceConsumer {
    def commit(offsets: Map[TopicPartition, OffsetAndMetadata]): Task[Unit] =
      blocking.effectBlocking(consumer.commitSync(offsets.asJava))
  }
}
