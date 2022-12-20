package zio.kafka.admin.acl

import org.apache.kafka.clients.admin.{ DescribeAclsOptions => JDescribeAclsOptions }
import zio._

final case class DescribeAclOptions(timeout: Option[Duration]) {
  def asJava: JDescribeAclsOptions = {
    val jopts = new JDescribeAclsOptions()

    timeout.fold(jopts)(timeout => jopts.timeoutMs(timeout.toMillis.toInt))
  }
}
