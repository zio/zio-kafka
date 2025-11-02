package zio.kafka.producer

import org.apache.kafka.clients.consumer.{ ConsumerGroupMetadata, OffsetAndMetadata }
import org.apache.kafka.clients.producer.{ Callback, ProducerRecord, RecordMetadata }
import org.apache.kafka.common.{ Metric, MetricName, PartitionInfo, TopicPartition, Uuid }
import org.apache.kafka.clients.producer.{ Producer => KafkaProducer }
import org.apache.kafka.common.metrics.KafkaMetric
import zio._

import java.time.Duration
import java.util.concurrent.atomic.AtomicInteger
import java.util.{ List => JList, Map => JMap }
import java.util.concurrent.{ CompletableFuture, Future => JFuture }
import scala.collection.mutable

/**
 * A test framework for unit testing users of the async `send(ProducerRecord, Callback)` method of the Kafka producer,
 * featuring precise control over when the callback is called.
 *
 * This was build because the MockProducer from the Kafka library does not provide callback control.
 *
 * Use it as follows:
 * {{{
 * val sendException = new RuntimeException("fail from send")
 * val callbackException = new RuntimeException("fail from callback")
 * val mockBehavior = AsyncProducerTestSupport.newMockBehavior[Array[Byte], Array[Byte]]()
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
 * When all mock behavior have been given, you can use the `run` method and use the given mock producer as if it is a
 * regular Kafka producer.
 *
 * {{{
 * mockBehavior.run { mockProducer =>
 *   // use the producer, make assertions, etc.
 * }
 * }}}
 *
 * The 'send' invocations are expected in the order they are given. When 'publisher.send' is invoked while a callback
 * mock behavior is pending, the 'send' operation is blocked until the callback is done.
 *
 * Get a history of all send records (all 'send' attempts) with: `mockBehavior.history()`.
 *
 * Current limitations:
 *   - the given mock producer is not thread-safe; only one threads can use it at a time
 *   - the future that is returned from the `send` method never completes
 *   - the metadata that is returned is always the same, it contains no information
 */
object AsyncProducerTestSupport {

  // Developer notes:
  // `n` is used as the callback index (and therefore the index of `sendSuccess` mock behavior)
  //
  // While the behavior methods are invoked, and with a final check in `run`, we check that every callback behavior
  // (either a `callbackSucceed` or a `callbackException`) is coupled to exactly 1 `sendSuccess` behavior. Too
  // little, or additional callbacks result in an exception.
  //
  // Method `run` works with 2 fibers that run concurrently:
  //  - a coordinator fiber, that loops over the behaviors, awaiting `send`s and invoking callbacks
  //  - the test code that can use `mockProducer.send`
  // The two coordinate via several promises:
  //  - the test code awaits a start-promise that tells the `send` may proceed
  //  - the coordinator awaits the callback-promise that tells that send was invoked, and to get the callback
  //    that can later be invoked

  trait AsyncProducerTestSupportBehavior[K, V] {
    def sendSucceed(): AsyncProducerTestSupportBehavior[K, V]
    def sendFail(e: Throwable): AsyncProducerTestSupportBehavior[K, V]
    def callbackSucceed(): AsyncProducerTestSupportBehavior[K, V]
    def callbackSucceed(n: Int): AsyncProducerTestSupportBehavior[K, V]
    def callbackFail(e: Exception): AsyncProducerTestSupportBehavior[K, V]
    def callbackFail(n: Int, e: Exception): AsyncProducerTestSupportBehavior[K, V]
    def run[R, A](testCode: KafkaProducer[K, V] => ZIO[R, Throwable, A]): ZIO[R, Throwable, A]
    def history: Chunk[ProducerRecord[K, V]]
  }

  private sealed trait MockBehavior
  private final case class SendSucceed(n: Int)    extends MockBehavior
  private final case class SendFail(e: Throwable) extends MockBehavior {
    override def toString: String = s"SendFail(${e.getClass.getSimpleName})"
  }
  private final case class CallbackSucceed(n: Int)            extends MockBehavior
  private final case class CallbackFail(n: Int, e: Exception) extends MockBehavior {
    override def toString: String = s"CallbackFail($n, ${e.getClass.getSimpleName})"
  }

