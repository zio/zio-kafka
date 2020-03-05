package zio.kafka.serde

import java.nio.ByteBuffer
import java.util.UUID

import org.apache.kafka.common.serialization.{ Serdes => KafkaSerdes }

private[zio] trait Serdes {
  val long: Serde[Any, Long]             = Serde(KafkaSerdes.Long()).inmap(Long2long)(long2Long)
  val int: Serde[Any, Int]               = Serde(KafkaSerdes.Integer()).inmap(Integer2int)(int2Integer)
  val short: Serde[Any, Short]           = Serde(KafkaSerdes.Short()).inmap(Short2short)(short2Short)
  val float: Serde[Any, Float]           = Serde(KafkaSerdes.Float()).inmap(Float2float)(float2Float)
  val double: Serde[Any, Double]         = Serde(KafkaSerdes.Double()).inmap(Double2double)(double2Double)
  val string: Serde[Any, String]         = Serde(KafkaSerdes.String())
  val byteArray: Serde[Any, Array[Byte]] = Serde(KafkaSerdes.ByteArray())
  val byteBuffer: Serde[Any, ByteBuffer] = Serde(KafkaSerdes.ByteBuffer())
  val uuid: Serde[Any, UUID]             = Serde(KafkaSerdes.UUID())
}
