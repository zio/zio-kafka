package zio.kafka.serde

import org.apache.kafka.common.header.internals.RecordHeaders
import zio.test.Assertion._
import zio.test._
import zio.{ ZAny, ZIO }
import zio.kafka.ZIOSpecDefaultSlf4j

object SerializerSpec extends ZIOSpecDefaultSlf4j {
  override def spec: Spec[ZAny with Any, Throwable] = suite("Serializer")(
    suite("asOption")(
      test("serialize None values to null") {
        assert(stringSerializer.asOption.serialize("topic1", new RecordHeaders, None))(isNull)
      },
      test("serialize Some values") {
        check(Gen.string) { string =>
          assertZIO(
            ZIO
              .succeed(stringSerializer.asOption.serialize("topic1", new RecordHeaders, Some(string)))
              .map(new String(_, "UTF-8"))
          )(
            equalTo(string)
          )
        }
      }
    )
  )
  private lazy val stringSerializer: Serializer[String] = Serde.string
}
