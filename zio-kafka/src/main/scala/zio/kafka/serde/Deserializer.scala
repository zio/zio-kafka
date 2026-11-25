package zio.kafka.serde

import org.apache.kafka.common.header.Headers
import org.apache.kafka.common.serialization.{ Deserializer => KafkaDeserializer }
import zio._

import scala.jdk.CollectionConverters._
import scala.util.{ Failure, Success, Try }

/**
 * Deserializer from byte array to a value of some type A.
 *
 * @tparam R
 *   Environment available to the deserializer
 * @tparam A
 *   Value type
 *
 * @see
 *   [[https://zio.dev/zio-kafka/serialization-and-deserialization/ Serialization and Deserialization]]
 */
trait Deserializer[-R, +A] {

  /**
   * Deserializes the data of a record's key or value. The topic and headers of the record are passed along. `data` is
   * `null` when the key or value of the record is `null`.
   */
  def deserialize(topic: String, headers: Headers, data: Array[Byte]): RIO[R, A]

  /**
   * Returns a new deserializer that executes its deserialization function on the blocking thread pool.
   */
  def blocking: Deserializer[R, A] =
    Deserializer((topic, headers, data) => ZIO.blocking(deserialize(topic, headers, data)))

  /**
   * Create a deserializer for a type B based on the deserializer for type A and a mapping function.
   */
  def map[B](f: A => B): Deserializer[R, B] = Deserializer(deserialize(_, _, _).map(f))

  /**
   * Create a deserializer for a type B based on the deserializer for type A and an effectful mapping function.
   */
  def mapZIO[R1 <: R, B](f: A => RIO[R1, B]): Deserializer[R1, B] = Deserializer(deserialize(_, _, _).flatMap(f))

  /**
   * When this deserializer fails, attempt to deserialize with the alternative.
   *
   * If both deserializers fail, the error will be the last deserializer's exception.
   */
  def orElse[R1 <: R, A1 >: A](alternative: Deserializer[R1, A1]): Deserializer[R1, A1] =
    Deserializer { (topic, headers, data) =>
      deserialize(topic, headers, data) orElse alternative.deserialize(topic, headers, data)
    }

  /**
   * Returns a new deserializer that returns a `Try`: a `Success` with the deserialized value, or a `Failure` with the
   * exception when deserialization fails.
   *
   * This is useful for explicitly handling deserialization failures.
   */
  def asTry: Deserializer[R, Try[A]] =
    Deserializer(deserialize(_, _, _).fold(e => Failure(e), v => Success(v)))

  /**
   * Returns a new deserializer that deserializes values as Option values, mapping null data to None values.
   */
  def asOption: Deserializer[R, Option[A]] =
    Deserializer((topic, headers, data) => ZIO.foreach(Option(data))(deserialize(topic, headers, _)))
}

/**
 * Creates deserializers. It also has the predefined serdes of `Serdes`, such as `Deserializer.string` and
 * `Deserializer.long`.
 */
object Deserializer extends Serdes {

  /**
   * Create a deserializer from a function.
   */
  def apply[R, A](deser: (String, Headers, Array[Byte]) => RIO[R, A]): Deserializer[R, A] =
    (topic: String, headers: Headers, data: Array[Byte]) => deser(topic, headers, data)

  /**
   * Create a Deserializer from a Kafka Deserializer.
   *
   * Note: the wrapped `deserializer` is never closed.
   */
  def fromKafkaDeserializer[A](
    deserializer: KafkaDeserializer[A],
    props: Map[String, AnyRef],
    isKey: Boolean
  ): Task[Deserializer[Any, A]] =
    ZIO
      .attempt(deserializer.configure(props.asJava, isKey))
      .as(
        new Deserializer[Any, A] {
          override def deserialize(topic: String, headers: Headers, data: Array[Byte]): Task[A] =
            ZIO.attempt(deserializer.deserialize(topic, headers, data))
        }
      )
}
