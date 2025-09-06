package zio.kafka.admin.internal

import zio.kafka.admin.AdminClient.{ ConsumerGroupDescription, _ }
import org.apache.kafka.clients.admin.ListOffsetsResult.{ ListOffsetsResultInfo => JListOffsetsResultInfo }
import org.apache.kafka.clients.admin.{
  AlterConfigOp => JAlterConfigOp,
  AlterConfigsOptions => JAlterConfigsOptions,
  AlterConsumerGroupOffsetsOptions => JAlterConsumerGroupOffsetsOptions,
  Config => JConfig,
  ConsumerGroupDescription => JConsumerGroupDescription,
  ConsumerGroupListing => _,
  CreatePartitionsOptions => JCreatePartitionsOptions,
  CreateTopicsOptions => JCreateTopicsOptions,
  DeleteAclsOptions => _,
  DeleteConsumerGroupsOptions => JDeleteConsumerGroupsOptions,
  DeleteRecordsOptions => JDeleteRecordsOptions,
  DeleteTopicsOptions => JDeleteTopicsOptions,
  DescribeClusterOptions => JDescribeClusterOptions,
  DescribeConfigsOptions => JDescribeConfigsOptions,
  DescribeConsumerGroupsOptions => JDescribeConsumerGroupsOptions,
  DescribeTopicsOptions => JDescribeTopicsOptions,
  GroupListing => JGroupListing,
  ListConsumerGroupOffsetsOptions => JListConsumerGroupOffsetsOptions,
  ListConsumerGroupOffsetsSpec => JListConsumerGroupOffsetsSpec,
  ListConsumerGroupsOptions => _,
  ListGroupsOptions => JListGroupsOptions,
  ListOffsetsOptions => JListOffsetsOptions,
  ListTopicsOptions => JListTopicsOptions,
  LogDirDescription => JLogDirDescription,
  MemberDescription => JMemberDescription,
  NewPartitions => JNewPartitions,
  NewTopic => JNewTopic,
  OffsetSpec => JOffsetSpec,
  ReplicaInfo => JReplicaInfo,
  TopicDescription => JTopicDescription,
  TopicListing => JTopicListing
}
import org.apache.kafka.clients.consumer.{ OffsetAndMetadata => JOffsetAndMetadata }
import org.apache.kafka.common.config.{ ConfigResource => JConfigResource }
import org.apache.kafka.common.{
  GroupState => JGroupState,
  GroupType => JGroupType,
  IsolationLevel => JIsolationLevel,
  Metric => JMetric,
  MetricName => JMetricName,
  Node => JNode,
  TopicPartition => JTopicPartition,
  TopicPartitionInfo => JTopicPartitionInfo
}
import zio.kafka.admin.acl._
import org.apache.kafka.common.acl.{ AclOperation => JAclOperation }

import java.util.Optional
import scala.annotation.tailrec
import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.jdk.CollectionConverters._
import scala.util.{ Failure, Success, Try }

object JavaConverters {

  implicit final class MapOps[K1, V1](private val v: Map[K1, V1]) extends AnyVal {
    def bimap[K2, V2](fk: K1 => K2, fv: V1 => V2): Map[K2, V2] = v.map { case (k, v) => fk(k) -> fv(v) }
  }

  implicit final class MutableMapOps[K1, V1](private val v: mutable.Map[K1, V1]) extends AnyVal {
    def bimap[K2, V2](fk: K1 => K2, fv: V1 => V2): mutable.Map[K2, V2] = v.map { case (k, v) => fk(k) -> fv(v) }
  }

  implicit final class OptionalOps[T](private val v: Optional[T]) extends AnyVal {
    def toScala: Option[T] = if (v.isPresent) Some(v.get()) else None
  }

  implicit final class OptionOps[T](private val v: Option[T]) extends AnyVal {
    def toJava: Optional[T] = v.fold(Optional.empty[T])(Optional.of)
  }

