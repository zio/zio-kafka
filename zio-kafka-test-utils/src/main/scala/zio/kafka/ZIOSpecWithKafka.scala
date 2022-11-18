package zio.kafka

import zio.ZLayer
import zio.kafka.embedded.Kafka
import zio.test._

trait ZIOSpecWithKafka extends ZIOSpec[TestEnvironment with Kafka] with KafkaRandom {

  override val bootstrap: ZLayer[Any, Any, TestEnvironment with Kafka] =
    testEnvironment ++ Kafka.embedded

}
