package zio.kafka.admin.acl

import org.apache.kafka.common.acl.{ AclBinding => JAclBinding }
import zio.kafka.admin.resource.ResourcePattern

final case class AclBinding(pattern: ResourcePattern, entry: AccessControlEntry) {
  def asJava: JAclBinding = new JAclBinding(pattern.asJava, entry.asJava)
}

object AclBinding {
  def apply(jAclBinding: JAclBinding): AclBinding =
    AclBinding(pattern = ResourcePattern(jAclBinding.pattern()), entry = AccessControlEntry(jAclBinding.entry()))
}
