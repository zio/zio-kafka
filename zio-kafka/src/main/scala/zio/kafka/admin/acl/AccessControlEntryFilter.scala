package zio.kafka.admin.acl

import org.apache.kafka.common.acl.{ AccessControlEntryFilter => JAccessControlEntryFilter }

/**
 * A filter that matches [[AccessControlEntry]] values. A `null` `principal` or `host` matches any value,
 * [[AclOperation.Any]] matches any operation, and [[AclPermissionType.Any]] matches any permission type. `operation`
 * and `permissionType` must not be `null`.
 */
final case class AccessControlEntryFilter(
  principal: String,
  host: String,
  operation: AclOperation,
  permissionType: AclPermissionType
) {

  /** Converts to the Java `AccessControlEntryFilter`. */
  def asJava: JAccessControlEntryFilter =
    new JAccessControlEntryFilter(principal, host, operation.asJava, permissionType.asJava)
}

/** Predefined filters, and the conversion from the Java `AccessControlEntryFilter`. */
object AccessControlEntryFilter {

  /**
   * A filter that matches any access control entry: `null` principal and host, and `Any` for the operation and the
   * permission type.
   */
  val Any: AccessControlEntryFilter = AccessControlEntryFilter(null, null, AclOperation.Any, AclPermissionType.Any)

  /** Converts a Java `AccessControlEntryFilter`. */
  def apply(jAccessControlEntryFilter: JAccessControlEntryFilter): AccessControlEntryFilter = AccessControlEntryFilter(
    principal = jAccessControlEntryFilter.principal(),
    host = jAccessControlEntryFilter.host(),
    operation = AclOperation(jAccessControlEntryFilter.operation()),
    permissionType = AclPermissionType(jAccessControlEntryFilter.permissionType())
  )
}
