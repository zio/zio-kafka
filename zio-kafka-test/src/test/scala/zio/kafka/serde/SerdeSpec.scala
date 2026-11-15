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
    testPrimitiveNullDeser(Serde.long, 0L)
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
