package zio.kafka.producer

import org.apache.kafka.clients.consumer.OffsetAndMetadata
import org.apache.kafka.clients.producer.{ Producer => JProducer }
import org.apache.kafka.common.errors.InvalidGroupIdException
import zio.Cause.Fail
import zio.blocking.Blocking
import zio.kafka.consumer.OffsetBatch
import zio.{ Exit, Has, RIO, RLayer, RManaged, Ref, Semaphore, Task, UIO, ZIO, ZManaged }

import scala.jdk.CollectionConverters._

trait TransactionalProducer {
  def createTransaction: ZManaged[Any, Throwable, Transaction]
}

object TransactionalProducer {
  case object UserInitiatedAbort
  final case class TransactionLeaked(offsetBatch: OffsetBatch) extends Throwable

  private final case class LiveTransactionalProducer(
    live: Producer.Live,
    blocking: Blocking.Service,
    semaphore: Semaphore
  ) extends TransactionalProducer {
    val abortTransaction: Task[Unit] = blocking.effectBlocking(live.p.abortTransaction())
    def commitTransactionWithOffsets(offsetBatch: OffsetBatch): Task[Unit] =
      blocking
        .effectBlocking(
          live.p.sendOffsetsToTransaction(
            offsetBatch.offsets.map { case (topicPartition, offset) =>
              topicPartition -> new OffsetAndMetadata(offset + 1)
            }.asJava,
            offsetBatch.consumerGroupMetadata
              .getOrElse(
                throw new InvalidGroupIdException(
                  "To use the group management or offset commit APIs, you must provide a valid group.id in the consumer configuration."
                )
              )
          )
        )
        .unless(offsetBatch.offsets.isEmpty) *>
        blocking.effectBlocking(live.p.commitTransaction())

    def commitOrAbort(transaction: TransactionImpl, exit: Exit[Any, Any]): UIO[Unit] = exit match {
      case Exit.Success(_) =>
        transaction.offsetBatchRef.get
          .flatMap(offsetBatch => commitTransactionWithOffsets(offsetBatch).retryN(5).orDie)
      case Exit.Failure(Fail(UserInitiatedAbort)) => abortTransaction.retryN(5).orDie
      case Exit.Failure(_)                        => abortTransaction.retryN(5).orDie
    }

    def createTransaction: ZManaged[Any, Throwable, Transaction] =
      semaphore.withPermitManaged *> {
        ZManaged.makeExit {
          for {
            offsetBatchRef <- Ref.make(OffsetBatch.empty)
            closedRef      <- Ref.make(false)
            _              <- blocking.effectBlocking(live.p.beginTransaction())
          } yield new TransactionImpl(producer = live, offsetBatchRef = offsetBatchRef, closed = closedRef)
        } { case (transaction: TransactionImpl, exit) => transaction.markAsClosed *> commitOrAbort(transaction, exit) }
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
    fromManagedJavaProducer(Producer.javaProducerFromSettings(settings.producerSettings))

  def fromJavaProducer(javaProducer: => JProducer[Array[Byte], Array[Byte]]): RIO[Blocking, TransactionalProducer] =
    for {
      blocking  <- ZIO.service[Blocking.Service]
      _         <- blocking.effectBlocking(javaProducer.initTransactions())
      semaphore <- Semaphore.make(1)
      live = Producer.Live(javaProducer, blocking)
    } yield LiveTransactionalProducer(live, blocking, semaphore)

  def fromManagedJavaProducer[R](
    managedJavaProducer: ZManaged[R, Throwable, JProducer[Array[Byte], Array[Byte]]]
  ): ZManaged[R with Blocking, Throwable, TransactionalProducer] =
    managedJavaProducer.flatMap(javaProducer => ZManaged.fromEffect(fromJavaProducer(javaProducer)))

}
