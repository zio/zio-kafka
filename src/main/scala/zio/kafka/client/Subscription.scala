package zio.kafka.client
import scala.util.matching.Regex

sealed trait Subscription
object Subscription {
  case class Topics(topics: Set[String]) extends Subscription
  case class Pattern(pattern: Regex)     extends Subscription
}
