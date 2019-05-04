package zio.kafka.client

import org.apache.kafka.clients.consumer.ConsumerConfig
import scalaz.zio.duration._

case class ConsumerSettings(
  bootstrapServers: List[String],
  groupId: String,
  clientId: String,
  closeTimeout: Duration,
  extraDriverSettings: Map[String, String]
) {
  def driverSettings: Map[String, String] =
    Map(
      ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG -> bootstrapServers.mkString(","),
      ConsumerConfig.GROUP_ID_CONFIG          -> groupId,
      ConsumerConfig.CLIENT_ID_CONFIG         -> clientId
    ) ++
      extraDriverSettings

}
