package zio.kafka.admin.resource

import org.apache.kafka.common.resource.{ ResourcePatternFilter => JResourcePatternFilter }

/**
 * A filter that matches [[ResourcePattern]] values. `ResourceType.Any` ignores the resource type of the pattern, a
 * `null` name ignores its name, `PatternType.Any` matches any pattern type, and `PatternType.Match` matches the
 * patterns that match `name`, including prefixed and wildcard patterns. Any other value matches only patterns with the
 * same value.
 */
final case class ResourcePatternFilter(resourceType: ResourceType, name: String, patternType: PatternType) {
  /** Converts to the Java `ResourcePatternFilter`. */
  def asJava: JResourcePatternFilter = new JResourcePatternFilter(resourceType.asJava, name, patternType.asJava)
}

/** Predefined filters, and the conversion from the Java `ResourcePatternFilter`. */
object ResourcePatternFilter {
  /**
   * A filter that matches any resource pattern: `Any` for the resource type and the pattern type, and a `null` name.
   */
  val Any: ResourcePatternFilter = ResourcePatternFilter(ResourceType.Any, null, PatternType.Any)

  /** Converts a Java `ResourcePatternFilter`. */
  def apply(jResourcePatternFilter: JResourcePatternFilter): ResourcePatternFilter =
    ResourcePatternFilter(
      ResourceType(jResourcePatternFilter.resourceType()),
      jResourcePatternFilter.name(),
      PatternType(jResourcePatternFilter.patternType())
    )
}
