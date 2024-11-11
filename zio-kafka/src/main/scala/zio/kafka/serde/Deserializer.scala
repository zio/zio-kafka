package zio.kafka.serde

import org.apache.kafka.common.header.Headers
import zio.{ IO, ZIO }

/**
 * Deserializer from byte array to a value of some type T
 *
 * @tparam R
 *   Environment available to the deserializer
 * @tparam T
 *   Value type
 */
trait Deserializer[+E, T] {
  def deserialize(topic: String, headers: Headers, data: Array[Byte]): IO[E, T]

  /**
   * Create a deserializer for a type U based on the deserializer for type T and a mapping function
   */
  def map[U](f: T => U): Deserializer[E, U] = Deserializer(deserialize(_, _, _).map(f))

  /**
   * Create a deserializer for a type U based on the deserializer for type T and an effectful mapping function
   */
  def mapM[E1 >: E, U](f: T => IO[E1, U]): Deserializer[E1, U] = Deserializer(deserialize(_, _, _).flatMap(f))

  /**
   * When this serializer fails, attempt to deserialize with the alternative
   *
   * If both deserializers fail, the error will be the last deserializer's exception.
   */
  def orElse[E1 >: E, U >: T](alternative: Deserializer[E1, U]): Deserializer[E1, U] =
    Deserializer { (topic, headers, data) =>
      deserialize(topic, headers, data) orElse alternative.deserialize(topic, headers, data)
    }

  /**
   * Returns a new deserializer that deserializes values as Option values, mapping null data to None values.
   */
  def asOption: Deserializer[E, Option[T]] =
    Deserializer((topic, headers, data) => ZIO.foreach(Option(data))(deserialize(topic, headers, _)))

  def mapError[E1](f: E => E1): Deserializer[E1, T] =
    Deserializer((topic, headers, data) => deserialize(topic, headers, data).mapError(f))

  def catchAll[E1 >: E](f: E => IO[E1, T]): Deserializer[E1, T] =
    Deserializer((topic, headers, data) => deserialize(topic, headers, data).catchAll(f))
}

object Deserializer extends Serdes {

  /**
   * Create a deserializer from a function
   */
  def apply[E, T](deser: (String, Headers, Array[Byte]) => IO[E, T]): Deserializer[E, T] =
    (topic: String, headers: Headers, data: Array[Byte]) => deser(topic, headers, data)
}
