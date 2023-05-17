package zio.kafka.testkit

import org.apache.kafka.clients.consumer.{ ConsumerConfig, ConsumerRecord }
import org.apache.kafka.clients.producer.{ ProducerRecord, RecordMetadata }
import zio._
import zio.kafka.admin._
import zio.kafka.consumer.Consumer.{ AutoOffsetStrategy, OffsetRetrieval }
import zio.kafka.consumer._
import zio.kafka.consumer.diagnostics.Diagnostics
import zio.kafka.producer._
import zio.kafka.serde.{ Deserializer, Serde, Serializer }

import java.io.File
import java.nio.file.{ Files, StandardCopyOption }

object KafkaTestUtils {

  private def readResourceFile(file: String, tmpFileName: String, tmpFileSuffix: String): File =
    try {
      val tmpFile = Files.createTempFile(tmpFileName, tmpFileSuffix)
      Files.copy(getClass.getClassLoader.getResourceAsStream(file), tmpFile, StandardCopyOption.REPLACE_EXISTING)
      val result = tmpFile.toFile
      result.deleteOnExit()
      result
    } catch {
      case e: Throwable =>
        val _ = Unsafe.unsafe { implicit u =>
          zio.Runtime.default.unsafe.run(ZIO.logErrorCause("Failed to read resource file", Cause.fail(e)))
        }
        throw e
    }

  val trustStoreFile: File = readResourceFile("truststore/kafka.truststore.jks", "truststore", ".jks")
  val keyStoreFile: File   = readResourceFile("keystore/kafka.keystore.jks", "keystore", ".jks")

  val producerSettings: ZIO[Kafka, Nothing, ProducerSettings] =
    ZIO.serviceWith[Kafka](_.bootstrapServers).map(ProducerSettings(_))

  val producer: ZLayer[Kafka, Throwable, Producer] =
    (ZLayer.fromZIO(producerSettings) ++ ZLayer.succeed(Serde.string: Serializer[Any, String])) >>>
      Producer.live

  val transactionalProducerSettings: ZIO[Kafka, Nothing, TransactionalProducerSettings] =
    ZIO.serviceWith[Kafka](_.bootstrapServers).map(TransactionalProducerSettings(_, "test-transaction"))

  val transactionalProducer: ZLayer[Kafka, Throwable, TransactionalProducer] =
    (ZLayer.fromZIO(transactionalProducerSettings) ++ ZLayer.succeed(
      Serde.string: Serializer[Any, String]
    )) >>>
      TransactionalProducer.live

  def produceOne(
    topic: String,
    key: String,
    message: String
  ): ZIO[Producer, Throwable, RecordMetadata] =
    Producer.produce[Any, String, String](new ProducerRecord(topic, key, message), Serde.string, Serde.string)

  def produceMany(
    topic: String,
    partition: Int,
    kvs: Iterable[(String, String)]
  ): ZIO[Producer, Throwable, Chunk[RecordMetadata]] =
    Producer
      .produceChunk[Any, String, String](
        Chunk.fromIterable(kvs.map { case (k, v) =>
          new ProducerRecord(topic, partition, null, k, v)
        }),
        Serde.string,
        Serde.string
      )

  def produceMany(
    topic: String,
    kvs: Iterable[(String, String)]
  ): ZIO[Producer, Throwable, Chunk[RecordMetadata]] =
    Producer
      .produceChunk[Any, String, String](
        Chunk.fromIterable(kvs.map { case (k, v) =>
          new ProducerRecord(topic, k, v)
        }),
        Serde.string,
        Serde.string
      )

