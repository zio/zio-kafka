package zio.kafka

import org.apache.kafka.common.internals.KafkaFutureImpl
import zio._
import zio.kafka.admin._
import zio.test._
import zio.test.TestAspect.flaky
import zio.test.Assertion._

object KafkaFutureSpec extends ZIOSpecDefault {
  override def spec =
    suite("kafka future conversion")(
      test("completes successfully") {
        withKafkaFuture.flatMap { f =>
          for {
            fiber  <- AdminClient.fromKafkaFuture(ZIO.succeed(f)).fork
            _      <- ZIO.succeed(f.complete(true))
            result <- fiber.await
          } yield assert(result)(equalTo(Exit.succeed(true))) &&
            assert(f.isDone)(equalTo(true) ?? "Kafka future is done")
        }
      },
      test("completes with failure") {
        withKafkaFuture.flatMap { f =>
          val t = new RuntimeException("failure")
          for {
            fiber  <- AdminClient.fromKafkaFuture(ZIO.succeed(f)).fork
            _      <- ZIO.succeed(f.completeExceptionally(t))
            result <- fiber.await
          } yield assert(result)(equalTo(Exit.fail(t))) &&
            assert(f.isDone)(equalTo(true) ?? "Kafka future is done")
        }
      },
      test("future is cancelled") {
        withKafkaFuture.flatMap { f =>
          for {
            fiber  <- AdminClient.fromKafkaFuture(ZIO.succeed(f)).fork
            _      <- ZIO.succeed(f.cancel(true))
            result <- fiber.await
          } yield assert(result.isInterrupted)(equalTo(true) ?? "fiber was interrupted") &&
            assert(f.isCancelled)(equalTo(true) ?? "Kafka future was cancelled") &&
            assert(f.isDone)(equalTo(true) ?? "Kafka future is done")
        }
      },
      test("interrupted") {
        withKafkaFuture.flatMap { f =>
          for {
            latch  <- Promise.make[Nothing, Unit]
            fiber  <- AdminClient.fromKafkaFuture(latch.succeed(()).as(f)).fork
            _      <- latch.await
            result <- fiber.interrupt
          } yield assert(result.isInterrupted)(equalTo(true) ?? "fiber was interrupted") &&
            assert(f.isCancelled)(equalTo(true) ?? "Kafka future was cancelled") &&
            assert(f.isDone)(equalTo(true) ?? "Kafka future is done")
        }
      } @@ flaky
    )

  def withKafkaFuture: ZIO[Any with Scope, Nothing, KafkaFutureImpl[Boolean]] =
    ZIO.acquireRelease(ZIO.succeed(new KafkaFutureImpl[Boolean])) { f =>
      ZIO.succeed {
        f.completeExceptionally(new RuntimeException("Kafka future was not completed"))
      }
    }
}
