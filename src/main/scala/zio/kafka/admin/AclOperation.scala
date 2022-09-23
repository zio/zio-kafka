package zio.kafka.admin

import org.apache.kafka.common.acl.{ AclOperation => JAclOperation }

sealed trait AclOperation

object AclOperation {
  case object Unknown         extends AclOperation
  case object Any             extends AclOperation
  case object All             extends AclOperation
  case object Read            extends AclOperation
  case object Write           extends AclOperation
  case object Create          extends AclOperation
  case object Delete          extends AclOperation
  case object Alter           extends AclOperation
  case object Describe        extends AclOperation
  case object ClusterAction   extends AclOperation
  case object DescribeConfigs extends AclOperation
  case object AlterConfigs    extends AclOperation
  case object IdempotentWrite extends AclOperation

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
