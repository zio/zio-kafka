package zio.kafka.admin.resource

import org.apache.kafka.common.resource.{ ResourceType => JResourceType }

/**
 * A type of resource that an ACL can be applied to. Each case object in the companion maps to a Java `ResourceType`.
 */
sealed trait ResourceType {
  /** Converts to the Java `ResourceType`. */
  def asJava: JResourceType
}

/** The resource types of a [[ResourceType]], and the conversion from the Java `ResourceType`. */
object ResourceType {
  /** A transactional id (Java `TRANSACTIONAL_ID`). */
  case object TransactionalId extends ResourceType {
    /** Returns the Java `ResourceType.TRANSACTIONAL_ID`. */
    def asJava: JResourceType = JResourceType.TRANSACTIONAL_ID
  }
  /** A resource type that this client cannot understand, perhaps because the client is too old (Java `UNKNOWN`). */
  case object Unknown extends ResourceType {
    /** Returns the Java `ResourceType.UNKNOWN`. */
    def asJava: JResourceType = JResourceType.UNKNOWN
  }
  /** A Kafka topic (Java `TOPIC`). */
  case object Topic extends ResourceType {
    /** Returns the Java `ResourceType.TOPIC`. */
    def asJava: JResourceType = JResourceType.TOPIC
  }
  /** The cluster as a whole (Java `CLUSTER`). */
  case object Cluster extends ResourceType {
    /** Returns the Java `ResourceType.CLUSTER`. */
    def asJava: JResourceType = JResourceType.CLUSTER
  }
  /**
   * In a filter, matches any resource type (Java `ANY`). A [[ResourcePattern]] does not accept it: its `asJava` throws
   * a `java.lang.IllegalArgumentException`.
   */
  case object Any extends ResourceType {
    /** Returns the Java `ResourceType.ANY`. */
    def asJava: JResourceType = JResourceType.ANY
  }
  /** A consumer group (Java `GROUP`). */
  case object Group extends ResourceType {
    /** Returns the Java `ResourceType.GROUP`. */
    def asJava: JResourceType = JResourceType.GROUP
  }
  /** A delegation token, identified by its token id (Java `DELEGATION_TOKEN`). */
  case object DelegationToken extends ResourceType {
    /** Returns the Java `ResourceType.DELEGATION_TOKEN`. */
    def asJava: JResourceType = JResourceType.DELEGATION_TOKEN
  }
  /** A user principal (Java `USER`). */
  case object User extends ResourceType {
    /** Returns the Java `ResourceType.USER`. */
    def asJava: JResourceType = JResourceType.USER
  }

  /** Converts a Java `ResourceType`. */
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
