---
id: transactions
title: "Tutorial: Exactly-Once Processing with Transactions"
sidebar_label: "Transactions Tutorial"
---

Welcome! This tutorial is for zio-kafka users who already know how to consume and produce records, for example from the [zio-kafka tutorial](tutorial.md), and now want to process records exactly once. You don't need any prior experience with Kafka transactions to follow along.

## Learning Objectives

By the end of this tutorial, you will understand:

- What a Kafka transaction is and how it differs from a database transaction
- How a `Consumer` and a `TransactionalProducer` work together
- How to produce the records of a consumed chunk, and commit the chunk's offsets, in one transaction
- Why zio-kafka needs rebalance-safe commits to make transactions succeed

We'll learn these concepts through:

1. What Are Kafka Transactions?
2. A Warning About Consuming Transactional Records
3. Producing Transactional Records
4. How It All Works

We recommend reading from top to bottom. The API details of every type used here are on the [Transactional Producer](transactional-producer.md) reference page.

## What Are Kafka Transactions?

[Kafka transactions](https://www.confluent.io/blog/transactions-apache-kafka/) are different from what you may know from relational databases. In Kafka, a transaction means that your program consumes some records, produces some other records to the same Kafka cluster, and then commits the offsets of the _consumed_ records as part of the transaction. When the transaction commits, the produced records become available to other consumers, and the consumed records count as processed, all at once.

For a gentler introduction with more background, read [Kafka Transactions Explained (Twice!)](https://www.warpstream.com/blog/kafka-transactions-explained-twice).

## A Warning About Consuming Transactional Records

By default, produced records become immediately visible to consumers, even if they are produced in a not yet committed Kafka transaction! To prevent reading transactional records before they are committed, configure every consumer of the output topics with the `isolation.level` property set to `read_committed`, for example with `ConsumerSettings#withReadCommitted`:

```scala mdoc:compile-only
import zio.kafka.consumer.ConsumerSettings

val bootstrapServers = List("localhost:9092")
val groupId          = "my-group"

val settings = ConsumerSettings(bootstrapServers)
  .withGroupId(groupId)
  .withReadCommitted(true) // Only read committed records
```

## Producing Transactional Records

To produce records transactionally, we need a `Consumer` and a `TransactionalProducer` that work together to safely commit transactions. In this tutorial we use the [ZIO service pattern](https://zio.dev/reference/service-pattern/), so the `Consumer` and the `TransactionalProducer` are created as layers. The program we build copies every record of an input topic to an output topic, exactly once.

### Creating the Consumer

First we create the `Consumer` layer. Rebalance-safe commits must be enabled; the [How It All Works](#how-it-all-works) section explains why. Since the default `maxRebalanceDuration` is quite high, we also set it to a lower duration, one in which we are sure all records of a poll can be processed:

```scala mdoc:compile-only
import zio._
import zio.kafka.consumer.{ Consumer, ConsumerSettings }

val bootstrapBrokers = List("localhost:9092")

val consumerLayer: ZLayer[Any, Throwable, Consumer] = ZLayer.scoped {
  val consumerSettings = ConsumerSettings(bootstrapBrokers)
    .withGroupId("somegroupid")
    .withRebalanceSafeCommits(true) // required!
    .withMaxRebalanceDuration(30.seconds)
  Consumer.make(consumerSettings)
}
```

The code above:

- `withGroupId` gives the consumer a group; a transaction commits the offsets of that group.
- `withRebalanceSafeCommits(true)` makes the consumer hold up a rebalance until every consumed record is committed. `TransactionalProducer.make` refuses a consumer without it.
- `withMaxRebalanceDuration(30.seconds)` limits how long a rebalance is held up.

The consumer has a few more requirements, such as its partition assignor and group protocol; zio-kafka's defaults meet them. [Consumer Requirements](transactional-producer.md#consumer-requirements) lists them all.

### Creating the Transactional Producer

Now we can create the `TransactionalProducer` layer. The producer must connect to the same Kafka cluster as the consumer:

```scala mdoc:compile-only
import zio._
import zio.kafka.consumer.Consumer
import zio.kafka.producer.{ ProducerSettings, TransactionalProducer, TransactionalProducerSettings }

import java.util.UUID

val bootstrapBrokers = List("localhost:9092")

val producerLayer: ZLayer[Consumer, Throwable, TransactionalProducer] =
  ZLayer.scoped {
    // See the next section for a better transactionalId
    val transactionalId = UUID.randomUUID().toString
    val producerSettings = ProducerSettings(bootstrapBrokers)
    val transactionalProducerSettings = TransactionalProducerSettings(producerSettings, transactionalId)

    for {
      consumer <- ZIO.service[Consumer]
      tp       <- TransactionalProducer.make(transactionalProducerSettings, consumer)
    } yield tp
  }
```

Notice that the `producerLayer` requires a `Consumer`. The producer coordinates with the consumer to make sure that the brokers accept its transactions: the consumer holds up rebalances until the producer has committed all records consumed before the rebalance.

### Choosing a Transactional Id

The `transactionalId` uniquely identifies a producer and is, ideally, retained across restarts. It allows the Kafka brokers to identify zombies, producers that are accidentally still running. Records produced by a zombie are rejected. For a more complete description, see [Kafka’s transactional.id – your first guess is probably wrong. It’s actually all about zombies](https://community.ibm.com/community/user/integration/blogs/kim-clark1/2024/06/19/kafka-transactionalid).

Ideally, the `transactionalId` is stable and is provided by the environment. For example, the first instance of the application uses a `transactionalId` that ends with `0`, the second instance one that ends with `1`, and so on. The random id of the previous section keeps the examples short, but gives up this protection.

### Consuming and Mapping Records

With these in place we can look at the application logic of consuming and producing. In this example we use `Consumer#plainStream` to consume records with an `Int` key and a `Long` value:

```scala mdoc:compile-only
import zio.kafka.consumer.{ Consumer, Subscription }
import zio.kafka.serde.Serde

val consumer: Consumer = ??? // the consumer from the layer above

val consumedRecordsStream = consumer
  .plainStream(Subscription.topics("my-input-topic"), Serde.int, Serde.long)
```

For each consumed record, we then create a `ProducerRecord` with a `String` value containing `Copy of <input value>`. Notice that we also keep the offset of the consumed record, so that we can commit it later:

```scala mdoc:compile-only
import org.apache.kafka.clients.producer.ProducerRecord
import zio._
import zio.kafka.consumer.CommittableRecord
import zio.stream.ZStream

val consumedRecordsStream: ZStream[Any, Throwable, CommittableRecord[Int, Long]] = ??? // from the previous step

val producedRecordsStream = consumedRecordsStream
  .mapChunks { (records: Chunk[CommittableRecord[Int, Long]]) =>
    records.map { record =>
      val key: Int    = record.record.key()
      val value: Long = record.record.value()
      val newValue: String = "Copy of " + value.toString

      val producerRecord: ProducerRecord[Int, String] =
        new ProducerRecord("my-output-topic", key, newValue)
      (producerRecord, record.offset)
    }
  }
```

To optimize throughput, we want to produce records in batches. The chunks of the consumer stream are ideal for that, because zio-kafka guarantees that each chunk contains records that were fetched together from the broker. We must be careful to keep the chunks intact, though. For example, we should not use `ZStream#mapZIO`, because it results in a stream where each chunk contains only a single item. Therefore, we use `ZStream#mapChunksZIO` instead (see also [avoiding chunk-breakers](avoiding-chunk-breakers.md)).

### Producing in a Transaction

These new records can now be produced. Let's build it up slowly, starting with an empty `ZStream#mapChunksZIO` (this outline does not compile yet):

```scala
producedRecordsStream
  .mapChunksZIO { (recordsAndOffsets: Chunk[(ProducerRecord[Int, String], Offset)]) =>
    ???
  }
```

Let's work on those question marks. We need to create a transaction. Each zio-kafka transaction runs in a ZIO `Scope`: the transaction commits when the scope closes successfully, and is aborted when the scope closes with a failure or an interruption. We start with the scope:

```scala
ZIO.scoped {
  ??? // transaction stuff
}
```

We also want to complete the transaction, even when the program is shutting down. Therefore, we add `ZIO#uninterruptible`:

```scala
ZIO
  .scoped {
    ??? // transaction stuff
  }
  .uninterruptible
```

Now we can fill in the "transaction stuff": we create the transaction and use it to produce the records:

```scala mdoc:compile-only
import org.apache.kafka.clients.producer.ProducerRecord
import zio._
import zio.kafka.consumer.{ Offset, OffsetBatch }
import zio.kafka.producer.TransactionalProducer
import zio.kafka.serde.Serde

val transactionalProducer: TransactionalProducer                    = ??? // the producer from the layer above
val recordsAndOffsets: Chunk[(ProducerRecord[Int, String], Offset)] = ??? // the input of `mapChunksZIO`

val transactionStuff =
  for {
    tx <- transactionalProducer.createTransaction
    _  <- {
      val (records, offsets) = recordsAndOffsets.unzip
      tx.produceChunkBatch(
        records,
        Serde.int,
        Serde.string,
        OffsetBatch(offsets)
      )
    }
    // running inside `mapChunksZIO`, we need to return a Chunk.
    // Since we're not using the result, the empty chunk will do.
  } yield Chunk.empty
```

Notice how we pass the offsets of the consumed records to `Transaction#produceChunkBatch`. When the transaction commits, these offsets are committed with it, so the copied records and the progress of the consumer become visible together.

### Putting It All Together

Putting it all together we get the complete application. Compared to the snippets above, it builds the layers from settings with `Consumer.live` and `TransactionalProducer.live`, and logs when a transaction completes:

```scala mdoc:compile-only
import org.apache.kafka.clients.producer.ProducerRecord
import zio._
import zio.kafka.consumer._
import zio.kafka.producer._
import zio.kafka.serde.Serde

import java.util.UUID

object Transactional extends ZIOAppDefault {

  private val topic = "transactional-test-topic"

  val bootstrapBrokers = List("localhost:9092")

  val consumerSettings = ZLayer.succeed {
    ConsumerSettings(bootstrapBrokers)
      .withGroupId("transactional-example-app")
      .withRebalanceSafeCommits(true) // required for transactional producing
      .withMaxRebalanceDuration(30.seconds)
  }

  val producerSettings = ZLayer.succeed {
    TransactionalProducerSettings(
      bootstrapServers = bootstrapBrokers,
      transactionalId = UUID.randomUUID().toString
    )
  }

  private val runConsumerStream: ZIO[Consumer & TransactionalProducer, Throwable, Unit] =
    for {
      consumer              <- ZIO.service[Consumer]
      transactionalProducer <- ZIO.service[TransactionalProducer]
      _                     <- ZIO.logInfo(s"Consuming messages from topic $topic...")
      _ <- consumer
        .plainStream(Subscription.topics(topic), Serde.int, Serde.long)
        .mapChunks { (records: Chunk[CommittableRecord[Int, Long]]) =>
          records.map { record =>
            val key: Int         = record.record.key()
            val value: Long      = record.record.value()
            val newValue: String = "Copy of " + value.toString

            val producerRecord: ProducerRecord[Int, String] =
              new ProducerRecord("my-output-topic", key, newValue)
            (producerRecord, record.offset)
          }
        }
        .mapChunksZIO { (recordsAndOffsets: Chunk[(ProducerRecord[Int, String], Offset)]) =>
          ZIO.scoped {
            for {
              _  <- ZIO.addFinalizer(ZIO.logInfo("Completing transaction"))
              tx <- transactionalProducer.createTransaction
              _ <- {
                val (records, offsets) = recordsAndOffsets.unzip
                tx.produceChunkBatch(
                  records,
                  Serde.int,
                  Serde.string,
                  OffsetBatch(offsets)
                )
              }
            } yield Chunk.empty
          }.uninterruptible
        }
        .runDrain
    } yield ()

  override def run: ZIO[Scope, Any, Any] =
    runConsumerStream
      .provide(
        consumerSettings,
        Consumer.live,
        producerSettings,
        TransactionalProducer.live
      )

}
```

What happens when a transaction fails, and how to abort one on purpose, is described in [How a Transaction Ends](transactional-producer.md#how-a-transaction-ends).

## How It All Works

Kafka must ensure that each record is processed only once, even though a partition can be revoked and assigned to another consumer while the original consumer knows nothing about it. In this situation, the original consumer happily continues producing records, while another consumer is doing the same thing with the same records.

Kafka prevents this by validating the group epoch. The group epoch is a number that increases after every rebalance. When the transactional producer commits a transaction with the offsets of consumed records, it also includes the group epoch in which the records were consumed. If the broker detects a commit with a group epoch that is not equal to the current epoch, it rejects the commit.

In addition, to prevent an old consumer from continuing to poll into the next epoch without participating in the rebalance, consumers polling within the wrong group epoch are "fenced". The broker rejects the commits of a fenced consumer.

Zio-kafka prevents these rejected commits. The `TransactionalProducer` informs the `Consumer` which offsets were committed, and when a rebalance starts, the consumer holds it up until all records consumed so far are committed. This feature is called rebalance-safe commits. Because of it, every transaction commits in the same group epoch as the epoch in which its records were consumed, and the brokers accept the transaction.

When a zio-kafka consumer misses a rebalance and continues polling, nothing can be done: the broker rejects its transactions, and zio-kafka gives up after a few retries (see [How a Transaction Ends](transactional-producer.md#how-a-transaction-ends)). In these cases the consumer, or more simply the whole program, should restart.

## What You've Learned

In this tutorial, you learned:

- That a Kafka transaction commits produced records together with the offsets of the consumed records
- That consumers of transactional topics need `read_committed` to skip uncommitted records
- How to create a rebalance-safe `Consumer` and a `TransactionalProducer` that work together
- How to produce each chunk of a consumer stream in its own transaction with `Transaction#produceChunkBatch`
- Why rebalance-safe commits make the brokers accept zio-kafka's transactions

## Where to Go Next

- **Want the API details?** The [Transactional Producer](transactional-producer.md) reference documents `TransactionalProducer`, `Transaction` and `TransactionalProducerSettings`, including how to abort a transaction.
- **Want to know more about rebalances?** [Preventing Duplicates](preventing-duplicates.md) explains rebalance-safe commits.
- **Writing tests?** [Writing Tests](writing-tests.md) shows how to create a transactional producer against an embedded broker.
