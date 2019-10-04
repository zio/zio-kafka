package zio.kafka.client

import java.util.concurrent.Future
import java.util.concurrent.atomic.AtomicLong

import org.apache.kafka.clients.producer.{ Callback, KafkaProducer, ProducerRecord, RecordMetadata }
import org.apache.kafka.common.serialization.ByteArraySerializer
import zio._
import zio.blocking._
import zio.kafka.client.serde.Serializer
import zio.stream.ZSink

import scala.collection.JavaConverters._

trait Producer[R, K, V] {

  /**
   * Produce a single record. The effect returned from this method has two layers and
   * describes the completion of two actions:
   * 1. The outer layer describes the enqueueing of the record to the Producer's internal
   *    buffer.
   * 2. The inner layer describes receiving an acknowledgement from the broker for the
   *    transmission of the record.
   *
   * It is usually recommended to not await the inner layer of every individual record,
   * but enqueue a batch of records and await all of their acknowledgements at once. That
   * amortizes the cost of sending requests to Kafka and increases throughput.
   */
  def produce(record: ProducerRecord[K, V]): RIO[R with Blocking, Task[RecordMetadata]]

  /**
   * Produces a chunk of records record. The effect returned from this method has two layers
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
  def produceChunk(records: Chunk[ProducerRecord[K, V]]): RIO[R with Blocking, Task[Array[RecordMetadata]]]

  /**
   * Flushes the producer's internal buffer. This will guarantee that all records
   * currently buffered will be transmitted to the broker.
   */
  def flush: BlockingTask[Unit]
}

object Producer {
  type ByteArrayProducer       = KafkaProducer[Array[Byte], Array[Byte]]
  type ByteArrayProducerRecord = ProducerRecord[Array[Byte], Array[Byte]]

  def unsafeMake[R, K, V](p: ByteArrayProducer, keySerializer: Serializer[R, K], valueSerializer: Serializer[R, V]) =
    new Producer[R, K, V] {
      def produce(record: ProducerRecord[K, V]): RIO[R with Blocking, Task[RecordMetadata]] =
        for {
          done             <- Promise.make[Throwable, RecordMetadata]
          serializedRecord <- serialize(record)
          runtime          <- ZIO.runtime[Blocking]
          _ <- effectBlocking {
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

      def flush: BlockingTask[Unit] =
        effectBlocking(p.flush())

      def produceChunk(records: Chunk[ProducerRecord[K, V]]) =
        if (records.isEmpty) {
          ZIO.succeed(Task.succeed(Array.empty[RecordMetadata]))
        } else {
          for {
            done              <- Promise.make[Throwable, Array[RecordMetadata]]
            runtime           <- ZIO.runtime[Blocking]
            serializedRecords <- ZIO.traverse(records.toSeq)(serialize(_))
            _ <- effectBlocking {
                  val it: Iterator[(ByteArrayProducerRecord, Int)] =
                    serializedRecords.toArray.iterator.zipWithIndex
                  val res: Array[RecordMetadata] = new Array[RecordMetadata](records.length)
                  val futures                    = Array.fill[Option[Future[RecordMetadata]]](records.length)(None)
                  val count: AtomicLong          = new AtomicLong

                  while (it.hasNext) {
                    val (rec, idx): (ByteArrayProducerRecord, Int) = it.next
                    val future = p.send(
                      rec,
                      new Callback {
                        def onCompletion(metadata: RecordMetadata, err: Exception): Unit = {
                          if (err != null) {
                            futures.map(_.map(_.cancel(true)))
                            runtime.unsafeRun(done.fail(err))
                          } else {
                            res(idx) = metadata
                            if (count.incrementAndGet == records.length) runtime.unsafeRun(done.succeed(res))
                          }
                          ()
                        }
                      }
                    )
                    futures.update(idx, Some(future))
                  }
                }
          } yield done.await
        }

      private def serialize(
        r: ProducerRecord[K, V]
      ): RIO[R, ByteArrayProducerRecord] =
        for {
          key   <- keySerializer.serialize(r.topic, r.headers, r.key())
          value <- valueSerializer.serialize(r.topic, r.headers, r.value())
        } yield new ProducerRecord(r.topic, r.partition(), r.timestamp(), key, value, r.headers)
    }

  def make[R, K, V](
    settings: ProducerSettings,
    keySerializer: Serializer[R, K],
    valueSerializer: Serializer[R, V]
  ): ZManaged[Blocking, Throwable, Producer[R, K, V]] = {
    val p = ZIO {
      val props = settings.driverSettings.asJava
      new KafkaProducer[Array[Byte], Array[Byte]](
        props,
        new ByteArraySerializer(),
        new ByteArraySerializer()
      )
    }

    p.toManaged(p => UIO(p.close(settings.closeTimeout.asJava)))
      .map(unsafeMake[R, K, V](_, keySerializer, valueSerializer))
  }

  /**
   * Sink that produces records to Kafka in chunks
   *
   * @param settings
   * @tparam K
   * @tparam V
   * @return
   */
  def sink[R, K, V](
    settings: ProducerSettings,
    keySerializer: Serializer[R, K],
    valueSerializer: Serializer[R, V]
  ): ZManaged[Blocking, Throwable, ZSink[R with Blocking, Throwable, Nothing, Chunk[ProducerRecord[K, V]], Unit]] =
    make[R, K, V](settings, keySerializer, valueSerializer).map { producer =>
      ZSink.drain.contramapM(producer.produceChunk)
    }
}
