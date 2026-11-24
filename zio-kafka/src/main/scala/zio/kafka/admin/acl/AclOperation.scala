package zio.kafka.admin.acl

import org.apache.kafka.common.acl.{ AclOperation => JAclOperation }

/**
 * An operation that an ACL grants or denies permission to perform. Each case object in the companion maps to a Java
 * `AclOperation`.
 *
 * Allowing `All` implies allowing everything, and denying `All` implies denying everything. Allowing `Read`, `Write`,
 * `Delete` or `Alter` implies allowing `Describe`, and allowing `AlterConfigs` implies allowing `DescribeConfigs`.
 */
sealed trait AclOperation {

  /** Converts to the Java `AclOperation`. */
  def asJava: JAclOperation
}

/** The operations of an [[AclOperation]], and the conversion from the Java `AclOperation`. */
object AclOperation {

  /** An operation that this client cannot understand, perhaps because the client is too old (Java `UNKNOWN`). */
  case object Unknown extends AclOperation {

    /** Returns the Java `AclOperation.UNKNOWN`. */
    def asJava: JAclOperation = JAclOperation.UNKNOWN
  }

  /**
   * In a filter, matches any operation (Java `ANY`). An [[AccessControlEntry]] does not accept it: its `asJava` throws
   * a `java.lang.IllegalArgumentException`.
   */
  case object Any extends AclOperation {

    /** Returns the Java `AclOperation.ANY`. */
    def asJava: JAclOperation = JAclOperation.ANY
  }

  /**
   * The all operation (Java `ALL`). Allowing it implies allowing everything, and denying it implies denying everything.
   */
  case object All extends AclOperation {

    /** Returns the Java `AclOperation.ALL`. */
    def asJava: JAclOperation = JAclOperation.ALL
  }

  /** The read operation (Java `READ`). Allowing it implies allowing `Describe`. */
  case object Read extends AclOperation {

    /** Returns the Java `AclOperation.READ`. */
    def asJava: JAclOperation = JAclOperation.READ
  }

  /** The write operation (Java `WRITE`). Allowing it implies allowing `Describe`. */
  case object Write extends AclOperation {

    /** Returns the Java `AclOperation.WRITE`. */
    def asJava: JAclOperation = JAclOperation.WRITE
  }

  /** The create operation (Java `CREATE`). */
  case object Create extends AclOperation {

    /** Returns the Java `AclOperation.CREATE`. */
    def asJava: JAclOperation = JAclOperation.CREATE
  }

  /** The delete operation (Java `DELETE`). Allowing it implies allowing `Describe`. */
  case object Delete extends AclOperation {

    /** Returns the Java `AclOperation.DELETE`. */
    def asJava: JAclOperation = JAclOperation.DELETE
  }

  /** The alter operation (Java `ALTER`). Allowing it implies allowing `Describe`. */
  case object Alter extends AclOperation {

    /** Returns the Java `AclOperation.ALTER`. */
    def asJava: JAclOperation = JAclOperation.ALTER
  }

  /** The describe operation (Java `DESCRIBE`). */
  case object Describe extends AclOperation {

    /** Returns the Java `AclOperation.DESCRIBE`. */
    def asJava: JAclOperation = JAclOperation.DESCRIBE
  }

  /** The cluster action operation (Java `CLUSTER_ACTION`). */
  case object ClusterAction extends AclOperation {

    /** Returns the Java `AclOperation.CLUSTER_ACTION`. */
    def asJava: JAclOperation = JAclOperation.CLUSTER_ACTION
  }

  /** The describe configs operation (Java `DESCRIBE_CONFIGS`). */
  case object DescribeConfigs extends AclOperation {

    /** Returns the Java `AclOperation.DESCRIBE_CONFIGS`. */
    def asJava: JAclOperation = JAclOperation.DESCRIBE_CONFIGS
  }

  /** The alter configs operation (Java `ALTER_CONFIGS`). Allowing it implies allowing `DescribeConfigs`. */
  case object AlterConfigs extends AclOperation {

    /** Returns the Java `AclOperation.ALTER_CONFIGS`. */
    def asJava: JAclOperation = JAclOperation.ALTER_CONFIGS
  }

  /** The idempotent write operation (Java `IDEMPOTENT_WRITE`). */
  case object IdempotentWrite extends AclOperation {

    /** Returns the Java `AclOperation.IDEMPOTENT_WRITE`. */
    def asJava: JAclOperation = JAclOperation.IDEMPOTENT_WRITE
  }

  /** The create tokens operation (Java `CREATE_TOKENS`). */
  case object CreateTokens extends AclOperation {

    /** Returns the Java `AclOperation.CREATE_TOKENS`. */
    def asJava: JAclOperation = JAclOperation.CREATE_TOKENS
  }

  /** The describe tokens operation (Java `DESCRIBE_TOKENS`). */
  case object DescribeTokens extends AclOperation {

    /** Returns the Java `AclOperation.DESCRIBE_TOKENS`. */
    def asJava: JAclOperation = JAclOperation.DESCRIBE_TOKENS
  }

  /** The two phase commit operation (Java `TWO_PHASE_COMMIT`). */
  case object TwoPhaseCommit extends AclOperation {

    /** Returns the Java `AclOperation.TWO_PHASE_COMMIT`. */
    def asJava: JAclOperation = JAclOperation.TWO_PHASE_COMMIT
  }

  /** Converts a Java `AclOperation`. */
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
      case JAclOperation.CREATE_TOKENS    => CreateTokens
      case JAclOperation.DESCRIBE_TOKENS  => DescribeTokens
      case JAclOperation.TWO_PHASE_COMMIT => TwoPhaseCommit
    }
}
