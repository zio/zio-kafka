package zio.kafka.testkit

import zio._

import java.util.UUID

/**
 * Generates unique names for topics, consumer groups and clients, so that tests that share a Kafka broker do not
 * interfere with each other. Every name is a prefix, a dash and a random `UUID`.
 *
 * {{{
 * import zio._
 * import zio.kafka.testkit.KafkaRandom
 *
 * object MyTestSupport extends KafkaRandom {
 *   override def kafkaPrefix: String = "my-spec"
 * }
 *
 * val topic: Task[String] = MyTestSupport.randomTopic // "my-spec-topic-" followed by a random UUID
 * }}}
 */
trait KafkaRandom {

  /** The start of every name that this trait generates for a topic, group or client. */
  def kafkaPrefix: String

  /** Returns `prefix`, a dash and a random `UUID`. */
  def randomThing(prefix: String): Task[String] = ZIO.attempt(s"$prefix-${UUID.randomUUID()}")

  /** Returns a random topic name that starts with `kafkaPrefix` followed by `-topic`. */
  def randomTopic: Task[String] = randomThing(s"$kafkaPrefix-topic")

  /** Returns a random consumer group id that starts with `kafkaPrefix` followed by `-group`. */
  def randomGroup: Task[String] = randomThing(s"$kafkaPrefix-group")

  /** Returns a random client id that starts with `kafkaPrefix` followed by `-client`. */
  def randomClient: Task[String] = randomThing(s"$kafkaPrefix-client")
}
