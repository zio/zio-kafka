package zio.kafka.consumer

import org.apache.kafka.clients.consumer._
import org.apache.kafka.common.TopicPartition
import zio._
import zio.kafka.ZIOSpecDefaultSlf4j
import zio.test._

import scala.jdk.OptionConverters.RichOption

//noinspection OptionEqualsSome
object OffsetBatchSpec extends ZIOSpecDefaultSlf4j {

  private val tpA1 = new TopicPartition("a", 1)
  private val tpA2 = new TopicPartition("a", 2)
  private val tpB1 = new TopicPartition("b", 1)
  private val tpB2 = new TopicPartition("b", 2)
  private val tpC1 = new TopicPartition("c", 1)

  override def spec: Spec[TestEnvironment with Scope, Throwable] =
    suite("OffsetBatchSpec")(
      test("empty/apply/add") {
        val offsetAndMetadata = makeOffsetAndMetaData(150)
        val batch             = OffsetBatch(makeOffset(tpA1, offsetAndMetadata))
        assertTrue(
          batch.nextOffsets == Map(tpA1 -> offsetAndMetadata)
        )
      },
      test("add keeps highest offset with associated epoch+meta") {
        // Note: `OffsetBatch.apply` does a foldLeft with `add`
        val batch = OffsetBatch(
          Seq(
            makeOffset(tpA1, makeOffsetAndMetaData(150, 1, "m1")),
            makeOffset(tpA2, makeOffsetAndMetaData(250, 2, "m2")),
            makeOffset(tpB1, makeOffsetAndMetaData(350, 3, "m3")),
            makeOffset(tpA1, makeOffsetAndMetaData(149, 4, "m4")),
            makeOffset(tpA2, makeOffsetAndMetaData(250, 5, "m5")),
            makeOffset(tpB1, makeOffsetAndMetaData(351, 6, "m6"))
          )
        )
        assertTrue(
          batch.nextOffsets == Map(
            tpA1 -> makeOffsetAndMetaData(150, 1, "m1"),
            tpA2 -> makeOffsetAndMetaData(250, 2, "m2"), // equal offset, keep first epoch+meta
            tpB1 -> makeOffsetAndMetaData(351, 6, "m6")
          )
        )
      },
      test("merge keeps highest offset with associated epoch+meta") {
        val batch1 = OffsetBatch(
          Seq(
            makeOffset(tpA1, makeOffsetAndMetaData(150, 1, "m1")),
            makeOffset(tpA2, makeOffsetAndMetaData(250, 2, "m2")),
            makeOffset(tpB1, makeOffsetAndMetaData(350, 3, "m3")),
            makeOffset(tpC1, makeOffsetAndMetaData(550, 7, "m7"))
          )
        )
        val batch2 = OffsetBatch(
          Seq(
            makeOffset(tpA1, makeOffsetAndMetaData(149, 4, "m4")),
            makeOffset(tpA2, makeOffsetAndMetaData(250, 5, "m5")),
            makeOffset(tpB1, makeOffsetAndMetaData(351, 5, "m5")),
            makeOffset(tpB2, makeOffsetAndMetaData(450, 6, "m6"))
          )
        )
        assertTrue(
          batch1.merge(batch2).nextOffsets == Map(
            tpA1 -> makeOffsetAndMetaData(150, 1, "m1"),
            tpA2 -> makeOffsetAndMetaData(250, 2, "m2"), // equal offset, keep first epoch+meta
            tpB1 -> makeOffsetAndMetaData(351, 5, "m5"),
            tpB2 -> makeOffsetAndMetaData(450, 6, "m6"),
            tpC1 -> makeOffsetAndMetaData(550, 7, "m7")
          )
        )
      },
      test("commit passes nextOffset in a single call to the first commit handler") {
        for {
          ref <- Ref.make[Seq[Map[TopicPartition, OffsetAndMetadata]]](Seq.empty)
          handle = (m: Map[TopicPartition, OffsetAndMetadata]) => ref.update(_.appended(m))
          batch = OffsetBatch(
                    Seq(
                      makeOffset(tpA1, makeOffsetAndMetaData(150), handle),
                      makeOffset(tpA2, makeOffsetAndMetaData(250), _ => ZIO.unit) // handler not used
                    )
                  )
          _      <- batch.commit
          result <- ref.get
        } yield assertTrue(
          result.size == 1,
          result.head == Map(
            tpA1 -> makeOffsetAndMetaData(150),
            tpA2 -> makeOffsetAndMetaData(250)
          )
        )
      }
    )

  private def makeOffset(
    tp: TopicPartition,
    nextOffset: OffsetAndMetadata,
    handle: Map[TopicPartition, OffsetAndMetadata] => Task[Unit] = _ => ZIO.unit
  ): Offset =
    OffsetImpl(tp.topic(), tp.partition(), nextOffset.offset() - 1, nextOffset, handle, None)

  private def makeOffsetAndMetaData(nextOffset: Long, leaderEpoch: Int = 0, meta: String = ""): OffsetAndMetadata =
    new OffsetAndMetadata(nextOffset, Some(Int.box(leaderEpoch)).toJava, meta)
}
