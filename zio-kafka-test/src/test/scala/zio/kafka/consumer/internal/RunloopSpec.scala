package zio.kafka.consumer.internal

import zio.kafka.ZIOKafkaSpec
import zio.kafka.consumer.diagnostics.{ DiagnosticEvent, Diagnostics }
import zio.kafka.consumer.internal.Runloop.Command
import zio.kafka.consumer.{ Consumer, ConsumerSettings }
import zio.kafka.embedded.Kafka
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
        val pollInterval = 2.seconds

        for {
          kafka       <- ZIO.service[Kafka]
          diagnostics <- Diagnostics.SlidingQueue.make()
          diagnosticsQueue = diagnostics.queue
          _ <- Consumer.make(ConsumerSettings(kafka.bootstrapServers).withPollInterval(pollInterval), diagnostics)

          before <- diagnosticsQueue.takeAll

          justAfterBoot <- diagnosticsQueue.take
        } yield assertTrue(
          before.isEmpty,
          justAfterBoot == DiagnosticEvent.RunloopEvent(Command.Poll)
        )
      } // @@ flaky
    )

  override def spec: Spec[TestEnvironment with Kafka with Scope, Any] =
    suite("Runloop")(
      runSpec
    ) @@ TestAspect.timeout(180.seconds)
}
