package zio.kafka.example

import zio.kafka.testkit.KafkaTestUtils
import zio.kafka.testkit.{ Kafka, KafkaRandom }
import zio.test.{ assertTrue, Spec, TestEnvironment, ZIOSpecDefault }
import zio._
import zio.test.TestAspect.withLiveClock

/**
 * Used to write documentation
 */
object MyServiceSpec extends ZIOSpecDefault with KafkaRandom {
  override def kafkaPrefix: String = "my-service"

  override def spec: Spec[TestEnvironment & Scope, Any] =
    suite("MyService")(
      test("minimal example") {
        for {
          group    <- randomGroup
          clientId <- randomClient
          consumer <- KafkaTestUtils.makeConsumer(clientId = clientId, groupId = Some(group))
          metrics  <- consumer.metrics

        } yield assertTrue(metrics.nonEmpty)
      }
    )
      .provideSomeShared[Scope](Kafka.embedded) @@ withLiveClock
}
