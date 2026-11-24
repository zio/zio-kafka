package zio.kafka.consumer

import zio._

import scala.util.matching.Regex
import java.util.regex.{ Pattern => JPattern }
import org.apache.kafka.common.TopicPartition

/**
 * Describes what a [[Consumer]] reads: the records of named topics, of the topics that match a pattern, or of a fixed
 * set of topic-partitions.
 *
 * {{{
 * val byName: Subscription    = Subscription.topics("orders", "payments")
 * val byPattern: Subscription = Subscription.pattern("orders-.*".r)
 * val fixed: Subscription     = Subscription.manual("orders" -> 0, "orders" -> 1)
 * }}}
 *
 * @see
 *   [[https://zio.dev/zio-kafka/consumer/ Consumer]]
 */
sealed trait Subscription

/** Constructors for [[Subscription]]. */
object Subscription {

  /** A subscription to the given topics. */
  final case class Topics(topics: Set[String]) extends Subscription

  /** A subscription to the topics whose names match the given pattern. */
  final case class Pattern(pattern: Regex) extends Subscription

  /**
   * A subscription to a fixed set of topic-partitions. A consumer with this type of subscription does not perform
   * consumer rebalancing.
   */
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

  private[consumer] def union(s1: Subscription, s2: Subscription): Option[Subscription] = (s1, s2) match {
    case (Pattern(p1), Pattern(p2)) => Some(Pattern(s"$p1|$p2".r))
    case (Topics(t1), Topics(t2))   => Some(Topics(t1 ++ t2))
    case (Manual(tp1), Manual(tp2)) => Some(Manual(tp1 ++ tp2))
    case _ =>
      None // Although we could combine Pattern and Topics, the Kafka consumer will throw exceptions when changing the subscription type
  }

  private[consumer] def unionAll(subscriptions: NonEmptyChunk[Subscription]): Option[Subscription] =
    subscriptions.foldLeft(Option(subscriptions.head)) { case (s1, s2) => s1.flatMap(union(_, s2)) }

  private[consumer] def subscriptionMatches(subscription: Subscription, tp: TopicPartition): Boolean =
    subscription match {
      case Topics(topics)          => topics.contains(tp.topic())
      case Pattern(pattern)        => pattern.findFirstIn(tp.topic()).isDefined
      case Manual(topicPartitions) => topicPartitions.contains(tp)
    }
}
