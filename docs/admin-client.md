---
id: admin-client
title: "Admin Client"
---

`AdminClient` manages a Kafka cluster from ZIO: it creates and deletes topics, reads and changes configuration, inspects and resets consumer groups, lists offsets, describes the cluster, and manages access control lists (ACLs). It wraps the Java [`Admin`](https://kafka.apache.org/documentation/#adminapi) client, turns every Kafka future into a ZIO effect, and replaces Java collections and nullable values with Scala collections, `Option` and small case classes. Unlike the Java client, a write operation (for example creating a topic) completes only after its effect is visible, wherever that is possible; see [Write Operations Wait Until They Are Visible](#write-operations-wait-until-they-are-visible). The main types are `AdminClient`, `AdminClientSettings`, the model case classes nested in the `AdminClient` companion object (`NewTopic`, `ConfigResource`, `OffsetSpec`, and so on), and the ACL types in the `zio.kafka.admin.acl` and `zio.kafka.admin.resource` packages.

The shape of the two entry points is:

```scala
trait AdminClient {
  def createTopic(newTopic: AdminClient.NewTopic, validateOnly: Boolean = false): Task[Unit]
  def listTopics(listTopicsOptions: Option[AdminClient.ListTopicsOptions] = None): Task[Map[String, AdminClient.TopicListing]]
  // ... 36 more operations
}

final case class AdminClientSettings(closeTimeout: Duration, properties: Map[String, AnyRef])
```

## Write Operations Wait Until They Are Visible

The Java admin client completes a write operation as soon as the broker accepts it, but the change is not yet visible to the next read: a topic that was just created may be missing from `listTopics` for a while (see [KAFKA-18818](https://issues.apache.org/jira/browse/KAFKA-18818)). Zio-kafka hides this. Its write operations only complete once the change is visible, using one of two strategies:

- **Polling:** after the broker accepts the request, zio-kafka waits 50 ms and then repeatedly reads the state back, with exponential backoff starting at 50 ms, until the change shows up. If it does not show up within 5 seconds, the operation still succeeds: zio-kafka assumes that the write succeeded and was undone by another write.
- **Fixed sleep:** where polling is impractical, the operation sleeps for its `visibilitySleep` parameter (default 1 second) after the broker accepts the request. Pass `Duration.Zero` when you don't care when the operation takes effect.

The table lists which operations wait and how:

| Operation                                                             | How it waits                                                                                                                    |
| --------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------- |
| `createTopic`, `createTopics`                                         | Polls `listTopics` until every new topic is listed                                                                              |
| `deleteTopic`, `deleteTopics`                                         | Polls `listTopics` until no deleted topic is listed                                                                             |
| `createPartitions`                                                    | Polls `describeTopics` until each topic has at least the requested partition count                                              |
| `deleteConsumerGroups`                                                | Polls `listConsumerGroups` until no deleted group is listed                                                                     |
| `alterConsumerGroupOffsets`                                           | Polls `listConsumerGroupOffsets` until every partition shows the new offset                                                     |
| `incrementalAlterConfigs`, `incrementalAlterConfigsAsync`             | Polls `describeConfigs` for `Set`/`Delete` ops; sleeps `visibilitySleep` when the request contains an `Append` or `Subtract` op |
| `createAcls`, `createAclsAsync`                                       | Polls `describeAcls` until every new binding is listed                                                                          |
| `deleteAcls`, `deleteAclsAsync`                                       | Polls `describeAcls` with each filter until it matches nothing                                                                  |
| `deleteRecords`                                                       | Sleeps `visibilitySleep`                                                                                                        |
| `removeMembersFromConsumerGroup`, `removeAllMembersFromConsumerGroup` | Sleeps `visibilitySleep`                                                                                                        |

A request made with `validateOnly = true` changes nothing, so `createTopic`, `createTopics`, `createPartitions`, `incrementalAlterConfigs` and `incrementalAlterConfigsAsync` skip the waiting when `validateOnly` is set and return as soon as the broker has validated the request. The waiting uses the ZIO `Clock`; in a ZIO Test spec that uses `TestClock`, add `@@ TestAspect.withLiveClock` (see [Test Producers and Consumers with the Test Kit](writing-tests.md)).

## Creating an Admin Client

An `AdminClient` holds a network connection that must be closed when you are done with it. `AdminClient.make` and `AdminClient.live` close it automatically when their scope ends. With `AdminClient.fromScopedJavaClient`, the scoped effect you pass must close the Java client, for example by creating it with `ZIO.acquireRelease`. With `AdminClient.fromJavaClient` you close the Java client yourself. All constructors are in the `AdminClient` companion object:

```scala
object AdminClient {
  def make(settings: AdminClientSettings): ZIO[Scope, Throwable, AdminClient]
  val live: ZLayer[AdminClientSettings, Throwable, AdminClient]
  def fromJavaClient(javaClient: org.apache.kafka.clients.admin.Admin): URIO[Any, AdminClient]
  def fromScopedJavaClient[R, E](scopedJavaClient: ZIO[R & Scope, E, Admin]): ZIO[R & Scope, E, AdminClient]
  def javaClientFromSettings(settings: AdminClientSettings): ZIO[Scope, Throwable, Admin]
}
```

`AdminClient.make` is the usual entry point. It runs zio-kafka's SSL endpoint check (see [Security](security.md)), creates the Java client, and closes it, waiting at most `AdminClientSettings#closeTimeout`, when the scope closes. To create a topic and then list all topic names inside one scope:

```scala mdoc:compile-only
import zio._
import zio.kafka.admin.{ AdminClient, AdminClientSettings }
import zio.kafka.admin.AdminClient.NewTopic

val bootstrapServers = List("localhost:9092")

val topicNames: ZIO[Any, Throwable, Set[String]] =
  ZIO.scoped {
    for {
      admin <- AdminClient.make(AdminClientSettings(bootstrapServers))
      _ <- admin.createTopic(
             NewTopic(
               name = "orders",
               numPartitions = 6,
               replicationFactor = 3,
               configs = Map("cleanup.policy" -> "compact")
             )
           )
      topics <- admin.listTopics()
    } yield topics.keySet
  }
```

Alternatively, you can provide an `AdminClient` as a ZIO layer. `AdminClient.live` builds the client from an `AdminClientSettings` in the environment, so the rest of the application can depend on `AdminClient` as a service:

```scala mdoc:compile-only
import zio._
import zio.kafka.admin.{ AdminClient, AdminClientSettings }

val bootstrapServers = List("localhost:9092")

val adminLayer: ZLayer[Any, Throwable, AdminClient] =
  ZLayer.succeed(AdminClientSettings(bootstrapServers)) >>> AdminClient.live

val clusterId: ZIO[AdminClient, Throwable, String] =
  ZIO.serviceWithZIO[AdminClient](_.describeClusterId())
```

The other constructors work with a Java `Admin` client:

- `AdminClient.fromJavaClient(javaClient)` wraps a Java client that you created. You must close the Java client yourself.
- `AdminClient.fromScopedJavaClient(effect)` wraps the Java client that a scoped effect creates. It does not close the client itself: create the client with `ZIO.acquireRelease` in that effect so that it is closed when the scope ends.
- `AdminClient.javaClientFromSettings(settings)` creates a Java client from settings and closes it when the scope ends. `AdminClient.make` uses it. Use it when you also need the Java client, for example for an operation that zio-kafka does not wrap.

`AdminClient.fromJavaClient` and `AdminClient.fromScopedJavaClient` do not run the [SSL endpoint check](security.md#ssl-endpoint-check); `AdminClient.javaClientFromSettings` (and so `AdminClient.make`) does.

## AdminClientSettings

`AdminClientSettings` holds the Java client properties and the close timeout:

```scala
final case class AdminClientSettings(closeTimeout: Duration, properties: Map[String, AnyRef]) {
  def driverSettings: Map[String, AnyRef]
  def withBootstrapServers(servers: List[String]): AdminClientSettings
  def withProperty(key: String, value: AnyRef): AdminClientSettings
  def withProperties(kvs: (String, AnyRef)*): AdminClientSettings
  def withProperties(kvs: Map[String, AnyRef]): AdminClientSettings
  def withCredentials(credentialsStore: KafkaCredentialStore): AdminClientSettings
}

object AdminClientSettings {
  def apply(bootstrapServers: List[String]): AdminClientSettings
}
```

Construct it with `AdminClientSettings.apply(bootstrapServers)`, which sets a close timeout of 30 seconds and no other property, and refine it with the `with*` methods, which keeps your code source compatible with future releases:

- `AdminClientSettings#withBootstrapServers` replaces the `bootstrap.servers` property.
- `AdminClientSettings#withProperty` and the two `AdminClientSettings#withProperties` overloads set any [Java admin client configuration](https://kafka.apache.org/documentation/#adminclientconfigs); a property set later replaces one with the same key set earlier.
- `AdminClientSettings#withCredentials` adds the TLS properties of a `KafkaCredentialStore` (see [Security](security.md)).

The close timeout has no `with` method; change it with `copy`. `AdminClientSettings#driverSettings` returns the properties passed to the Java client, the same map as `properties`. To set a client id and a shorter request timeout, and to close faster:

```scala mdoc:compile-only
import org.apache.kafka.clients.admin.AdminClientConfig
import zio._
import zio.kafka.admin.AdminClientSettings

val settings: AdminClientSettings =
  AdminClientSettings(List("localhost:9092"))
    .withProperty(AdminClientConfig.CLIENT_ID_CONFIG, "ops-tool")
    .withProperties(AdminClientConfig.REQUEST_TIMEOUT_MS_CONFIG -> "15000")
    .copy(closeTimeout = 10.seconds)
```

## How the Types Work Together

Every operation takes zio-kafka model values and returns zio-kafka model values; apart from a few Java classes such as `ConfigEntry` and `RecordsToDelete`, the Java types stay inside the wrapper. Many operations also accept an options value with operation-specific flags, usually as `Option[...Options] = None` (see [Options](#options)). The types connect as follows:

```
AdminClientSettings ──> AdminClient.make / AdminClient.live ──> AdminClient
                                                                   │
  NewTopic, NewPartitions ─────────────> topics ───────────────────┤──> TopicListing, TopicDescription
  ConfigResource + AlterConfigOp ──────> configs ──────────────────┤──> KafkaConfig (entries: ConfigEntry)
  groupId, ListConsumerGroupOffsetsSpec > consumer groups ─────────┤──> ConsumerGroupListing, ConsumerGroupDescription,
                                                                   │    OffsetAndMetadata
  TopicPartition + OffsetSpec ─────────> offsets ──────────────────┤──> ListOffsetsResultInfo
  DescribeClusterOptions ──────────────> cluster ──────────────────┤──> Node, cluster id, AclOperation
  AclBinding, AclBindingFilter ────────> ACLs ─────────────────────┘──> AclBinding
```

A typical admin task follows the same steps:

1. Build `AdminClientSettings` and create the client with `AdminClient.make` or `AdminClient.live`.
2. Describe the target with model values: a `NewTopic`, a `ConfigResource`, a `TopicPartition` with an `OffsetSpec`, an `AclBinding`.
3. Call the operation. Write operations return once the change is visible; read operations return Scala collections of model values, most often a `Map` keyed by topic name, `TopicPartition`, `ConfigResource`, group id or broker id.
4. Close the scope, which closes the client.

:::note[Name clashes]
Most model types in the `AdminClient` companion, for example `TopicPartition`, `OffsetAndMetadata`, `Node`, `NewTopic`, `ConfigResource`, `MetricName`, `IsolationLevel` and the `*Options` classes, have the same name as a Java class in `org.apache.kafka.clients.admin` or `org.apache.kafka.common`. Import the model types you need by name, for example `import zio.kafka.admin.AdminClient.{ NewTopic, TopicPartition }`, rather than with a wildcard.
:::

## Options

The options classes carry a request timeout and operation-specific flags. The classes are nested in the `AdminClient` companion, except the three ACL ones, which are in `zio.kafka.admin.acl`:

| Options class                      | Fields (default)                                                         | Used by                                                   |
| ---------------------------------- | ------------------------------------------------------------------------ | --------------------------------------------------------- |
| `CreateTopicsOptions`              | `validateOnly`, `timeout`                                                | `createTopics`                                            |
| `CreatePartitionsOptions`          | `validateOnly` (`false`), `retryOnQuotaViolation` (`true`), `timeout`    | `createPartitions`                                        |
| `DeleteTopicsOptions`              | `retryOnQuotaViolation` (`true`), `timeout`                              | `deleteTopics`                                            |
| `DeleteRecordsOptions`             | `timeout`                                                                | `deleteRecords`                                           |
| `ListTopicsOptions`                | `listInternal` (`false`), `timeout`                                      | `listTopics`                                              |
| `DescribeTopicsOptions`            | `includeAuthorizedOperations`, `timeout`                                 | `describeTopics`                                          |
| `DescribeConfigsOptions`           | `includeSynonyms` (`false`), `includeDocumentation` (`false`), `timeout` | `describeConfigs`, `describeConfigsAsync`                 |
| `AlterConfigsOptions`              | `validateOnly` (`false`), `timeout` (`None`)                             | `incrementalAlterConfigs`, `incrementalAlterConfigsAsync` |
| `DescribeClusterOptions`           | `includeAuthorizedOperations`, `timeout`                                 | `describeClusterNodes` and the other `describeCluster*`   |
| `ListOffsetsOptions`               | `isolationLevel` (`IsolationLevel.ReadUncommitted`), `timeout`           | `listOffsets`, `listOffsetsAsync`                         |
| `ListConsumerGroupsOptions`        | `states`                                                                 | `listConsumerGroups`                                      |
| `ListGroupsOptions`                | `groupStates`, `groupTypes`, `protocolTypes` (all `Set.empty`)           | `listGroups`                                              |
| `DescribeConsumerGroupsOptions`    | `includeAuthorizedOperations`, `timeout`                                 | `describeConsumerGroups`                                  |
| `ListConsumerGroupOffsetsOptions`  | `requireStable`                                                          | `listConsumerGroupOffsets`                                |
| `AlterConsumerGroupOffsetsOptions` | `timeout`                                                                | `alterConsumerGroupOffsets`                               |
| `DeleteConsumerGroupOptions`       | `timeout`                                                                | `deleteConsumerGroups`                                    |
| `CreateAclOptions`                 | `timeout`                                                                | `createAcls`, `createAclsAsync`                           |
| `DeleteAclsOptions`                | `timeout`                                                                | `deleteAcls`, `deleteAclsAsync`                           |
| `DescribeAclOptions`               | `timeout`                                                                | `describeAcls`                                            |

`timeout` is an `Option[Duration]`; `None` uses the client's `default.api.timeout.ms`. Except in `AlterConfigsOptions`, the `timeout` field has no default value, so pass `timeout = None` explicitly when you only want to set a flag. `ListConsumerGroupOffsetsOptions#requireStable` asks for stable offsets only: offsets that a still-open transaction commits are not returned until the transaction completes. `ListConsumerGroupsOptions#states` and `ListGroupsOptions#groupStates` filter by `GroupState`, and an empty set lists groups in every state. Most operations take the options as `Option[...Options] = None`; the `describeConsumerGroups` overload with options takes an `Option` without a default, `incrementalAlterConfigs`, `incrementalAlterConfigsAsync` and one `listConsumerGroupOffsets` overload take them without the `Option`, and `createTopic`, `deleteTopic`, `describeLogDirs`, `describeLogDirsAsync`, `metrics` and the `remove*MembersFromConsumerGroup` operations take none. To describe a topic's configuration including the synonyms of every entry:

```scala mdoc:compile-only
import zio._
import zio.kafka.admin.AdminClient
import zio.kafka.admin.AdminClient.{ ConfigResource, ConfigResourceType, DescribeConfigsOptions, KafkaConfig }

def describeWithSynonyms(admin: AdminClient, topic: String): Task[Map[ConfigResource, KafkaConfig]] =
  admin.describeConfigs(
    List(ConfigResource(ConfigResourceType.Topic, topic)),
    Some(DescribeConfigsOptions(includeSynonyms = true, timeout = None))
  )
```

## Operations by Task

The operations group naturally by the part of the cluster they manage. Every operation returns a `Task` (`ZIO[Any, Throwable, *]`), and a Kafka error, such as a `TopicExistsException` or a `GroupNotEmptyException`, becomes the failure of that task. The sections below show short examples per group; for the full parameter list of every operation, see the [`AdminClient` API docs](https://javadoc.io/doc/dev.zio/zio-kafka_2.13/latest/zio/kafka/admin/AdminClient.html), and for the model case classes, the [`AdminClient` companion API docs](https://javadoc.io/doc/dev.zio/zio-kafka_2.13/latest/zio/kafka/admin/AdminClient$.html).

### Topics

`AdminClient#createTopic`, `AdminClient#createTopics`, `AdminClient#deleteTopic`, `AdminClient#deleteTopics`, `AdminClient#listTopics`, `AdminClient#describeTopics`, `AdminClient#createPartitions` and `AdminClient#deleteRecords` create, inspect and remove topics and their data. A `NewTopic` describes a topic to create and `NewPartitions` raises a topic's partition count; `listTopics` leaves out internal topics unless `ListTopicsOptions#listInternal` is set, and `TopicDescription#authorizedOperations` is `None` unless the request set `includeAuthorizedOperations = true`. `deleteRecords` takes the Java `RecordsToDelete` class, for example `RecordsToDelete.beforeOffset(offset)`.

To add partitions to an existing topic and then read back its partition leaders:

```scala mdoc:compile-only
import zio._
import zio.kafka.admin.AdminClient
import zio.kafka.admin.AdminClient.NewPartitions

def growTopic(admin: AdminClient, topic: String, partitions: Int): Task[Map[Int, Option[Int]]] =
  for {
    _            <- admin.createPartitions(Map(topic -> NewPartitions(totalCount = partitions)))
    descriptions <- admin.describeTopics(List(topic))
  } yield descriptions(topic).partitions.map(p => p.partition -> p.leader.map(_.id)).toMap
```

### Configuration

`AdminClient#describeConfigs` and `AdminClient#incrementalAlterConfigs`, and their `*Async` variants, read and change the configuration of topics, brokers and other resources. A `ConfigResource` names the resource; for a broker, its name is the broker id as a string, for example `"0"`. Each change is an `AlterConfigOp`: `AlterConfigOpType.Set` sets a value, `Delete` reverts it to the default, and `Append` and `Subtract` add to or remove from a list-valued config. The `AlterConfigsOptions` parameter of `incrementalAlterConfigs` is required, but `AlterConfigsOptions()` is enough in most cases. The result, a `KafkaConfig`, maps each config name to a Java `ConfigEntry`.

To set a topic's retention and read the new value back:

```scala mdoc:compile-only
import org.apache.kafka.clients.admin.ConfigEntry
import zio._
import zio.kafka.admin.AdminClient
import zio.kafka.admin.AdminClient.{ AlterConfigOp, AlterConfigOpType, AlterConfigsOptions, ConfigResource, ConfigResourceType }

def setRetention(admin: AdminClient, topic: String, retention: Duration): Task[Option[String]] = {
  val resource = ConfigResource(ConfigResourceType.Topic, topic)
  val setOp    = AlterConfigOp(new ConfigEntry("retention.ms", retention.toMillis.toString), AlterConfigOpType.Set)
  for {
    _       <- admin.incrementalAlterConfigs(Map(resource -> List(setOp)), AlterConfigsOptions())
    configs <- admin.describeConfigs(List(resource))
  } yield configs.get(resource).flatMap(_.entries.get("retention.ms")).map(_.value())
}
```

Because `incrementalAlterConfigs` polls until the `Set` is visible, the `describeConfigs` call that follows already sees the new value.

### Consumer Groups

`AdminClient#listConsumerGroups`, `AdminClient#listGroups`, `AdminClient#describeConsumerGroups`, `AdminClient#listConsumerGroupOffsets`, `AdminClient#alterConsumerGroupOffsets`, `AdminClient#deleteConsumerGroups` and the `AdminClient#removeMembersFromConsumerGroup` and `AdminClient#removeAllMembersFromConsumerGroup` operations list, inspect, reset and delete consumer groups:

```scala
trait AdminClient {
  def listConsumerGroups(options: Option[ListConsumerGroupsOptions] = None): Task[List[ConsumerGroupListing]]
  def listGroups(options: Option[ListGroupsOptions] = None): Task[List[GroupListing]]
  def describeConsumerGroups(groupIds: String*): Task[Map[String, ConsumerGroupDescription]]
  def describeConsumerGroups(groupIds: List[String], options: Option[DescribeConsumerGroupsOptions]): Task[Map[String, ConsumerGroupDescription]]
  def listConsumerGroupOffsets(groupId: String, options: Option[ListConsumerGroupOffsetsOptions] = None): Task[Map[TopicPartition, OffsetAndMetadata]]
  def listConsumerGroupOffsets(groupSpecs: Map[String, ListConsumerGroupOffsetsSpec]): Task[Map[String, Map[TopicPartition, OffsetAndMetadata]]]
  def listConsumerGroupOffsets(groupSpecs: Map[String, ListConsumerGroupOffsetsSpec], options: ListConsumerGroupOffsetsOptions): Task[Map[String, Map[TopicPartition, OffsetAndMetadata]]]
  def alterConsumerGroupOffsets(groupId: String, offsets: Map[TopicPartition, OffsetAndMetadata], options: Option[AlterConsumerGroupOffsetsOptions] = None): Task[Unit]
  def deleteConsumerGroups(groupIds: Iterable[String], options: Option[DeleteConsumerGroupOptions] = None): Task[Unit]
  def removeMembersFromConsumerGroup(groupId: String, membersToRemove: Set[String], visibilitySleep: Duration = 1.second): Task[Unit]
  def removeMembersFromConsumerGroup(groupId: String): Task[Unit]
  def removeAllMembersFromConsumerGroup(groupId: String, visibilitySleep: Duration = 1.second): Task[Unit]
}
```

`listConsumerGroups` returns consumer groups only, while `listGroups` returns groups of every type. `describeConsumerGroups` takes the group ids either as varargs or as a list with options; set `DescribeConsumerGroupsOptions#includeAuthorizedOperations` to fill `ConsumerGroupDescription#authorizedOperations`, which is otherwise empty. The single-group `listConsumerGroupOffsets` returns every committed offset of one group. The two overloads that take a `Map[String, ListConsumerGroupOffsetsSpec]` look up several groups in one request; a `ListConsumerGroupOffsetsSpec(partitions)` lists the partitions to look up for that group, and the result is keyed by group id. As always in Kafka, a committed `OffsetAndMetadata` holds the offset of the *next* record the group will read.

A group must be empty before `deleteConsumerGroups` can delete it, and must have no active members before `alterConsumerGroupOffsets` can change its offsets. `removeMembersFromConsumerGroup(groupId, membersToRemove)` removes static members by their group instance id. `removeAllMembersFromConsumerGroup` removes every member of the group, dynamic members included, and `removeMembersFromConsumerGroup(groupId)` is an alias for it.

A group's state is a `GroupState`: `Unknown`, `PreparingRebalance`, `CompletingRebalance`, `Stable`, `Dead`, `Empty`, `Assigning`, `Reconciling` or `NotReady`. `GroupListing#groupType` is a `GroupType`: `Unknown`, `Consumer`, `Classic`, `Share` or `Streams`. Both are sealed traits in the `AdminClient` companion.

To compute the lag of every partition a consumer group has committed offsets for, combine the group's committed offsets with the partitions' end offsets:

```scala mdoc:compile-only
import zio._
import zio.kafka.admin.AdminClient
import zio.kafka.admin.AdminClient.{ OffsetSpec, TopicPartition }

def consumerLag(admin: AdminClient, groupId: String): Task[Map[TopicPartition, Long]] =
  for {
    committed  <- admin.listConsumerGroupOffsets(groupId)
    endOffsets <- admin.listOffsets(committed.map { case (tp, _) => tp -> OffsetSpec.LatestSpec })
  } yield committed.flatMap { case (tp, committedOffset) =>
    endOffsets.get(tp).map(end => tp -> (end.offset - committedOffset.offset))
  }
```

To read the committed offset of one partition for two groups in one request:

```scala mdoc:compile-only
import zio._
import zio.kafka.admin.AdminClient
import zio.kafka.admin.AdminClient.{ ListConsumerGroupOffsetsSpec, OffsetAndMetadata, TopicPartition }

def offsetsOfTwoGroups(admin: AdminClient): Task[Map[String, Map[TopicPartition, OffsetAndMetadata]]] = {
  val spec = ListConsumerGroupOffsetsSpec(Chunk(TopicPartition("orders", 0)))
  admin.listConsumerGroupOffsets(Map("billing" -> spec, "shipping" -> spec))
}
```

To move a group back to an offset, for example to reprocess a partition after a bug fix, stop all members of the group first and then alter its offsets:

```scala mdoc:compile-only
import zio._
import zio.kafka.admin.AdminClient
import zio.kafka.admin.AdminClient.{ OffsetAndMetadata, TopicPartition }

def rewindPartition(admin: AdminClient, groupId: String, partition: TopicPartition, offset: Long): Task[Unit] =
  admin.alterConsumerGroupOffsets(groupId, Map(partition -> OffsetAndMetadata(offset)))
```

### Offsets

`AdminClient#listOffsets` and `AdminClient#listOffsetsAsync` look up offsets in the partitions themselves, independently of any consumer group. Each partition, given as the zio-kafka admin `TopicPartition` case class rather than the Java one, comes with an `OffsetSpec`: `OffsetSpec.EarliestSpec` (the first offset still in the log), `OffsetSpec.LatestSpec` (the offset the next produced record will get), or `OffsetSpec.TimestampSpec(timestamp)` (the first offset whose timestamp is at or after `timestamp`, in epoch milliseconds). The isolation level in `ListOffsetsOptions` defaults to `IsolationLevel.ReadUncommitted`; with `IsolationLevel.ReadCommitted`, `LatestSpec` returns the last stable offset, which excludes records of open transactions, so use it for topics written by a [transactional producer](transactional-producer.md).

### Cluster

The `describeCluster*` operations (`AdminClient#describeClusterNodes`, `AdminClient#describeClusterController`, `AdminClient#describeClusterId` and `AdminClient#describeClusterAuthorizedOperations`), `AdminClient#describeLogDirs` and `AdminClient#metrics` describe the brokers, their log directories and the client itself. `describeClusterAuthorizedOperations` returns an empty set unless `DescribeClusterOptions#includeAuthorizedOperations` is set. A `Node` describes a broker, with `Option` fields where Kafka uses empty or negative placeholders. `metrics` returns the Java admin client's own metrics; see [Metrics](metrics.md) for the consumer and producer metrics zio-kafka publishes.

### Access Control Lists

`describeAcls`, `createAcls` and `deleteAcls` (the last two also have `*Async` variants) need a broker with an authorizer configured; `deleteAcls` returns the bindings it deleted. They use the types from the `zio.kafka.admin.acl` and `zio.kafka.admin.resource` packages. An ACL is an `AclBinding(pattern, entry)` that binds a resource pattern to an access control entry:

```
AclBinding ──> ResourcePattern(resourceType, name, patternType)
           └─> AccessControlEntry(principal, host, operation, permissionType)

AclBindingFilter ──> ResourcePatternFilter(resourceType, name, patternType)
                 └─> AccessControlEntryFilter(principal, host, operation, permissionType)
```

A `PatternType.Literal` pattern matches the exact name (or `*` for all), and `PatternType.Prefixed` matches every name that starts with it. The `principal` is a Kafka principal such as `"User:alice"` (or `"User:*"`), and `host` is a client host, or `"*"` for any host.

The enumerations are sealed traits with one case object per value:

| Type                | Package                    | Values                                                                                                                                                                                                       |
| ------------------- | -------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| `ResourceType`      | `zio.kafka.admin.resource` | `Topic`, `Group`, `Cluster`, `TransactionalId`, `DelegationToken`, `User`, `Any`, `Unknown`                                                                                                                  |
| `PatternType`       | `zio.kafka.admin.resource` | `Literal`, `Prefixed`, `Match`, `Any`, `Unknown`                                                                                                                                                             |
| `AclOperation`      | `zio.kafka.admin.acl`      | `All`, `Read`, `Write`, `Create`, `Delete`, `Alter`, `Describe`, `ClusterAction`, `DescribeConfigs`, `AlterConfigs`, `IdempotentWrite`, `CreateTokens`, `DescribeTokens`, `TwoPhaseCommit`, `Any`, `Unknown` |
| `AclPermissionType` | `zio.kafka.admin.acl`      | `Allow`, `Deny`, `Any`, `Unknown`                                                                                                                                                                            |

A binding uses concrete values. The `Any` values, and `PatternType.Match`, are for filters: `Any` matches every value, and `PatternType.Match` matches every pattern that applies to the filter's name, whether literal, wildcard or prefixed. In a filter, a `null` principal, host or resource name also matches anything; that is how `ResourcePatternFilter.Any` and `AccessControlEntryFilter.Any` are built, and `AclBindingFilter(ResourcePatternFilter.Any, AccessControlEntryFilter.Any)` selects every ACL. `Unknown` stands for a value this version of zio-kafka does not know.

To let a principal read every topic whose name starts with a prefix, and then list the ACLs on that prefix:

```scala mdoc:compile-only
import zio._
import zio.kafka.admin.AdminClient
import zio.kafka.admin.acl._
import zio.kafka.admin.resource.{ PatternType, ResourcePattern, ResourcePatternFilter, ResourceType }

def allowReadOnPrefix(admin: AdminClient, principal: String, prefix: String): Task[Set[AclBinding]] = {
  val binding = AclBinding(
    ResourcePattern(ResourceType.Topic, name = prefix, patternType = PatternType.Prefixed),
    AccessControlEntry(
      principal = principal,
      host = "*",
      operation = AclOperation.Read,
      permissionType = AclPermissionType.Allow
    )
  )
  val filter = AclBindingFilter(
    ResourcePatternFilter(ResourceType.Topic, prefix, PatternType.Prefixed),
    AccessControlEntryFilter.Any
  )
  admin.createAcls(Set(binding)) *> admin.describeAcls(filter)
}
```

## The Async Variants

Several operations come in two flavours: `describeConfigs` and `describeConfigsAsync`, `incrementalAlterConfigs` and `incrementalAlterConfigsAsync`, `listOffsets` and `listOffsetsAsync`, `describeLogDirs` and `describeLogDirsAsync`, `createAcls` and `createAclsAsync`, `deleteAcls` and `deleteAclsAsync`. The plain variant waits for the whole request and fails if any part of it fails. The `*Async` variant returns a nested effect, `Task[Map[K, Task[V]]]`:

- The outer effect sends the request and completes as soon as the Java client returns its per-item futures, without waiting for the broker's answer.
- Each inner effect waits for the result of one item (one config resource, partition, broker, ACL binding or filter). For `incrementalAlterConfigsAsync`, `createAclsAsync` and `deleteAclsAsync`, the inner effect also does the visibility wait described above. The inner effect of `deleteAclsAsync` returns, for each binding the filter matched, `None` when it was deleted or `Some(error)` when its deletion failed.

This gives you a result, or a failure, per item: one missing topic does not fail the lookup of the others. The request is sent when the outer effect runs, so not running an inner effect does not cancel anything. To describe the configuration of several topics and keep a separate outcome for each:

```scala mdoc:compile-only
import zio._
import zio.kafka.admin.AdminClient
import zio.kafka.admin.AdminClient.{ ConfigResource, ConfigResourceType, KafkaConfig }

def describeEach(admin: AdminClient, topics: List[String]): Task[Map[ConfigResource, Either[Throwable, KafkaConfig]]] =
  admin
    .describeConfigsAsync(topics.map(ConfigResource(ConfigResourceType.Topic, _)))
    .flatMap { perResource =>
      ZIO.foreachPar(perResource) { case (resource, config) => config.either.map(resource -> _) }
    }
```

## Using the Java Admin Client Directly

Zio-kafka wraps most, but not all, operations of the Java `Admin` interface. For an operation it does not wrap, create the Java client with `AdminClient.javaClientFromSettings` and convert its `KafkaFuture` results with the public helpers `AdminClient.fromKafkaFuture` and `AdminClient.fromKafkaFutureVoid`. To describe the cluster's feature flags, which zio-kafka does not wrap:

```scala mdoc:compile-only
import org.apache.kafka.clients.admin.FeatureMetadata
import zio._
import zio.kafka.admin.{ AdminClient, AdminClientSettings }

val features: ZIO[Any, Throwable, FeatureMetadata] =
  ZIO.scoped {
    for {
      javaAdmin <- AdminClient.javaClientFromSettings(AdminClientSettings(List("localhost:9092")))
      metadata  <- AdminClient.fromKafkaFuture(ZIO.attempt(javaAdmin.describeFeatures().featureMetadata()))
    } yield metadata
  }
```

Operations called this way get none of zio-kafka's visibility waiting.

## See Also

- [Security](security.md) for TLS and SASL settings and the SSL endpoint check that `AdminClient.make` runs.
- [Test Producers and Consumers with the Test Kit](writing-tests.md) for the testkit helpers that create admin clients against an embedded broker.
- [Transactional Producer](transactional-producer.md) for when `IsolationLevel.ReadCommitted` matters.