  def consumerSettings(
    clientId: String,
    groupId: Option[String] = None,
    clientInstanceId: Option[String] = None,
    allowAutoCreateTopics: Boolean = true,
    offsetRetrieval: OffsetRetrieval = OffsetRetrieval.Auto(),
    restartStreamOnRebalancing: Boolean = false,
    `max.poll.records`: Int = 100, // settings this higher can cause concurrency bugs to go unnoticed
    runloopTimeout: Duration = ConsumerSettings.defaultRunloopTimeout,
    properties: Map[String, String] = Map.empty
  ): URIO[Kafka, ConsumerSettings] =
    ZIO.serviceWith[Kafka] { (kafka: Kafka) =>
      val settings = ConsumerSettings(kafka.bootstrapServers)
        .withClientId(clientId)
        .withCloseTimeout(5.seconds)
        .withPollTimeout(100.millis)
        .withRunloopTimeout(runloopTimeout)
        .withProperties(
          ConsumerConfig.AUTO_OFFSET_RESET_CONFIG        -> "earliest",
          ConsumerConfig.METADATA_MAX_AGE_CONFIG         -> "100",
          ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG       -> "3000",
          ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG     -> "10000",
          ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG    -> "1000",
          ConsumerConfig.MAX_POLL_RECORDS_CONFIG         -> s"${`max.poll.records`}",
          ConsumerConfig.ALLOW_AUTO_CREATE_TOPICS_CONFIG -> allowAutoCreateTopics.toString
        )
        .withOffsetRetrieval(offsetRetrieval)
        .withRestartStreamOnRebalancing(restartStreamOnRebalancing)
        .withProperties(properties)

      val withClientInstanceId = clientInstanceId.fold(settings)(settings.withGroupInstanceId)
      groupId.fold(withClientInstanceId)(withClientInstanceId.withGroupId)
    }

  def transactionalConsumerSettings(
    groupId: String,
    clientId: String,
    clientInstanceId: Option[String] = None,
    allowAutoCreateTopics: Boolean = true,
    offsetRetrieval: OffsetRetrieval = OffsetRetrieval.Auto(),
    restartStreamOnRebalancing: Boolean = false,
    properties: Map[String, String] = Map.empty
  ): URIO[Kafka, ConsumerSettings] =
    consumerSettings(
      clientId = clientId,
      groupId = Some(groupId),
      clientInstanceId = clientInstanceId,
      allowAutoCreateTopics = allowAutoCreateTopics,
      offsetRetrieval = offsetRetrieval,
      restartStreamOnRebalancing = restartStreamOnRebalancing,
      properties = properties
    )
      .map(
        _.withProperties(
          ConsumerConfig.ISOLATION_LEVEL_CONFIG -> "read_committed"
        )
      )

  def simpleConsumer(diagnostics: Diagnostics = Diagnostics.NoOp): ZLayer[ConsumerSettings, Throwable, Consumer] =
    ZLayer.makeSome[ConsumerSettings, Consumer](
      ZLayer.succeed(diagnostics) >>> Consumer.live
    )

  def consumer(
    clientId: String,
    groupId: Option[String] = None,
    clientInstanceId: Option[String] = None,
    offsetRetrieval: OffsetRetrieval = OffsetRetrieval.Auto(AutoOffsetStrategy.Earliest),
    allowAutoCreateTopics: Boolean = true,
    diagnostics: Diagnostics = Diagnostics.NoOp,
    restartStreamOnRebalancing: Boolean = false,
    properties: Map[String, String] = Map.empty
  ): ZLayer[Kafka, Throwable, Consumer] =
    (ZLayer(
      consumerSettings(
        clientId = clientId,
        groupId = groupId,
        clientInstanceId = clientInstanceId,
        allowAutoCreateTopics = allowAutoCreateTopics,
        offsetRetrieval = offsetRetrieval,
        restartStreamOnRebalancing = restartStreamOnRebalancing,
        properties = properties
      )
    ) ++ ZLayer.succeed(diagnostics)) >>> Consumer.live

