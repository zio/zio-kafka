package zio.kafka.client

import zio.Task
import scala.util.matching.Regex
import java.util.regex.{ Pattern => JPattern }

sealed trait Subscription
object Subscription {
  case class Topics(topics: Set[String]) extends Subscription
  case class Pattern(pattern: Regex)     extends Subscription

  /**
   * Create a subscription for one or more topics
   *
   * @param name Topic to subscribe to
   * @param names Additional topics to subscribe to
   */
  def topics(name: String, names: String*): Subscription =
    Topics(Set(name) ++ names.toSet)

  /**
   * Create a subscription for all topics matching the given pattern
   *
   * @param pattern Pattern
   */
  def pattern(pattern: Regex): Subscription =
    Pattern(pattern)

  /**
   * Create a subscription for all topics matching the given pattern
   *
   * @param pattern Pattern to be converted to a Regex
   * @return The created subscription or failure when the pattern is invalid
   */
  def pattern(pattern: String): Task[Subscription] =
    Task.effect(new Regex(pattern)).map(Pattern)

  /**
   * Create a subscription for all topics matching the given pattern
   *
   * @param pattern Pattern
   */
  def pattern(pattern: JPattern): Subscription =
    Pattern(new Regex(pattern.pattern()))
}
