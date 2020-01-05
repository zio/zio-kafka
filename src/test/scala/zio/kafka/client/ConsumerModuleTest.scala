package zio.kafka.client

import zio.test.{ assertM, suite, testM }
import zio.test.DefaultRunnableSpec
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

  lazy val cannedAssignments: Set[TopicPartition] = Set(
    new TopicPartition("foobar", 1),
    new TopicPartition("foobar", 3)
  )
}
import ConsumerModuleTestUtils._

object ConsumerModuleTest
    extends DefaultRunnableSpec(
      suite("Consumer module")(
        suite("delegates")(
          testM("assignment") {
            val eff  = Consumer.assignment
            val mock = ConsumerMock.assignment returns value(cannedAssignments)
            val env  = makeEnv(mock)

            val result = eff.provideManaged(env)

            assertM(result, equalTo(cannedAssignments))
          }
        )
      )
    )
