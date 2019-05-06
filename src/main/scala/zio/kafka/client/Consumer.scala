package zio.kafka.client

import scala.collection.JavaConverters._

import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.errors.WakeupException
import org.apache.kafka.common.serialization.Serde
import org.apache.kafka.clients.consumer.{ ConsumerRebalanceListener, ConsumerRecord, ConsumerRecords, KafkaConsumer }

import scalaz.zio.{ Chunk, UIO, ZIO, ZManaged }
import scalaz.zio.blocking._
import scalaz.zio.duration._

trait Consumer[K, V] {
  def commit(data: OffsetMap): BlockingTask[Unit]

  def poll(pollTimeout: Duration): BlockingTask[Map[TopicPartition, Chunk[ConsumerRecord[K, V]]]]

  def subscribe(subscription: Subscription): BlockingTask[Unit] = subscribeWith(subscription)(_ => UIO.unit)

  def subscribeWith(subscription: Subscription)(listener: Rebalance => UIO[Unit]): BlockingTask[Unit]

  def unsubscribe: BlockingTask[Unit]

  def pause(partitions: Set[TopicPartition]): BlockingTask[Unit]

  def resume(partitions: Set[TopicPartition]): BlockingTask[Unit]

  def seek(partition: TopicPartition, offset: Long): BlockingTask[Unit]

  def seekToBeginning(partitions: Set[TopicPartition]): BlockingTask[Unit]

  def seekToEnd(partitions: Set[TopicPartition]): BlockingTask[Unit]
}

object Consumer {
  def withConsumer[A, K, V](c: KafkaConsumer[K, V])(f: KafkaConsumer[K, V] => A): BlockingTask[A] =
    ZIO.effectAsyncInterrupt[Blocking, Throwable, A] { cb =>
      cb(blocking(ZIO.effect(f(c))).catchSome {
        case _: WakeupException => ZIO.interrupt
      })

      Left(UIO(c.wakeup()))
    }

  def unsafeMake[K, V](c: KafkaConsumer[K, V]): Consumer[K, V] =
    new Consumer[K, V] {
      def commit(data: OffsetMap) =
        withConsumer(c)(_.commitSync(data.asJava))

      def adaptConsumerRecords(records: ConsumerRecords[K, V]): Map[TopicPartition, Chunk[ConsumerRecord[K, V]]] = {
        val tps          = records.partitions()
        val partitionMap = Map.newBuilder[TopicPartition, Chunk[ConsumerRecord[K, V]]]
        partitionMap.sizeHint(tps.size)

        val tpsIt = tps.iterator()
        while (tpsIt.hasNext) {
          val tp   = tpsIt.next
          val recs = Chunk.fromIterable(records.records(tp).asScala)

          partitionMap += tp -> recs
        }

        partitionMap.result()
      }

      def poll(pollTimeout: Duration): BlockingTask[Map[TopicPartition, Chunk[ConsumerRecord[K, V]]]] =
        withConsumer(c) { c =>
          adaptConsumerRecords(c.poll(pollTimeout.asJava))
        }

      def subscribeWith(subscription: Subscription)(listener: Rebalance => UIO[Unit]): BlockingTask[Unit] =
        for {
          runtime <- ZIO.runtime[Blocking]
          jlistener = new ConsumerRebalanceListener {
            def onPartitionsRevoked(partitions: java.util.Collection[TopicPartition]): Unit =
              runtime.unsafeRun(listener(Rebalance.Revoke(partitions.asScala.toList)))
            def onPartitionsAssigned(partitions: java.util.Collection[TopicPartition]): Unit =
              runtime.unsafeRun(listener(Rebalance.Assign(partitions.asScala.toList)))
          }
          _ <- subscription match {
                case Subscription.Topics(topics) =>
                  withConsumer(c)(_.subscribe(topics.asJava, jlistener))
                case Subscription.Pattern(pattern) =>
                  for {
                    p <- ZIO(java.util.regex.Pattern.compile(pattern))
                    _ <- withConsumer(c)(_.subscribe(p, jlistener))
                  } yield ()
              }
        } yield ()

      def unsubscribe: BlockingTask[Unit] =
        withConsumer(c)(_.unsubscribe())

      def pause(partitions: Set[TopicPartition]): BlockingTask[Unit] =
        withConsumer(c)(_.pause(partitions.asJava))

      def resume(partitions: Set[TopicPartition]): BlockingTask[Unit] =
        withConsumer(c)(_.resume(partitions.asJava))

      def seek(partition: TopicPartition, offset: Long): BlockingTask[Unit] =
        withConsumer(c)(_.seek(partition, offset))

      def seekToBeginning(partitions: Set[TopicPartition]): BlockingTask[Unit] =
        withConsumer(c)(_.seekToBeginning(partitions.asJava))

      def seekToEnd(partitions: Set[TopicPartition]): BlockingTask[Unit] =
        withConsumer(c)(_.seekToEnd(partitions.asJava))
    }

  def make[K, V](
    settings: ConsumerSettings
  )(implicit keySerde: Serde[K], valueSerde: Serde[V]): ZManaged[Blocking, Throwable, Consumer[K, V]] = {
    val c = blocking {
      ZIO {
        val props = settings.driverSettings.asJava

        new KafkaConsumer[K, V](props, keySerde.deserializer, valueSerde.deserializer)
      }
    }

    c.toManaged(c => UIO(c.close(settings.closeTimeout.asJava)))
      .map(unsafeMake)
  }
}
