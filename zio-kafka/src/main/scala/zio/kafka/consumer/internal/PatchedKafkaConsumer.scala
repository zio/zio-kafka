package zio.kafka.consumer.internal

import net.bytebuddy.ByteBuddy
import net.bytebuddy.implementation.StubMethod
import net.bytebuddy.matcher.ElementMatchers.named
import org.apache.kafka.clients.consumer._
import org.apache.kafka.common.serialization.{ ByteArrayDeserializer, Deserializer }
import org.apache.kafka.common.{ Metric, MetricName, PartitionInfo, TopicPartition }

import java.lang.reflect.Constructor
import java.time.Duration
import java.util.OptionalLong
import java.util.regex.Pattern
import java.{ lang, util }
import scala.annotation.nowarn

object PatchedKafkaConsumer {

  /**
   * See https://bytebuddy.net/#/tutorial
   *
   * We no-op the private [[KafkaConsumer#acquireAndEnsureOpen]] method because it's an expensive operation we don't
   * need because we already control that the access to the Consumer instances are not concurrent.
   */
  @noinline
  private val consumer: Constructor[_ <: KafkaConsumer[_, _]] =
    new ByteBuddy()
      .subclass(classOf[KafkaConsumer[_, _]])
      .method(named("acquireAndEnsureOpen"))
      .intercept(StubMethod.INSTANCE)
      .make()
      .load(getClass.getClassLoader)
      .getLoaded
      .getConstructor(classOf[java.util.Map[String, AnyRef]], classOf[Deserializer[_]], classOf[Deserializer[_]])

  def make(settings: java.util.Map[String, AnyRef]): Consumer[Array[Byte], Array[Byte]] =
    new PatchedKafkaConsumer(
      consumer
        .newInstance(settings, new ByteArrayDeserializer(), new ByteArrayDeserializer())
        .asInstanceOf[Consumer[Array[Byte], Array[Byte]]]
    )
}

