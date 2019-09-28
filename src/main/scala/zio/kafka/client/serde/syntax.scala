package zio.kafka.client.serde
import zio.Task

object syntax {
  implicit class DeseralizerOps[T](val deserializer: Deserializer[T]) extends AnyVal {
    def map[U](f: T => U): Deserializer[U] = Deserializer(deserializer.deserialize(_).map(f))

    def mapM[U](f: T => Task[U]): Deserializer[U] = Deserializer(deserializer.deserialize(_).flatMap(f))

    /**
     * Serde that handles deserialization failures by returning a Left of a throwable
     *
     * This is useful for explicitly handling deserialization failures.
     */
    def either: Deserializer[Either[Throwable, T]] = Deserializer(deserializer.deserialize(_).either)
  }

  implicit class SerializerOps[T](val serializer: Serializer[T]) extends AnyVal {
    def contramap[U](f: U => T): Serializer[U] = Serializer(value => serializer.serialize(f(value)))

    def contramapM[U](f: U => Task[T]): Serializer[U] = Serializer(f(_) >>= serializer.serialize)
  }

  implicit class SerdeOps[T](val serde: Serde[T]) extends AnyVal {

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
}
