---
id: transactional-producer
title: "Transactional Producer"
---

`TransactionalProducer` is the **zio-kafka client for exactly-once consume-transform-produce**: it produces records and commits the offsets of the consumed records that led to them in one Kafka transaction. Consumers that read with `isolation.level=read_committed` then see either all records of a transaction, together with its offset commit, or none of them. A transaction is a `Transaction` value that lives in a ZIO `Scope`: it commits when the scope closes successfully and aborts otherwise. The producer is configured with `TransactionalProducerSettings` and works together with a zio-kafka `Consumer`. For a step-by-step introduction, read the [transactions tutorial](transactions.md).

`TransactionalProducer`:

- Scope-Bound — a transaction's boundaries are the boundaries of a ZIO `Scope`, so a failure or interruption aborts it.
- One at a Time — a producer runs one transaction at a time; the next one starts when the previous scope has closed.
- Rebalance-Safe — the producer tells the consumer which offsets it committed, so that the consumer can hold up a rebalance until every consumed record is committed.

The three types have the following shape:

```scala
trait TransactionalProducer {
  def createTransaction: ZIO[Scope, Throwable, Transaction]
}

trait Transaction {
  def produce[R, K, V](topic: String, key: K, value: V, keySerializer: Serializer[R, K], valueSerializer: Serializer[R, V], offset: Option[Offset]): RIO[R, RecordMetadata]
  def produce[R, K, V](producerRecord: ProducerRecord[K, V], keySerializer: Serializer[R, K], valueSerializer: Serializer[R, V], offset: Option[Offset]): RIO[R, RecordMetadata]
  def produceChunk[R, K, V](records: Chunk[ProducerRecord[K, V]], keySerializer: Serializer[R, K], valueSerializer: Serializer[R, V], offset: Option[Offset]): RIO[R, Chunk[RecordMetadata]]
  def produceChunkBatch[R, K, V](records: Chunk[ProducerRecord[K, V]], keySerializer: Serializer[R, K], valueSerializer: Serializer[R, V], offsets: OffsetBatch): RIO[R, Chunk[RecordMetadata]]
  def abort: IO[TransactionalProducer.UserInitiatedAbort.type, Nothing]
}

final case class TransactionalProducerSettings private (producerSettings: ProducerSettings)
```

`Offset` and `OffsetBatch` come from `zio.kafka.consumer`; `ProducerRecord` and `RecordMetadata` are the Kafka client's own classes.

## Usage

To copy every record of one topic to another topic exactly once, we produce each chunk of consumed records in a transaction that also carries the chunk's offsets:

```scala mdoc:compile-only
import org.apache.kafka.clients.producer.ProducerRecord
import zio._
import zio.kafka.consumer._
import zio.kafka.producer._
import zio.kafka.serde.Serde

val bootstrapServers = List("localhost:9092")

val copyTopic: ZIO[Consumer & TransactionalProducer, Throwable, Unit] =
  for {
    consumer <- ZIO.service[Consumer]
    producer <- ZIO.service[TransactionalProducer]
    _ <- consumer
           .plainStream(Subscription.topics("input"), Serde.string, Serde.string)
           .mapChunksZIO { records =>
             ZIO.scoped {
               for {
                 tx <- producer.createTransaction
                 _ <- tx.produceChunkBatch(
                        records.map(r => new ProducerRecord("output", r.key, r.value)),
                        Serde.string,
                        Serde.string,
                        OffsetBatch(records.map(_.offset))
                      )
               } yield Chunk.empty[Unit]
             }.uninterruptible
           }
           .runDrain
  } yield ()

val program: Task[Unit] =
  copyTopic.provide(
    ZLayer.succeed(ConsumerSettings(bootstrapServers).withGroupId("copier").withRebalanceSafeCommits(true)),
    Consumer.live,
    ZLayer.succeed(TransactionalProducerSettings(bootstrapServers, "copier-0")),
    TransactionalProducer.live
  )
```

