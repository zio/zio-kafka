package zio.kafka.client

import zio.test.{DefaultRunnableSpec, suite}
import MultiConsumerTestHelper._

object EmbeddedMultiConsumerTest
  extends DefaultRunnableSpec(
    suite("consumer test suite3 - parallel consumers")(
      List(test1) :_*
    ).provideManagedShared(KafkaTestUtils.embeddedKafkaEnvironment)
)


