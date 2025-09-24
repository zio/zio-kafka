package zio.kafka.serde

import org.apache.kafka.common.header.Headers
import org.apache.kafka.common.serialization.{ Serializer => KafkaSerializer }
import zio._

import scala.jdk.CollectionConverters._

/**
 * Serializer from values of some type A to a byte array.
 *
 * @tparam R
 *   Environment available to the serializer
 * @tparam A
 */
trait Serializer[-R, -A] {
  def serialize(topic: String, headers: Headers, value: A): RIO[R, Array[Byte]]

  /**
   * Create a serializer for a type B based on the serializer for type A and a mapping function.
   */
  def contramap[B](f: B => A): Serializer[R, B] =
    Serializer((topic, headers, u) => serialize(topic, headers, f(u)))

  /**
   * Create a serializer for a type B based on the serializer for type A and an effectful mapping function.
   */
  def contramapZIO[R1 <: R, B](f: B => RIO[R1, A]): Serializer[R1, B] =
    Serializer((topic, headers, u) => f(u).flatMap(serialize(topic, headers, _)))

  /**
   * Returns a new serializer that executes its serialization function on the blocking thread pool.
   */
  def blocking: Serializer[R, A] =
    Serializer((topic, headers, t) => ZIO.blocking(serialize(topic, headers, t)))

  /**
   * Returns a new serializer that handles optional values and serializes them as nulls.
   */
  def asOption[A1 <: A]: Serializer[R, Option[A1]] =
    Serializer { (topic, headers, valueOpt) =>
      valueOpt match {
        case None        => Exit.succeed(null)
        case Some(value) => serialize(topic, headers, value)
      }
    }
}

object Serializer extends Serdes {

  /**
   * Create a serializer from a function.
   */
  def apply[R, A](ser: (String, Headers, A) => RIO[R, Array[Byte]]): Serializer[R, A] =
    (topic: String, headers: Headers, value: A) => ser(topic, headers, value)

  /**
   * Create a Serializer from a Kafka Serializer.
   */
  def fromKafkaSerializer[A](
    serializer: KafkaSerializer[A],
    props: Map[String, AnyRef],
    isKey: Boolean
  ): Task[Serializer[Any, A]] =
    ZIO
      .attempt(serializer.configure(props.asJava, isKey))
      .as(
        new Serializer[Any, A] {
          override def serialize(topic: String, headers: Headers, value: A): Task[Array[Byte]] =
            ZIO.attempt(serializer.serialize(topic, headers, value))
        }
      )

}
