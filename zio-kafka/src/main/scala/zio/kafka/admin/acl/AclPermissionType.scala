package zio.kafka.admin.acl

import org.apache.kafka.common.acl.{ AclPermissionType => JAclPermissionType }

/**
 * Whether an ACL grants or denies permission. Each case object in the companion maps to a Java `AclPermissionType`.
 */
sealed trait AclPermissionType {

  /** Converts to the Java `AclPermissionType`. */
  def asJava: JAclPermissionType
}

/** The permission types of an [[AclPermissionType]], and the conversion from the Java `AclPermissionType`. */
object AclPermissionType {

  /** A permission type that this client cannot understand, perhaps because the client is too old (Java `UNKNOWN`). */
  case object Unknown extends AclPermissionType {

    /** Returns the Java `AclPermissionType.UNKNOWN`. */
    override def asJava: JAclPermissionType = JAclPermissionType.UNKNOWN
  }

  /**
   * In a filter, matches any permission type (Java `ANY`). An [[AccessControlEntry]] does not accept it: its `asJava`
   * throws a `java.lang.IllegalArgumentException`.
   */
  case object Any extends AclPermissionType {

    /** Returns the Java `AclPermissionType.ANY`. */
    override def asJava: JAclPermissionType = JAclPermissionType.ANY
  }

  /** Disallows access (Java `DENY`). */
  case object Deny extends AclPermissionType {

    /** Returns the Java `AclPermissionType.DENY`. */
    override def asJava: JAclPermissionType = JAclPermissionType.DENY
  }

  /** Grants access (Java `ALLOW`). */
  case object Allow extends AclPermissionType {

    /** Returns the Java `AclPermissionType.ALLOW`. */
    override def asJava: JAclPermissionType = JAclPermissionType.ALLOW
  }

  /** Converts a Java `AclPermissionType`. */
  def apply(jAclPermissionType: JAclPermissionType): AclPermissionType = jAclPermissionType match {
    case JAclPermissionType.UNKNOWN => Unknown
    case JAclPermissionType.ANY     => Any
    case JAclPermissionType.DENY    => Deny
    case JAclPermissionType.ALLOW   => Allow
  }
}
