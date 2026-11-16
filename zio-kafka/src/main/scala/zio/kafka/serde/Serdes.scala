package zio.kafka.serde

import org.apache.kafka.common.header.Headers
import org.apache.kafka.common.serialization.{ Serde => KafkaSerde, Serdes => KafkaSerdes }
import org.apache.kafka.common.utils.Bytes
import zio.{ RIO, ZIO }

import java.nio.ByteBuffer
import java.util.UUID

/**
 * @define simplePrimitiveDeserialization
 *   A tombstone value or missing key (Kafka's `null`) gets deserialized to a zero `0`. Use the option variant (e.g.
 *   `Serde.longOption`) to deserialize to an `Option` where `null` is deserialized as `None`.
 * @define nullDeserialization
 *   A tombstone value or missing key is returned as `null`. Combine with `.asOption` (e.g. `Serde.bytes.asOption`) to
 *   deserialize to an `Option` where `null` is deserialized as `None`.
 */

private[zio] trait Serdes {
  val longOption: Serde[Any, Option[Long]]     = Serdes.longOption
  val intOption: Serde[Any, Option[Int]]       = Serdes.intOption
  val shortOption: Serde[Any, Option[Short]]   = Serdes.shortOption
  val floatOption: Serde[Any, Option[Float]]   = Serdes.floatOption
  val doubleOption: Serde[Any, Option[Double]] = Serdes.doubleOption

  /** $simplePrimitiveDeserialization */
  val long: Serde[Any, Long] = Serdes.long

  /** $simplePrimitiveDeserialization */
  val int: Serde[Any, Int] = Serdes.int

  /** $simplePrimitiveDeserialization */
  val short: Serde[Any, Short] = Serdes.short

  /** $simplePrimitiveDeserialization */
  val float: Serde[Any, Float] = Serdes.float

  /** $simplePrimitiveDeserialization */
  val double: Serde[Any, Double] = Serdes.double

  /** $nullDeserialization */
  val string: Serde[Any, String] = Serdes.string

  /** $nullDeserialization */
  val bytes: Serde[Any, Bytes] = Serdes.bytes

  /** $nullDeserialization */
  val byteBuffer: Serde[Any, ByteBuffer] = Serdes.byteBuffer

  /** $nullDeserialization */
  val uuid: Serde[Any, UUID] = Serdes.uuid

  /** $nullDeserialization */
  val byteArray: Serde[Any, Array[Byte]] = Serdes.byteArray
}

private[zio] object Serdes {
  val longOption: Serde[Any, Option[Long]] =
    inmapOption(convertPrimitiveSerde(KafkaSerdes.Long()), Long2long, long2Long)
  val intOption: Serde[Any, Option[Int]] =
    inmapOption(convertPrimitiveSerde(KafkaSerdes.Integer()), Integer2int, int2Integer)
  val shortOption: Serde[Any, Option[Short]] =
    inmapOption(convertPrimitiveSerde(KafkaSerdes.Short()), Short2short, short2Short)
  val floatOption: Serde[Any, Option[Float]] =
    inmapOption(convertPrimitiveSerde(KafkaSerdes.Float()), Float2float, float2Float)
  val doubleOption: Serde[Any, Option[Double]] =
    inmapOption(convertPrimitiveSerde(KafkaSerdes.Double()), Double2double, double2Double)

  val long: Serde[Any, Long]     = convertPrimitiveSerde(KafkaSerdes.Long()).inmap(Long2long)(long2Long)
  val int: Serde[Any, Int]       = convertPrimitiveSerde(KafkaSerdes.Integer()).inmap(Integer2int)(int2Integer)
  val short: Serde[Any, Short]   = convertPrimitiveSerde(KafkaSerdes.Short()).inmap(Short2short)(short2Short)
  val float: Serde[Any, Float]   = convertPrimitiveSerde(KafkaSerdes.Float()).inmap(Float2float)(float2Float)
  val double: Serde[Any, Double] = convertPrimitiveSerde(KafkaSerdes.Double()).inmap(Double2double)(double2Double)
  val string: Serde[Any, String] = convertPrimitiveSerde(KafkaSerdes.String())
  val bytes: Serde[Any, Bytes]   = convertPrimitiveSerde(KafkaSerdes.Bytes())
  val byteBuffer: Serde[Any, ByteBuffer] = convertPrimitiveSerde(KafkaSerdes.ByteBuffer())

  val uuid: Serde[Any, UUID] = convertPrimitiveSerde(KafkaSerdes.UUID())

  /**
   * Optimisation
   *
   * Here, we're not using `KafkaSerdes.ByteArray()` because the underlying deserializer and serializer implementations
   * are just identity functions.
   *
   * That allows us to use [[ZIO.succeed]] instead of [[ZIO.attempt]].
   */
  val byteArray: Serde[Any, Array[Byte]] =
    new Serde[Any, Array[Byte]] {
      override final def serialize(topic: String, headers: Headers, value: Array[Byte]): RIO[Any, Array[Byte]] =
        ZIO.succeed(value)

      override final def deserialize(topic: String, headers: Headers, data: Array[Byte]): RIO[Any, Array[Byte]] =
        ZIO.succeed(data)
    }

  private[this] def convertPrimitiveSerde[A](serde: KafkaSerde[A]): Serde[Any, A] =
    new Serde[Any, A] {
      private final val serializer   = serde.serializer()
      private final val deserializer = serde.deserializer()

      override final def deserialize(topic: String, headers: Headers, data: Array[Byte]): RIO[Any, A] =
        ZIO.attempt(deserializer.deserialize(topic, headers, data))

      override final def serialize(topic: String, headers: Headers, value: A): RIO[Any, Array[Byte]] =
        ZIO.attempt(serializer.serialize(topic, headers, value))
    }

  private[this] def inmapOption[A, B](serde: Serde[Any, A], f: A => B, g: B => A): Serde[Any, Option[B]] =
    Serde(serde.map(f).asOption)(serde.contramap(g).asOption)

}
