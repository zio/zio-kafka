package zio.kafka.client

import zio.test.{ suite, DefaultRunnableSpec }
import MultiConsumerTestHelper._

object EmbeddedMultiConsumerTest
    extends DefaultRunnableSpec(
      suite("consumer test suite3 - multiple consumers")(
        List(testMultipleConsumers, testSingleConsumerManyRecords): _*
      ).provideManagedShared(KafkaTestUtils.embeddedKafkaEnvironment)
    )
