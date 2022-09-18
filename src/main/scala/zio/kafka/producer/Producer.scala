package zio.kafka.producer

import org.apache.kafka.clients.producer.{
  Callback,
  KafkaProducer,
  Producer => JProducer,
  ProducerRecord,
  RecordMetadata
}
import org.apache.kafka.common.serialization.ByteArraySerializer
import org.apache.kafka.common.{ Metric, MetricName }
import zio._
import zio.kafka.serde.Serializer
import zio.stream.ZPipeline

import java.util.concurrent.atomic.AtomicLong
import scala.jdk.CollectionConverters._

trait Producer {

  /**
   * Produces a single record and await broker acknowledgement. See [[produceAsync[R,K,V](record*]] for version that
   * allows to avoid round-trip-time penalty for each record.
   */
  def produce[R, K, V](
    record: ProducerRecord[K, V],
    keySerializer: Serializer[R, K],
    valueSerializer: Serializer[R, V]
  ): RIO[R, RecordMetadata]

  /**
   * Produces a single record and await broker acknowledgement. See [[produceAsync[R,K,V](topic:String*]] for version
   * that allows to avoid round-trip-time penalty for each record.
   */
  def produce[R, K, V](
    topic: String,
    key: K,
    value: V,
    keySerializer: Serializer[R, K],
    valueSerializer: Serializer[R, V]
  ): RIO[R, RecordMetadata]

  /**
   * A stream pipeline that produces all records from the stream.
   */
  def produceAll[R, K, V](
    keySerializer: Serializer[R, K],
    valueSerializer: Serializer[R, V]
  ): ZPipeline[R with Producer, Throwable, ProducerRecord[K, V], RecordMetadata] =
    ZPipeline.mapChunksZIO(records => produceChunk(records, keySerializer, valueSerializer))

  /**
   * Produces a single record. The effect returned from this method has two layers and describes the completion of two
   * actions:
   *   1. The outer layer describes the enqueueing of the record to the Producer's internal buffer. 2. The inner layer
   *      describes receiving an acknowledgement from the broker for the transmission of the record.
   *
   * It is usually recommended to not await the inner layer of every individual record, but enqueue a batch of records
   * and await all of their acknowledgements at once. That amortizes the cost of sending requests to Kafka and increases
   * throughput. See [[produce[R,K,V](record*]] for version that awaits broker acknowledgement.
   */
  def produceAsync[R, K, V](
    record: ProducerRecord[K, V],
    keySerializer: Serializer[R, K],
    valueSerializer: Serializer[R, V]
  ): RIO[R, Task[RecordMetadata]]

  /**
   * Produces a single record. The effect returned from this method has two layers and describes the completion of two
   * actions:
   *   1. The outer layer describes the enqueueing of the record to the Producer's internal buffer. 2. The inner layer
   *      describes receiving an acknowledgement from the broker for the transmission of the record.
   *
   * It is usually recommended to not await the inner layer of every individual record, but enqueue a batch of records
   * and await all of their acknowledgements at once. That amortizes the cost of sending requests to Kafka and increases
   * throughput. See [[produce[R,K,V](topic*]] for version that awaits broker acknowledgement.
   */
  def produceAsync[R, K, V](
    topic: String,
    key: K,
    value: V,
    keySerializer: Serializer[R, K],
    valueSerializer: Serializer[R, V]
  ): RIO[R, Task[RecordMetadata]]

  /**
   * Produces a chunk of records. The effect returned from this method has two layers and describes the completion of
   * two actions:
   *   1. The outer layer describes the enqueueing of all the records to the Producer's internal buffer. 2. The inner
   *      layer describes receiving an acknowledgement from the broker for the transmission of the records.
   *
   * It is possible that for chunks that exceed the producer's internal buffer size, the outer layer will also signal
   * the transmission of part of the chunk. Regardless, awaiting the inner layer guarantees the transmission of the
   * entire chunk.
   */
  def produceChunkAsync[R, K, V](
    records: Chunk[ProducerRecord[K, V]],
    keySerializer: Serializer[R, K],
    valueSerializer: Serializer[R, V]
  ): RIO[R, Task[Chunk[RecordMetadata]]]

  /**
   * Produces a chunk of records. See [[produceChunkAsync]] for version that allows to avoid round-trip-time penalty for
   * each chunk.
   */
  def produceChunk[R, K, V](
    records: Chunk[ProducerRecord[K, V]],
    keySerializer: Serializer[R, K],
    valueSerializer: Serializer[R, V]
  ): RIO[R, Chunk[RecordMetadata]]

  /**
   * Flushes the producer's internal buffer. This will guarantee that all records currently buffered will be transmitted
   * to the broker.
   */
  def flush: Task[Unit]

  /**
   * Expose internal producer metrics
   */
  def metrics: Task[Map[MetricName, Metric]]
}

object Producer {

