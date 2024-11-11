package zio.kafka.serde

import org.apache.kafka.common.header.Headers

/**
 * Serializer from values of some type T to a byte array
 *
 * Serialization may not fail and any value can be serialized. If effectful operations are needed for serialization,
 * this must be done before passing a serialized value to the Producer. Use [[Serdes.byteArray]]
 *
 * @tparam T
 */
trait Serializer[T] {
  def serialize(topic: String, headers: Headers, value: T): Array[Byte]

  /**
   * Create a serializer for a type U based on the serializer for type T and a mapping function
   */
  def contramap[U](f: U => T): Serializer[U] =
    Serializer((topic, headers, u) => serialize(topic, headers, f(u)))

  /**
   * Returns a new serializer that handles optional values and serializes them as nulls.
   */
  def asOption[U <: T]: Serializer[Option[U]] =
    Serializer { (topic, headers, valueOpt) =>
      valueOpt match {
        case None        => null
        case Some(value) => serialize(topic, headers, value)
      }
    }
}

object Serializer extends Serdes {

  /**
   * Create a serializer from a function
   */
  def apply[T](ser: (String, Headers, T) => Array[Byte]): Serializer[T] =
    (topic: String, headers: Headers, value: T) => ser(topic, headers, value)
}
