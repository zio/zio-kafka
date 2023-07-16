package zio.kafka.utils

import zio._
import zio.kafka.ZIOSpecDefaultSlf4j
import zio.stream.ZStream
import zio.test._
import zio.test.Assertion._
import zio.test.TestAspect.timeout

import scala.util.control.NoStackTrace

object ExtraZStreamOpsSpec extends ZIOSpecDefaultSlf4j {
  import ExtraZStreamOps._

  private object ConsumeTimeout extends RuntimeException with NoStackTrace

  private val stream10 = ZStream(1, 2, 3, 4, 5, 6, 7, 8, 9, 10).rechunk(1)

  override def spec: Spec[TestEnvironment with Scope, Any] =
    suite("ExtraZStreamOps")(
      test("consumeTimeoutFail does not fail stream for fast producer") {
        for {
          consumed <- stream10
                        .consumeTimeoutFail(ConsumeTimeout)(1.second)
                        .take(5)
                        .runCollect
        } yield assertTrue(consumed == Chunk(1, 2, 3, 4, 5))
      },
      test("consumeTimeoutFail does not fail stream for slow producer") {
        for {
          f <- ZStream
                 .fromSchedule(Schedule.fixed(5.seconds))
                 .consumeTimeoutFail(ConsumeTimeout)(1.second)
                 .take(5)
                 .runCollect
                 .fork
          _        <- TestClock.adjust(1.seconds).repeatN(25)
          consumed <- f.join
        } yield assertTrue(consumed == Chunk[Long](0, 1, 2, 3, 4))
      },
      test("consumeTimeoutFail retains chunking structure") {
        for {
          result <- ZStream(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
                      .rechunk(5)
                      .consumeTimeoutFail(ConsumeTimeout)(100.seconds)
                      .chunks
                      .runHead
        } yield assertTrue(result.map(_.size).getOrElse(0) == 5)
      },
      test("consumeTimeoutFail does not fail stream for fast consumer") {
        for {
          consumedRef <- Ref.make(Seq.empty[Int])
          pull <- stream10
                    .consumeTimeoutFail(ConsumeTimeout)(3.seconds)
                    .toPull
          f <- pull
                 .tap(chunk => consumedRef.update(_ ++ chunk))
                 .schedule(Schedule.spaced(1.second) && Schedule.recurs(5))
                 .fork
          _        <- TestClock.adjust(1.second).schedule(Schedule.recurs(5))
          _        <- f.join
          consumed <- consumedRef.get
        } yield assertTrue(consumed.size == 5)
      },
      test("consumeTimeoutFail fail stream for slow consumer") {
        for {
          pull <- stream10
                    .consumeTimeoutFail(ConsumeTimeout)(100.seconds)
                    .toPull
          pullExit <- (for {
                        _ <- pull.delay(50.seconds)
                        _ <- pull.delay(50.seconds)
                        e <- pull.delay(200.second).exit
                      } yield e).fork
          _      <- TestClock.adjust(50.second).schedule(Schedule.recurs(6))
          result <- pullExit.join
        } yield assert(result)(fails(isSome(equalTo(ConsumeTimeout))))
      },
//      test("consumeTimeoutFail fails stream when first pull is slow") {
//        for {
//          pull <- stream10
//                    .consumeTimeoutFail(ConsumeTimeout)(100.seconds)
//                    .toPull
//          pullExit <- pull.delay(200.second).exit.fork
//          _        <- TestClock.adjust(50.second).schedule(Schedule.recurs(6))
//          result   <- pullExit.join
//        } yield assert(result)(fails(isSome(equalTo(ConsumeTimeout))))
//      }
    ) @@ timeout(5.seconds)

}
