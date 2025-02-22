package zio.kafka.bench.comparison

import zio.kafka.admin.AdminClient.{ NewTopic, TopicPartition }
import zio.kafka.bench.Layers.LowLevelKafka
import zio.kafka.bench.comparison.ComparisonBenchmark.Env
import zio.kafka.bench.{ ConsumerZioBenchmark, Layers }
import zio.kafka.consumer.{ Consumer, ConsumerSettings }
import zio.kafka.testkit.{ Kafka, KafkaTestUtils }
import zio.{ Scope => _, _ }

trait ComparisonBenchmark extends ConsumerZioBenchmark[Env] {

  protected final val topicPartitions: List[TopicPartition] =
    (0 until partitionCount).map(TopicPartition(topic1, _)).toList

  override final def bootstrap: ULayer[Env] =
    ZLayer
      .make[Env](
        Kafka.embedded,
        Layers.consumerSettings,
        Layers.javaKafkaConsumer,
        Layers.consumerLayer
      )
      .orDie

  override final def initialize: ZIO[Env, Throwable, Any] =
    ZIO.scoped {
      for {
        adminClient <- KafkaTestUtils.makeAdminClient
        _           <- adminClient.deleteTopic(topic1).ignore
        _           <- adminClient.createTopic(NewTopic(topic1, partitionCount, replicationFactor = 1))
        producer    <- KafkaTestUtils.makeProducer
        _           <- KafkaTestUtils.produceMany(producer, topic1, kvs)
      } yield ()
    }

}

object ComparisonBenchmark {

  type Env = Kafka with Consumer with LowLevelKafka with ConsumerSettings

  def zAssert(p: => Boolean, message: => String): ZIO[Any, AssertionError, Unit] =
    ZIO.when(!p)(ZIO.fail(new AssertionError(s"Assertion failed: $message"))).unit
}
