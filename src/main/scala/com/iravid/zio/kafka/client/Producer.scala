package com.iravid.zio.kafka.client

import java.util.concurrent.TimeUnit
import org.apache.kafka.clients.producer.RecordMetadata
import org.apache.kafka.clients.producer.Callback
import org.apache.kafka.common.serialization.ByteArraySerializer
import scalaz.zio.blocking._
import scalaz.zio.{ Managed, UIO, ZIO }

import scala.util.control.NonFatal

trait Producer {
  def produce(record: ByteProducerRecord): BlockingTask[RecordMetadata]
}

object Producer {
  def unsafeMake(p: ByteProducer) =
    new Producer {
      def produce(record: ByteProducerRecord): BlockingTask[RecordMetadata] =
        blocking {
          ZIO.effectAsync { (cb: ZIO[Any, Throwable, RecordMetadata] => Unit) =>
            try p.send(record, new Callback {
              def onCompletion(metadata: RecordMetadata, err: Exception): Unit =
                if (err != null) cb(ZIO.fail(err))
                else cb(ZIO.succeed(metadata))
            })
            catch {
              case NonFatal(e) => cb(ZIO.fail(e))
            }

            ()
          }
        }
    }

  def make(settings: ProducerSettings): Managed[Blocking, Throwable, Producer] = {
    val p = ZIO {
      val props = new java.util.Properties

      settings.driverSettings.foreach {
        case (k, v) => props.put(k, v)
      }

      new ByteProducer(props, new ByteArraySerializer, new ByteArraySerializer)
    }

    p.managed(p => UIO(p.close(settings.closeTimeout.toMillis, TimeUnit.MILLISECONDS)))
      .map(unsafeMake)
  }
}
