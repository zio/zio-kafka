---
id: preventing-duplicates
title: "Preventing duplicates"
---

In zio-kafka processing of records runs asynchronously with partition management. This brings substantial performance
advantages but causes some records to be consumed and processed _twice_ when a rebalance occurs. To prevent this,
since version 2.7.1 zio-kafka supports a new mode in which we prevent duplicates due to rebalances. You can enable it
as follows:

```scala
import zio.*
import zio.kafka.consumer.ConsumerSettings

val consumerSettings: ConsumerSettings =
  ConsumerSettings(List("localhost:9092"))
    .withGroupId("group")
    .withRebalanceSafeCommits(true)       // enable rebalance-safe-commits mode
    .withMaxRebalanceDuration(30.seconds) // defaults to 3 minutes
```

With rebalance-safe-commits mode enabled, rebalances are held up for up to max-rebalance-duration to wait for pending
commits to be completed. Once pending commits are completed, it is safe for another consumer in the group to take over
a partition.

For this to work correctly, your program must process a chunk of records within max-rebalance-duration. The clock
starts the moment the chunk is pushed into the stream and ends when the commits for these records complete.

In addition, your program must commit the offsets of consumed records. The most straightforward way is to commit to the
Kafka brokers. This is done by calling `.commit` on the offset of consumed records (see the consumer documentation).
However, there are more options: external commits and transactional producing.

### Commit to an external system

When you commit to an external system (e.g. by writing to a relational database) the zio-kafka consumer needs to know
about those commits before it can work in rebalance-safe-commits mode. Inform zio-kafka about external commits by
invoking method `Consumer.registerExternalCommits(offsetBatch: OffsetBatch)` (available since zio-kafka 2.10.0).

Here is what this could look like:

```scala
import zio.kafka.consumer._

consumer.plainStream(Subscription.topics("topic2000"), Serde.string, Serde.string)
  .mapZIO { record =>
    database.store(record.offset) *> // <-- the external commit
      consumer.registerExternalCommits(OffsetBatch(record.offset))
  }
  .runDrain
```

### Commit with a transactional producer

Although transactional producing is possible with zio-kafka, it is not easy and the code is very messy (see
`ConsumerSpec` for an example). Transactional producing can not be used in combination with rebalance-safe-commits mode.

Zio-kafka v3.0.0 will make transactional producing much easier.

## More information

There is more information in the scaladocs of `ConsumerSettings` and the description of
[pull request #1098](https://github.com/zio/zio-kafka/pull/1098) that introduced this feature.
You can also watch the presentation
[Making ZIO-Kafka Safer And Faster](https://www.youtube.com/watch?v=MJoRwEyyVxM). The relevant part starts at 10:24.
