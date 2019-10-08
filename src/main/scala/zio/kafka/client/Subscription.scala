package zio.kafka.client

import java.util.regex.{Pattern => JPattern}

import zio.Task

import scala.util.matching.Regex

sealed trait Subscription {
  val rebalanceListener: RebalanceListener
}
object Subscription {
  case class Topics(topics: Set[String], rebalanceListener: RebalanceListener = RebalanceListener.None) extends Subscription
  case class Pattern(pattern: Regex, rebalanceListener: RebalanceListener = RebalanceListener.None)     extends Subscription

  def topics(name: String, names: String*): Subscription =
    Topics(Set(name) ++ names.toSet, RebalanceListener.None)

  def pattern(pattern: Regex): Subscription =
    Pattern(pattern, RebalanceListener.None)

  def pattern(pattern: String): Task[Subscription] =
    Task.effect(new Regex(pattern)).map(Pattern(_, RebalanceListener.None))

  def pattern(pattern: JPattern): Subscription =
    Pattern(new Regex(pattern.pattern()), RebalanceListener.None)

  implicit class Extensions(subscription: Subscription) {
    def withRebalanceListener(rebalanceListener: RebalanceListener): Subscription =
      subscription match {
        case s: Topics => s.copy(rebalanceListener = rebalanceListener)
        case s: Pattern => s.copy(rebalanceListener = rebalanceListener)
        case _ => subscription
      }
  }
}
