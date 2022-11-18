package zio.kafka.admin.resource

import org.apache.kafka.common.resource.{ ResourcePattern => JResourcePattern }

final case class ResourcePattern(resourceType: ResourceType, name: String, patternType: PatternType) {
  def asJava: JResourcePattern = new JResourcePattern(resourceType.asJava, name, patternType.asJava)
}

object ResourcePattern {
  def apply(jResourcePattern: JResourcePattern): ResourcePattern =
    ResourcePattern(
      ResourceType(jResourcePattern.resourceType()),
      jResourcePattern.name(),
      PatternType(jResourcePattern.patternType())
    )
}
