package zio.kafka.admin.acl

import zio.kafka.admin.internal.JavaConverters._
import org.apache.kafka.common.acl.{ AccessControlEntryFilter => JAccessControlEntryFilter }

final case class AccessControlEntryFilter(
  principal: String,
  host: String,
  operation: AclOperation,
  permissionType: AclPermissionType
) {
  def asJava: JAccessControlEntryFilter =
    new JAccessControlEntryFilter(principal, host, operation.asJava, permissionType.asJava)
}

object AccessControlEntryFilter {
  val Any: AccessControlEntryFilter = AccessControlEntryFilter(null, null, AclOperation.Any, AclPermissionType.Any)

  def apply(jAccessControlEntryFilter: JAccessControlEntryFilter): AccessControlEntryFilter = AccessControlEntryFilter(
    principal = jAccessControlEntryFilter.principal(),
    host = jAccessControlEntryFilter.host(),
    operation = jAccessControlEntryFilter.operation().asScala,
    permissionType = AclPermissionType(jAccessControlEntryFilter.permissionType())
  )
}
