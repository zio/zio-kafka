---
id: record-headers
title: "Record Headers"
---

A Kafka record carries an optional list of **headers** next to its key and value. Each header has a `String` key and an `Array[Byte]` value (which may be `null`). Headers keep the order in which they are added, and several headers can share a key. Applications use them for metadata that does not belong in the payload, such as a content type, a schema version, a trace id or the name of the producing service.

Zio-kafka has no header type of its own. It uses the Kafka client's `org.apache.kafka.common.header.Headers` and `Header` interfaces directly, and passes them along at every step:

- When producing, the headers of a `ProducerRecord` are written with the record.
- When consuming, `CommittableRecord#record` returns the Kafka `ConsumerRecord`, whose `headers()` method returns the headers.
- Every `Serializer` and `Deserializer` receives the record's headers as a parameter.

## Adding Headers When Producing

A `ProducerRecord` always has a mutable `Headers` collection, so the simplest way to add a header is `Headers#add` on `ProducerRecord#headers`. To produce a record with two headers:

```scala mdoc:compile-only
import zio._
import zio.kafka.producer._
import zio.kafka.serde._
import org.apache.kafka.clients.producer.{ ProducerRecord, RecordMetadata }
import java.nio.charset.StandardCharsets.UTF_8

def produceOrder(producer: Producer): Task[RecordMetadata] = {
  val record = new ProducerRecord("orders", "order-1", "created")
  record.headers().add("source", "web-shop".getBytes(UTF_8))
  record.headers().add("trace-id", "4bf92f3577b34da6".getBytes(UTF_8))
  producer.produce(record, Serde.string, Serde.string)
}
```

