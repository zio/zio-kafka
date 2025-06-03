package zio.kafka.producer

import org.apache.kafka.clients.producer.{ KafkaProducer, Producer => JProducer, ProducerRecord, RecordMetadata }
import org.apache.kafka.common.errors.{ AuthenticationException, AuthorizationException }
import org.apache.kafka.common.serialization.ByteArraySerializer
import org.apache.kafka.common.{ Metric, MetricName, PartitionInfo }
import zio._
import zio.kafka.diagnostics.Diagnostics
import zio.kafka.diagnostics.internal.ConcurrentDiagnostics
import zio.kafka.producer.Producer.{ NoDiagnostics, ProducerDiagnostics }
import zio.kafka.producer.ProducerLive.NanoTime
import zio.kafka.producer.internal.{ ProducerMetrics, ZioProducerMetrics }
import zio.kafka.serde.Serializer
import zio.kafka.utils.SslHelper
import zio.stream.{ ZPipeline, ZStream }

import java.util.concurrent.atomic.{ AtomicInteger, AtomicLong }
import scala.collection.immutable.BitSet
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
  )(implicit trace: Trace): Task[RecordMetadata]

  /**
   * Produces a single record and await broker acknowledgement.
   *
   * Use `produceAsync` to avoid the round-trip-time penalty for each record.
   */
  def produce[R, K, V](
    record: ProducerRecord[K, V],
    keySerializer: Serializer[R, K],
    valueSerializer: Serializer[R, V]
  )(implicit trace: Trace): RIO[R, RecordMetadata]

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
  )(implicit trace: Trace): RIO[R, RecordMetadata]

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
  )(implicit trace: Trace): Task[Task[RecordMetadata]]

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
  )(implicit trace: Trace): RIO[R, Task[RecordMetadata]]

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
  )(implicit trace: Trace): RIO[R, Task[RecordMetadata]]

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
  )(implicit trace: Trace): Task[Chunk[RecordMetadata]]

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
  )(implicit trace: Trace): RIO[R, Chunk[RecordMetadata]]

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
  )(implicit trace: Trace): Task[Task[Chunk[RecordMetadata]]]

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
  )(implicit trace: Trace): RIO[R, Task[Chunk[RecordMetadata]]]

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
  )(implicit trace: Trace): UIO[UIO[Chunk[Either[Throwable, RecordMetadata]]]]

  /**
   * Get the partition metadata for the given topic
   */
  def partitionsFor(topic: String)(implicit trace: Trace): Task[Chunk[PartitionInfo]]

  /**
   * Flushes the producer's internal buffer. This will guarantee that all records currently buffered will be transmitted
   * to the broker.
   */
  def flush(implicit trace: Trace): Task[Unit]

  /**
   * Expose internal producer metrics
   */
  def metrics(implicit trace: Trace): Task[Map[MetricName, Metric]]
}

object Producer {

  /** A callback for producer diagnostic events. */
  type ProducerDiagnostics = zio.kafka.diagnostics.Diagnostics[ProducerEvent]

  /** A diagnostics implementation that does nothing. */
  val NoDiagnostics: ProducerDiagnostics = zio.kafka.diagnostics.Diagnostics.NoOp

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

  def make(settings: ProducerSettings)(implicit trace: Trace): ZIO[Scope, Throwable, Producer] =
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
  )(implicit trace: Trace): ZIO[Scope, Throwable, Producer] =
    for {
      wrappedDiagnostics <- makeConcurrentDiagnostics(settings.diagnostics)
      runtime            <- ZIO.runtime[Any]
      sendQueue <-
        Queue.bounded[(Chunk[ByteRecord], NanoTime, Chunk[Either[Throwable, RecordMetadata]] => UIO[Unit])](
          settings.sendBufferSize
        )
      metrics  = new ZioProducerMetrics(settings.metricLabels)
      producer = new ProducerLive(settings, wrappedDiagnostics, javaProducer, runtime, sendQueue, metrics)
      _ <- sendQueue.size.flatMap(metrics.observeSendQueueSize).schedule(Schedule.fixed(10.seconds)).forkScoped
      _ <- ZIO.blocking(producer.sendFromQueue).forkScoped
    } yield producer

  private[producer] def makeConcurrentDiagnostics(
    diagnostics: ProducerDiagnostics
  )(implicit trace: Trace): ZIO[Scope, Nothing, ProducerDiagnostics] =
    if (diagnostics == Diagnostics.NoOp) ZIO.succeed(diagnostics)
    else ConcurrentDiagnostics.make(diagnostics, ProducerEvent.ProducerFinalized)

}