  implicit final class ListOps[A](private val list: List[A]) extends AnyVal {
    def forEach[B](f: A => Try[B]): Try[List[B]] = {
      @tailrec
      def loop(acc: ListBuffer[B], rest: List[A]): Try[List[B]] =
        rest match {
          case Nil => Success(acc.toList)
          case h :: t =>
            f(h) match {
              case Success(b)        => loop(acc += b, t)
              case fail @ Failure(_) => fail.asInstanceOf[Try[List[B]]]
            }
        }

      loop(ListBuffer.empty, list)
    }
  }

  implicit class ConfigResourceAsJava(val cr: ConfigResource) extends AnyVal {
    def asJava: JConfigResource = new JConfigResource(cr.`type`.asJava, cr.name)
  }
  implicit class JConfigResourceAsScala(val jcr: JConfigResource) extends AnyVal {
    def asScala: ConfigResource =
      ConfigResource(`type` = jcr.`type`().asScala, name = jcr.name())
  }

  implicit class ConfigResourceTypeAsJava(val crt: ConfigResourceType) extends AnyVal {
    def asJava: JConfigResource.Type =
      crt match {
        case ConfigResourceType.BrokerLogger  => JConfigResource.Type.BROKER_LOGGER
        case ConfigResourceType.Broker        => JConfigResource.Type.BROKER
        case ConfigResourceType.Topic         => JConfigResource.Type.TOPIC
        case ConfigResourceType.Unknown       => JConfigResource.Type.UNKNOWN
        case ConfigResourceType.ClientMetrics => JConfigResource.Type.CLIENT_METRICS
        case ConfigResourceType.Group         => JConfigResource.Type.GROUP
      }
  }
  implicit class JConfigResourceTypeAsScala(val jcrt: JConfigResource.Type) extends AnyVal {
    def asScala: ConfigResourceType =
      jcrt match {
        case JConfigResource.Type.BROKER_LOGGER  => ConfigResourceType.BrokerLogger
        case JConfigResource.Type.BROKER         => ConfigResourceType.Broker
        case JConfigResource.Type.TOPIC          => ConfigResourceType.Topic
        case JConfigResource.Type.UNKNOWN        => ConfigResourceType.Unknown
        case JConfigResource.Type.CLIENT_METRICS => ConfigResourceType.ClientMetrics
        case JConfigResource.Type.GROUP          => ConfigResourceType.Group
      }
  }

  implicit class GroupStateAsJava(val gs: GroupState) extends AnyVal {
    def asJava: JGroupState =
      gs match {
        case GroupState.Unknown             => JGroupState.UNKNOWN
        case GroupState.PreparingRebalance  => JGroupState.PREPARING_REBALANCE
        case GroupState.CompletingRebalance => JGroupState.COMPLETING_REBALANCE
        case GroupState.Stable              => JGroupState.STABLE
        case GroupState.Dead                => JGroupState.DEAD
        case GroupState.Empty               => JGroupState.EMPTY
        case GroupState.Assigning           => JGroupState.ASSIGNING
        case GroupState.Reconciling         => JGroupState.RECONCILING
        case GroupState.NotReady            => JGroupState.NOT_READY
      }
  }
  implicit class JGroupStateAsScala(val jgs: JGroupState) extends AnyVal {
    def asScala: GroupState =
      jgs match {
        case JGroupState.UNKNOWN              => GroupState.Unknown
        case JGroupState.PREPARING_REBALANCE  => GroupState.PreparingRebalance
        case JGroupState.COMPLETING_REBALANCE => GroupState.CompletingRebalance
        case JGroupState.STABLE               => GroupState.Stable
        case JGroupState.DEAD                 => GroupState.Dead
        case JGroupState.EMPTY                => GroupState.Empty
        case JGroupState.ASSIGNING            => GroupState.Assigning
        case JGroupState.RECONCILING          => GroupState.Reconciling
        case JGroupState.NOT_READY            => GroupState.NotReady
      }
  }

