package zio.kafka.serde

import org.apache.kafka.common.header.Headers
import org.apache.kafka.common.serialization.{ Serde => KafkaSerde, Serdes => KafkaSerdes }
import org.apache.kafka.common.utils.Bytes
import zio.{ IO, ZIO }

import java.nio.ByteBuffer
import java.util.UUID

private[zio] trait Serdes {
  val long: Serde[Throwable, Long]     = convertPrimitiveSerde(KafkaSerdes.Long()).inmap(Long2long)(long2Long)
  val int: Serde[Throwable, Int]       = convertPrimitiveSerde(KafkaSerdes.Integer()).inmap(Integer2int)(int2Integer)
  val short: Serde[Throwable, Short]   = convertPrimitiveSerde(KafkaSerdes.Short()).inmap(Short2short)(short2Short)
  val float: Serde[Throwable, Float]   = convertPrimitiveSerde(KafkaSerdes.Float()).inmap(Float2float)(float2Float)
  val double: Serde[Throwable, Double] = convertPrimitiveSerde(KafkaSerdes.Double()).inmap(Double2double)(double2Double)
  val string: Serde[Throwable, String] = convertPrimitiveSerde(KafkaSerdes.String())
  val bytes: Serde[Throwable, Bytes]   = convertPrimitiveSerde(KafkaSerdes.Bytes())
  val byteBuffer: Serde[Throwable, ByteBuffer] = convertPrimitiveSerde(KafkaSerdes.ByteBuffer())
  val uuid: Serde[Throwable, UUID]             = convertPrimitiveSerde(KafkaSerdes.UUID())

  /**
   * Optimisation
   *
   * Here, we're not using `KafkaSerdes.ByteArray()` because the underlying deserializer and serializer implementations
   * are just identity functions.
   *
   * That allows us to use [[ZIO.succeed]] instead of [[ZIO.attempt]].
   */
  val byteArray: Serde[Nothing, Array[Byte]] =
    new Serde[Nothing, Array[Byte]] {
      override final def serialize(topic: String, headers: Headers, value: Array[Byte]): Array[Byte] =
        value

      override final def deserialize(topic: String, headers: Headers, data: Array[Byte]): IO[Nothing, Array[Byte]] =
        ZIO.succeed(data)
    }

  private[this] def convertPrimitiveSerde[T](serde: KafkaSerde[T]): Serde[Throwable, T] =
    new Serde[Throwable, T] {
      private final val serializer   = serde.serializer()
      private final val deserializer = serde.deserializer()

      override final def deserialize(topic: String, headers: Headers, data: Array[Byte]): IO[Throwable, T] =
        ZIO.attempt(deserializer.deserialize(topic, headers, data))

      override final def serialize(topic: String, headers: Headers, value: T): Array[Byte] =
        serializer.serialize(topic, headers, value)
    }
}
