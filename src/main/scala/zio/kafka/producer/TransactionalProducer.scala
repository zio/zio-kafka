package zio.kafka.producer

import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.common.serialization.ByteArraySerializer
import zio.Cause.Fail
import zio.blocking.Blocking
import zio.{ Exit, Has, IO, RLayer, RManaged, Semaphore, Task, ZIO, ZManaged }

import scala.jdk.CollectionConverters._

trait TransactionalProducer extends Producer {
  def createTransaction: ZManaged[Any, Throwable, Transaction]
}

object TransactionalProducer {

  case object UserInitiatedAbort

  private class LiveTransactionalProducer(
    p: KafkaProducer[Array[Byte], Array[Byte]],
    producerSettings: ProducerSettings,
    blocking: Blocking.Service,
    semaphore: Semaphore
  ) extends Producer.Live(p, producerSettings, blocking)
      with TransactionalProducer {
    val abortTransaction: Task[Unit]  = blocking.effectBlocking(p.abortTransaction())
    val commitTransaction: Task[Unit] = blocking.effectBlocking(p.commitTransaction())

    override def createTransaction: ZManaged[Any, Throwable, Transaction] =
      semaphore.withPermitManaged *> {
        ZManaged.makeExit {
          for {
            _ <- IO(p.beginTransaction())
          } yield new Transaction(producer = this)
        } {
          case (_, Exit.Success(_))                        => commitTransaction.retryN(5).orDie
          case (_, Exit.Failure(Fail(UserInitiatedAbort))) => abortTransaction.retryN(5).orDie
          case (_, Exit.Failure(_))                        => abortTransaction.retryN(5).orDie
        }
      }
  }

  def createTransaction: RManaged[Has[TransactionalProducer], Transaction] =
    ZManaged.service[TransactionalProducer].flatMap(_.createTransaction)

  val live: RLayer[Has[TransactionalProducerSettings] with Blocking, Has[TransactionalProducer]] =
    (for {
      settings <- ZManaged.service[TransactionalProducerSettings]
      producer <- make(settings)
    } yield producer).toLayer

  def make(settings: TransactionalProducerSettings): RManaged[Blocking, TransactionalProducer] =
    (for {
      props       <- ZIO.effect(settings.driverSettings)
      blocking    <- ZIO.service[Blocking.Service]
      rawProducer <- ZIO.effect(
                       new KafkaProducer[Array[Byte], Array[Byte]](
                         props.asJava,
                         new ByteArraySerializer(),
                         new ByteArraySerializer()
                       )
                     )
      _           <- blocking.effectBlocking(rawProducer.initTransactions())
      semaphore   <- Semaphore.make(1)
    } yield new LiveTransactionalProducer(rawProducer, settings, blocking, semaphore)).toManaged(_.close)
}
