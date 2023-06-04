package zio.kafka.admin.acl

import zio._
import org.apache.kafka.clients.admin.{DeleteAclsOptions => JDeleteAclsOptions}

final case class DeleteAclsOptions(timeout: Option[Duration]) {
  def asJava: JDeleteAclsOptions = {
    val jopts = new JDeleteAclsOptions()

    timeout.fold(jopts)(timeout => jopts.timeoutMs(timeout.toMillis.toInt))
  }
}