  private[producer] final case class Live(
    p: JProducer[Array[Byte], Array[Byte]],
    producerSettings: ProducerSettings
  ) extends Producer {

    override def produceAsync[R, K, V](
      record: ProducerRecord[K, V],
      keySerializer: Serializer[R, K],
      valueSerializer: Serializer[R, V]
    ): RIO[R, Task[RecordMetadata]] =
      for {
        done             <- Promise.make[Throwable, RecordMetadata]
        serializedRecord <- serialize(record, keySerializer, valueSerializer)
        runtime          <- ZIO.runtime[Any]
        _ <- ZIO.attemptBlocking {
               p.send(
                 serializedRecord,
                 new Callback {
                   def onCompletion(metadata: RecordMetadata, err: Exception): Unit =
                     Unsafe.unsafe { implicit u =>
                       if (err != null) runtime.unsafe.run(done.fail(err)).getOrThrowFiberFailure()
                       else runtime.unsafe.run(done.succeed(metadata)).getOrThrowFiberFailure()
                       ()
                     }
                 }
               )
             }
      } yield done.await

    override def produceChunkAsync[R, K, V](
      records: Chunk[ProducerRecord[K, V]],
      keySerializer: Serializer[R, K],
      valueSerializer: Serializer[R, V]
    ): RIO[R, Task[Chunk[RecordMetadata]]] =
      if (records.isEmpty) ZIO.succeed(ZIO.succeed(Chunk.empty))
      else {
        for {
          done              <- Promise.make[Throwable, Chunk[RecordMetadata]]
          runtime           <- ZIO.runtime[Any]
          serializedRecords <- ZIO.foreach(records.toSeq)(serialize(_, keySerializer, valueSerializer))
          _ <- ZIO.attemptBlocking {
                 val it: Iterator[(ByteRecord, Int)] =
                   serializedRecords.iterator.zipWithIndex
                 val res: Array[RecordMetadata] = new Array[RecordMetadata](records.length)
                 val count: AtomicLong          = new AtomicLong

                 while (it.hasNext) {
                   val (rec, idx): (ByteRecord, Int) = it.next()

                   p.send(
                     rec,
                     new Callback {
                       def onCompletion(metadata: RecordMetadata, err: Exception): Unit =
                         Unsafe.unsafe { implicit u =>
                           if (err != null) runtime.unsafe.run(done.fail(err)).getOrThrowFiberFailure()
                           else {
                             res(idx) = metadata
                             if (count.incrementAndGet == records.length)
                               runtime.unsafe.run(done.succeed(Chunk.fromArray(res))).getOrThrowFiberFailure()
                           }

                           ()
                         }
                     }
                   )
                 }
               }
        } yield done.await
      }

    override def produce[R, K, V](
      record: ProducerRecord[K, V],
      keySerializer: Serializer[R, K],
      valueSerializer: Serializer[R, V]
    ): RIO[R, RecordMetadata] =
      produceAsync(record, keySerializer, valueSerializer).flatten

    override def produce[R, K, V](
      topic: String,
      key: K,
      value: V,
      keySerializer: Serializer[R, K],
      valueSerializer: Serializer[R, V]
    ): RIO[R, RecordMetadata] =
      produce(new ProducerRecord(topic, key, value), keySerializer, valueSerializer)

    override def produceAsync[R, K, V](
      topic: String,
      key: K,
      value: V,
      keySerializer: Serializer[R, K],
      valueSerializer: Serializer[R, V]
    ): RIO[R, Task[RecordMetadata]] =
      produceAsync(new ProducerRecord(topic, key, value), keySerializer, valueSerializer)

    override def produceChunk[R, K, V](
      records: Chunk[ProducerRecord[K, V]],
      keySerializer: Serializer[R, K],
      valueSerializer: Serializer[R, V]
    ): RIO[R, Chunk[RecordMetadata]] =
      produceChunkAsync(records, keySerializer, valueSerializer).flatten

    override def flush: Task[Unit] = ZIO.attemptBlocking(p.flush())

    override def metrics: Task[Map[MetricName, Metric]] = ZIO.attemptBlocking(p.metrics().asScala.toMap)

    private def serialize[R, K, V](
      r: ProducerRecord[K, V],
      keySerializer: Serializer[R, K],
      valueSerializer: Serializer[R, V]
    ): RIO[R, ByteRecord] =
      for {
        key   <- keySerializer.serialize(r.topic, r.headers, r.key())
        value <- valueSerializer.serialize(r.topic, r.headers, r.value())
      } yield new ProducerRecord(r.topic, r.partition(), r.timestamp(), key, value, r.headers)

    private[producer] def close: UIO[Unit] = ZIO.succeed(p.close(producerSettings.closeTimeout))
  }

