package zio.kafka.benchmarks.fixtures

import java.time.Duration
import java.util
import java.util.concurrent.TimeUnit
import java.util.{ Arrays, Properties, UUID }

import com.typesafe.scalalogging.LazyLogging
import org.apache.kafka.clients.admin.{ AdminClient, NewTopic }
import org.apache.kafka.clients.producer.{ KafkaProducer, ProducerConfig, ProducerRecord }
import org.apache.kafka.common.serialization.ByteArraySerializer
import zio.blocking.Blocking
import zio.kafka.benchmarks.scenarios.ZioProducerBenchmarks.logStep
import zio.kafka.producer.Producer.Live
import zio.kafka.producer.{ Producer, ProducerSettings }
import zio.kafka.serde.{ Serde, Serializer }
import zio.stream.Stream
import zio.{ UIO, ZIO, ZManaged }

import scala.concurrent.duration._
import scala.jdk.CollectionConverters._
import scala.language.postfixOps

object PerfFixtureHelpers {
  def stringOfSize(size: Int) = new String(Array.fill(size)('0'))

  def randomId(): String = UUID.randomUUID().toString

  case class FilledTopic(
    msgCount: Int,
    msgSize: Int,
    numberOfPartitions: Int = 1,
    replicationFactor: Int = 1,
    topic: String = randomId()
  ) {
    def freshTopic: FilledTopic = copy(topic = randomId())
  }
}

private[benchmarks] trait PerfFixtureHelpers extends LazyLogging {
  import PerfFixtureHelpers._

  val producerTimeout         = 6 minutes
  val logPercentStep          = 25
  val adminClientCloseTimeout = Duration.ofSeconds(5)
  val producerCloseTimeout    = adminClientCloseTimeout

  def randomId(): String = PerfFixtureHelpers.randomId()

  def createTopic(
    ft: FilledTopic,
    kafkaHost: String
  ): ZManaged[Blocking, Throwable, Producer.Live[Any, Array[Byte], Array[Byte]]] = {
    val props = new Properties
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaHost)
    ZManaged
      .makeEffect(AdminClient.create(props))(_.close(adminClientCloseTimeout))
      .flatMap(admin => createTopicAndFill(ft, kafkaHost, admin))
  }

  private def makeProducer[R, K, V](
    settings: ProducerSettings,
    keySerializer: Serializer[R, K],
    valueSerializer: Serializer[R, V]
  ): ZManaged[Any, Throwable, Live[R, K, V]] =
    (for {
      props <- ZIO.effect(settings.driverSettings)
      _     <- keySerializer.configure(props, isKey = true)
      _     <- valueSerializer.configure(props, isKey = false)
      rawProducer <- ZIO.effect(
                      new KafkaProducer[Array[Byte], Array[Byte]](
                        props.asJava,
                        new ByteArraySerializer(),
                        new ByteArraySerializer()
                      )
                    )
    } yield Live(rawProducer, settings, keySerializer, valueSerializer)).toManaged { live =>
      UIO(live.p.close(live.producerSettings.closeTimeout))
    }

  private def createTopicAndFill(
    ft: FilledTopic,
    kafkaHost: String,
    admin: AdminClient
  ): ZManaged[Blocking, Throwable, Producer.Live[Any, Array[Byte], Array[Byte]]] = {
    val parallelism = 10
    ZManaged.succeed {
      val result = admin.createTopics(
        Arrays.asList(
          new NewTopic(ft.topic, ft.numberOfPartitions, ft.replicationFactor.toShort)
            .configs(new util.HashMap[String, String]())
        )
      )
      result.all().get(10, TimeUnit.SECONDS)
    } *> {
      // fill topic with messages
      makeProducer(ProducerSettings(List(kafkaHost)), Serde.byteArray, Serde.byteArray).mapM { zProducer =>
        val loggedStep = if (ft.msgCount > logPercentStep) ft.msgCount / (100 / logPercentStep) else 1
        val msg        = stringOfSize(ft.msgSize)
        Stream
          .range(0, ft.msgCount - 1)
          .chunkN(1000)
          .map { i =>
            val partition: Int = (i % ft.numberOfPartitions).toInt
            new ProducerRecord[Array[Byte], Array[Byte]](ft.topic, partition, null, msg.getBytes)
          }
          .mapMPar(parallelism)(zProducer.produce)
          .mapAccum(System.nanoTime()) {
            case (lastPartStart, msg) =>
              val lastPartEnd = if (msg.offset % logStep == 0) {
                val lastPartEnd = System.nanoTime()
                val took        = (lastPartEnd - lastPartStart).nanos
                logger.info(s"Sent ${msg.offset}, took ${took.toMillis} ms to send last $loggedStep")
                lastPartEnd
              } else lastPartStart
              (lastPartEnd, msg)
          }
          .runDrain
          .as(zProducer)
      }
    }
  }
}
