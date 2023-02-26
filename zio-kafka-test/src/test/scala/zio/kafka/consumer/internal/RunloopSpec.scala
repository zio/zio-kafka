package zio.kafka.consumer.internal

import zio.kafka.ZIOKafkaSpec
import zio.kafka.consumer.diagnostics.{ DiagnosticEvent, Diagnostics }
import zio.kafka.consumer.internal.Runloop.Command
import zio.kafka.consumer.{ Consumer, ConsumerSettings }
import zio.kafka.embedded.Kafka
import zio.test._
import zio.{ &, durationInt, Scope, ZIO }

object RunloopSpec extends ZIOKafkaSpec {
  override val kafkaPrefix: String = "runloopspec"

  private val runSpec =
    suite("::run")(
      test("Immediately triggers a first Poll command, doesn't wait for `pollFrequency` to trigger it") {
        for {
          kafka       <- ZIO.service[Kafka]
          diagnostics <- Diagnostics.SlidingQueue.make()
          diagnosticsQueue = diagnostics.queue
          consumer <- Consumer.make(ConsumerSettings(kafka.bootstrapServers).withPollInterval(2.seconds), diagnostics)
          runloop = consumer.asInstanceOf[Consumer.Live].runloop
          before        <- diagnosticsQueue.takeAll
          _             <- runloop.run
          _             <- TestClock.adjust(10.milliseconds)
          justAfter     <- diagnosticsQueue.takeAll
          _             <- TestClock.adjust(1.second)
          afterOneSec   <- diagnosticsQueue.takeAll
          _             <- TestClock.adjust(1.second)
          afterTwoSec   <- diagnosticsQueue.takeAll
          _             <- TestClock.adjust(1.second)
          afterThreeSec <- diagnosticsQueue.takeAll
        } yield assertTrue(before.isEmpty) &&
          assertTrue(justAfter.size == 1 && justAfter.head == DiagnosticEvent.RunEvent(Command.Poll)) &&
          assertTrue(afterOneSec.isEmpty) &&
          assertTrue(afterTwoSec.size == 1 && afterTwoSec.head == DiagnosticEvent.RunEvent(Command.Poll)) &&
          assertTrue(afterThreeSec.isEmpty)
      }
    )

  override def spec: Spec[TestEnvironment & Kafka & Scope, Any] =
    suite("Runloop")(
      runSpec
    )
}
