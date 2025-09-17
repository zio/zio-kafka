package zio.kafka.consumer.internal

import zio._
import zio.kafka.ZIOSpecDefaultSlf4j
import zio.kafka.consumer.ConsumerSettings
import zio.test._

object RunloopConfigSpec extends ZIOSpecDefaultSlf4j {

  private val consumerSettings = ConsumerSettings(List("bootstrap"))

  override def spec: Spec[TestEnvironment with Scope, Any] =
    suite("RunloopConfigSpec")(
      test("uses defaults from java-kafka") {
        for {
          runloopConfig <- RunloopConfig(consumerSettings)
        } yield assertTrue(
          runloopConfig.maxPollRecords == 500,
          runloopConfig.maxPollInterval == 5.minutes,
          runloopConfig.maxStreamPullInterval == 5.minutes,
          runloopConfig.maxRebalanceDuration == 3.minutes
        )
      },
      test("maxStreamPullInterval defaults to maxPollInterval") {
        for {
          runloopConfig <- RunloopConfig(
                             consumerSettings.withMaxPollInterval(1.minute)
                           )
        } yield assertTrue(
          runloopConfig.maxStreamPullInterval == 1.minute
        )
      },
      test("maxRebalanceDuration defaults to 3/5 of maxPollInterval") {
        for {
          runloopConfig <- RunloopConfig(
                             consumerSettings.withMaxPollInterval(50.seconds)
                           )
        } yield assertTrue(
          runloopConfig.maxRebalanceDuration == 30.seconds
        )
      },
      test("each config can be overridden separately") {
        for {
          runloopConfig <- RunloopConfig(
                             consumerSettings
                               .withMaxPollRecords(100)
                               .withMaxPollInterval(1.minute)
                               .withMaxStreamPullInterval(2.minutes)
                               .withMaxRebalanceDuration(90.seconds)
                           )
        } yield assertTrue(
          runloopConfig.maxPollRecords == 100,
          runloopConfig.maxPollInterval == 1.minute,
          runloopConfig.maxStreamPullInterval == 2.minutes,
          runloopConfig.maxRebalanceDuration == 90.seconds
        )
      }
    )

}
