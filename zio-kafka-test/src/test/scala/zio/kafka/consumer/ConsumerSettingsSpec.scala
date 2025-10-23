package zio.kafka.consumer

import org.apache.kafka.clients.consumer.{
  ConsumerConfig,
  CooperativeStickyAssignor,
  RangeAssignor,
  RoundRobinAssignor
}
import zio._
import zio.kafka.ZIOSpecDefaultSlf4j
import zio.test._
import zio.test.Assertion._

import scala.jdk.CollectionConverters._

object ConsumerSettingsSpec extends ZIOSpecDefaultSlf4j {

  override def spec: Spec[TestEnvironment with Scope, Throwable] =
    suite("ConsumerSettingsSpec")(
      suite("driverSettings")(
        test("outputs the hosts") {
          val driverSettings = ConsumerSettings(List("host1", "host2")).driverSettings
          assertTrue(driverSettings.getOrElse(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "") == "host1,host2")
        },
        test("disables auto-commit") {
          val driverSettings = ConsumerSettings(List("host")).driverSettings
          assertTrue(driverSettings.getOrElse(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "") == "false")
        },
        test("set range assigner as partition assigner when rebalanceSafeCommits is enabled") {
          val driverSettings = ConsumerSettings(List("host")).withRebalanceSafeCommits(true).driverSettings
          assertTrue(
            driverSettings
              .getOrElse(ConsumerConfig.PARTITION_ASSIGNMENT_STRATEGY_CONFIG, "") == classOf[RangeAssignor].getName
          )
        },
        test("does not set partition assigner when rebalanceSafeCommits is disabled") {
          val driverSettings = ConsumerSettings(List("host")).withRebalanceSafeCommits(false).driverSettings
          assertTrue(!driverSettings.contains(ConsumerConfig.PARTITION_ASSIGNMENT_STRATEGY_CONFIG))
        },
        test("adds other properties") {
          val driverSettings = ConsumerSettings(List("host"))
            .withProperty("p1", "v1")
            .withProperties(Map("p2" -> "v2"))
            .withProperties("p3" -> "v3", "p4" -> "v4")
            .driverSettings
          assertTrue(
            driverSettings.getOrElse("p1", "") == "v1",
            driverSettings.getOrElse("p2", "") == "v2",
            driverSettings.getOrElse("p3", "") == "v3",
            driverSettings.getOrElse("p4", "") == "v4"
          )
        },
        test("allows overriding default settings") {
          val driverSettings = ConsumerSettings(List("host1", "host2"))
            .withProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "another-host")
            .driverSettings
          assertTrue(driverSettings.getOrElse(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "") == "another-host")
        }
      ),
      suite("validate")(
        test("accepts no auto.commit") {
          ConsumerSettings(List("host")).validate *> assertCompletesZIO
        },
        test("accepts disabled auto.commit") {
          ConsumerSettings(List("host")).withProperty("enable.auto.commit", "false").validate *> assertCompletesZIO
        },
        test("rejects auto.commit") {
          assertFailsWithIllegalArgumentException(
            ConsumerSettings(List("host")).withProperty("enable.auto.commit", "true").validate,
            "Invalid consumer settings: Because zio-kafka does pre-fetching, auto commit is not supported, do not set config enable.auto.commit."
          )
        },
        test("accepts commitTimeout > 2 * pollTimeout") {
          Seq(2.minutes + 1.milli, 10.minutes).map { commitTimeout =>
            ConsumerSettings(List("host"))
              .withCommitTimeout(commitTimeout)
              .withPollTimeout(1.minute)
              .validate *> assertCompletesZIO
          }
            .reduce(_ && _)
        },
        test("rejects commitTimeout <= 2 * pollTimeout") {
          Seq(15.seconds, 2.minutes).map { commitTimeout =>
            assertFailsWithIllegalArgumentException(
              ConsumerSettings(List("host"))
                .withCommitTimeout(commitTimeout)
                .withPollTimeout(1.minute)
                .validate,
              s"Invalid consumer settings: Commit timeout must be larger than 2 * pollTimeout, saw commitTimeout ${commitTimeout} and pollTimeout PT1M."
            )
          }
            .reduce(_ && _)
        },
        test("rejects with multiple problems") {
          assertFailsWithIllegalArgumentException(
            ConsumerSettings(List("host"))
              .withProperty("enable.auto.commit", "true")
              .withCommitTimeout(15.seconds)
              .withPollTimeout(1.minute)
              .validate,
            "Invalid consumer settings (2 errors): " +
              "Because zio-kafka does pre-fetching, auto commit is not supported, do not set config enable.auto.commit. " +
              "Commit timeout must be larger than 2 * pollTimeout, saw commitTimeout PT15S and pollTimeout PT1M."
          )
        }
      ),
      suite("validateForTransactional")(
        test("accepts rebalanceSafeCommits enabled") {
          ConsumerSettings(List("host")).withRebalanceSafeCommits(true).validateForTransactional *> assertCompletesZIO
        },
        test("rejects rebalanceSafeCommits disabled") {
          assertFailsWithIllegalArgumentException(
            ConsumerSettings(List("host")).withRebalanceSafeCommits(false).validateForTransactional,
            "Invalid consumer settings: " +
              "RebalanceSafeCommits must be enabled when the consumer is used with a transactional producer."
          )
        },
        test("accepts RangeAssignor by name") {
          acceptPartitionAssignor(classOf[RangeAssignor].getName)
        },
        test("accepts RangeAssignor by class") {
          acceptPartitionAssignor(classOf[RangeAssignor])
        },
        test("accepts RangeAssignor in a list") {
          acceptPartitionAssignor(Chunk(classOf[RangeAssignor], classOf[RoundRobinAssignor]))
        },
        test("rejects CooperativeStickyAssignor by name, ignoring spaces") {
          rejectCooperativeStickyAssignor(classOf[CooperativeStickyAssignor].getName + " ")
        },
        test("rejects CooperativeStickyAssignor by class") {
          rejectCooperativeStickyAssignor(classOf[CooperativeStickyAssignor])
        },
        test("rejects CooperativeStickyAssignor in a comma separated string, ignoring spaces") {
          rejectCooperativeStickyAssignor(
            " " + classOf[RangeAssignor].getName + " , " + classOf[CooperativeStickyAssignor].getName + " "
          )
        },
        test("rejects CooperativeStickyAssignor in a string list, ignoring spaces") {
          rejectCooperativeStickyAssignor(Chunk(classOf[CooperativeStickyAssignor].getName + " ").asJava)
        },
        test("rejects CooperativeStickyAssignor in a class list") {
          rejectCooperativeStickyAssignor(Chunk(classOf[RangeAssignor], classOf[CooperativeStickyAssignor]).asJava)
        }
      )
    )

  private def assertFailsWithIllegalArgumentException(
    settings: ZIO[Any, IllegalArgumentException, Unit],
    message: String
  ): UIO[TestResult] =
    assertZIO(settings.exit)(failsWithA[IllegalArgumentException] && fails(hasMessage(equalTo(message))))

  private def acceptPartitionAssignor(value: AnyRef): ZIO[Any, IllegalArgumentException, TestResult] =
    ConsumerSettings(List("host"))
      .withRebalanceSafeCommits(true)
      .withProperty(ConsumerConfig.PARTITION_ASSIGNMENT_STRATEGY_CONFIG, value)
      .validateForTransactional *> assertCompletesZIO

  private def rejectCooperativeStickyAssignor(value: AnyRef): UIO[TestResult] = {
    val settings = ConsumerSettings(List("host"))
      .withRebalanceSafeCommits(true)
      .withProperty(ConsumerConfig.PARTITION_ASSIGNMENT_STRATEGY_CONFIG, value)
      .validateForTransactional
    assertZIO(settings.exit)(
      failsWithA[IllegalArgumentException] &&
        fails(
          hasMessage(
            equalTo(
              "Invalid consumer settings: A partition assigner that revokes all partitions during a rebalance is " +
                "required when the consumer is used with a transactional producer, therefore " +
                "CooperativeStickyAssignor is not supported."
            )
          )
        )
    )
  }

}