final class PatchedKafkaConsumer(patchedConsumer: Consumer[Array[Byte], Array[Byte]])
    extends Consumer[Array[Byte], Array[Byte]] {
  override def assignment(): util.Set[TopicPartition] =
    patchedConsumer.assignment()

  override def subscription(): util.Set[String] =
    patchedConsumer.subscription()

  override def subscribe(collection: util.Collection[String]): Unit =
    patchedConsumer.subscribe(collection)

  override def subscribe(
    collection: util.Collection[String],
    consumerRebalanceListener: ConsumerRebalanceListener
  ): Unit =
    patchedConsumer.subscribe(collection, consumerRebalanceListener)

  override def assign(collection: util.Collection[TopicPartition]): Unit =
    patchedConsumer.assign(collection)

  override def subscribe(pattern: Pattern, consumerRebalanceListener: ConsumerRebalanceListener): Unit =
    patchedConsumer.subscribe(pattern, consumerRebalanceListener)

  override def subscribe(pattern: Pattern): Unit =
    patchedConsumer.subscribe(pattern)

  override def unsubscribe(): Unit =
    patchedConsumer.unsubscribe()

  @nowarn("cat=deprecation")
  override def poll(l: Long): ConsumerRecords[Array[Byte], Array[Byte]] =
    patchedConsumer.poll(l)

  override def poll(duration: Duration): ConsumerRecords[Array[Byte], Array[Byte]] =
    patchedConsumer.poll(duration)

  override def commitSync(): Unit =
    patchedConsumer.commitSync()

  override def commitSync(duration: Duration): Unit =
    patchedConsumer.commitSync(duration)

  override def commitSync(map: util.Map[TopicPartition, OffsetAndMetadata]): Unit =
    patchedConsumer.commitSync(map)

  override def commitSync(map: util.Map[TopicPartition, OffsetAndMetadata], duration: Duration): Unit =
    patchedConsumer.commitSync(map, duration)

  override def commitAsync(): Unit =
    patchedConsumer.commitAsync()

  override def commitAsync(offsetCommitCallback: OffsetCommitCallback): Unit =
    patchedConsumer.commitAsync(offsetCommitCallback)

  override def commitAsync(
    map: util.Map[TopicPartition, OffsetAndMetadata],
    offsetCommitCallback: OffsetCommitCallback
  ): Unit =
    patchedConsumer.commitAsync(map, offsetCommitCallback)

  override def seek(topicPartition: TopicPartition, l: Long): Unit =
    patchedConsumer.seek(topicPartition, l)

  override def seek(topicPartition: TopicPartition, offsetAndMetadata: OffsetAndMetadata): Unit =
    patchedConsumer.seek(topicPartition, offsetAndMetadata)

  override def seekToBeginning(collection: util.Collection[TopicPartition]): Unit =
    patchedConsumer.seekToBeginning(collection)

  override def seekToEnd(collection: util.Collection[TopicPartition]): Unit =
    patchedConsumer.seekToEnd(collection)

  override def position(topicPartition: TopicPartition): Long =
    patchedConsumer.position(topicPartition)

  override def position(topicPartition: TopicPartition, duration: Duration): Long =
    patchedConsumer.position(topicPartition, duration)

  @nowarn("cat=deprecation")
  override def committed(topicPartition: TopicPartition): OffsetAndMetadata =
    patchedConsumer.committed(topicPartition)

  @nowarn("cat=deprecation")
  override def committed(topicPartition: TopicPartition, duration: Duration): OffsetAndMetadata =
    patchedConsumer.committed(topicPartition, duration)

  override def committed(set: util.Set[TopicPartition]): util.Map[TopicPartition, OffsetAndMetadata] =
    patchedConsumer.committed(set)

  override def committed(
    set: util.Set[TopicPartition],
    duration: Duration
  ): util.Map[TopicPartition, OffsetAndMetadata] =
    patchedConsumer.committed(set, duration)

  override def metrics(): util.Map[MetricName, _ <: Metric] =
    patchedConsumer.metrics()

  override def partitionsFor(s: String): util.List[PartitionInfo] =
    patchedConsumer.partitionsFor(s)

  override def partitionsFor(s: String, duration: Duration): util.List[PartitionInfo] =
    patchedConsumer.partitionsFor(s, duration)

  override def listTopics(): util.Map[String, util.List[PartitionInfo]] =
    patchedConsumer.listTopics()

  override def listTopics(duration: Duration): util.Map[String, util.List[PartitionInfo]] =
    patchedConsumer.listTopics(duration)

  override def paused(): util.Set[TopicPartition] =
    patchedConsumer.paused()

  override def pause(collection: util.Collection[TopicPartition]): Unit =
    patchedConsumer.pause(collection)

  override def resume(collection: util.Collection[TopicPartition]): Unit =
    patchedConsumer.resume(collection)

  override def offsetsForTimes(map: util.Map[TopicPartition, lang.Long]): util.Map[TopicPartition, OffsetAndTimestamp] =
    patchedConsumer.offsetsForTimes(map)

  override def offsetsForTimes(
    map: util.Map[TopicPartition, lang.Long],
    duration: Duration
  ): util.Map[TopicPartition, OffsetAndTimestamp] =
    patchedConsumer.offsetsForTimes(map, duration)

  override def beginningOffsets(collection: util.Collection[TopicPartition]): util.Map[TopicPartition, lang.Long] =
    patchedConsumer.beginningOffsets(collection)

  override def beginningOffsets(
    collection: util.Collection[TopicPartition],
    duration: Duration
  ): util.Map[TopicPartition, lang.Long] =
    patchedConsumer.beginningOffsets(collection, duration)

  override def endOffsets(collection: util.Collection[TopicPartition]): util.Map[TopicPartition, lang.Long] =
    patchedConsumer.endOffsets(collection)

  override def endOffsets(
    collection: util.Collection[TopicPartition],
    duration: Duration
  ): util.Map[TopicPartition, lang.Long] =
    patchedConsumer.endOffsets(collection, duration)

  override def currentLag(topicPartition: TopicPartition): OptionalLong =
    patchedConsumer.currentLag(topicPartition)

  override def groupMetadata(): ConsumerGroupMetadata =
    patchedConsumer.groupMetadata()

  override def enforceRebalance(): Unit =
    patchedConsumer.enforceRebalance()

  override def enforceRebalance(s: String): Unit =
    patchedConsumer.enforceRebalance(s)

  override def close(): Unit =
    patchedConsumer.close()

  override def close(duration: Duration): Unit =
    patchedConsumer.close(duration)

  override def wakeup(): Unit =
    patchedConsumer.wakeup()
}
