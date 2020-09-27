/*
 * Copyright (C) 2014 - 2016 Softwaremill <https://softwaremill.com>
 * Copyright (C) 2016 - 2020 Lightbend Inc. <https://www.lightbend.com>
 */

package zio.kafka.benchmarks.fixtures

import org.apache.kafka.clients.producer.KafkaProducer
import zio.kafka.benchmarks.commands.RunTestCommand
import zio.kafka.benchmarks.fixtures.PerfFixtureHelpers.FilledTopic

case class KafkaProducerTestFixture(
  topic: String,
  msgCount: Int,
  msgSize: Int,
  producer: KafkaProducer[Array[Byte], String],
  numberOfPartitions: Int
) {
  def close(): Unit = producer.close()
}

object KafkaProducerFixtures extends PerfFixtureHelpers {

  def noopFixtureGen(c: RunTestCommand) = FixtureGen[KafkaProducerTestFixture](
    c,
    msgCount => KafkaProducerTestFixture("topic", msgCount, c.msgSize, null, c.numberOfPartitions)
  )

  def initializedProducer(c: RunTestCommand) = FixtureGen[KafkaProducerTestFixture](
    c,
    msgCount => {
      val ft          = FilledTopic(msgCount = 1, msgSize = c.msgSize, numberOfPartitions = c.numberOfPartitions)
      val rawProducer = createTopic(ft, c.kafkaHost)
      KafkaProducerTestFixture(ft.topic, msgCount, c.msgSize, rawProducer, c.numberOfPartitions)
    }
  )
}
