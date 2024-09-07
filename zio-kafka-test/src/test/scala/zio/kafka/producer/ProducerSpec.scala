package zio.kafka.producer

import org.apache.kafka.clients.producer
import org.apache.kafka.clients.producer.{ MockProducer, ProducerRecord, RecordMetadata }
import org.apache.kafka.common.KafkaException
import org.apache.kafka.common.errors.AuthenticationException
import org.apache.kafka.common.serialization.ByteArraySerializer
import zio._
import zio.test.TestAspect.withLiveClock
import zio.test._

import java.util.concurrent.Future
import java.util.concurrent.atomic.AtomicBoolean

object ProducerSpec extends ZIOSpecDefault {

  private object TestKeyValueSerializer extends ByteArraySerializer

  private class BinaryMockProducer(autoComplete: Boolean)
      extends MockProducer[Array[Byte], Array[Byte]](
        autoComplete,
        TestKeyValueSerializer,
        TestKeyValueSerializer
      ) {

    private val nextSendAllowed = new AtomicBoolean(autoComplete)

    override def send(
      record: ProducerRecord[Array[Byte], Array[Byte]],
      callback: producer.Callback
    ): Future[RecordMetadata] = {
      awaitSendAllowed()
      val sendResult = super.send(record, callback)
      nextSendAllowed.set(autoComplete)

      sendResult
    }

    def allowNextSendAndAwaitSendCompletion(): Unit = {
      allowNextSend()
      awaitSendCompletion()
    }

    def allowNextSend(): Unit =
      nextSendAllowed.set(true)

    def awaitSendAllowed(): Unit =
      awaitSendCondition(true)

    def awaitSendCompletion(): Unit =
      awaitSendCondition(false)

    private def awaitSendCondition(expectedCondition: Boolean): Unit = {
      var awaitingSendCondition = true
      while (awaitingSendCondition)
        awaitingSendCondition = expectedCondition != nextSendAllowed.get()
    }

  }

