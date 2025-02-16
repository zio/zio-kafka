package zio.kafka.producer

import org.apache.kafka.common.record.CompressionType
import zio._
import zio.test._

object ProducerCompressionSpec extends ZIOSpecDefault {

  override def spec: Spec[TestEnvironment with Scope, Any] =
    suite("ProducerCompression")(
      test("all Kafka supported compression codes have a corresponding ProducerCompression") {
        val compressions = Seq(
          ProducerCompression.NoCompression,
          ProducerCompression.Gzip(),
          ProducerCompression.Snappy(),
          ProducerCompression.Lz4(),
          ProducerCompression.Zstd()
        )
        val availableCompressionsCount = CompressionType.values().length
        assertTrue(availableCompressionsCount == compressions.size)
      }
    )
}
