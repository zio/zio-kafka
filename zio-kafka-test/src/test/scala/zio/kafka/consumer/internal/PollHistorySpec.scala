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
        PollHistory.Empty.addPollHistory(PollHistory.Resumed).latestWasResumed,
        "1".toPollHistory.addPollHistory(PollHistory.Resumed).latestWasResumed,
        "101010".toPollHistory.addPollHistory(PollHistory.Resumed).latestWasResumed,
        !PollHistory.Empty.addPollHistory(PollHistory.Paused).latestWasResumed,
        !"1".toPollHistory.addPollHistory(PollHistory.Paused).latestWasResumed,
        !"101010".toPollHistory.addPollHistory(PollHistory.Paused).latestWasResumed
      )
    }
  )

  private implicit class PollHistoryOps(private val s: String) extends AnyVal {
    def toPollHistory: PollHistory =
      s.foldLeft(PollHistory.Empty) { case (ph, b) =>
        ph.addPollHistory(if (b == '1') PollHistory.Resumed else PollHistory.Paused)
      }
  }
}