  override def spec: Spec[TestEnvironment with Scope, Any] =
    suite("Producer")(
      suite("produceChunkAsyncWithFailures")(
        test("successfully produces chunk of records") {
          withProducer() { (_, producer) =>
            val recordsToSend = Chunk(
              makeProducerRecord(),
              makeProducerRecord(),
              makeProducerRecord(),
              makeProducerRecord(),
              makeProducerRecord()
            )
            for {
              results <- producer.produceChunkAsyncWithFailures(recordsToSend).flatten
            } yield assertTrue(
              results.length == recordsToSend.length,
              results.forall(_.isRight)
            )
          }
        },
        test("omits sending further records in chunk in case the first send call fails") {
          withProducer() { (mockJavaProducer, producer) =>
            val recordsToSend = Chunk(
              makeProducerRecord(),
              makeProducerRecord(),
              makeProducerRecord(),
              makeProducerRecord(),
              makeProducerRecord()
            )
            val testAuthenticationExceptionMessage = "test authentication exception"
            mockJavaProducer.sendException = new AuthenticationException(testAuthenticationExceptionMessage)
            for {
              results <- producer.produceChunkAsyncWithFailures(recordsToSend).flatten
            } yield assertTrue(
              results.length == recordsToSend.length,
              results.head.isLeft,
              results.head.left.forall(_.getMessage == testAuthenticationExceptionMessage),
              results.tail.forall(_ == Left(Producer.PublishOmittedException))
            )
          }
        },
        test("provides correct results in case last send call fails") {
          withProducer(autoCompleteProducerRequests = false) { (mockJavaProducer, producer) =>
            val recordsToSend = Chunk(
              makeProducerRecord(),
              makeProducerRecord(),
              makeProducerRecord(),
              makeProducerRecord(),
              makeProducerRecord()
            )
            val testAuthenticationExceptionMessage = "test authentication exception"
            val mockJavaProducerBehaviour = ZIO.succeed {
              // Send calls behaviours
              mockJavaProducer.allowNextSendAndAwaitSendCompletion()
              mockJavaProducer.allowNextSendAndAwaitSendCompletion()
              mockJavaProducer.allowNextSendAndAwaitSendCompletion()
              mockJavaProducer.allowNextSendAndAwaitSendCompletion()
              mockJavaProducer.sendException = new AuthenticationException(testAuthenticationExceptionMessage)
              mockJavaProducer.allowNextSend()
              // Send callbacks behaviours
              mockJavaProducer.completeNext()
              mockJavaProducer.completeNext()
              mockJavaProducer.completeNext()
              mockJavaProducer.completeNext()
            }
            for {
              _       <- mockJavaProducerBehaviour.forkScoped
              results <- producer.produceChunkAsyncWithFailures(recordsToSend).flatten
            } yield assertTrue(
              results.length == recordsToSend.length,
              results.init.forall(_.isRight),
              results.last.isLeft,
              results.last.left.forall(_.getMessage == testAuthenticationExceptionMessage)
            )
          }
        },
        test("omits sending further records in chunk and provides correct results in case middle send call fails") {
          withProducer(autoCompleteProducerRequests = false) { (mockJavaProducer, producer) =>
            val recordsToSend = Chunk(
              makeProducerRecord(),
              makeProducerRecord(),
              makeProducerRecord(),
              makeProducerRecord(),
              makeProducerRecord()
            )
            val testAuthenticationExceptionMessage = "test authentication exception"
            val mockJavaProducerBehaviour = ZIO.succeed {
              // Send calls behaviours
              mockJavaProducer.allowNextSendAndAwaitSendCompletion()
              mockJavaProducer.allowNextSendAndAwaitSendCompletion()
              mockJavaProducer.sendException = new AuthenticationException(testAuthenticationExceptionMessage)
              mockJavaProducer.allowNextSend()
              // Send callbacks behaviours
              mockJavaProducer.completeNext()
              mockJavaProducer.completeNext()
            }
            for {
              _       <- mockJavaProducerBehaviour.forkScoped
              results <- producer.produceChunkAsyncWithFailures(recordsToSend).flatten
            } yield assertTrue(
              results.length == recordsToSend.length,
              results(0).isRight,
              results(1).isRight,
              results(2).left.forall(_.getMessage == testAuthenticationExceptionMessage),
              results(3) == Left(Producer.PublishOmittedException),
              results(4) == Left(Producer.PublishOmittedException)
            )
          }
        },
        test(
          "omits sending further records in chunk and provides correct results in case second publication to broker fails along with middle send call failure"
        ) {
          withProducer(autoCompleteProducerRequests = false) { (mockJavaProducer, producer) =>
            val recordsToSend = Chunk(
              makeProducerRecord(),
              makeProducerRecord(),
              makeProducerRecord(),
              makeProducerRecord(),
              makeProducerRecord()
            )
            val testAuthenticationExceptionMessage = "test authentication exception"
            val testKafkaExceptionMessage          = "unexpected broker exception"
            val mockJavaProducerBehaviour = ZIO.succeed {
              // Send calls behaviours
              mockJavaProducer.allowNextSendAndAwaitSendCompletion()
              mockJavaProducer.allowNextSendAndAwaitSendCompletion()
              mockJavaProducer.sendException = new AuthenticationException(testAuthenticationExceptionMessage)
              mockJavaProducer.allowNextSend()
              // Send callbacks behaviours
              mockJavaProducer.completeNext()
              mockJavaProducer.errorNext(new KafkaException(testKafkaExceptionMessage))
            }
            for {
              _       <- mockJavaProducerBehaviour.forkScoped
              results <- producer.produceChunkAsyncWithFailures(recordsToSend).flatten
            } yield assertTrue(
              results.length == recordsToSend.length,
              results(0).isRight,
              results(1).isLeft,
              results(1).left.forall(_.getMessage == testKafkaExceptionMessage),
              results(2).left.forall(_.getMessage == testAuthenticationExceptionMessage),
              results(3) == Left(Producer.PublishOmittedException),
              results(4) == Left(Producer.PublishOmittedException)
            )
          }
        }
      )
    ) @@ withLiveClock

  private def withProducer(autoCompleteProducerRequests: Boolean = true)(
    producerTest: (BinaryMockProducer, Producer) => ZIO[Scope, Throwable, TestResult]
  ): ZIO[Scope, Throwable, TestResult] =
    ZIO.scoped {
      val mockJavaProducer = new BinaryMockProducer(autoCompleteProducerRequests)

      Producer
        .fromJavaProducer(mockJavaProducer, ProducerSettings())
        .flatMap(producerTest(mockJavaProducer, _))
    }

  private def makeProducerRecord(
    topic: String = "testTopic",
    key: String = "key",
    value: String = "value"
  ): ProducerRecord[Array[Byte], Array[Byte]] =
    new ProducerRecord[Array[Byte], Array[Byte]](topic, key.getBytes, value.getBytes)

}
