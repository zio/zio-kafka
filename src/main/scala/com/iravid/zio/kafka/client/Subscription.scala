package zio.kafka.client

sealed trait Subscription
object Subscription {
  case class Topics(topics: Set[String]) extends Subscription
  case class Pattern(pattern: String)    extends Subscription
}
