package zio.kafka.client

import java.util.concurrent.Future
import java.util.concurrent.atomic.AtomicLong

import org.apache.kafka.clients.producer.{ Callback, KafkaProducer, ProducerRecord, RecordMetadata }
import org.apache.kafka.common.serialization.Serde
import zio._
import zio.blocking._
import zio.stream.ZSink

import scala.collection.JavaConverters._

trait Producer[K, V] {
  def produce(record: ProducerRecord[K, V]): BlockingTask[RecordMetadata]

  def produceChunk(records: Chunk[ProducerRecord[K, V]]): BlockingTask[Array[RecordMetadata]]

  def flush: BlockingTask[Unit]
}

object Producer {
  def unsafeMake[K, V](p: KafkaProducer[K, V]) =
    new Producer[K, V] {
      def produce(record: ProducerRecord[K, V]): BlockingTask[RecordMetadata] =
        for {
          done    <- Promise.make[Throwable, RecordMetadata]
          runtime <- ZIO.runtime[Blocking]
          _ <- effectBlocking {
                p.send(
                  record,
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

      def produceChunk(records: Chunk[ProducerRecord[K, V]]): BlockingTask[Array[RecordMetadata]] =
        if (records.isEmpty) {
          ZIO.succeed(Array.empty[RecordMetadata])
        } else {
          for {
            done    <- Promise.make[Throwable, Array[RecordMetadata]]
            runtime <- ZIO.runtime[Blocking]
            _ <- effectBlocking {
                  val it: Iterator[(ProducerRecord[K, V], Int)] = records.toArray.iterator.zipWithIndex
                  val res: Array[RecordMetadata]                = new Array[RecordMetadata](records.length)
                  val futures                                   = Array.fill[Option[Future[RecordMetadata]]](records.length)(None)
                  val count: AtomicLong                         = new AtomicLong

                  while (it.hasNext) {
                    val (rec, idx): (ProducerRecord[K, V], Int) = it.next
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
    }

  def make[K, V](
    settings: ProducerSettings
  )(implicit keySerde: Serde[K], valueSerde: Serde[V]): ZManaged[Blocking, Throwable, Producer[K, V]] = {
    val p = ZIO {
      val props = settings.driverSettings.asJava

      new KafkaProducer(props, keySerde.serializer, valueSerde.serializer)
    }

    p.toManaged(p => UIO(p.close(settings.closeTimeout.asJava)))
      .map(unsafeMake)
  }

  /**
   * Sink that produces records to Kafka in chunks
   *
   * @param settings
   * @tparam K
   * @tparam V
   * @return
   */
  def sink[K: Serde, V: Serde](
    settings: ProducerSettings
  ): ZManaged[Blocking, Throwable, ZSink[Blocking, Throwable, Nothing, Chunk[ProducerRecord[K, V]], Unit]] =
    make[K, V](settings).map { producer =>
      ZSink.drain.contramapM(producer.produceChunk)
    }
}
