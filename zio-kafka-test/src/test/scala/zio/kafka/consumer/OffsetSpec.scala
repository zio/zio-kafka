package zio.kafka.consumer

import org.apache.kafka.clients.consumer._
import org.apache.kafka.common.TopicPartition
import zio._
import zio.kafka.ZIOSpecDefaultSlf4j
import zio.test._

import scala.jdk.OptionConverters.{ RichOption, RichOptional }

//noinspection OptionEqualsSome
object OffsetSpec extends ZIOSpecDefaultSlf4j {

  private val offsetAndMetadata   = new OffsetAndMetadata(33L, None.toJava, "meta")
  private val offset              = OffsetImpl("t", 3, 32L, offsetAndMetadata, Map.empty, None)
  private val offsetAndMetadata71 = new OffsetAndMetadata(33L, Some(Int.box(71)).toJava, "meta")
  private val offset71            = OffsetImpl("t", 3, 32L, offsetAndMetadata71, Map.empty, None)
  private val tp                  = new TopicPartition("t", 3)

  override def spec: Spec[TestEnvironment with Scope, Throwable] =
    suite("OffsetSpec")(
      test("validate fixtures") {
        assertTrue(
          offset.offset != offset.nextOffset.offset(),
          offset71.offset != offset71.nextOffset.offset()
        )
      },
      test("delegates leaderEpoch") {
        assertTrue(
          offset.leaderEpoch.isEmpty,
          offset71.leaderEpoch.map(Int.unbox).toScala == Some(71)
        )
      },
      test("batch tracks nextOffset") {
        assertTrue(
          offset.batch.nextOffsets == Map(tp -> offsetAndMetadata)
        )
      },
      test("withMetadata") {
        assertTrue(
          offset.withMetadata("meta2").nextOffset.metadata() == "meta2"
        )
      },
      test("commit passes nextOffset in a single call to the commit handler") {
        for {
          ref <- Ref.make[Seq[Map[TopicPartition, OffsetAndMetadata]]](Seq.empty)
          handle = (m: Map[TopicPartition, OffsetAndMetadata]) => ref.update(_.appended(m))
          offset = OffsetImpl("t", 3, 32L, offsetAndMetadata, handle, None)
          _      <- offset.commit
          result <- ref.get
        } yield assertTrue(
          result.size == 1,
          result.head.keySet == Set(tp),
          result.head.get(tp).contains(offsetAndMetadata)
        )
      }
    )

}
