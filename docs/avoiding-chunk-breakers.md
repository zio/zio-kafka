---
id: avoiding-chunk-breakers
title: "Keep Stream Chunks Intact"
sidebar_label: "Keep Stream Chunks Intact"
---

In this guide we keep the chunks of a zio-kafka consumer stream intact while processing records, so that processing runs at full throughput. We look at why some stream operators break chunks, replace them with chunk-preserving alternatives, and check the chunk sizes the stream really sees.

## The Problem

ZIO streams are not just a simple sequence of elements. The elements are grouped into chunks, which makes many operations faster. Zio-kafka helps with this by guaranteeing that all records (for a given partition), fetched together from the broker, are in the same chunk.

Be careful when using `ZStream#mapZIO`, `ZStream#tap` and some other stream operators that break the chunking structure of the stream (or more precisely, the resulting stream has chunks with a single element). The throughput may be significantly lower than with the original chunking structure intact.

## Recognize Chunk-Breakers

Chunk-breaking operators can be found by looking at their scaladocs. Since zio-streams 2.1.17, the scaladocs of chunk-breaking operators such as `ZStream#mapZIO`, `ZStream#tap`, `ZStream#buffer`, `ZStream#mapZIOPar` and `ZStream#flattenZIO` contain the words `This combinator destroys the chunking structure`.

You can regain full throughput by processing all elements in a chunk together in one go. However, there is a catch: the order of processing changes. For example, given a stream with elements `a` and `b` in the same chunk, for `stream.mapZIO(f).mapZIO(g)` the evaluation order is `f(a), g(a), f(b), g(b)`. For the alternatives listed below the evaluation order changes to `f(a), f(b), g(a), g(b)`. Now imagine that `g(a)` fails, with `ZStream#mapZIO`, `f(b)` is not executed, but with the alternatives it _is_ executed. It is up to you to decide if this is a problem.

## Replace Chunk-Breakers

The following operators process records without breaking chunks. Pick the one that fits the shape of your processing.

### Use `ZStream#mapZIOChunked`

_Available since zio-streams 2.1.17._

The simplest alternative is the stream operator `ZStream#mapZIOChunked`; it has the same signature as `ZStream#mapZIO`:

```scala
def f(a: A): ZIO[R, E, B]

stream               // ZStream[R, E, A]
  .mapZIOChunked(f)  // ZStream[R, E, B]
```

### Use `ZStream#chunksWith`

If you have a single processing step that needs to work on a chunk, you can use `ZStream#chunksWith`:

```scala
def f(a: A): ZIO[R, E, B]

stream             // ZStream[R, E, A]
  .chunksWith { chunkStream =>
    chunkStream.mapZIO(chunk => ZIO.foreach(chunk)(f))
  }                // ZStream[R, E, B]
```

### Expose the Chunking Structure With `ZStream#chunks`

Use `ZStream#chunks` when you have multiple processing steps that can all work on a chunk at a time. Since `ZStream#chunks` exposes the chunking structure explicitly, the program can no longer accidentally break the chunking structure (unless `ZStream#flattenChunks` is also used):

```scala
def f(a: A): ZIO[R, E, B]
def g(b: B): ZIO[R, E, C]

stream                                         // ZStream[R, E, A]
  .chunks                                      // ZStream[R, E, Chunk[A]]
  .mapZIO { chunk => ZIO.foreach(chunk)(f) }   // ZStream[R, E, Chunk[B]]
  .mapZIO { chunk => ZIO.foreach(chunk)(g) }   // ZStream[R, E, Chunk[C]]
  .flattenChunks                               // ZStream[R, E, C]
```

### Run Side Effects per Chunk With `ZStream#tapChunks`

_Available since zio-streams 2.1.17._

Unlike `ZStream#tap`, the `ZStream#tapChunks` stream operator preserves the chunking structure and allows side effects per chunk:

```scala
stream                                                     // ZStream[R, E, A]
  .tapChunks(c => ZIO.logInfo(s"Chunk of size ${c.size}")) // ZStream[R, E, A]
```

### Avoid `ZStream#rechunk`

ZStream also provides the `ZStream#rechunk` operator. This operator rebuilds the chunking structure by grouping elements into chunks of fixed size. If there are not enough elements available for a chunk, the operator waits for these elements to arrive. Only when the stream ends a smaller chunk is emitted.

In low-volume kafka applications, waiting for enough elements to arrive can take a long time! In high-volume kafka applications, this operator causes data from multiple polls to be processed together. This results in increased processing latency and memory usage.

Therefore, we recommend that you avoid `ZStream#rechunk` in Kafka applications.

## Verify the Result

To see the chunk sizes your processing receives, add `ZStream#tapChunks` right after the consumer stream and right before the commit step, and compare the logged sizes. To log the size of every chunk the consumer emits and the size of every chunk after processing:

```scala mdoc:compile-only
import zio._
import zio.kafka.consumer._
import zio.kafka.serde.Serde

def consume(consumer: Consumer): Task[Unit] =
  consumer
    .plainStream(Subscription.topics("my-topic"), Serde.string, Serde.string)
    .tapChunks(chunk => ZIO.logDebug(s"Consumed chunk of ${chunk.size} records"))
    .mapZIOChunked(record => ZIO.logInfo(record.value).as(record.offset))
    .tapChunks(chunk => ZIO.logDebug(s"Processed chunk of ${chunk.size} offsets"))
    .aggregateAsync(Consumer.collectOffsets)
    .mapZIO(_.commit)
    .runDrain
```

When the topic has a backlog, both lines show the same, usually large, chunk sizes. If the second line always shows chunks of 1 while the first shows larger chunks, an operator between them breaks the chunks.
