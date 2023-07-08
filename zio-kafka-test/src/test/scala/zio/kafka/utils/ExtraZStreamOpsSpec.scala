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
          consumed <- Ref.make(Seq.empty[Int])
          f <- ZStream
                 .fromIterator(Iterator.from(1), 1)
                 .consumeTimeoutFail(ConsumeTimeout)(100.millis)
                 .tap(elem => ZIO.sleep(200.millis).when(elem == 4))
                 .take(10)
                 .tap(elem => consumed.update(_ :+ elem))
                 .runDrain
                 .exit
                 .fork
          _       <- TestClock.adjust(100.millis).repeatN(2)
          fResult <- f.join
          result  <- consumed.get
        } yield assert(fResult)(fails(equalTo(ConsumeTimeout))) && assertTrue(result == Seq(1, 2, 3))
      },
      test("consumeTimeoutFail fails stream for slow consumer - oud") {
        for {
          result <- ZStream
                      .fromIterator(Iterator.from(1), 1)
                      .consumeTimeoutFail(ConsumeTimeout)(10.millis)
                      .tap(elem => ZIO.sleep(200.millis).when(elem == 40))
                      .take(1000)
                      .runDrain // todo: test that we get items up till the slow consumer
                      .exit
        } yield assert(result)(fails(equalTo(ConsumeTimeout)))
      } @@ withLiveClock
    )

}
