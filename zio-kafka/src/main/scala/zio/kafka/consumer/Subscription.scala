package zio.kafka.consumer

import org.apache.kafka.common.TopicPartition
import zio.kafka.consumer.Subscription.SubscriptionKind
import zio.{ NonEmptyChunk, Task, ZIO }

import java.util.regex.{ Pattern => JPattern }
import scala.util.matching.Regex

sealed trait Subscription[+Kind <: SubscriptionKind]
object Subscription {
  final case class Topics(topics: Set[String])                  extends Subscription[TopicsKind]
  final case class Pattern(pattern: Regex)                      extends Subscription[PatternKind]
  final case class Manual(topicPartitions: Set[TopicPartition]) extends Subscription[ManualKind]

  sealed trait SubscriptionKind
  case object TopicsKind  extends SubscriptionKind
  case object PatternKind extends SubscriptionKind
  case object ManualKind  extends SubscriptionKind

  type TopicsKind  = TopicsKind.type
  type PatternKind = PatternKind.type
  type ManualKind  = ManualKind.type

  /**
   * Create a subscription for one or more topics
   *
   * @param name
   *   Topic to subscribe to
   * @param names
   *   Additional topics to subscribe to
   */
  def topics(names: Set[String]): Subscription[TopicsKind] =
    Topics(names)

  /**
   * Create a subscription for one or more topics
   *
   * @param name
   *   Topic to subscribe to
   * @param names
   *   Additional topics to subscribe to
   */
  def topics(name: String, names: String*): Subscription[TopicsKind] =
    topics(Set(name) ++ names.toSet)

  /**
   * Create a subscription for all topics matching the given pattern
   *
   * @param pattern
   *   Pattern
   */
  def pattern(pattern: Regex): Subscription[PatternKind] =
    Pattern(pattern)

  /**
   * Create a subscription for all topics matching the given pattern
   *
   * @param pattern
   *   Pattern to be converted to a Regex
   * @return
   *   The created subscription or failure when the pattern is invalid
   */
  def pattern(pattern: String): Task[Subscription[PatternKind]] =
    ZIO.attempt(new Regex(pattern)).map(Pattern.apply)

  /**
   * Create a subscription for all topics matching the given pattern
   *
   * @param pattern
   *   Pattern
   */
  def pattern(pattern: JPattern): Subscription[PatternKind] =
    Pattern(new Regex(pattern.pattern()))

  /**
   * Create a manual subscription to a fixed set of topic-partitions
   *
   * A consumer with this type of subscription does not perform consumer rebalancing
   *
   * @param topicPartitions
   *   Tuples of topic and partition
   */
  def manual(topicPartitions: Set[TopicPartition]): Subscription[ManualKind] =
    Manual(topicPartitions)

  /**
   * Create a manual subscription to a fixed set of topic-partitions
   *
   * A consumer with this type of subscription does not perform consumer rebalancing
   *
   * @param topicPartitions
   *   Tuples of topic and partition
   */
  def manual(topicPartitions: (String, Int)*): Subscription[ManualKind] =
    manual(topicPartitions.map { case (topic, partition) => new TopicPartition(topic, partition) }.toSet)

  /**
   * Create a subscription to a single topic-partition
   *
   * A consumer with this type of subscription does not perform consumer rebalancing
   *
   * @param topic
   * @param partition
   * @return
   */
  def manual(topic: String, partition: Int): Subscription[ManualKind] =
    manual((topic, partition))

  def union[Kind <: SubscriptionKind](s1: Subscription[Kind], s2: Subscription[Kind]): Subscription[Kind] =
    (s1, s2) match {
      case (Pattern(p1), Pattern(p2)) => Subscription.pattern(s"$p1|$p2".r)
      case (Topics(t1), Topics(t2))   => Subscription.topics(t1 ++ t2)
      case (Manual(tp1), Manual(tp2)) => Subscription.manual(tp1 ++ tp2)
      case _                          =>
        // Although we could combine Pattern and Topics, the Kafka consumer will throw exceptions when changing the subscription type
        throw new RuntimeException("Can never happen")
    }

  def unionAll[Kind <: SubscriptionKind](subscriptions: NonEmptyChunk[Subscription[Kind]]): Subscription[Kind] =
    subscriptions.reduce(union)

  def subscriptionMatches[Kind <: Subscription](subscription: Subscription[Kind], tp: TopicPartition): Boolean =
    subscription match {
      case Topics(topics)          => topics.contains(tp.topic())
      case Pattern(pattern)        => pattern.findFirstIn(tp.topic()).isDefined
      case Manual(topicPartitions) => topicPartitions.contains(tp)
    }
}
