package zio.kafka.consumer.internal

import zio.Chunk

/**
 * Optimistically resume partitions for streams that are likely to need more data _before_ the next poll.
 */
private[internal] object OptimisticResume {

  /**
   * A sequence of partition statuses (resumed or paused) before a poll. We use one bit per poll, where value 1
   * indicates that the partition was resumed and value 0 indicates it was paused.
   *
   * The most recent poll is in the least significant bit, the oldest poll is in the most significant bit.
   */
  type PollHistory = Int

  implicit class PollHistoryOps(val pollHistory: PollHistory) extends AnyVal {

    /**
     * @return
     *   true when based on the poll history an optimistic resume is possible
     */
    def optimisticResume: Boolean =
      OptimisticResumePollPatterns.exists { case (mask, pattern) =>
        (pollHistory & mask) == pattern
      }

    /**
     * Updates a poll history by appending the given partition status.
     *
     * @param resumed
     *   true when this partition was resumed before the poll, false when it was paused
     */
    def addPollHistory(resumed: Boolean): PollHistory =
      pollHistory << 1 | (if (resumed) 1 else 0)
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
