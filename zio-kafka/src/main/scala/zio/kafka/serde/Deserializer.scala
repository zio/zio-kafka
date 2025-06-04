package zio.kafka.serde

import org.apache.kafka.common.header.Headers
import org.apache.kafka.common.serialization.{ Deserializer => KafkaDeserializer }
import zio._
//import zio.stacktracer.TracingImplicits.disableAutoTrace

import scala.jdk.CollectionConverters._
import scala.util.{ Failure, Success, Try }

/**
 * Deserializer from byte array to a value of some type A
 *
 * @tparam R
 *   Environment available to the deserializer
 * @tparam A
 *   Value type
 */
trait Deserializer[-R, +A] {
  def deserialize(topic: String, headers: Headers, data: Array[Byte])(implicit trace: Trace): RIO[R, A]

  /**
   * Returns a new deserializer that executes its deserialization function on the blocking threadpool.
   */
  def blocking: Deserializer[R, A] =
    new Deserializer[R, A] {
      override def deserialize(topic: String, headers: Headers, data: Array[Byte])(implicit trace: Trace): RIO[R, A] =
        ZIO.blocking(deserialize(topic, headers, data))
    }

  /**
   * Create a deserializer for a type U based on the deserializer for type T and a mapping function
   */
  def map[U](f: A => U)(implicit trace: Trace): Deserializer[R, U] = Deserializer(deserialize(_, _, _).map(f))

  /**
   * Create a deserializer for a type U based on the deserializer for type T and an effectful mapping function
   */
  def mapZIO[R1 <: R, U](f: A => RIO[R1, U])(implicit trace: Trace): Deserializer[R1, U] = Deserializer(
    deserialize(_, _, _).flatMap(f)
  )

  /**
   * When this serializer fails, attempt to deserialize with the alternative
   *
   * If both deserializers fail, the error will be the last deserializer's exception.
   */
  def orElse[R1 <: R, U >: A](alternative: Deserializer[R1, U])(implicit trace: Trace): Deserializer[R1, U] =
    Deserializer { (topic, headers, data) =>
      deserialize(topic, headers, data) orElse alternative.deserialize(topic, headers, data)
    }

  /**
   * Serde that handles deserialization failures by returning a Task
   *
   * This is useful for explicitly handling deserialization failures.
   */
  def asTry(implicit trace: Trace): Deserializer[R, Try[A]] =
    Deserializer(deserialize(_, _, _).fold(e => Failure(e), v => Success(v)))

  /**
   * Returns a new deserializer that deserializes values as Option values, mapping null data to None values.
   */
  def asOption(implicit trace: Trace): Deserializer[R, Option[A]] =
    Deserializer((topic, headers, data) => ZIO.foreach(Option(data))(deserialize(topic, headers, _)))
}

object Deserializer extends Serdes {

  /**
   * Create a deserializer from a function
   */
  def apply[R, A](deser: (String, Headers, Array[Byte]) => RIO[R, A]): Deserializer[R, A] =
    new Deserializer[R, A] {
      def deserialize(topic: String, headers: Headers, data: Array[Byte])(implicit trace: Trace): RIO[R, A] =
        deser(topic, headers, data)
    }

  /**
   * Create a Deserializer from a Kafka Deserializer
   */
  def fromKafkaDeserializer[A](
    deserializer: KafkaDeserializer[A],
    props: Map[String, AnyRef],
    isKey: Boolean
  )(implicit trace: Trace): Task[Deserializer[Any, A]] =
    ZIO
      .attempt(deserializer.configure(props.asJava, isKey))
      .as(
        new Deserializer[Any, A] {
          override def deserialize(topic: String, headers: Headers, data: Array[Byte])(implicit trace: Trace): Task[A] =
            ZIO.attempt(deserializer.deserialize(topic, headers, data))
        }
      )
}
