package zio.kafka.serde

import org.apache.kafka.common.header.Headers
import org.apache.kafka.common.serialization.{ Deserializer => KafkaDeserializer }
import zio.{ RIO, Task, ZIO }

import scala.annotation.nowarn
import scala.util.{ Failure, Success, Try }

/**
 * Deserializer from byte array to a value of some type T
 *
 * @tparam R Environment available to the deserializer
 * @tparam T Value type
 */
trait Deserializer[-R, +T] {
  def deserialize(topic: String, headers: Headers, data: Array[Byte]): RIO[R, T]

  /**
   * Create a deserializer for a type U based on the deserializer for type T and a mapping function
   */
  def map[U](f: T => U): Deserializer[R, U] = Deserializer(deserialize(_, _, _).map(f))

  /**
   * Create a deserializer for a type U based on the deserializer for type T and an effectful mapping function
   */
  def mapM[R1 <: R, U](f: T => RIO[R1, U]): Deserializer[R1, U] = Deserializer(deserialize(_, _, _).flatMap(f))

  /**
   * When this serializer fails, attempt to deserialize with the alternative
   *
   * If both deserializers fail, the error will be the last deserializer's exception.
   */
  def orElse[R1 <: R, U >: T](alternative: Deserializer[R1, U]): Deserializer[R1, U] =
    Deserializer { (topic, headers, data) =>
      deserialize(topic, headers, data) orElse alternative.deserialize(topic, headers, data)
    }

  /**
   * Serde that handles deserialization failures by returning a Task
   *
   * This is useful for explicitly handling deserialization failures.
   */
  def asTry: Deserializer[R, Try[T]] =
    Deserializer(deserialize(_, _, _).fold(e => Failure(e), v => Success(v)))

  def asOption(implicit @nowarn ev: T <:< AnyRef): Deserializer[R, Option[T]] =
    Deserializer((topic, headers, data) => ZIO.foreach(Option(data))(deserialize(topic, headers, _)))
}

object Deserializer extends Serdes {

  /**
   * Create a deserializer from a function
   */
  def apply[R, T](deser: (String, Headers, Array[Byte]) => RIO[R, T]): Deserializer[R, T] = new Deserializer[R, T] {
    override def deserialize(topic: String, headers: Headers, data: Array[Byte]): RIO[R, T] =
      deser(topic, headers, data)
  }

  /**
   * Create a Deserializer from a Kafka Deserializer
   */
  def apply[T](deserializer: KafkaDeserializer[T]): Deserializer[Any, T] = new Deserializer[Any, T] {
    override def deserialize(topic: String, headers: Headers, data: Array[Byte]): Task[T] =
      Task(deserializer.deserialize(topic, headers, data))
  }
}
