---
id: detecting-authorization-failures
title: "Detect Silent Authorization Failures"
sidebar_label: "Detect Silent Authorization Failures"
---

In this guide we make a zio-kafka consumer fail fast when it loses read access to a topic, instead of waiting forever for records that never come. We enable zio-kafka's empty-poll probe, choose its threshold, decide how the consumer handles authorization errors that polls do report, and turn the failure into an alert or a restart.

## The Problem

When a consumer's `READ` access control list (ACL) entry is revoked at runtime, the Kafka broker stops sending records for the affected topic. The Java consumer's `poll()` does not report this as an error: it silently returns zero records. A zio-kafka stream (`Consumer#plainStream`, `Consumer#partitionedStream` or `Consumer#consumeWith`) on that topic then runs on indefinitely, without any sign that it lost its access. From the outside, it looks exactly like a quiet topic.

Use this guide when your cluster enforces topic-level ACLs that can be revoked while consumers run, and you want the consumer to fail, alert or restart when that happens. If your cluster does not change ACLs at runtime, leave the probe disabled (the default): disabled, it does no bookkeeping and sends no extra requests.

## Prerequisites

The probe reads the consumer group's committed offsets, so the consumer needs a group id (`ConsumerSettings#withGroupId`). Without one, the Java consumer throws an `InvalidGroupIdException` as soon as the probe runs.

## Enable the Empty-Poll Probe

`ConsumerSettings#withEmptyPollCountToMetaRefresh(n)` makes the consumer count, per topic, the consecutive polls that return no records for that topic. Only topics that are being fetched count; a topic whose partitions are all paused because their streams are not pulling is skipped. When a topic reaches `n` consecutive empty polls, the consumer asks the broker for the committed offset of one of its assigned partitions of that topic, and resets the topic's counter. Unlike `poll()`, this request fails with a `TopicAuthorizationException` when read access is denied. To probe after 3 consecutive empty polls:

```scala mdoc:compile-only
import zio.kafka.consumer.ConsumerSettings

val settings: ConsumerSettings =
  ConsumerSettings(List("localhost:9092"))
    .withGroupId("my-group")
    .withEmptyPollCountToMetaRefresh(3)
```

The probe runs inside the consumer's poll loop and can block it for up to the Kafka setting `default.api.timeout.ms`. A failing probe fails the consumer at once: it is not retried with the auth error retry schedule of the next section, and it does not count in the `ziokafka_consumer_poll_auth_errors` metric.

## Choose a Threshold

The threshold trades detection speed against load on the broker. A small value, such as 3 or 5, detects a lost permission within a few polls. With the default poll timeout of 50 ms, an idle topic reaches 3 empty polls quickly, so the probe then runs several times per second for each idle topic.

For low-volume topics that are quiet for long periods, choose a higher threshold. Every probe is an offset-fetch request to the group coordinator, so a low threshold on quiet topics creates needless network traffic and coordinator load. The same applies when one consumer subscribes to many topics, since each idle topic is probed separately.

## Decide How Polls Retry Authorization Errors

Some authorization problems are not silent: a poll fails with an `AuthorizationException` or an `AuthenticationException`, for example while a broker is still authorizing a new connection. The consumer retries such polls according to `ConsumerSettings#withAuthErrorRetrySchedule`, by default `Schedule.recurs(5) && Schedule.spaced(500.millis)`: 5 retries, 500 ms apart. When the schedule is exhausted, the consumer fails. Each failed poll increments the metric `ziokafka_consumer_poll_auth_errors`.

To fail on the first error instead, use `Schedule.stop`. To ride out slower authorization, allow more retries:

```scala mdoc:compile-only
import zio._
import zio.kafka.consumer.ConsumerSettings

val failFast: ConsumerSettings =
  ConsumerSettings(List("localhost:9092"))
    .withGroupId("my-group")
    .withEmptyPollCountToMetaRefresh(3)
    .withAuthErrorRetrySchedule(Schedule.stop)

val patient: ConsumerSettings =
  ConsumerSettings(List("localhost:9092"))
    .withGroupId("my-group")
    .withEmptyPollCountToMetaRefresh(3)
    .withAuthErrorRetrySchedule(Schedule.recurs(10) && Schedule.spaced(1.second))
```

A larger `pollTimeout` also lowers the authorization work on the broker, because the consumer polls less often.

## Handle the Failure

When the probe or a poll fails, the consumer stops and every stream of the consumer fails with the error. The error is an `AuthorizationException` (a `TopicAuthorizationException` for a revoked topic permission) or, for a poll that keeps failing authentication, an `AuthenticationException`, so your program can recognize it and raise an alert before it fails, or let the application restart with fresh credentials. To log an alert for both kinds and let the program fail:

```scala mdoc:compile-only
import org.apache.kafka.common.errors.{ AuthenticationException, AuthorizationException }
import zio._
import zio.kafka.consumer._
import zio.kafka.serde.Serde

def consume(consumer: Consumer): Task[Unit] =
  consumer
    .plainStream(Subscription.topics("orders"), Serde.string, Serde.string)
    .mapZIOChunked(record => ZIO.logInfo(record.value).as(record.offset))
    .aggregateAsync(Consumer.collectOffsets)
    .mapZIO(_.commit)
    .runDrain

def run(settings: ConsumerSettings): Task[Unit] =
  ZIO
    .scoped(Consumer.make(settings).flatMap(consume))
    .tapError {
      case e: AuthorizationException  => ZIO.logError(s"Lost access to Kafka: ${e.getMessage}")
      case e: AuthenticationException => ZIO.logError(s"Kafka rejected our credentials: ${e.getMessage}")
      case _                          => ZIO.unit
    }
```

## Verify the Result

To check the setup, run the consumer against a cluster with ACLs, for example a staging cluster or the SASL broker that `Kafka.saslEmbedded` from `zio-kafka-testkit` starts (see [Test Producers and Consumers with the Test Kit](writing-tests.md)). Once the consumer runs, remove its `READ` permission on the topic, for example with Kafka's `kafka-acls.sh --remove` or with `AdminClient#deleteAcls` (see [Admin Client](admin-client.md)). Within about `n` polls of the topic going quiet, the stream fails with a `TopicAuthorizationException` and the alert appears in the log. Without the probe, the same consumer keeps running and logs nothing.
