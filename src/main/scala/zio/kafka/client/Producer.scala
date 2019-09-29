package zio.kafka.client

import java.util.concurrent.Future
import java.util.concurrent.atomic.AtomicLong

import org.apache.kafka.clients.producer.{ Callback, KafkaProducer, ProducerRecord, RecordMetadata }
import org.apache.kafka.common.serialization.ByteArraySerializer
import zio.blocking._
import zio.kafka.client.serde.Serializer
import zio._

import scala.collection.JavaConverters._

trait Producer[R, K, V] {
  def produce(record: ProducerRecord[K, V]): RIO[R with Blocking, RecordMetadata]

  def produceChunk(records: Chunk[ProducerRecord[K, V]]): RIO[R with Blocking, Array[RecordMetadata]]

  def flush: BlockingTask[Unit]
}

object Producer {
  type ByteArrayProducer       = KafkaProducer[Array[Byte], Array[Byte]]
  type ByteArrayProducerRecord = ProducerRecord[Array[Byte], Array[Byte]]

  def unsafeMake[R, K, V](
    p: ByteArrayProducer
  )(implicit keySerializer: Serializer[R, K], valueSerializer: Serializer[R, V]) =
    new Producer[R, K, V] {
      def produce(record: ProducerRecord[K, V]): RIO[R with Blocking, RecordMetadata] =
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
          recordMetadata <- done.await
        } yield recordMetadata

      def flush: BlockingTask[Unit] =
        effectBlocking(p.flush())

      def produceChunk(records: Chunk[ProducerRecord[K, V]]): RIO[R with Blocking, Array[RecordMetadata]] =
        if (records.isEmpty) {
          ZIO.succeed(Array.empty[RecordMetadata])
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
            data <- done.await
          } yield data
        }

      private def serialize(
        r: ProducerRecord[K, V]
      ): RIO[R, ByteArrayProducerRecord] =
        for {
          key   <- keySerializer.serialize(r.key())
          value <- valueSerializer.serialize(r.value())
        } yield new ProducerRecord(r.topic, r.partition(), r.timestamp(), key, value, r.headers)
    }

  def make[R, K, V](
    settings: ProducerSettings
  )(
    implicit keySerializer: Serializer[R, K],
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
      .map(unsafeMake[R, K, V])
  }

}
