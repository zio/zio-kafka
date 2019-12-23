package zio.kafka.client

import org.apache.kafka.clients.consumer.ConsumerConfig
import zio.duration._
import zio.kafka.client.Consumer.OffsetRetrieval

case class ConsumerSettings(
  bootstrapServers: List[String],
  groupId: String,
  clientId: String,
  closeTimeout: Duration,
  extraDriverSettings: Map[String, AnyRef],
  pollInterval: Duration,
  pollTimeout: Duration,
  perPartitionChunkPrefetch: Int,
  offsetRetrieval: OffsetRetrieval = OffsetRetrieval.Auto()
) {
  def autoOffsetResetConfig = offsetRetrieval match {
    case OffsetRetrieval.Auto(reset) => Map(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG -> reset.toConfig)
    case OffsetRetrieval.Manual(_)   => Map.empty
  }

  def driverSettings: Map[String, AnyRef] =
    Map(
      ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG  -> bootstrapServers.mkString(","),
      ConsumerConfig.GROUP_ID_CONFIG           -> groupId,
      ConsumerConfig.CLIENT_ID_CONFIG          -> clientId,
      ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG -> "false"
    ) ++ autoOffsetResetConfig ++ extraDriverSettings

}
