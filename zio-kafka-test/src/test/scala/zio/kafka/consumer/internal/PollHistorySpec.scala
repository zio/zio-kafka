package zio.kafka.consumer.internal

import zio.Scope
import zio.test._

object PollHistorySpec extends ZIOSpecDefault {
  override def spec: Spec[TestEnvironment with Scope, Any] = suite("PollHistorySpec")(
    test("optimisticResume for listed pattern") {
      assertTrue("011111".toPollHistory.optimisticResume)
    },
    test("optimisticResume for unknown pattern") {
      assertTrue(!"0111111".toPollHistory.optimisticResume)
    },
    test("add to history") {
      assertTrue(
        PollHistory.Empty.addPollHistory(true).latestWasResumed,
        "1".toPollHistory.addPollHistory(true).latestWasResumed,
        "101010".toPollHistory.addPollHistory(true).latestWasResumed,
        !PollHistory.Empty.addPollHistory(false).latestWasResumed,
        !"1".toPollHistory.addPollHistory(false).latestWasResumed,
        !"101010".toPollHistory.addPollHistory(false).latestWasResumed
      )
    }
  )

  private implicit class PollHistoryOps(val s: String) extends AnyVal {
    def toPollHistory: PollHistory =
      s.foldLeft(PollHistory.Empty) { case (ph, b) =>
        ph.addPollHistory(b == '1')
      }
  }
}
