package zio.kafka.consumer.internal

import org.apache.kafka.clients.consumer.ConsumerConfig
import zio._
import zio.kafka.consumer.ConsumerSettings

import java.util.{ Map => JavaMap }

/**
 * Configuration for the Runloop.
 *
 * See ConsumerSettings for a description of each config.
 */
private[internal] final case class RunloopConfig(
  maxPollRecords: Int,
  maxPollInterval: Duration,
  maxStreamPullInterval: Duration,
  maxRebalanceDuration: Duration
)

private[internal] object RunloopConfig {
  def apply(settings: ConsumerSettings): Task[RunloopConfig] = ZIO.attempt {
    // Give an Int config value as overridden by the user, or else None.
    // Note: it is safe to ignore invalid values, they will lead to an exception
    // in the java Kafka consumer anyway.
    def overriddenConfigInt(configName: String): Option[Int] =
      settings.properties
        .get(configName)
        .flatMap(_.toString.toIntOption) // Ignore invalid

    // Some Kafka configs that are needed by the runloop have a default in Kafka and can be overridden via
    // [[ConsumerSettings]]. Unfortunately the Kafka consumer does not expose its actual config. Therefore, we mimic
    // what Kafka does: get the config value from the consumer properties and if it is not present, use the default
    // value.
    lazy val defaultConfigs: JavaMap[String, AnyRef] = ConsumerConfig.configDef().defaultValues()
    def defaultConfigInt(configName: String): Int    = defaultConfigs.get(configName).asInstanceOf[Integer]

    val maxPollInterval = {
      val configName = ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG
      overriddenConfigInt(configName).getOrElse(defaultConfigInt(configName)).millis
    }

    val maxPollRecords = {
      val configName = ConsumerConfig.MAX_POLL_RECORDS_CONFIG
      overriddenConfigInt(configName).getOrElse(defaultConfigInt(configName))
    }

    val maxStreamPullInterval = settings.maxStreamPullIntervalOption.getOrElse(maxPollInterval)
    // See scaladoc of [[ConsumerSettings.withMaxRebalanceDuration]]:
    val maxRebalanceDuration = settings.maxRebalanceDuration.getOrElse(((maxPollInterval.toNanos / 5L) * 3L).nanos)

    RunloopConfig(maxPollRecords, maxPollInterval, maxStreamPullInterval, maxRebalanceDuration)
  }
}
