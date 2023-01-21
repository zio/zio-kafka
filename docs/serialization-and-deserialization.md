---
id: serialization-and-deserialization
title: "Serialization And Deserialization"
---

## Custom Data Type Serdes

Serializers and deserializers (serdes) for custom data types can be constructed from scratch or by converting existing serdes. For example, to create a serde for an `Instant`:

```scala
import java.time.Instant
import zio.kafka.serde._

val instantSerde: Serde[Any, Instant] = Serde.long.inmap(java.time.Instant.ofEpochMilli)(_.toEpochMilli)
```

## Handling deserialization failures

The default behavior for a consumer stream when encountering a deserialization failure is to fail the stream. In many cases you may want to handle this situation differently, e.g. by skipping the message that failed to deserialize or by executing an alternative effect. For this purpose, any `Deserializer[T]` for some type `T` can be easily converted into a `Deserializer[Try[T]]` where deserialization failures are converted to a `Failure` using the `asTry` method.

Below is an example of skipping messages that fail to deserialize. The offset is passed downstream to be committed.

```scala
import zio._, stream._
import zio.kafka.consumer._
import zio.kafka.serde._
import scala.util.{Try, Success, Failure}

val consumer = ZLayer.scoped(Consumer.make(consumerSettings))

val stream = Consumer
  .plainStream(Subscription.topics("topic150"), Serde.string, Serde.string.asTry)

stream 
  .mapZIO { record => 
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
