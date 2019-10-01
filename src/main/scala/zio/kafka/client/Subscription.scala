package zio.kafka.client

import zio.Task
import scala.util.matching.Regex
import java.util.regex.{ Pattern => JPattern }

sealed trait Subscription
object Subscription {
  case class Topics(topics: Set[String]) extends Subscription
  case class Pattern(pattern: Regex)     extends Subscription

  def topics(name: String, names: String*): Subscription =
    Topics(Set(name) ++ names.toSet)

  def pattern(pattern: Regex): Subscription =
    Pattern(pattern)

  def pattern(pattern: String): Task[Subscription] =
    Task.effect(new Regex(pattern)).map(Pattern)

  def pattern(pattern: JPattern): Subscription =
    Pattern(new Regex(pattern.pattern()))
}
