package zio.kafka.producer

import org.apache.kafka.clients.consumer.{ ConsumerGroupMetadata, OffsetAndMetadata }
import org.apache.kafka.clients.producer.{ Callback, ProducerRecord, RecordMetadata }
import org.apache.kafka.common.{ Metric, MetricName, PartitionInfo, TopicPartition, Uuid }
import org.apache.kafka.clients.producer.{ Producer => KafkaProducer }
import zio._

import java.time.Duration
import java.util.concurrent.atomic.AtomicInteger
import java.util.{ List => JList, Map => JMap }
import java.util.concurrent.{ CompletableFuture, Future => JFuture }
import scala.collection.mutable

/**
 * A test framework for testing use of the async `send(ProducerRecord, Callback)` method of the Kafka producer, where
 * the resulting [[java.util.concurrent.Future]] is ignored.
 *
 * Use it as follows:
 * {{{
 * val sendException = new RuntimeException("fail from send")
 * val callbackException = new RuntimeException("fail from callback")
 * val expectations = AsyncProducerTestSupport.expectations[Array[Byte], Array[Byte]]()
 *   .sendSucceed()                   // send 0
 *   .sendFail(sendException)         // send fails immediately (no number)
 *   .sendSucceed()                   // send 1
 *   .callbackSucceed()               // callback for send 0
 *   .callbackFail(callbackException) // callback for send 1
 *   ////// Out of order callbacks:
 *   .sendSucceed() // send 2
 *   .sendSucceed() // send 3
 *   .sendSucceed() // send 4
 *   .callbackSucceed(3) // out of order callback of send 3
 *   .callbackSucceed()  // callback of send 2
 *   .callbackSucceed()  // callback of send 4
 *   ////// Basic checks
 *   .callbackSucceed()   // Fails because all sends already have a callback
 *   .callbackSucceed(1)  // Fails because send 1 already has a callback
 * }}}
 *
 * When all expectations have been set, you can use the `run` method and use the given mock producer as if it is a
 * regular Kafka producer. The mock producer is thread-safe; multiple threads can use it simultaneously.
 *
 * {{{
 * expectations.run { producer =>
 *   // use the producer, make assertions
 * }
 * }}}
 *
 * The 'send' expectations are expected in the order they are given. When 'publisher.send' is invoked while a callback
 * expectation is pending, the 'send' operation is blocked until the callback expectation is met.
 *
 * Get a history of all send records (all 'send' attempts) with: `expectations.history()`.
 */
object AsyncProducerTestSupport {

  trait AsyncMockProducerExpectations[K, V] {
    def sendSucceed(): AsyncMockProducerExpectations[K, V]
    def sendFail(e: Throwable): AsyncMockProducerExpectations[K, V]
    def callbackSucceed(): AsyncMockProducerExpectations[K, V]
    def callbackSucceed(n: Int): AsyncMockProducerExpectations[K, V]
    def callbackFail(e: Exception): AsyncMockProducerExpectations[K, V]
    def callbackFail(n: Int, e: Exception): AsyncMockProducerExpectations[K, V]
    def run[R, A](testCode: KafkaProducer[K, V] => ZIO[R, Throwable, A]): ZIO[R, Throwable, A]
    def history: Chunk[ProducerRecord[K, V]]
  }

  private sealed trait MockOperation
  private case class SendSucceed(n: Int) extends MockOperation
  private case class SendFail(e: Throwable) extends MockOperation {
    override def toString: String = s"SendFail(${e.getClass.getSimpleName})"
  }
  private case class CallbackSucceed(n: Int) extends MockOperation
  private case class CallbackFail(n: Int, e: Exception) extends MockOperation {
    override def toString: String = s"CallbackFail($n, ${e.getClass.getSimpleName})"
  }

