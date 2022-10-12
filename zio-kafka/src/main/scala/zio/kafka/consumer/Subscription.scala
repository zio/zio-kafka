package zio.kafka.consumer

import zio.{ NonEmptyChunk, Task, ZIO }

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
    ZIO.attempt(new Regex(pattern)).map(Pattern.apply)

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

  def union(s1: Subscription, s2: Subscription): Option[Subscription] = (s1, s2) match {
    case (Pattern(p1), Pattern(p2)) => Some(Pattern(s"$p1|$p2".r))
    case (Topics(t1), Topics(t2))   => Some(Topics(t1 ++ t2))
    case (Manual(tp1), Manual(tp2)) => Some(Manual(tp1 ++ tp2))
    case _ =>
      None // Although we could combine Pattern and Topics, the Kafka consumer will throw exceptions when changing the subscription type
  }

  def unionAll(subscriptions: NonEmptyChunk[Subscription]): Option[Subscription] =
    subscriptions.foldLeft(Option(subscriptions.head)) { case (s1, s2) => s1.flatMap(union(_, s2)) }

  def subscriptionMatches(subscription: Subscription, tp: TopicPartition): Boolean = subscription match {
    case Topics(topics)          => topics.contains(tp.topic())
    case Pattern(pattern)        => pattern.findFirstIn(tp.topic()).isDefined
    case Manual(topicPartitions) => topicPartitions.contains(tp)
  }
}