  implicit class GroupTypeAsJava(val gt: GroupType) extends AnyVal {
    def asJava: JGroupType =
      gt match {
        case GroupType.Unknown  => JGroupType.UNKNOWN
        case GroupType.Consumer => JGroupType.CONSUMER
        case GroupType.Classic  => JGroupType.CLASSIC
        case GroupType.Share    => JGroupType.SHARE
        case GroupType.Streams  => JGroupType.STREAMS
      }
  }
  implicit class JGroupTypeAsScale(val jgt: JGroupType) extends AnyVal {
    def asScala: GroupType =
      jgt match {
        case JGroupType.UNKNOWN  => GroupType.Unknown
        case JGroupType.CONSUMER => GroupType.Consumer
        case JGroupType.CLASSIC  => GroupType.Classic
        case JGroupType.SHARE    => GroupType.Share
        case JGroupType.STREAMS  => GroupType.Streams
      }
  }

  implicit class JMemberDescriptionAsScala(val jmd: JMemberDescription) extends AnyVal {
    def asScala: MemberDescription =
      MemberDescription(
        consumerId = jmd.consumerId,
        groupInstanceId = jmd.groupInstanceId.toScala,
        clientId = jmd.clientId(),
        host = jmd.host(),
        assignment = jmd.assignment.topicPartitions().asScala.map(_.asScala).toSet
      )
  }

  implicit class JConsumerGroupDescriptionAsScala(val description: JConsumerGroupDescription) extends AnyVal {
    def asScala: ConsumerGroupDescription =
      ConsumerGroupDescription(
        groupId = description.groupId,
        isSimpleConsumerGroup = description.isSimpleConsumerGroup,
        members = description.members.asScala.map(_.asScala).toList,
        partitionAssignor = description.partitionAssignor,
        state = description.groupState.asScala,
        coordinator = description.coordinator().asScala,
        authorizedOperations = Option(description.authorizedOperations())
          .fold(Set.empty[AclOperation])(_.asScala.map(_.asScala).toSet)
      )
  }

  implicit class CreatePartitionsOptionsAsJava(val cpo: CreatePartitionsOptions) extends AnyVal {
    def asJava: JCreatePartitionsOptions = {
      val opts = new JCreatePartitionsOptions()
        .validateOnly(cpo.validateOnly)
        .retryOnQuotaViolation(cpo.retryOnQuotaViolation)
      cpo.timeout.fold(opts)(timeout => opts.timeoutMs(timeout.toMillis.toInt))
    }
  }

  implicit class CreateTopicsOptionsAsJava(val cto: CreateTopicsOptions) extends AnyVal {
    def asJava: JCreateTopicsOptions = {
      val opts = new JCreateTopicsOptions().validateOnly(cto.validateOnly)
      cto.timeout.fold(opts)(timeout => opts.timeoutMs(timeout.toMillis.toInt))
    }
  }

  implicit class DeleteConsumerGroupsOptionsAsJava(val cto: DeleteConsumerGroupsOptions) extends AnyVal {
    def asJava: JDeleteConsumerGroupsOptions = {
      val opts = new JDeleteConsumerGroupsOptions()
      cto.timeout.fold(opts)(timeout => opts.timeoutMs(timeout.toMillis.toInt))
    }
  }

  implicit class DeleteTopicsOptionsAsJava(val dto: DeleteTopicsOptions) extends AnyVal {
    def asJava: JDeleteTopicsOptions = {
      val opts = new JDeleteTopicsOptions().retryOnQuotaViolation(dto.retryOnQuotaViolation)
      dto.timeout.fold(opts)(timeout => opts.timeoutMs(timeout.toMillis.toInt))
    }
  }

  implicit class ListTopicsOptionsAsJava(val lto: ListTopicsOptions) extends AnyVal {
    def asJava: JListTopicsOptions = {
      val opts = new JListTopicsOptions().listInternal(lto.listInternal)
      lto.timeout.fold(opts)(timeout => opts.timeoutMs(timeout.toMillis.toInt))
    }
  }

  implicit class DescribeTopicsOptionsAsJava(val dto: DescribeTopicsOptions) extends AnyVal {
    def asJava: JDescribeTopicsOptions = {
      val opts = new JDescribeTopicsOptions().includeAuthorizedOperations(dto.includeAuthorizedOperations)
      dto.timeout.fold(opts)(timeout => opts.timeoutMs(timeout.toMillis.toInt))
    }
  }

