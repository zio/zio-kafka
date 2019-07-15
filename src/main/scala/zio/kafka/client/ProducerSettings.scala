package zio.kafka.client

import org.apache.kafka.clients.producer.ProducerConfig

import zio.duration.Duration

case class ProducerSettings(
  bootstrapServers: List[String],
  closeTimeout: Duration,
  extraDriverSettings: Map[String, String]
) {
  def driverSettings: Map[String, AnyRef] =
    Map(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG -> bootstrapServers.mkString(",")) ++
      extraDriverSettings
}
