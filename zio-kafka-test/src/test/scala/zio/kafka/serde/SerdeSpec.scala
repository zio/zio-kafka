package zio.kafka.serde

import org.apache.kafka.common.header.internals.RecordHeaders
import zio.kafka.ZIOSpecDefaultSlf4j
import zio.test.Assertion._
import zio.test._

import scala.reflect.ClassTag

object SerdeSpec extends ZIOSpecDefaultSlf4j {
  final case class TestDataStructure(value: String)

  val testDataStructureSerde: Serde[Any, TestDataStructure] =
    Serde.string.inmap[TestDataStructure](TestDataStructure.apply)(_.value)

  private val anyBytes = Gen.listOf(Gen.byte).map(bytes => new org.apache.kafka.common.utils.Bytes(bytes.toArray))

  override def spec: Spec[Any, Throwable] = suite("Serde")(
    testSerde(Serde.string, Gen.string),
    testSerde(Serde.int, Gen.int),
    testSerde(Serde.short, Gen.short),
    testSerde(Serde.float, Gen.float),
    testSerde(Serde.double, Gen.double),
    testSerde(Serde.long, Gen.long),
    testSerde(Serde.uuid, Gen.uuid),
    testSerde(Serde.bytes, anyBytes),
    testSerde(Serde.byteArray, Gen.listOf(Gen.byte).map(_.toArray)),
    testSerde(Serde.intOption, Gen.option(Gen.int), "Int"),
    testSerde(Serde.shortOption, Gen.option(Gen.short), "Short"),
    testSerde(Serde.floatOption, Gen.option(Gen.float), "Float"),
    testSerde(Serde.doubleOption, Gen.option(Gen.double), "Double"),
    testSerde(Serde.longOption, Gen.option(Gen.long), "Long"),
    suite("asOption")(
      test("serialize and deserialize None values to null and visa versa") {
        val serde = testDataStructureSerde.asOption
        for {
          serialized   <- serde.serialize("topic1", new RecordHeaders, None)
          deserialized <- serde.deserialize("topic1", new RecordHeaders, serialized)
        } yield assertTrue(serialized == null) && assert(deserialized)(isNone)
      }
    ),
    testPrimitiveNullDeser(Serde.int, 0),
    testPrimitiveNullDeser(Serde.short, 0: Short),
    testPrimitiveNullDeser(Serde.float, 0f),
    testPrimitiveNullDeser(Serde.double, 0d),
    testPrimitiveNullDeser(Serde.long, 0L),
    suite("shared instances")(
      test("Serde, Deserializer and Serializer share the same instances") {
        assertTrue(
          (Serde.byteArray eq Deserializer.byteArray) && (Serde.byteArray eq Serializer.byteArray),
          (Serde.string eq Deserializer.string) && (Serde.string eq Serializer.string),
          (Serde.int eq Deserializer.int) && (Serde.int eq Serializer.int),
          (Serde.long eq Deserializer.long) && (Serde.long eq Serializer.long),
          (Serde.short eq Deserializer.short) && (Serde.short eq Serializer.short),
          (Serde.float eq Deserializer.float) && (Serde.float eq Serializer.float),
          (Serde.double eq Deserializer.double) && (Serde.double eq Serializer.double),
          (Serde.bytes eq Deserializer.bytes) && (Serde.bytes eq Serializer.bytes),
          (Serde.byteBuffer eq Deserializer.byteBuffer) && (Serde.byteBuffer eq Serializer.byteBuffer),
          (Serde.uuid eq Deserializer.uuid) && (Serde.uuid eq Serializer.uuid),
          (Serde.longOption eq Deserializer.longOption) && (Serde.longOption eq Serializer.longOption),
          (Serde.intOption eq Deserializer.intOption) && (Serde.intOption eq Serializer.intOption),
          (Serde.shortOption eq Deserializer.shortOption) && (Serde.shortOption eq Serializer.shortOption),
          (Serde.floatOption eq Deserializer.floatOption) && (Serde.floatOption eq Serializer.floatOption),
          (Serde.doubleOption eq Deserializer.doubleOption) && (Serde.doubleOption eq Serializer.doubleOption)
        )
      }
    )
  )

  private def testSerde[R, A](serde: Serde[Any, A], gen: Gen[R, A], extra: String = "")(implicit clsTag: ClassTag[A]) =
    test(s"serialize and deserialize ${clsTag.runtimeClass.getSimpleName} $extra") {
      check(gen) { value =>
        for {
          serialized   <- serde.serialize("topic1", new RecordHeaders, value)
          deserialized <- serde.deserialize("topic1", new RecordHeaders, serialized)
        } yield assertTrue(deserialized == value)
      }
    }

  private def testPrimitiveNullDeser[R, A](serde: Serde[Any, A], zero: A)(implicit clsTag: ClassTag[A]) =
    test(s"deserialize ${clsTag.runtimeClass.getSimpleName} null") {
      for {
        deserialized <- serde.deserialize("topic1", new RecordHeaders, null)
      } yield assertTrue(deserialized == zero)
    }

}
