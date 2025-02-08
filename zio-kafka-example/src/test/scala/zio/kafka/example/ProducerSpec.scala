package zio.kafka.example

import org.apache.kafka.clients.producer.ProducerRecord
import zio._
import zio.kafka.serde.Serde
import zio.kafka.testkit.Kafka
import zio.kafka.testkit.KafkaTestUtils
import zio.test.TestAspect.{ timeout, withLiveClock }
import zio.test._

/**
 * Used to write documentation
 */
object ProducerSpec extends ZIOSpecDefault {
  override def spec: Spec[TestEnvironment & Scope, Any] =
    (
      suite("Producer test suite")(
        test("minimal example") {
          for {
            producer <- KafkaTestUtils.makeProducer
            _        <- producer.produce(new ProducerRecord("topic", "boo", "baa"), Serde.string, Serde.string)
          } yield assertCompletes
        }
        // ... more tests ...
      )
        .provideSomeShared[Scope](Kafka.embedded) // Provide an instance of Kafka for the entire suite
    ) @@ withLiveClock @@ timeout(2.minutes)
}
