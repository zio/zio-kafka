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

/**
 * A producer that produces records transactionally.
 */
trait TransactionalProducer {
  def createTransaction: ZIO[Scope, Throwable, Transaction]
}

object TransactionalProducer {
  case object UserInitiatedAbort
  final case class TransactionLeaked(offsetBatch: OffsetBatch) extends RuntimeException
  case object RebalanceSafeConsumerRequired
      extends RuntimeException(
        "Consumer settings must have rebalanceSafeCommits set to true to use transactional producer."
      )

  private final class LiveTransactionalProducer(
    live: ProducerLive,
    semaphore: Semaphore,
    consumer: Consumer
  ) extends TransactionalProducer {
    private val abortTransaction: Task[Unit] = ZIO.attemptBlocking(live.p.abortTransaction())

    private def commitTransactionWithOffsets(offsetBatch: OffsetBatch): Task[Unit] = {
      val sendOffsetsToTransaction: Task[Unit] =
        ZIO.suspend {
          @inline def invalidGroupIdException: IO[InvalidGroupIdException, Nothing] =
            ZIO.fail(
              new InvalidGroupIdException(
                "To use the group management or offset commit APIs, you must provide a valid group.id in the consumer configuration."
              )
            )

          offsetBatch.consumerGroupMetadata match {
            case None => invalidGroupIdException
            case Some(consumerGroupMetadata) =>
              val offsets: util.Map[TopicPartition, OffsetAndMetadata] =
                offsetBatch.offsets.map { case (topicPartition, offset) =>
                  topicPartition -> new OffsetAndMetadata(offset.offset + 1, offset.metadata)
                }.asJava

              ZIO.attemptBlocking(live.p.sendOffsetsToTransaction(offsets, consumerGroupMetadata))
          }
        }

      sendOffsetsToTransaction.when(offsetBatch.offsets.nonEmpty) *>
        ZIO.attemptBlocking(live.p.commitTransaction()) *>
        consumer.registerExternalCommits(offsetBatch).unit
    }

    private def commitOrAbort(transaction: TransactionImpl, exit: Exit[Any, Any]): ZIO[Any, Nothing, Unit] =
      exit match {
        case Exit.Success(_) =>
          transaction.offsetBatchRef.get
            .flatMap(offsetBatch => commitTransactionWithOffsets(offsetBatch).retryN(5).orDie)
        case Exit.Failure(Fail(UserInitiatedAbort, _)) => abortTransaction.retryN(5).orDie
        case Exit.Failure(_)                           => abortTransaction.retryN(5).orDie
      }

    override def createTransaction: ZIO[Scope, Throwable, Transaction] =
      semaphore.withPermitScoped *>
        ZIO.acquireReleaseExit {
          for {
            offsetBatchRef <- Ref.make(OffsetBatch.empty)
            closedRef      <- Ref.make(false)
            _              <- ZIO.attemptBlocking(live.p.beginTransaction())
          } yield new TransactionImpl(producer = live, offsetBatchRef = offsetBatchRef, closed = closedRef)
        } { case (transaction: TransactionImpl, exit) => transaction.markAsClosed *> commitOrAbort(transaction, exit) }
  }

  val live: RLayer[TransactionalProducerSettings with Consumer, TransactionalProducer] =
    ZLayer.scoped {
      for {
        settings <- ZIO.service[TransactionalProducerSettings]
        consumer <- ZIO.service[Consumer]
        producer <- make(settings, consumer)
      } yield producer
    }

  def make(settings: TransactionalProducerSettings, consumer: Consumer): ZIO[Scope, Throwable, TransactionalProducer] =
    for {
      _ <- ZIO.cond(consumer.consumerSettings.rebalanceSafeCommits, (), RebalanceSafeConsumerRequired)
      rawProducer <- ZIO.acquireRelease(
                       ZIO.attempt(
                         new KafkaProducer[Array[Byte], Array[Byte]](
                           settings.producerSettings.driverSettings.asJava,
                           new ByteArraySerializer(),
                           new ByteArraySerializer()
                         )
                       )
                     )(p => ZIO.attemptBlocking(p.close(settings.producerSettings.closeTimeout)).orDie)
      _         <- ZIO.attemptBlocking(rawProducer.initTransactions())
      semaphore <- Semaphore.make(1)
      runtime   <- ZIO.runtime[Any]
      sendQueue <-
        Queue.bounded[(Chunk[ByteRecord], Chunk[Either[Throwable, RecordMetadata]] => UIO[Unit])](
          settings.producerSettings.sendBufferSize
        )
      live = new ProducerLive(settings.producerSettings, rawProducer, runtime, sendQueue)
      _ <- ZIO.blocking(live.sendFromQueue).forkScoped
    } yield new LiveTransactionalProducer(live, semaphore, consumer)
}
