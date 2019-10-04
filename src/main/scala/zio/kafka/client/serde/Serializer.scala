package zio.kafka.client.serde

import zio.RIO

/**
 * Serializer from values of some type T to a byte array
 *
 * @tparam R Environment available to the serializer
 * @tparam T
 */
trait Serializer[-R, -T] {
  def serialize(topic: String, value: T): RIO[R, Array[Byte]]

  /**
   * Create a serializer for a type U based on the serializer for type T and a mapping function
   */
  def contramap[U](f: U => T): Serializer[R, U] =
    Serializer { (topic, u) =>
      serialize(topic, f(u))
    }

  /**
   * Create a serializer for a type U based on the serializer for type T and an effectful mapping function
   */
  def contramapM[R1 <: R, U](f: U => RIO[R1, T]): Serializer[R1, U] =
    Serializer { (topic, u) =>
      f(u).flatMap(serialize(topic, _))
    }
}

object Serializer extends Serdes {
  /**
   * Create a serializer from a function
   */
  def apply[R, T](ser: (String, T) => RIO[R, Array[Byte]]): Serializer[R, T] =
    new Serializer[R, T] {
      override def serialize(topic: String, value: T): RIO[R, Array[Byte]] = ser(topic, value)
    }
}
