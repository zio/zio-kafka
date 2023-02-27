package zio.kafka.serde

import org.apache.kafka.common.header.Headers
import org.apache.kafka.common.serialization.{ Serde => KafkaSerde, Serdes => KafkaSerdes }
import org.apache.kafka.common.utils.Bytes
import zio.{ RIO, ZIO }

import java.nio.ByteBuffer
import java.util.UUID

private[zio] trait Serdes {
  lazy val long: Serde[Any, Long]     = convertPrimitiveSerde(KafkaSerdes.Long()).inmap(Long2long)(long2Long)
  lazy val int: Serde[Any, Int]       = convertPrimitiveSerde(KafkaSerdes.Integer()).inmap(Integer2int)(int2Integer)
  lazy val short: Serde[Any, Short]   = convertPrimitiveSerde(KafkaSerdes.Short()).inmap(Short2short)(short2Short)
  lazy val float: Serde[Any, Float]   = convertPrimitiveSerde(KafkaSerdes.Float()).inmap(Float2float)(float2Float)
  lazy val double: Serde[Any, Double] = convertPrimitiveSerde(KafkaSerdes.Double()).inmap(Double2double)(double2Double)
  lazy val string: Serde[Any, String] = convertPrimitiveSerde(KafkaSerdes.String())
  lazy val bytes: Serde[Any, Bytes]           = convertPrimitiveSerde(KafkaSerdes.Bytes())
  lazy val byteBuffer: Serde[Any, ByteBuffer] = convertPrimitiveSerde(KafkaSerdes.ByteBuffer())
  lazy val uuid: Serde[Any, UUID]             = convertPrimitiveSerde(KafkaSerdes.UUID())

  /**
   * Optimisation
   *
   * Here, we're not using `KafkaSerdes.ByteArray()` because the underlying deserializer and serializer implementations
   * are just identity functions.
   *
   * That allows us to use [[ZIO.succeed]] instead of [[ZIO.attempt]].
   */
  lazy val byteArray: Serde[Any, Array[Byte]] =
    new Serde[Any, Array[Byte]] {
      override final def serialize(topic: String, headers: Headers, value: Array[Byte]): RIO[Any, Array[Byte]] =
        ZIO.succeed(value)

      override final def deserialize(topic: String, headers: Headers, data: Array[Byte]): RIO[Any, Array[Byte]] =
        ZIO.succeed(data)
    }

  private[this] def convertPrimitiveSerde[T](serde: KafkaSerde[T]): Serde[Any, T] =
    new Serde[Any, T] {
      val serializer   = serde.serializer()
      val deserializer = serde.deserializer()

      override def deserialize(topic: String, headers: Headers, data: Array[Byte]): RIO[Any, T] =
        ZIO.attempt(deserializer.deserialize(topic, headers, data))

      override def serialize(topic: String, headers: Headers, value: T): RIO[Any, Array[Byte]] =
        ZIO.attempt(serializer.serialize(topic, headers, value))
    }
}
