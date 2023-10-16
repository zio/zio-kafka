---
id: consumer-tuning
title: "Tuning the consumer"
---

Zio-kafka's consumer can be tuned with the `ConsumerSettings` class. 

```scala
val settings = ConsumerSettings(bootstrapServers)
  .withGroupId(groupId)
  .withProperties(properties)
  .... etc.
```

## Names

Two very similarly named settings are relevant. Don't mix them up:

* `pollTimeout` — how long a poll may take
* `max.poll.interval.ms` — the maximum time between polls

## Throughput and latency

The kafka client can be tuned for either high throughput or low latency, unfortunately not both.
The most important settings for tuning throughput and latency are:

* zio-kafka's `pollTimeout` — This is the maximum time to block while polling the Kafka consumer. Zio-kafka's default
  is `50ms` which is good for low latency applications. Set this higher, e.g. `500ms` for better throughput.
* kafka's [configuration `max.poll.records`](https://kafka.apache.org/documentation/#consumerconfigs_max.poll.records) — The maximum number of records a poll will return. Kafka defaults
  this to `500`. You can set this higher for more throughput, or lower for lower latency.
* zio-kafka's fetch-strategy `partitionPreFetchBufferLimit` — when the number of records in a partition queue is
  below this value, zio-kafka will start to pre-fetch and buffer more records from Kafka. The default value for this
  parameter is `1024`; 2 * the default `max.poll.records` of 500, rounded to the nearest power of 2.

Zio-kafka provides 2 methods that set these settings for 2 common use cases: `ConsumerSettings.tuneForHighThroughput`
and `ConsumerSettings.tuneForLowLatency`.
Note that their implementation may vary over time. You can use them as follows:

```scala
val highThroughputSettings = ConsumerSettings(bootstrapServers).tuneForHighThroughput
val lowLatencySettings = ConsumerSettings(bootstrapServers).tuneForLowLatency
```

Kafka’s performance is not very sensitive to record size. However, when records become very small (< 100 bytes) or very
large (> 100Kb), increasing or decreasing `max.poll.records` and `partitionPreFetchBufferLimit` can be considered.

## High number of partitions

When a lot of partitions need to be consumed, we need to take into account that heap is needed to store the records in
the partition queues. A very rough estimate for the maximum amount of heap needed is given by: `average record size` *
`number of of partitions` * max(`partitionPreFetchBufferLimit`, `max.poll.records`).

The total can be tuned by changing the `partitionPreFetchBufferLimit`, `max.poll.records` settings.

Another option is to write a custom `FetchStrategy`. For example the `ManyPartitionsQueueSizeBasedFetchStrategy` in
[draft PR 970](https://github.com/zio/zio-kafka/pull/970) (not yet tested at scale, use at your own risk). Note that the fetch strategy API is marked as
experimental and may change without notice in any future zio-kafka version.

## Long processing durations

To detect stalled consumers, Kafka revokes a partition when a consumer does not poll within the max poll interval (see
[configuration `max.poll.interval.ms`](https://kafka.apache.org/documentation/#consumerconfigs_max.poll.interval.ms)). The default max poll interval is 5 minutes. After a partition is revoked,
it will be assigned to another consumer.

In zio-kafka (versions 2.5.1+) a stream needs to pull data within the max poll interval. If this doesn't happen, the
stream is interrupted with a failure and the whole consumer shuts down.

To see if your application must be configured with a higher `max.poll.interval.ms` value we need to consider the
maximum duration between polls. If processing is sequential, we can obtain this maximum by multiplying
`max.poll.records` with the maximum duration to process a single record. To also accommodate things like long garbage
collections and buffering, configuration `max.poll.interval.ms` should be substantially higher than the maximum
processing time.

`max.poll.interval.ms` can be set with:

```scala
  .withMaxPollInterval(15.minutes)
```

On older zio-kafka versions `withMaxPollInterval` is not available. Use the following instead:

```scala
  .withProperty("max.poll.interval.ms", 15.minutes.toMillis.toString)
```

⚠️In zio-kafka versions 2.2 up to 2.5.0 it may also be necessary to increase the `runloopTimeout` setting.
When no stream is processing data for this amount of time (while new data is available), the consumer will halt with a
failure. In zio-kafka 2.5.0 `runloopTimeout` defaults to 4 minutes, a little bit lower than `max.poll.interval.ms`.
