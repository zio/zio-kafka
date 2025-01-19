package zio.kafka.producer

import org.apache.kafka.clients.producer.{ Producer => KafkaProducer, ProducerRecord, RecordMetadata }
import org.apache.kafka.common.errors.AuthenticationException
import zio._
import zio.test.TestAspect.withLiveClock
import zio.test._

object ProducerSpec extends ZIOSpecDefault {
  private val recordsToSend = Chunk(
    makeProducerRecord(),
    makeProducerRecord(),
    makeProducerRecord(),
    makeProducerRecord(),
    makeProducerRecord()
  )
  private val testAuthenticationExceptionMessage = "test authentication exception"
  private val authException                      = new AuthenticationException(testAuthenticationExceptionMessage)

  override def spec: Spec[TestEnvironment with Scope, Any] =
    suite("Producer")(
      suite("produceChunkAsyncWithFailures")(
        test("successfully produces chunk of records") {
          val mockBehavior = AsyncProducerTestSupport
            .newMockBehavior[Array[Byte], Array[Byte]]()
            .sendSucceed()
            .sendSucceed()
            .sendSucceed()
            .sendSucceed()
            .sendSucceed()
            .callbackSucceed()
            .callbackSucceed()
            .callbackSucceed()
            .callbackSucceed()
            .callbackSucceed()
          for {
            results <- runTest(mockBehavior, recordsToSend)
          } yield {
            val history = mockBehavior.history
            assertTrue(
              results.length == recordsToSend.length,
              results.forall(_.isRight),
              history.size == 5
            )
          }
        },
        test("successfully produces chunk of records, with callbacks in an arbitrary order") {
          val mockBehavior = AsyncProducerTestSupport
            .newMockBehavior[Array[Byte], Array[Byte]]()
            .sendSucceed()
            .sendSucceed()
            .callbackSucceed(1)
            .sendSucceed()
            .callbackSucceed(0)
            .sendSucceed()
            .callbackSucceed(2)
            .sendSucceed()
            .callbackSucceed(4)
            .callbackSucceed(3)
          for {
            results <- runTest(mockBehavior, recordsToSend)
          } yield {
            val history = mockBehavior.history
            assertTrue(
              results.length == recordsToSend.length,
              results.forall(_.isRight),
              history.size == 5
            )
          }
        },
        test("omits sending further records in chunk when send call fails") {
          val mockBehavior = AsyncProducerTestSupport
            .newMockBehavior[Array[Byte], Array[Byte]]()
            .sendSucceed()
            .sendFail(authException)
            .callbackSucceed()
          for {
            results <- runTest(mockBehavior, recordsToSend)
          } yield {
            val history = mockBehavior.history
            assertTrue(
              results.length == recordsToSend.length,
              results.head.isRight,
              results(1).left.forall(_.getMessage == testAuthenticationExceptionMessage),
              results.drop(2).forall(_ == Left(Producer.PublishOmittedException)),
              history.size == 2
            )
          }
        },
        test("retries send after an AuthenticationException from send") {
          val mockBehavior = AsyncProducerTestSupport
            .newMockBehavior[Array[Byte], Array[Byte]]()
            .sendSucceed()
            .sendFail(authException) // send fails immediately
            .callbackSucceed()       // send 0 ok
            // Sending record 1 failed with AuthError, sending records 2, 3, and 4 was skipped
            // All 4 are retried:
            .sendSucceed()
            .sendSucceed()
            .sendSucceed()
            .sendSucceed()
            .callbackSucceed()
            .callbackSucceed()
            .callbackSucceed()
            .callbackSucceed()
          for {
            results <- runTest(mockBehavior, recordsToSend, Schedule.recurs(1))
          } yield {
            val history = mockBehavior.history
            assertTrue(
              results.length == recordsToSend.length,
              results.forall(_.isRight),
              history.size == 6
            )
          }
        },
        test("retries send after an AuthenticationException from callback") {
          val mockBehavior = AsyncProducerTestSupport
            .newMockBehavior[Array[Byte], Array[Byte]]()
            .sendSucceed()
            .sendSucceed()
            .sendSucceed()
            .sendSucceed()
            .sendSucceed()
            .callbackSucceed()
            .callbackFail(authException)
            .callbackSucceed()
            .callbackFail(authException)
            .callbackSucceed()
            // Sending record 0, 2 and 4 -> okay
            // Sending record 1 and 3 failed with AuthError
            // Only 1 and 3 are retried:
            .sendSucceed()
            .sendSucceed()
            .callbackSucceed()
            .callbackSucceed()
          for {
            results <- runTest(mockBehavior, recordsToSend, Schedule.recurs(1))
          } yield {
            val history = mockBehavior.history
            assertTrue(
              results.length == recordsToSend.length,
              results.forall(_.isRight),
              history.size == 7
            )
          }
        },
        test("does not retry send after another Exception from send") {
          val mockBehavior = AsyncProducerTestSupport
            .newMockBehavior[Array[Byte], Array[Byte]]()
            .sendSucceed()
            .sendFail(new RuntimeException())
            .callbackSucceed()
          for {
            results <- runTest(mockBehavior, recordsToSend, Schedule.recurs(1))
          } yield {
            val history = mockBehavior.history
            assertTrue(
              results.length == recordsToSend.length,
              results.head.isRight,
              results.tail.forall(_.isLeft),
              history.size == 2
            )
          }
        },
        test("does not retry send after another Exception from callback, even when there is also an AuthException") {
          val mockBehavior = AsyncProducerTestSupport
            .newMockBehavior[Array[Byte], Array[Byte]]()
            .sendSucceed()
            .sendSucceed()
            .sendSucceed()
            .sendSucceed()
            .sendSucceed()
            .callbackSucceed()
            .callbackFail(authException)
            .callbackSucceed()
            .callbackFail(new RuntimeException())
            .callbackSucceed()
          for {
            results <- runTest(mockBehavior, recordsToSend, Schedule.recurs(1))
          } yield {
            val history = mockBehavior.history
            assertTrue(
              results.length == recordsToSend.length,
              results(0).isRight,
              results(1).isLeft,
              results(2).isRight,
              results(3).isLeft,
              results(4).isRight,
              history.size == 5
            )
          }
        },
        test("does multiple send retries after an AuthenticationException") {
          val mockBehavior = AsyncProducerTestSupport
            .newMockBehavior[Array[Byte], Array[Byte]]()
            .sendSucceed()
            .sendSucceed()
            .sendSucceed()
            .sendSucceed()
            .sendSucceed()
            .callbackSucceed()
            .callbackFail(authException)
            .callbackSucceed()
            .callbackFail(authException)
            .callbackSucceed()
            // Sending record 0, 2 and 4 -> okay
            // Sending record 1 and 3 failed with AuthError
            // Only 1 and 3 are retried:
            .sendSucceed()
            .sendSucceed()
            .callbackFail(authException)
            .callbackFail(authException)
            // Retried again
            .sendSucceed()
            .sendSucceed()
            .callbackFail(authException)
            .callbackSucceed()
            // Retried again
            .sendSucceed()
            .callbackSucceed()
          for {
            results <- runTest(mockBehavior, recordsToSend, Schedule.forever)
          } yield {
            val history = mockBehavior.history
            assertTrue(
              results.length == recordsToSend.length,
              results.forall(_.isRight),
              history.size == 10
            )
          }
        }
      )
    ) @@ withLiveClock

