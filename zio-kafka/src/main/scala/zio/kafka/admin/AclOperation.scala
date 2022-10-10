package zio.kafka.admin

import org.apache.kafka.common.acl.{ AclOperation => JAclOperation }

sealed trait AclOperation {
  def asJava: JAclOperation = this match {
    case AclOperation.Unknown         => JAclOperation.UNKNOWN
    case AclOperation.Any             => JAclOperation.ANY
    case AclOperation.Read            => JAclOperation.READ
    case AclOperation.Write           => JAclOperation.WRITE
    case AclOperation.All             => JAclOperation.ALL
    case AclOperation.Create          => JAclOperation.CREATE
    case AclOperation.Delete          => JAclOperation.DELETE
    case AclOperation.Alter           => JAclOperation.ALTER
    case AclOperation.Describe        => JAclOperation.DESCRIBE
    case AclOperation.ClusterAction   => JAclOperation.CLUSTER_ACTION
    case AclOperation.DescribeConfigs => JAclOperation.DESCRIBE_CONFIGS
    case AclOperation.AlterConfigs    => JAclOperation.ALTER_CONFIGS
    case AclOperation.IdempotentWrite => JAclOperation.IDEMPOTENT_WRITE
  }
}

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
