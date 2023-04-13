package zio.kafka.consumer.internal

import zio.Scope
import zio.test._

import zio.kafka.consumer.internal.OptimisticResume._

object OptimisticResumeSpec extends ZIOSpecDefault {
  override def spec: Spec[TestEnvironment with Scope, Any] = suite("OptimisticResumeSpec")(
    test("optimisticResume for listed pattern") {
      assertTrue("011111".asBits.optimisticResume)
    },
    test("optimisticResume for unknown pattern") {
      assertTrue(!"0111111".asBits.optimisticResume)
    },
    test("add resume to history") {
      assertTrue("101010".asBits.addPollHistory(true) == "1010101".asBits)
    },
    test("add pause to history") {
      assertTrue("101010".asBits.addPollHistory(false) == "1010100".asBits)
    }
  )

  private implicit class IntBinaryOps(val s: String) extends AnyVal {
    def asBits: Int = Integer.parseInt(s, 2)
  }
}