  implicit class DescribeConfigsOptionsAsJava(val dco: DescribeConfigsOptions) extends AnyVal {
    def asJava: JDescribeConfigsOptions = {
      val opts = new JDescribeConfigsOptions()
        .includeSynonyms(dco.includeSynonyms)
        .includeDocumentation(dco.includeDocumentation)
      dco.timeout.fold(opts)(timeout => opts.timeoutMs(timeout.toMillis.toInt))
    }
  }

  implicit class DescribeClusterOptionsAsJava(val dco: DescribeClusterOptions) extends AnyVal {
    def asJava: JDescribeClusterOptions = {
      val opts = new JDescribeClusterOptions().includeAuthorizedOperations(dco.includeAuthorizedOperations)
      dco.timeout.fold(opts)(timeout => opts.timeoutMs(timeout.toMillis.toInt))
    }
  }

  implicit class DescribeConsumerGroupsOptionsAsJava(val dcgo: DescribeConsumerGroupsOptions) extends AnyVal {
    def asJava: JDescribeConsumerGroupsOptions = {
      val jOpts = new JDescribeConsumerGroupsOptions()
        .includeAuthorizedOperations(dcgo.includeAuthorizedOperations)
      dcgo.timeout.fold(jOpts)(timeout => jOpts.timeoutMs(timeout.toMillis.toInt))
    }
  }

  implicit class AlterConfigsOptionsAsJava(val aco: AlterConfigsOptions) extends AnyVal {
    def asJava: JAlterConfigsOptions = {
      val jOpts = new JAlterConfigsOptions().validateOnly(aco.validateOnly)
      aco.timeout.fold(jOpts)(timeout => jOpts.timeoutMs(timeout.toMillis.toInt))
    }
  }

  implicit class AlterConfigOpAsJava(val aco: AlterConfigOp) extends AnyVal {
    def asJava: JAlterConfigOp = new JAlterConfigOp(aco.configEntry, aco.opType.asJava)
  }

  implicit class AlterConfigOpTypeAsJava(val acot: AlterConfigOpType) extends AnyVal {
    def asJava: JAlterConfigOp.OpType =
      acot match {
        case AlterConfigOpType.Set      => JAlterConfigOp.OpType.SET
        case AlterConfigOpType.Delete   => JAlterConfigOp.OpType.DELETE
        case AlterConfigOpType.Append   => JAlterConfigOp.OpType.APPEND
        case AlterConfigOpType.Subtract => JAlterConfigOp.OpType.SUBTRACT
      }
  }

  implicit class JMetricNameAsScala(val jmn: JMetricName) extends AnyVal {
    def asScala: MetricName =
      MetricName(
        name = jmn.name(),
        group = jmn.group(),
        description = jmn.description(),
        tags = jmn.tags().asScala.toMap
      )
  }

  implicit class JMetricAsScala(val jm: JMetric) extends AnyVal {
    def asScala: Metric =
      Metric(name = jm.metricName().asScala, metricValue = jm.metricValue())
  }

  implicit class NewTopicAsJava(val nt: NewTopic) extends AnyVal {
    def asJava: JNewTopic = {
      val jn = new JNewTopic(nt.name, nt.numPartitions, nt.replicationFactor)

      if (nt.configs.nonEmpty) {
        val _ = jn.configs(nt.configs.asJava)
      }

      jn
    }
  }

  implicit class NewPartitionsAsJava(val np: NewPartitions) extends AnyVal {
    def asJava: JNewPartitions =
      if (np.newAssignments.isEmpty) JNewPartitions.increaseTo(np.totalCount)
      else JNewPartitions.increaseTo(np.totalCount, np.newAssignments.map(_.map(Int.box).asJava).asJava)
  }

  implicit class NodeAsJava(val n: Node) extends AnyVal {
    def asJava: JNode = new JNode(n.id, n.host.getOrElse(""), n.port.getOrElse(-1), n.rack.orNull)
  }

