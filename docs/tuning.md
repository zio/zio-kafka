# Tuning the consumer

Zio-kafka's consumer can be tuned with the `ConsumerSettings` class. 

```scala
val settings = ConsumerSettings(bootstrapServers)
  .withGroupId(groupId)
  .withProperties(properties)
  .... etc.
```

## Throughput and latency

The kafka client can be tuned for either high throughput or low latency, unfortunately not both.
The most important settings for tuning throughput and latency are:

* poll timeout — This is the maximum time to block while polling the Kafka consumer. Zio-kafka's default is 50ms which is good for low latency applications. Set this higher, e.g. `500ms` for better throughput.
* [configuration `max.poll.records`](https://kafka.apache.org/documentation/#consumerconfigs_max.poll.records) — The maximum number of records a poll will return. Kafka defaults this to 500. You can set this higher for more throughput, or lower for lower latency.

Methods `ConsumerSettings.tuneForHighThroughput` and `ConsumerSettings.tuneForLowLatency` make it easy to configure
these parameters:

```scala
val highThroughputSettings = ConsumerSettings(bootstrapServers).tuneForHighThroughput
val lowLatencySettings = ConsumerSettings(bootstrapServers).tuneForLowLatency
```

## Long processing durations

To detect stalled consumers, Kafka revokes a partition when a consumer does not poll within the max poll interval (see [configuration `max.poll.interval.ms`](https://kafka.apache.org/documentation/#consumerconfigs_max.poll.interval.ms)). The default max poll interval is 5 minutes. After a partition is revoked, it will be assigned to another consumer.

To see if your application must be configured with a higher `max.poll.interval.ms` value we need to consider the
maximum duration between polls. If processing is sequential, we can obtain this maximum by multiplying
`max.poll.records` with the maximum duration to process a single record. To also accommodate things like long garbage
collections and buffering, configuration `max.poll.interval.ms` should be substantially higher than the maximum
processing time.

`max.poll.interval.ms` can be set with:

```scala
ConsumerSettings.withMaxPollInterval(15.minutes)
```

on older zio-kafka version `withMaxPollInterval` is not available, use the following instead:

```scala
withProperty("max.poll.interval.ms", "900000") // 15 minutes
```

⚠️In zio-kafka versions 2.2 up to 2.5.0 it may also be necessary to tune the `runloopTimeout` setting.
When no stream is processing data for this amount of time, the consumer will halt with a failure.
