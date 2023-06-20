package zio.kafka.testkit

import zio.ZLayer
import zio.test._

/**
 * This trait should be used if you want to run your tests with a shared-across-your-suites embedded Kafka cluster.
 *
 * More information about sharing a resource between multiple suites can be found here:
 * https://zio.dev/reference/test/sharing-layers-between-multiple-files/
 */
trait ZIOSpecWithKafka extends ZIOSpec[TestEnvironment with Kafka] {

  override val bootstrap: ZLayer[Any, Any, TestEnvironment with Kafka] =
    testEnvironment ++ Kafka.embedded

}
