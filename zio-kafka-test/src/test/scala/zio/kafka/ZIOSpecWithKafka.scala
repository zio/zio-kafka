package zio.kafka

import zio.kafka.embedded.Kafka
import zio.test._
import zio.{ Scope, ZLayer }

trait ZIOSpecWithKafka extends ZIOSpec[TestEnvironment with Kafka] with KafkaRandom {
  override val bootstrap: ZLayer[Scope, Any, TestEnvironment with Kafka] =
    testEnvironment ++ Kafka.embedded
}
