package zio.kafka.serde

import org.apache.kafka.common.header.internals.RecordHeaders
import zio.test.Assertion._
import zio.test._

import scala.reflect.ClassTag

object SerdeSpec extends DefaultRunnableSpec {
  override def spec = suite("Serde")(
    testSerde(Serde.string, Gen.anyString),
    testSerde(Serde.int, Gen.anyInt),
    testSerde(Serde.short, Gen.anyShort),
    testSerde(Serde.float, Gen.anyFloat),
    testSerde(Serde.double, Gen.anyDouble),
    testSerde(Serde.long, Gen.anyLong),
    testSerde(Serde.uuid, Gen.anyUUID),
    testSerde(Serde.byteArray, Gen.listOf(Gen.anyByte).map(_.toArray)),
    suite("asOption")(
      testM("serialize and deserialize None values to null and visa versa") {
        val serde = Serde.string.asOption
        for {
          serialized   <- serde.serialize("topic1", new RecordHeaders, None)
          deserialized <- serde.deserialize("topic1", new RecordHeaders, serialized)
        } yield assert(serialized)(isNull) && assert(deserialized)(isNone)
      }
    )
  )

  private def testSerde[R, A](serde: Serde[Any, A], gen: Gen[R, A])(implicit clsTag: ClassTag[A]) =
    testM(s"serialize and deserialize ${clsTag.runtimeClass.getSimpleName}") {
      checkM(gen) { value =>
        for {
          serialized   <- serde.serialize("topic1", new RecordHeaders, value)
          deserialized <- serde.deserialize("topic1", new RecordHeaders, serialized)
        } yield assert(deserialized)(equalTo(deserialized))
      }
    }

}
