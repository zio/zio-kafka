package zio.kafka.serde

import org.apache.kafka.common.header.Headers
import zio.IO

/**
 * A serializer and deserializer for values of type T
 *
 * @tparam R
 *   Environment available to the deserializer
 * @tparam T
 *   Value type
 */
trait Serde[+E, T] extends Deserializer[E, T] with Serializer[T] {

  /**
   * Creates a new Serde that uses optional values. Null data will be mapped to None values.
   */
  override def asOption: Serde[E, Option[T]] =
    Serde(super[Deserializer].asOption)(super[Serializer].asOption)

  /**
   * Converts to a Serde of type U with pure transformations
   */
  def inmap[U](f: T => U)(g: U => T): Serde[E, U] =
    Serde(map(f))(contramap(g))

  /**
   * Convert to a Serde of type U with effectful transformations
   */
  def inmapM[E1 >: E, U](f: T => IO[E1, U])(g: U => T): Serde[E1, U] =
    Serde(mapM(f))(contramap(g))
}

object Serde extends Serdes {

  /**
   * Create a Serde from a deserializer and serializer function
   *
   * The (de)serializer functions can returned a failure ZIO with a Throwable to indicate (de)serialization failure
   */
  def apply[E, T](
    deser: (String, Headers, Array[Byte]) => IO[E, T]
  )(ser: (String, Headers, T) => Array[Byte]): Serde[E, T] =
    new Serde[E, T] {
      override final def serialize(topic: String, headers: Headers, value: T): Array[Byte] =
        ser(topic, headers, value)
      override final def deserialize(topic: String, headers: Headers, data: Array[Byte]): IO[E, T] =
        deser(topic, headers, data)
    }

  /**
   * Create a Serde from a deserializer and serializer function
   */
  def apply[E, T](deser: Deserializer[E, T])(ser: Serializer[T]): Serde[E, T] =
    new Serde[E, T] {
      override final def serialize(topic: String, headers: Headers, value: T): Array[Byte] =
        ser.serialize(topic, headers, value)
      override final def deserialize(topic: String, headers: Headers, data: Array[Byte]): IO[E, T] =
        deser.deserialize(topic, headers, data)
    }
}
