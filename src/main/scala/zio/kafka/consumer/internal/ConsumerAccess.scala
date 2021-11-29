package zio.kafka.consumer.internal

import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.errors.WakeupException
import org.apache.kafka.common.serialization.ByteArrayDeserializer
import zio._
import zio.kafka.consumer.ConsumerSettings
import zio.kafka.consumer.internal.ConsumerAccess.ByteArrayKafkaConsumer

import scala.jdk.CollectionConverters._

private[consumer] class ConsumerAccess(
  private[consumer] val consumer: ByteArrayKafkaConsumer,
  access: Semaphore
) {
  def withConsumer[A](f: ByteArrayKafkaConsumer => A): Task[A] =
    withConsumerM[Any, A](c => ZIO(f(c)))

  def withConsumerM[R, A](f: ByteArrayKafkaConsumer => RIO[R, A]): RIO[R, A] =
    access.withPermit(withConsumerNoPermit(f))

  private[consumer] def withConsumerNoPermit[R, A](
    f: ByteArrayKafkaConsumer => RIO[R, A]
  ): RIO[R, A] =
    ZIO
      .blocking(ZIO.suspend(f(consumer)))
      .catchSome { case _: WakeupException =>
        ZIO.interrupt
      }
      .fork
      .flatMap(fib => fib.join.onInterrupt(ZIO.succeed(consumer.wakeup()) *> fib.interrupt))
}

private[consumer] object ConsumerAccess {
  type ByteArrayKafkaConsumer = KafkaConsumer[Array[Byte], Array[Byte]]

  def make(settings: ConsumerSettings): TaskManaged[ConsumerAccess] =
    for {
      access <- Semaphore.make(1).toManaged
      consumer <- ZIO.attemptBlocking {
                    new KafkaConsumer[Array[Byte], Array[Byte]](
                      settings.driverSettings.asJava,
                      new ByteArrayDeserializer(),
                      new ByteArrayDeserializer()
                    )
                  }.toManagedWith(c => ZIO.blocking(access.withPermit(UIO(c.close(settings.closeTimeout)))))
    } yield new ConsumerAccess(consumer, access)
}
