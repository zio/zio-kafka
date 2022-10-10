package zio.kafka.admin

import org.apache.kafka.common.acl.{ AclPermissionType => JAclPermissionType }

sealed trait AclPermissionType {
  def asJava: JAclPermissionType = this match {
    case AclPermissionType.Allow   => JAclPermissionType.ALLOW
    case AclPermissionType.Any     => JAclPermissionType.ANY
    case AclPermissionType.Deny    => JAclPermissionType.DENY
    case AclPermissionType.Unknown => JAclPermissionType.UNKNOWN
  }
}

object AclPermissionType {
  case object Unknown extends AclPermissionType
  case object Any     extends AclPermissionType
  case object Deny    extends AclPermissionType
  case object Allow   extends AclPermissionType

  def apply(jAclPermissionType: JAclPermissionType): AclPermissionType =
    jAclPermissionType match {
      case JAclPermissionType.ALLOW   => Allow
      case JAclPermissionType.ANY     => Any
      case JAclPermissionType.DENY    => Deny
      case JAclPermissionType.UNKNOWN => Unknown
    }
}
