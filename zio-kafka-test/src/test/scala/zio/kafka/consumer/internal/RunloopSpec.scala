package zio.kafka.consumer.internal

import zio.kafka.ZIOKafkaSpec
import zio.kafka.consumer.diagnostics.{ DiagnosticEvent, Diagnostics }
import zio.kafka.consumer.internal.Runloop.Command
import zio.kafka.consumer.{ Consumer, ConsumerSettings }
import zio.kafka.embedded.Kafka
import zio.stream.ZStream
import zio.test._
import zio.{ &, durationInt, Chunk, Queue, Ref, Schedule, Scope, UIO, ZIO }

object RunloopSpec extends ZIOKafkaSpec {
  override val kafkaPrefix: String = "runloopspec"

  private val repeatWithScheduleSpec =
    suite("ZStream.repeatZIOWithSchedule")(
      test("Schedule.fixed vs Schedule.spaced") {
        final case class Toto(cycle: Int)

        val oneCycle         = 500.millis
        val oneAndAHalfCycle = 750.millis

        def behaviour(counter: Ref.Synchronized[Int], queue: Queue[Toto]): UIO[Unit] =
          counter
            .getAndUpdateZIO(i => (ZIO.sleep(oneAndAHalfCycle).when(i % 2 != 0) *> queue.offer(Toto(i))).as(i + 1))
            .unit

        for {
          fixedCounter  <- Ref.Synchronized.make(0)
          spacedCounter <- Ref.Synchronized.make(0)

          fixedQueue  <- Queue.unbounded[Toto]
          spacedQueue <- Queue.unbounded[Toto]

          fixed  = Schedule.fixed(oneCycle)
          spaced = Schedule.spaced(oneCycle)

          fixedStream  = ZStream.repeatZIOWithSchedule(behaviour(fixedCounter, fixedQueue), fixed)
          spacedStream = ZStream.repeatZIOWithSchedule(behaviour(spacedCounter, spacedQueue), spaced)

          fixedBefore <- fixedQueue.takeAll
          spaceBefore <- spacedQueue.takeAll

          _ <- ZStream.mergeAll(2, 1)(fixedStream, spacedStream).runDrain.forkScoped

          _              <- TestClock.adjust(20.milliseconds) // Cycle 0 - Not enough time to trigger the first cycle
          fixedJustAfter <- fixedQueue.takeAll
          spaceJustAfter <- spacedQueue.takeAll

          _                  <- TestClock.adjust(oneCycle) // Cycle 1
          fixedAfterCycle_1  <- fixedQueue.takeAll
          spacedAfterCycle_1 <- spacedQueue.takeAll

          _                  <- TestClock.adjust(oneCycle) // Cycle 2
          fixedAfterCycle_2  <- fixedQueue.takeAll
          spacedAfterCycle_2 <- spacedQueue.takeAll

          _                  <- TestClock.adjust(oneCycle) // Cycle 3
          fixedAfterCycle_3  <- fixedQueue.takeAll
          spacedAfterCycle_3 <- spacedQueue.takeAll

          _                  <- TestClock.adjust(oneCycle) // Cycle 4
          fixedAfterCycle_4  <- fixedQueue.takeAll
          spacedAfterCycle_4 <- spacedQueue.takeAll

          _                  <- TestClock.adjust(oneCycle) // Cycle 5
          fixedAfterCycle_5  <- fixedQueue.takeAll
          spacedAfterCycle_5 <- spacedQueue.takeAll

          _                  <- TestClock.adjust(oneCycle) // Cycle 6
          fixedAfterCycle_6  <- fixedQueue.takeAll
          spacedAfterCycle_6 <- spacedQueue.takeAll

          _                  <- TestClock.adjust(oneCycle) // Cycle 7
          fixedAfterCycle_7  <- fixedQueue.takeAll
          spacedAfterCycle_7 <- spacedQueue.takeAll

        // format: off
        } yield assertTrue(fixedBefore.isEmpty) && assertTrue(spaceBefore.isEmpty) &&
          assertTrue(fixedJustAfter.size == 1) && assertTrue(spaceJustAfter.size == 1) &&
          assertTrue(fixedAfterCycle_1.isEmpty) && assertTrue(spacedAfterCycle_1.isEmpty) &&
          assertTrue(fixedAfterCycle_2.isEmpty) && assertTrue(spacedAfterCycle_2.isEmpty) &&
          assertTrue(fixedAfterCycle_3 == Chunk(Toto(cycle = 1), Toto(cycle = 2))) && assertTrue(spacedAfterCycle_3 == Chunk(Toto(cycle = 1))) &&
          assertTrue(fixedAfterCycle_4.isEmpty) && assertTrue(spacedAfterCycle_4 == Chunk(Toto(cycle = 2))) &&
          assertTrue(fixedAfterCycle_5 == Chunk(Toto(cycle = 3), Toto(cycle = 4))) && assertTrue(spacedAfterCycle_5.isEmpty) &&
          assertTrue(fixedAfterCycle_6.isEmpty) && assertTrue(spacedAfterCycle_6 == Chunk(Toto(cycle = 3))) &&
        assertTrue(fixedAfterCycle_7 == Chunk(Toto(cycle = 5), Toto(cycle = 6))) && assertTrue(spacedAfterCycle_7 == Chunk(Toto(cycle = 4)))
        // format: on
      }
    )

  /**
   * Note: the [[Runloop.run]] call is made in the [[Consumer.make]] method (when calling the [[Runloop.apply]]), so we
   * dont need to call it explicitly.
   */
  private val runSpec =
    suite("::run")(
      test("Immediately triggers a first Poll command, doesn't wait for `pollFrequency` to trigger it") {
        for {
          kafka       <- ZIO.service[Kafka]
          diagnostics <- Diagnostics.SlidingQueue.make()
          diagnosticsQueue = diagnostics.queue
          _         <- Consumer.make(ConsumerSettings(kafka.bootstrapServers).withPollInterval(2.seconds), diagnostics)
          before    <- diagnosticsQueue.takeAll
          _         <- TestClock.adjust(10.milliseconds)
          justAfter <- diagnosticsQueue.takeAll
          _         <- TestClock.adjust(1.second)
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
      runSpec
    )
}
