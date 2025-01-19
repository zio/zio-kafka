package zio.kafka.producer

import org.apache.kafka.clients.producer.{ KafkaProducer, Producer => JProducer, ProducerRecord, RecordMetadata }
import org.apache.kafka.common.errors.{ AuthenticationException, AuthorizationException }
import org.apache.kafka.common.serialization.ByteArraySerializer
import org.apache.kafka.common.{ Metric, MetricName, PartitionInfo }
import zio._
import zio.kafka.serde.Serializer
import zio.kafka.utils.SslHelper
import zio.stream.{ ZPipeline, ZStream }

import java.util.concurrent.atomic.AtomicInteger
import scala.jdk.CollectionConverters._
import scala.util.control.{ NoStackTrace, NonFatal }

trait Producer {

  /**
   * Produces a single record and await broker acknowledgement.
   *
   * Use `produceAsync` to avoid the round-trip-time penalty for each record.
   */
  def produce(
    record: ProducerRecord[Array[Byte], Array[Byte]]
  ): Task[RecordMetadata]

  /**
   * Produces a single record and await broker acknowledgement.
   *
   * Use `produceAsync` to avoid the round-trip-time penalty for each record.
   */
  def produce[R, K, V](
    record: ProducerRecord[K, V],
    keySerializer: Serializer[R, K],
    valueSerializer: Serializer[R, V]
  ): RIO[R, RecordMetadata]

  /**
   * Produces a single record and await broker acknowledgement.
   *
   * Use `produceAsync` to avoid the round-trip-time penalty for each record.
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
   * throughput.
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
   * throughput.
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
   * throughput.
   */
  def produceAsync[R, K, V](
    topic: String,
    key: K,
    value: V,
    keySerializer: Serializer[R, K],
    valueSerializer: Serializer[R, V]
  ): RIO[R, Task[RecordMetadata]]

  /**
   * Produces a chunk of records.
   *
   * Use `produceChunkAsync` to avoid the round-trip-time penalty for each record.
   *
   * When publishing any of the records fails, the whole batch fails even though some records might have been published.
   * Use [[produceChunkAsyncWithFailures]] to get results per record.
   */
  def produceChunk(
    records: Chunk[ProducerRecord[Array[Byte], Array[Byte]]]
  ): Task[Chunk[RecordMetadata]]

  /**
   * Produces a chunk of records.
   *
   * Use `produceChunkAsync` to avoid the round-trip-time penalty for each record.
   *
   * When publishing any of the records fails, the whole batch fails even though some records might have been published.
   * Use [[produceChunkAsyncWithFailures]] to get results per record.
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
   *
   * When publishing any of the records fails, the whole batch fails even though some records might have been published.
   * Use [[produceChunkAsyncWithFailures]] to get results per record.
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
   *
   * When publishing any of the records fails, the whole batch fails even though some records might have been published.
   * Use [[produceChunkAsyncWithFailures]] to get results per record.
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
   * When attempt to send a record into buffer for publication fails, the following records in the chunk are not
   * published. This is indicated with a [[Producer.PublishOmittedException]].
   *
   * This variant does not accept serializers as they may also fail independently of each record and this is not
   * reflected in the return type.
   */
  def produceChunkAsyncWithFailures(
    records: Chunk[ProducerRecord[Array[Byte], Array[Byte]]]
  ): UIO[UIO[Chunk[Either[Throwable, RecordMetadata]]]]

  /**
   * Get the partition metadata for the given topic
   */
  def partitionsFor(topic: String): Task[Chunk[PartitionInfo]]

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
  case object PublishOmittedException
      extends RuntimeException("Publish omitted due to a publish error for a previous record in the chunk")
      with NoStackTrace

  val live: RLayer[ProducerSettings, Producer] =
    ZLayer.scoped {
      for {
        settings <- ZIO.service[ProducerSettings]
        producer <- make(settings)
      } yield producer
    }

