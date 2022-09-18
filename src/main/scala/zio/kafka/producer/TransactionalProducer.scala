package zio.kafka.producer

import org.apache.kafka.clients.consumer.OffsetAndMetadata
import org.apache.kafka.clients.producer.{ Producer => JProducer }
import org.apache.kafka.common.errors.InvalidGroupIdException
import zio.Cause.Fail
import zio.kafka.consumer.OffsetBatch
import zio._

import scala.jdk.CollectionConverters._

trait TransactionalProducer {
  def createTransaction: ZIO[Scope, Throwable, Transaction]
}

object TransactionalProducer {
  case object UserInitiatedAbort
  final case class TransactionLeaked(offsetBatch: OffsetBatch) extends Throwable

  private final case class LiveTransactionalProducer(
    live: Producer.Live,
    semaphore: Semaphore
  ) extends TransactionalProducer {
    val abortTransaction: Task[Unit] = ZIO.attemptBlocking(live.p.abortTransaction())
    def commitTransactionWithOffsets(offsetBatch: OffsetBatch): Task[Unit] =
      ZIO
        .attemptBlocking(
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
        ZIO.attemptBlocking(live.p.commitTransaction())

    def commitOrAbort(transaction: TransactionImpl, exit: Exit[Any, Any]): UIO[Unit] = exit match {
      case Exit.Success(_) =>
        transaction.offsetBatchRef.get
          .flatMap(offsetBatch => commitTransactionWithOffsets(offsetBatch).retryN(5).orDie)
      case Exit.Failure(Fail(UserInitiatedAbort, _)) => abortTransaction.retryN(5).orDie
      case Exit.Failure(_)                           => abortTransaction.retryN(5).orDie
    }

    def createTransaction: ZIO[Scope, Throwable, Transaction] =
      semaphore.withPermitScoped *> {
        ZIO.acquireReleaseExit {
          for {
            offsetBatchRef <- Ref.make(OffsetBatch.empty)
            closedRef      <- Ref.make(false)
            _              <- ZIO.attemptBlocking(live.p.beginTransaction())
          } yield new TransactionImpl(producer = live, offsetBatchRef = offsetBatchRef, closed = closedRef)
        } { case (transaction: TransactionImpl, exit) => transaction.markAsClosed *> commitOrAbort(transaction, exit) }
      }
  }

  def createTransaction: ZIO[TransactionalProducer with Scope, Throwable, Transaction] =
    ZIO.service[TransactionalProducer].flatMap(_.createTransaction)

  val live: RLayer[TransactionalProducerSettings, TransactionalProducer] =
    ZLayer.scoped {
      for {
        settings <- ZIO.service[TransactionalProducerSettings]
        producer <- make(settings)
      } yield producer
    }

  def make(settings: TransactionalProducerSettings): RIO[Scope, TransactionalProducer] =
    fromManagedJavaProducer(Producer.javaProducerFromSettings(settings.producerSettings), settings.producerSettings)

  def fromJavaProducer(
    javaProducer: => JProducer[Array[Byte], Array[Byte]],
    settings: ProducerSettings
  ): Task[TransactionalProducer] =
    for {
      _         <- ZIO.attemptBlocking(javaProducer.initTransactions())
      semaphore <- Semaphore.make(1)
      live = Producer.Live(javaProducer, settings)
    } yield LiveTransactionalProducer(live, semaphore)

  def fromManagedJavaProducer[R](
    managedJavaProducer: ZIO[R & Scope, Throwable, JProducer[Array[Byte], Array[Byte]]],
    settings: ProducerSettings
  ): ZIO[R & Scope, Throwable, TransactionalProducer] =
    managedJavaProducer.flatMap(javaProducer => fromJavaProducer(javaProducer, settings))

}
