package zio.kafka.client.serde
import java.nio.ByteBuffer
import java.util.UUID

import org.apache.kafka.common.serialization.{ Serdes => KafkaSerdes }

import scala.util.Try

trait Serdes {
  implicit val long: Serde[Any, Long]   = Serde(KafkaSerdes.Long()).inmap(Long2long)(long2Long)
  implicit val int: Serde[Any, Int]     = Serde(KafkaSerdes.Integer()).inmap(Integer2int)(int2Integer)
  implicit val short: Serde[Any, Short] = Serde(KafkaSerdes.Short()).inmap(Short2short)(short2Short)
  implicit val float: Serde[Any, Float] = Serde(KafkaSerdes.Float()).inmap(Float2float)(float2Float)
  implicit val double: Serde[Any, Double] =
    Serde(KafkaSerdes.Double()).inmap(Double2double)(double2Double)
  implicit val string: Serde[Any, String]         = Serde(KafkaSerdes.String())
  implicit val byteArray: Serde[Any, Array[Byte]] = Serde(KafkaSerdes.ByteArray())
  implicit val byteBuffer: Serde[Any, ByteBuffer] = Serde(KafkaSerdes.ByteBuffer())
  implicit val uuid: Serde[Any, UUID]             = Serde(KafkaSerdes.UUID())

  implicit def deserializerWithError[R, T](implicit deser: Deserializer[R, T]): Deserializer[R, Try[T]] =
    deser.asTry
}

object Serdes extends Serdes