  /**
   * @param jn
   *   may be null
   */
  implicit class JNodeAsScala(val jn: JNode) extends AnyVal {
    def asScala: Option[Node] =
      Option(jn)
        .filter(_.id() >= 0)
        .map { jNode =>
          Node(
            id = jNode.id(),
            host = Option(jNode.host()).filterNot(_.isEmpty),
            port = Option(jNode.port()).filter(_ >= 0),
            rack = Option(jNode.rack())
          )
        }
  }

  implicit class JTopicDescriptionAsScala(val td: JTopicDescription) extends AnyVal {
    def asScala: Try[TopicDescription] = {
      val authorizedOperations = Option(td.authorizedOperations).map(_.asScala.toSet).map(_.map(_.asScala))

      td.partitions.asScala.toList.forEach(_.asScala).map { partitions =>
        TopicDescription(
          name = td.name,
          internal = td.isInternal,
          partitions = partitions,
          authorizedOperations = authorizedOperations
        )
      }
    }
  }

  implicit class TopicPartitionInfoAsJava(val tpi: TopicPartitionInfo) extends AnyVal {
    def asJava: JTopicPartitionInfo =
      new JTopicPartitionInfo(
        tpi.partition,
        tpi.leader.map(_.asJava).getOrElse(JNode.noNode()),
        tpi.replicas.map(_.asJava).asJava,
        tpi.isr.map(_.asJava).asJava
      )
  }
  implicit class JTopicPartitionInfoAsScala(val jtpi: JTopicPartitionInfo) extends AnyVal {
    def asScala: Try[TopicPartitionInfo] = {
      val replicas: Try[List[Node]] =
        jtpi
          .replicas()
          .asScala
          .toList
          .forEach { jNode =>
            jNode.asScala match {
              case Some(node) => Success(node)
              case None       => Failure(new RuntimeException("NoNode node not expected among topic replicas"))
            }
          }

      val inSyncReplicas: Try[List[Node]] =
        jtpi
          .isr()
          .asScala
          .toList
          .forEach { jNode =>
            jNode.asScala match {
              case Some(node) => Success(node)
              case None       => Failure(new RuntimeException("NoNode node not expected among topic in sync replicas"))
            }
          }

      for {
        replicas       <- replicas
        inSyncReplicas <- inSyncReplicas
      } yield TopicPartitionInfo(
        partition = jtpi.partition(),
        leader = jtpi.leader().asScala,
        replicas = replicas,
        isr = inSyncReplicas
      )
    }
  }

  implicit class TopicListingAsJava(val tl: TopicListing) extends AnyVal {
    def asJava: JTopicListing = new JTopicListing(tl.name, tl.topicId, tl.isInternal)
  }
  implicit class JTopicListingAsScala(val jtl: JTopicListing) extends AnyVal {
    def asScala: TopicListing = TopicListing(jtl.name(), jtl.topicId(), jtl.isInternal)
  }

  implicit class TopicPartitionAsJava(val tp: TopicPartition) extends AnyVal {
    def asJava: JTopicPartition = new JTopicPartition(tp.name, tp.partition)
  }

  implicit class JTopicPartitionAsScala(val jtp: JTopicPartition) extends AnyVal {
    def asScala: TopicPartition =
      TopicPartition(name = jtp.topic(), partition = jtp.partition())
  }

  implicit class OffsetSpecAsJava(val os: OffsetSpec) extends AnyVal {
    def asJava: JOffsetSpec =
      os match {
        case OffsetSpec.EarliestSpec             => JOffsetSpec.earliest()
        case OffsetSpec.LatestSpec               => JOffsetSpec.latest()
        case OffsetSpec.TimestampSpec(timestamp) => JOffsetSpec.forTimestamp(timestamp)
      }
  }

  implicit class IsolationLevelAsJava(val il: IsolationLevel) extends AnyVal {
    def asJava: JIsolationLevel =
      il match {
        case IsolationLevel.ReadUncommitted => JIsolationLevel.READ_UNCOMMITTED
        case IsolationLevel.ReadCommitted   => JIsolationLevel.READ_COMMITTED
      }
  }

