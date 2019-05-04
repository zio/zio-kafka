package zio.kafka

import java.util.{ Map => JMap }
import org.apache.kafka.clients.consumer.{
  ConsumerRecord,
  ConsumerRecords,
  KafkaConsumer => JKafkaConsumer,
  OffsetAndMetadata
}
import org.apache.kafka.clients.producer.{ KafkaProducer => JKafkaProducer, ProducerRecord }
import org.apache.kafka.common.TopicPartition
import scalaz.zio.ZIO
import scalaz.zio.blocking.Blocking

package object client {
  type BlockingTask[A] = ZIO[Blocking, Throwable, A]

  type ByteConsumer = JKafkaConsumer[Array[Byte], Array[Byte]]
  type ByteRecord   = ConsumerRecord[Array[Byte], Array[Byte]]
  type ByteRecords  = ConsumerRecords[Array[Byte], Array[Byte]]

  type ByteProducer       = JKafkaProducer[Array[Byte], Array[Byte]]
  type ByteProducerRecord = ProducerRecord[Array[Byte], Array[Byte]]

  type OffsetMap = Map[TopicPartition, OffsetAndMetadata]

  type JOffsetMap = JMap[TopicPartition, OffsetAndMetadata]
}
