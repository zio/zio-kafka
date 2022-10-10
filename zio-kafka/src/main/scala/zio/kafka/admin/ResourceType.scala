package zio.kafka.admin

import org.apache.kafka.common.resource.{ ResourceType => JResourceType }

sealed trait ResourceType {
  def asJava: JResourceType = this match {
    case ResourceType.Any             => JResourceType.ANY
    case ResourceType.Cluster         => JResourceType.CLUSTER
    case ResourceType.DelegationToken => JResourceType.DELEGATION_TOKEN
    case ResourceType.Group           => JResourceType.GROUP
    case ResourceType.Topic           => JResourceType.TOPIC
    case ResourceType.TransactionalId => JResourceType.TRANSACTIONAL_ID
    case ResourceType.Unknown         => JResourceType.UNKNOWN
  }
}

object ResourceType {
  case object Any             extends ResourceType
  case object Cluster         extends ResourceType
  case object DelegationToken extends ResourceType
  case object Group           extends ResourceType
  case object Topic           extends ResourceType
  case object TransactionalId extends ResourceType
  case object Unknown         extends ResourceType

  def apply(jResourceType: JResourceType): ResourceType =
    jResourceType match {
      case JResourceType.ANY              => Any
      case JResourceType.CLUSTER          => Cluster
      case JResourceType.DELEGATION_TOKEN => DelegationToken
      case JResourceType.GROUP            => Group
      case JResourceType.TOPIC            => Topic
      case JResourceType.TRANSACTIONAL_ID => TransactionalId
      case JResourceType.UNKNOWN          => Unknown
    }
}