  implicit class DeleteRecordsOptionsAsJava(val dro: DeleteRecordsOptions) extends AnyVal {
    def asJava: JDeleteRecordsOptions = {
      val deleteRecordsOpt = new JDeleteRecordsOptions()
      dro.timeout.fold(deleteRecordsOpt)(timeout => deleteRecordsOpt.timeoutMs(timeout.toMillis.toInt))
    }
  }

  implicit class ListOffsetsOptionsAsJava(val loo: ListOffsetsOptions) extends AnyVal {
    def asJava: JListOffsetsOptions = {
      val offsetOpt = new JListOffsetsOptions(loo.isolationLevel.asJava)
      loo.timeout.fold(offsetOpt)(timeout => offsetOpt.timeoutMs(timeout.toMillis.toInt))
    }
  }

  implicit class JListOffsetsResultInfoAsScala(val jlori: JListOffsetsResultInfo) extends AnyVal {
    def asScala: ListOffsetsResultInfo =
      ListOffsetsResultInfo(jlori.offset(), jlori.timestamp(), jlori.leaderEpoch().toScala.map(_.toInt))
  }

  implicit class JListConsumerGroupOffsetsOptionsAsJava(val lcgoo: ListConsumerGroupOffsetsOptions) extends AnyVal {
    def asJava: JListConsumerGroupOffsetsOptions = {
      val opts = new JListConsumerGroupOffsetsOptions()
      opts.requireStable(lcgoo.requireStable)
    }
  }

  implicit class ListConsumerGroupOffsetsSpecAsScala(val lcgos: ListConsumerGroupOffsetsSpec) extends AnyVal {
    def asJava: JListConsumerGroupOffsetsSpec = {
      val opts = new JListConsumerGroupOffsetsSpec
      opts.topicPartitions(lcgos.partitions.map(_.asJava).asJava)
      opts
    }
  }

  implicit class OffsetAndMetadataAsJava(val oam: OffsetAndMetadata) extends AnyVal {
    def asJava: JOffsetAndMetadata =
      new JOffsetAndMetadata(oam.offset, oam.leaderEpoch.map(Int.box).toJava, oam.metadata.orNull)
  }
  implicit class JOffsetAndMetadataAsScala(val joam: JOffsetAndMetadata) extends AnyVal {
    def asScala: OffsetAndMetadata =
      OffsetAndMetadata(
        offset = joam.offset(),
        leaderEpoch = joam.leaderEpoch().toScala.map(_.toInt),
        metadata = Some(joam.metadata())
      )
  }

  implicit class AlterConsumerGroupOffsetsOptionsAsJava(val acgoo: AlterConsumerGroupOffsetsOptions) extends AnyVal {
    def asJava: JAlterConsumerGroupOffsetsOptions = {
      val options = new JAlterConsumerGroupOffsetsOptions()
      acgoo.timeout.fold(options)(timeout => options.timeoutMs(timeout.toMillis.toInt))
    }
  }

  implicit class ListConsumerGroupsOptionsAsJava(val lcgo: ListConsumerGroupsOptions) extends AnyVal {
    def asJListGroupsOptions: JListGroupsOptions =
      JListGroupsOptions.forConsumerGroups().inGroupStates(lcgo.states.map(_.asJava).asJava)
  }

  implicit class JGroupListingAsScala(val jgl: JGroupListing) extends AnyVal {
    def asConsumerGroupListing: ConsumerGroupListing =
      ConsumerGroupListing(
        groupId = jgl.groupId(),
        isSimple = jgl.isSimpleConsumerGroup,
        state = jgl.groupState().toScala.map(_.asScala)
      )

    def asScala: GroupListing =
      GroupListing(
        jgl.groupId(),
        jgl.`type`().toScala.map(_.asScala),
        jgl.protocol(),
        jgl.groupState().toScala.map(_.asScala)
      )
  }

  implicit class ListGroupsOptionsAsJava(val lgo: ListGroupsOptions) extends AnyVal {
    def asJava: JListGroupsOptions =
      new JListGroupsOptions()
        .inGroupStates(lgo.groupStates.map(_.asJava).asJava)
        .withTypes(lgo.groupTypes.map(_.asJava).asJava)
        .withProtocolTypes(lgo.protocolTypes.asJava)
  }

