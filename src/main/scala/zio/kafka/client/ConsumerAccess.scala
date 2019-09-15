package zio.kafka.client

import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.errors.WakeupException
import org.apache.kafka.common.serialization.Serde
import zio._
import zio.blocking.{ blocking, Blocking }

import scala.collection.JavaConverters._

class ConsumerAccess[K, V](private[client] val consumer: KafkaConsumer[K, V], access: Semaphore) {
  def withConsumer[A](f: KafkaConsumer[K, V] => A): BlockingTask[A] =
    withConsumerM[Any, A](c => ZIO(f(c)))

  def withConsumerM[R, A](f: KafkaConsumer[K, V] => ZIO[R, Throwable, A]): ZIO[R with Blocking, Throwable, A] =
    access.withPermit(withConsumerNoPermit(f))

  private[client] def withConsumerNoPermit[R, A](
    f: KafkaConsumer[K, V] => ZIO[R, Throwable, A]
  ): ZIO[R with Blocking, Throwable, A] =
    blocking(f(consumer)).catchSome {
      case _: WakeupException => ZIO.interrupt
    }.fork.flatMap { fib =>
      fib.join.onInterrupt(ZIO.effectTotal(consumer.wakeup()) *> fib.interrupt)
    }
}

object ConsumerAccess {
  def make[K, V](settings: ConsumerSettings)(implicit K: Serde[K], V: Serde[V]) =
    for {
      access <- Semaphore.make(1).toManaged_
      consumer <- blocking {
                   ZIO {
                     new KafkaConsumer[K, V](
                       settings.driverSettings.asJava,
                       K.deserializer,
                       V.deserializer
                     )
                   }
                 }.toManaged { c =>
                   blocking(access.withPermit(UIO(c.close(settings.closeTimeout.asJava))))
                 }
    } yield new ConsumerAccess(consumer, access)
}
