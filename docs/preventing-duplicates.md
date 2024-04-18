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

For more information see the scaladocs in `ConsumerSettings`, read the description of
[pull request #1098](https://github.com/zio/zio-kafka/pull/1098) that introduced this feature, or watch the presentation
[Making ZIO-Kafka Safer And Faster](https://www.youtube.com/watch?v=MJoRwEyyVxM). The relevant part starts at 10:24.
