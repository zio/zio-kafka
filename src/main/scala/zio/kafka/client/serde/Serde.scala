package zio.kafka.client.serde
import zio.RIO

trait Deserializer[-R, +T] {
  def deserialize(data: Array[Byte]): RIO[R, T]
}

object Deserializer {
  def of[R, T](implicit deser: Deserializer[R, T]): Deserializer[R, T] = deser
  def apply[R, T](deser: Array[Byte] => RIO[R, T]): Deserializer[R, T] = new Deserializer[R, T] {
    override def deserialize(data: Array[Byte]): RIO[R, T] = deser(data)
  }
}

trait Serializer[-R, -T] {
  def serialize(value: T): RIO[R, Array[Byte]]
}

object Serializer {
  def of[R, T](implicit ser: Serializer[R, T]): Serializer[R, T] = ser
  def apply[R, T](ser: T => RIO[R, Array[Byte]]): Serializer[R, T] = new Serializer[R, T] {
    override def serialize(value: T): RIO[R, Array[Byte]] = ser(value)
  }
}

trait Serde[-R, T] extends Deserializer[R, T] with Serializer[R, T]

object Serde {
  def of[T](implicit serde: Serde[Any, T]): Serde[Any, T] = serde

  def apply[R, T](deser: Deserializer[R, T])(ser: Serializer[R, T]): Serde[R, T] = new Serde[R, T] {
    override def serialize(value: T): RIO[R, Array[Byte]]  = ser.serialize(value)
    override def deserialize(data: Array[Byte]): RIO[R, T] = deser.deserialize(data)
  }
}