private object ProducerLive {
  private[producer] type NanoTime = Long

  private val set0: Set[Int] = Set(0)

  private val leftPublishOmitted = Left(Producer.PublishOmittedException)

  private val retryAfterAuthException: Throwable =
    new RuntimeException("Authentication error, retry?") with NoStackTrace
}

private[producer] final class ProducerLive(
  settings: ProducerSettings,
  diagnostics: ProducerDiagnostics,
  private[producer] val p: JProducer[Array[Byte], Array[Byte]],
  runtime: Runtime[Any],
  sendQueue: Queue[(Chunk[ByteRecord], NanoTime, Chunk[Either[Throwable, RecordMetadata]] => UIO[Unit])],
  producerMetrics: ProducerMetrics
) extends Producer {
  import ProducerLive._

  private val diagnosticsEmitterMaker =
    if (diagnostics eq NoDiagnostics) EmptyDiagnosticsEmitterMaker
    else new BatchDiagnosticsEmitterMaker

  override def produce(record: ProducerRecord[Array[Byte], Array[Byte]])(implicit trace: Trace): Task[RecordMetadata] =
    produceAsync(record).flatten

  override def produce[R, K, V](
    record: ProducerRecord[K, V],
    keySerializer: Serializer[R, K],
    valueSerializer: Serializer[R, V]
  )(implicit trace: Trace): RIO[R, RecordMetadata] =
    produceAsync(record, keySerializer, valueSerializer).flatten

  override def produce[R, K, V](
    topic: String,
    key: K,
    value: V,
    keySerializer: Serializer[R, K],
    valueSerializer: Serializer[R, V]
  )(implicit trace: Trace): RIO[R, RecordMetadata] =
    produce(new ProducerRecord(topic, key, value), keySerializer, valueSerializer)

  // noinspection YieldingZIOEffectInspection
  override def produceAsync(
    record: ProducerRecord[Array[Byte], Array[Byte]]
  )(implicit trace: Trace): Task[Task[RecordMetadata]] =
    ZIO.suspendSucceed {
      val diagEm = diagnosticsEmitterMaker.makeDiagnosticsEmitter(record)
      def loop(
        startNanos: NanoTime,
        done: Promise[Throwable, RecordMetadata],
        driver: Schedule.Driver[Any, Any, Throwable, Any]
      ): UIO[Any] = {
        val continuation: Chunk[Either[Throwable, RecordMetadata]] => UIO[Unit] = { results =>
          // Since we're sending only 1 record, we know `results` has 1 item
          results.head match {
            case Right(recordMetaData) =>
              for {
                end <- Clock.nanoTime
                _   <- producerMetrics.observeProduce((end - startNanos).nanos, 1)
                _   <- diagEm.emitSuccess()
                _   <- done.succeed(recordMetaData)
              } yield ()
            case Left(error @ (_: AuthorizationException | _: AuthenticationException)) =>
              driver
                .next(retryAfterAuthException)
                .foldZIO(
                  _ => diagEm.emitSingleFailure() *> done.fail(error).unit, // schedule say "no"
                  _ => loop(startNanos, done, driver).unit                  // start retry
                )
            case Left(error) =>
              diagEm.emitSingleFailure() *> done.fail(error).unit
          }
        }

        sendQueue.offer((Chunk.single(record), startNanos, continuation))
      }

      for {
        start <- Clock.nanoTime
        _     <- diagEm.emitOffer()
        done  <- Promise.make[Throwable, RecordMetadata]
        _     <- settings.authErrorRetrySchedule.driver.flatMap(d => loop(start, done, d))
      } yield done.await
    }

  override def produceAsync[R, K, V](
    record: ProducerRecord[K, V],
    keySerializer: Serializer[R, K],
    valueSerializer: Serializer[R, V]
  )(implicit trace: Trace): RIO[R, Task[RecordMetadata]] =
    serialize(record, keySerializer, valueSerializer).flatMap(produceAsync)

  override def produceAsync[R, K, V](
    topic: String,
    key: K,
    value: V,
    keySerializer: Serializer[R, K],
    valueSerializer: Serializer[R, V]
  )(implicit trace: Trace): RIO[R, Task[RecordMetadata]] =
    produceAsync(new ProducerRecord(topic, key, value), keySerializer, valueSerializer)

  override def produceChunk(records: Chunk[ProducerRecord[Array[Byte], Array[Byte]]])(implicit
    trace: Trace
  ): Task[Chunk[RecordMetadata]] =
    produceChunkAsync(records).flatten

  override def produceChunk[R, K, V](
    records: Chunk[ProducerRecord[K, V]],
    keySerializer: Serializer[R, K],
    valueSerializer: Serializer[R, V]
  )(implicit trace: Trace): RIO[R, Chunk[RecordMetadata]] =
    produceChunkAsync(records, keySerializer, valueSerializer).flatten

  // noinspection YieldingZIOEffectInspection
  override def produceChunkAsync(
    records: Chunk[ProducerRecord[Array[Byte], Array[Byte]]]
  )(implicit trace: Trace): Task[Task[Chunk[RecordMetadata]]] =
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
  )(implicit trace: Trace): RIO[R, Task[Chunk[RecordMetadata]]] =
    ZIO
      .foreach(records)(serialize(_, keySerializer, valueSerializer))
      .flatMap(produceChunkAsync)

  // noinspection YieldingZIOEffectInspection
  override def produceChunkAsyncWithFailures(
    records: Chunk[ByteRecord]
  )(implicit trace: Trace): UIO[UIO[Chunk[Either[Throwable, RecordMetadata]]]] =
    if (records.isEmpty) ZIO.succeed(ZIO.succeed(Chunk.empty))
    else {
      val totalRecordCount = records.size
      val diagEm           = diagnosticsEmitterMaker.makeDiagnosticsEmitter(records)

      // Collects the send results over one or more retries.
      // Lazy to prevent allocation when all sends succeed on the first attempt.
      lazy val finalResults: Array[Either[Throwable, RecordMetadata]] =
        Array.fill(totalRecordCount)(leftPublishOmitted)

      def storeResults(recordIndices: Seq[Int], results: Chunk[Either[Throwable, RecordMetadata]]): Unit =
        (recordIndices lazyZip results).foreach { case (index, result) => finalResults(index) = result }

      def complete(done: Promise[Nothing, Chunk[Either[Throwable, RecordMetadata]]]): UIO[Unit] =
        done.succeed(Chunk.fromArray(finalResults)).unit

      ZIO.suspendSucceed {
        def loop(
          startNanos: NanoTime,
          recordIndices: Seq[Int],
          records: Chunk[ByteRecord],
          done: Promise[Nothing, Chunk[Either[Throwable, RecordMetadata]]],
          driver: Schedule.Driver[Any, Any, Throwable, Any]
        ): ZIO[Any, Nothing, Unit] = {
          def retryFailedRecords(results: Chunk[Either[Throwable, RecordMetadata]]): UIO[Unit] = {
            // Note that if we get here, all Left's can be retried. Also, we know there is at least 1 Left.
            val toRetry: Chunk[(Int, ByteRecord)] =
              (results lazyZip recordIndices lazyZip records).flatMap {
                case (Right(_), _, _)     => Chunk.empty
                case (Left(_), i, record) => Chunk.single((i, record))
              }
            val (retryIndices, retryRecords) = toRetry.unzip
            ZIO.logInfo(
              s"Retrying publish ${retryRecords.size} (of ${records.size}) records after AuthorizationException/AuthenticationException"
            ) *>
              loop(startNanos, retryIndices, retryRecords, done, driver)
          }

          def observeProduce(startNanos: NanoTime): UIO[Unit] =
            for {
              now <- Clock.nanoTime
              _   <- producerMetrics.observeProduce((now - startNanos).nanos, totalRecordCount)
            } yield ()

          val continuation: Chunk[Either[Throwable, RecordMetadata]] => UIO[Unit] = { results =>
            if (results.forall(_.isRight)) {
              // All records were successfully published, we're done
              observeProduce(startNanos) *>
                diagEm.emitSuccess() *> {
                  if (recordIndices.size == totalRecordCount) {
                    // Optimization (not allocating `finalResults`) for when everything goes well first time
                    done.succeed(results).unit
                  } else {
                    // Copy results of this attempt to the final results
                    storeResults(recordIndices, results)
                    complete(done)
                  }
                }
            } else {
              // Copy results of this attempt to the final results
              storeResults(recordIndices, results)
              // There are some failures, let's see if we can retry some records.
              // We only retry after an auth error. Any other error and we give up.
              val hasFatalError = results.exists {
                case Right(_) |
                    Left(_: AuthorizationException | _: AuthenticationException | Producer.PublishOmittedException) =>
                  false
                case _ => true
              }
              if (hasFatalError) {
                diagEm.emitFailures(finalResults) *> complete(done)
              } else {
                producerMetrics.observeSendAuthError(
                  results.count {
                    case Left(_: AuthorizationException | _: AuthenticationException) => true
                    case _                                                            => false
                  }
                ) *>
                  // Ask the schedule if we should retry
                  driver
                    .next(retryAfterAuthException)
                    .foldZIO(
                      _ => diagEm.emitFailures(finalResults) *> complete(done), // Schedule says no
                      _ => retryFailedRecords(results)
                    )
              }
            }
          }

          sendQueue.offer((records, startNanos, continuation)).unit
        }

        for {
          startNanos <- Clock.nanoTime
          _          <- diagEm.emitOffer()
          done       <- Promise.make[Nothing, Chunk[Either[Throwable, RecordMetadata]]]
          d          <- settings.authErrorRetrySchedule.driver
          _          <- loop(startNanos, records.indices, records, done, d)
        } yield done.await
      }
    }

  override def partitionsFor(topic: String)(implicit trace: Trace): Task[Chunk[PartitionInfo]] =
    ZIO.attemptBlocking(Chunk.fromJavaIterable(p.partitionsFor(topic)))

  override def flush(implicit trace: Trace): Task[Unit] = ZIO.attemptBlocking(p.flush())

  override def metrics(implicit trace: Trace): Task[Map[MetricName, Metric]] =
    ZIO.attemptBlocking(p.metrics().asScala.toMap)

  private def observeTake(startNanos: NanoTime)(implicit trace: Trace): UIO[Unit] =
    for {
      now <- Clock.nanoTime
      _   <- producerMetrics.observeSendQueueTake((startNanos - now).nanos)
    } yield ()

  /**
   * Calls to send may block when updating metadata or when communication with the broker is (temporarily) lost,
   * therefore this stream is run on the blocking thread pool
   */
  val sendFromQueue: ZIO[Any, Nothing, Any] =
    ZStream
      .fromQueueWithShutdown(sendQueue)
      .mapZIO { case (serializedRecords, startNanos, continuation) =>
        observeTake(startNanos).as {
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

          // Must be volatile so that reads in this thread always see the latest value, even when the callback sets it
          // from another thread.
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
  )(implicit trace: Trace): RIO[R, ByteRecord] =
    for {
      key   <- keySerializer.serialize(r.topic, r.headers, r.key())
      value <- valueSerializer.serialize(r.topic, r.headers, r.value())
    } yield new ProducerRecord(r.topic, r.partition(), r.timestamp(), key, value, r.headers)

  private class BatchDiagnosticsEmitterMaker extends DiagnosticsEmitterMaker {
    private val batchIds = new AtomicLong(0L)

    override def makeDiagnosticsEmitter(record: ByteRecord): DiagnosticsEmitter =
      makeDiagnosticsEmitter(Chunk.single(record))

    override def makeDiagnosticsEmitter(records: Chunk[ByteRecord]): DiagnosticsEmitter = {
      val batchId = batchIds.getAndIncrement()
      val producedRecords =
        records.map(record => ProducerEvent.ProducedRecord(record.topic(), record.partition(), record.value().length))
      new BatchDiagnosticsEmitter(batchId, producedRecords)
    }
  }

  private class BatchDiagnosticsEmitter(batchId: Long, producedRecords: Chunk[ProducerEvent.ProducedRecord])
      extends DiagnosticsEmitter {
    override def emitOffer()(implicit trace: Trace): ZIO[Any, Nothing, Unit] =
      diagnostics.emit(ProducerEvent.RecordsOffered(batchId, producedRecords))

    override def emitSuccess()(implicit trace: Trace): ZIO[Any, Nothing, Unit] =
      diagnostics.emit(ProducerEvent.RecordsSent(batchId, producedRecords, Set.empty))

    override def emitSingleFailure()(implicit trace: Trace): ZIO[Any, Nothing, Unit] =
      diagnostics.emit(ProducerEvent.RecordsSent(batchId, producedRecords, set0))

    override def emitFailures(
      finalResults: Array[_ <: Either[_, _]]
    )(implicit trace: Trace): ZIO[Any, Nothing, Unit] = {
      val failed     = BitSet.empty ++ finalResults.zipWithIndex.filter(_._1.isLeft).map(_._2)
      val recordSent = ProducerEvent.RecordsSent(batchId, producedRecords, failed)
      diagnostics.emit(recordSent)
    }
  }

}

