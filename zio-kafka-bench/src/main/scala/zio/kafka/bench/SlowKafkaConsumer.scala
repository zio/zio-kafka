package zio.kafka.bench

import org.apache.kafka.clients.consumer._
import org.apache.kafka.common._

import java.time.Duration
import java.util.regex.Pattern
import java.util.{ Collection => JavaCollection, List => JavaList, Map => JavaMap, OptionalLong, Set => JavaSet }
import java.lang.{ Long => JavaLong }
import scala.annotation.nowarn

/**
 * A consumer that start each call with the given delay, and then calls the wrapped consumer. This can be used as a
 * rough approximation of network delay.
 */
class SlowKafkaConsumer[K, V](wrapped: Consumer[K, V], delay: Duration) extends Consumer[K, V] {
  private val delayMillis = delay.toMillis

  override def assignment(): JavaSet[TopicPartition] = {
    Thread.sleep(delayMillis)
    wrapped.assignment()
  }

  override def subscription(): JavaSet[String] = {
    Thread.sleep(delayMillis)
    wrapped.subscription()
  }

  override def subscribe(topics: JavaCollection[String]): Unit = {
    Thread.sleep(delayMillis)
    wrapped.subscribe(topics)
  }

  override def subscribe(topics: JavaCollection[String], callback: ConsumerRebalanceListener): Unit = {
    Thread.sleep(delayMillis)
    wrapped.subscribe(topics, callback)
  }

  override def assign(partitions: JavaCollection[TopicPartition]): Unit = {
    Thread.sleep(delayMillis)
    wrapped.assign(partitions)
  }

  override def subscribe(pattern: Pattern, callback: ConsumerRebalanceListener): Unit = {
    Thread.sleep(delayMillis)
    wrapped.subscribe(pattern, callback)
  }

  override def subscribe(pattern: Pattern): Unit = {
    Thread.sleep(delayMillis)
    wrapped.subscribe(pattern)
  }

  override def unsubscribe(): Unit = {
    Thread.sleep(delayMillis)
    wrapped.unsubscribe()
  }

  // noinspection ScalaDeprecation
  @nowarn("msg=deprecated")
  override def poll(timeout: Long): ConsumerRecords[K, V] = {
    Thread.sleep(delayMillis)
    wrapped.poll(timeout)
  }

  override def commitSync(): Unit = {
    Thread.sleep(delayMillis)
    wrapped.commitSync()
  }

  override def commitSync(timeout: Duration): Unit = {
    Thread.sleep(delayMillis)
    wrapped.commitSync(timeout)
  }

  override def commitSync(offsets: JavaMap[TopicPartition, OffsetAndMetadata]): Unit = {
    Thread.sleep(delayMillis)
    wrapped.commitSync(offsets)
  }

  override def commitSync(offsets: JavaMap[TopicPartition, OffsetAndMetadata], timeout: Duration): Unit = {
    Thread.sleep(delayMillis)
    wrapped.commitSync(offsets, timeout)
  }

  override def commitAsync(): Unit = {
    Thread.sleep(delayMillis)
    wrapped.commitAsync()
  }

  override def commitAsync(callback: OffsetCommitCallback): Unit = {
    Thread.sleep(delayMillis)
    wrapped.commitAsync(callback)
  }

  override def commitAsync(
    offsets: JavaMap[TopicPartition, OffsetAndMetadata],
    callback: OffsetCommitCallback
  ): Unit = {
    Thread.sleep(delayMillis)
    wrapped.commitAsync(offsets, callback)
  }

  override def seek(partition: TopicPartition, offset: Long): Unit = {
    Thread.sleep(delayMillis)
    wrapped.seek(partition, offset)
  }

  override def seek(partition: TopicPartition, offsetAndMetadata: OffsetAndMetadata): Unit = {
    Thread.sleep(delayMillis)
    wrapped.seek(partition, offsetAndMetadata)
  }

  override def seekToBeginning(partitions: JavaCollection[TopicPartition]): Unit = {
    Thread.sleep(delayMillis)
    wrapped.seekToBeginning(partitions)
  }

  override def seekToEnd(partitions: JavaCollection[TopicPartition]): Unit = {
    Thread.sleep(delayMillis)
    wrapped.seekToEnd(partitions)
  }

  override def position(partition: TopicPartition): Long = {
    Thread.sleep(delayMillis)
    wrapped.position(partition)
  }

  override def position(partition: TopicPartition, timeout: Duration): Long = {
    Thread.sleep(delayMillis)
    wrapped.position(partition, timeout)
  }

  // noinspection ScalaDeprecation
  @nowarn("msg=deprecated")
  override def committed(partition: TopicPartition): OffsetAndMetadata = {
    Thread.sleep(delayMillis)
    wrapped.committed(partition)
  }

