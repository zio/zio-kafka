package zio.kafka.admin.acl

import org.apache.kafka.common.acl.{ AclBindingFilter => JAclBindingFilter }
import zio.kafka.admin.resource.ResourcePatternFilter

/**
 * A filter that matches [[AclBinding]] values: a binding matches when its resource pattern matches `patternFilter` and
 * its entry matches `entryFilter`.
 */
final case class AclBindingFilter(patternFilter: ResourcePatternFilter, entryFilter: AccessControlEntryFilter) {
  /** Converts to the Java `AclBindingFilter`. */
  def asJava: JAclBindingFilter = new JAclBindingFilter(patternFilter.asJava, entryFilter.asJava)
}

/** Conversion from the Java `AclBindingFilter`. */
object AclBindingFilter {
  /** Converts a Java `AclBindingFilter`. */
  def apply(jAclBindingFilter: JAclBindingFilter): AclBindingFilter = AclBindingFilter(
    patternFilter = ResourcePatternFilter(jAclBindingFilter.patternFilter()),
    entryFilter = AccessControlEntryFilter(jAclBindingFilter.entryFilter())
  )
}
