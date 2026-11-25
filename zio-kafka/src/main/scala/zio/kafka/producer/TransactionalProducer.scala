package zio.kafka.producer

import org.apache.kafka.clients.consumer.OffsetAndMetadata
import org.apache.kafka.clients.producer.{ KafkaProducer, RecordMetadata }
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.errors.InvalidGroupIdException
import org.apache.kafka.common.serialization.ByteArraySerializer
import zio.Cause.Fail
import zio._
import zio.kafka.consumer.{ Consumer, OffsetBatch }
import zio.kafka.producer.ProducerLive.NanoTime
import zio.kafka.producer.metrics.ZioMetricsProducerMetricsObserver
import zio.kafka.utils.SslHelper

import java.util
import scala.jdk.CollectionConverters._

/**
 * A producer that produces records transactionally.
 *
 * It is used together with a [[zio.kafka.consumer.Consumer]]: a transaction commits the offsets of the consumed records
 * together with the produced records.
 *
 * @see
 *   [[https://zio.dev/zio-kafka/transactions/ Transactions Tutorial]]
 */
trait TransactionalProducer {

  /**
   * Begins a transaction. The transaction lives in the given `Scope`: when the scope closes successfully, the offsets
   * that were given to the transaction's produce methods are sent to the transaction, the transaction is committed and
   * the consumer is told about the committed offsets. When the scope closes with a failure or an interruption, the
   * transaction is aborted. Committing and aborting are each retried up to 5 times, after which the effect that closes
   * the scope dies.
   *
   * A transactional producer runs one transaction at a time: this effect waits until the scope of the previous
   * transaction has closed.
   */
  def createTransaction: ZIO[Scope, Throwable, Transaction]
}

/**
 * Creates a [[TransactionalProducer]], and has the failures of its transactions.
 */
object TransactionalProducer {

  /**
   * The failure of [[Transaction.abort]]. It is not a `Throwable`.
   */
  case object UserInitiatedAbort

  /**
   * The failure of the produce methods of a [[Transaction]] that is used after its scope has closed.
   *
   * @param offsetBatch
   *   the offsets that the transaction had collected when it was closed
   */
  final case class TransactionLeaked(offsetBatch: OffsetBatch) extends RuntimeException

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
              val offsets: util.Map[TopicPartition, OffsetAndMetadata] = offsetBatch.nextOffsets.asJava
              ZIO.attemptBlocking(live.p.sendOffsetsToTransaction(offsets, consumerGroupMetadata))
          }
        }

      sendOffsetsToTransaction.when(offsetBatch.nextOffsets.nonEmpty) *>
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

  /**
   * A layer that creates a [[TransactionalProducer]] with [[make]], using the `TransactionalProducerSettings` and the
   * `Consumer` from the environment.
   */
  val live: RLayer[TransactionalProducerSettings with Consumer, TransactionalProducer] =
    ZLayer.scoped {
      for {
        settings <- ZIO.service[TransactionalProducerSettings]
        consumer <- ZIO.service[Consumer]
        producer <- make(settings, consumer)
      } yield producer
    }

  /**
   * Creates a [[TransactionalProducer]] that works together with the given consumer.
   *
   * It first runs [[zio.kafka.utils.SslHelper.validateEndpoint]] on the producer's properties, and checks that the
   * consumer's settings are valid for use with a transactional producer. Rebalance-safe commits must be enabled, and
   * the `CooperativeStickyAssignor` is not supported; otherwise the effect fails with an `IllegalArgumentException`. It
   * then creates the Kafka producer and initializes its transactions. The Kafka producer is closed when the scope
   * closes, waiting at most `closeTimeout` of the producer settings.
   */
  def make(settings: TransactionalProducerSettings, consumer: Consumer): ZIO[Scope, Throwable, TransactionalProducer] =
    for {
      _                  <- SslHelper.validateEndpoint(settings.producerSettings.driverSettings)
      _                  <- consumer.consumerSettings.validateForTransactional
      wrappedDiagnostics <- Producer.makeConcurrentDiagnostics(settings.producerSettings.diagnostics)
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
        Queue.bounded[(Chunk[ByteRecord], NanoTime, Chunk[Either[Throwable, RecordMetadata]] => UIO[Unit])](
          settings.producerSettings.sendBufferSize
        )
      metrics = settings.producerSettings.metricsObserver
                  .getOrElse(new ZioMetricsProducerMetricsObserver(settings.producerSettings.metricLabels))
      live = new ProducerLive(settings.producerSettings, wrappedDiagnostics, rawProducer, runtime, sendQueue, metrics)
      _ <- sendQueue.size
             .flatMap(metrics.observeSendQueueSize)
             .schedule(Schedule.fixed(10.seconds))
             .interruptible
             .forkScoped
      _ <- ZIO.blocking(live.sendFromQueue).interruptible.forkScoped
    } yield new LiveTransactionalProducer(live, semaphore, consumer)
}
