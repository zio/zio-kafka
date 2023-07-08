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
                 .consumeTimeoutFail(ConsumeTimeout)(100.millis)
                 .tap(_ => ZIO.sleep(1.millis))
                 .take(10)
                 .runDrain
                 .fork
          _ <- TestClock.adjust(1.millis).repeatN(10)
          _ <- f.join
        } yield assertCompletes
      },
      test("consumeTimeoutFail fails stream for slow consumer") {
        for {
          consumedRef <- Ref.make(Seq.empty[Int])
          f <- ZStream
                 .fromIterator(Iterator.from(1), 1)
                 .consumeTimeoutFail(ConsumeTimeout)(100.millis)
                 .tap(elem => ZIO.sleep(200.millis).when(elem == 4))
                 .take(10)
                 .tap(elem => consumedRef.update(_ :+ elem))
                 .runDrain
                 .exit
                 .fork
          _        <- TestClock.adjust(100.millis).repeatN(2)
          fResult  <- f.join
          consumed <- consumedRef.get
        } yield assert(fResult)(fails(equalTo(ConsumeTimeout))) && assertTrue(consumed == Seq(1, 2, 3, 4))
      },
      test("consumeTimeoutFail fails stream for slow consumer - live clock") {
        for {
          consumedRef <- Ref.make(Seq.empty[Int])
          result <- ZStream
                      .fromIterator(Iterator.from(1), 1)
                      .consumeTimeoutFail(ConsumeTimeout)(100.millis)
                      .tap(elem => ZIO.sleep(200.millis).when(elem == 4))
                      .take(10)
                      .tap(elem => consumedRef.update(_ :+ elem))
                      .runDrain
                      .exit
          consumed <- consumedRef.get
        } yield assert(result)(fails(equalTo(ConsumeTimeout))) && assertTrue(consumed == Seq(1, 2, 3, 4))
      } @@ withLiveClock,
      test("consumeTimeoutFail retain chunking structure") {
        for {
          result <- ZStream
                      .fromChunks(Chunk(1, 2, 3, 4, 5), Chunk(1, 2, 3, 4, 5))
                      .consumeTimeoutFail(ConsumeTimeout)(100.millis)
                      .chunks
                      .runHead
        } yield assertTrue(result.map(_.size).getOrElse(0) == 5)
      }
    )

}
