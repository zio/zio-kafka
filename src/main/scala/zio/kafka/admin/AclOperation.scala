package zio.kafka.admin

import org.apache.kafka.common.acl.{ AclOperation => JAclOperation }

sealed trait AclOperation

object AclOperation {
  object Unknown         extends AclOperation
  object Any             extends AclOperation
  object All             extends AclOperation
  object Read            extends AclOperation
  object Write           extends AclOperation
  object Create          extends AclOperation
  object Delete          extends AclOperation
  object Alter           extends AclOperation
  object Describe        extends AclOperation
  object ClusterAction   extends AclOperation
  object DescribeConfigs extends AclOperation
  object AlterConfigs    extends AclOperation
  object IdempotentWrite extends AclOperation

  def apply(jAclOperation: JAclOperation): AclOperation =
    jAclOperation match {
      case JAclOperation.UNKNOWN          => Unknown
      case JAclOperation.ANY              => Any
      case JAclOperation.ALL              => All
      case JAclOperation.READ             => Read
      case JAclOperation.WRITE            => Write
      case JAclOperation.CREATE           => Create
      case JAclOperation.DELETE           => Delete
      case JAclOperation.ALTER            => Alter
      case JAclOperation.DESCRIBE         => Describe
      case JAclOperation.CLUSTER_ACTION   => ClusterAction
      case JAclOperation.DESCRIBE_CONFIGS => DescribeConfigs
      case JAclOperation.ALTER_CONFIGS    => AlterConfigs
      case JAclOperation.IDEMPOTENT_WRITE => IdempotentWrite
    }
}
