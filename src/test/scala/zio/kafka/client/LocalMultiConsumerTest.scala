package zio.kafka.client

import zio.test.{ suite, DefaultRunnableSpec }

object LocalMultiConsumerTest
    extends DefaultRunnableSpec(
      suite("consumer test suite3 - parallel consumers")(
        ).provideManagedShared(KafkaTestUtils.localKafkaEnvironment)
    )
