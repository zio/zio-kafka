package zio.kafka.client.serde

import zio.{ RIO, Task }
import org.apache.kafka.common.header.Headers
import org.apache.kafka.common.serialization.{ Serializer => KafkaSerializer }

/**
 * Serializer from values of some type T to a byte array
 *
 * @tparam R Environment available to the serializer
 * @tparam T
 */
trait Serializer[-R, -T] {
  def serialize(topic: String, headers: Headers, value: T): RIO[R, Array[Byte]]

  /**
   * Create a serializer for a type U based on the serializer for type T and a mapping function
   */
  def contramap[U](f: U => T): Serializer[R, U] =
    Serializer { (topic, headers, u) =>
      serialize(topic, headers, f(u))
    }

  /**
   * Create a serializer for a type U based on the serializer for type T and an effectful mapping function
   */
  def contramapM[R1 <: R, U](f: U => RIO[R1, T]): Serializer[R1, U] =
    Serializer { (topic, headers, u) =>
      f(u).flatMap(serialize(topic, headers, _))
    }
}

object Serializer extends Serdes {

  /**
   * Create a serializer from a function
   */
  def apply[R, T](ser: (String, Headers, T) => RIO[R, Array[Byte]]): Serializer[R, T] =
    new Serializer[R, T] {
      override def serialize(topic: String, headers: Headers, value: T): RIO[R, Array[Byte]] =
        ser(topic, headers, value)
    }

  /**
   * Create a Serializer from a Kafka Serializer
   */
  def apply[T](serializer: KafkaSerializer[T]): Serializer[Any, T] = new Serializer[Any, T] {
    override def serialize(topic: String, headers: Headers, value: T): Task[Array[Byte]] =
      Task(serializer.serialize(topic, headers, value))
  }
}
