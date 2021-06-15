package zio.kafka

import org.apache.kafka.clients.producer.ProducerRecord

package object producer {
  type ByteRecord = ProducerRecord[Array[Byte], Array[Byte]]
}
