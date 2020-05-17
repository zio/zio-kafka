package zio.kafka.serde

import java.nio.ByteBuffer
import java.util.UUID

import org.apache.kafka.common.serialization.{ Serdes => KafkaSerdes }

private[zio] trait Serdes {
  lazy val long: Serde[Any, Long]             = Serde(KafkaSerdes.Long()).inmap(Long2long)(long2Long)
  lazy val int: Serde[Any, Int]               = Serde(KafkaSerdes.Integer()).inmap(Integer2int)(int2Integer)
  lazy val short: Serde[Any, Short]           = Serde(KafkaSerdes.Short()).inmap(Short2short)(short2Short)
  lazy val float: Serde[Any, Float]           = Serde(KafkaSerdes.Float()).inmap(Float2float)(float2Float)
  lazy val double: Serde[Any, Double]         = Serde(KafkaSerdes.Double()).inmap(Double2double)(double2Double)
  lazy val string: Serde[Any, String]         = Serde(KafkaSerdes.String())
  lazy val byteArray: Serde[Any, Array[Byte]] = Serde(KafkaSerdes.ByteArray())
  lazy val byteBuffer: Serde[Any, ByteBuffer] = Serde(KafkaSerdes.ByteBuffer())
  lazy val uuid: Serde[Any, UUID]             = Serde(KafkaSerdes.UUID())
}
