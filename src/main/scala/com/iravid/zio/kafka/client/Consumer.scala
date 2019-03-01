package com.iravid.zio.kafka.client

import java.time.{ Duration => JDuration }
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.serialization.ByteArrayDeserializer
import org.apache.kafka.clients.consumer.ConsumerRebalanceListener
import scalaz.zio.{ Chunk, Exit, Managed, UIO, ZIO }
import scalaz.zio.blocking._
import scalaz.zio.duration._

import scala.collection.JavaConverters._

trait Consumer {
  def commit(data: OffsetMap): BlockingTask[Unit]

  def poll(pollTimeout: Duration): BlockingTask[Map[TopicPartition, Chunk[ByteRecord]]]

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
  def unsafeMake(c: ByteConsumer): Consumer =
    new Consumer {
      def commit(data: OffsetMap) = blocking(ZIO(c.commitSync(data.asJava)))

      def adaptConsumerRecords(records: ByteRecords): Map[TopicPartition, Chunk[ByteRecord]] = {
        val tps          = records.partitions()
        val partitionMap = Map.newBuilder[TopicPartition, Chunk[ByteRecord]]
        partitionMap.sizeHint(tps.size)

        val tpsIt = tps.iterator()
        while (tpsIt.hasNext) {
          val tp   = tpsIt.next
          val recs = Chunk.fromArray(records.records(tp).toArray().asInstanceOf[Array[ByteRecord]])

          partitionMap += tp -> recs
        }

        partitionMap.result()
      }

      def poll(pollTimeout: Duration): BlockingTask[Map[TopicPartition, Chunk[ByteRecord]]] =
        blocking {
          ZIO(adaptConsumerRecords(c.poll(JDuration.ofMillis(pollTimeout.fold(Long.MaxValue, _.toMillis))))).fork.bracketExit {
            (_, e: Exit[Throwable, Map[TopicPartition, Chunk[ByteRecord]]]) =>
              if (e.interrupted) ZIO.effect(c.wakeup()).either
              else ZIO.unit
          }(_.join).catchSome {
            case _: org.apache.kafka.common.errors.WakeupException =>
              ZIO.succeed(Map.empty)
          }
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
                  ZIO(c.subscribe(topics.asJava, jlistener))
                case Subscription.Pattern(pattern) =>
                  for {
                    p <- ZIO(java.util.regex.Pattern.compile(pattern))
                    _ <- ZIO(c.subscribe(p, jlistener))
                  } yield ()
              }
        } yield ()

      def unsubscribe: BlockingTask[Unit] =
        blocking(ZIO(c.unsubscribe()))

      def pause(partitions: Set[TopicPartition]): BlockingTask[Unit] =
        blocking(ZIO(c.pause(partitions.asJava)))

      def resume(partitions: Set[TopicPartition]): BlockingTask[Unit] =
        blocking(ZIO(c.resume(partitions.asJava)))

      def seek(partition: TopicPartition, offset: Long): BlockingTask[Unit] =
        blocking(ZIO(c.seek(partition, offset)))

      def seekToBeginning(partitions: Set[TopicPartition]): BlockingTask[Unit] =
        blocking(ZIO(c.seekToBeginning(partitions.asJava)))

      def seekToEnd(partitions: Set[TopicPartition]): BlockingTask[Unit] =
        blocking(ZIO(c.seekToEnd(partitions.asJava)))
    }

  def make(settings: ConsumerSettings): Managed[Blocking, Throwable, Consumer] = {
    val c = blocking {
      ZIO {
        val props = new java.util.Properties

        settings.driverSettings.foreach {
          case (k, v) => props.put(k, v)
        }

        new ByteConsumer(props, new ByteArrayDeserializer, new ByteArrayDeserializer)
      }
    }

    c.managed(c => UIO(c.close(JDuration.ofMillis(settings.closeTimeout.toMillis))))
      .map(unsafeMake)
  }
}
