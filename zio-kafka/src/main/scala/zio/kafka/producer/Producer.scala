package zio.kafka.producer

import org.apache.kafka.clients.producer.{ KafkaProducer, Producer => JProducer, ProducerRecord, RecordMetadata }
import org.apache.kafka.common.serialization.ByteArraySerializer
import org.apache.kafka.common.{ Metric, MetricName }
import zio._
import zio.kafka.serde.Serializer
import zio.kafka.utils.SslHelper
import zio.stream.{ ZPipeline, ZStream }

import java.util.concurrent.atomic.AtomicLong
import scala.jdk.CollectionConverters._
import scala.util.control.NonFatal

trait Producer {

  /**
   * Produces a single record and await broker acknowledgement. See [[produceAsync[R,K,V](topic:String*]] for version
   * that allows to avoid round-trip-time penalty for each record.
   */
  def produce(
    record: ProducerRecord[Array[Byte], Array[Byte]]
  ): Task[RecordMetadata]

  /**
   * Produces a single record and await broker acknowledgement. See [[produceAsync[R,K,V](topic:String*]] for version
   * that allows to avoid round-trip-time penalty for each record.
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
  final def produceAll[R, K, V](
    keySerializer: Serializer[R, K],
    valueSerializer: Serializer[R, V]
  ): ZPipeline[R, Throwable, ProducerRecord[K, V], RecordMetadata] =
    ZPipeline.mapChunksZIO(records => produceChunk(records, keySerializer, valueSerializer))

  /**
   * Produces a single record. The effect returned from this method has two layers and describes the completion of two
   * actions:
   *   1. The outer layer describes the enqueueing of the record to the Producer's internal buffer.
   *   1. The inner layer describes receiving an acknowledgement from the broker for the transmission of the record.
   *
   * It is usually recommended to not await the inner layer of every individual record, but enqueue a batch of records
   * and await all of their acknowledgements at once. That amortizes the cost of sending requests to Kafka and increases
   * throughput. See [[produce[R,K,V](record*]] for version that awaits broker acknowledgement.
   */
  def produceAsync(
    record: ProducerRecord[Array[Byte], Array[Byte]]
  ): Task[Task[RecordMetadata]]

  /**
   * Produces a single record. The effect returned from this method has two layers and describes the completion of two
   * actions:
   *   1. The outer layer describes the enqueueing of the record to the Producer's internal buffer.
   *   1. The inner layer describes receiving an acknowledgement from the broker for the transmission of the record.
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
   *   1. The outer layer describes the enqueueing of the record to the Producer's internal buffer.
   *   1. The inner layer describes receiving an acknowledgement from the broker for the transmission of the record.
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
   * Produces a chunk of records. See [[produceChunkAsync(records*]] for version that allows to avoid round-trip-time
   * penalty for each chunk.
   */
  def produceChunk(
    records: Chunk[ProducerRecord[Array[Byte], Array[Byte]]]
  ): Task[Chunk[RecordMetadata]]

  /**
   * Produces a chunk of records. See [[produceChunkAsync(records*]] for version that allows to avoid round-trip-time
   * penalty for each chunk.
   */
  def produceChunk[R, K, V](
    records: Chunk[ProducerRecord[K, V]],
    keySerializer: Serializer[R, K],
    valueSerializer: Serializer[R, V]
  ): RIO[R, Chunk[RecordMetadata]]

  /**
   * Produces a chunk of records. The effect returned from this method has two layers and describes the completion of
   * two actions:
   *   1. The outer layer describes the enqueueing of all the records to the Producer's internal buffer.
   *   1. The inner layer describes receiving an acknowledgement from the broker for the transmission of the records.
   *
   * It is possible that for chunks that exceed the producer's internal buffer size, the outer layer will also signal
   * the transmission of part of the chunk. Regardless, awaiting the inner layer guarantees the transmission of the
   * entire chunk.
   */
  def produceChunkAsync(
    records: Chunk[ProducerRecord[Array[Byte], Array[Byte]]]
  ): Task[Task[Chunk[RecordMetadata]]]

