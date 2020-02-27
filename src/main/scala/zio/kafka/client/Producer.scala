package zio.kafka.client

import java.util.concurrent.atomic.AtomicLong

import org.apache.kafka.clients.producer.{ Callback, KafkaProducer, ProducerRecord, RecordMetadata }
import org.apache.kafka.common.serialization.ByteArraySerializer
import zio._
import zio.blocking._
import zio.kafka.client.serde.Serializer
import zio.stream.ZSink

import scala.jdk.CollectionConverters._

object Producer {

  trait Service[R, K, V] {

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
    def produce(record: ProducerRecord[K, V]): RIO[Blocking with R, Task[RecordMetadata]]

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
    def produceChunk(records: Chunk[ProducerRecord[K, V]]): RIO[R with Blocking, Task[Chunk[RecordMetadata]]]

    final def stream: ZSink[R with Blocking, Throwable, Nothing, Chunk[ProducerRecord[K, V]], Unit] =
      ZSink.drain.contramapM(produceChunk)
  }

  class Live[R, K, V](
    p: KafkaProducer[Array[Byte], Array[Byte]],
    keySerializer: Serializer[R, K],
    valueSerializer: Serializer[R, V]
  ) extends Service[R, K, V] {

    override def produce(record: ProducerRecord[K, V]): RIO[Blocking with R, Task[RecordMetadata]] =
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

    override def produceChunk(records: Chunk[ProducerRecord[K, V]]): RIO[R with Blocking, Task[Chunk[RecordMetadata]]] =
      if (records.isEmpty) ZIO.succeed(Task.succeed(Chunk.empty))
      else {
        for {
          done              <- Promise.make[Throwable, Chunk[RecordMetadata]]
          runtime           <- ZIO.runtime[Blocking]
          serializedRecords <- ZIO.foreach(records.toSeq)(serialize)
          _ <- effectBlocking {
                val it: Iterator[(ByteArrayProducerRecord, Int)] =
                  serializedRecords.iterator.zipWithIndex
                val res: Array[RecordMetadata] = new Array[RecordMetadata](records.length)
                val count: AtomicLong          = new AtomicLong

                while (it.hasNext) {
                  val (rec, idx): (ByteArrayProducerRecord, Int) = it.next

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

    /**
     * Flushes the producer's internal buffer. This will guarantee that all records
     * currently buffered will be transmitted to the broker.
     */
    def flush: BlockingTask[Unit] = effectBlocking(p.flush())

    private def serialize(r: ProducerRecord[K, V]): RIO[R, ByteArrayProducerRecord] =
      for {
        key   <- keySerializer.serialize(r.topic, r.headers, r.key())
        value <- valueSerializer.serialize(r.topic, r.headers, r.value())
      } yield new ProducerRecord(r.topic, r.partition(), r.timestamp(), key, value, r.headers)
  }

  private def createLive[R, K, V](
    p: KafkaProducer[Array[Byte], Array[Byte]],
    keySerializer: Serializer[R, K],
    valueSerializer: Serializer[R, V]
  ): Live[R, K, V] = new Live(p, keySerializer, valueSerializer)

  def producer[R, K, V](
    settings: ProducerSettings,
    keySerializer: Serializer[R, K],
    valueSerializer: Serializer[R, V]
  )(implicit tagged: Tagged[Service[R, K, V]]): ZLayer.NoDeps[Throwable, Producer[R, K, V]] = {
    val p: Task[KafkaProducer[Array[Byte], Array[Byte]]] = ZIO {
      val props = settings.driverSettings.asJava
      new KafkaProducer[Array[Byte], Array[Byte]](
        props,
        new ByteArraySerializer(),
        new ByteArraySerializer()
      )
    }

    ZLayer.fromManaged(
      p.toManaged(p => UIO(p.close(settings.closeTimeout.asJava)))
        .map(producer => createLive(producer, keySerializer, valueSerializer))
    )
  }
}
