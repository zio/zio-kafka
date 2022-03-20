package zio.kafka.producer

import org.apache.kafka.clients.consumer.OffsetAndMetadata
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.common.errors.InvalidGroupIdException
import org.apache.kafka.common.serialization.ByteArraySerializer
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

  def createTransaction: ZIO[Scope with TransactionalProducer, Throwable, Transaction] =
    ZIO.service[TransactionalProducer].flatMap(_.createTransaction)

  val live: RLayer[TransactionalProducerSettings, TransactionalProducer] =
    ZLayer.scoped {
      for {
        settings <- ZIO.service[TransactionalProducerSettings]
        producer <- make(settings)
      } yield producer
    }

  def make(settings: TransactionalProducerSettings): ZIO[Scope, Throwable, TransactionalProducer] =
    ZIO.acquireRelease {
      for {
        props <- ZIO.attempt(settings.producerSettings.driverSettings)
        rawProducer <- ZIO.attempt(
                         new KafkaProducer[Array[Byte], Array[Byte]](
                           props.asJava,
                           new ByteArraySerializer(),
                           new ByteArraySerializer()
                         )
                       )
        _         <- ZIO.attemptBlocking(rawProducer.initTransactions())
        semaphore <- Semaphore.make(1)
        live = Producer.Live(rawProducer, settings.producerSettings)
      } yield LiveTransactionalProducer(live, semaphore)
    }(_.live.close)
}