  /**
   * Produces a chunk of records. The effect returned from this method has two layers and describes the completion of
   * two actions:
   *   1. The outer layer describes the enqueueing of all the records to the Producer's internal buffer.
   *   1. The inner layer describes receiving an acknowledgement from the broker for the transmission of the records.
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
   * Produces a chunk of records. The effect returned from this method has two layers and describes the completion of
   * two actions:
   *   1. The outer layer describes the enqueueing of all the records to the Producer's internal buffer.
   *   1. The inner layer describes receiving an acknowledgement from the broker for the transmission of the records.
   *
   * It is possible that for chunks that exceed the producer's internal buffer size, the outer layer will also signal
   * the transmission of part of the chunk. Regardless, awaiting the inner layer guarantees the transmission of the
   * entire chunk.
   *
   * This variant of `produceChunkAsync` more accurately reflects that individual records within the Chunk can fail to
   * publish, rather than the failure being at the level of the Chunk.
   *
   * This variant does not accept serializers as they may also fail independently of each record and this is not
   * reflected in the return type.
   */
  def produceChunkAsyncWithFailures(
    records: Chunk[ProducerRecord[Array[Byte], Array[Byte]]]
  ): UIO[UIO[Chunk[Either[Throwable, RecordMetadata]]]]

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
  val live: RLayer[ProducerSettings, Producer] =
    ZLayer.scoped {
      for {
        settings <- ZIO.service[ProducerSettings]
        producer <- make(settings)
      } yield producer
    }

  def make(settings: ProducerSettings): ZIO[Scope, Throwable, Producer] =
    for {
      props <- ZIO.attempt(settings.driverSettings)
      _     <- SslHelper.validateEndpoint(settings.bootstrapServers, props)
      rawProducer <- ZIO.acquireRelease(
                       ZIO.attempt(
                         new KafkaProducer[Array[Byte], Array[Byte]](
                           props.asJava,
                           new ByteArraySerializer(),
                           new ByteArraySerializer()
                         )
                       )
                     )(p => ZIO.attemptBlocking(p.close(settings.closeTimeout)).orDie)
      producer <- fromJavaProducer(rawProducer, settings)
    } yield producer

  /**
   * Create a zio-kafka Producer from an existing org.apache.kafka KafkaProducer
   *
   * You are responsible for creating and closing the KafkaProducer
   */
  def fromJavaProducer(
    javaProducer: KafkaProducer[Array[Byte], Array[Byte]],
    settings: ProducerSettings
  ): ZIO[Scope, Throwable, Producer] =
    for {
      runtime <- ZIO.runtime[Any]
      sendQueue <-
        Queue.bounded[(Chunk[ByteRecord], Promise[Nothing, Chunk[Either[Throwable, RecordMetadata]]])](
          settings.sendBufferSize
        )
      producer = new ProducerLive(javaProducer, runtime, sendQueue)
      _ <- ZIO.blocking(producer.sendFromQueue).forkScoped
    } yield producer

  /**
   * Create a zio-kafka Producer from an existing org.apache.kafka Producer
   *
   * You are responsible for creating and closing the Producer
   */
  def fromJavaProducer(
    javaProducer: JProducer[Array[Byte], Array[Byte]],
    settings: ProducerSettings
  ): ZIO[Scope, Throwable, Producer] =
    for {
      runtime <- ZIO.runtime[Any]
      sendQueue <-
        Queue.bounded[(Chunk[ByteRecord], Promise[Nothing, Chunk[Either[Throwable, RecordMetadata]]])](
          settings.sendBufferSize
        )
      producer = new ProducerLive(javaProducer, runtime, sendQueue)
      _ <- ZIO.blocking(producer.sendFromQueue).forkScoped
    } yield producer

  /**
   * Accessor method
   */
  def produce(
    record: ProducerRecord[Array[Byte], Array[Byte]]
  ): RIO[Producer, RecordMetadata] =
    ZIO.serviceWithZIO[Producer](_.produce(record))

  /**
   * Accessor method for [[Producer!.produce[R,K,V](record*]]
   */
  def produce[R, K, V](
    record: ProducerRecord[K, V],
    keySerializer: Serializer[R, K],
    valueSerializer: Serializer[R, V]
  ): RIO[R & Producer, RecordMetadata] =
    ZIO.serviceWithZIO[Producer](_.produce(record, keySerializer, valueSerializer))

  /**
   * Accessor method for [[Producer!.produce[R,K,V](topic*]]
   */
  def produce[R, K, V](
    topic: String,
    key: K,
    value: V,
    keySerializer: Serializer[R, K],
    valueSerializer: Serializer[R, V]
  ): RIO[R & Producer, RecordMetadata] =
    ZIO.serviceWithZIO[Producer](_.produce(topic, key, value, keySerializer, valueSerializer))

  /**
   * A stream pipeline that produces all records from the stream.
   */
  def produceAll[R, K, V](
    keySerializer: Serializer[R, K],
    valueSerializer: Serializer[R, V]
  ): ZPipeline[R & Producer, Throwable, ProducerRecord[K, V], RecordMetadata] =
    ZPipeline.mapChunksZIO(records => produceChunk(records, keySerializer, valueSerializer))

