---
id: migrating-to-zio-kafka-3
title: "Migrating to zio-kafka 3"
---

Zio-kafka 3.0.0 brings a number of backwards incompatible changes:

1. Removal of all deprecated methods, _including accessor methods,_
2. The transactional producer is now much easier to use, 
3. `restartStreamOnRebalancing` mode is no longer supported.

This document helps you migrate from zio-kafka 2 to zio-kafka 3.

# 1. Deprecations

Zio-kafka 3.0.0 removes everything that was deprecated in the zio-kafka 2.x series. In particular, this includes
accessor methods. To prepare for zio-kafka 3.0, _you should always first migrate to zio-kafka 2.11.0_ and solve all
deprecation issues, using this page as a guide.

## Renamed methods

Some methods have just been renamed. Read the deprecation message and try the new method name. If it compiles, you're
done. Otherwise, read on.

## Consumer, Producer and TransactionalProducer accessor methods

Accessor methods are little helper methods that look up a service from the environment, and then forward your call to
that service. Accessor methods have not been recommended for some time and are now deprecated. The
[ZIO service pattern](https://zio.dev/reference/service-pattern/) provides a much cleaner approach for accessing
services.

All accessor methods provided by zio-kafka are deprecated in zio-kafka 2.11 and will be removed in zio-kafka 3.0. If
you use these accessor methods follow one of these approaches:

### Use the ZIO Service pattern

This is the best option. For established codebases it may be a lot of work to get here. If you are already follow
this pattern, using it for zio-kafka services as well will be easy. See [ZIO service pattern](https://zio.dev/reference/service-pattern/) for more information.

Here is an example with a `Consumer`, but it works the same with `Producer` and `TransactionalProducer`. We get the
`Consumer` from the environment in the layer with the `ZIO.service` method, and then inject it into the service class.
The service now uses the consumer directly: with `consumer` instead of `Consumer`.

```scala
import zio.kafka.consumer.Consumer

trait Service {
    def someMethod: ZIO[Any, Throwable, String]
}

object Service {
  
  def layer: ZLayer[Consumer, Throwable, Service] = ZLayer {
      for {
        consumer <- ZIO.service[Consumer]
      } yield ServiceLive(consumer)
  }
  
}

case class ServiceLive(consumer: Consumer) extends Service {

  override def someMethod: ZIO[Any, Throwable, String] = {
    // use `consumer`, not `Consumer`:
    consumer.plainStream(/*...*/).runHead
  }
}
```

Constructing a `Consumer` layer is described in [creating a consumer](creating-a-consumer.md). Constructing a
`Producer` or `TransactionalProducer` layer works in a similar way.

### YOLO, use `ZIO.service` everywhere

The other option is to replace all accessor methods of `Consumer`, `Producer` and `TransactionalProducer` as follows:

- `Consumer.method(...)` => `ZIO.serviceWithZIO[Consumer](_.method(...))`
- `Producer.method(...)` => `ZIO.serviceWithZIO[Producer](_.method(...))`
- `TransactionalProducer.method(...)` => `ZIO.serviceWithZIO[TransactionalProducer](_.method(...))`

Or, alternatively, use `ZIO.service`.

For example: `Consumer.method(...)` is transformed to:
```scala
for {
  consumer <- ZIO.service[Consumer]
  _        <- consumer.method(...)
} yield () 
```

## Zio-test-kit

Zio-kafka provides a `zio-kafka-testkit` library to help you test your code using zio-kafka. Several methods in the
`KafkaTestUtils` class have been replaced:

- The `produce*` and `scheduledProduce*` methods, which get a producer from the environment, have been deprecated.
  Each of these methods now has a variant with an explicit producer parameter.
- All methods that produce a layer (e.g. `consumer`, `producer`) have _not_ been deprecated, but they have been given a
  more convenient alternative: `makeConsumer`, `makeProducer`, etc.
- The `withAdmin`, `withSaslAdmin`, `withSslAdmin` methods are deprecated and are replaced by `makeAdminClient`,
  `makeSaslAdminClient` and `makeSslAdminClient`.

Here is a typical example and the new version:

```scala
// Old
override def spec: Spec[TestEnvironment, Any] =
  suite("old example suite")(
    test("uses a producer") {
      for {
        _ <- KafkaTestUtils.produceOne("topic", "key", "message")
      } yield assertCompletes
    }
  )
    .provideSome[Kafka](KafkaTestUtils.producer)
    .provideShared(Kafka.embedded)

// New
// Added `Scope`:
override def spec: Spec[TestEnvironment & Scope, Any] =
  suite("new example suite")(
    test("uses a producer") {
      for {
        // Make a producer explicitly (instead of via a layer):
        producer <- KafkaTestUtils.makeProducer
        // Notice explicit producer parameter:
        _ <- KafkaTestUtils.produceOne(producer, "topic", "key", "message")
      } yield assertCompletes
    }
    // Producer layer removed.
    .provideSomeShared[Scope](Kafka.embedded)
    // Using `provideSomeShared[Scope]` instead of `provideShared`
  )
```

`KafkaTestUtils.makeProducer` and the other `make*` methods require a `Scope` in the environment. This scope is
normally provided by the test framework (notice how `Scope` was added to the type parameter of `Spec`). If needed, a
smaller scope can be given with the `ZIO.scoped` method. See also [writing tests](writing-tests.md) for details on how
to provide more layers.

Here is an example of how to rewrite code that uses one of the deprecated `with*Admin` methods:

```scala
// Old
KafkaTestUtils.withAdmin { adminClient =>
  adminClient.method() // Use the admin client...
}

// New
for {
  adminClient <- KafkaTestUtils.makeAdminClient
  _           <- adminClient.method() // Use the admin client...
} yield ()
```

# 2. Changes in the transactional producer

The transactional producer is now much easier to use. The zio-kafka 2.x transactional API is so complicated that we
expect only a few very experienced users to make use of it. Therefore, only the new API is described in
[transactions](transactions.md).

# 3. `restartStreamOnRebalancing` mode

This mode is longer be available in zio-kafka 3. With `restartStreamOnRebalancing` all streams are ended during a
rebalance, even when the partition for that stream was not revoked. One of its purposes was to enable transactional
consuming. Since zio-kafka 3 however, transactional consuming no longer needs this mode.

If your goal is to [prevent duplicates](preventing-duplicates.md), please use `rebalanceSafeCommits` instead.

# Other changes?

If you find a change that is not documented here then please let us know via a
[new issue](https://github.com/zio/zio-kafka/issues/new).
