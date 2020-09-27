/*
 * Copyright (C) 2014 - 2016 Softwaremill <https://softwaremill.com>
 * Copyright (C) 2016 - 2020 Lightbend Inc. <https://www.lightbend.com>
 */

package zio.kafka.benchmarks.fixtures

import org.apache.kafka.clients.producer.KafkaProducer
import zio.{ UIO, ZIO, ZLayer }
import zio.kafka.benchmarks.commands.RunTestCommand
import zio.kafka.benchmarks.fixtures.PerfFixtureHelpers.FilledTopic
import zio.kafka.producer.Producer.Live
import zio.kafka.producer.{ Producer, ProducerSettings }
import zio.kafka.serde.Serde

case class KafkaProducerTestFixture(
  topic: String,
  msgCount: Int,
  msgSize: Int,
  settings: ProducerSettings,
  producer: KafkaProducer[Array[Byte], Array[Byte]],
  numberOfPartitions: Int
) {
  def close(): Unit = producer.close()
  def zioProducer: ZLayer[Any, Nothing, Producer[Any, Array[Byte], String]] =
    ZIO
      .succeed(Live(producer, settings, Serde.byteArray, Serde.string))
      .toManaged(_ => UIO(producer.close(settings.closeTimeout)))
      .toLayer
}

object KafkaProducerFixtures extends PerfFixtureHelpers {

  def noopFixtureGen(c: RunTestCommand) = FixtureGen[KafkaProducerTestFixture](
    c,
    msgCount =>
      KafkaProducerTestFixture("topic", msgCount, c.msgSize, ProducerSettings(Nil), null, c.numberOfPartitions)
  )

  def initializedProducer(c: RunTestCommand) = FixtureGen[KafkaProducerTestFixture](
    c,
    msgCount => {
      val ft          = FilledTopic(msgCount = 1, msgSize = c.msgSize, numberOfPartitions = c.numberOfPartitions)
      val rawProducer = createTopic(ft, c.kafkaHost)
      val settings    = ProducerSettings(List(c.kafkaHost))
      KafkaProducerTestFixture(ft.topic, msgCount, c.msgSize, settings, rawProducer, c.numberOfPartitions)
    }
  )
}
