package zio.kafka.admin.acl

import org.apache.kafka.common.acl.{ AclBinding => JAclBinding }
import zio.kafka.admin.resource.ResourcePattern

/**
 * A binding between a resource pattern and an access control entry: the entry applies to the resources that the pattern
 * matches.
 *
 * {{{
 * import zio.kafka.admin.acl._
 * import zio.kafka.admin.resource._
 *
 * val binding: AclBinding = AclBinding(
 *   ResourcePattern(ResourceType.Topic, "orders", PatternType.Literal),
 *   AccessControlEntry("User:alice", "*", AclOperation.Read, AclPermissionType.Allow)
 * )
 * }}}
 */
final case class AclBinding(pattern: ResourcePattern, entry: AccessControlEntry) {

  /** Converts to the Java `AclBinding`. */
  def asJava: JAclBinding = new JAclBinding(pattern.asJava, entry.asJava)
}

/** Conversion from the Java `AclBinding`. */
object AclBinding {

  /** Converts a Java `AclBinding`. */
  def apply(jAclBinding: JAclBinding): AclBinding =
    AclBinding(pattern = ResourcePattern(jAclBinding.pattern()), entry = AccessControlEntry(jAclBinding.entry()))
}
