package zio.kafka.client.serde
import zio.Task

trait Deserializer[+T] {
  def deserialize(data: Array[Byte]): Task[T]
}

object Deserializer {
  def of[T: Deserializer]: Deserializer[T] = implicitly
  def apply[T](deser: Array[Byte] => Task[T]): Deserializer[T] = new Deserializer[T] {
    override def deserialize(data: Array[Byte]): Task[T] = deser(data)
  }
}

trait Serializer[-T] {
  def serialize(value: T): Task[Array[Byte]]
}

object Serializer {
  def of[T: Serializer]: Serializer[T] = implicitly
  def apply[T](ser: T => Task[Array[Byte]]): Serializer[T] = new Serializer[T] {
    override def serialize(value: T): Task[Array[Byte]] = ser(value)
  }
}

trait Serde[T] extends Deserializer[T] with Serializer[T]

object Serde {
  def of[T: Serde]: Serde[T] = implicitly

  def apply[T](deser: Array[Byte] => Task[T])(ser: T => Task[Array[Byte]]): Serde[T] = new Serde[T] {
    override def serialize(value: T): Task[Array[Byte]]  = ser(value)
    override def deserialize(data: Array[Byte]): Task[T] = deser(data)
  }

  def apply[T](deser: Deserializer[T])(ser: Serializer[T]): Serde[T] = new Serde[T] {
    override def serialize(value: T): Task[Array[Byte]]  = ser.serialize(value)
    override def deserialize(data: Array[Byte]): Task[T] = deser.deserialize(data)
  }
}
