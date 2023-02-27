---
id: example-of-consuming-producing-and-committing-offsets
title: "Example of Consuming, Producing and Committing Offsets"
---

This example shows how to consume messages from topic `topic_a` and produce transformed messages to `topic_b`, after which consumer offsets are committed. Processing is done in chunks using `ZStreamChunk` for more efficiency.

```scala
import zio.ZLayer
import zio.kafka.consumer._
import zio.kafka.producer._
import zio.kafka.serde._
import org.apache.kafka.clients.producer.ProducerRecord

val consumerSettings: ConsumerSettings = ConsumerSettings(List("localhost:9092")).withGroupId("group")
val producerSettings: ProducerSettings = ProducerSettings(List("localhost:9092"))

val consumerAndProducer = 
  ZLayer.scoped(Consumer.make(consumerSettings)) ++
    ZLayer.scoped(Producer.make(producerSettings, Serde.int, Serde.string))

val consumeProduceStream = Consumer
  .plainStream(Subscription.topics("my-input-topic"), Serde.int, Serde.long)
  .map { record =>
    val key: Int    = record.record.key()
    val value: Long = record.record.value()
    val newValue: String = value.toString

    val producerRecord: ProducerRecord[Int, String] = new ProducerRecord("my-output-topic", key, newValue)
    (producerRecord, record.offset)
  }
  .mapChunksZIO { chunk =>
    val records     = chunk.map(_._1)
    val offsetBatch = OffsetBatch(chunk.map(_._2).toSeq)

    Producer.produceChunk[Any, Int, String](records) *> offsetBatch.commit.as(Chunk(()))
  }
  .runDrain
  .provideSomeLayer(consumerAndProducer)
```
