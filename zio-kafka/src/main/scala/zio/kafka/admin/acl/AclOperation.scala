package zio.kafka.admin.acl

import org.apache.kafka.common.acl.{ AclOperation => JAclOperation }

sealed trait AclOperation {
  def asJava: JAclOperation
}

object AclOperation {
  case object Unknown extends AclOperation {
    def asJava: JAclOperation = JAclOperation.UNKNOWN
  }
  case object Any extends AclOperation {
    def asJava: JAclOperation = JAclOperation.ANY
  }
  case object All extends AclOperation {
    def asJava: JAclOperation = JAclOperation.ALL
  }
  case object Read extends AclOperation {
    def asJava: JAclOperation = JAclOperation.READ
  }
  case object Write extends AclOperation {
    def asJava: JAclOperation = JAclOperation.WRITE
  }
  case object Create extends AclOperation {
    def asJava: JAclOperation = JAclOperation.CREATE
  }
  case object Delete extends AclOperation {
    def asJava: JAclOperation = JAclOperation.DELETE
  }
  case object Alter extends AclOperation {
    def asJava: JAclOperation = JAclOperation.ALTER
  }
  case object Describe extends AclOperation {
    def asJava: JAclOperation = JAclOperation.DESCRIBE
  }
  case object ClusterAction extends AclOperation {
    def asJava: JAclOperation = JAclOperation.CLUSTER_ACTION
  }
  case object DescribeConfigs extends AclOperation {
    def asJava: JAclOperation = JAclOperation.DESCRIBE_CONFIGS
  }
  case object AlterConfigs extends AclOperation {
    def asJava: JAclOperation = JAclOperation.ALTER_CONFIGS
  }
  case object IdempotentWrite extends AclOperation {
    def asJava: JAclOperation = JAclOperation.IDEMPOTENT_WRITE
  }

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
