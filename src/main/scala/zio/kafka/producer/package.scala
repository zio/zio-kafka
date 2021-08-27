package zio.kafka

import org.apache.kafka.clients.producer.ProducerRecord
import zio.kafka.producer.ProducerSettings.TransactionalProducerSettings

package object producer {
  type ByteRecord                    = ProducerRecord[Array[Byte], Array[Byte]]
  type TransactionalProducerSettings = TransactionalProducerSettings.Type
}
