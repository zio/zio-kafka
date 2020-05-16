package zio.kafka.serde

import org.apache.kafka.common.header.internals.RecordHeaders
import zio.test.Assertion._
import zio.test._

object SerializerSpec extends DefaultRunnableSpec {
  override def spec = suite("Serializer")(
    suite("asOption")(
      testM("serialize None values to null") {
        assertM(stringSerializer.asOption.serialize("topic1", new RecordHeaders, None))(isNull)
      },
      testM("serialize Some values") {
        checkM(Gen.anyString) { string =>
          assertM(stringSerializer.asOption.serialize("topic1", new RecordHeaders, Some(string)))(
            equalTo(string.getBytes)
          )
        }
      }
    )
  )
  private lazy val stringSerializer: Serializer[Any, String] = Serde.string
}
