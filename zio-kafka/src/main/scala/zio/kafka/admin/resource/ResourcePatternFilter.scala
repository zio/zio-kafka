package zio.kafka.admin.resource

import org.apache.kafka.common.resource.{ResourcePatternFilter => JResourcePatternFilter}

final case class ResourcePatternFilter(resourceType: ResourceType, name: String, patternType: PatternType) {
  def asJava: JResourcePatternFilter = new JResourcePatternFilter(resourceType.asJava, name, patternType.asJava)
}

object ResourcePatternFilter {
  val Any: ResourcePatternFilter = ResourcePatternFilter(ResourceType.Any, null, PatternType.Any)

  def apply(jResourcePatternFilter: JResourcePatternFilter): ResourcePatternFilter =
    ResourcePatternFilter(
      ResourceType(jResourcePatternFilter.resourceType()),
      jResourcePatternFilter.name(),
      PatternType(jResourcePatternFilter.patternType()),
    )
}
