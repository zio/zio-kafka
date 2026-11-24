package zio.kafka.admin.resource

import org.apache.kafka.common.resource.{ ResourcePattern => JResourcePattern }

/**
 * A pattern that an ACL uses to match zero or more resources: a resource type, a name and a pattern type. The
 * `patternType` controls how `name` is matched against resource names. The name `*` stands for all resources of the
 * type.
 *
 * `name` must not be `null`, `resourceType` must not be `Any`, and `patternType` must not be `Any` or `Match`; `asJava`
 * throws a `java.lang.NullPointerException` or a `java.lang.IllegalArgumentException` otherwise.
 */
final case class ResourcePattern(resourceType: ResourceType, name: String, patternType: PatternType) {
  /** Converts to the Java `ResourcePattern`. */
  def asJava: JResourcePattern = new JResourcePattern(resourceType.asJava, name, patternType.asJava)
}

/** Conversion from the Java `ResourcePattern`. */
object ResourcePattern {
  /** Converts a Java `ResourcePattern`. */
  def apply(jResourcePattern: JResourcePattern): ResourcePattern =
    ResourcePattern(
      ResourceType(jResourcePattern.resourceType()),
      jResourcePattern.name(),
      PatternType(jResourcePattern.patternType())
    )
}
