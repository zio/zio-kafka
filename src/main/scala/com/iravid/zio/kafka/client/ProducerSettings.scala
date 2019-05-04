package zio.kafka.client

import scalaz.zio.duration.Duration
import org.apache.kafka.clients.producer.ProducerConfig

case class ProducerSettings(
  bootstrapServers: List[String],
  closeTimeout: Duration,
  extraDriverSettings: Map[String, String]
) {
  def driverSettings: Map[String, String] =
    Map(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG -> bootstrapServers.mkString(",")) ++
      extraDriverSettings
}
