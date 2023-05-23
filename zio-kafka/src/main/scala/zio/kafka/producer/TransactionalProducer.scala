package zio.kafka.producer

import org.apache.kafka.clients.consumer.OffsetAndMetadata
import org.apache.kafka.clients.producer.{ KafkaProducer, RecordMetadata }
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.errors.InvalidGroupIdException
import org.apache.kafka.common.serialization.ByteArraySerializer
import zio.Cause.Fail
import zio._
import zio.kafka.consumer.{ Consumer, OffsetBatch }

import java.util
import scala.jdk.CollectionConverters._

trait TransactionalProducer {
  def createTransaction: ZIO[Scope & Consumer, Throwable, Transaction]
}

object TransactionalProducer {
  case object UserInitiatedAbort
  final case class TransactionLeaked(offsetBatch: OffsetBatch) extends RuntimeException

  private final class LiveTransactionalProducer(
    live: Producer.Live,
    semaphore: Semaphore
  ) extends TransactionalProducer {
    private val abortTransaction: Task[Unit] = ZIO.attemptBlocking(live.p.abortTransaction())

    private def commitTransactionWithOffsets(offsetBatch: OffsetBatch): ZIO[Consumer, Throwable, Unit] = {
      def sendOffsetsToTransaction: ZIO[Consumer, Throwable, Unit] =
        for {
          consumerGroupMetadata <-
            ZIO.serviceWithZIO[Consumer](
              _.consumerGroupMetadata.flatMap {
                case Some(metadata) => ZIO.succeed(metadata)
                case None =>
                  ZIO.fail(
                    new InvalidGroupIdException(
                      "To use the group management or offset commit APIs, you must provide a valid group.id in the consumer configuration."
                    )
                  )
              }
            )

          offsets = offsetBatch.offsets.map { case (topicPartition, offset) =>
                      topicPartition -> new OffsetAndMetadata(offset + 1)
                    }.asJava

          _ <- ZIO.attemptBlocking(live.p.sendOffsetsToTransaction(offsets, consumerGroupMetadata))
        } yield ()

      sendOffsetsToTransaction.when(offsetBatch.nonEmpty) *> ZIO.attemptBlocking(live.p.commitTransaction())
    }

    private def commitOrAbort(transaction: TransactionImpl, exit: Exit[Any, Any]): URIO[Consumer, Unit] =
      exit match {
        case Exit.Success(_) =>
          transaction.offsetBatchRef.get
            .flatMap(offsetBatch => commitTransactionWithOffsets(offsetBatch).retryN(5).orDie)
        case Exit.Failure(Fail(UserInitiatedAbort, _)) => abortTransaction.retryN(5).orDie
        case Exit.Failure(_)                           => abortTransaction.retryN(5).orDie
      }

    override def createTransaction: ZIO[Scope & Consumer, Throwable, Transaction] =
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

  def createTransaction: ZIO[TransactionalProducer & Scope & Consumer, Throwable, Transaction] =
    ZIO.service[TransactionalProducer].flatMap(_.createTransaction)

  val live: RLayer[TransactionalProducerSettings, TransactionalProducer] =
    ZLayer.scoped {
      for {
        settings <- ZIO.service[TransactionalProducerSettings]
        producer <- make(settings)
      } yield producer
    }

  def make(settings: TransactionalProducerSettings): ZIO[Scope, Throwable, TransactionalProducer] =
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
      runtime   <- ZIO.runtime[Any]
      sendQueue <-
        Queue.bounded[(Chunk[ByteRecord], Promise[Throwable, Chunk[RecordMetadata]])](
          settings.producerSettings.sendBufferSize
        )
      live <- ZIO.acquireRelease(
                ZIO.succeed(new Producer.Live(rawProducer, settings.producerSettings, runtime, sendQueue))
              )(_.close)
      _ <- ZIO.blocking(live.sendFromQueue).forkScoped
    } yield new LiveTransactionalProducer(live, semaphore)
}
