---
id: avoiding-chunk-breakers
title: "Avoiding chunk-breakers"
---

## A warning about `mapZIO` and other chunk-breakers

Be careful with using `mapZIO`, `tap` and some other stream operators that break the chunking structure of the stream
(or more precisely, the resulting stream has chunks with a single element). Throughput can be considerably lower than
with the chunking structure intact.

You can get back to full throughput performance by processing all elements in a chunk together, in one go. There is one
gotcha: the processing order changes. For example, given a stream with elements `a` and `b` in the same chunk, for
`stream.mapZIO(f).mapZIO(g)` the evaluation order is `f(a), g(a), f(b), g(b)`. With the alternatives listed below the
evaluation order changes to `f(a), f(b), g(a), g(b)`. Now imagine that `g(a)` fails, with `mapZIO` `f(b)` is not
executed, but with the alternatives it _is_ executed. It is up to you to determine if this is a problem.

<!-- NOT YET AVAILABLE YET
### Use `mapZIOChunked`

_Available since zio-streams 2.1.17._

The simplest alternative is the stream operator `mapZIOChunked` which has the same signature as `mapZIO`.

```scala
def f(a: A): ZIO[R, E, B]

stream               // ZStream[R, E, A]
  .mapZIOChunked(f)  // ZStream[R, E, B]
```
-->

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

<!-- NOT AVAILABLE YET
### Side effects per chunk with `tapChunks`

_Available since zio-streams 2.1.17._

Unlike `tap`, stream operator `tapChunks` keeps the chunking structure and allows for side effects per chunk.

```scala
stream                                                     // ZStream[R, E, A]
  .tapChunks(c => ZIO.logInfo(s"Chunk of size ${c.size}")) // ZStream[R, E, A]
```
-->