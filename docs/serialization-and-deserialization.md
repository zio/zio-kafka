---
id: serialization-and-deserialization
title: "Serialization and Deserialization"
---

Zio-kafka deserializes incoming data and serializes outgoing data (both keys and values) from byte arrays to any
other type and back. This works by passing a key and value `Deserializer` to the stream methods of the
[consumer](consumer.md) (for example `Consumer#plainStream`), and a key and value `Serializer` to the `produce*` methods
of the [producer](producer.md).

A `Serde` combines a `Deserializer` and a `Serializer`. Common serdes are provided in the `Serde` companion object, e.g.
`Serde.byteArray`, `Serde.string` and `Serde.long`. A serde can be converted to other serdes, or you can create a
custom serde by implementing the `Serde` trait directly.
Serializers and deserializers also receive the record's headers, see [record headers](record-headers.md).

This document contains:

- Handling failures in a serde
- How to create a custom serde
- How to create and use a custom serde that wraps invalid data
- How to do deserialization in the consumer stream

## Handling failures in a serde

Ideally, a serde can not fail serializing and deserializing. This is for example the case with the provided
`Serde.byteArray` and `Serde.string`. This is not the case for any serde that needs to handle invalid input,
(for example `Serde.long`), or a serde that needs to do a remote lookup.

By default, a consumer stream will fail if it encounters a deserialization error in the serde. Unfortunately, the
resulting failure might not clearly indicate that the cause is in the serde.

There are 2 solutions for improving this:

- Wrap the result of the deserializer in a `Try` with the `Deserializer#asTry` method.
- Use `Serde.byteArray`, put the deserialization code in the consumer stream, or do serialization before handing the
  data to zio-kafka. This way you can handle failures any way you want.

Both approaches are discussed further below.

## Custom Data Type Serdes

Serializers and deserializers for custom data types can be created from scratch, or by converting existing
serdes. For example, to create a serde for an `Instant` from a serde for a `Long`:

```scala mdoc:compile-only
import java.time.Instant
import zio.kafka.serde._

val instantSerde: Serde[Any, Instant] =
  Serde.long.inmap(java.time.Instant.ofEpochMilli)(_.toEpochMilli)
```

For a complete example of creating a custom serde with zio-json, see the
[Producing and Consuming JSON Data](tutorial.md#producing-and-consuming-json-data) section in the tutorial.

To handle missing data (a `null` key or value), you can use the `Serde#asOption` transformer. For example:
`Serde.string.asOption`. This results in a `None` if the key or value is `null` (absent, for example the value of a
tombstone record), and in a `Some(string)` otherwise. When serializing, `None` is written as `null`.

## Custom serdes that wraps invalid data

Any `Deserializer[R, A]` for a given type `A` can be converted into a `Deserializer[R, Try[A]]` where deserialization
failures are converted to a `Failure` using the `Deserializer#asTry` method. (Method `Serde#asTry`
is also available; it returns a `Deserializer`, so the result can only be used for consuming.)

Below is an example of skipping records that fail to deserialize. The offset is passed downstream to be committed. The
example uses `mapZIO` for brevity; see [avoiding chunk-breakers](avoiding-chunk-breakers.md) before using it in production:

```scala mdoc:compile-only
import zio._
import zio.kafka.consumer._
import zio.kafka.serde._
import scala.util.{Try, Success, Failure}

val consumerSettings = ConsumerSettings(List("localhost:9092")).withGroupId("group")
def someEffect(value: String): Task[Unit] = ???

val keySerde = Serde.string
val valueDeserializer = Serde.string.asTry   // <-- using `asTry`

val program: ZIO[Scope, Throwable, Unit] =
  Consumer.make(consumerSettings).flatMap { consumer =>
    consumer
      .plainStream(Subscription.topics("topic150"), keySerde, valueDeserializer)
      .mapZIO { record => // ⚠️ mapZIO is a chunk-breaker, see "Avoiding chunk-breakers"
        val tryValue: Try[String] = record.record.value()
        val offset: Offset = record.offset

        tryValue match {
          case Success(value) =>
            // Action for successful deserialization
            someEffect(value).as(offset)
          case Failure(exception) =>
            // Possibly log the exception or take alternative action
            ZIO.succeed(offset)
        }
      }
      .aggregateAsyncWithin(Consumer.collectOffsets, Schedule.fixed(100.millis))
      .mapZIO(_.commit)
      .runDrain
  }
```

## Deserialization in the consumer stream

In this approach we provide zio-kafka with the `Serde.byteArray` serde (which is a pass-through serde) and do the
deserialization in the consumer stream. The deserialization can be done with regular ZIO operators.

This approach provides more freedom at the cost of having to write more code. It also allows for optimizations such as
operating on chunks of records (see [avoiding chunk-breakers](avoiding-chunk-breakers.md)), and more contextual failure
handling.

The following example deserializes each value in the stream and falls back to another effect when deserialization
fails:

```scala mdoc:compile-only
import zio._
import zio.kafka.consumer._
import zio.kafka.serde._

final case class Message(text: String)

val consumerSettings = ConsumerSettings(List("localhost:9092")).withGroupId("group")
def deserialize(value: Array[Byte]): ZIO[Any, Throwable, Message] = ???
def someEffect(value: Array[Byte]): ZIO[Any, Throwable, Message] = ???
def processMessage(message: Message): Task[Unit] = ???

val program: ZIO[Scope, Throwable, Unit] =
  Consumer.make(consumerSettings).flatMap { consumer =>
    consumer
      .plainStream(Subscription.topics("topic150"), Serde.byteArray, Serde.byteArray)
      .mapZIO { record => // ⚠️ mapZIO is a chunk-breaker, see "Avoiding chunk-breakers"
        val value: Array[Byte] = record.record.value()
        val offset: Offset = record.offset

        deserialize(value)
          // possible action to take if deserialization fails:
          .catchAll(_ => someEffect(value))
          .flatMap(processMessage)
          .as(offset)
      }
      .aggregateAsyncWithin(Consumer.collectOffsets, Schedule.fixed(100.millis))
      .mapZIO(_.commit)
      .runDrain
  }
```

## A warning about `mapZIO`

Moved to [avoiding chunk-breakers](avoiding-chunk-breakers.md).
