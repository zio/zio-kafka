package zio.kafka.example

import zio.kafka.consumer.Consumer
import zio.kafka.testkit.KafkaTestUtils.consumer
import zio.kafka.testkit.{ Kafka, KafkaRandom }
import zio.test.{ assertTrue, Spec, TestEnvironment, ZIOSpecDefault }
import zio.{ &, Scope }

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
          metrics <- Consumer.metrics
                       .provideSome[Kafka](
                         consumer(clientId = clientId, groupId = Some(group)) // Comes from KafkaTestUtils
                       )
        } yield assertTrue(metrics.nonEmpty)
      }
    ).provideSomeShared[Scope](Kafka.embedded)
}
