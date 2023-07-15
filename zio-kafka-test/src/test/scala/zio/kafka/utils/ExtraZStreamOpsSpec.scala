package zio.kafka.utils

import zio._
import zio.kafka.ZIOSpecDefaultSlf4j
import zio.stream.ZStream
import zio.test._
import zio.test.Assertion._
import zio.test.TestAspect.withLiveClock

import scala.util.control.NoStackTrace

object ExtraZStreamOpsSpec extends ZIOSpecDefaultSlf4j {
  import ExtraZStreamOps._

  private object ConsumeTimeout extends RuntimeException with NoStackTrace

  override def spec: Spec[TestEnvironment, Any] =
    suite("ExtraZStreamOps")(
      test("consumeTimeoutFail does not fail stream for fast consumer") {
        for {
          f <- ZStream
                 .fromIterator(Iterator.from(1), 1)
                 .consumeTimeoutFail(ConsumeTimeout)(3.seconds)
                 .tap(_ => ZIO.sleep(1.second))
                 .take(5)
                 .runCollect
                 .fork
          _        <- TestClock.adjust(1.second).repeatN(5)
          consumed <- f.join
        } yield assertTrue(consumed.size == 5)
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
        } yield assertTrue(consumed.size == 5)
      },
      test("consumeTimeoutFail fails stream for slow consumer") {
        for {
          consumedRef <- Ref.make(Seq.empty[Int])
          f <- ZStream
                 .fromIterator(Iterator.from(1), 1)
                 .consumeTimeoutFail(ConsumeTimeout)(1.seconds)
                 .tap(elem => ZIO.sleep(2.seconds).when(elem == 4))
                 .take(10)
                 .tap(elem => consumedRef.update(_ :+ elem))
                 .runDrain
                 .exit
                 .fork
          fResult  <- f.join
          consumed <- consumedRef.get
        } yield assert(fResult)(fails(equalTo(ConsumeTimeout))) && assertTrue(consumed == Seq(1, 2, 3, 4))
      } @@ withLiveClock,
      test("consumeTimeoutFail retains chunking structure") {
        for {
          result <- ZStream
                      .fromChunks(Chunk(1, 2, 3, 4, 5), Chunk(1, 2, 3, 4, 5))
                      .consumeTimeoutFail(ConsumeTimeout)(100.seconds)
                      .chunks
                      .runHead
        } yield assertTrue(result.map(_.size).getOrElse(0) == 5)
      }
    )

}
