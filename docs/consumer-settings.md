---
id: consumer-settings
title: "Consumer Settings"
description: "Every ConsumerSettings option of a zio-kafka consumer, with its default, grouped by purpose, plus settings validation."
keywords:
  - "Kafka Consumer Configuration"
  - "Consumer Defaults"
  - "Settings Validation"
  - "ConsumerSettings"
---

`ConsumerSettings` is the **immutable configuration of a zio-kafka [`Consumer`](consumer.md)**. It holds two kinds of settings: the properties of the wrapped Apache Kafka `KafkaConsumer` (such as `group.id` and `max.poll.records`), and zio-kafka's own settings (such as the poll timeout, the offset retrieval strategy and rebalance-safe commits). `Consumer.make` and `Consumer.live` take a `ConsumerSettings`, check it, and create the consumer from it.

`ConsumerSettings` is a case class. Every `with*` method returns an updated copy, and the fields can be read back, for example `settings.pollTimeout`:

```scala
final case class ConsumerSettings(
  properties: Map[String, AnyRef] = Map.empty,
  closeTimeout: Duration = 30.seconds,
  pollTimeout: Duration = 50.millis,
  commitTimeout: Duration = ConsumerSettings.defaultCommitTimeout,
  offsetRetrieval: OffsetRetrieval = OffsetRetrieval.Auto(),
  rebalanceSafeCommits: Boolean = false,
  maxRebalanceDuration: Option[Duration] = None,
  fetchStrategy: FetchStrategy = QueueSizeBasedFetchStrategy(),
  metricLabels: Set[MetricLabel] = Set.empty,
  metricsObserver: Option[ConsumerMetricsObserver] = None,
  runloopMetricsSchedule: Schedule[Any, Unit, Long] = Schedule.fixed(500.millis),
  authErrorRetrySchedule: Schedule[Any, Throwable, Any] = Schedule.recurs(5) && Schedule.spaced(500.millis),
  maxStreamPullIntervalOption: Option[Duration] = None,
  diagnostics: Consumer.ConsumerDiagnostics = Consumer.NoDiagnostics,
  emptyPollCountToMetaRefresh: Option[Int] = None
  // ...
)
```

## Usage

To configure a consumer for a service, we start from the bootstrap servers, add the group id, and change only the settings that differ from their defaults:

```scala mdoc:compile-only
import zio._
import zio.kafka.consumer._
import zio.kafka.consumer.Consumer.{ AutoOffsetStrategy, OffsetRetrieval }

val settings: ConsumerSettings =
  ConsumerSettings(List("localhost:9092"))
    .withGroupId("orders-service")
    .withClientId("orders-service-1")
    .withOffsetRetrieval(OffsetRetrieval.Auto(AutoOffsetStrategy.Earliest))
    .withRebalanceSafeCommits(true)
    .withMaxPollInterval(10.minutes)
    .withProperty("fetch.min.bytes", "1024")

val consumer: ZIO[Scope, Throwable, Consumer] = Consumer.make(settings)
```

A group id is needed for `Topics` and `Pattern` subscriptions and for committing offsets to Kafka. A consumer with only `Manual` subscriptions works without one.

## Creating Settings

`ConsumerSettings.apply` creates settings with the given bootstrap servers and every other setting at its default. Build on it with the `with*` methods. New fields may be added to the case class in later releases, and this style stays source compatible when they are. The constructor and the default commit timeout live in the companion object:

```scala
object ConsumerSettings {
  def apply(bootstrapServers: List[String]): ConsumerSettings
  val defaultCommitTimeout: Duration // 15 seconds
}
```

`ConsumerSettings` also has two presets that change several settings at once. Their exact values may change in later releases:

```scala
final case class ConsumerSettings(/* ... */) {
  def tuneForHighThroughput: ConsumerSettings
  def tuneForLowLatency: ConsumerSettings
}
```

The presets set the following values:

| Preset                                   | `pollTimeout` | `max.poll.records` | `partitionPreFetchBufferLimit` |
| ---------------------------------------- | ------------- | ------------------ | ------------------------------ |
| `ConsumerSettings#tuneForHighThroughput` | 500 ms        | 2000               | 4096                           |
| `ConsumerSettings#tuneForLowLatency`     | 50 ms         | 100                | 512                            |

Both presets replace the fetch strategy with a `QueueSizeBasedFetchStrategy`, so apply them before `ConsumerSettings#withFetchStrategy`. [Tune the Consumer](consumer-tuning.md) explains when to use them.

## Defaults at a Glance

The table lists every setting, the method that changes it, and its default. "Kafka default" means zio-kafka does not set the property, so the Kafka client's own default applies:

