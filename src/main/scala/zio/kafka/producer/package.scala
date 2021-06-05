package zio.kafka

import java.util.concurrent.atomic.AtomicLong

import org.apache.kafka.clients.producer.{ Callback, KafkaProducer, ProducerRecord, RecordMetadata }
import org.apache.kafka.common.{ Metric, MetricName }
import org.apache.kafka.common.serialization.ByteArraySerializer
import zio._
import zio.blocking._
import zio.kafka.serde.Serializer
import zio.stream.ZTransducer

import scala.jdk.CollectionConverters._

package object producer {
  type ByteRecord = ProducerRecord[Array[Byte], Array[Byte]]

  type Producer[R, K, V] = Has[Producer.Service[R, K, V]]

  object Producer {
    trait Service[R, K, V] {

      /**
       * Produces a single record and await broker acknowledgement. See [[produceAsync(record*]] for
       * version that allows to avoid round-trip-time penalty for each record.
       */
      def produce(record: ProducerRecord[K, V]): RIO[R, RecordMetadata]

      /**
       * Produces a single record and await broker acknowledgement. See [[produceAsync(topic*]] for
       * version that allows to avoid round-trip-time penalty for each record.
       */
      def produce(topic: String, key: K, value: V): RIO[R, RecordMetadata]

      /**
       * A stream transducer that produces all records from the stream.
       */
      final val produceAll: ZTransducer[R, Throwable, ProducerRecord[K, V], RecordMetadata] =
        ZTransducer.fromPush {
          case None        => UIO.succeed(Chunk.empty)
          case Some(chunk) => produceChunk(chunk)
        }

      /**
       * Produces a single record. The effect returned from this method has two layers and
       * describes the completion of two actions:
       * 1. The outer layer describes the enqueueing of the record to the Producer's internal
       *    buffer.
       * 2. The inner layer describes receiving an acknowledgement from the broker for the
       *    transmission of the record.
       *
       * It is usually recommended to not await the inner layer of every individual record,
       * but enqueue a batch of records and await all of their acknowledgements at once. That
       * amortizes the cost of sending requests to Kafka and increases throughput.
       * See [[produce(record*]] for version that awaits broker acknowledgement.
       */
      def produceAsync(record: ProducerRecord[K, V]): RIO[R, Task[RecordMetadata]]

      /**
       * Produces a single record. The effect returned from this method has two layers and
       * describes the completion of two actions:
       * 1. The outer layer describes the enqueueing of the record to the Producer's internal
       *    buffer.
       * 2. The inner layer describes receiving an acknowledgement from the broker for the
       *    transmission of the record.
       *
       * It is usually recommended to not await the inner layer of every individual record,
       * but enqueue a batch of records and await all of their acknowledgements at once. That
       * amortizes the cost of sending requests to Kafka and increases throughput.
       * See [[produce(topic*]] for version that awaits broker acknowledgement.
       */
      def produceAsync(topic: String, key: K, value: V): RIO[R, Task[RecordMetadata]]

      /**
       * Produces a chunk of records. The effect returned from this method has two layers
       * and describes the completion of two actions:
       * 1. The outer layer describes the enqueueing of all the records to the Producer's
       *    internal buffer.
       * 2. The inner layer describes receiving an acknowledgement from the broker for the
       *    transmission of the records.
       *
       * It is possible that for chunks that exceed the producer's internal buffer size, the
       * outer layer will also signal the transmission of part of the chunk. Regardless,
       * awaiting the inner layer guarantees the transmission of the entire chunk.
       */
      def produceChunkAsync(records: Chunk[ProducerRecord[K, V]]): RIO[R, Task[Chunk[RecordMetadata]]]

      /**
       * Produces a chunk of records. See [[produceChunkAsync]] for version that allows
       * to avoid round-trip-time penalty for each chunk.
       */
      def produceChunk(records: Chunk[ProducerRecord[K, V]]): RIO[R, Chunk[RecordMetadata]]

      /**
       * Flushes the producer's internal buffer. This will guarantee that all records
       * currently buffered will be transmitted to the broker.
       */
      def flush: Task[Unit]

      /**
       * Expose internal producer metrics
       */
      def metrics: Task[Map[MetricName, Metric]]
    }

    final case class Live[R, K, V](
      p: KafkaProducer[Array[Byte], Array[Byte]],
      producerSettings: ProducerSettings,
      keySerializer: Serializer[R, K],
      valueSerializer: Serializer[R, V],
      blocking: Blocking.Service
    ) extends Service[R, K, V] {
      override def produceAsync(record: ProducerRecord[K, V]): RIO[R, Task[RecordMetadata]] =
        for {
          done             <- Promise.make[Throwable, RecordMetadata]
          serializedRecord <- serialize(record)
          runtime          <- ZIO.runtime[Any]
          _                <- blocking.effectBlocking {
                                p.send(
                                  serializedRecord,
                                  new Callback {
                                    def onCompletion(metadata: RecordMetadata, err: Exception): Unit = {
                                      if (err != null) runtime.unsafeRun(done.fail(err))
                                      else runtime.unsafeRun(done.succeed(metadata))

                                      ()
                                    }
                                  }
                                )
                              }
        } yield done.await

      override def produceChunkAsync(
        records: Chunk[ProducerRecord[K, V]]
      ): RIO[R, Task[Chunk[RecordMetadata]]] =
        if (records.isEmpty) ZIO.succeed(Task.succeed(Chunk.empty))
        else {
          for {
            done              <- Promise.make[Throwable, Chunk[RecordMetadata]]
            runtime           <- ZIO.runtime[Any]
            serializedRecords <- ZIO.foreach(records.toSeq)(serialize)
            _                 <- blocking.effectBlocking {
                                   val it: Iterator[(ByteRecord, Int)] =
                                     serializedRecords.iterator.zipWithIndex
                                   val res: Array[RecordMetadata]      = new Array[RecordMetadata](records.length)
                                   val count: AtomicLong               = new AtomicLong

                                   while (it.hasNext) {
                                     val (rec, idx): (ByteRecord, Int) = it.next

                                     p.send(
                                       rec,
                                       new Callback {
                                         def onCompletion(metadata: RecordMetadata, err: Exception): Unit = {
                                           if (err != null) runtime.unsafeRun(done.fail(err))
                                           else {
                                             res(idx) = metadata
                                             if (count.incrementAndGet == records.length)
                                               runtime.unsafeRun(done.succeed(Chunk.fromArray(res)))
                                           }

                                           ()
                                         }
                                       }
                                     )
                                   }
                                 }
          } yield done.await
        }

      override def produce(record: ProducerRecord[K, V]): RIO[R, RecordMetadata] =
        produceAsync(record).flatten

      override def produce(topic: String, key: K, value: V): RIO[R, RecordMetadata] =
        produce(new ProducerRecord(topic, key, value))

      override def produceAsync(topic: String, key: K, value: V): RIO[R, Task[RecordMetadata]] =
        produceAsync(new ProducerRecord(topic, key, value))

      override def produceChunk(
        records: Chunk[ProducerRecord[K, V]]
      ): RIO[R, Chunk[RecordMetadata]] =
        produceChunkAsync(records).flatten

      override def flush: Task[Unit] = blocking.effectBlocking(p.flush())

      override def metrics: Task[Map[MetricName, Metric]] = blocking.effectBlocking(p.metrics().asScala.toMap)

      private def serialize(r: ProducerRecord[K, V]): RIO[R, ByteRecord] =
        for {
          key   <- keySerializer.serialize(r.topic, r.headers, r.key())
          value <- valueSerializer.serialize(r.topic, r.headers, r.value())
        } yield new ProducerRecord(r.topic, r.partition(), r.timestamp(), key, value, r.headers)

      private[producer] def close: UIO[Unit] = UIO(p.close(producerSettings.closeTimeout))
    }

    def live[R: Tag, K: Tag, V: Tag]: RLayer[Has[Serializer[R, K]] with Has[Serializer[R, V]] with Has[
      ProducerSettings
    ] with Blocking, Producer[R, K, V]] =
      (for {
        keySerializer   <- ZManaged.service[Serializer[R, K]]
        valueSerializer <- ZManaged.service[Serializer[R, V]]
        settings        <- ZManaged.service[ProducerSettings]
        producer        <- make(settings, keySerializer, valueSerializer)
      } yield producer).toLayer

    def make[R, K, V](
      settings: ProducerSettings,
      keySerializer: Serializer[R, K],
      valueSerializer: Serializer[R, V]
    ): RManaged[Blocking, Service[R, K, V]] =
      (for {
        props       <- ZIO.effect(settings.driverSettings)
        _           <- keySerializer.configure(props, isKey = true)
        _           <- valueSerializer.configure(props, isKey = false)
        blocking    <- ZIO.service[Blocking.Service]
        rawProducer <- ZIO.effect(
                         new KafkaProducer[Array[Byte], Array[Byte]](
                           props.asJava,
                           new ByteArraySerializer(),
                           new ByteArraySerializer()
                         )
                       )
      } yield Live(rawProducer, settings, keySerializer, valueSerializer, blocking)).toManaged(_.close)

    def withProducerService[R: Tag, K: Tag, V: Tag, A](
      r: Producer.Service[R, K, V] => RIO[R, A]
    ): RIO[R with Producer[R, K, V], A] =
      ZIO.accessM(env => r(env.get[Producer.Service[R, K, V]]))

    /**
     * Accessor method for [[Service.produce(record*]]
     */
    def produce[R: Tag, K: Tag, V: Tag](
      record: ProducerRecord[K, V]
    ): RIO[R with Producer[R, K, V], RecordMetadata] =
      withProducerService(_.produce(record))

    /**
     * Accessor method for [[Service.produce(topic*]]
     */
    def produce[R: Tag, K: Tag, V: Tag](
      topic: String,
      key: K,
      value: V
    ): RIO[R with Producer[R, K, V], RecordMetadata] =
      withProducerService(_.produce(topic, key, value))

    /**
     * A stream transducer that produces all records from the stream.
     */
    def produceAll[R: Tag, K: Tag, V: Tag]
      : ZTransducer[R with Producer[R, K, V], Throwable, ProducerRecord[K, V], RecordMetadata] =
      ZTransducer.fromPush {
        case None        => UIO.succeed(Chunk.empty)
        case Some(chunk) =>
          produceChunk[R, K, V](chunk)
      }

    /**
     * Accessor method for [[Service.produceAsync(record*]]
     */
    def produceAsync[R: Tag, K: Tag, V: Tag](
      record: ProducerRecord[K, V]
    ): RIO[R with Producer[R, K, V], Task[RecordMetadata]] =
      withProducerService(_.produceAsync(record))

    /**
     * Accessor method for [[Service.produceAsync(topic*]]
     */
    def produceAsync[R: Tag, K: Tag, V: Tag](
      topic: String,
      key: K,
      value: V
    ): RIO[R with Producer[R, K, V], Task[RecordMetadata]] =
      withProducerService(_.produceAsync(topic, key, value))

    /**
     * Accessor method for [[Service.produceChunkAsync]]
     */
    def produceChunkAsync[R: Tag, K: Tag, V: Tag](
      records: Chunk[ProducerRecord[K, V]]
    ): RIO[R with Producer[R, K, V], Task[Chunk[RecordMetadata]]] =
      withProducerService(_.produceChunkAsync(records))

    /**
     * Accessor method for [[Service.produceChunk]]
     */
    def produceChunk[R: Tag, K: Tag, V: Tag](
      records: Chunk[ProducerRecord[K, V]]
    ): RIO[R with Producer[R, K, V], Chunk[RecordMetadata]] =
      withProducerService(_.produceChunk(records))

    /**
     * Accessor method for [[Service.flush]]
     */
    def flush[R: Tag, K: Tag, V: Tag]: RIO[R with Producer[R, K, V], Unit] =
      withProducerService(_.flush)

    /**
     * Accessor method for [[Service.metrics]]
     */
    def metrics[R: Tag, K: Tag, V: Tag]: RIO[R with Producer[R, K, V], Map[MetricName, Metric]] =
      withProducerService(_.metrics)
  }
}
