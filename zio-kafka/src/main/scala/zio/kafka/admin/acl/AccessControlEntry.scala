package zio.kafka.admin.acl

import zio.kafka.admin.internal.JavaConverters._
import org.apache.kafka.common.acl.{ AccessControlEntry => JAccessControlEntry }

final case class AccessControlEntry(
  principal: String,
  host: String,
  operation: AclOperation,
  permissionType: AclPermissionType
) {
  def asJava: JAccessControlEntry = new JAccessControlEntry(principal, host, operation.asJava, permissionType.asJava)
}

object AccessControlEntry {
  def apply(jAccessControlEntry: JAccessControlEntry): AccessControlEntry = AccessControlEntry(
    principal = jAccessControlEntry.principal(),
    host = jAccessControlEntry.host(),
    operation = jAccessControlEntry.operation().asScala,
    permissionType = AclPermissionType(jAccessControlEntry.permissionType())
  )
}
