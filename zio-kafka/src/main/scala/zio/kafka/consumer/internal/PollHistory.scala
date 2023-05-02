package zio.kafka.consumer.internal

import zio.Chunk
import zio.kafka.consumer.internal.PollHistory.PoolHistoryBit

/**
 * Keep track of the partition status ('resumed' or 'paused') just before a poll.
 *
 * The goal is to predict when a the stream is likely to need more data _before_ the next poll.
 */
private[internal] trait PollHistory {

  /**
   * @return
   *   true when -based on the poll history- the stream is likely to need more data _before_ the next poll
   */
  def optimisticResume: Boolean

  /**
   * Creates a new poll history by appending the given partition status as the latest poll.
   *
   * @param resumed
   *   true when this partition was 'resumed' before the poll, false when it was 'paused'
   */
  def addPollHistory(resumed: PoolHistoryBit): PollHistory

  /**
   * @return
   *   true when the partition status in the latest poll was 'resumed', false when it was 'paused'
   */
  def latestWasResumed: Boolean
}

private[internal] object PollHistory {

  /**
   * An empty poll history.
   *
   * The implementation is optimized but is equivalent to `new PollHistoryImpl(0)`.
   */
  private object EmptyPollHistory extends PollHistory {
    override def optimisticResume: Boolean                            = false
    override def addPollHistory(resumed: PoolHistoryBit): PollHistory = new PollHistoryImpl(resumed)
    override def latestWasResumed: Boolean                            = false
  }

  val Empty: PollHistory = EmptyPollHistory

  type PoolHistoryBit = Int
  // `final` allows the compiler to inline these values
  final val Resumed: PoolHistoryBit = 1
  final val Paused: PoolHistoryBit  = 0

  private final class PollHistoryImpl private[PollHistory] (resumeBits: Int) extends PollHistory {
    // ResumeBits is a sequence of partition statuses (resumed or paused) before a poll.
    // We use one bit per poll, where value 1 indicates that the partition was resumed
    // and value 0 indicates it was paused.
    //
    // The most recent poll is in the least significant bit, the oldest poll is in the most significant bit.

    override val optimisticResume: Boolean =
      OptimisticResumePollPatterns.exists { case (mask, pattern) =>
        (resumeBits & mask) == pattern
      }

    override def addPollHistory(resumed: PoolHistoryBit): PollHistory =
      new PollHistoryImpl(resumeBits << 1 | resumed)

    override val latestWasResumed: Boolean = (resumeBits & 1) == 1
  }

  /**
   * Patterns that, when they match the end of a poll history, warrant an optimistic poll.
   *
   * Warning! Be aware of the feedback loop:
   *
   * If a pattern matches, the partition will be resumed (adding a `1` to the poll history). If in the next poll,
   * another pattern matches, the partition will be resumed again. This is not desirable because the stream might not
   * more data already.
   *
   * As a consequence, if pattern `p` is included, `p1` may _not_ be included.
   */
  private val OptimisticResumePollPatterns: Chunk[(Int, Int)] =
    Chunk(
      // Patterns ending with 5 resumes
      "011111",
      // Patterns ending with 4 resumes (require preceding resumes to break feedback cycle)
      "11101111",
      // Patterns ending with 3 resumes
      "0111",
      // Patterns ending with 2 resumes (require 2 resumes preceding)
      "11011",
      "101011",
      // Patterns ending with 1 resume (require 3 resumes preceding)
      "11101",
      "101101",
      "110101",
      // Patterns ending with 1 pause (require 2 resumes preceding)
      "110",
      "1010",
      // Patterns ending with 2 pauses (require 3 resumes preceding)
      "11100",
      "101100",
      "110100",
      "1001100",
      "1010100",
      "1100100"
    ).map { pattern =>
      val bitPattern = Integer.parseInt(pattern, 2)
      val mask       = (1 << pattern.length) - 1
      assert(
        mask != bitPattern,
        "A pattern of all 1s causes a runaway feedback loop, effectively disabling partition pausing"
      )
      (mask, bitPattern)
    }

}
