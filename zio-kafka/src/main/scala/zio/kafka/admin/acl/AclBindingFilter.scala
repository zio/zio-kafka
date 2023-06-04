package zio.kafka.admin.acl

import org.apache.kafka.common.acl.{AclBindingFilter => JAclBindingFilter}
import zio.kafka.admin.resource.ResourcePatternFilter

final case class AclBindingFilter(patternFilter: ResourcePatternFilter, entryFilter: AccessControlEntryFilter) {
  def asJava: JAclBindingFilter = new JAclBindingFilter(patternFilter.asJava, entryFilter.asJava)
}

object AclBindingFilter {
  def apply(jAclBindingFilter: JAclBindingFilter): AclBindingFilter = AclBindingFilter(
    patternFilter = ResourcePatternFilter(jAclBindingFilter.patternFilter()),
    entryFilter = AccessControlEntryFilter(jAclBindingFilter.entryFilter()),
  )
}
