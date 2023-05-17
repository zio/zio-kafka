package zio.kafka.consumer.internal

import org.apache.kafka.clients.consumer.{
  ConsumerGroupMetadata,
  ConsumerRebalanceListener,
  ConsumerRecords,
  OffsetAndMetadata,
  OffsetAndTimestamp,
  OffsetCommitCallback
}
import org.apache.kafka.common.{ Metric, MetricName, PartitionInfo, TopicPartition }

import java.time.Duration
import java.{ lang, util }
import java.util.OptionalLong
import java.util.regex.Pattern

class ProxyKafkaConsumer extends org.apache.kafka.clients.consumer.Consumer[Array[Byte], Array[Byte]] {
  override def assignment(): util.Set[TopicPartition] = ???

  override def subscription(): util.Set[String] = ???

  override def subscribe(topics: util.Collection[String]): Unit = ???

  override def subscribe(topics: util.Collection[String], callback: ConsumerRebalanceListener): Unit = ???

  override def assign(partitions: util.Collection[TopicPartition]): Unit = ???

  override def subscribe(pattern: Pattern, callback: ConsumerRebalanceListener): Unit = ???

  override def subscribe(pattern: Pattern): Unit = ???

  override def unsubscribe(): Unit = ???

  override def poll(timeout: Long): ConsumerRecords[Array[Byte], Array[Byte]] = ???

  override def poll(timeout: Duration): ConsumerRecords[Array[Byte], Array[Byte]] = ???

  override def commitSync(): Unit = ???

  override def commitSync(timeout: Duration): Unit = ???

  override def commitSync(offsets: util.Map[TopicPartition, OffsetAndMetadata]): Unit = ???

  override def commitSync(offsets: util.Map[TopicPartition, OffsetAndMetadata], timeout: Duration): Unit = ???

  override def commitAsync(): Unit = ???

  override def commitAsync(callback: OffsetCommitCallback): Unit = ???

  override def commitAsync(offsets: util.Map[TopicPartition, OffsetAndMetadata], callback: OffsetCommitCallback): Unit =
    ???

  override def seek(partition: TopicPartition, offset: Long): Unit = ???

  override def seek(partition: TopicPartition, offsetAndMetadata: OffsetAndMetadata): Unit = ???

  override def seekToBeginning(partitions: util.Collection[TopicPartition]): Unit = ???

  override def seekToEnd(partitions: util.Collection[TopicPartition]): Unit = ???

  override def position(partition: TopicPartition): Long = ???

  override def position(partition: TopicPartition, timeout: Duration): Long = ???

  override def committed(partition: TopicPartition): OffsetAndMetadata = ???

  override def committed(partition: TopicPartition, timeout: Duration): OffsetAndMetadata = ???

  override def committed(partitions: util.Set[TopicPartition]): util.Map[TopicPartition, OffsetAndMetadata] = ???

  override def committed(
    partitions: util.Set[TopicPartition],
    timeout: Duration
  ): util.Map[TopicPartition, OffsetAndMetadata] = ???

  override def metrics(): util.Map[MetricName, _ <: Metric] = ???

  override def partitionsFor(topic: String): util.List[PartitionInfo] = ???

  override def partitionsFor(topic: String, timeout: Duration): util.List[PartitionInfo] = ???

  override def listTopics(): util.Map[String, util.List[PartitionInfo]] = ???

  override def listTopics(timeout: Duration): util.Map[String, util.List[PartitionInfo]] = ???

  override def paused(): util.Set[TopicPartition] = ???

  override def pause(partitions: util.Collection[TopicPartition]): Unit = ???

  override def resume(partitions: util.Collection[TopicPartition]): Unit = ???

  override def offsetsForTimes(
    timestampsToSearch: util.Map[TopicPartition, lang.Long]
  ): util.Map[TopicPartition, OffsetAndTimestamp] = ???

  override def offsetsForTimes(
    timestampsToSearch: util.Map[TopicPartition, lang.Long],
    timeout: Duration
  ): util.Map[TopicPartition, OffsetAndTimestamp] = ???

  override def beginningOffsets(partitions: util.Collection[TopicPartition]): util.Map[TopicPartition, lang.Long] = ???

  override def beginningOffsets(
    partitions: util.Collection[TopicPartition],
    timeout: Duration
  ): util.Map[TopicPartition, lang.Long] = ???

  override def endOffsets(partitions: util.Collection[TopicPartition]): util.Map[TopicPartition, lang.Long] = ???

  override def endOffsets(
    partitions: util.Collection[TopicPartition],
    timeout: Duration
  ): util.Map[TopicPartition, lang.Long] = ???

  override def currentLag(topicPartition: TopicPartition): OptionalLong = ???

  override def groupMetadata(): ConsumerGroupMetadata = ???

  override def enforceRebalance(): Unit = ???

  override def enforceRebalance(reason: String): Unit = ???

  override def close(): Unit = ???

  override def close(timeout: Duration): Unit = ???

  override def wakeup(): Unit = ???
}