  def expectations[K, V](): AsyncMockProducerExpectations[K, V] = new AsyncMockProducerExpectations[K, V] {
    private val operations: ChunkBuilder[MockOperation]      = Chunk.newBuilder
    private val callbacksAvailable: mutable.Set[Int]         = mutable.BitSet.empty
    private var callbackCount: Int                           = 0
    private val _history: ChunkBuilder[ProducerRecord[K, V]] = Chunk.newBuilder

    override def sendSucceed(): AsyncMockProducerExpectations[K, V] = {
      operations += SendSucceed(callbackCount)
      callbacksAvailable += callbackCount
      callbackCount += 1
      this
    }
    override def sendFail(e: Throwable): AsyncMockProducerExpectations[K, V] = {
      operations += SendFail(e)
      this
    }
    private def addCallback(n: Option[Int], op: Int => MockOperation): AsyncMockProducerExpectations[K, V] = {
      n match {
        case Some(n) =>
          if (callbacksAvailable.contains(n)) {
            callbacksAvailable -= n
            operations += op(n)
          } else {
            throw new AssertionError(
              s"Callback expectation for send $n can not be added because that send expectation does not exist, or it already has a callback"
            )
          }
        case None =>
          throw new AssertionError(
            s"Callback expectation can not be added because all send expectations already have a callback"
          )
      }
      this
    }
    override def callbackSucceed(): AsyncMockProducerExpectations[K, V] =
      addCallback(callbacksAvailable.minOption, CallbackSucceed(_))
    override def callbackSucceed(n: Int): AsyncMockProducerExpectations[K, V] =
      addCallback(Some(n), CallbackSucceed(_))
    override def callbackFail(e: Exception): AsyncMockProducerExpectations[K, V] =
      addCallback(callbacksAvailable.minOption, CallbackFail(_, e))
    override def callbackFail(n: Int, e: Exception): AsyncMockProducerExpectations[K, V] =
      addCallback(Some(n), CallbackFail(_, e))

    override def run[R, A](testCode: KafkaProducer[K, V] => ZIO[R, Throwable, A]): ZIO[R, Throwable, A] = {
      if (callbacksAvailable.nonEmpty) {
        throw new AssertionError(s"Missing ${callbacksAvailable.size} callback expectations")
      }

      sealed trait SendOperation {
        def startPromise: Promise[Nothing, Unit]
        def callbackPromise: Promise[Nothing, Callback]
      }
      case class SendSucceedOperation(
        n: Int,
        startPromise: Promise[Nothing, Unit],
        callbackPromise: Promise[Nothing, Callback]
      ) extends SendOperation
      case class SendFailOperation(
        e: Throwable,
        startPromise: Promise[Nothing, Unit],
        callbackPromise: Promise[Nothing, Callback]
      ) extends SendOperation

      val ops = operations.result()
      for {
        sendOperations <- ZIO.collect(ops) {
                            case SendSucceed(n) =>
                              for {
                                startPromise    <- Promise.make[Nothing, Unit]
                                callbackPromise <- Promise.make[Nothing, Callback]
                              } yield SendSucceedOperation(n, startPromise, callbackPromise)
                            case SendFail(e) =>
                              for {
                                startPromise    <- Promise.make[Nothing, Unit]
                                callbackPromise <- Promise.make[Nothing, Callback]
                              } yield SendFailOperation(e, startPromise, callbackPromise)
                            case _ => ZIO.fail(None)
                          }
        runtime <- ZIO.runtime[Any]
        mockProducer = new NotSupportedProducer[K, V] {
                         private val sendCount = new AtomicInteger(0)
                         override def send(
                           record: ProducerRecord[K, V],
                           callback: Callback
                         ): JFuture[RecordMetadata] = {
                           _history += record
                           val n = sendCount.getAndIncrement()
                           if (n >= sendOperations.size)
                             throw new AssertionError(s"There is no expectation for send $n")
                           val sendOperation = sendOperations(n)
                           Unsafe.unsafe { implicit u =>
                             runtime.unsafe.run {
                               for {
                                 _ <- sendOperation.startPromise.await
                                 _ <- sendOperation.callbackPromise.succeed(callback)
                               } yield ()
                             }
                               .getOrThrowFiberFailure()
                           }
                           sendOperation match {
                             // return a dummy future, it is never completed
                             case _: SendSucceedOperation => new CompletableFuture[RecordMetadata]()
                             case sfo: SendFailOperation  => throw sfo.e
                           }
                         }
                       }
        soi = sendOperations.iterator
        handleOperations = ZIO.foreach(ops) {
                             case op @ (SendSucceed(_) | SendFail(_)) =>
                               for {
                                 sendOperation <-
                                   ZIO
                                     .fromOption(soi.nextOption())
                                     .orElseFail(new AssertionError("Bug in AsyncProducerTestSupport: unexpected operation"))
                                 _ <- sendOperation.startPromise.succeed(())
                                 _ <- ZIO.raceFirst(
                                        sendOperation.callbackPromise.await,
                                        Seq(ZIO.logInfo(s"Expectation for $op still not met").delay(5.seconds).forever)
                                      )
                               } yield ()
                             case CallbackSucceed(n) =>
                               for {
                                 callbackPromise <-
                                   ZIO
                                     .fromOption(sendOperations.collectFirst {
                                       case SendSucceedOperation(n2, _, callbackPromise) if n2 == n => callbackPromise
                                     })
                                     .orElseFail(new AssertionError("Bug in AsyncProducerTestSupport: unexpected operation"))
                                 callback <- callbackPromise.await
                                 // return dummy metadata
                                 metadata = new RecordMetadata(new TopicPartition("", 0), 0, 0, 0, 0, 0)
                                 _ <- ZIO.attempt(callback.onCompletion(metadata, null))
                               } yield ()
                             case CallbackFail(n, e) =>
                               for {
                                 callbackPromise <-
                                   ZIO
                                     .fromOption(sendOperations.collectFirst {
                                       case SendSucceedOperation(n2, _, callbackPromise) if n2 == n => callbackPromise
                                     })
                                     .orElseFail(new AssertionError("Bug in AsyncProducerTestSupport: unexpected operation"))
                                 callback <- callbackPromise.await
                                 _        <- ZIO.attempt(callback.onCompletion(null, e))
                               } yield ()
                           }
        result <- handleOperations &> testCode(mockProducer)
      } yield result
    }

    override def history: Chunk[ProducerRecord[K, V]] = _history.result()
  }
}

