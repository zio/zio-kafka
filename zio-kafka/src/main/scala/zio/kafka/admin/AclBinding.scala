package zio.kafka.admin

import org.apache.kafka.common.acl.{
  AccessControlEntry => JAccessControlEntry,
  AclBinding => JAclBinding,
}
import org.apache.kafka.common.resource.{
  ResourcePattern => JResourcePattern
}

case class AclBinding(pattern: ResourcePattern, entry: AccessControlEntry) {
  def asJava: JAclBinding = new JAclBinding(pattern.asJava, entry.asJava)
}

object AclBinding {
  def apply(jAclBinding: JAclBinding): AclBinding =
    AclBinding(ResourcePattern(jAclBinding.pattern()), AccessControlEntry(jAclBinding.entry()))
}

case class ResourcePattern(
  resourceType: ResourceType,
  name: String,
  patternType: PatternType
) {
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

case class AccessControlEntry(
  principal: String,
  host: String,
  operation: AclOperation,
  permissionType: AclPermissionType
) {
  def asJava: JAccessControlEntry = new JAccessControlEntry(principal, host, operation.asJava, permissionType.asJava)
}

object AccessControlEntry {
  def apply(jAccessControlEntry: JAccessControlEntry): AccessControlEntry =
    AccessControlEntry(
      jAccessControlEntry.principal(),
      jAccessControlEntry.host(),
      AclOperation(jAccessControlEntry.operation()),
      AclPermissionType(jAccessControlEntry.permissionType())
    )
}
