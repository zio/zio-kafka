package zio.kafka.client.serde
import java.nio.ByteBuffer
import java.util.UUID

import org.apache.kafka.common.serialization.{ Serde => KafkaSerde, Serdes => KafkaSerdes }
import zio.Task

import scala.util.Try

trait Serdes {
  private val dummyTopic = "noTopic"

  def fromKafkaSerde[T](serde: KafkaSerde[T]): Serde[Any, T] = new Serde[Any, T] {
    override def serialize(value: T): Task[Array[Byte]]  = Task(serde.serializer().serialize(dummyTopic, value))
    override def deserialize(data: Array[Byte]): Task[T] = Task(serde.deserializer().deserialize(dummyTopic, data))
  }

  implicit val long: Serde[Any, Long]   = fromKafkaSerde(KafkaSerdes.Long()).inmap(Long2long)(long2Long)
  implicit val int: Serde[Any, Int]     = fromKafkaSerde(KafkaSerdes.Integer()).inmap(Integer2int)(int2Integer)
  implicit val short: Serde[Any, Short] = fromKafkaSerde(KafkaSerdes.Short()).inmap(Short2short)(short2Short)
  implicit val float: Serde[Any, Float] = fromKafkaSerde(KafkaSerdes.Float()).inmap(Float2float)(float2Float)
  implicit val double: Serde[Any, Double] =
    fromKafkaSerde(KafkaSerdes.Double()).inmap(Double2double)(double2Double)
  implicit val string: Serde[Any, String]         = fromKafkaSerde(KafkaSerdes.String())
  implicit val byteArray: Serde[Any, Array[Byte]] = fromKafkaSerde(KafkaSerdes.ByteArray())
  implicit val byteBuffer: Serde[Any, ByteBuffer] = fromKafkaSerde(KafkaSerdes.ByteBuffer())
  implicit val uuid: Serde[Any, UUID]             = fromKafkaSerde(KafkaSerdes.UUID())

  implicit def deserializerWithError[R, T](implicit deser: Deserializer[R, T]): Deserializer[R, Try[T]] =
    deser.asTry
}

object Serdes extends Serdes
