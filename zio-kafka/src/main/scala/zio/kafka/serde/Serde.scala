package zio.kafka.serde

import org.apache.kafka.common.header.Headers
import org.apache.kafka.common.serialization.{ Serde => KafkaSerde }
import zio._

import scala.jdk.CollectionConverters._
import scala.util.Try

/**
 * A serializer and deserializer for values of type A.
 *
 * @tparam R
 *   Environment available to the serializer and the deserializer
 * @tparam A
 *   Value type
 *
 * @see
 *   [[https://zio.dev/zio-kafka/serialization-and-deserialization/ Serialization and Deserialization]]
 */
trait Serde[-R, A] extends Deserializer[R, A] with Serializer[R, A] {

  /**
   * Creates a new Serde that uses optional values. Null data will be mapped to None values.
   */
  override def asOption: Serde[R, Option[A]] =
    Serde(super[Deserializer].asOption)(super[Serializer].asOption)

  /**
   * Creates a new Serde that executes its serialization and deserialization functions on the blocking thread pool.
   */
  override def blocking: Serde[R, A] =
    Serde(super[Deserializer].blocking)(super[Serializer].blocking)

  /**
   * Converts to a Serde of type B with pure transformations.
   */
  def inmap[B](f: A => B)(g: B => A): Serde[R, B] =
    Serde(map(f))(contramap(g))

  /**
   * Convert to a Serde of type B with effectful transformations.
   */
  def inmapZIO[R1 <: R, B](f: A => RIO[R1, B])(g: B => RIO[R1, A]): Serde[R1, B] =
    Serde(mapZIO(f))(contramapZIO(g))
}

/**
 * Creates serdes, and has the predefined serdes for primitive types, strings, byte arrays and `UUID`s (such as
 * `Serde.string` and `Serde.long`).
 *
 * {{{
 * import zio.kafka.serde.Serde
 *
 * final case class OrderId(value: Long)
 *
 * val orderIdSerde: Serde[Any, OrderId] = Serde.long.inmap(OrderId(_))(_.value)
 * }}}
 */
object Serde extends Serdes {

  /**
   * Create a Serde from a deserializer and serializer function.
   *
   * The (de)serializer functions can return a failure ZIO with a Throwable to indicate (de)serialization failure.
   */
  def apply[R, A](
    deser: (String, Headers, Array[Byte]) => RIO[R, A]
  )(ser: (String, Headers, A) => RIO[R, Array[Byte]]): Serde[R, A] =
    new Serde[R, A] {
      override final def serialize(topic: String, headers: Headers, value: A): RIO[R, Array[Byte]] =
        ser(topic, headers, value)
      override final def deserialize(topic: String, headers: Headers, data: Array[Byte]): RIO[R, A] =
        deser(topic, headers, data)
    }

  /**
   * Create a Serde from a deserializer and a serializer.
   */
  def apply[R, A](deser: Deserializer[R, A])(ser: Serializer[R, A]): Serde[R, A] =
    new Serde[R, A] {
      override final def serialize(topic: String, headers: Headers, value: A): RIO[R, Array[Byte]] =
        ser.serialize(topic, headers, value)
      override final def deserialize(topic: String, headers: Headers, data: Array[Byte]): RIO[R, A] =
        deser.deserialize(topic, headers, data)
    }

  /**
   * Create a Serde from a Kafka Serde.
   *
   * Note: the wrapped `serde` is never closed.
   */
  def fromKafkaSerde[A](serde: KafkaSerde[A], props: Map[String, AnyRef], isKey: Boolean): Task[Serde[Any, A]] =
    ZIO
      .attempt(serde.configure(props.asJava, isKey))
      .as(
        new Serde[Any, A] {
          private final val serializer   = serde.serializer()
          private final val deserializer = serde.deserializer()

          override final def deserialize(topic: String, headers: Headers, data: Array[Byte]): Task[A] =
            ZIO.attempt(deserializer.deserialize(topic, headers, data))

          override final def serialize(topic: String, headers: Headers, value: A): Task[Array[Byte]] =
            ZIO.attempt(serializer.serialize(topic, headers, value))
        }
      )

  /**
   * An implicit deserializer that returns a `Try`, derived from the implicit deserializer for `A`: a deserialization
   * failure is returned as a `Failure`. See [[Deserializer.asTry]].
   */
  implicit def deserializerWithError[R, A](implicit deser: Deserializer[R, A]): Deserializer[R, Try[A]] =
    deser.asTry
}
