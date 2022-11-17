package zio.kafka

import zio.kafka.embedded.Kafka
import zio.test._
import zio.{ Scope, ZLayer }

trait ZIOSpecWithSaslKafka extends ZIOSpec[TestEnvironment with Kafka.Sasl] with KafkaRandom {
  override val bootstrap: ZLayer[Scope, Any, TestEnvironment with Kafka.Sasl] =
    testEnvironment ++ Kafka.saslEmbedded
}
