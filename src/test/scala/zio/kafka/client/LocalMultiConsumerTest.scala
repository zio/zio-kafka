package zio.kafka.client

import zio.test.{ suite, DefaultRunnableSpec }

/**
 * simple example of trivial change to consumer test to test against local running kafka. Left in because it might
 * be useful, empty because otherwise it would fail CI
 */
object LocalMultiConsumerTest
    extends DefaultRunnableSpec(
      suite("consumer test suite3 - parallel consumers local running kafka (empty to pass ci)")(
        ).provideManagedShared(KafkaTestUtils.localKafkaEnvironment)
    )
