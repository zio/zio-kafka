package zio.kafka

import zio.kafka.embedded.Kafka
import zio.test._
import zio.ZLayer

trait ZIOSpecWithKafka extends ZIOSpec[TestEnvironment with Kafka] {

  override val bootstrap: ZLayer[Any, Any, TestEnvironment with Kafka] =
    testEnvironment ++ Kafka.embedded

}
