package zio.kafka.consumer.internal

import zio.kafka.ZIOKafkaSpec
import zio.kafka.consumer.diagnostics.{ DiagnosticEvent, Diagnostics }
import zio.kafka.consumer.internal.Runloop.Command
import zio.kafka.consumer.{ Consumer, ConsumerSettings }
import zio.kafka.embedded.Kafka
import zio.test.TestAspect.flaky
import zio.test._
import zio.{ durationInt, Scope, ZIO }

object RunloopSpec extends ZIOKafkaSpec {
  override val kafkaPrefix: String = "runloopspec"

  /**
   * Note: the [[Runloop.run]] call is made in the [[Consumer.make]] method (when calling the [[Runloop.apply]]), so we
   * dont need to call it explicitly.
   */
  private val runSpec =
    suite("::run")(
      test("Immediately triggers a first Poll command, doesn't wait an initial `pollFrequency` to trigger it") {
        val halfACycle = 500.milliseconds
        val oneCycle   = 1.second
        val twoCycles  = 2.seconds

        for {
          kafka       <- ZIO.service[Kafka]
          diagnostics <- Diagnostics.SlidingQueue.make()
          diagnosticsQueue = diagnostics.queue
          _ <- Consumer.make(ConsumerSettings(kafka.bootstrapServers).withPollInterval(twoCycles), diagnostics)

          before <- diagnosticsQueue.takeAll

          _             <- TestClock.adjust(halfACycle)
          justAfterBoot <- diagnosticsQueue.takeAll

          _            <- TestClock.adjust(oneCycle)
          afterCycle_1 <- diagnosticsQueue.takeAll

          _            <- TestClock.adjust(oneCycle)
          afterCycle_2 <- diagnosticsQueue.takeAll

          _            <- TestClock.adjust(oneCycle)
          afterCycle_3 <- diagnosticsQueue.takeAll

          _            <- TestClock.adjust(oneCycle)
          afterCycle_4 <- diagnosticsQueue.takeAll
        } yield assertTrue(
          before.isEmpty,
          justAfterBoot.size == 1 && justAfterBoot.head == DiagnosticEvent.RunloopEvent(Command.Poll),
          afterCycle_1.isEmpty,
          afterCycle_2.size == 1 && afterCycle_2.head == DiagnosticEvent.RunloopEvent(Command.Poll),
          afterCycle_3.isEmpty,
          afterCycle_4.size == 1 && afterCycle_4.head == DiagnosticEvent.RunloopEvent(Command.Poll)
        )
      } @@ flaky
    )

  override def spec: Spec[TestEnvironment with Kafka with Scope, Any] =
    suite("Runloop")(
      runSpec
    )
}
