package zio.kafka

import zio.Chunk
import zio.logging.backend.SLF4J
import zio.test.{TestAspect, TestAspectAtLeastR, TestEnvironment, ZIOSpecAbstract, ZIOSpecDefault}

/**
 * Use this class instead of `ZIOSpecDefault` if you want your tests to use SLF4J to log.
 *
 * Useful when you want to use logback to configure your logger, for example.
 */
trait ZIOSpecDefaultSlf4j extends ZIOSpecDefault with ZIOSpecAbstractSlf4j

/**
 * Mix this into your ZIO Spec if you want your tests to use SLF4J to log.
 */
trait ZIOSpecAbstractSlf4j extends ZIOSpecAbstract {

  abstract override def aspects: Chunk[TestAspectAtLeastR[Environment with TestEnvironment]] =
    super.aspects :+ TestAspect.fromLayer(zio.Runtime.removeDefaultLoggers >>> SLF4J.slf4j)

}