The [transactions tutorial](transactions.md#producing-transactional-records) builds this program step by step and explains each choice.

## TransactionalProducerSettings

`TransactionalProducerSettings` wraps a `ProducerSettings` and adds Kafka's `transactional.id` property to it. Its constructor is private, and it has no `with*` methods of its own: configure a `ProducerSettings` first (see [ProducerSettings](producer.md#producersettings)) and pass it to one of the three `apply` overloads, each of which sets `transactional.id`:

```scala
object TransactionalProducerSettings {
  def apply(producerSettings: ProducerSettings, transactionalId: String): TransactionalProducerSettings
  def apply(bootstrapServers: List[String], transactionalId: String): TransactionalProducerSettings
  def apply(
    bootstrapServers: List[String],
    closeTimeout: Duration,
    properties: Map[String, AnyRef],
    transactionalId: String,
    sendBufferSize: Int
  ): TransactionalProducerSettings
}
```

The first overload is the one to prefer, because it accepts every producer setting. The second creates `ProducerSettings(bootstrapServers)` with default settings. The third also sets `ProducerSettings#closeTimeout`, `ProducerSettings#sendBufferSize` and the given Kafka properties.

The `TransactionalProducerSettings#producerSettings` field returns the wrapped settings, including `transactional.id`. To see the Kafka properties a transactional producer is created with:

```scala mdoc
import zio.kafka.producer.{ ProducerSettings, TransactionalProducerSettings }

TransactionalProducerSettings(ProducerSettings(List("localhost:9092")), "orders-copier-0").producerSettings.driverSettings
```

The `transactionalId` identifies one producer instance across restarts. When a producer starts with a `transactional.id` that another producer already uses, Kafka fences the older one, a "zombie", and rejects its transactions. Give every instance of the application its own stable id, for example a fixed prefix followed by the instance number; [Choosing a Transactional Id](transactions.md#choosing-a-transactional-id) in the tutorial explains why.

The transactional producer honours the zio-kafka options of the wrapped settings: `ProducerSettings#closeTimeout`, `ProducerSettings#sendBufferSize`, `ProducerSettings#authErrorRetrySchedule`, diagnostics and metrics.

:::warning[Idempotence must stay enabled]
Kafka only accepts a `transactional.id` on an idempotent producer. Idempotence is on by default; when the wrapped `ProducerSettings` turns it off with `ProducerSettings#withIdempotence(false)`, or sets `enable.idempotence` to `false`, `TransactionalProducer.make` fails with a Kafka `ConfigException`: "Cannot set a transactional.id without also enabling idempotence."
:::

## Creating a Transactional Producer

A transactional producer needs the `Consumer` that reads the records it processes, so both constructors take one:

```scala
object TransactionalProducer {
  def make(settings: TransactionalProducerSettings, consumer: Consumer): ZIO[Scope, Throwable, TransactionalProducer]
  val live: RLayer[TransactionalProducerSettings with Consumer, TransactionalProducer]
}
```

`TransactionalProducer.make` goes through these steps:

1. It runs the [SSL endpoint check](security.md#ssl-endpoint-check) with the properties of the wrapped `ProducerSettings`.
2. It checks that the consumer meets the [consumer requirements](#consumer-requirements), and fails with an `IllegalArgumentException` when it does not.
3. It creates the Java `KafkaProducer` and calls its `initTransactions` method, which registers the `transactional.id` with the cluster and fences an older producer that uses the same id.
4. When the scope closes, it closes the Java producer, waiting at most `ProducerSettings#closeTimeout`.

`TransactionalProducer.live` reads the settings and the consumer from the environment and calls `TransactionalProducer.make`. To build the producer as a layer on top of a consumer layer:

```scala mdoc:compile-only
import zio._
import zio.kafka.consumer.{ Consumer, ConsumerSettings }
import zio.kafka.producer.{ ProducerSettings, TransactionalProducer, TransactionalProducerSettings }

val bootstrapServers = List("localhost:9092")

val consumerLayer: TaskLayer[Consumer] =
  ZLayer.succeed(
    ConsumerSettings(bootstrapServers)
      .withGroupId("orders-copier")
      .withRebalanceSafeCommits(true)
  ) >>> Consumer.live

val producerLayer: TaskLayer[TransactionalProducer] =
  (consumerLayer ++ ZLayer.succeed(
    TransactionalProducerSettings(ProducerSettings(bootstrapServers), "orders-copier-0")
  )) >>> TransactionalProducer.live
```

## Consumer Requirements

Kafka accepts a transaction's offsets only when they are committed in the same consumer group generation (the group epoch) in which the records were consumed. Zio-kafka makes sure of that by holding up rebalances until every consumed record is committed, which puts these requirements on the consumer:

- Rebalance-safe commits must be enabled with `ConsumerSettings#withRebalanceSafeCommits(true)`. `TransactionalProducer.make` checks this.
- The partition assignor must revoke all partitions during a rebalance, so the `CooperativeStickyAssignor` is not supported. With rebalance-safe commits enabled, zio-kafka uses the `RangeAssignor` unless you configure another one. `TransactionalProducer.make` rejects the `CooperativeStickyAssignor`, but cannot detect a custom cooperative assignor.
- The consumer must use the classic group protocol. With `group.protocol=consumer` (see [The Consumer Group Protocol](consumer-settings.md#the-consumer-group-protocol)) the broker assigns partitions incrementally, which `TransactionalProducer.make` does not detect, so do not use it with a transactional producer.
- The consumer must have a group id. Without one, committing a transaction with offsets fails with an `InvalidGroupIdException`, and after the retries described in [How a Transaction Ends](#how-a-transaction-ends) the effect dies.
- The consumer and the producer must connect to the same Kafka cluster, because the offsets are committed to the consumer group's coordinator as part of the transaction.

A rebalance waits at most `ConsumerSettings#maxRebalanceDuration` for the commits. Its default is long, so consider a lower value, one in which every record of a poll can be processed. See [Preventing Duplicates](preventing-duplicates.md) for how rebalance-safe commits work.

Consumers that read the topics a transactional producer writes to should use `ConsumerSettings#withReadCommitted`; otherwise they also see records of open and aborted transactions.

## Producing in a Transaction

`TransactionalProducer#createTransaction` begins a transaction and returns a `Transaction` in a `Scope`. It waits while the scope of the previous transaction is still open, because a producer runs one transaction at a time.

The `Transaction` produce methods work like their `Producer` namesakes (see [Producer](producer.md#producing-records)) and additionally record which consumed offsets the transaction commits:

- `Transaction#produce` produces one record, given as a `ProducerRecord` or as a topic, key and value. Its `offset` parameter is the `Offset` of the consumed record, or `None`.
- `Transaction#produceChunk` produces a chunk of records with a single `Option[Offset]` for the whole chunk. Pass the offset of the last consumed record when the records came from one partition.
- `Transaction#produceChunkBatch` produces a chunk of records with an `OffsetBatch`, which can hold offsets of many partitions. This is the method to use for a chunk of a consumer stream.

Each of these methods waits until the broker acknowledges the records; a transaction has no async variants. When several offsets of one partition are recorded, the transaction commits the highest one. With `None` and an empty `OffsetBatch`, the transaction only produces records. To produce one record for one consumed record:

```scala mdoc:compile-only
import org.apache.kafka.clients.producer.{ ProducerRecord, RecordMetadata }
import zio._
import zio.kafka.consumer.CommittableRecord
import zio.kafka.producer.TransactionalProducer
import zio.kafka.serde.Serde

def forward(producer: TransactionalProducer, record: CommittableRecord[String, String]): Task[RecordMetadata] =
  ZIO.scoped {
    for {
      tx       <- producer.createTransaction
      metadata <- tx.produce(new ProducerRecord("output", record.key, record.value), Serde.string, Serde.string, Some(record.offset))
    } yield metadata
  }
```

## How a Transaction Ends

A transaction ends when its scope closes:

- When the scope closes successfully, zio-kafka sends the collected offsets to the transaction, commits it, and then tells the consumer which offsets were committed.
- When the scope closes with a failure or an interruption, zio-kafka aborts the transaction, and the records produced in it never become visible to `read_committed` consumers.

Committing and aborting are each retried up to 5 times; when they keep failing, the effect dies. This happens, for example, when the consumer was fenced because it missed a rebalance, and the broker rejects the transaction. Restart the consumer, or the whole application, in that case. Wrap the scope in `ZIO#uninterruptible` so that a transaction in progress still completes when the application shuts down.

Do not keep a `Transaction` after its scope has closed: producing with it then fails with `TransactionalProducer.TransactionLeaked`, a `RuntimeException` whose `offsetBatch` field holds the offsets the transaction had collected.

To abort a transaction on purpose, call `Transaction#abort`. It fails the effect with `TransactionalProducer.UserInitiatedAbort`, which is not a `Throwable`. The transaction is aborted only if this failure reaches the end of the scope: if you catch it inside the scope, the scope closes successfully and the transaction commits. So catch it outside the scope when the abort is expected:

```scala mdoc:compile-only
import org.apache.kafka.clients.producer.ProducerRecord
import zio._
import zio.kafka.producer.TransactionalProducer
import zio.kafka.producer.TransactionalProducer.UserInitiatedAbort
import zio.kafka.serde.Serde

val transactionalProducer: TransactionalProducer = ???

val abortedTransaction: Task[Unit] =
  ZIO.scoped {
    for {
      tx <- transactionalProducer.createTransaction
      _  <- tx.produce(new ProducerRecord("my-output-topic", "key", "value"), Serde.string, Serde.string, None)
      _  <- tx.abort // the record above is never visible to read_committed consumers
    } yield ()
  }.catchSome { case UserInitiatedAbort => ZIO.unit }
```

## Integration

These pages work together with `TransactionalProducer`:

- [Transactions Tutorial](transactions.md) builds a complete exactly-once application and explains how Kafka transactions work.
- [Producer](producer.md) documents `ProducerSettings`, which `TransactionalProducerSettings` wraps, and the produce methods that `Transaction` mirrors.
- [Offsets and Committing](offsets-and-committing.md) covers `Offset` and `OffsetBatch`.
- [Writing Tests](writing-tests.md) shows the testkit helpers that create a transactional producer against an embedded broker.
