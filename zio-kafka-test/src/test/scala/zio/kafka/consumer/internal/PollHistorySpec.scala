package zio.kafka.consumer.internal

import zio.Scope
import zio.kafka.ZIOSpecDefaultSlf4j
import zio.kafka.consumer.internal.PollHistory.PollHistoryImpl
import zio.test._

object PollHistorySpec extends ZIOSpecDefaultSlf4j {
  override def spec: Spec[TestEnvironment with Scope, Any] = suite("PollHistorySpec")(
    test("estimates poll count for very regular pattern") {
      assertTrue(
        (("001" * 22) + "").toPollHistory.estimatedPollCountToResume == 3,
        (("001" * 22) + "0").toPollHistory.estimatedPollCountToResume == 2,
        (("001" * 22) + "00").toPollHistory.estimatedPollCountToResume == 1,
        (("00001" * 13) + "").toPollHistory.estimatedPollCountToResume == 5,
      )
    },
    test("estimates poll count for somewhat irregular pattern") {
      assertTrue(
        "000101001001100001000011001001001".toPollHistory.estimatedPollCountToResume == 3,
      )
    },
    test("estimates poll count only when paused for less than 16 polls") {
      assertTrue(
        "0".toPollHistory.estimatedPollCountToResume == 64,
        "10000000000000000000000000000000".toPollHistory.estimatedPollCountToResume == 64,
        ("11" * 8 + "00" * 8).toPollHistory.estimatedPollCountToResume == 64,
        ("11" * 9 + "00" * 7).toPollHistory.estimatedPollCountToResume == 0,
      )
    },
    test("estimates poll count for edge cases") {
      assertTrue(
        "11111111111111111111111111111111".toPollHistory.estimatedPollCountToResume == 1,
        "10000000000000001000000000000000".toPollHistory.estimatedPollCountToResume == 1,
        "01000000000000000100000000000000".toPollHistory.estimatedPollCountToResume == 2,
        "00100000000000000010000000000000".toPollHistory.estimatedPollCountToResume == 3,
        "00010000000000000001000000000000".toPollHistory.estimatedPollCountToResume == 4,
      )
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
    }
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
