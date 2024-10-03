---
id: serialization-and-deserialization
title: "Serialization and Deserialization"
---

Zio-kafka deserializes incoming data, and deserializes outgoing data (both keys and values) from byte arrays to any
other type and back. This works by providing a key and value `Deserializer` while constructing a `Consumer`,
and a key and value `Serializer` while constructing the `Producer`.

A `Serde` combines a `Deserializer` and a `Serializer`. Common serdes are provided in the `Serdes` object, e.g.
`Serdes.byteArray`, `Serdes.string` and `Serdes.long`. A serde can be converted to other serdes, or you can create a
custom serde by implementing the `Serde` trait directly.

This document contains:

- Handling failures in a serde
- How to create a custom serde
- How to create and use a custom serde that wraps invalid data
- How to do deserialization in the consumer stream

- A warning about using `mapZIO`

## Handling failures in a serde

Ideally, a serde can not fail serializing and deserializing. This is for example the case with the provided
`Serdes.byteArray` and `Serdes.string`. This is not the case for any serde that needs to handle invalid input,
(for example `Serdes.long`), or a serde that needs to do a remote lookup.

By default, a consumer stream will fail if it encounters a deserialization error in the serde. Unfortunately, the
resulting failure might not clearly indicate that the cause is in the serde.

There are 2 solutions for improving this:

- Wrap the result of the serde in a `Try` with the `Serde.asTry` method.
- Use `Serdes.byteArray`, put the deserialization code in the consumer stream, or do serialization before handing the
  data to zio-kafka. This way you can handle failures any way you want.

Both approaches are discussed further below.

## Custom Data Type Serdes

Serializers and deserializers for custom data types can be created from scratch, or by converting existing
serdes. For example, to create a serde for an `Instant` from a serde for a `Long`:

```scala
import java.time.Instant
import zio.kafka.serde._

val instantSerde: Serde[Any, Instant] =
  Serdes.long.inmap(java.time.Instant.ofEpochMilli)(_.toEpochMilli)
```

To handle missing data (an empty key or value), you can use the `Serde.asOption` transformer. For example:
`Serdes.string.asOption`. This results in a `None` if the key or value is empty, and in a `Some(string)` otherwise.

## Custom serdes that wraps invalid data

Any `Deserializer[A]` for a given type `A` can be converted into  a `Deserializer[Try[A]]` where deserialization
failures are converted to a `Failure` using the `asTry` method. (Method `asTry` is also available on `Serde`.)

Below is an example of skipping records that fail to deserialize. The offset is passed downstream to be committed.

```scala
import zio._, stream._
import zio.kafka.consumer._
import zio.kafka.serde._
import scala.util.{Try, Success, Failure}

val consumer = ZLayer.scoped(Consumer.make(consumerSettings))

val keySerde = Serdes.string
val valueSerde = Serdes.string.asTry   // <-- using `.asTry`
val stream = Consumer
  .plainStream(Subscription.topics("topic150"), keySerde, valueSerde)

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

## Deserialization in the consumer stream

In this approach we provide zio-kafka with the `Serdes.byteArray` serde (which is a pass-through serde) and do the
deserialization in the consumer stream. The deserialization can be done with regular ZIO operators.

This approach provides more freedom at the cost of having to write more code. It also allows for optimizations such as
operating on chunks of records (see next section), and more contextual failure handling.

Here is an example:

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
