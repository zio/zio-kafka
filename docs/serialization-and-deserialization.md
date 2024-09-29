---
id: serialization-and-deserialization
title: "Serialization and Deserialization"
---

Zio-kafka supports serializers and deserializers (serde) to convert record keys and values to and from byte arrays.

A `Serde` combines a `Deserializer` and a `Serializer`. Common serdes are provided in the `Serdes` object, e.g.
`Serdes.byteArray`, `Serdes.string` and `Serdes.long`. A serde can be converted to other serdes, or you can create a
custom serde by implementing the `Serde` trait directly.

This document contains:

- Handling failures in a serde
- How to write a custom serde
- How to do deserialization in the consumer stream
- How to write and use a custom serde that wraps invalid data
- A warning about using `mapZIO`

## Handling failures in a serde

By default, a consumer stream will fail if it encounters a deserialization error in the serde. Unfortunately, the
resulting failure might not clearly indicate that the cause is in the serde. It is also not straightforward to handle
failures from a serde.

Therefore, we recommend serdes (custom or not) only when they always succeed. This is for example the case with the
provided `Serdes.byteArray` and `Serdes.string`. Custom serdes should not fail on invalid input, and should not do
remote lookups that could fail.

For cases where failure is possible, we recommend using `Serdes.byteArray`, put the deserialization code in the consumer
stream, and do serialization before handing the data to zio-kafka. This way you can handle failure any way you want.<br>
Another option is to make a serde that wraps data in a Try.

These approaches are further discussed below.

## Custom Data Type Serdes

Serializers and deserializers for custom data types can be created from scratch, or by converting existing
serdes. For example, to create a serde for an `Instant` from a serde for a `Long`:

```scala
import java.time.Instant
import zio.kafka.serde._

val instantSerde: Serde[Any, Instant] = Serdes.long.inmap(java.time.Instant.ofEpochMilli)(_.toEpochMilli)
```

To handle missing data (an empty key or value), you can use the `Serde.asOption` transformer. For example:
`Serdes.string.asOption`. This results in a `None` if the key or value is empty, and in a `Some(string)` otherwise.

## Deserialization in the consumer stream

Deserialization in the consumer stream can be done with regular ZIO operators. Here is an example:

```scala
import zio._, stream._
import zio.kafka.consumer._

val consumer = ZLayer.scoped(Consumer.make(consumerSettings))

val stream = Consumer
  .plainStream(Subscription.topics("topic150"), Serde.byteArray, Serde.byteArray)

def deserialize(value: Array[Byte]): ZIO[Any, Throwable, Message] = ???

stream 
  .mapZIO { record => // ⚠️ see section about `mapZIO` below!
    val value: Array[Byte] = record.record.value()
    val offset: Offset = record.offset

    deserialize(value)
      // possible action to take if deserialization fails:
      .recoverWith(_ => someEffect(value))
      .flatMap(processMessage)
      .as(offset)
  }
  .aggregateAsync(Consumer.offsetBatches)
  .mapZIO(_.commit)
  .runDrain
  .provideSomeLayer(consumer)
```

## Custom serdes that wraps invalid data

Any `Deserializer[A]` for a given type `A` can be converted into  a `Deserializer[Try[A]]` where deserialization
failures are converted to a `Failure` using the `asTry` method.

Below is an example that skips messages that fail to deserialize. The offset is passed downstream to be committed.

```scala
import zio._, stream._
import zio.kafka.consumer._
import zio.kafka.serde._
import scala.util.{Try, Success, Failure}

val consumer = ZLayer.scoped(Consumer.make(consumerSettings))

val stream = Consumer
  .plainStream(Subscription.topics("topic150"), Serde.string, Serde.string.asTry)

stream 
  .mapZIO { record => // ⚠️ see section about `mapZIO` below!
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
  .aggregateAsync(Consumer.offsetBatches)
  .mapZIO(_.commit)
  .runDrain
  .provideSomeLayer(consumer)
```

## A warning about `mapZIO`

Be careful with using `mapZIO` as it breaks the chunking structure of the stream (or more precisely, the resulting
stream has chunks with a single element). Throughput can be considerably lower than with the chunking structure intact.

If your application requirements allow all elements of a chunk to be processed in one go, then you can use one of these
techniques to preserve the chunking structure:

### Use `chunksWith`

Use `chunksWith` when you have a single processing step that needs to work on a chunk.

```scala
def f(a: A): ZIO[R, E, B]

stream                                        // ZStream[R, E, A]
  .chunksWith { stream => stream.mapZIO(f) }  // ZStream[R, E, B]
```

### Expose chunking structure with `chunks`

Use `chunks` when you have multiple processing steps that can all work on a chunk at a time. Since `chunks` exposes the
chunking structure explicitly, the program can no longer accidentally break the chunking structure (unless
`flattenChunks` is also used).

```scala
def f(a: A): ZIO[R, E, B]
def g(b: B): ZIO[R, E, C]

stream                                         // ZStream[R, E, A]
  .chunks                                      // ZStream[R, E, Chunk[A]]
  .mapZIO { chunk => ZIO.foreach(chunk)(f) }   // ZStream[R, E, Chunk[B]]
  .mapZIO { chunk => ZIO.foreach(chunk)(g) }   // ZStream[R, E, Chunk[C]]
  .flattenChunks                               // ZStream[R, E, C]
```
