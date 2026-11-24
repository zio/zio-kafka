package zio.kafka.admin.acl

import zio._
import org.apache.kafka.clients.admin.{ CreateAclsOptions => JCreateAclOptions }

/**
 * Options for `createAcls`. `timeout` is the request timeout; when it is `None`, the Java client's default API timeout
 * applies. The timeout is converted to whole milliseconds as an `Int`, so a duration longer than `Int.MaxValue`
 * milliseconds (about 24.8 days) overflows.
 */
final case class CreateAclOptions(timeout: Option[Duration]) {
  /** Converts to the Java `CreateAclsOptions`. */
  def asJava: JCreateAclOptions = {
    val jopts = new JCreateAclOptions()

    timeout.fold(jopts)(timeout => jopts.timeoutMs(timeout.toMillis.toInt))
  }
}