  def make(settings: ProducerSettings): ZIO[Scope, Throwable, Producer] =
    for {
      _ <- SslHelper.validateEndpoint(settings.driverSettings)
      rawProducer <- ZIO.acquireRelease(
                       ZIO.attemptBlocking(
                         new KafkaProducer[Array[Byte], Array[Byte]](
                           settings.driverSettings.asJava,
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
    javaProducer: JProducer[Array[Byte], Array[Byte]],
    settings: ProducerSettings
  ): ZIO[Scope, Throwable, Producer] =
    for {
      runtime <- ZIO.runtime[Any]
      sendQueue <-
        Queue.bounded[(Chunk[ByteRecord], Chunk[Either[Throwable, RecordMetadata]] => UIO[Unit])](
          settings.sendBufferSize
        )
      producer = new ProducerLive(settings, javaProducer, runtime, sendQueue)
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
  def partitionsFor(topic: String): RIO[Producer, Chunk[PartitionInfo]] =
    ZIO.serviceWithZIO(_.partitionsFor(topic))

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
  settings: ProducerSettings,
  private[producer] val p: JProducer[Array[Byte], Array[Byte]],
  runtime: Runtime[Any],
  sendQueue: Queue[(Chunk[ByteRecord], Chunk[Either[Throwable, RecordMetadata]] => UIO[Unit])]
) extends Producer {

  private val leftPublishOmitted = Left(Producer.PublishOmittedException)
  private val retryAfterAuthException: Throwable =
    new RuntimeException("Authentication error, retry?") with NoStackTrace

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
    ZIO.suspendSucceed {
      def loop(
        done: Promise[Throwable, RecordMetadata],
        driver: Schedule.Driver[Any, Any, Throwable, Any]
      ): ZIO[Any, Nothing, Any] =
        sendQueue.offer(
          Chunk.single(record) -> { results =>
            // Since we're sending only 1 record, we know `results` has 1 item
            results.head match {
              case Right(recordMetaData) =>
                done.succeed(recordMetaData).unit
              case Left(error @ (_: AuthorizationException | _: AuthenticationException)) =>
                driver
                  .next(retryAfterAuthException)
                  .foldZIO(
                    _ => done.fail(error).unit,  // schedule say "no"
                    _ => loop(done, driver).unit // start retry
                  )
              case Left(error) =>
                done.fail(error).unit
            }
          }
        )

      for {
        done <- Promise.make[Throwable, RecordMetadata]
        _    <- settings.authErrorRetrySchedule.driver.flatMap(d => loop(done, d))
      } yield done.await
    }

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
      val totalRecordCount = records.size

      lazy val finalResults: Array[Either[Throwable, RecordMetadata]] =
        Array.fill(totalRecordCount)(leftPublishOmitted)

      def storeResults(recordIndices: Seq[Int], results: Chunk[Either[Throwable, RecordMetadata]]): Unit =
        (recordIndices lazyZip results).foreach { case (index, result) => finalResults(index) = result }

      def complete(done: Promise[Nothing, Chunk[Either[Throwable, RecordMetadata]]]): UIO[Unit] =
        done.succeed(Chunk.fromArray(finalResults)).unit

      ZIO.suspendSucceed {
        def loop(
          recordIndices: Seq[Int],
          records: Chunk[ByteRecord],
          done: Promise[Nothing, Chunk[Either[Throwable, RecordMetadata]]],
          driver: Schedule.Driver[Any, Any, Throwable, Any]
        ): ZIO[Any, Nothing, Any] = {
          def retryFailedRecords(results: Chunk[Either[Throwable, RecordMetadata]]) = {
            // Note that if we get here, all Left's can be retried. Also, we know there is at least 1 Left.
            val toRetry: Seq[(RuntimeFlags, ByteRecord)] =
              (recordIndices lazyZip records lazyZip results).flatMap {
                case (_, _, Right(_))     => Seq.empty
                case (i, record, Left(_)) => Seq((i, record))
              }
            val (retryIndices, retryRecords) = toRetry.unzip
            ZIO.logInfo(
              s"Retrying publish ${retryRecords.size} (of ${records.size}) records after AuthorizationException/AuthenticationException"
            ) *>
              loop(retryIndices, Chunk.from(retryRecords), done, driver).unit
          }

          val continuation: Chunk[Either[Throwable, RecordMetadata]] => UIO[Unit] = { results =>
            if (results.forall(_.isRight)) {
              // All records were successfully published, we're done
              if (recordIndices.size == totalRecordCount) {
                // Optimization (not allocating `finalResults`) for when everything goes well first time
                done.succeed(results).unit
              } else {
                // Copy results of this attempt to the final results
                storeResults(recordIndices, results)
                complete(done)
              }
            } else {
              // Copy results of this attempt to the final results
              storeResults(recordIndices, results)
              // There are some failures, let's see if we can retry some records.
              // We only retry after an auth error. Any other error and we give up.
              val hasFatalError = results.exists {
                case Right(_) | Left(
                      _: AuthorizationException | _: AuthenticationException | Producer.PublishOmittedException
                    ) =>
                  false
                case _ => true
              }
              if (hasFatalError) {
                complete(done)
              } else {
                // Ask the schedule if we should retry
                driver
                  .next(retryAfterAuthException)
                  .foldZIO(
                    _ => // Schedule says no
                      complete(done),
                    _ => // Schedule allows retry, select the records to retry.
                      retryFailedRecords(results)
                  )
              }
            }
          }

          sendQueue.offer(records -> continuation)
        }

        for {
          done <- Promise.make[Nothing, Chunk[Either[Throwable, RecordMetadata]]]
          _    <- settings.authErrorRetrySchedule.driver.flatMap(d => loop(records.indices, records, done, d))
        } yield done.await
      }
    }

  override def partitionsFor(topic: String): Task[Chunk[PartitionInfo]] =
    ZIO.attemptBlocking(Chunk.fromJavaIterable(p.partitionsFor(topic)))

  override def flush: Task[Unit] = ZIO.attemptBlocking(p.flush())

  override def metrics: Task[Map[MetricName, Metric]] = ZIO.attemptBlocking(p.metrics().asScala.toMap)

  /**
   * Calls to send may block when updating metadata or when communication with the broker is (temporarily) lost,
   * therefore this stream is run on the blocking thread pool
   */
  val sendFromQueue: ZIO[Any, Nothing, Any] =
    ZStream
      .fromQueueWithShutdown(sendQueue)
      .mapZIO { case (serializedRecords, continuation) =>
        ZIO.succeed {
          val recordsLength                                = serializedRecords.length
          val sentRecordsCounter                           = new AtomicInteger(0)
          val recordsIterator: Iterator[(ByteRecord, Int)] = serializedRecords.iterator.zipWithIndex
          val sentResults: Array[Either[Throwable, RecordMetadata]] =
            new Array[Either[Throwable, RecordMetadata]](recordsLength)

          @inline def insertSentResult(resultIndex: Int, sentResult: Either[Throwable, RecordMetadata]): Unit = {
            // Updating sentResults[resultIndex] here is safe,
            //  because only a single update for every resultIndex of sentResults is performed
            sentResults.update(resultIndex, sentResult)

            // Reading from sentRecordsCounter guarantees fully updated version of sentResults
            //  will be visible and used to complete done promise
            if (sentRecordsCounter.incrementAndGet() == recordsLength) {
              val sentResultsChunk = Chunk.fromArray(sentResults)

              Unsafe.unsafe { implicit u =>
                val _ = runtime.unsafe.run(continuation(sentResultsChunk))
              }
            }
          }

          @volatile var previousSendCallsSucceed = true

          recordsIterator.foreach { case (record: ByteRecord, recordIndex: Int) =>
            if (previousSendCallsSucceed) {
              try {
                val _ = p.send(
                  record,
                  (metadata: RecordMetadata, err: Exception) => {
                    val sendResult = if (err eq null) {
                      Right(metadata)
                    } else {
                      previousSendCallsSucceed = false
                      Left(err)
                    }
                    insertSentResult(recordIndex, sendResult)
                  }
                )
              } catch {
                case NonFatal(err) =>
                  previousSendCallsSucceed = false

                  insertSentResult(
                    recordIndex,
                    Left(err)
                  )
              }
            } else {
              insertSentResult(
                recordIndex,
                Left(Producer.PublishOmittedException)
              )
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
}
