package zio.kafka.admin.resource

import org.apache.kafka.common.resource.{ ResourceType => JResourceType }

sealed trait ResourceType {
  def asJava: JResourceType
}

object ResourceType {
  case object TransactionalId extends ResourceType {
    def asJava: JResourceType = JResourceType.TRANSACTIONAL_ID
  }
  case object Unknown extends ResourceType {
    def asJava: JResourceType = JResourceType.UNKNOWN
  }
  case object Topic extends ResourceType {
    def asJava: JResourceType = JResourceType.TOPIC
  }
  case object Cluster extends ResourceType {
    def asJava: JResourceType = JResourceType.CLUSTER
  }
  case object Any extends ResourceType {
    def asJava: JResourceType = JResourceType.ANY
  }
  case object Group extends ResourceType {
    def asJava: JResourceType = JResourceType.GROUP
  }
  case object DelegationToken extends ResourceType {
    def asJava: JResourceType = JResourceType.DELEGATION_TOKEN
  }
  case object User extends ResourceType {
    def asJava: JResourceType = JResourceType.USER
  }

  def apply(jResourceType: JResourceType): ResourceType = jResourceType match {
    case JResourceType.TRANSACTIONAL_ID => TransactionalId
    case JResourceType.UNKNOWN          => Unknown
    case JResourceType.TOPIC            => Topic
    case JResourceType.CLUSTER          => Cluster
    case JResourceType.ANY              => Any
    case JResourceType.GROUP            => Group
    case JResourceType.DELEGATION_TOKEN => DelegationToken
    case JResourceType.USER             => User
  }
}
