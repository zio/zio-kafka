package zio.kafka.admin.resource

import org.apache.kafka.common.resource.{ PatternType => JPatternType }

sealed trait PatternType {
  def asJava: JPatternType
}

object PatternType {
  case object Literal extends PatternType {
    def asJava: JPatternType = JPatternType.LITERAL
  }
  case object Unknown extends PatternType {
    def asJava: JPatternType = JPatternType.UNKNOWN
  }
  case object Any extends PatternType {
    def asJava: JPatternType = JPatternType.ANY
  }
  case object Prefixed extends PatternType {
    def asJava: JPatternType = JPatternType.PREFIXED
  }
  case object Match extends PatternType {
    def asJava: JPatternType = JPatternType.MATCH
  }

  def apply(jPatternType: JPatternType): PatternType = jPatternType match {
    case JPatternType.LITERAL  => Literal
    case JPatternType.UNKNOWN  => Unknown
    case JPatternType.ANY      => Any
    case JPatternType.PREFIXED => Prefixed
    case JPatternType.MATCH    => Match
  }
}
