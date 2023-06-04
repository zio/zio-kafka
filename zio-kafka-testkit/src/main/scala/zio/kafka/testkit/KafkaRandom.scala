package zio.kafka.testkit

import zio.{Task, ZIO}

import java.util.UUID

trait KafkaRandom {

  def kafkaPrefix: String

  def randomThing(prefix: String): Task[String] = ZIO.attempt(s"$prefix-${UUID.randomUUID()}")

  def randomTopic: Task[String] = randomThing(s"$kafkaPrefix-topic")

  def randomGroup: Task[String] = randomThing(s"$kafkaPrefix-group")

  def randomClient: Task[String] = randomThing(s"$kafkaPrefix-client")
}
