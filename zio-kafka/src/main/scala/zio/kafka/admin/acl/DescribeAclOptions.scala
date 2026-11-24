package zio.kafka.admin.acl

import org.apache.kafka.clients.admin.{ DescribeAclsOptions => JDescribeAclsOptions }
import zio._

/**
 * Options for `describeAcls`. `timeout` is the request timeout; when it is `None`, the Java client's default API
 * timeout applies. The timeout is converted to whole milliseconds as an `Int`, so a duration longer than `Int.MaxValue`
 * milliseconds (about 24.8 days) overflows.
 */
final case class DescribeAclOptions(timeout: Option[Duration]) {

  /** Converts to the Java `DescribeAclsOptions`. */
  def asJava: JDescribeAclsOptions = {
    val jopts = new JDescribeAclsOptions()

    timeout.fold(jopts)(timeout => jopts.timeoutMs(timeout.toMillis.toInt))
  }
}
