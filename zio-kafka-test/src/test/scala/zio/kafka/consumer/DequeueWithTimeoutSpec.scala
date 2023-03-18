package zio.kafka.consumer
import zio.test._
import zio._
import zio.kafka.consumer.internal.DequeueWithTimeout

object DequeueWithTimeoutSpec extends ZIOSpecDefault {
  override def spec = suite("DequeueWithTimeout")(
    test("dequeues all immediately available elements with takeAll") {
      for {
        q      <- Queue.unbounded[Int]
        d      <- DequeueWithTimeout.make(q)
        _      <- q.offerAll(List(1, 2, 3, 4))
        result <- d.takeAll(1.second)
      } yield assertTrue(result == Chunk(1, 2, 3, 4))
    },
    test("dequeues immediately with takeBetween") {
      for {
        q      <- Queue.unbounded[Int]
        d      <- DequeueWithTimeout.make(q)
        _      <- q.offerAll(List(1, 2, 3, 4))
        result <- d.takeBetween(1, 10, 1.second)
      } yield assertTrue(result == Chunk(1, 2, 3, 4))
    },
    test("resumes interrupted takeBetween") {
      for {
        q       <- Queue.unbounded[Int]
        d       <- DequeueWithTimeout.make(q)
        result1 <- d.takeBetween(1, 10, 1.second) <& TestClock.adjust(1.second)
        _       <- q.offerAll(List(1, 2, 3, 4))
        result2 <- d.takeBetween(1, 10, 1.second)
        result3 <- d.takeAll(1.second)
      } yield assertTrue(result1.isEmpty && result2.headOption.contains(1) && (result2 ++ result3) == Chunk(1, 2, 3, 4))
    },
    test("resumes interrupted takeBetween followed by takeAll") {
      for {
        q       <- Queue.unbounded[Int]
        d       <- DequeueWithTimeout.make(q)
        result1 <- d.takeBetween(1, 10, 1.second) <& TestClock.adjust(1.second)
        _       <- q.offerAll(List(1, 2, 3, 4))
        result2 <- d.takeAll(1.second)
      } yield assertTrue(result1.isEmpty && result2 == Chunk(1, 2, 3, 4))
    },
    test("handles interruption") {
      for {
        q       <- Queue.unbounded[Int]
        d       <- DequeueWithTimeout.make(q)
        fib     <- d.takeBetween(1, 10, 2.second).fork
        _       <- TestClock.adjust(1.second)
        _       <- fib.interrupt
        _       <- q.offerAll(List(1, 2, 3, 4))
        result2 <- d.takeAll(1.second)
      } yield assertTrue(result2 == Chunk(1, 2, 3, 4))
    }
  ) @@ TestAspect.nonFlaky(10)

}
