package zio.kafka.admin

import org.apache.kafka.common.acl.{ AclOperation => JAclOperation }

sealed trait AclOperation

object AclOperation {
  final object Unknown         extends AclOperation
  final object Any             extends AclOperation
  final object All             extends AclOperation
  final object Read            extends AclOperation
  final object Write           extends AclOperation
  final object Create          extends AclOperation
  final object Delete          extends AclOperation
  final object Alter           extends AclOperation
  final object Describe        extends AclOperation
  final object ClusterAction   extends AclOperation
  final object DescribeConfigs extends AclOperation
  final object AlterConfigs    extends AclOperation
  final object IdempotentWrite extends AclOperation

  def apply(jAclOperation: JAclOperation): AclOperation =
    jAclOperation match {
      case JAclOperation.UNKNOWN          => Unknown
      case JAclOperation.ANY              => Any
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
