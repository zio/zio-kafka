package zio.kafka.consumer

import zio.Task

import scala.util.matching.Regex
import java.util.regex.{ Pattern => JPattern }

import org.apache.kafka.common.TopicPartition

sealed trait Subscription
object Subscription {
  final case class Topics(topics: Set[String])                  extends Subscription
  final case class Pattern(pattern: Regex)                      extends Subscription
  final case class Manual(topicPartitions: Set[TopicPartition]) extends Subscription

  /**
   * Create a subscription for one or more topics
   *
   * @param name
   *   Topic to subscribe to
   * @param names
   *   Additional topics to subscribe to
   */
  def topics(name: String, names: String*): Subscription =
    Topics(Set(name) ++ names.toSet)

  /**
   * Create a subscription for all topics matching the given pattern
   *
   * @param pattern
   *   Pattern
   */
  def pattern(pattern: Regex): Subscription =
    Pattern(pattern)

  /**
   * Create a subscription for all topics matching the given pattern
   *
   * @param pattern
   *   Pattern to be converted to a Regex
   * @return
   *   The created subscription or failure when the pattern is invalid
   */
  def pattern(pattern: String): Task[Subscription] =
    Task.attempt(new Regex(pattern)).map(Pattern.apply)

  /**
   * Create a subscription for all topics matching the given pattern
   *
   * @param pattern
   *   Pattern
   */
  def pattern(pattern: JPattern): Subscription =
    Pattern(new Regex(pattern.pattern()))

  /**
   * Create a manual subscription to a fixed set of topic-partitions
   *
   * A consumer with this type of subscription does not perform consumer rebalancing
   *
   * @param topicPartitions
   *   Tuples of topic and partition
   */
  def manual(topicPartitions: (String, Int)*): Manual =
    Manual(topicPartitions.map { case (topic, partition) => new TopicPartition(topic, partition) }.toSet)

  /**
   * Create a subscription to a single topic-partition
   *
   * A consumer with this type of subscription does not perform consumer rebalancing
   *
   * @param topic
   * @param partition
   * @return
   */
  def manual(topic: String, partition: Int): Manual =
    manual((topic, partition))
}
