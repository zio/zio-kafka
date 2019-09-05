package zio.kafka.client

import net.manub.embeddedkafka.EmbeddedKafka
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.TopicPartition
import zio.{ Chunk, UIO, ZIO }
import org.apache.kafka.clients.producer.ProducerRecord

object KafkaTestUtils {

  def produceOne(t: String, k: String, m: String): UIO[Unit] = ZIO.effectTotal {
    import net.manub.embeddedkafka.Codecs._
    EmbeddedKafka.publishToKafka(t, k, m)
  }

  def produceMany(t: String, kvs: List[(String, String)]): UIO[Unit] =
    UIO.foreach(kvs)(i => produceOne(t, i._1, i._2)).unit

  def produceMany(topic: String, partition: Int, kvs: List[(String, String)]) =
    ZIO.effectTotal {
      import net.manub.embeddedkafka.Codecs._
      val records = kvs.map {
        case (k, v) => new ProducerRecord[String, String](topic, partition, null, k, v)
      }

      records.foreach(rec => EmbeddedKafka.publishToKafka[String](rec))
    }

  def recordsFromAllTopics[K, V](
    pollResult: Map[TopicPartition, Chunk[ConsumerRecord[K, V]]]
  ): Chunk[ConsumerRecord[K, V]] =
    Chunk.fromIterable(pollResult.values).flatMap(identity)

  def getAllRecordsFromMultiplePolls[K, V](
    res: List[Map[TopicPartition, Chunk[ConsumerRecord[K, V]]]]
  ): Chunk[ConsumerRecord[K, V]] =
    res.foldLeft[Chunk[ConsumerRecord[K, V]]](Chunk.empty)(
      (acc, pollResult) => acc ++ recordsFromAllTopics[K, V](pollResult)
    )

  def tp(topic: String, partition: Int): TopicPartition = new TopicPartition(topic, partition)
}
