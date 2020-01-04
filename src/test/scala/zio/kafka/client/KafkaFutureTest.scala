package zio.kafka.client

import org.apache.kafka.common.internals.KafkaFutureImpl
import zio._
import zio.test._
import zio.test.Assertion._

import KafkaFutureTestHelper._

object KafkaFutureTest
    extends DefaultRunnableSpec(
      suite("kafka future conversion")(
        successfully,
        failure,
        cancelled,
        interrupted
      )
    )

object KafkaFutureTestHelper {
  val successfully = testM("completes successfully") {
    withKafkaFuture.use { f =>
      for {
        fiber  <- AdminClient.fromKafkaFuture(ZIO.effectTotal(f)).fork
        _      <- ZIO.effectTotal(f.complete(true))
        result <- fiber.await
      } yield {
        assert(result, equalTo(Exit.succeed(true))) &&
        assert(f.isDone, equalTo(true) ?? "Kafka future is done")
      }
    }
  }

  val failure = testM("completes with failure") {
    withKafkaFuture.use { f =>
      val t = new RuntimeException("failure")
      for {
        fiber  <- AdminClient.fromKafkaFuture(ZIO.effectTotal(f)).fork
        _      <- ZIO.effectTotal(f.completeExceptionally(t))
        result <- fiber.await
      } yield {
        assert(result, equalTo(Exit.fail(t))) &&
        assert(f.isDone, equalTo(true) ?? "Kafka future is done")
      }
    }
  }

  val cancelled = testM("future is cancelled") {
    withKafkaFuture.use { f =>
      for {
        fiber  <- AdminClient.fromKafkaFuture(ZIO.effectTotal(f)).fork
        _      <- ZIO.effectTotal(f.cancel(true))
        result <- fiber.await
      } yield {
        assert(result.interrupted, equalTo(true) ?? "fiber was interrupted") &&
        assert(f.isCancelled, equalTo(true) ?? "Kafka future was cancelled") &&
        assert(f.isDone, equalTo(true) ?? "Kafka future is done")
      }
    }
  }

  val interrupted = testM("interrupted") {
    withKafkaFuture.use { f =>
      for {
        fiber  <- AdminClient.fromKafkaFuture(ZIO.effectTotal(f)).fork
        result <- fiber.interrupt
      } yield {
        assert(result.interrupted, equalTo(true) ?? "fiber was interrupted") &&
        assert(f.isCancelled, equalTo(true) ?? "Kafka future was cancelled") &&
        assert(f.isDone, equalTo(true) ?? "Kafka future is done")
      }
    }
  }

  private def withKafkaFuture =
    ZIO.effectTotal(new KafkaFutureImpl[Boolean]).toManaged { f =>
      ZIO.effectTotal {
        f.completeExceptionally(new RuntimeException("Kafka future was not completed"))
      }
    }
}
