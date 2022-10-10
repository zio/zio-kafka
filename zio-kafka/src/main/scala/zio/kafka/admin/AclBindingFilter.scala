package zio.kafka.admin

import org.apache.kafka.common.acl.{
  AccessControlEntryFilter => JAccessControlEntryFilter,
  AclBindingFilter => JAclBindingFilter
}
import org.apache.kafka.common.resource.{
  ResourcePattern => JResourcePattern,
  ResourcePatternFilter => JResourcePatternFilter
}

case class AclBindingFilter(pattern: ResourcePatternFilter, entry: AccessControlEntryFilter) {
  def asJava: JAclBindingFilter = new JAclBindingFilter(pattern.asJava, entry.asJava)
}

case class ResourcePatternFilter(
  resourceType: ResourceType,
  name: String,
  patternType: PatternType
) {
  def asJava: JResourcePatternFilter = new JResourcePatternFilter(resourceType.asJava, name, patternType.asJava)
}

object ResourcePatternFilter {
  def apply(jResourcePattern: JResourcePattern): ResourcePattern =
    ResourcePattern(
      ResourceType(jResourcePattern.resourceType()),
      jResourcePattern.name(),
      PatternType(jResourcePattern.patternType())
    )
}

case class AccessControlEntryFilter(
  principal: String,
  host: String,
  operation: AclOperation,
  permissionType: AclPermissionType
) {
  def asJava: JAccessControlEntryFilter =
    new JAccessControlEntryFilter(principal, host, operation.asJava, permissionType.asJava)
}

object AccessControlEntryFilter {
  def apply(jAccessControlEntryFilter: JAccessControlEntryFilter): AccessControlEntry =
    AccessControlEntry(
      jAccessControlEntryFilter.principal(),
      jAccessControlEntryFilter.host(),
      AclOperation(jAccessControlEntryFilter.operation()),
      AclPermissionType(jAccessControlEntryFilter.permissionType())
    )
}
