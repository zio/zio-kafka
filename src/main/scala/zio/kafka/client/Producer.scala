package zio.kafka.client

import scala.collection.JavaConverters._

import org.apache.kafka.clients.producer.{ Callback, KafkaProducer, ProducerRecord, RecordMetadata }
import org.apache.kafka.common.serialization.Serde

import zio.{ Promise, UIO, ZIO, ZManaged }
import zio.blocking._

trait Producer[K, V] {
  def produce(record: ProducerRecord[K, V]): BlockingTask[RecordMetadata]

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
}