  /**
   * Accessor method
   */
  def produceAsync(
    record: ProducerRecord[Array[Byte], Array[Byte]]
  ): RIO[Producer, Task[RecordMetadata]] =
    ZIO.serviceWithZIO[Producer](_.produceAsync(record))

  /**
   * Accessor method
   */
  def produceAsync[R, K, V](
    record: ProducerRecord[K, V],
    keySerializer: Serializer[R, K],
    valueSerializer: Serializer[R, V]
  ): RIO[R & Producer, Task[RecordMetadata]] =
    ZIO.serviceWithZIO[Producer](_.produceAsync(record, keySerializer, valueSerializer))

  /**
   * Accessor method
   */
  def produceAsync[R, K, V](
    topic: String,
    key: K,
    value: V,
    keySerializer: Serializer[R, K],
    valueSerializer: Serializer[R, V]
  ): RIO[R & Producer, Task[RecordMetadata]] =
    ZIO.serviceWithZIO[Producer](_.produceAsync(topic, key, value, keySerializer, valueSerializer))

  /**
   * Accessor method
   */
  def produceChunkAsync(
    records: Chunk[ProducerRecord[Array[Byte], Array[Byte]]]
  ): RIO[Producer, Task[Chunk[RecordMetadata]]] =
    ZIO.serviceWithZIO[Producer](_.produceChunkAsync(records))

  /**
   * Accessor method
   */
  def produceChunkAsync[R, K, V](
    records: Chunk[ProducerRecord[K, V]],
    keySerializer: Serializer[R, K],
    valueSerializer: Serializer[R, V]
  ): RIO[R & Producer, Task[Chunk[RecordMetadata]]] =
    ZIO.serviceWithZIO[Producer](_.produceChunkAsync(records, keySerializer, valueSerializer))

  /**
   * Accessor method for [[Producer.produceChunkAsyncWithFailures]]]
   */
  def produceChunkAsyncWithFailures(
    records: Chunk[ProducerRecord[Array[Byte], Array[Byte]]]
  ): RIO[Producer, UIO[Chunk[Either[Throwable, RecordMetadata]]]] =
    ZIO.serviceWithZIO[Producer](_.produceChunkAsyncWithFailures(records))

  /**
   * Accessor method
   */
  def produceChunk(
    records: Chunk[ProducerRecord[Array[Byte], Array[Byte]]]
  ): RIO[Producer, Chunk[RecordMetadata]] =
    ZIO.serviceWithZIO[Producer](_.produceChunk(records))

  /**
   * Accessor method
   */
  def produceChunk[R, K, V](
    records: Chunk[ProducerRecord[K, V]],
    keySerializer: Serializer[R, K],
    valueSerializer: Serializer[R, V]
  ): RIO[R & Producer, Chunk[RecordMetadata]] =
    ZIO.serviceWithZIO[Producer](_.produceChunk(records, keySerializer, valueSerializer))

  /**
   * Accessor method
   */
  val flush: RIO[Producer, Unit] = ZIO.serviceWithZIO(_.flush)

  /**
   * Accessor method
   */
  val metrics: RIO[Producer, Map[MetricName, Metric]] = ZIO.serviceWithZIO(_.metrics)

}

