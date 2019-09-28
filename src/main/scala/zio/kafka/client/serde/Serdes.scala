package zio.kafka.client.serde
import java.nio.ByteBuffer
import java.util.UUID
import org.apache.kafka.common.serialization.{ Serde => KafkaSerde, Serdes => KafkaSerdes }
import syntax._

import zio.Task

trait Serdes {
  private val dummyTopic = "noTopic"

  def fromKafkaSerde[T](serde: KafkaSerde[T]): Serde[T] = new Serde[T] {
    override def serialize(value: T): Task[Array[Byte]]  = Task(serde.serializer().serialize(dummyTopic, value))
    override def deserialize(data: Array[Byte]): Task[T] = Task(serde.deserializer().deserialize(dummyTopic, data))
  }

  implicit val longSerde: Serde[Long]             = fromKafkaSerde(KafkaSerdes.Long()).inmap(Long2long)(long2Long)
  implicit val intSerde: Serde[Int]               = fromKafkaSerde(KafkaSerdes.Integer()).inmap(Integer2int)(int2Integer)
  implicit val shortSerde: Serde[Short]           = fromKafkaSerde(KafkaSerdes.Short()).inmap(Short2short)(short2Short)
  implicit val floatSerde: Serde[Float]           = fromKafkaSerde(KafkaSerdes.Float()).inmap(Float2float)(float2Float)
  implicit val doubleSerde: Serde[Double]         = fromKafkaSerde(KafkaSerdes.Double()).inmap(Double2double)(double2Double)
  implicit val stringSerde: Serde[String]         = fromKafkaSerde(KafkaSerdes.String())
  implicit val byteArraySerde: Serde[Array[Byte]] = fromKafkaSerde(KafkaSerdes.ByteArray())
  implicit val byteBufferSerde: Serde[ByteBuffer] = fromKafkaSerde(KafkaSerdes.ByteBuffer())
  implicit val uuidSerde: Serde[UUID]             = fromKafkaSerde(KafkaSerdes.UUID())

  implicit def deserializerWithError[T](implicit deser: Deserializer[T]): Deserializer[Either[Throwable, T]] =
    deser.either
}

object Serdes extends Serdes
