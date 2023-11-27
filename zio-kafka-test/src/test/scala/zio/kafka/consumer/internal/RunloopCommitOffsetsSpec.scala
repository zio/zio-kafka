package zio.kafka.consumer.internal

import org.apache.kafka.common.TopicPartition
import zio._
import org.apache.kafka.clients.consumer.OffsetAndMetadata
import zio.test._

object RunloopCommitOffsetsSpec extends ZIOSpecDefault {

  private val tp10 = new TopicPartition("t1", 0)
  private val tp11 = new TopicPartition("t1", 1)
  private val tp20 = new TopicPartition("t2", 0)
  private val tp21 = new TopicPartition("t2", 1)
  private val tp22 = new TopicPartition("t2", 2)

  override def spec: Spec[TestEnvironment with Scope, Any] =
    suite("Runloop.CommitOffsets spec")(
      test("addCommits adds to empty CommitOffsets") {
        val s1 = Runloop.CommitOffsets(Map.empty)
        val s2 = s1.addCommits(Chunk(makeCommit(Map(tp10 -> 10))))
        assertTrue(s2.offsets == Map(tp10 -> 10L))
      },
      test("addCommits updates offset when it is higher") {
        val s1 = Runloop.CommitOffsets(Map(tp10 -> 5L))
        val s2 = s1.addCommits(Chunk(makeCommit(Map(tp10 -> 10))))
        assertTrue(s2.offsets == Map(tp10 -> 10L))
      },
      test("addCommits ignores an offset when it is lower") {
        val s1 = Runloop.CommitOffsets(Map(tp10 -> 10L))
        val s2 = s1.addCommits(Chunk(makeCommit(Map(tp10 -> 5))))
        assertTrue(s2.offsets == Map(tp10 -> 10L))
      },
      test("addCommits keeps unrelated partitions") {
        val s1 = Runloop.CommitOffsets(Map(tp10 -> 10L))
        val s2 = s1.addCommits(Chunk(makeCommit(Map(tp11 -> 11))))
        assertTrue(s2.offsets == Map(tp10 -> 10L, tp11 -> 11L))
      },
      test("addCommits does it all at once") {
        val s1 = Runloop.CommitOffsets(Map(tp10 -> 10L, tp20 -> 205L, tp21 -> 210L, tp22 -> 220L))
        val s2 = s1.addCommits(Chunk(makeCommit(Map(tp11 -> 11, tp20 -> 206L, tp21 -> 209L, tp22 -> 220L))))
        assertTrue(s2.offsets == Map(tp10 -> 10L, tp11 -> 11L, tp20 -> 206L, tp21 -> 210L, tp22 -> 220L))
      },
      test("addCommits adds multiple commits") {
        val s1 = Runloop.CommitOffsets(Map(tp10 -> 10L, tp20 -> 200L, tp21 -> 210L, tp22 -> 220L))
        val s2 = s1.addCommits(
          Chunk(
            makeCommit(Map(tp11 -> 11, tp20 -> 199L, tp21 -> 211L, tp22 -> 219L)),
            makeCommit(Map(tp20 -> 198L, tp21 -> 209L, tp22 -> 221L))
          )
        )
        assertTrue(s2.offsets == Map(tp10 -> 10L, tp11 -> 11L, tp20 -> 200L, tp21 -> 211L, tp22 -> 221L))
      },
      test("keepPartitions removes some partitions") {
        val s1 = Runloop.CommitOffsets(Map(tp10 -> 10L, tp20 -> 20L))
        val s2 = s1.keepPartitions(Set(tp10))
        assertTrue(s2.offsets == Map(tp10 -> 10L))
      },
      test("does not 'contain' offset when tp is not present") {
        val s1     = Runloop.CommitOffsets(Map(tp10 -> 10L))
        val result = s1.contains(tp20, 10)
        assertTrue(!result)
      },
      test("does not 'contain' a higher offset") {
        val s1     = Runloop.CommitOffsets(Map(tp10 -> 10L, tp20 -> 20L))
        val result = s1.contains(tp10, 11)
        assertTrue(!result)
      },
      test("does 'contain' equal offset") {
        val s1     = Runloop.CommitOffsets(Map(tp10 -> 10L, tp20 -> 20L))
        val result = s1.contains(tp10, 10)
        assertTrue(result)
      },
      test("does 'contain' lower offset") {
        val s1     = Runloop.CommitOffsets(Map(tp10 -> 10L, tp20 -> 20L))
        val result = s1.contains(tp20, 19)
        assertTrue(result)
      }
    )

  private def makeCommit(offsets: Map[TopicPartition, Long]): Runloop.Commit = {
    val o = offsets.map { case (tp, offset) => tp -> new OffsetAndMetadata(offset) }
    val p = Unsafe.unsafe(implicit unsafe => Promise.unsafe.make[Throwable, Unit](FiberId.None))
    Runloop.Commit(o, p)
  }
}