  def newMockBehavior[K, V](): AsyncProducerTestSupportBehavior[K, V] = new AsyncProducerTestSupportBehavior[K, V] {
    private val behaviorBuilder: ChunkBuilder[MockBehavior]  = Chunk.newBuilder
    private val callbacksAvailable: mutable.Set[Int]         = mutable.BitSet.empty
    private var callbackCount: Int                           = 0
    private val _history: ChunkBuilder[ProducerRecord[K, V]] = Chunk.newBuilder

    override def sendSucceed(): AsyncProducerTestSupportBehavior[K, V] = {
      behaviorBuilder += SendSucceed(callbackCount)
      callbacksAvailable += callbackCount
      callbackCount += 1
      this
    }
    override def sendFail(e: Throwable): AsyncProducerTestSupportBehavior[K, V] = {
      behaviorBuilder += SendFail(e)
      this
    }
    private def addCallback(n: Option[Int], op: Int => MockBehavior): AsyncProducerTestSupportBehavior[K, V] = {
      n match {
        case Some(n) =>
          if (callbacksAvailable.contains(n)) {
            callbacksAvailable -= n
            behaviorBuilder += op(n)
          } else {
            throw new AssertionError(
              s"Callback mock behavior for send $n can not be added because that send expectation does not exist, or it already has a callback"
            )
          }
        case None =>
          throw new AssertionError(
            s"Callback mock behavior can not be added because all send expectations already have a callback"
          )
      }
      this
    }
    override def callbackSucceed(): AsyncProducerTestSupportBehavior[K, V] =
      addCallback(callbacksAvailable.minOption, CallbackSucceed(_))
    override def callbackSucceed(n: Int): AsyncProducerTestSupportBehavior[K, V] =
      addCallback(Some(n), CallbackSucceed(_))
    override def callbackFail(e: Exception): AsyncProducerTestSupportBehavior[K, V] =
      addCallback(callbacksAvailable.minOption, CallbackFail(_, e))
    override def callbackFail(n: Int, e: Exception): AsyncProducerTestSupportBehavior[K, V] =
      addCallback(Some(n), CallbackFail(_, e))

    override def run[R, A](testCode: KafkaProducer[K, V] => ZIO[R, Throwable, A]): ZIO[R, Throwable, A] = {
      if (callbacksAvailable.nonEmpty) {
        throw new AssertionError(s"Missing ${callbacksAvailable.size} callback mock behaviors")
      }

      final case class SendExpectation(
        mockBehavior: MockBehavior, // For scala 3 change to: `SendSucceed | SendFail`
        startPromise: Promise[Nothing, Unit],
        callbackPromise: Promise[Nothing, Callback]
      )

      def fromOptionOrDie[A1](value: => Option[A1]): ZIO[Any, Nothing, A1] =
        ZIO
          .fromOption(value)
          .orDieWith(_ => new AssertionError("Bug in AsyncProducerTestSupport"))

      def callbackPromiseForN(sendExpectations: Seq[SendExpectation], n: Int): Option[Promise[Nothing, Callback]] =
        sendExpectations.collectFirst {
          case SendExpectation(SendSucceed(n1), _, callbackPromise) if n1 == n => callbackPromise
        }

      val behaviors = behaviorBuilder.result()
      for {
        sendExpectations <- ZIO.collect(behaviors) {
                              case behavior @ (SendSucceed(_) | SendFail(_)) =>
                                for {
                                  startPromise    <- Promise.make[Nothing, Unit]
                                  callbackPromise <- Promise.make[Nothing, Callback]
                                } yield SendExpectation(behavior, startPromise, callbackPromise)
                              case _ => ZIO.fail(None)
                            }
        runtime <- ZIO.runtime[Any]
        mockProducer = new NotSupportedProducer[K, V] {
                         private val currentSendIndex = new AtomicInteger(0)
                         override def send(
                           record: ProducerRecord[K, V],
                           callback: Callback
                         ): JFuture[RecordMetadata] = {
                           _history += record
                           val sendIndex = currentSendIndex.getAndIncrement()
                           if (sendIndex >= sendExpectations.size)
                             throw new AssertionError(s"No mock behavior defined for send $sendIndex")
                           val sendExpectation = sendExpectations(sendIndex)
                           Unsafe.unsafe { implicit u =>
                             runtime.unsafe.run {
                               for {
                                 _ <- sendExpectation.startPromise.await
                                 _ <- sendExpectation.callbackPromise.succeed(callback)
                               } yield ()
                             }
                               .getOrThrowFiberFailure()
                           }
                           (sendExpectation.mockBehavior: @unchecked) match {
                             // return a dummy future, it is never completed
                             case _: SendSucceed => new CompletableFuture[RecordMetadata]()
                             case SendFail(e)    => throw e
                           }
                         }
                       }
        sei             = sendExpectations.iterator
        handleBehaviors =
          ZIO.foreach(behaviors) {
            case mb @ (SendSucceed(_) | SendFail(_)) =>
              for {
                sendOperation <- fromOptionOrDie(sei.nextOption())
                _             <- sendOperation.startPromise.succeed(())
                _             <- ZIO
                       .raceFirst(
                         sendOperation.callbackPromise.await,
                         Seq(ZIO.logInfo(s"Still expecting mock behavior $mb").delay(3.seconds).forever)
                       )
                       .timeoutFail(new AssertionError(s"Timed out waiting for mock behavior $mb"))(1.minute)
              } yield ()
            case CallbackSucceed(n) =>
              for {
                callbackPromise <- fromOptionOrDie(callbackPromiseForN(sendExpectations, n))
                callback        <- callbackPromise.await
                // return dummy metadata
                metadata = new RecordMetadata(new TopicPartition("", 0), 0, 0, 0, 0, 0)
                _ <- ZIO.attempt(callback.onCompletion(metadata, null))
              } yield ()
            case CallbackFail(n, e) =>
              for {
                callbackPromise <- fromOptionOrDie(callbackPromiseForN(sendExpectations, n))
                callback        <- callbackPromise.await
                _               <- ZIO.attempt(callback.onCompletion(null, e))
              } yield ()
          }
        result <- handleBehaviors &> testCode(mockProducer)
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

  override def registerMetricForSubscription(metric: KafkaMetric): Unit = throw new UnsupportedOperationException()

  override def unregisterMetricFromSubscription(metric: KafkaMetric): Unit = throw new UnsupportedOperationException()
}
