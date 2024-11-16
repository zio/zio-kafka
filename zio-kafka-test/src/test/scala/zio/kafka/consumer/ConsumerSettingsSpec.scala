package zio.kafka.consumer

import zio._
import zio.kafka.ZIOSpecDefaultSlf4j
import zio.test._
import zio.test.Assertion._

object ConsumerSettingsSpec extends ZIOSpecDefaultSlf4j {

  override def spec: Spec[TestEnvironment with Scope, Throwable] =
    suite("ConsumerSettingsSpec")(
      test("accepts no auto.commit") {
        ZIO.attempt(ConsumerSettings(List("host"))) *> assertCompletesZIO
      },
      test("accepts disabled auto.commit") {
        ZIO.attempt(ConsumerSettings(List("host")).withProperty("enable.auto.commit", "false")) *> assertCompletesZIO
      },
      test("rejects auto.commit") {
        val settings = ZIO.attempt(ConsumerSettings(List("host")).withProperty("enable.auto.commit", "true")).exit
        assertZIO(settings)(failsWithA[IllegalArgumentException])
      }
    )

}