  private def withProducer[A](
    mockJavaProducer: KafkaProducer[Array[Byte], Array[Byte]],
    authErrorRetrySchedule: Schedule[Any, Throwable, Any]
  )(
    producerTest: Producer => ZIO[Scope, Throwable, A]
  ): ZIO[Any, Throwable, A] =
    ZIO.scoped {
      val producerSettings = ProducerSettings()
        .withAuthErrorRetrySchedule(authErrorRetrySchedule)

      Producer
        .fromJavaProducer(mockJavaProducer, producerSettings)
        .flatMap(producerTest(_))
    }

  private def runTest(
    mockBehavior: AsyncProducerTestSupport.AsyncProducerTestSupportBehavior[Array[Byte], Array[Byte]],
    recordsToSend: Chunk[ProducerRecord[Array[Byte], Array[Byte]]],
    authErrorRetrySchedule: Schedule[Any, Throwable, Any] = Schedule.stop
  ): ZIO[Any, Throwable, Chunk[Either[Throwable, RecordMetadata]]] =
    mockBehavior.run { mockProducer =>
      withProducer(mockProducer, authErrorRetrySchedule) { producer =>
        producer.produceChunkAsyncWithFailures(recordsToSend).flatten
      }
    }

  private def makeProducerRecord(
    topic: String = "testTopic",
    key: String = "key",
    value: String = "value"
  ): ProducerRecord[Array[Byte], Array[Byte]] =
    new ProducerRecord[Array[Byte], Array[Byte]](topic, key.getBytes, value.getBytes)

}
