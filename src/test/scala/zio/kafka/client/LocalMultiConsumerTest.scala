package zio.kafka.client

import zio.kafka.client.MultiConsumerTestHelper.test1
import zio.test.{DefaultRunnableSpec, suite}

object LocalMultiConsumerTest
  extends DefaultRunnableSpec(
    suite("consumer test suite3 - parallel consumers")(
      List(test1) :_*
    ).provideManagedShared(KafkaTestUtils.localKafkaEnvironment)
  )
