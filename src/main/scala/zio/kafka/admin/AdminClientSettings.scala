package zio.kafka.admin

import org.apache.kafka.clients.admin.AdminClientConfig
import zio.duration._

case class AdminClientSettings(
  bootstrapServers: List[String],
  closeTimeout: Duration,
  properties: Map[String, AnyRef]
) {
  def driverSettings: Map[String, AnyRef] =
    Map(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG -> bootstrapServers.mkString(",")) ++
      properties

  def withProperty(key: String, value: AnyRef): AdminClientSettings =
    copy(properties = properties + (key -> value))

  def withProperties(kvs: (String, AnyRef)*): AdminClientSettings =
    withProperties(kvs.toMap)

  def withProperties(kvs: Map[String, AnyRef]): AdminClientSettings =
    copy(properties = properties ++ kvs)
}

object AdminClientSettings {
  def apply(bootstrapServers: List[String]): AdminClientSettings =
    AdminClientSettings(bootstrapServers, 30.seconds, Map())
}
