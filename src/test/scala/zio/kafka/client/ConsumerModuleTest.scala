package zio.kafka.client

import zio.test.{ assertM, checkM, suite, testM, DefaultRunnableSpec, Gen }
import zio.test.mock.Expectation.value
import zio.Managed
import zio.test.environment.TestEnvironment
import zio.blocking.Blocking
import org.apache.kafka.common.TopicPartition
import zio.test.Assertion.equalTo

object ConsumerModuleTestUtils {
  def makeEnv(managed: Managed[Nothing, Consumer]): Managed[Nothing, Consumer with Blocking] =
    for {
      testEnv <- TestEnvironment.Value
      c       <- managed
    } yield new Consumer with Blocking {
      val blocking = testEnv.blocking
      val consumer = c.consumer
    }

  lazy val assignmentsGen = Gen.sized { s =>
    val inner = Gen.alphaNumericString <*> Gen.int(0, 100) map {
      case (t, p) => new TopicPartition(t, p)
    }
    Gen.vectorOfN(s)(inner) map { _.toSet }
  }
}
import ConsumerModuleTestUtils._

object ConsumerModuleTest
    extends DefaultRunnableSpec(
      suite("Consumer module")(
        suite("delegates")(
          testM("assignment")(
            checkM(assignmentsGen) { a =>
              val eff  = Consumer.assignment
              val mock = ConsumerMock.assignment returns value(a)
              val env  = makeEnv(mock)

              val result = eff.provideManaged(env)

              assertM(result, equalTo(a))
            }
          )
        )
      )
    )
