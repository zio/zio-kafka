package zio.kafka.diagnostics

import zio._
import zio.kafka.ZIOSpecDefaultSlf4j
import zio.kafka.diagnostics.internal.ConcurrentDiagnostics
import zio.test.Assertion._
import zio.test.TestAspect.{ timeout, withLiveClock }
import zio.test._

object ConcurrentDiagnosticsSpec extends ZIOSpecDefaultSlf4j {
  override def spec: Spec[Any, Any] =
    suite("ConcurrentDiagnosticsSpec")(
      test("emits events to wrapped diagnostics") {
        for {
          ref <- Ref.make(Chunk.empty[String])
          wrapped = new Diagnostics[String] {
                      override def emit(event: => String): UIO[Unit] =
                        ref.update(_ :+ event).delay(10.millis)
                    }
          // After the scope closes, finalEvent is emitted and the internal queue is drained
          _ <- ZIO.scoped {
                 for {
                   diagnostics <- ConcurrentDiagnostics.make(wrapped, "final")
                   _           <- diagnostics.emit("event1")
                   _           <- diagnostics.emit("event2")
                 } yield ()
               }
          events <- ref.get
        } yield assertTrue(events == Chunk("event1", "event2", "final"))
      } @@ withLiveClock,
      test("wrapper's emit dies when wrapped emit died and killed the background fiber") {
        val boom = new RuntimeException("boom")
        val wrapped = new Diagnostics[String] {
          override def emit(event: => String): UIO[Unit] =
            ZIO.die(boom).when(event == "die").unit
        }

        ZIO.scoped {
          for {
            diagnostics <- ConcurrentDiagnostics.make(wrapped, "final")
            _           <- diagnostics.emit("event1")
            _           <- diagnostics.emit("die")
            // Wait until the fiber died
            _    <- diagnostics.emit("after-die").exit.repeatUntil(_.isFailure)
            exit <- diagnostics.emit("after-die").exit
          } yield assertTrue(exit.isFailure) && assert(exit)(dies(equalTo(boom)))
        }
      } @@ timeout(1.second),
      test("wrapper's emit dies when wrapped emit throws and killed the background fiber") {
        val boom = new RuntimeException("boom")
        val wrapped = new Diagnostics[String] {
          override def emit(event: => String): UIO[Unit] =
            if (event == "throw") throw boom else ZIO.unit
        }

        ZIO.scoped {
          for {
            diagnostics <- ConcurrentDiagnostics.make(wrapped, "final")
            _           <- diagnostics.emit("throw")
            // Wait until the fiber died
            _    <- diagnostics.emit("after-throw").exit.repeatUntil(_.isFailure)
            exit <- diagnostics.emit("after-throw").exit
          } yield assertTrue(exit.isFailure) && assert(exit)(dies(equalTo(boom)))
        }
      } @@ timeout(1.second)
    )
}
