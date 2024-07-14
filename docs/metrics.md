---
id: metrics
title: "Zio-Kafka Metrics"
---

Zio-kafka exposes all the metrics of the wrapped Java based consumer and producer, plus some more metrics about the
zio-kafka consumer itself.

## Java client metrics

The metrics from the Java metrics can be obtained via the `Consumer.metrics` and `Producer.metrics` methods. Both
return a live view on the internal metrics of the consumer/producer. We currently do not expose these metrics elsewhere,
a PR to copy them to the zio-metrics API is welcome.

## Zio-kafka consumer metrics

The zio-kafka consumer collects some additional metrics using the zio-metrics API. This allows any zio-metrics backend
to access and process the observed values.

By default, no tags are added, but this can be configured via the new method `ConsumerSettings.withMetricsLabels`.

The following metrics are collected:

- Poll metrics: poll count (counter), number of records per poll (histogram), poll latency (histogram).
- Partition stream metrics: queue size per partition (histogram), total queue size per consumer (histogram), number of polls for which records are idle in the queue (histogram).
- The number of partitions that are paused/resumed (gauge).
- Rebalance metrics: currently assigned partitions count (gauge), assigned/revoked/lost partitions (counter).
- Commit metrics: commit count (counter), commit latency (histogram). These metrics measure commit requests issued through zio-kafka's api.
- Aggregated commit metrics: commit count (counter), commit latency (histogram), commit size (number of offsets per commit) (histogram). After every poll zio-kafka combines all outstanding commit requests into 1 aggregated commit. These metrics are for the aggregated commits.
- Number of entries in the command and commit queues (histogram).
- Subscription state, `1` for subscribed, `0` of unsubscribed (gauge).
- The number of polls that ended with an authentication or authorization error (counter).

Like the zio-metrics API we follow Prometheus conventions. This means that:

- durations are expressed in seconds,
- counters can only increase,
- metric names use snake_case and end in the unit where possible.

The histograms each use 10 buckets. To reach a decent range while keeping sufficient accuracy at the low end, most
bucket boundaries use an exponential series based on 𝑒.
