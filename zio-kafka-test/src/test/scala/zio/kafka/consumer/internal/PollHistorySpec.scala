package zio.kafka.consumer.internal

import zio.Scope
import zio.kafka.ZIOSpecDefaultSlf4j
import zio.kafka.consumer.internal.PollHistory.PollHistoryImpl
import zio.test._

object PollHistorySpec extends ZIOSpecDefaultSlf4j {
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
        "101010".toPollHistory.addPollHistory(false).asBitString == "1010100",
      )
    },
  )

  private implicit class RichPollHistory(private val ph: PollHistory) extends AnyVal {
    def asBitString: String =
      ph.asInstanceOf[PollHistoryImpl].resumeBits.toBinaryString
  }

  private implicit class PollHistoryOps(private val s: String) extends AnyVal {
    def toPollHistory: PollHistory =
      s.foldLeft(PollHistory.Empty) { case (ph, b) =>
        ph.addPollHistory(b == '1')
      }
  }
}
