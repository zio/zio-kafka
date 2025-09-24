package zio.kafka.consumer

import zio._
import zio.kafka.ZIOSpecDefaultSlf4j
import zio.test._
import zio.test.Assertion._

object ConsumerSettingsSpec extends ZIOSpecDefaultSlf4j {

  override def spec: Spec[TestEnvironment with Scope, Throwable] =
    suite("ConsumerSettingsSpec")(
      test("accepts no auto.commit") {
        ConsumerSettings(List("host")).validate() *> assertCompletesZIO
      },
      test("accepts disabled auto.commit") {
        ConsumerSettings(List("host")).withProperty("enable.auto.commit", "false").validate() *> assertCompletesZIO
      },
      test("rejects auto.commit") {
        val settings = ConsumerSettings(List("host")).withProperty("enable.auto.commit", "true").validate().exit
        assertZIO(settings)(
          failsWithA[IllegalArgumentException] &&
            fails(
              hasMessage(
                equalTo(
                  "Invalid settings: Because zio-kafka does pre-fetching, auto commit is not supported. Please do not set config enable.auto.commit."
                )
              )
            )
        )
      },
      test("accepts commitTimeout > 2 * pollTimeout") {
        Seq(2.minutes + 1.milli, 10.minutes).map { commitTimeout =>
          ConsumerSettings(List("host"))
            .withCommitTimeout(commitTimeout)
            .withPollTimeout(1.minute)
            .validate() *> assertCompletesZIO
        }
          .reduce(_ && _)
      },
      test("rejects commitTimeout <= 2 * pollTimeout") {
        Seq(15.seconds, 2.minutes).map { commitTimeout =>
          val settings = ConsumerSettings(List("host"))
            .withCommitTimeout(commitTimeout)
            .withPollTimeout(1.minute)
            .validate()
            .exit
          assertZIO(settings)(
            failsWithA[IllegalArgumentException] &&
              fails(
                hasMessage(
                  equalTo(
                    s"Invalid settings: Commit timeout must be larger than 2 * pollTimeout, saw commitTimeout ${commitTimeout} and pollTimeout PT1M."
                  )
                )
              )
          )
        }
          .reduce(_ && _)
      },
      test("rejects with multiple problems") {
        val settings = ConsumerSettings(List("host"))
          .withProperty("enable.auto.commit", "true")
          .withCommitTimeout(15.seconds)
          .withPollTimeout(1.minute)
          .validate()
          .exit
        assertZIO(settings)(
          failsWithA[IllegalArgumentException] &&
            fails(
              hasMessage(
                equalTo(
                  "Invalid settings: " +
                    "Because zio-kafka does pre-fetching, auto commit is not supported. Please do not set config enable.auto.commit. " +
                    "Commit timeout must be larger than 2 * pollTimeout, saw commitTimeout PT15S and pollTimeout PT1M."
                )
              )
            )
        )
      }
    )

}
