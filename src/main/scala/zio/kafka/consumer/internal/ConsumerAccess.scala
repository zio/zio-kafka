package zio.kafka.consumer.internal

import org.apache.kafka.clients.consumer.{ Consumer => JConsumer }
import org.apache.kafka.common.errors.WakeupException
import zio._
import zio.kafka.consumer.internal.ConsumerAccess.ByteArrayKafkaConsumer

private[consumer] class ConsumerAccess(
  private[consumer] val consumer: ByteArrayKafkaConsumer,
  access: Semaphore
) {
  def withConsumer[A](f: ByteArrayKafkaConsumer => A): Task[A] =
    withConsumerM[Any, A](c => ZIO.attempt(f(c)))

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
  type ByteArrayKafkaConsumer = JConsumer[Array[Byte], Array[Byte]]

  def fromJavaConsumer(
    javaConsumer: => ByteArrayKafkaConsumer,
    closeTimeout: Duration
  ): ZIO[Scope, Throwable, ConsumerAccess] =
    for {
      access <- Semaphore.make(1)
      consumer <- ZIO.acquireRelease {
                    ZIO.attemptBlocking(javaConsumer)
                  } { consumer =>
                    ZIO.blocking(access.withPermit(ZIO.succeed(consumer.close(closeTimeout))))
                  }
    } yield new ConsumerAccess(consumer, access)
}
