package zio.kafka

import zio.{ Scope, Task, ZIOAppArgs, ZLayer }
import zio.kafka.embedded.Kafka
import zio.test.{ TestEnvironment, ZIOSpec }

import java.util.UUID

trait ZIOSpecWithKafka extends ZIOSpec[TestEnvironment with Kafka] {

  def kafkaPrefix: String

  override val layer: ZLayer[ZIOAppArgs with Scope, Any, TestEnvironment with Kafka] =
    (zio.ZEnv.live >>> TestEnvironment.live) ++ Kafka.embedded

  def randomThing(prefix: String): Task[String] =
    Task.attempt(UUID.randomUUID()).map(uuid => s"$prefix-$uuid")

  def randomTopic: Task[String] = randomThing(s"$kafkaPrefix-topic")

  def randomGroup: Task[String] = randomThing(s"$kafkaPrefix-group")

}