| Setting                       | Method                                                                                                       | Default                                                   |
| ----------------------------- | ------------------------------------------------------------------------------------------------------------ | --------------------------------------------------------- |
| `bootstrap.servers`           | `ConsumerSettings.apply`, `ConsumerSettings#withBootstrapServers`                                            | required                                                  |
| `client.id`                   | `ConsumerSettings#withClientId`                                                                              | Kafka default (generated)                                 |
| Credentials                   | `ConsumerSettings#withCredentials`                                                                           | none                                                      |
| Close timeout                 | `ConsumerSettings#withCloseTimeout`                                                                          | 30 seconds                                                |
| `group.id`                    | `ConsumerSettings#withGroupId`                                                                               | none                                                      |
| `group.instance.id`           | `ConsumerSettings#withGroupInstanceId`                                                                       | none (dynamic membership)                                 |
| Offset retrieval              | `ConsumerSettings#withOffsetRetrieval`                                                                       | `OffsetRetrieval.Auto(AutoOffsetStrategy.Latest)`         |
| `isolation.level`             | `ConsumerSettings#withReadCommitted`                                                                         | Kafka default (`read_uncommitted`)                        |
| Poll timeout                  | `ConsumerSettings#withPollTimeout`                                                                           | 50 ms                                                     |
| `max.poll.records`            | `ConsumerSettings#withMaxPollRecords`                                                                        | Kafka default (500)                                       |
| `max.poll.interval.ms`        | `ConsumerSettings#withMaxPollInterval`                                                                       | Kafka default (5 minutes)                                 |
| Max stream pull interval      | `ConsumerSettings#withMaxStreamPullInterval`                                                                 | `max.poll.interval.ms`                                    |
| Fetch strategy                | `ConsumerSettings#withFetchStrategy`, `#withPartitionPreFetchBufferLimit`, `#withoutPartitionPreFetching`    | `QueueSizeBasedFetchStrategy(1024)`                       |
| Commit timeout                | `ConsumerSettings#withCommitTimeout`                                                                         | 15 seconds                                                |
| Rebalance-safe commits        | `ConsumerSettings#withRebalanceSafeCommits`                                                                  | `false`                                                   |
| Max rebalance duration        | `ConsumerSettings#withMaxRebalanceDuration`                                                                  | 3/5 of `max.poll.interval.ms` (3 minutes)                 |
| Auth error retry schedule     | `ConsumerSettings#withAuthErrorRetrySchedule`                                                                | `Schedule.recurs(5) && Schedule.spaced(500.millis)`       |
| Empty polls before a probe    | `ConsumerSettings#withEmptyPollCountToMetaRefresh`                                                           | disabled                                                  |
| Diagnostics                   | `ConsumerSettings#withDiagnostics`                                                                           | `Consumer.NoDiagnostics`                                  |
| Metric labels                 | `ConsumerSettings#withMetricsLabels`                                                                         | none                                                      |
| Metrics observer              | `ConsumerSettings#withMetricsObserver`                                                                       | the built-in zio-metrics observer                         |
| Runloop metrics schedule      | `ConsumerSettings#withRunloopMetricsSchedule`                                                                | `Schedule.fixed(500.millis)`                              |
| Any other Kafka property      | `ConsumerSettings#withProperty`, `ConsumerSettings#withProperties`                                           | Kafka default                                             |

## Settings by Purpose

The `with*` methods fall into groups by what they control. The following sections list each group's signatures and what zio-kafka does with them.

### Connection and Closing

These methods tell the consumer where the cluster is, who it is, and how long it may take to close:

```scala
final case class ConsumerSettings(/* ... */) {
  def withBootstrapServers(servers: List[String]): ConsumerSettings
  def withClientId(clientId: String): ConsumerSettings
  def withCredentials(credentialsStore: KafkaCredentialStore): ConsumerSettings
  def withCloseTimeout(timeout: Duration): ConsumerSettings
}
```

`ConsumerSettings#withBootstrapServers` replaces the servers given to `ConsumerSettings.apply`; it joins them with commas into `bootstrap.servers`. `ConsumerSettings#withClientId` sets `client.id`, which appears in broker logs and in the Kafka client's metrics. `ConsumerSettings#withCredentials` adds the TLS properties of a `KafkaCredentialStore`; see [Security](security.md). When the consumer's scope closes, zio-kafka stops consuming and closes the Kafka consumer, giving it at most the close timeout, 30 seconds by default.

### Consumer Group

These methods configure how the consumer joins a consumer group:

