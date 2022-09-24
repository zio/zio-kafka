package zio.kafka

import zio.kafka.embedded.Kafka
import zio.test._
import zio.{ Scope, Task, ZIO, ZLayer }

import java.util.UUID

trait ZIOSpecWithKafka extends ZIOSpec[TestEnvironment with Kafka] {

  def kafkaPrefix: String

  override val bootstrap: ZLayer[Scope, Any, TestEnvironment with Kafka] =
    testEnvironment ++ Kafka.embedded

  def randomThing(prefix: String): Task[String] =
    ZIO.attempt(UUID.randomUUID()).map(uuid => s"$prefix-$uuid")

  def randomTopic: Task[String] = randomThing(s"$kafkaPrefix-topic")

  def randomGroup: Task[String] = randomThing(s"$kafkaPrefix-group")

  def randomClient: Task[String] = randomThing(s"$kafkaPrefix-client")
}
