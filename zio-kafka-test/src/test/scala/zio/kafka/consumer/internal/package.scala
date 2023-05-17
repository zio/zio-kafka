package zio.kafka.consumer

package object internal {
  import zio.kafka.consumer.internal.PollHistory.PollHistoryImpl

  private[internal] implicit class RichPollHistory(private val ph: PollHistory) extends AnyVal {
    def asBitString: String =
      ph.asInstanceOf[PollHistoryImpl].resumeBits.toBinaryString
  }

  private[internal] implicit class PollHistoryOps(private val s: String) extends AnyVal {
    def toPollHistory: PollHistory =
      s.foldLeft(PollHistory.Empty) { case (ph, b) =>
        ph.addPollHistory(b == '1')
      }
  }

}
