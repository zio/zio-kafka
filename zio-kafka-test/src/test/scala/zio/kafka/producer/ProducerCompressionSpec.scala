package zio.kafka.producer

import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.record.internal.CompressionType
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
      },
      test("compression names and defaults correspond to Kafka library") {
        // The class `CompressionType` is in different packages for different Kafka releases.
        // Therefore, we must not refer to it from zio-kafka code. However, since we can
        // refer to it from test code, we can validate the names and defaults as set by the
        // Kafka library.
        assertTrue(
          compressionName(ProducerCompression.NoCompression) == CompressionType.NONE.name,
          compressionName(ProducerCompression.Gzip()) == CompressionType.GZIP.name,
          compressionName(ProducerCompression.Snappy()) == CompressionType.SNAPPY.name,
          compressionName(ProducerCompression.Lz4()) == CompressionType.LZ4.name,
          compressionName(ProducerCompression.Zstd()) == CompressionType.ZSTD.name,
          assertCompressionLevel(
            ProducerCompression.Gzip(),
            ProducerConfig.COMPRESSION_GZIP_LEVEL_CONFIG,
            CompressionType.GZIP.defaultLevel()
          ),
          assertCompressionLevel(
            ProducerCompression.Lz4(),
            ProducerConfig.COMPRESSION_LZ4_LEVEL_CONFIG,
            CompressionType.LZ4.defaultLevel()
          ),
          assertCompressionLevel(
            ProducerCompression.Zstd(),
            ProducerConfig.COMPRESSION_ZSTD_LEVEL_CONFIG,
            CompressionType.ZSTD.defaultLevel()
          )
        )
      }
    )

  private def compressionName(compression: ProducerCompression): String =
    compression.properties(ProducerConfig.COMPRESSION_TYPE_CONFIG).toString

  private def assertCompressionLevel(
    compression: ProducerCompression,
    propertyName: String,
    expectedLevel: Int
  ): Boolean =
    compression.properties.contains(propertyName) && {
      val value = compression.properties(propertyName)
      value.isInstanceOf[java.lang.Integer] && (Int.unbox(value) == expectedLevel)
    }
}