/**
 * A [[KafkaProducer]] that does not support any operation.
 *
 * @tparam K
 *   key type
 * @tparam V
 *   value type
 */
class NotSupportedProducer[K, V] extends KafkaProducer[K, V] {
  override def initTransactions(): Unit = throw new UnsupportedOperationException()

  override def beginTransaction(): Unit = throw new UnsupportedOperationException()

  override def sendOffsetsToTransaction(
    offsets: JMap[TopicPartition, OffsetAndMetadata],
    consumerGroupId: String
  ): Unit = throw new UnsupportedOperationException()

  override def sendOffsetsToTransaction(
    offsets: JMap[TopicPartition, OffsetAndMetadata],
    groupMetadata: ConsumerGroupMetadata
  ): Unit = throw new UnsupportedOperationException()

  override def commitTransaction(): Unit = throw new UnsupportedOperationException()

  override def abortTransaction(): Unit = throw new UnsupportedOperationException()

  override def send(record: ProducerRecord[K, V]): JFuture[RecordMetadata] = throw new UnsupportedOperationException()

  override def send(record: ProducerRecord[K, V], callback: Callback): JFuture[RecordMetadata] =
    throw new UnsupportedOperationException()

  override def flush(): Unit = throw new UnsupportedOperationException()

  override def partitionsFor(topic: String): JList[PartitionInfo] = throw new UnsupportedOperationException()

  override def metrics(): JMap[MetricName, _ <: Metric] = throw new UnsupportedOperationException()

  override def clientInstanceId(timeout: Duration): Uuid = throw new UnsupportedOperationException()

  override def close(): Unit = throw new UnsupportedOperationException()

  override def close(timeout: Duration): Unit = throw new UnsupportedOperationException()
}