private[producer] final class ProducerLive(
  private[producer] val p: JProducer[Array[Byte], Array[Byte]],
  runtime: Runtime[Any],
  sendQueue: Queue[(Chunk[ByteRecord], Promise[Nothing, Chunk[Either[Throwable, RecordMetadata]]])]
) extends Producer {

  override def produce(record: ProducerRecord[Array[Byte], Array[Byte]]): Task[RecordMetadata] =
    produceAsync(record).flatten

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

  // noinspection YieldingZIOEffectInspection
  override def produceAsync(record: ProducerRecord[Array[Byte], Array[Byte]]): Task[Task[RecordMetadata]] =
    for {
      done <- Promise.make[Nothing, Chunk[Either[Throwable, RecordMetadata]]]
      _    <- sendQueue.offer((Chunk.single(record), done))
    } yield done.await.flatMap(result => ZIO.fromEither(result.head))

  override def produceAsync[R, K, V](
    record: ProducerRecord[K, V],
    keySerializer: Serializer[R, K],
    valueSerializer: Serializer[R, V]
  ): RIO[R, Task[RecordMetadata]] =
    serialize(record, keySerializer, valueSerializer).flatMap(produceAsync)

  override def produceAsync[R, K, V](
    topic: String,
    key: K,
    value: V,
    keySerializer: Serializer[R, K],
    valueSerializer: Serializer[R, V]
  ): RIO[R, Task[RecordMetadata]] =
    produceAsync(new ProducerRecord(topic, key, value), keySerializer, valueSerializer)

  override def produceChunk(records: Chunk[ProducerRecord[Array[Byte], Array[Byte]]]): Task[Chunk[RecordMetadata]] =
    produceChunkAsync(records).flatten

  override def produceChunk[R, K, V](
    records: Chunk[ProducerRecord[K, V]],
    keySerializer: Serializer[R, K],
    valueSerializer: Serializer[R, V]
  ): RIO[R, Chunk[RecordMetadata]] =
    produceChunkAsync(records, keySerializer, valueSerializer).flatten

  // noinspection YieldingZIOEffectInspection
  override def produceChunkAsync(
    records: Chunk[ProducerRecord[Array[Byte], Array[Byte]]]
  ): Task[Task[Chunk[RecordMetadata]]] =
    produceChunkAsyncWithFailures(records).map(_.flatMap { chunkResults =>
      val (errors, success) = chunkResults.partitionMap(identity)
      errors.headOption match {
        case Some(error) => ZIO.fail(error) // Only the first failure is returned.
        case None        => ZIO.succeed(success)
      }
    })

  override def produceChunkAsync[R, K, V](
    records: Chunk[ProducerRecord[K, V]],
    keySerializer: Serializer[R, K],
    valueSerializer: Serializer[R, V]
  ): RIO[R, Task[Chunk[RecordMetadata]]] =
    ZIO
      .foreach(records)(serialize(_, keySerializer, valueSerializer))
      .flatMap(produceChunkAsync)

  // noinspection YieldingZIOEffectInspection
  override def produceChunkAsyncWithFailures(
    records: Chunk[ByteRecord]
  ): UIO[UIO[Chunk[Either[Throwable, RecordMetadata]]]] =
    if (records.isEmpty) ZIO.succeed(ZIO.succeed(Chunk.empty))
    else {
      for {
        done <- Promise.make[Nothing, Chunk[Either[Throwable, RecordMetadata]]]
        _    <- sendQueue.offer((records, done))
      } yield done.await
    }

  override def flush: Task[Unit] = ZIO.attemptBlocking(p.flush())

  override def metrics: Task[Map[MetricName, Metric]] = ZIO.attemptBlocking(p.metrics().asScala.toMap)

  /**
   * Calls to send may block when updating metadata or when communication with the broker is (temporarily) lost,
   * therefore this stream is run on a the blocking thread pool
   */
  val sendFromQueue: ZIO[Any, Nothing, Any] =
    ZStream
      .fromQueueWithShutdown(sendQueue)
      .mapZIO { case (serializedRecords, done) =>
        ZIO.succeed {
          try {
            val it: Iterator[(ByteRecord, Int)] = serializedRecords.iterator.zipWithIndex
            val res: Array[Either[Throwable, RecordMetadata]] =
              new Array[Either[Throwable, RecordMetadata]](serializedRecords.length)
            val count: AtomicLong = new AtomicLong
            val length            = serializedRecords.length

            while (it.hasNext) {
              val (rec, idx): (ByteRecord, Int) = it.next()

              val _ = p.send(
                rec,
                (metadata: RecordMetadata, err: Exception) =>
                  Unsafe.unsafe { implicit u =>
                    exec {
                      if (err != null) res(idx) = Left(err)
                      else res(idx) = Right(metadata)

                      if (count.incrementAndGet == length) {
                        exec {
                          runtime.unsafe.run(done.succeed(Chunk.fromArray(res))).getOrThrowFiberFailure()
                        }
                      }
                    }
                  }
              )
            }
          } catch {
            case NonFatal(e) =>
              Unsafe.unsafe { implicit u =>
                exec {
                  runtime.unsafe.run(done.succeed(Chunk.fill(serializedRecords.size)(Left(e)))).getOrThrowFiberFailure()
                }
              }
          }
        }
      }
      .runDrain

  private def serialize[R, K, V](
    r: ProducerRecord[K, V],
    keySerializer: Serializer[R, K],
    valueSerializer: Serializer[R, V]
  ): RIO[R, ByteRecord] =
    for {
      key   <- keySerializer.serialize(r.topic, r.headers, r.key())
      value <- valueSerializer.serialize(r.topic, r.headers, r.value())
    } yield new ProducerRecord(r.topic, r.partition(), r.timestamp(), key, value, r.headers)

  /** Used to prevent warnings about not using the result of an expression. */
  @inline private def exec[A](f: => A): Unit = { val _ = f }
}