You can also pass the headers to the `ProducerRecord` constructor, which accepts any `java.lang.Iterable[Header]` after the topic, partition, timestamp, key and value. Pass `null` for the partition and timestamp to let Kafka choose them. Kafka's own implementations are `RecordHeader` and `RecordHeaders` in the `org.apache.kafka.common.header.internals` package. That package is not part of Kafka's documented public API, but the classes are widely used (zio-kafka's own tests use them). To build the headers first and then the record:

```scala mdoc:compile-only
import zio._
import zio.kafka.producer._
import zio.kafka.serde._
import org.apache.kafka.clients.producer.{ ProducerRecord, RecordMetadata }
import org.apache.kafka.common.header.internals.RecordHeaders
import java.nio.charset.StandardCharsets.UTF_8

def produceWithHeaders(producer: Producer): Task[RecordMetadata] = {
  val headers = new RecordHeaders()
    .add("source", "web-shop".getBytes(UTF_8))
    .add("schema-version", "2".getBytes(UTF_8))

  val record = new ProducerRecord[String, String]("orders", null, null, "order-1", "created", headers)
  producer.produce(record, Serde.string, Serde.string)
}
```

Headers work the same with every produce method of the [Producer](producer.md), including the chunk variants and `Producer#produceAll`, and with the [transactional producer](transactions.md): `Transaction#produce`, `Transaction#produceChunk` and `Transaction#produceChunkBatch` send records through the same code path.

:::warning[Build a New Record for Every Send]
When the Kafka client sends a record, it makes the record's `Headers` read-only, and a later `Headers#add` on them throws an `IllegalStateException`. The produce methods that take serializers send a new `ProducerRecord` with the serialized key and value, but that record shares the `Headers` object of the record you passed in, so your record's headers become read-only too. Producing the same `ProducerRecord` again therefore fails as soon as something adds a header to it, for example a serializer that adds headers (see [Header-Aware Serializers and Deserializers](#header-aware-serializers-and-deserializers)). Build a new `ProducerRecord` for every send.
:::

## Reading Headers When Consuming

A `CommittableRecord` has no header accessor of its own; read them from the wrapped Kafka record with `record.record.headers()`. The `Headers` interface offers `Headers#lastHeader` for the most recent header with a key (or `null` when there is none), `Headers#headers` for all headers with a key, and iteration over all headers. Since both a missing header and a header value can be `null`, wrap them in `Option`. To read a header as a string and to list all headers of a record:

```scala mdoc:compile-only
import zio.kafka.consumer._
import scala.jdk.CollectionConverters._
import java.nio.charset.StandardCharsets.UTF_8

def headerValue[K, V](record: CommittableRecord[K, V], key: String): Option[String] =
  Option(record.record.headers().lastHeader(key))
    .flatMap(header => Option(header.value()))
    .map(bytes => new String(bytes, UTF_8))

def allHeaders[K, V](record: CommittableRecord[K, V]): List[(String, Option[String])] =
  record.record
    .headers()
    .asScala
    .map(header => header.key() -> Option(header.value()).map(new String(_, UTF_8)))
    .toList
```

In a consumer stream, read the headers in an operator that keeps the chunk structure, such as `ZStream#map`. To pair each record's `source` header with its offset for later committing:

```scala mdoc:compile-only
import zio.stream._
import zio.kafka.consumer._
import zio.kafka.serde._
import java.nio.charset.StandardCharsets.UTF_8

def sources(consumer: Consumer): ZStream[Any, Throwable, (String, Offset)] =
  consumer
    .plainStream(Subscription.topics("orders"), Serde.string, Serde.string)
    .map { record =>
      val source = Option(record.record.headers().lastHeader("source"))
        .flatMap(header => Option(header.value()))
        .map(new String(_, UTF_8))
        .getOrElse("unknown")
      (source, record.offset)
    }
```

`Consumer#consumeWith` hands your function a `ConsumerRecord` directly, so there you call `record.headers()`. See [Consumer](consumer.md) for the stream methods and [Offsets and Committing](offsets-and-committing.md) for committing the collected offsets.

## Header-Aware Serializers and Deserializers

Every `Serializer` and `Deserializer` receives the topic and the headers along with the value, so the encoding of a record can depend on its headers. The function-based constructors `Serializer.apply` and `Deserializer.apply`, described in [From Functions](serialization-and-deserialization.md#from-functions), pass the headers to your function as the second parameter.

A serializer may also add headers. When producing with a serializer, zio-kafka runs the key serializer and then the value serializer on the record's headers, and writes the record with the headers as they are afterwards. A deserializer receives the headers of the consumed record, so it can choose how to decode the bytes. The pair below writes a `content-type` header and reads it back, to tell JSON from plain text:

```scala mdoc:compile-only
import zio._
import zio.kafka.serde._
import java.nio.charset.StandardCharsets.UTF_8

val ContentTypeHeader = "content-type"

val jsonTextSerializer: Serializer[Any, String] =
  Serializer { (_, headers, json) =>
    ZIO.attempt {
      headers.add(ContentTypeHeader, "application/json".getBytes(UTF_8))
      json.getBytes(UTF_8)
    }
  }

val contentTypeDeserializer: Deserializer[Any, (Option[String], String)] =
  Deserializer { (_, headers, data) =>
    val contentType = Option(headers.lastHeader(ContentTypeHeader))
      .flatMap(header => Option(header.value()))
      .map(new String(_, UTF_8))
    ZIO.succeed((contentType, new String(data, UTF_8)))
  }
```

Because such a serializer adds a header to the `ProducerRecord` you pass in, produce a new record for every send, as explained in the warning under [Adding Headers When Producing](#adding-headers-when-producing). The predefined serdes such as `Serde.string` ignore headers. See [Serialization and Deserialization](serialization-and-deserialization.md) for more ways to build serdes.

The deserializers you pass to `Consumer#plainStream` and the other stream methods receive the record's headers. So do the deserializers you pass to `CommittableRecord#deserializeWith`, which deserializes a record of raw bytes later in the stream and keeps its headers; see [CommittableRecord](consumer.md#committablerecord).
