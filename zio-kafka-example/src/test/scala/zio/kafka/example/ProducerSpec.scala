package zio.kafka.example

import org.apache.kafka.clients.producer.ProducerRecord
import zio._
import zio.kafka.producer.Producer
import zio.kafka.serde.Serde
import zio.kafka.testkit.Kafka
import zio.kafka.testkit.KafkaTestUtils._
import zio.test.TestAspect.{ sequential, timeout }
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
            _ <- Producer.produce(new ProducerRecord("topic", "boo", "baa"), Serde.string, Serde.string)
          } yield assertCompletes
        }
        // ... more tests ...
      )
        .provideSome[Kafka](producer)             // Here, we provide a new instance of Producer per test
        .provideSomeShared[Scope](Kafka.embedded) // Here, we provide an instance of Kafka for the entire suite
    ) @@ timeout(2.minutes) @@ sequential
}
