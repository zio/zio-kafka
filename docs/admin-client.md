---
id: admin-client
title: "Admin Client"
---

`AdminClient` manages a Kafka cluster from ZIO: it creates and deletes topics, reads and changes configuration, inspects and resets consumer groups, lists offsets, describes the cluster, and manages access control lists (ACLs). It wraps the Java [`Admin`](https://kafka.apache.org/documentation/#adminapi) client, turns every Kafka future into a ZIO effect, and replaces Java collections and nullable values with Scala collections, `Option` and small case classes. The main types are `AdminClient`, `AdminClientSettings`, the model case classes nested in the `AdminClient` companion object (`NewTopic`, `ConfigResource`, `OffsetSpec`, and so on), and the ACL types in the `zio.kafka.admin.acl` and `zio.kafka.admin.resource` packages.

The shape of the two entry points is:

```scala
trait AdminClient {
  def createTopic(newTopic: AdminClient.NewTopic, validateOnly: Boolean = false): Task[Unit]
  def listTopics(listTopicsOptions: Option[AdminClient.ListTopicsOptions] = None): Task[Map[String, AdminClient.TopicListing]]
  // ... about 40 more operations
}

final case class AdminClientSettings(closeTimeout: Duration, properties: Map[String, AnyRef])
```

## Write Operations Wait Until They Are Visible

The Java admin client completes a write operation as soon as the broker accepts it, but the change is not yet visible to the next read: a topic that was just created may be missing from `listTopics` for a while (see [KAFKA-18818](https://issues.apache.org/jira/browse/KAFKA-18818)). zio-kafka hides this. Its write operations only complete once the change is visible, using one of two strategies:

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

Two consequences follow. First, a request made with `validateOnly = true` changes nothing, so the polling runs until its 5-second limit before the call returns. Second, the waiting uses the ZIO `Clock`; in a ZIO Test spec that uses `TestClock`, add `@@ TestAspect.withLiveClock` (see [Writing Tests](writing-tests.md)).

## Creating an Admin Client

An `AdminClient` holds a network connection, so every constructor ties it to a `Scope` or leaves closing to you. All constructors live in the `AdminClient` companion object:

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

`AdminClient.live` builds the same client as a layer from an `AdminClientSettings` in the environment, so the rest of the application can depend on `AdminClient` as a service:

```scala mdoc:compile-only
import zio._
import zio.kafka.admin.{ AdminClient, AdminClientSettings }

val bootstrapServers = List("localhost:9092")

val adminLayer: ZLayer[Any, Throwable, AdminClient] =
  ZLayer.succeed(AdminClientSettings(bootstrapServers)) >>> AdminClient.live

val clusterId: ZIO[AdminClient, Throwable, String] =
  ZIO.serviceWithZIO[AdminClient](_.describeClusterId())
```

The remaining constructors wrap a Java `Admin` that you create yourself. `AdminClient.fromJavaClient` only wraps it: you stay responsible for closing it. `AdminClient.fromScopedJavaClient` wraps a Java client that a scoped effect creates and closes. `AdminClient.javaClientFromSettings` is the scoped effect `AdminClient.make` uses; it is useful when you also need the raw Java client, for example for an operation zio-kafka does not wrap. Neither `AdminClient.fromJavaClient` nor `AdminClient.fromScopedJavaClient` runs the SSL endpoint check by itself.

## AdminClientSettings

`AdminClientSettings` holds the Java client properties and the close timeout. Construct it from the bootstrap servers and refine it with the `with*` methods, which keeps your code source compatible with future releases:

| Member                                                  | Description                                                                       |
| ------------------------------------------------------- | --------------------------------------------------------------------------------- |
| `AdminClientSettings.apply(bootstrapServers)`           | Sets `bootstrap.servers` and a close timeout of 30 seconds                        |
| `AdminClientSettings#withBootstrapServers(servers)`     | Replaces `bootstrap.servers`                                                      |
| `AdminClientSettings#withProperty(key, value)`          | Adds or replaces one Java client property                                         |
| `AdminClientSettings#withProperties(kvs)`               | Adds or replaces several properties, as varargs pairs or as a `Map`               |
| `AdminClientSettings#withCredentials(credentialsStore)` | Adds the TLS properties of a `KafkaCredentialStore` (see [Security](security.md)) |
| `AdminClientSettings#closeTimeout`                      | How long closing the client may take; there is no `with` method, use `copy`       |
| `AdminClientSettings#driverSettings`                    | The properties passed to the Java client                                          |

Any [Java admin client configuration](https://kafka.apache.org/documentation/#adminclientconfigs) can be set as a property. To set a client id and a shorter request timeout, and to close faster:

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

Every operation takes zio-kafka model values and returns zio-kafka model values; the Java types stay inside the wrapper. Most operations also accept an optional options value (`Option[...Options] = None`) that carries a request `timeout` and operation-specific flags:

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
3. Call the operation. Write operations return once the change is visible; read operations return Scala maps keyed by topic name, `TopicPartition`, `ConfigResource` or group id.
4. Close the scope, which closes the client.

:::note[Name clashes]
The `AdminClient` companion defines its own `TopicPartition`, `OffsetAndMetadata`, `Node`, `Metric` and `IsolationLevel`, which clash with the Java classes of the same name. Import the model types you need by name, for example `import zio.kafka.admin.AdminClient.{ NewTopic, TopicPartition }`, rather than with a wildcard.
:::

## Operations by Task

The operations group naturally by the part of the cluster they manage. Every operation returns a `Task` (`ZIO[Any, Throwable, *]`), and a Kafka error, such as a `TopicExistsException` or a `GroupNotEmptyException`, becomes the failure of that task.

### Topics

These operations create, inspect and remove topics and their data:

| Operation                                                                                 | Returns                               | Notes                                                                                                                                       |
| ----------------------------------------------------------------------------------------- | ------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------- |
| `createTopic(newTopic, validateOnly = false)`                                             | `Task[Unit]`                          | Creates one topic                                                                                                                           |
| `createTopics(newTopics, options = None)`                                                 | `Task[Unit]`                          | `CreateTopicsOptions(validateOnly, timeout)`                                                                                                |
| `deleteTopic(topic)`                                                                      | `Task[Unit]`                          | Deletes one topic                                                                                                                           |
| `deleteTopics(topics, options = None)`                                                    | `Task[Unit]`                          | `DeleteTopicsOptions(retryOnQuotaViolation = true, timeout)`                                                                                |
| `listTopics(listTopicsOptions = None)`                                                    | `Task[Map[String, TopicListing]]`     | `ListTopicsOptions(listInternal = false, timeout)` includes internal topics                                                                 |
| `describeTopics(topicNames, options = None)`                                              | `Task[Map[String, TopicDescription]]` | `DescribeTopicsOptions(includeAuthorizedOperations, timeout)`                                                                               |
| `createPartitions(newPartitions, options = None)`                                         | `Task[Unit]`                          | `newPartitions: Map[String, NewPartitions]`; `CreatePartitionsOptions(validateOnly = false, retryOnQuotaViolation = true, timeout)`         |
| `deleteRecords(recordsToDelete, deleteRecordsOptions = None, visibilitySleep = 1.second)` | `Task[Unit]`                          | `recordsToDelete: Map[TopicPartition, RecordsToDelete]`, where `RecordsToDelete` is the Java class (`RecordsToDelete.beforeOffset(offset)`) |

The topic model types describe what to create and what comes back:

- `NewTopic(name, numPartitions, replicationFactor: Short, configs = Map.empty)` describes a topic to create, with optional topic configuration such as `"retention.ms"`.
- `NewPartitions(totalCount, newAssignments = List.empty)` raises a topic's partition count to `totalCount`, optionally with a broker id list per new partition.
- `TopicListing(name, topicId, isInternal)` is one entry of `listTopics`.
- `TopicDescription(name, internal, partitions, authorizedOperations)` lists a `TopicPartitionInfo(partition, leader, replicas, isr)` per partition, with `Node` values for the brokers; `authorizedOperations` is `None` unless the request set `includeAuthorizedOperations = true`.

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

These operations read and change the configuration of topics, brokers and other resources:

| Operation                                                                    | Returns                                        | Notes                                                                                    |
| ---------------------------------------------------------------------------- | ---------------------------------------------- | ---------------------------------------------------------------------------------------- |
| `describeConfigs(configResources, options = None)`                           | `Task[Map[ConfigResource, KafkaConfig]]`       | `DescribeConfigsOptions(includeSynonyms = false, includeDocumentation = false, timeout)` |
| `describeConfigsAsync(configResources, options = None)`                      | `Task[Map[ConfigResource, Task[KafkaConfig]]]` | One inner effect per resource                                                            |
| `incrementalAlterConfigs(configs, options, visibilitySleep = 1.second)`      | `Task[Unit]`                                   | `options: AlterConfigsOptions` is required                                               |
| `incrementalAlterConfigsAsync(configs, options, visibilitySleep = 1.second)` | `Task[Map[ConfigResource, Task[Unit]]]`        | Waiting happens in the inner effects                                                     |

The configuration model types are:

- `ConfigResource(type, name)` names the resource. `ConfigResourceType` is one of `Topic`, `Broker`, `BrokerLogger`, `Group`, `ClientMetrics` or `Unknown`. For a broker, `name` is the broker id as a string, for example `"0"`.
- `KafkaConfig(entries: Map[String, ConfigEntry])` is the configuration of one resource, keyed by config name. `ConfigEntry` is the Java `org.apache.kafka.clients.admin.ConfigEntry`, with `value()`, `source()`, `isReadOnly()` and so on.
- `AlterConfigOp(configEntry, opType)` is one change: `AlterConfigOpType.Set` sets a value, `Delete` reverts it to the default, and `Append` and `Subtract` add to or remove from a list-valued config.
- `AlterConfigsOptions(validateOnly = false, timeout = None)` holds the options; `AlterConfigsOptions()` is enough in most cases.

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

These operations list, inspect, reset and delete consumer groups:

| Operation                                                                              | Returns                                                     | Notes                                                                                                   |
| -------------------------------------------------------------------------------------- | ----------------------------------------------------------- | ------------------------------------------------------------------------------------------------------- |
| `listConsumerGroups(options = None)`                                                   | `Task[List[ConsumerGroupListing]]`                          | Consumer groups only; `ListConsumerGroupsOptions(states)` filters by `GroupState`                       |
| `listGroups(options = None)`                                                           | `Task[List[GroupListing]]`                                  | Groups of every type; `ListGroupsOptions(groupStates, groupTypes, protocolTypes)`, all empty by default |
| `describeConsumerGroups(groupIds*)`                                                    | `Task[Map[String, ConsumerGroupDescription]]`               | Varargs form                                                                                            |
| `describeConsumerGroups(groupIds, options)`                                            | `Task[Map[String, ConsumerGroupDescription]]`               | `DescribeConsumerGroupsOptions(includeAuthorizedOperations, timeout)`                                   |
| `deleteConsumerGroups(groupIds, options = None)`                                       | `Task[Unit]`                                                | `DeleteConsumerGroupOptions(timeout)`; the groups must be empty                                         |
| `listConsumerGroupOffsets(groupId, options = None)`                                    | `Task[Map[TopicPartition, OffsetAndMetadata]]`              | All committed offsets of one group; `ListConsumerGroupOffsetsOptions(requireStable)`                    |
| `listConsumerGroupOffsets(groupSpecs)`                                                 | `Task[Map[String, Map[TopicPartition, OffsetAndMetadata]]]` | `groupSpecs: Map[String, ListConsumerGroupOffsetsSpec]` selects partitions per group                    |
| `listConsumerGroupOffsets(groupSpecs, options)`                                        | `Task[Map[String, Map[TopicPartition, OffsetAndMetadata]]]` | Same, with `ListConsumerGroupOffsetsOptions`                                                            |
| `alterConsumerGroupOffsets(groupId, offsets, options = None)`                          | `Task[Unit]`                                                | `AlterConsumerGroupOffsetsOptions(timeout)`; the group must have no active members                      |
| `removeMembersFromConsumerGroup(groupId, membersToRemove, visibilitySleep = 1.second)` | `Task[Unit]`                                                | `membersToRemove` are group instance ids of static members                                              |
| `removeMembersFromConsumerGroup(groupId)`                                              | `Task[Unit]`                                                | Same as `removeAllMembersFromConsumerGroup(groupId)`                                                    |
| `removeAllMembersFromConsumerGroup(groupId, visibilitySleep = 1.second)`               | `Task[Unit]`                                                | Removes every member                                                                                    |

The consumer group model types are:

- `ConsumerGroupListing(groupId, isSimple, state: Option[GroupState])` and `GroupListing(groupId, groupType: Option[GroupType], protocol, groupState: Option[GroupState])` are list entries. `GroupType` is `Consumer`, `Classic`, `Share`, `Streams` or `Unknown`.
- `ConsumerGroupDescription(groupId, isSimpleConsumerGroup, members, partitionAssignor, state, coordinator, authorizedOperations)` describes one group; each `MemberDescription(consumerId, groupInstanceId, clientId, host, assignment)` lists the partitions assigned to that member.
- `GroupState` is `Stable`, `Empty`, `Dead`, `PreparingRebalance`, `CompletingRebalance`, `Assigning`, `Reconciling`, `NotReady` or `Unknown`.
- `OffsetAndMetadata(offset, leaderEpoch = None, metadata = None)` is a committed offset. As always in Kafka, the committed offset is the offset of the *next* record the group will read.
- `ListConsumerGroupOffsetsSpec(partitions: Chunk[TopicPartition])` restricts an offsets lookup to some partitions.

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

To rewind a group to the start of its partitions, for example to reprocess a topic after a bug fix, stop all members of the group first and then alter its offsets:

```scala mdoc:compile-only
import zio._
import zio.kafka.admin.AdminClient
import zio.kafka.admin.AdminClient.{ GroupState, OffsetAndMetadata, OffsetSpec }

def rewindGroup(admin: AdminClient, groupId: String): Task[Unit] =
  for {
    description <- admin.describeConsumerGroups(groupId).map(_(groupId))
    _ <- ZIO.fail(new IllegalStateException(s"Group $groupId still has members"))
           .unless(description.state == GroupState.Empty)
    committed <- admin.listConsumerGroupOffsets(groupId)
    earliest  <- admin.listOffsets(committed.map { case (tp, _) => tp -> OffsetSpec.EarliestSpec })
    _ <- admin.alterConsumerGroupOffsets(
           groupId,
           earliest.map { case (tp, info) => tp -> OffsetAndMetadata(info.offset) }
         )
  } yield ()
```

### Offsets

`listOffsets` looks up offsets in the partitions themselves, independently of any consumer group:

| Operation                                                 | Returns                                                  | Notes                          |
| --------------------------------------------------------- | -------------------------------------------------------- | ------------------------------ |
| `listOffsets(topicPartitionOffsets, options = None)`      | `Task[Map[TopicPartition, ListOffsetsResultInfo]]`       | One lookup per partition       |
| `listOffsetsAsync(topicPartitionOffsets, options = None)` | `Task[Map[TopicPartition, Task[ListOffsetsResultInfo]]]` | One inner effect per partition |

The offsets model types are:

- `TopicPartition(name, partition)` identifies a partition. This is the zio-kafka admin case class, not the Java `org.apache.kafka.common.TopicPartition`.
- `OffsetSpec` selects which offset to look up: `OffsetSpec.EarliestSpec` (the first offset still in the log), `OffsetSpec.LatestSpec` (the offset the next produced record will get), or `OffsetSpec.TimestampSpec(timestamp)` (the first offset whose timestamp is at or after `timestamp`, in epoch milliseconds).
- `ListOffsetsOptions(isolationLevel = IsolationLevel.ReadUncommitted, timeout)` sets the isolation level. With `IsolationLevel.ReadCommitted`, `LatestSpec` returns the last stable offset, which excludes records of open transactions; use it for topics written by a [transactional producer](transactions.md).
- `ListOffsetsResultInfo(offset, timestamp, leaderEpoch: Option[Int])` is the result.

### Cluster

These operations describe the brokers, their log directories and the client itself:

| Operation                                             | Returns                                                | Notes                                             |
| ----------------------------------------------------- | ------------------------------------------------------ | ------------------------------------------------- |
| `describeClusterNodes(options = None)`                | `Task[List[Node]]`                                     | All brokers                                       |
| `describeClusterController(options = None)`           | `Task[Option[Node]]`                                   | The controller, if known                          |
| `describeClusterId(options = None)`                   | `Task[String]`                                         | The cluster id                                    |
| `describeClusterAuthorizedOperations(options = None)` | `Task[Set[AclOperation]]`                              | Empty unless `includeAuthorizedOperations = true` |
| `describeLogDirs(brokersId)`                          | `Task[Map[Int, Map[String, LogDirDescription]]]`       | Per broker id, per log directory path             |
| `describeLogDirsAsync(brokersId)`                     | `Task[Map[Int, Task[Map[String, LogDirDescription]]]]` | One inner effect per broker                       |
| `metrics`                                             | `Task[Map[MetricName, Metric]]`                        | The Java admin client's own metrics               |

All `describeCluster*` operations take `DescribeClusterOptions(includeAuthorizedOperations, timeout)`. A `Node(id, host, port, rack)` describes a broker, with `Option` fields where Kafka uses empty or negative placeholders. A `LogDirDescription(error, replicaInfos)` maps each partition in the directory to a `ReplicaInfo(size, offsetLag, isFuture)`. `MetricName(name, group, description, tags)` and `Metric(name, metricValue)` are Scala copies of the Java metric types; see [Metrics](metrics.md) for the consumer and producer metrics zio-kafka publishes.

### Access Control Lists

The ACL operations need a broker with an authorizer configured. They use the types from `zio.kafka.admin.acl` and `zio.kafka.admin.resource`:

| Operation                                  | Returns                                                                 | Notes                                                           |
| ------------------------------------------ | ----------------------------------------------------------------------- | --------------------------------------------------------------- |
| `describeAcls(filter, options = None)`     | `Task[Set[AclBinding]]`                                                 | `DescribeAclOptions(timeout)`                                   |
| `createAcls(acls, options = None)`         | `Task[Unit]`                                                            | `CreateAclOptions(timeout)`                                     |
| `createAclsAsync(acls, options = None)`    | `Task[Map[AclBinding, Task[Unit]]]`                                     | One inner effect per binding                                    |
| `deleteAcls(filters, options = None)`      | `Task[Set[AclBinding]]`                                                 | Returns the deleted bindings; `DeleteAclsOptions(timeout)`      |
| `deleteAclsAsync(filters, options = None)` | `Task[Map[AclBindingFilter, Task[Map[AclBinding, Option[Throwable]]]]]` | Per filter: each matched binding and its deletion error, if any |

An ACL is an `AclBinding(pattern, entry)` that binds a resource pattern to an access control entry:

```
AclBinding ──> ResourcePattern(resourceType, name, patternType)
           └─> AccessControlEntry(principal, host, operation, permissionType)

AclBindingFilter ──> ResourcePatternFilter(resourceType, name, patternType)
                 └─> AccessControlEntryFilter(principal, host, operation, permissionType)
```

The building blocks are:

- `ResourceType`: `Topic`, `Group`, `Cluster`, `TransactionalId`, `DelegationToken`, `User`, `Any` or `Unknown`.
- `PatternType`: `Literal` (the exact name, or `*` for all), `Prefixed` (every name that starts with `name`), and, for filters only, `Any` and `Match`.
- `AclOperation`: `Read`, `Write`, `Create`, `Delete`, `Alter`, `Describe`, `ClusterAction`, `DescribeConfigs`, `AlterConfigs`, `IdempotentWrite`, `CreateTokens`, `DescribeTokens`, `TwoPhaseCommit`, `All`, `Any` or `Unknown`.
- `AclPermissionType`: `Allow`, `Deny`, `Any` or `Unknown`.
- `principal` is a Kafka principal such as `"User:alice"` (or `"User:*"`), and `host` is a client host, or `"*"` for any host.
- `ResourcePatternFilter.Any` and `AccessControlEntryFilter.Any` match everything; combine them in `AclBindingFilter(ResourcePatternFilter.Any, AccessControlEntryFilter.Any)` to select every ACL.

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
- Each inner effect waits for the result of one item (one config resource, partition, broker, ACL binding or filter). For `incrementalAlterConfigsAsync`, `createAclsAsync` and `deleteAclsAsync`, the inner effect also does the visibility wait described above.

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

zio-kafka wraps most, but not all, operations of the Java `Admin` interface. For an operation it does not wrap, create the Java client with `AdminClient.javaClientFromSettings` and convert its `KafkaFuture` results with the public helpers `AdminClient.fromKafkaFuture` and `AdminClient.fromKafkaFutureVoid`. To describe the cluster's feature flags, which zio-kafka does not wrap:

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
- [Writing Tests](writing-tests.md) for the testkit helpers that create admin clients against an embedded broker.
- [Transactions](transactions.md) for when `IsolationLevel.ReadCommitted` matters.
