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

  implicit val longSerde: Serde[Any, Long]   = fromKafkaSerde(KafkaSerdes.Long()).inmap(Long2long)(long2Long)
  implicit val intSerde: Serde[Any, Int]     = fromKafkaSerde(KafkaSerdes.Integer()).inmap(Integer2int)(int2Integer)
  implicit val shortSerde: Serde[Any, Short] = fromKafkaSerde(KafkaSerdes.Short()).inmap(Short2short)(short2Short)
  implicit val floatSerde: Serde[Any, Float] = fromKafkaSerde(KafkaSerdes.Float()).inmap(Float2float)(float2Float)
  implicit val doubleSerde: Serde[Any, Double] =
    fromKafkaSerde(KafkaSerdes.Double()).inmap(Double2double)(double2Double)
  implicit val stringSerde: Serde[Any, String]         = fromKafkaSerde(KafkaSerdes.String())
  implicit val byteArraySerde: Serde[Any, Array[Byte]] = fromKafkaSerde(KafkaSerdes.ByteArray())
  implicit val byteBufferSerde: Serde[Any, ByteBuffer] = fromKafkaSerde(KafkaSerdes.ByteBuffer())
  implicit val uuidSerde: Serde[Any, UUID]             = fromKafkaSerde(KafkaSerdes.UUID())

  implicit def deserializerWithError[R, T](implicit deser: Deserializer[R, T]): Deserializer[R, Try[T]] =
    deser.asTry
}

object Serdes extends Serdes