private abstract class DiagnosticsEmitterMaker {
  def makeDiagnosticsEmitter(record: ByteRecord): DiagnosticsEmitter
  def makeDiagnosticsEmitter(records: Chunk[ByteRecord]): DiagnosticsEmitter
}

private object EmptyDiagnosticsEmitterMaker extends DiagnosticsEmitterMaker {
  override def makeDiagnosticsEmitter(record: ByteRecord): DiagnosticsEmitter         = EmptyDiagnosticsEmitter
  override def makeDiagnosticsEmitter(records: Chunk[ByteRecord]): DiagnosticsEmitter = EmptyDiagnosticsEmitter
}

private abstract class DiagnosticsEmitter {
  def emitOffer()(implicit trace: Trace): ZIO[Any, Nothing, Unit]
  def emitSuccess()(implicit trace: Trace): ZIO[Any, Nothing, Unit]
  def emitSingleFailure()(implicit trace: Trace): ZIO[Any, Nothing, Unit]
  def emitFailures(finalResults: Array[_ <: Either[_, _]])(implicit trace: Trace): ZIO[Any, Nothing, Unit]
}

private object EmptyDiagnosticsEmitter extends DiagnosticsEmitter {
  override def emitOffer()(implicit trace: Trace): ZIO[Any, Nothing, Unit]         = ZIO.unit
  override def emitSuccess()(implicit trace: Trace): ZIO[Any, Nothing, Unit]       = ZIO.unit
  override def emitSingleFailure()(implicit trace: Trace): ZIO[Any, Nothing, Unit] = ZIO.unit
  override def emitFailures(finalResults: Array[_ <: Either[_, _]])(implicit trace: Trace): ZIO[Any, Nothing, Unit] =
    ZIO.unit
}
