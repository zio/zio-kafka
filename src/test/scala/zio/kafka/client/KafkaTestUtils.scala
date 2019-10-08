package zio.kafka.client

import net.manub.embeddedkafka.EmbeddedKafka
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.TopicPartition
import zio.{ Chunk, UIO, ZIO }

object KafkaTestUtils {

  def produceOne(t: String, k: String, m: String): UIO[Unit] = ZIO.effectTotal {
    import net.manub.embeddedkafka.Codecs._
    EmbeddedKafka.publishToKafka(t, k, m)
  }

  def produceOne(t: String, partition: Int, k: String, m: String): UIO[Unit] = ZIO.effectTotal {
    import net.manub.embeddedkafka.Codecs._
    val record = new ProducerRecord[String, String](t, partition, null, k, m)
    EmbeddedKafka.publishToKafka[String](record)
  }

  def produceMany(t: String, kvs: List[(String, String)]): UIO[Unit] = ZIO.effectTotal {
    import net.manub.embeddedkafka.Codecs._
    EmbeddedKafka.publishToKafka(t, kvs)
  }

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
