package zio.kafka.client.serde

import zio.RIO

import scala.util.{ Failure, Success, Try }

trait SerdeSyntax {
  implicit final def deserializerSyntax[R, T](deserializer: Deserializer[R, T]): DeserializerOps[R, T] =
    new DeserializerOps[R, T](deserializer)

  implicit final def serializerSyntax[R, T](serializer: Serializer[R, T]): SerializerOps[R, T] =
    new SerializerOps[R, T](serializer)

  implicit final def serdeSyntax[R, T](serde: Serde[R, T]): SerdeOps[R, T] =
    new SerdeOps[R, T](serde)
}

final class DeserializerOps[R, T](private val deserializer: Deserializer[R, T]) extends AnyVal {

  /**
   * Create a deserializer for a type U based on the deserializer for type T and a mapping function
   */
  def map[U](f: T => U): Deserializer[R, U] = Deserializer(deserializer.deserialize(_).map(f))

  /**
   * Create a deserializer for a type U based on the deserializer for type T and an effectful mapping function
   */
  def mapM[R1 <: R, U](f: T => RIO[R1, U]): Deserializer[R1, U] = Deserializer(deserializer.deserialize(_).flatMap(f))

  /**
   * Serde that handles deserialization failures by returning a Task
   *
   * This is useful for explicitly handling deserialization failures.
   */
  def asTry: Deserializer[R, Try[T]] =
    Deserializer(deserializer.deserialize(_).fold(e => Failure(e), v => Success(v)))
}

final class SerializerOps[R, T](private val serializer: Serializer[R, T]) extends AnyVal {

  /**
   * Create a serializer for a type U based on the serializer for type T and a mapping function
   */
  def contramap[U](f: U => T): Serializer[R, U] = Serializer(f andThen serializer.serialize)

  /**
   * Create a serializer for a type U based on the serializer for type T and an effectful mapping function
   */
  def contramapM[R1 <: R, U](f: U => RIO[R1, T]): Serializer[R1, U] = Serializer(f(_).flatMap(serializer.serialize))
}

final class SerdeOps[R, T](private val serde: Serde[R, T]) extends AnyVal {

  /**
   * Converts to a Serde of type U with pure transformations
   */
  def inmap[U](f: T => U)(g: U => T): Serde[R, U] =
    Serde(serde.map(f))(serde.contramap(g))

  /**
   * Convert to a Serde of type U with effectful transformations
   */
  def inmapM[R1 <: R, U](f: T => RIO[R1, U])(g: U => RIO[R1, T]): Serde[R1, U] =
    Serde(serde.mapM(f))(serde.contramapM(g))
}
