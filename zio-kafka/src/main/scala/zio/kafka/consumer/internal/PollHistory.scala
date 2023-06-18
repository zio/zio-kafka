package zio.kafka.consumer.internal

import java.lang.{ Long => JavaLong }

/**
 * Keep track of a partition status ('resumed' or 'paused') history as it is just before a poll.
 *
 * The goal is to predict in how many polls the partition will be resumed.
 */
private[internal] sealed trait PollHistory {

  /**
   * @return
   *   the estimated number of polls before the partition is resumed (a positive number). When no estimate can be made,
   *   this returns a high positive number.
   */
  def estimatedPollCountToResume: Int

  /**
   * Creates a new poll history by appending the given partition status as the latest poll. The history length might be
   * limited. When the maximum length is reached, older poll are discarded.
   *
   * @param resumed
   *   true when this partition was 'resumed' before the poll, false when it was 'paused'
   */
  def addPollHistory(resumed: Boolean): PollHistory
}

private[internal] object PollHistory {

  /**
   * An implementation of [[PollHistory]] that stores the poll statuses as bits in an unsigned [[Long]].
   *
   * Bit value 1 indicates that the partition was resumed and value 0 indicates it was paused. The most recent poll is
   * in the least significant bit, the oldest poll is in the most significant bit.
   */
  private[internal] final class PollHistoryImpl private[PollHistory] (
    /* exposed only for tests */ private[internal] val resumeBits: Long
  ) extends PollHistory {
    override def estimatedPollCountToResume: Int = {
      // This class works with 64 bits, but let's assume an 8 bit history for this example.
      // Full history is "00100100"
      // We are currently paused for 2 polls (last "00")
      // The 'before history' contains 2 polls (in "001001", 6 bits long),
      // so the average resume cycle is 6 / 2 = 3 polls,
      // and the estimated wait time before next resume is
      // average resume cycle (3) - currently pause (2) = 1 poll.

      // Now consider the pattern "0100010001000100" (16 bit history).
      // It is very regular but the estimate will be off because the oldest cycle
      // (at beginning of the bitstring) is not complete.
      // We compensate by removing the first cycle from the 'before history'.
      // This also helps predicting when the stream only just started.

      // When no resumes are observed in 'before history', we cannot estimate and we return the maximum estimate (64).

      // Also when 'before history' is too short, we can not make a prediction and we return 64.
      // We require that 'before history' is at least 16 polls long.

      val currentPausedCount   = JavaLong.numberOfTrailingZeros(resumeBits)
      val firstPollCycleLength = JavaLong.numberOfLeadingZeros(resumeBits) + 1
      val beforeHistory        = resumeBits >>> currentPausedCount
      val resumeCount          = JavaLong.bitCount(beforeHistory) - 1
      val beforeHistoryLength  = JavaLong.SIZE - firstPollCycleLength - currentPausedCount
      if (resumeCount == 0 || beforeHistoryLength < 16) {
        JavaLong.SIZE
      } else {
        val averageResumeCycleLength = Math.round(beforeHistoryLength / resumeCount.toDouble).toInt
        Math.max(0, averageResumeCycleLength - currentPausedCount)
      }
    }

    override def addPollHistory(resumed: Boolean): PollHistory =
      new PollHistoryImpl(resumeBits << 1 | (if (resumed) 1 else 0))
  }

  /** An empty poll history. */
  val Empty: PollHistory = new PollHistoryImpl(0)
}
