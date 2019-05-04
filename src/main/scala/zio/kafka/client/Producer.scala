package zio.kafka.client

import org.apache.kafka.clients.producer.RecordMetadata
import org.apache.kafka.clients.producer.Callback
import org.apache.kafka.common.serialization.ByteArraySerializer
import scalaz.zio.blocking._
import scalaz.zio.{ UIO, ZIO, ZManaged }
import scalaz.zio.Promise

trait Producer {
  def produce(record: ByteProducerRecord): BlockingTask[RecordMetadata]

  def flush: BlockingTask[Unit]
}

object Producer {
  def unsafeMake(p: ByteProducer) =
    new Producer {
      def produce(record: ByteProducerRecord): BlockingTask[RecordMetadata] =
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

  def make(settings: ProducerSettings): ZManaged[Blocking, Throwable, Producer] = {
    val p = ZIO {
      val props = new java.util.Properties

      settings.driverSettings.foreach {
        case (k, v) => props.put(k, v)
      }

      new ByteProducer(props, new ByteArraySerializer, new ByteArraySerializer)
    }

    p.toManaged(p => UIO(p.close(settings.closeTimeout.asJava)))
      .map(unsafeMake)
  }
}
