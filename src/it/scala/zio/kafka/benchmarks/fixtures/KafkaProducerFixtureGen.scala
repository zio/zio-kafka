package zio.kafka.benchmarks.fixtures

import zio.ZManaged
import zio.blocking.Blocking
import zio.kafka.benchmarks.commands.RunTestCommand
import zio.kafka.benchmarks.fixtures.PerfFixtureHelpers.FilledTopic
import zio.kafka.producer.{ Producer, ProducerSettings }

case class KafkaProducerTestFixture(
  topic: String,
  msgCount: Int,
  msgSize: Int,
  settings: ProducerSettings,
  producer: Producer.Live[Any, Array[Byte], Array[Byte]],
  numberOfPartitions: Int
)

object KafkaProducerFixtures extends PerfFixtureHelpers {

  def initializedProducer(c: RunTestCommand): ZManaged[Blocking, Throwable, FixtureGen[KafkaProducerTestFixture]] = {
    val ft = FilledTopic(msgCount = 1, msgSize = c.msgSize, numberOfPartitions = c.numberOfPartitions)
    createTopic(ft, c.kafkaHost).map { producer =>
      FixtureGen[KafkaProducerTestFixture](
        c,
        msgCount => {
          val ft       = FilledTopic(msgCount = 1, msgSize = c.msgSize, numberOfPartitions = c.numberOfPartitions)
          val settings = ProducerSettings(List(c.kafkaHost))
          KafkaProducerTestFixture(ft.topic, msgCount, c.msgSize, settings, producer, c.numberOfPartitions)
        }
      )
    }
  }
}
