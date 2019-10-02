package zio.kafka.client.serde
import org.apache.kafka.common.serialization.{ Serde => KafkaSerde }
import zio.{ RIO, Task }

/**
 * Deserializer from byte array to a value of some type T
 *
 * @tparam R Environment available to the deserializer
 * @tparam T Value type
 */
trait Deserializer[-R, +T] {
  def deserialize(data: Array[Byte]): RIO[R, T]
}

object Deserializer {

  /**
   * Obtain an instance of a Deserializer of some type from the implicit scope
   */
  def of[T](implicit deser: Deserializer[Any, T]): Deserializer[Any, T] = deser

  /**
   * Create a deserializer from a function
   */
  def apply[R, T](deser: Array[Byte] => RIO[R, T]): Deserializer[R, T] = new Deserializer[R, T] {
    override def deserialize(data: Array[Byte]): RIO[R, T] = deser(data)
  }
}

/**
 * Serializer from values of some type T to a byte array
 *
 * @tparam R Environment available to the serializer
 * @tparam T
 */
trait Serializer[-R, -T] {
  def serialize(value: T): RIO[R, Array[Byte]]
}

object Serializer {

  /**
   * Obtain an instance of a Serializer of some type from the implicit scope
   */
  def of[T](implicit ser: Serializer[Any, T]): Serializer[Any, T] = ser

  /**
   * Create a serializer from a function
   */
  def apply[R, T](ser: T => RIO[R, Array[Byte]]): Serializer[R, T] = new Serializer[R, T] {
    override def serialize(value: T): RIO[R, Array[Byte]] = ser(value)
  }
}

/**
 * A serializer and deserializer for values of type T
 *
 * @tparam R Environment available to the deserializer
 * @tparam T Value type
 */
trait Serde[-R, T] extends Deserializer[R, T] with Serializer[R, T]

object Serde {
  def of[T](implicit serde: Serde[Any, T]): Serde[Any, T] = serde

  /**
   * Create a Serde from a deserializer and serializer function
   *
   * The (de)serializer functions can returned a failure ZIO with a Throwable to indicate (de)serialization failure
   */
  def apply[R, T](deser: Array[Byte] => RIO[R, T])(ser: T => RIO[R, Array[Byte]]): Serde[R, T] =
    new Serde[R, T] {
      override def serialize(value: T): RIO[R, Array[Byte]]  = ser(value)
      override def deserialize(data: Array[Byte]): RIO[R, T] = deser(data)
    }

  /**
   * Create a Serde from a deserializer and serializer function
   */
  def apply[R, T](deser: Deserializer[R, T])(ser: Serializer[R, T]): Serde[R, T] = new Serde[R, T] {
    override def serialize(value: T): RIO[R, Array[Byte]]  = ser.serialize(value)
    override def deserialize(data: Array[Byte]): RIO[R, T] = deser.deserialize(data)
  }

  /**
   * Create a Serde from a Kafka Serde
   */
  def apply[T](serde: KafkaSerde[T]): Serde[Any, T] = new Serde[Any, T] {
    private val dummyTopic = "noTopic"

    override def serialize(value: T): Task[Array[Byte]]  = Task(serde.serializer().serialize(dummyTopic, value))
    override def deserialize(data: Array[Byte]): Task[T] = Task(serde.deserializer().deserialize(dummyTopic, data))
  }
}
