package zio.kafka

import zio.{ Scope, ZIOAppArgs, ZLayer }
import zio.kafka.embedded.Kafka
import zio.test.{ TestEnvironment, ZIOSpec }

trait ZIOSpecWithKafka extends ZIOSpec[TestEnvironment with Kafka] {

  override val layer: ZLayer[ZIOAppArgs with Scope, Any, TestEnvironment with Kafka] =
    (zio.ZEnv.live >>> TestEnvironment.live) ++ Kafka.embedded

}
