package zio.kafka.client.serde

import zio.Task

import scala.util.{ Failure, Success, Try }

trait SerdeSyntax {
  implicit final def deserializerSyntax[T](deserializer: Deserializer[T]): DeserializerOps[T] =
    new DeserializerOps[T](deserializer)

  implicit final def serializerSyntax[T](serializer: Serializer[T]): SerializerOps[T] =
    new SerializerOps[T](serializer)

  implicit final def serdeSyntax[T](serde: Serde[T]): SerdeOps[T] =
    new SerdeOps[T](serde)
}

final class DeserializerOps[T](private val deserializer: Deserializer[T]) extends AnyVal {
  def map[U](f: T => U): Deserializer[U] = Deserializer(deserializer.deserialize(_).map(f))

  def mapM[U](f: T => Task[U]): Deserializer[U] = Deserializer(deserializer.deserialize(_).flatMap(f))

  /**
   * Serde that handles deserialization failures by returning a Task
   *
   * This is useful for explicitly handling deserialization failures.
   */
  def asTry: Deserializer[Try[T]] =
    Deserializer(deserializer.deserialize(_).fold(e => Failure(e), v => Success(v)))
}

final class SerializerOps[T](private val serializer: Serializer[T]) extends AnyVal {
  def contramap[U](f: U => T): Serializer[U] = Serializer(f andThen serializer.serialize)

  def contramapM[U](f: U => Task[T]): Serializer[U] = Serializer(f(_).flatMap(serializer.serialize))
}

final class SerdeOps[T](private val serde: Serde[T]) extends AnyVal {

  /**
   * Converts to a Serde of type U with pure transformations
   */
  def inmap[U](f: T => U)(g: U => T): Serde[U] =
    Serde(serde.map(f))(serde.contramap(g))

  /**
   * Convert to a Serde of type U with an effectful transformation
   */
  def inmapM[U](f: T => Task[U])(g: U => Task[T]): Serde[U] =
    Serde(serde.mapM(f))(serde.contramapM(g))
}
