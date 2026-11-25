package zio.kafka.admin.acl

import org.apache.kafka.common.acl.{ AccessControlEntry => JAccessControlEntry }

/**
 * An access control entry: a principal, a host, an operation and a permission type. It says that `principal`,
 * connecting from `host`, is allowed or denied `operation`. The host `*` stands for all hosts.
 *
 * `principal` and `host` must not be `null`, and `operation` and `permissionType` must not be `Any`; `asJava` throws a
 * `java.lang.NullPointerException` or a `java.lang.IllegalArgumentException` otherwise.
 */
final case class AccessControlEntry(
  principal: String,
  host: String,
  operation: AclOperation,
  permissionType: AclPermissionType
) {

  /** Converts to the Java `AccessControlEntry`. */
  def asJava: JAccessControlEntry = new JAccessControlEntry(principal, host, operation.asJava, permissionType.asJava)
}

/** Conversion from the Java `AccessControlEntry`. */
object AccessControlEntry {

  /** Converts a Java `AccessControlEntry`. */
  def apply(jAccessControlEntry: JAccessControlEntry): AccessControlEntry = AccessControlEntry(
    principal = jAccessControlEntry.principal(),
    host = jAccessControlEntry.host(),
    operation = AclOperation(jAccessControlEntry.operation()),
    permissionType = AclPermissionType(jAccessControlEntry.permissionType())
  )
}
