package zio.kafka.serde

import zio.kafka.ZIOSpecDefaultSlf4j
import zio.test._

object SerdesInitializationSpec extends ZIOSpecDefaultSlf4j {
  override def spec: Spec[Any, Any] =
    suite("SerdesInitializationSpec")(
      test("accessing Deserializer first does not result in null fields and preserves instance equality") {
        assertTrue(
          Deserializer.byteArray != null,
          Deserializer.string != null,
          Deserializer.int != null,
          Deserializer.long != null,
          Deserializer.short != null,
          Deserializer.float != null,
          Deserializer.double != null,
          Deserializer.bytes != null,
          Deserializer.byteBuffer != null,
          Deserializer.uuid != null,
          Deserializer.longOption != null,
          Deserializer.intOption != null,
          Deserializer.shortOption != null,
          Deserializer.floatOption != null,
          Deserializer.doubleOption != null,
          Serde.byteArray != null,
          Serde.string != null,
          Serializer.byteArray != null,
          Serializer.string != null,
          Deserializer.byteArray eq Serde.byteArray,
          Serializer.byteArray eq Serde.byteArray
        )
      }
    )
}
