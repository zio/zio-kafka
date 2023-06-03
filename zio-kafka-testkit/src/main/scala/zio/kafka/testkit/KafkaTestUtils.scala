package zio.kafka.testkit

import org.apache.kafka.clients.consumer.{ ConsumerConfig, ConsumerRecord }
import org.apache.kafka.clients.producer.{ ProducerRecord, RecordMetadata }
import zio._
import zio.kafka.admin.AdminClient.NewTopic
import zio.kafka.admin._
import zio.kafka.consumer.Consumer.{ AutoOffsetStrategy, OffsetRetrieval }
import zio.kafka.consumer._
import zio.kafka.consumer.diagnostics.Diagnostics
import zio.kafka.producer._
import zio.kafka.serde.{ Deserializer, Serde }

import java.io.File
import java.nio.file.{ Files, StandardCopyOption }

object KafkaTestUtils {

  /**
   * Default Producer settings you can use in your tests
   */
  val producerSettings: ZIO[Kafka, Nothing, ProducerSettings] =
    ZIO
      .serviceWith[Kafka](_.bootstrapServers)
      .map(ProducerSettings(_))

  /**
   * Producer instance you can use in your tests. It uses the default Producer settings.
   */
  val producer: ZLayer[Kafka, Throwable, Producer] =
    ZLayer.makeSome[Kafka, Producer](
      ZLayer(producerSettings),
      Producer.live
    )

  /**
   * Default transactional Producer settings you can use in your tests.
   */
  val transactionalProducerSettings: ZIO[Kafka, Nothing, TransactionalProducerSettings] =
    ZIO
      .serviceWith[Kafka](_.bootstrapServers)
      .map(TransactionalProducerSettings(_, "test-transaction"))

  /**
   * Transactional Producer instance you can use in your tests. It uses the default transactional Producer settings.
   */
  val transactionalProducer: ZLayer[Kafka, Throwable, TransactionalProducer] =
    ZLayer.makeSome[Kafka, TransactionalProducer](
      ZLayer(transactionalProducerSettings),
      TransactionalProducer.live
    )

  /**
   * Utility function to produce a single message in a Topic.
   */
  def produceOne(
    topic: String,
    key: String,
    message: String
  ): ZIO[Producer, Throwable, RecordMetadata] =
    Producer.produce[Any, String, String](new ProducerRecord(topic, key, message), Serde.string, Serde.string)

  /**
   * Utility function to produce many messages in give Partition of a Topic.
   */
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

  /**
   * Utility function to produce many messages in a Topic.
   */
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

  /**
   * Utility function to make a Consumer settings set.
   */
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

  /**
   * Utility function to make a transactional Consumer settings set.
   */
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

  /**
   * Utility function to make a Consumer. It requires a ConsumerSettings layer.
   */
  @deprecated("Use [[KafkaTestUtils.minimalConsumer]] instead", "2.3.1")
  def simpleConsumer(diagnostics: Diagnostics = Diagnostics.NoOp): ZLayer[ConsumerSettings, Throwable, Consumer] =
    ZLayer.makeSome[ConsumerSettings, Consumer](
      ZLayer.succeed(diagnostics) >>> Consumer.live
    )

  /**
   * Utility function to make a Consumer. It requires a ConsumerSettings layer.
   *
   * "minimal" because, unlike the other functions returning a `ZLayer[..., ..., Consumer]` of this file, you need to
   * provide the `ConsumerSettings` layer yourself.
   */
  def minimalConsumer(diagnostics: Diagnostics = Diagnostics.NoOp): ZLayer[ConsumerSettings, Throwable, Consumer] =
    ZLayer.makeSome[ConsumerSettings, Consumer](
      ZLayer.succeed(diagnostics) >>> Consumer.live
    )

  /**
   * Utility function to make a Consumer.
   */
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

  /**
   * Utility function to make a transactional Consumer.
   */
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

  /**
   * Utility function consume a stream of ConsumerRecords.
   *
   * For each consumed record, the provided `r` function will be called.
   */
  def consumeWithStrings(clientId: String, groupId: Option[String] = None, subscription: Subscription)(
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

  /**
   * Default AdminClient settings.
   */
  val adminSettings: ZIO[Kafka, Nothing, AdminClientSettings] =
    ZIO.serviceWith[Kafka](_.bootstrapServers).map(AdminClientSettings(_))

  /**
   * Utility function to make an AdminClient settings set using SASL_PLAINTEXT security protocol.
   */
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

  /**
   * Default AdminClient settings using SSL security protocol.
   */
  val sslAdminSettings: ZIO[Kafka, Nothing, AdminClientSettings] =
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

  /**
   * Utility function to execute something with an `AdminClient` instance configure with the default settings.
   */
  def withAdmin[T](f: AdminClient => RIO[Kafka, T]): ZIO[Kafka, Throwable, T] =
    for {
      settings <- adminSettings
      fRes     <- withAdminClient(settings)(f)
    } yield fRes

  /**
   * Utility function to execute something with an `AdminClient` instance configured to use the SASL_PLAINTEXT security
   * protocol.
   */
  def withSaslAdmin[T](
    username: String = "admin",
    password: String = "admin-secret"
  )(f: AdminClient => RIO[Kafka.Sasl, T]): ZIO[Kafka.Sasl, Throwable, T] =
    for {
      settings <- saslAdminSettings(username, password)
      fRes     <- withAdminClient(settings)(f)
    } yield fRes

  /**
   * Utility function to execute something with an `AdminClient` instance configured to use the SSL security protocol.
   */
  def withSslAdmin[T](f: AdminClient => RIO[Kafka, T]): ZIO[Kafka, Throwable, T] =
    for {
      settings <- sslAdminSettings
      fRes     <- withAdminClient(settings)(f)
    } yield fRes

  private def withAdminClient[R, T](settings: AdminClientSettings)(f: AdminClient => RIO[R, T]): ZIO[R, Throwable, T] =
    ZIO.scoped[R] {
      AdminClient.make(settings).flatMap(f)
    }

  /**
   * To be used together with [[withAdmin]], [[withSaslAdmin]], [[withSslAdmin]] or [[withAdminClient()]]. Useful for
   * when you set "auto.create.topics.enable" -> "false" in your Kafka brokers.
   */
  def createTopics(
    adminClient: AdminClient,
    topics: Iterable[String],
    partitions: Int = 1,
    replicationFactor: Short = 1,
    configs: Map[String, String] = Map.empty
  ) =
    adminClient.createTopics(topics.map(NewTopic(_, partitions, replicationFactor, configs)))

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

  private[zio] lazy val trustStoreFile: File = readResourceFile("truststore/kafka.truststore.jks", "truststore", ".jks")
  private[zio] lazy val keyStoreFile: File   = readResourceFile("keystore/kafka.keystore.jks", "keystore", ".jks")

}
