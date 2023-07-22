package zio.kafka

import zio.Chunk
import zio.logging.backend.SLF4J
import zio.test.{ TestAspect, TestAspectAtLeastR, TestEnvironment, ZIOSpecDefault }

/**
 * Use this class instead of `ZIOSpecDefault` if you want your tests to use SLF4J to log.
 *
 * Useful when you want to use logback to configure your logger, for example.
 */
abstract class ZIOSpecDefaultSlf4j extends ZIOSpecDefault {

  private val loggerLayer = SLF4J.slf4j

  override def aspects: Chunk[TestAspectAtLeastR[TestEnvironment]] =
    super.aspects :+ TestAspect.fromLayer(zio.Runtime.removeDefaultLoggers >>> loggerLayer)

}
