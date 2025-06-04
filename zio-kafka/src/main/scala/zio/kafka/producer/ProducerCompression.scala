package zio.kafka.producer

import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.record.CompressionType
//import zio.stacktracer.TracingImplicits.disableAutoTrace

abstract sealed class ProducerCompression(name: String, extra: Option[(String, AnyRef)] = None) {
  def properties: Map[String, AnyRef] =
    Map(ProducerConfig.COMPRESSION_TYPE_CONFIG -> name) ++ extra
}

/**
 * The compression codecs that Kafka supports while producing records.
 */
object ProducerCompression {

  /** Produce kafka records without compression. */
  case object NoCompression extends ProducerCompression(CompressionType.NONE.name)

  /**
   * Produce kafka records with GZIP compression.
   * @param level
   *   a value between 1 and 9 or -1 (defaults to -1)
   */
  final case class Gzip(level: Int = CompressionType.GZIP.defaultLevel())
      extends ProducerCompression(
        CompressionType.GZIP.name,
        Some(ProducerConfig.COMPRESSION_GZIP_LEVEL_CONFIG -> Int.box(level))
      )

  /** Produce kafka records with Snappy compression. */
  final case class Snappy() extends ProducerCompression(CompressionType.SNAPPY.name)

  /**
   * Produce kafka records with Lz4 compression.
   * @param level
   *   a value between 1 and 17 (defaults to 9)
   */
  final case class Lz4(level: Int = CompressionType.LZ4.defaultLevel())
      extends ProducerCompression(
        CompressionType.LZ4.name,
        Some(ProducerConfig.COMPRESSION_LZ4_LEVEL_CONFIG -> Int.box(level))
      )

  /**
   * Produce kafka records with Zstd compression.
   *
   * @param level
   *   a value between -131072 and 22 (defaults to 3)
   */
  final case class Zstd(level: Int = CompressionType.ZSTD.defaultLevel())
      extends ProducerCompression(
        CompressionType.ZSTD.name,
        Some(ProducerConfig.COMPRESSION_ZSTD_LEVEL_CONFIG -> Int.box(level))
      )
}
