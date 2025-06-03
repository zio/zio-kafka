package zio.kafka.testkit

import zio._

import java.util.UUID

trait KafkaRandom {

  def kafkaPrefix: String

  def randomThing(prefix: String)(implicit trace: Trace): Task[String] = ZIO.attempt(s"$prefix-${UUID.randomUUID()}")

  def randomTopic(implicit trace: Trace): Task[String] = randomThing(s"$kafkaPrefix-topic")

  def randomGroup(implicit trace: Trace): Task[String] = randomThing(s"$kafkaPrefix-group")

  def randomClient(implicit trace: Trace): Task[String] = randomThing(s"$kafkaPrefix-client")
}
