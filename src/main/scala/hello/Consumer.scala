package hello

import java.util
import java.util.Properties
import org.apache.kafka.clients.consumer.KafkaConsumer
import scala.collection.JavaConverters._

object SimpleConsumer extends App {

  val TOPIC = "topic0"

  val props = new Properties()
  props.put("bootstrap.servers", "localhost:9092")

  props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
  props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
  props.put("group.id", "something")

  val consumer = new KafkaConsumer[String, String](props)

  consumer.subscribe(util.Collections.singletonList(TOPIC))

  while (true) {
    val records = consumer.poll(java.time.Duration.ofSeconds(1))
    for (record <- records.asScala) {
      println("\n\n Received message: (" + record.key() + ", " + record.value() + ") at offset " + record.offset())
    }
  }
}
