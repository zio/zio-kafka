package zio.kafka.consumer.internal

import zio.kafka.ZIOKafkaSpec
import zio.kafka.consumer.diagnostics.{ DiagnosticEvent, Diagnostics }
import zio.kafka.consumer.internal.Runloop.Command
import zio.kafka.consumer.{ Consumer, ConsumerSettings }
import zio.kafka.embedded.Kafka
import zio.stream.ZStream
import zio.test._
import zio.{ &, durationInt, Queue, Schedule, Scope, ZIO }

object RunloopSpec extends ZIOKafkaSpec {
  override val kafkaPrefix: String = "runloopspec"

  private val repeatWithScheduleSpec =
    suite("ZStream.repeatWithSchedule")(
      test("tata") {
        for {
          queue <- Queue.unbounded[Command]
          schedule = Schedule.fixed(2.seconds)
          stream   = ZStream.repeatZIOWithSchedule(queue.offer(Command.Poll), schedule)
          before        <- queue.takeAll
          _             <- stream.runDrain.forkScoped
          _             <- TestClock.adjust(10.milliseconds)
          justAfter     <- queue.takeAll
          _             <- TestClock.adjust(1.second)
          afterOneSec   <- queue.takeAll
          _             <- TestClock.adjust(1.second)
          afterTwoSec   <- queue.takeAll
          _             <- TestClock.adjust(1.second)
          afterThreeSec <- queue.takeAll
        } yield assertTrue(before.isEmpty) &&
          assertTrue(justAfter.size == 1) &&
          assertTrue(afterOneSec.isEmpty) &&
          assertTrue(afterTwoSec.size == 1) &&
          assertTrue(afterThreeSec.isEmpty)
      }
    )

  private val mergeSpec =
    suite("ZStream.merge")(
      test("toto") {
        for {
          commandQueue <- Queue.unbounded[Command]
          queue_0      <- Queue.unbounded[Any]
          queue_1      <- Queue.unbounded[Any]
          schedule      = Schedule.fixed(2.seconds)
          commandStream = ZStream.repeatZIOWithSchedule(commandQueue.offer(Command.Poll), schedule)
          stream = ZStream
                     .mergeAll(3, 1)(
                       commandStream,
                       ZStream.fromQueue(queue_0),
                       ZStream.fromQueue(queue_1)
                     )
          before        <- commandQueue.takeAll
          _             <- stream.runDrain.forkScoped
          _             <- TestClock.adjust(10.milliseconds)
          justAfter     <- commandQueue.takeAll
          _             <- TestClock.adjust(1.second)
          afterOneSec   <- commandQueue.takeAll
          _             <- TestClock.adjust(1.second)
          afterTwoSec   <- commandQueue.takeAll
          _             <- TestClock.adjust(1.second)
          afterThreeSec <- commandQueue.takeAll
        } yield assertTrue(before.isEmpty) &&
          assertTrue(justAfter.size == 1) &&
          assertTrue(afterOneSec.isEmpty) &&
          assertTrue(afterTwoSec.size == 1) &&
          assertTrue(afterThreeSec.isEmpty)
      }
    )

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
      repeatWithScheduleSpec,
      mergeSpec,
      runSpec
    )
}
