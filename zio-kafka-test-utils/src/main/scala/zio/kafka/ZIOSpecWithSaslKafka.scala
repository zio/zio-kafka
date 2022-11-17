package zio.kafka

import zio.ZLayer
import zio.kafka.embedded.Kafka
import zio.test._

trait ZIOSpecWithSaslKafka extends ZIOSpec[TestEnvironment with Kafka.Sasl] with KafkaRandom {
  override val bootstrap: ZLayer[Any, Any, TestEnvironment with Kafka.Sasl] =
    testEnvironment ++ Kafka.saslEmbedded
}
