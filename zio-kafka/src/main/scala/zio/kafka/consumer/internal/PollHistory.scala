package zio.kafka.consumer.internal

import zio.Chunk

/**
 * Keep track of a partition status ('resumed' or 'paused') history as it is just before a poll.
 *
 * The goal is to predict when the partition is likely to be resumed in the next poll.
 */
private[internal] sealed trait PollHistory {

  /**
   * @return
   *   true when -based on the poll history- the partition is likely to be resumed in the next poll
   */
  def optimisticResume: Boolean

  /**
   * Creates a new poll history by appending the given partition status as the latest poll. The history length might be
   * limited. When the maximum length is reached, older poll are discarded.
   *
   * @param resumed
   *   true when this partition was 'resumed' before the poll, false when it was 'paused'
   */
  def addPollHistory(resumed: Boolean): PollHistory

  /**
   * @return
   *   the recorded history as a bit string where each character is either a '1': the partition was resumed or '0': the
   *   partition was paused. The oldest poll comes first and the newest last. Leading zeros are removed. Since the
   *   history length is limited, the return string will always have a bounded length.
   */
  def asBitString: String
}

private[internal] object PollHistory {

  /**
   * Patterns that, when they match the end of a poll history, indicate that the partition is likely to be resumed in
   * the next poll. The pattern is a bit string as described in [[PollHistory.asBitString]] except that leading zeros
   * are kept and need to match as well.
   *
   * '''Run-away feedback loop'''
   *
   * If a pattern matches, the partition will be resumed (causing a `1` to be added to the poll history). If in the next
   * poll, again a pattern matches, the partition will be resumed again, causing a run-away feedback loop of repeated
   * resumes while the stream might not need more data at all. (We do not make a distinction between resumes because of
   * a request for data, or because of an optimistic resume.)
   *
   * For example, the very short pattern `1` causes a run-away feedback loop immediately after the first resume. For the
   * same reason, if pattern `p` is included, `p1` may _not_ be included. (For example, if pattern `101` is included,
   * pattern `1011` may not be included.)
   *
   * '''How these patterns were found'''
   *
   * The patterns were found by iterating over all possible (short) patterns and selecting those that are likely to lead
   * to a resume. Iteration is done by the number of recent resumes or pauses. So first we list all patterns that end
   * with 5 resumes, then all patterns that end with 4 resumes, etc. Then we list all patterns that end with 1 pause,
   * with 2 pauses, etc.
   *
   * For example, lets look at all patterns that end with 2 resumes, so patterns that end with `011`. Just 2 resumes is
   * not a strong signal that the partition is likely to be resumed in the next poll. Therefore, we require that a least
   * 2 more resumes happened shortly before. (Note that this is a choice based on intuition. More research is needed to
   * select optimal requirements.) We do this by extending the pattern to the left. The patterns with at least 2 resumes
   * in 3 polls are: `11`, `101` and `110` (note: `11` matches both `011` and `111`). Prepending this to `011` gives the
   * patterns `11011`, `101011` and `110011`. An arbitrary choice was made to omit patterns with 2 consecutive pauses.
   *
   * Some patterns are not allowed because we want to prevent a run-away feedback loop. For example, because we list
   * pattern `0111`, pattern `01111` is not allowed. So instead of `01111` we include `11101111`.
   *
   * '''Mask and bit pattern'''
   *
   * The patterns are converted into pairs containing a mask and a bit pattern. The mask selects the history bits that
   * need to match the bit pattern.
   */
  private val OptimisticResumePollPatterns: Chunk[(Int, Int)] =
    Chunk(
      // Patterns ending with 5 resumes
      "011111",
      // Patterns ending with 4 resumes (require 3 preceding resumes to prevent run-away feedback loop)
      "11101111",
      // Patterns ending with 3 resumes
      "0111",
      // Patterns ending with 2 resumes (require 2 resumes in 3 polls preceding)
      "11011",
      "101011",
      // Patterns ending with 1 resume (require 3 resumes in 4 polls preceding)
      "11101",
      "101101",
      "110101",
      // Patterns ending with 1 pause (require 2 resumes in 3 polls preceding)
      "110",
      "1010",
      // Patterns ending with 2 pauses (require 3 resumes in 5 polls preceding)
      "11100",
      "101100",
      "110100",
      "1001100",
      "1010100",
      "1100100"
    ).map { pattern =>
      val bitPattern = Integer.parseUnsignedInt(pattern, 2)
      val mask       = (1 << pattern.length) - 1
      assert(
        mask != bitPattern,
        "A pattern of all 1s causes a runaway feedback loop, effectively disabling partition pausing"
      )
      (mask, bitPattern)
    }

  /**
   * An implementation of [[PollHistory]] that stores the poll statuses as bits in an unsigned [[Int]].
   *
   * Bit value 1 indicates that the partition was resumed and value 0 indicates it was paused. The most recent poll is
   * in the least significant bit, the oldest poll is in the most significant bit.
   */
  private final class PollHistoryImpl private[PollHistory] (resumeBits: Int) extends PollHistory {
    override val optimisticResume: Boolean =
      OptimisticResumePollPatterns.exists { case (mask, pattern) =>
        (resumeBits & mask) == pattern
      }

    override def addPollHistory(resumed: Boolean): PollHistory =
      new PollHistoryImpl(resumeBits << 1 | (if (resumed) 1 else 0))

    override def asBitString: String = resumeBits.toBinaryString
  }

  /** An empty poll history. */
  val Empty: PollHistory = new PollHistoryImpl(0)
}
