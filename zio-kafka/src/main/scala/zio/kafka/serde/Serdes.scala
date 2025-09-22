package zio.kafka.serde

import org.apache.kafka.common.header.Headers
import org.apache.kafka.common.serialization.{ Serde => KafkaSerde, Serdes => KafkaSerdes }
import org.apache.kafka.common.utils.Bytes
import zio.{ Exit, RIO, ZIO }

import java.nio.ByteBuffer
import java.util.UUID

private[zio] trait Serdes {
  val long: Serde[Any, Long]     = convertPrimitiveSerde(KafkaSerdes.Long()).inmap(Long2long)(long2Long)
  val int: Serde[Any, Int]       = convertPrimitiveSerde(KafkaSerdes.Integer()).inmap(Integer2int)(int2Integer)
  val short: Serde[Any, Short]   = convertPrimitiveSerde(KafkaSerdes.Short()).inmap(Short2short)(short2Short)
  val float: Serde[Any, Float]   = convertPrimitiveSerde(KafkaSerdes.Float()).inmap(Float2float)(float2Float)
  val double: Serde[Any, Double] = convertPrimitiveSerde(KafkaSerdes.Double()).inmap(Double2double)(double2Double)
  val string: Serde[Any, String] = convertPrimitiveSerde(KafkaSerdes.String())
  val bytes: Serde[Any, Bytes]   = convertPrimitiveSerde(KafkaSerdes.Bytes())
  val byteBuffer: Serde[Any, ByteBuffer] = convertPrimitiveSerde(KafkaSerdes.ByteBuffer())
  val uuid: Serde[Any, UUID]             = convertPrimitiveSerde(KafkaSerdes.UUID())

  /**
   * Optimisation
   *
   * Here, we're not using `KafkaSerdes.ByteArray()` because the underlying deserializer and serializer implementations
   * are just identity functions.
   *
   * That allows us to use [[Exit.succeed]] instead of [[ZIO.attempt]].
   */
  val byteArray: Serde[Any, Array[Byte]] =
    new Serde[Any, Array[Byte]] {
      override final def serialize(topic: String, headers: Headers, value: Array[Byte]): RIO[Any, Array[Byte]] =
        Exit.succeed(value)

      override final def deserialize(topic: String, headers: Headers, data: Array[Byte]): RIO[Any, Array[Byte]] =
        Exit.succeed(data)
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
}
