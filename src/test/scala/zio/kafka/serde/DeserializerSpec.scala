package zio.kafka.serde

import org.apache.kafka.common.header.internals.RecordHeaders
import zio._
import zio.test.Assertion._
import zio.test._

object DeserializerSpec extends ZIOSpecDefault {
  override def spec = suite("Deserializer")(
    suite("asOption")(
      test("deserialize to None when value is null") {
        assertZIO(stringDeserializer.asOption.deserialize("topic1", new RecordHeaders, null))(isNone)
      },
      test("deserialize to None when value is null also when underlying deserializer fails on null values") {
        val deserializer = Deserializer[Any, Nothing]((_, _, _) => ZIO.fail(new RuntimeException("cannot handle null")))
        assertZIO(deserializer.asOption.deserialize("topic1", new RecordHeaders, null))(isNone)
      },
      test("deserialize to Some when value is not null") {
        check(Gen.string) { string =>
          assertZIO(stringDeserializer.asOption.deserialize("topic1", new RecordHeaders, string.getBytes("UTF-8")))(
            isSome(equalTo(string))
          )
        }
      }
    )
  )

  private lazy val stringDeserializer: Deserializer[Any, String] = Serde.string
}