  // noinspection ScalaDeprecation
  @nowarn("msg=deprecated")
  override def committed(partition: TopicPartition, timeout: Duration): OffsetAndMetadata = {
    Thread.sleep(delayMillis)
    wrapped.committed(partition, timeout)
  }

  override def poll(timeout: Duration): ConsumerRecords[K, V] = {
    Thread.sleep(delayMillis)
    wrapped.poll(timeout)
  }

  override def committed(partitions: JavaSet[TopicPartition]): JavaMap[TopicPartition, OffsetAndMetadata] = {
    Thread.sleep(delayMillis)
    wrapped.committed(partitions)
  }

  override def committed(
    partitions: JavaSet[TopicPartition],
    timeout: Duration
  ): JavaMap[TopicPartition, OffsetAndMetadata] = {
    Thread.sleep(delayMillis)
    wrapped.committed(partitions, timeout)
  }

  override def clientInstanceId(timeout: Duration): Uuid = {
    Thread.sleep(delayMillis)
    wrapped.clientInstanceId(timeout)
  }

  override def metrics(): JavaMap[MetricName, _ <: Metric] = {
    // No network traffic, no delay
    wrapped.metrics()
  }

  override def partitionsFor(topic: String): JavaList[PartitionInfo] = {
    Thread.sleep(delayMillis)
    wrapped.partitionsFor(topic)
  }

  override def partitionsFor(topic: String, timeout: Duration): JavaList[PartitionInfo] = {
    Thread.sleep(delayMillis)
    wrapped.partitionsFor(topic, timeout)
  }

  override def listTopics(): JavaMap[String, JavaList[PartitionInfo]] = {
    Thread.sleep(delayMillis)
    wrapped.listTopics()
  }

  override def listTopics(timeout: Duration): JavaMap[String, JavaList[PartitionInfo]] = {
    Thread.sleep(delayMillis)
    wrapped.listTopics(timeout)
  }

  override def paused(): JavaSet[TopicPartition] = {
    // No network traffic, no delay
    wrapped.paused()
  }

  override def pause(partitions: JavaCollection[TopicPartition]): Unit =
    wrapped.pause(partitions)

  override def resume(partitions: JavaCollection[TopicPartition]): Unit = {
    // No network traffic, no delay
    wrapped.resume(partitions)
  }

  override def offsetsForTimes(
    timestampsToSearch: JavaMap[TopicPartition, JavaLong]
  ): JavaMap[TopicPartition, OffsetAndTimestamp] = {
    Thread.sleep(delayMillis)
    wrapped.offsetsForTimes(timestampsToSearch)
  }

  override def offsetsForTimes(
    timestampsToSearch: JavaMap[TopicPartition, JavaLong],
    timeout: Duration
  ): JavaMap[TopicPartition, OffsetAndTimestamp] = {
    Thread.sleep(delayMillis)
    wrapped.offsetsForTimes(timestampsToSearch, timeout)
  }

  override def beginningOffsets(partitions: JavaCollection[TopicPartition]): JavaMap[TopicPartition, JavaLong] = {
    Thread.sleep(delayMillis)
    wrapped.beginningOffsets(partitions)
  }

  override def beginningOffsets(
    partitions: JavaCollection[TopicPartition],
    timeout: Duration
  ): JavaMap[TopicPartition, JavaLong] = {
    Thread.sleep(delayMillis)
    wrapped.beginningOffsets(partitions, timeout)
  }

  override def endOffsets(partitions: JavaCollection[TopicPartition]): JavaMap[TopicPartition, JavaLong] = {
    Thread.sleep(delayMillis)
    wrapped.endOffsets(partitions)
  }

  override def endOffsets(
    partitions: JavaCollection[TopicPartition],
    timeout: Duration
  ): JavaMap[TopicPartition, JavaLong] = {
    Thread.sleep(delayMillis)
    wrapped.endOffsets(partitions, timeout)
  }

  override def currentLag(topicPartition: TopicPartition): OptionalLong = {
    Thread.sleep(delayMillis)
    wrapped.currentLag(topicPartition)
  }

  override def groupMetadata(): ConsumerGroupMetadata = {
    // No network traffic, no delay
    wrapped.groupMetadata()
  }

  override def enforceRebalance(): Unit = {
    Thread.sleep(delayMillis)
    wrapped.enforceRebalance()
  }

  override def enforceRebalance(reason: String): Unit = {
    Thread.sleep(delayMillis)
    wrapped.enforceRebalance(reason)
  }

  override def close(): Unit = {
    Thread.sleep(delayMillis)
    wrapped.close()
  }

  override def close(timeout: Duration): Unit = {
    Thread.sleep(delayMillis)
    wrapped.close(timeout)
  }

  override def wakeup(): Unit = {
    // No network traffic, no delay
    wrapped.wakeup()
  }
}
