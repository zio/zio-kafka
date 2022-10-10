package zio.kafka.admin

import org.apache.kafka.common.resource.{ PatternType => JPatternType }

sealed trait PatternType {
  def asJava: JPatternType = this match {
    case PatternType.Any      => JPatternType.ANY
    case PatternType.Literal  => JPatternType.LITERAL
    case PatternType.Match    => JPatternType.MATCH
    case PatternType.Prefixed => JPatternType.PREFIXED
    case PatternType.Unknown  => JPatternType.UNKNOWN
  }
}

object PatternType {
  case object Any      extends PatternType
  case object Literal  extends PatternType
  case object Match    extends PatternType
  case object Prefixed extends PatternType
  case object Unknown  extends PatternType

  def apply(jPatternType: JPatternType): PatternType =
    jPatternType match {
      case JPatternType.ANY      => Any
      case JPatternType.LITERAL  => Literal
      case JPatternType.MATCH    => Match
      case JPatternType.PREFIXED => Prefixed
      case JPatternType.UNKNOWN  => Unknown
    }
}
