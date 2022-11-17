package zio.kafka

import zio.kafka.embedded.Kafka
import zio.test._
import zio.{ Scope, ZLayer }

trait ZIOSpecWithSaslKafka extends ZIOSpecWithKafka {
  override val bootstrap: ZLayer[Scope, Any, TestEnvironment with Kafka] =
    testEnvironment ++ Kafka.saslEmbedded
}
