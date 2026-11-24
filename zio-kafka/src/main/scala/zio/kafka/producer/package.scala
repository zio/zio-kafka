package zio.kafka

import org.apache.kafka.clients.producer.ProducerRecord

package object producer {

  /** A Kafka `ProducerRecord` whose key and value are byte arrays. */
  type ByteRecord = ProducerRecord[Array[Byte], Array[Byte]]
}
