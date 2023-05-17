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
        PollHistory.Empty.addPollHistory(true).asBitString == "1",
        "1".toPollHistory.addPollHistory(true).asBitString == "11",
        "101010".toPollHistory.addPollHistory(true).asBitString == "1010101",
        PollHistory.Empty.addPollHistory(false).asBitString == "0",
        "1".toPollHistory.addPollHistory(false).asBitString == "10",
        "101010".toPollHistory.addPollHistory(false).asBitString == "1010100"
      )
    },
    test("resume count") {
      assertTrue(
        "0".toPollHistory.resumedPollsCount == 0,
        "1".toPollHistory.resumedPollsCount == 1,
        "11".toPollHistory.resumedPollsCount == 2,
        "11010111100011011".toPollHistory.resumedPollsCount == 2,
        ("1" * 31).toPollHistory.resumedPollsCount == 31,
        ("1" * 32).toPollHistory.resumedPollsCount == 32
      )
    }
  )
}
