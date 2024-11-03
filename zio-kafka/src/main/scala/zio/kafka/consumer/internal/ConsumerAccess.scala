package zio.kafka.consumer.internal

import org.apache.kafka.clients.consumer.{ Consumer => JConsumer, KafkaConsumer }
import org.apache.kafka.common.errors.WakeupException
import org.apache.kafka.common.serialization.ByteArrayDeserializer
import zio._
import zio.kafka.consumer.ConsumerSettings
import zio.kafka.consumer.internal.ConsumerAccess.ByteArrayKafkaConsumer

import scala.jdk.CollectionConverters._

private[consumer] final class ConsumerAccess(
  private[consumer] val consumer: ByteArrayKafkaConsumer,
  access: Semaphore
) {

  def withConsumer[A](f: ByteArrayKafkaConsumer => A): Task[A] =
    withConsumerZIO[Any, A](c => ZIO.attempt(f(c)))

  def withConsumerZIO[R, A](f: ByteArrayKafkaConsumer => RIO[R, A]): RIO[R, A] =
    access.withPermit(withConsumerNoPermit(f))

  private def withConsumerNoPermit[R, A](
    f: ByteArrayKafkaConsumer => RIO[R, A]
  ): RIO[R, A] =
    ZIO
      .blocking(ZIO.suspend(f(consumer)))
      .catchSome { case _: WakeupException =>
        ZIO.interrupt
      }
      .fork
      .flatMap(fib => fib.join.onInterrupt(ZIO.succeed(consumer.wakeup()) *> fib.interrupt))

  /**
   * Use this method only from Runloop.
   */
  private[internal] def runloopAccess[R, E, A](f: ByteArrayKafkaConsumer => ZIO[R, E, A]): ZIO[R, E, A] =
    access.withPermit(f(consumer))

  /**
   * Use this method ONLY from the rebalance listener.
   */
  private[internal] def rebalanceListenerAccess[R, A](f: ByteArrayKafkaConsumer => RIO[R, A]): RIO[R, A] =
    withConsumerNoPermit(f)

}

private[consumer] object ConsumerAccess {
  type ByteArrayKafkaConsumer = JConsumer[Array[Byte], Array[Byte]]

  def make(settings: ConsumerSettings): ZIO[Scope, Nothing, ConsumerAccess] =
    for {
      consumer <- ZIO.acquireRelease {
                    ZIO.attemptBlocking {
                      new KafkaConsumer[Array[Byte], Array[Byte]](
                        settings.driverSettings.asJava,
                        new ByteArrayDeserializer(),
                        new ByteArrayDeserializer()
                      )
                    }.orDie
                  } { consumer =>
                    ZIO.blocking(ZIO.attempt(consumer.close(settings.closeTimeout))).orDie
                  }
      result <- make(consumer)
    } yield result

  def make(consumer: ByteArrayKafkaConsumer): ZIO[Scope, Nothing, ConsumerAccess] =
    for {
      access <- Semaphore.make(1)
    } yield new ConsumerAccess(consumer, access)
}
