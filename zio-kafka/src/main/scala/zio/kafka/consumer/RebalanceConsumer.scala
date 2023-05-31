package zio.kafka.consumer

import org.apache.kafka.clients.consumer.OffsetAndMetadata
import org.apache.kafka.common.TopicPartition
import zio.kafka.consumer.internal.ConsumerAccess
import zio.{ Task, ZIO }

import scala.jdk.CollectionConverters._

/**
 * A subset of Consumer methods available during rebalances.
 */
trait RebalanceConsumer {
  def commit(offsets: Map[TopicPartition, OffsetAndMetadata]): Task[Unit]
}

object RebalanceConsumer {
  final case class Live(consumerAccess: ConsumerAccess) extends RebalanceConsumer {
    def commit(offsets: Map[TopicPartition, OffsetAndMetadata]): Task[Unit] =
      consumerAccess.withConsumerZIO(c => ZIO.attemptBlocking(c.commitSync(offsets.asJava)))
  }
}
