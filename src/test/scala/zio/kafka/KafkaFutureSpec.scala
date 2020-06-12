package zio.kafka

import org.apache.kafka.common.internals.KafkaFutureImpl
import zio._
import zio.kafka.admin._
import zio.test.{ testM, _ }
import zio.test.TestAspect.flaky
import zio.test.Assertion._

object KafkaFutureSpec extends DefaultRunnableSpec {
  override def spec =
    suite("kafka future conversion")(
      testM("completes successfully") {
        withKafkaFuture.use { f =>
          for {
            fiber  <- AdminClient.fromKafkaFuture(ZIO.effectTotal(f)).fork
            _      <- ZIO.effectTotal(f.complete(true))
            result <- fiber.await
          } yield {
            assert(result)(equalTo(Exit.succeed(true))) &&
            assert(f.isDone)(equalTo(true) ?? "Kafka future is done")
          }
        }
      },
      testM("completes with failure") {
        withKafkaFuture.use { f =>
          val t = new RuntimeException("failure")
          for {
            fiber  <- AdminClient.fromKafkaFuture(ZIO.effectTotal(f)).fork
            _      <- ZIO.effectTotal(f.completeExceptionally(t))
            result <- fiber.await
          } yield {
            assert(result)(equalTo(Exit.fail(t))) &&
            assert(f.isDone)(equalTo(true) ?? "Kafka future is done")
          }
        }
      },
      testM("future is cancelled") {
        withKafkaFuture.use { f =>
          for {
            fiber  <- AdminClient.fromKafkaFuture(ZIO.effectTotal(f)).fork
            _      <- ZIO.effectTotal(f.cancel(true))
            result <- fiber.await
          } yield {
            assert(result.interrupted)(equalTo(true) ?? "fiber was interrupted") &&
            assert(f.isCancelled)(equalTo(true) ?? "Kafka future was cancelled") &&
            assert(f.isDone)(equalTo(true) ?? "Kafka future is done")
          }
        }
      },
      testM("interrupted") {
        withKafkaFuture.use { f =>
          for {
            latch  <- Promise.make[Nothing, Unit]
            fiber  <- AdminClient.fromKafkaFuture(latch.succeed(()) *> ZIO.effectTotal(f)).fork
            _      <- latch.await
            result <- fiber.interrupt
          } yield {
            assert(result.interrupted)(equalTo(true) ?? "fiber was interrupted") &&
            assert(f.isCancelled)(equalTo(true) ?? "Kafka future was cancelled") &&
            assert(f.isDone)(equalTo(true) ?? "Kafka future is done")
          }
        }
      } @@ flaky
    )

  def withKafkaFuture =
    ZIO.effectTotal(new KafkaFutureImpl[Boolean]).toManaged { f =>
      ZIO.effectTotal {
        f.completeExceptionally(new RuntimeException("Kafka future was not completed"))
      }
    }
}