  val live: RLayer[ProducerSettings, Producer] =
    ZLayer.scoped {
      for {
        settings <- ZIO.service[ProducerSettings]
        producer <- make(settings)
      } yield producer
    }

  def make(settings: ProducerSettings): ZIO[Scope, Throwable, Producer] =
    fromManagedJavaProducer(javaProducerFromSettings(settings), settings)

  def fromJavaProducer(
    javaProducer: => JProducer[Array[Byte], Array[Byte]],
    settings: ProducerSettings
  ): UIO[Producer] =
    ZIO.succeed(Live(javaProducer, settings))

  def fromManagedJavaProducer[R, E](
    managedJavaProducer: ZIO[R & Scope, E, JProducer[Array[Byte], Array[Byte]]],
    settings: ProducerSettings
  ): ZIO[R with Scope, E, Producer] =
    managedJavaProducer.flatMap(javaProducer => fromJavaProducer(javaProducer, settings))

  def javaProducerFromSettings(
    settings: ProducerSettings
  ): ZIO[Scope, Throwable, JProducer[Array[Byte], Array[Byte]]] =
    ZIO.acquireRelease {
      for {
        props <- ZIO.attempt(settings.driverSettings)
        rawProducer <- ZIO.attempt(
                         new KafkaProducer[Array[Byte], Array[Byte]](
                           props.asJava,
                           new ByteArraySerializer(),
                           new ByteArraySerializer()
                         )
                       )
      } yield rawProducer
    } { producer =>
      ZIO.attempt(producer.close()).orDie
    }

  def withProducerService[R, A](
    r: Producer => RIO[R, A]
  ): RIO[R with Producer, A] =
    ZIO.serviceWithZIO[Producer](r)

  /**
   * Accessor method for [[Producer!.produce[R,K,V](record*]]
   */
  def produce[R, K, V](
    record: ProducerRecord[K, V],
    keySerializer: Serializer[R, K],
    valueSerializer: Serializer[R, V]
  ): RIO[R with Producer, RecordMetadata] =
    withProducerService(_.produce(record, keySerializer, valueSerializer))

  /**
   * Accessor method for [[Producer!.produce[R,K,V](topic*]]
   */
  def produce[R, K, V](
    topic: String,
    key: K,
    value: V,
    keySerializer: Serializer[R, K],
    valueSerializer: Serializer[R, V]
  ): RIO[R with Producer, RecordMetadata] =
    withProducerService(_.produce(topic, key, value, keySerializer, valueSerializer))

  /**
   * A stream pipeline that produces all records from the stream.
   */
  def produceAll[R, K, V](
    keySerializer: Serializer[R, K],
    valueSerializer: Serializer[R, V]
  ): ZPipeline[R with Producer, Throwable, ProducerRecord[K, V], RecordMetadata] =
    ZPipeline.mapChunksZIO(records => produceChunk(records, keySerializer, valueSerializer))

  /**
   * Accessor method for [[Producer!.produceAsync[R,K,V](record*]]
   */
  def produceAsync[R, K, V](
    record: ProducerRecord[K, V],
    keySerializer: Serializer[R, K],
    valueSerializer: Serializer[R, V]
  ): RIO[R with Producer, Task[RecordMetadata]] =
    withProducerService(_.produceAsync(record, keySerializer, valueSerializer))

  /**
   * Accessor method for [[Producer.produceAsync[R,K,V](topic*]]
   */
  def produceAsync[R, K, V](
    topic: String,
    key: K,
    value: V,
    keySerializer: Serializer[R, K],
    valueSerializer: Serializer[R, V]
  ): RIO[R with Producer, Task[RecordMetadata]] =
    withProducerService(_.produceAsync(topic, key, value, keySerializer, valueSerializer))

  /**
   * Accessor method for [[Producer.produceChunkAsync]]
   */
  def produceChunkAsync[R, K, V](
    records: Chunk[ProducerRecord[K, V]],
    keySerializer: Serializer[R, K],
    valueSerializer: Serializer[R, V]
  ): RIO[R with Producer, Task[Chunk[RecordMetadata]]] =
    withProducerService(_.produceChunkAsync(records, keySerializer, valueSerializer))

  /**
   * Accessor method for [[Producer.produceChunk]]
   */
  def produceChunk[R, K, V](
    records: Chunk[ProducerRecord[K, V]],
    keySerializer: Serializer[R, K],
    valueSerializer: Serializer[R, V]
  ): RIO[R with Producer, Chunk[RecordMetadata]] =
    withProducerService(_.produceChunk(records, keySerializer, valueSerializer))

  /**
   * Accessor method for [[Producer.flush]]
   */
  val flush: RIO[Producer, Unit] =
    ZIO.serviceWithZIO(_.flush)

  /**
   * Accessor method for [[Producer.metrics]]
   */
  val metrics: RIO[Producer, Map[MetricName, Metric]] =
    ZIO.serviceWithZIO(_.metrics)
}
