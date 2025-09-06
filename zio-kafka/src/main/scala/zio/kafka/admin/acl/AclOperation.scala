package zio.kafka.admin.acl

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
  case object CreateTokens    extends AclOperation
  case object DescribeTokens  extends AclOperation
  case object TwoPhaseCommit  extends AclOperation
}