  def transactionalConsumer(
    clientId: String,
    groupId: String,
    clientInstanceId: Option[String] = None,
    offsetRetrieval: OffsetRetrieval = OffsetRetrieval.Auto(),
    allowAutoCreateTopics: Boolean = true,
    diagnostics: Diagnostics = Diagnostics.NoOp,
    restartStreamOnRebalancing: Boolean = false,
    properties: Map[String, String] = Map.empty,
    rebalanceListener: RebalanceListener = RebalanceListener.noop
  ): ZLayer[Kafka, Throwable, Consumer] =
    (ZLayer(
      transactionalConsumerSettings(
        groupId = groupId,
        clientId = clientId,
        clientInstanceId = clientInstanceId,
        allowAutoCreateTopics = allowAutoCreateTopics,
        offsetRetrieval = offsetRetrieval,
        restartStreamOnRebalancing = restartStreamOnRebalancing,
        properties = properties
      ).map(_.withRebalanceListener(rebalanceListener))
    ) ++ ZLayer.succeed(diagnostics)) >>> Consumer.live

  def consumeWithStrings[RC](clientId: String, groupId: Option[String] = None, subscription: Subscription)(
    r: ConsumerRecord[String, String] => URIO[Any, Unit]
  ): RIO[Kafka, Unit] =
    consumerSettings(clientId, groupId, None).flatMap { settings =>
      Consumer.consumeWith[Any, Any, String, String](
        settings,
        subscription,
        Deserializer.string,
        Deserializer.string
      )(r)
    }

  def adminSettings: ZIO[Kafka, Nothing, AdminClientSettings] =
    ZIO.serviceWith[Kafka](_.bootstrapServers).map(AdminClientSettings(_))

  def saslAdminSettings(username: String, password: String): ZIO[Kafka.Sasl, Nothing, AdminClientSettings] =
    ZIO
      .serviceWith[Kafka.Sasl](_.value.bootstrapServers)
      .map(
        AdminClientSettings(_).withProperties(
          "sasl.mechanism"    -> "PLAIN",
          "security.protocol" -> "SASL_PLAINTEXT",
          "sasl.jaas.config" -> s"""org.apache.kafka.common.security.plain.PlainLoginModule required username="$username" password="$password";"""
        )
      )

  def sslAdminSettings: ZIO[Kafka, Nothing, AdminClientSettings] =
    ZIO
      .serviceWith[Kafka](_.bootstrapServers)
      .map(bootstrap =>
        AdminClientSettings(bootstrap).withProperties(
          "security.protocol"       -> "SSL",
          "ssl.truststore.location" -> trustStoreFile.getAbsolutePath,
          "ssl.truststore.password" -> "123456",
          "ssl.keystore.location"   -> keyStoreFile.getAbsolutePath,
          "ssl.keystore.password"   -> "123456",
          "ssl.key.password"        -> "123456",
          "ssl.enabled.protocols"   -> "TLSv1.2",
          "ssl.truststore.type"     -> "JKS",
          "ssl.keystore.type"       -> "JKS"
        )
      )

  def withAdmin[T](f: AdminClient => RIO[Kafka, T]): ZIO[Kafka, Throwable, T] =
    for {
      settings <- adminSettings
      fRes     <- withAdminClient(settings)(f)
    } yield fRes

  def withSaslAdmin[T](
    username: String = "admin",
    password: String = "admin-secret"
  )(
    f: AdminClient => RIO[Kafka.Sasl, T]
  ): ZIO[Kafka.Sasl, Throwable, T] =
    for {
      settings <- saslAdminSettings(username, password)
      fRes     <- withAdminClient(settings)(f)
    } yield fRes

  def withSslAdmin[T](
    f: AdminClient => RIO[Kafka, T]
  ): ZIO[Kafka, Throwable, T] =
    for {
      settings <- sslAdminSettings
      fRes     <- withAdminClient(settings)(f)
    } yield fRes

  private def withAdminClient[R, T](settings: AdminClientSettings)(f: AdminClient => RIO[R, T]): ZIO[R, Throwable, T] =
    ZIO.scoped[R] {
      AdminClient.make(settings).flatMap(f)
    }
}