```scala
final case class ConsumerSettings(/* ... */) {
  def withGroupId(groupId: String): ConsumerSettings
  def withGroupInstanceId(groupInstanceId: String): ConsumerSettings
}
```

`ConsumerSettings#withGroupId` sets `group.id`. `ConsumerSettings#withGroupInstanceId` sets `group.instance.id`, which makes the consumer a *static member* of the group: only one consumer with that id may be in the group at a time, and a consumer that restarts within the session timeout gets its partitions back without a rebalance. Give every instance of your service its own stable id, for example derived from the host or pod name:

```scala mdoc:compile-only
import zio.kafka.consumer.ConsumerSettings

val podName: String = sys.env.getOrElse("POD_NAME", "orders-service-0")

val staticMemberSettings: ConsumerSettings =
  ConsumerSettings(List("localhost:9092"))
    .withGroupId("orders-service")
    .withGroupInstanceId(podName)
```

Kafka 4 also offers a new group protocol; see [The Consumer Group Protocol](#the-consumer-group-protocol).

### Starting Offsets and Isolation

These methods decide where a newly assigned partition starts and which records of transactions are visible:

```scala
final case class ConsumerSettings(/* ... */) {
  def withOffsetRetrieval(retrieval: OffsetRetrieval): ConsumerSettings
  def withReadCommitted(readCommitted: Boolean = true): ConsumerSettings
}
```

`ConsumerSettings#withOffsetRetrieval` sets the offset retrieval strategy, by default `OffsetRetrieval.Auto(AutoOffsetStrategy.Latest)`: a partition starts at its committed offset, or at its end when the group has not committed one. With `OffsetRetrieval.External`, offsets come from your own storage. [Partition Assignment and Offset Retrieval](partition-assignment-and-offset-retrieval.md) describes every strategy. The method also sets Kafka's `auto.offset.reset` property to the strategy's `AutoOffsetStrategy` (`earliest`, `latest` or `none`), overwriting any value set before, and converts the deprecated `OffsetRetrieval.Manual` to `OffsetRetrieval.External`.

Kafka 4 adds a fourth reset strategy, `by_duration:<ISO-8601 duration>`, which starts a partition without a committed offset at the records of the last given duration. The Kafka documentation recommends it over `latest`, which can skip records written to a newly added partition before the consumer discovers it. `AutoOffsetStrategy` has no case for it, so set the property directly. Call `ConsumerSettings#withProperty` after `ConsumerSettings#withOffsetRetrieval`, which would overwrite it. With an `OffsetRetrieval.Auto` strategy, zio-kafka leaves the reset entirely to the Kafka client, so the property alone decides. To start new partitions at the records of the last 6 minutes:

```scala mdoc:compile-only
import zio.kafka.consumer.ConsumerSettings

val byDurationSettings: ConsumerSettings =
  ConsumerSettings(List("localhost:9092"))
    .withGroupId("orders-service")
    .withProperty("auto.offset.reset", "by_duration:PT6M")
```

`ConsumerSettings#withReadCommitted` sets `isolation.level`. With `true` (the parameter's default), the consumer only sees records of committed transactions; with `false`, it also sees records of aborted transactions. Records written without a transaction are always visible. Kafka's own default is `read_uncommitted`, so call this method when you consume topics written by a [transactional producer](transactions.md).

### Polling and Fetching

These methods control how often the consumer polls, how many records it buffers, and how slow a stream may be:

```scala
final case class ConsumerSettings(/* ... */) {
  def withPollTimeout(timeout: Duration): ConsumerSettings
  def withMaxPollRecords(maxPollRecords: Int): ConsumerSettings
  def withMaxPollInterval(maxPollInterval: Duration): ConsumerSettings
  def withMaxStreamPullInterval(maxStreamPullInterval: Duration): ConsumerSettings
  def withFetchStrategy(fetchStrategy: FetchStrategy): ConsumerSettings
  def withPartitionPreFetchBufferLimit(partitionPreFetchBufferLimit: Int): ConsumerSettings
  def withoutPartitionPreFetching: ConsumerSettings
}
```

The methods do the following:

- `ConsumerSettings#withPollTimeout` sets the longest time one poll of the Kafka consumer may block (50 ms by default). A poll returns earlier as soon as it has `max.poll.records` records.
- `ConsumerSettings#withMaxPollRecords` sets Kafka's `max.poll.records`, the most records one poll returns, over all partitions (Kafka default 500).
- `ConsumerSettings#withMaxPollInterval` sets Kafka's `max.poll.interval.ms` (Kafka default 5 minutes). When the consumer does not poll within this interval, Kafka removes it from the group and reassigns its partitions.
- `ConsumerSettings#withMaxStreamPullInterval` sets how long a partition stream that has records waiting may go without pulling them (default: the `max.poll.interval.ms` value). When a stream exceeds it, zio-kafka fails that stream and shuts down the whole consumer.
- `ConsumerSettings#withFetchStrategy` sets the `FetchStrategy` that picks, before every poll, which partitions may fetch more records. The default `QueueSizeBasedFetchStrategy` pre-fetches for a partition while at most 1024 records are buffered for it.
- `ConsumerSettings#withPartitionPreFetchBufferLimit` replaces the fetch strategy with a `QueueSizeBasedFetchStrategy` with the given limit, and `ConsumerSettings#withoutPartitionPreFetching` sets the limit to 0, which disables pre-fetching.

[Tune the Consumer](consumer-tuning.md) explains how to choose these values and how to write a custom `FetchStrategy`.

### Committing

The commit timeout bounds how long zio-kafka waits for one commit:

```scala
final case class ConsumerSettings(/* ... */) {
  def withCommitTimeout(timeout: Duration): ConsumerSettings
}
```

A commit that does not complete within the timeout (15 seconds by default, `ConsumerSettings.defaultCommitTimeout`) fails with `Consumer.CommitTimeout`; the commit may still complete later. The commit timeout must be more than twice the poll timeout (see [Settings Validation](#settings-validation)), and it also limits the processing time when rebalance-safe commits are on. [Offsets and Committing](offsets-and-committing.md#retrying-commits) shows how to retry failed commits.

### Rebalancing

These methods make a rebalance wait for the commits of revoked partitions:

```scala
final case class ConsumerSettings(/* ... */) {
  def withRebalanceSafeCommits(value: Boolean): ConsumerSettings
  def withMaxRebalanceDuration(value: Duration): ConsumerSettings
}
```

Rebalance-safe commits are off by default. With `ConsumerSettings#withRebalanceSafeCommits(true)`, zio-kafka holds up a rebalance until the offsets of all records consumed from revoked partitions are committed, so the consumer that takes over a partition does not process those records again. `ConsumerSettings#withMaxRebalanceDuration` caps that wait; by default it is 3/5 of `max.poll.interval.ms`, which is 3 minutes with Kafka's default. [Prevent Duplicates After a Rebalance](preventing-duplicates.md) explains when to enable it and how to size the timeouts.

Leave rebalance-safe commits off when your streams do not commit, when the stream needs to call the consumer while a rebalance is running (the consumer is blocked until the rebalance ends), or when processing a record twice is harmless. A [transactional producer](transactions.md) requires them to be on.

When rebalance-safe commits are on and you do not set `partition.assignment.strategy`, zio-kafka sets it to Kafka's `RangeAssignor`, which revokes all partitions in every rebalance. Because of this, rebalance-safe commits need the classic group protocol; see the next section.

### The Consumer Group Protocol

Kafka 4 brokers support two group protocols, chosen with the Kafka property `group.protocol`. The default, `classic`, assigns partitions in the clients. With `consumer` (KIP-848), the group coordinator on the broker assigns partitions and rebalances happen incrementally, one partition at a time. Set it with `ConsumerSettings#withProperty`:

```scala mdoc:compile-only
import zio.kafka.consumer.ConsumerSettings

val newProtocolSettings: ConsumerSettings =
  ConsumerSettings(List("localhost:9092"))
    .withGroupId("orders-service")
    .withProperty("group.protocol", "consumer")
```

With `group.protocol=consumer`, several group settings move to the broker:

| Classic client setting          | With `group.protocol=consumer`                                           |
| ------------------------------- | ------------------------------------------------------------------------ |
| `partition.assignment.strategy` | broker assignor, chosen with the client property `group.remote.assignor` |
| `session.timeout.ms`            | broker setting `group.consumer.session.timeout.ms`                       |
| `heartbeat.interval.ms`         | broker setting `group.consumer.heartbeat.interval.ms`                    |

The Kafka client refuses to start with a `ConfigException` when one of the three classic settings in the table is set together with `group.protocol=consumer`. Since rebalance-safe commits set `partition.assignment.strategy`, keep `group.protocol` at `classic` when you enable rebalance-safe commits, and therefore also when you use a transactional producer. The other settings on this page, including `max.poll.interval.ms` and `group.instance.id`, work with both protocols.

### Authorization Errors

These methods control what the consumer does when the broker denies access:

```scala
final case class ConsumerSettings(/* ... */) {
  def withAuthErrorRetrySchedule(authErrorRetrySchedule: Schedule[Any, Throwable, Any]): ConsumerSettings
  def withEmptyPollCountToMetaRefresh(count: Int): ConsumerSettings
}
```

When a poll fails with an `AuthorizationException` or an `AuthenticationException`, the consumer retries the poll according to `ConsumerSettings#withAuthErrorRetrySchedule`. The default, `Schedule.recurs(5) && Schedule.spaced(500.millis)`, retries 5 times, 500 ms apart; this rides out a broker that is slow to authorize a new connection. When the schedule is exhausted, the consumer fails and so do its streams. Use `Schedule.stop` to fail on the first error. Every failed poll increments the metric `ziokafka_consumer_poll_auth_errors`.

A revoked `READ` permission does not make a poll fail: the poll returns no records. `ConsumerSettings#withEmptyPollCountToMetaRefresh` enables a probe that detects this; it is disabled by default. [Detect Silent Authorization Failures](detecting-authorization-failures.md) shows how to use both settings.

### Diagnostics and Metrics

These methods connect the consumer to your monitoring:

```scala
final case class ConsumerSettings(/* ... */) {
  def withDiagnostics(diagnostics: Consumer.ConsumerDiagnostics): ConsumerSettings
  def withMetricsLabels(metricLabels: Set[MetricLabel]): ConsumerSettings
  def withMetricsObserver(observer: ConsumerMetricsObserver): ConsumerSettings
  def withRunloopMetricsSchedule(runloopMetricsSchedule: Schedule[Any, Unit, Long]): ConsumerSettings
}
```

`ConsumerSettings#withDiagnostics` registers a callback for consumer events such as polls, commits and rebalances; see [Diagnostics](diagnostics.md). `ConsumerSettings#withMetricsLabels` adds labels to every zio-kafka consumer metric, which helps tell several consumers apart. `ConsumerSettings#withMetricsObserver` replaces the built-in zio-metrics observer, for example with another metrics library or with `ConsumerMetricsObserver.NoOp`; the labels are then ignored. `ConsumerSettings#withRunloopMetricsSchedule` sets how often the consumer measures its queue sizes and pending commits (every 500 ms by default). See [Metrics](metrics.md) and [Metrics Observers](metrics-observers.md).

### Kafka Properties

These methods set any property of the Kafka consumer, including those without a dedicated method:

```scala
final case class ConsumerSettings(/* ... */) {
  def withProperty(key: String, value: AnyRef): ConsumerSettings
  def withProperties(kvs: (String, AnyRef)*): ConsumerSettings
  def withProperties(kvs: Map[String, AnyRef]): ConsumerSettings
}
```

A later call overwrites the value of an earlier one for the same key, whether that call is `ConsumerSettings#withProperty` or a dedicated method such as `ConsumerSettings#withGroupId`. The [Kafka consumer configuration reference](https://kafka.apache.org/documentation/#consumerconfigs) lists every property.

## Driver Settings

`ConsumerSettings#driverSettings` returns the properties that zio-kafka passes to the Kafka consumer:

```scala
final case class ConsumerSettings(/* ... */) {
  def driverSettings: Map[String, AnyRef]
}
```

It starts with `enable.auto.commit=false`, adds `partition.assignment.strategy` set to the `RangeAssignor` when rebalance-safe commits are on, and then adds your properties, which win over both. To see what reaches the Kafka client for a consumer with rebalance-safe commits:

```scala mdoc
import zio.kafka.consumer.ConsumerSettings

ConsumerSettings(List("localhost:9092"))
  .withGroupId("orders-service")
  .withRebalanceSafeCommits(true)
  .driverSettings
```

## Settings Validation

`Consumer.make` and `Consumer.live` check the settings before they connect. When a check fails, they fail with an `IllegalArgumentException` whose message starts with `Invalid consumer settings` and lists every problem found. The checks are:

- `enable.auto.commit` is set to anything other than `false`. Zio-kafka pre-fetches records, so Kafka's auto commit could commit records your program has not processed yet; zio-kafka always disables it.
- The commit timeout is not more than twice the poll timeout.
- The offset retrieval strategy is the deprecated `OffsetRetrieval.Manual`, put into the settings without `ConsumerSettings#withOffsetRetrieval`. Use `OffsetRetrieval.External`, or convert with `OffsetRetrieval.Manual#toExternal`.

`TransactionalProducer.make` checks the settings of the consumer it is given as well. It fails when rebalance-safe commits are off or when `partition.assignment.strategy` contains the `CooperativeStickyAssignor`; see [Transactions](transactions.md).

`Consumer.fromJavaConsumerWithPermit` does not validate the settings, because the Kafka consumer already exists; see [Consumer](consumer.md#consumerfromjavaconsumerwithpermit).
