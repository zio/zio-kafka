package zio.kafka.client.serde

import java.nio.ByteBuffer
import java.util.UUID

import org.apache.kafka.common.serialization.{ Serde => KafkaSerde, Serdes => KafkaSerdes }
import zio.{ RIO, Task }

import scala.util.Try

/**
 * A serializer and deserializer for values of type T
 *
 * @tparam R Environment available to the deserializer
 * @tparam T Value type
 */
trait Serde[-R, T] extends Deserializer[R, T] with Serializer[R, T] {

  /**
   * Converts to a Serde of type U with pure transformations
   */
  def inmap[U](f: T => U)(g: U => T): Serde[R, U] =
    Serde(map(f))(contramap(g))

  /**
   * Convert to a Serde of type U with effectful transformations
   */
  def inmapM[R1 <: R, U](f: T => RIO[R1, U])(g: U => RIO[R1, T]): Serde[R1, U] =
    Serde(mapM(f))(contramapM(g))
}

object Serde {
  def of[T](implicit serde: Serde[Any, T]): Serde[Any, T] = serde

  /**
   * Create a Serde from a deserializer and serializer function
   *
   * The (de)serializer functions can returned a failure ZIO with a Throwable to indicate (de)serialization failure
   */
  def apply[R, T](deser: (String, Array[Byte]) => RIO[R, T])(ser: (String, T) => RIO[R, Array[Byte]]): Serde[R, T] =
    new Serde[R, T] {
      override def serialize(topic: String, value: T): RIO[R, Array[Byte]]  = ser(topic, value)
      override def deserialize(topic: String, data: Array[Byte]): RIO[R, T] = deser(topic, data)
    }

  /**
   * Create a Serde from a deserializer and serializer function
   */
  def apply[R, T](deser: Deserializer[R, T])(ser: Serializer[R, T]): Serde[R, T] = new Serde[R, T] {
    override def serialize(topic: String, value: T): RIO[R, Array[Byte]]  = ser.serialize(topic, value)
    override def deserialize(topic: String, data: Array[Byte]): RIO[R, T] = deser.deserialize(topic, data)
  }

  /**
   * Create a Serde from a Kafka Serde
   */
  def apply[T](serde: KafkaSerde[T]): Serde[Any, T] = new Serde[Any, T] {
    override def serialize(topic: String, value: T): Task[Array[Byte]] =
      Task(serde.serializer().serialize(topic, value))
    override def deserialize(topic: String, data: Array[Byte]): Task[T] =
      Task(serde.deserializer().deserialize(topic, data))
  }

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
