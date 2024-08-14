package zio.kafka.example

import zio._
import zio.stream._

object ZStreamInterrupt extends ZIOAppDefault {
  override val bootstrap = Runtime.removeDefaultLoggers ++ Runtime.addLogger(ZLogger.simple(println))

  private val shutdownTimeout = 5.seconds

  override def run: ZIO[Any with ZIOAppArgs with Scope, Any, Any] = {
    val stream: ZStream[Any, Nothing, Take[Nothing, Long]] =
      ZStream
        .fromSchedule(Schedule.spaced(100.micros))
        .map(Take.single)

    val p = for {
      end <- Promise.make[Nothing, Unit]
      stoppableStream = stream.merge(ZStream.fromZIO(end.await).as(Take.end))
      fib <- stoppableStream
        .flattenTake
        .runDrain
        .onInterrupt(ZIO.logError("stream interrupted, this should not happen"))
        .forkScoped
      result <-
        fib.join.onInterrupt {
          end.succeed(()) *>
            ZIO.logError("Stream end sent") *>
            fib.join
              .timeout(shutdownTimeout)
              .tapErrorCause { cause =>
                ZIO.logErrorCause("Error joining fiber", cause)
              }
              .ignore *>
            ZIO.logError("Fiber joined")
        }
    } yield result

    // Interrupt after 1 second
    p.timeout(100.millis).repeatN(100000)
  }
}