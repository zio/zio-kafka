package zio.kafka

import zio.kafka.embedded.Kafka
import zio.test._
import zio.ZLayer

@deprecated(
  message = """
      This trait is deprecated.

      The advised way to bring a Kafka instance in your tests is to use the zio-test `provideShared` or `provideSomeShared` methods on your suite:
      ```
        val mySuite =
          suite("my suite") {
            test("my test") {
              ...
            }
          }.provideShared(Kafka.embedded)
      ```

      Look at the tests in the `zio-kafka-test` project for examples.
    """,
  since = "2.3.0"
)
trait ZIOSpecWithKafka extends ZIOSpec[TestEnvironment with Kafka] {

  override val bootstrap: ZLayer[Any, Any, TestEnvironment with Kafka] =
    testEnvironment ++ Kafka.embedded

}
