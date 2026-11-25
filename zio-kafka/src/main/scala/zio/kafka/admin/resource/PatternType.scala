package zio.kafka.admin.resource

import org.apache.kafka.common.resource.{ PatternType => JPatternType }

/**
 * How the name of a resource pattern is matched against resource names. Each case object in the companion maps to a
 * Java `PatternType`.
 */
sealed trait PatternType {

  /** Converts to the Java `PatternType`. */
  def asJava: JPatternType
}

/** The pattern types of a [[PatternType]], and the conversion from the Java `PatternType`. */
object PatternType {

  /**
   * A literal resource name (Java `LITERAL`): the full name of a resource, for example the topic `foo`. The name `*`
   * stands for a resource with any name.
   */
  case object Literal extends PatternType {

    /** Returns the Java `PatternType.LITERAL`. */
    def asJava: JPatternType = JPatternType.LITERAL
  }

  /** A pattern type that this client cannot understand, perhaps because the client is too old (Java `UNKNOWN`). */
  case object Unknown extends PatternType {

    /** Returns the Java `PatternType.UNKNOWN`. */
    def asJava: JPatternType = JPatternType.UNKNOWN
  }

  /**
   * In a filter, matches any pattern type (Java `ANY`). A [[ResourcePattern]] does not accept it: its `asJava` throws a
   * `java.lang.IllegalArgumentException`.
   */
  case object Any extends PatternType {

    /** Returns the Java `PatternType.ANY`. */
    def asJava: JPatternType = JPatternType.ANY
  }

  /**
   * A prefixed resource name (Java `PREFIXED`): the name is a prefix, for example topics whose names start with `foo`.
   */
  case object Prefixed extends PatternType {

    /** Returns the Java `PatternType.PREFIXED`. */
    def asJava: JPatternType = JPatternType.PREFIXED
  }

  /**
   * In a filter, performs pattern matching (Java `MATCH`). The filter matches every pattern that matches its name: a
   * literal pattern with the same name, a wildcard pattern (`*`), and a prefixed pattern whose name is a prefix of it.
   * A [[ResourcePattern]] does not accept it: its `asJava` throws a `java.lang.IllegalArgumentException`.
   */
  case object Match extends PatternType {

    /** Returns the Java `PatternType.MATCH`. */
    def asJava: JPatternType = JPatternType.MATCH
  }

  /** Converts a Java `PatternType`. */
  def apply(jPatternType: JPatternType): PatternType = jPatternType match {
    case JPatternType.LITERAL  => Literal
    case JPatternType.UNKNOWN  => Unknown
    case JPatternType.ANY      => Any
    case JPatternType.PREFIXED => Prefixed
    case JPatternType.MATCH    => Match
  }
}
