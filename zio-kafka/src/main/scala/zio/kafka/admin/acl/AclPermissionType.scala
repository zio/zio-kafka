package zio.kafka.admin.acl

import org.apache.kafka.common.acl.{ AclPermissionType => JAclPermissionType }

sealed trait AclPermissionType {
  def asJava: JAclPermissionType
}

object AclPermissionType {
  case object Unknown extends AclPermissionType {
    override def asJava: JAclPermissionType = JAclPermissionType.UNKNOWN
  }
  case object Any extends AclPermissionType {
    override def asJava: JAclPermissionType = JAclPermissionType.ANY
  }
  case object Deny extends AclPermissionType {
    override def asJava: JAclPermissionType = JAclPermissionType.DENY
  }
  case object Allow extends AclPermissionType {
    override def asJava: JAclPermissionType = JAclPermissionType.ALLOW
  }

  def apply(jAclPermissionType: JAclPermissionType): AclPermissionType = jAclPermissionType match {
    case JAclPermissionType.UNKNOWN => Unknown
    case JAclPermissionType.ANY     => Any
    case JAclPermissionType.DENY    => Deny
    case JAclPermissionType.ALLOW   => Allow
  }
}