  implicit class JListGroupsOptionsAsScala(val jlgo: JListGroupsOptions) extends AnyVal {
    def asScala: ListGroupsOptions =
      ListGroupsOptions(
        jlgo.groupStates().asScala.map(_.asScala).toSet,
        jlgo.types().asScala.map(_.asScala).toSet,
        jlgo.protocolTypes().asScala.toSet
      )
  }

  implicit class KafkaConfigAsJava(val kc: KafkaConfig) extends AnyVal {
    def asJava: JConfig = new JConfig(kc.entries.values.asJavaCollection)
  }
  implicit class JConfigAsScala(val jc: JConfig) extends AnyVal {
    def asScala: KafkaConfig =
      KafkaConfig(entries = jc.entries().asScala.map(e => e.name() -> e).toMap)
  }

  implicit class JLogDirDescriptionAsScala(val jldd: JLogDirDescription) extends AnyVal {
    def asScala: LogDirDescription =
      LogDirDescription(
        error = jldd.error(),
        replicaInfos = jldd.replicaInfos().asScala.bimap(_.asScala, _.asScala).toMap
      )
  }

  implicit class JReplicaInfoAsScala(val jri: JReplicaInfo) extends AnyVal {
    def asScala: ReplicaInfo =
      ReplicaInfo(size = jri.size(), offsetLag = jri.offsetLag(), isFuture = jri.isFuture)
  }

  implicit class AclOperationAsScala(val ao: AclOperation) extends AnyVal {
    def asJava: JAclOperation =
      ao match {
        case AclOperation.Unknown         => JAclOperation.UNKNOWN
        case AclOperation.Any             => JAclOperation.ANY
        case AclOperation.All             => JAclOperation.ALL
        case AclOperation.Read            => JAclOperation.READ
        case AclOperation.Write           => JAclOperation.WRITE
        case AclOperation.Create          => JAclOperation.CREATE
        case AclOperation.Delete          => JAclOperation.DELETE
        case AclOperation.Alter           => JAclOperation.ALTER
        case AclOperation.Describe        => JAclOperation.DESCRIBE
        case AclOperation.ClusterAction   => JAclOperation.CLUSTER_ACTION
        case AclOperation.DescribeConfigs => JAclOperation.DESCRIBE_CONFIGS
        case AclOperation.AlterConfigs    => JAclOperation.ALTER_CONFIGS
        case AclOperation.IdempotentWrite => JAclOperation.IDEMPOTENT_WRITE
        case AclOperation.CreateTokens    => JAclOperation.CREATE_TOKENS
        case AclOperation.DescribeTokens  => JAclOperation.DESCRIBE_TOKENS
        case AclOperation.TwoPhaseCommit  => JAclOperation.TWO_PHASE_COMMIT
      }
  }
  implicit class JAclOperationAsJava(val jao: JAclOperation) extends AnyVal {
    def asScala: AclOperation =
      jao match {
        case JAclOperation.UNKNOWN          => AclOperation.Unknown
        case JAclOperation.ANY              => AclOperation.Any
        case JAclOperation.ALL              => AclOperation.All
        case JAclOperation.READ             => AclOperation.Read
        case JAclOperation.WRITE            => AclOperation.Write
        case JAclOperation.CREATE           => AclOperation.Create
        case JAclOperation.DELETE           => AclOperation.Delete
        case JAclOperation.ALTER            => AclOperation.Alter
        case JAclOperation.DESCRIBE         => AclOperation.Describe
        case JAclOperation.CLUSTER_ACTION   => AclOperation.ClusterAction
        case JAclOperation.DESCRIBE_CONFIGS => AclOperation.DescribeConfigs
        case JAclOperation.ALTER_CONFIGS    => AclOperation.AlterConfigs
        case JAclOperation.IDEMPOTENT_WRITE => AclOperation.IdempotentWrite
        case JAclOperation.CREATE_TOKENS    => AclOperation.CreateTokens
        case JAclOperation.DESCRIBE_TOKENS  => AclOperation.DescribeTokens
        case JAclOperation.TWO_PHASE_COMMIT => AclOperation.TwoPhaseCommit
      }
  }

}
