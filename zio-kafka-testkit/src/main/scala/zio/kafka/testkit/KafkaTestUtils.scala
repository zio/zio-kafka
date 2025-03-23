package zio.kafka.testkit

import org.apache.kafka.clients.consumer.{ ConsumerConfig, ConsumerRecord }
import org.apache.kafka.clients.producer.{ ProducerRecord, RecordMetadata }
import zio._
import zio.kafka.admin._
import zio.kafka.consumer.Consumer.{ AutoOffsetStrategy, ConsumerDiagnostics, OffsetRetrieval }
import zio.kafka.consumer._
import zio.kafka.diagnostics.Diagnostics
import zio.kafka.producer._
import zio.kafka.serde.{ Deserializer, Serde }
import zio.stream.ZStream

import java.io.File
import java.nio.file.{ Files, StandardCopyOption }
import scala.annotation.nowarn

object KafkaTestUtils {

  // -----------------------------------------------------------------------------------------
  //
  // Producer construction
  //
  // -----------------------------------------------------------------------------------------

  /**
   * Makes `ProducerSettings` for use in tests.
   */
  val producerSettings: ZIO[Kafka, Nothing, ProducerSettings] =
    ZIO
      .serviceWith[Kafka](_.bootstrapServers)
      .map(ProducerSettings(_))

  /**
   * Makes a `Producer` for use in tests.
   */
  val makeProducer: ZIO[Scope & Kafka, Throwable, Producer] =
    producerSettings.flatMap(settings => Producer.make(settings))

  /**
   * `Producer` layer for use in tests.
   *
   * ℹ️ Instead of using a layer, consider using [[KafkaTestUtils.makeProducer]] to directly get a producer.
   */
  val producer: ZLayer[Kafka, Throwable, Producer] =
    ZLayer.scoped(makeProducer)

  // -----------------------------------------------------------------------------------------
  //
  // Transactional producer construction
  //
  // -----------------------------------------------------------------------------------------

  /**
   * Makes `TransactionalProducerSettings` for use in tests.
   *
   * Note: to run multiple tests in parallel, each producer needs a different transactional id.
   */
  def transactionalProducerSettings(transactionalId: String): ZIO[Kafka, Nothing, TransactionalProducerSettings] =
    ZIO
      .serviceWith[Kafka](_.bootstrapServers)
      .map(TransactionalProducerSettings(_, transactionalId))

  /**
   * Makes `TransactionalProducerSettings` for use in tests.
   *
   * Note: to run multiple tests in parallel, you need to use different transactional ids via
   * `transactionalProducerSettings(transactionalId)`.
   */
  val transactionalProducerSettings: ZIO[Kafka, Nothing, TransactionalProducerSettings] =
    transactionalProducerSettings("test-transaction")

  /**
   * Makes a `TransactionalProducer` for use in tests.
   *
   * Note: to run multiple tests in parallel, every test needs a different transactional id.
   */
  def makeTransactionalProducer(transactionalId: String): ZIO[Scope & Kafka, Throwable, TransactionalProducer] =
    transactionalProducerSettings(transactionalId).flatMap(TransactionalProducer.make)

  /**
   * `TransactionalProducer` layer for use in tests.
   *
   * Note: to run multiple tests in parallel, you need to use different transactional ids via
   * `transactionalProducer(transactionalId)`.
   *
   * ℹ️ Instead of using a layer, consider using [[KafkaTestUtils.makeTransactionalProducer]] to directly get a
   * producer.
   */
  val transactionalProducer: ZLayer[Kafka, Throwable, TransactionalProducer] =
    transactionalProducer("test-transaction")

  /**
   * `TransactionalProducer` layer for use in tests.
   *
   * ℹ️ Instead of using a layer, consider using [[KafkaTestUtils.makeTransactionalProducer]] to directly get a
   * producer.
   */
  def transactionalProducer(transactionalId: String): ZLayer[Kafka, Throwable, TransactionalProducer] =
    ZLayer.scoped(makeTransactionalProducer(transactionalId))

  // -----------------------------------------------------------------------------------------
  //
  // Producer helpers
  //
  // -----------------------------------------------------------------------------------------

  /**
   * Produce a single message to a topic.
   */
  def produceOne(
    producer: Producer,
    topic: String,
    key: String,
    message: String
  ): ZIO[Any, Throwable, RecordMetadata] =
    producer.produce[Any, String, String](new ProducerRecord(topic, key, message), Serde.string, Serde.string)

  /**
   * Produce a single message to a topic.
   *
   * @deprecated
   *   instead of using layers, use [[KafkaTestUtils.makeProducer]] to directly get a producer OR use
   *   `ZIO.service[Producer]` to get the producer from the environment. Then use the [[KafkaTestUtils.produceOne]]
   *   variant that accepts the `Producer` as a parameter.
   */
  @deprecated("Use method variant that accepts the Producer as parameter", since = "2.11.0")
  def produceOne(
    topic: String,
    key: String,
    message: String
  ): ZIO[Producer, Throwable, RecordMetadata] =
    ZIO.serviceWithZIO[Producer](produceOne(_, topic, key, message))

  /**
   * Produce many messages to the given partition of a topic.
   */
  def produceMany(
    producer: Producer,
    topic: String,
    partition: Int,
    kvs: Iterable[(String, String)]
  ): ZIO[Any, Throwable, Chunk[RecordMetadata]] =
    producer.produceChunk[Any, String, String](
      Chunk.fromIterable(kvs.map { case (k, v) =>
        new ProducerRecord(topic, partition, null, k, v)
      }),
      Serde.string,
      Serde.string
    )

  /**
   * Produce many messages to the given partition of a topic.
   *
   * @deprecated
   *   instead of using layers, use [[KafkaTestUtils.makeProducer]] to directly get a producer OR use
   *   `ZIO.service[Producer]` to get the producer from the environment. Then use the [[KafkaTestUtils.produceMany]]
   *   variant that accepts the `Producer` as a parameter.
   */
  @deprecated("Use method variant that accepts the Producer as parameter", since = "2.11.0")
  def produceMany(
    topic: String,
    partition: Int,
    kvs: Iterable[(String, String)]
  ): ZIO[Producer, Throwable, Chunk[RecordMetadata]] =
    ZIO.serviceWithZIO[Producer](produceMany(_, topic, partition, kvs))

  /**
   * Produce many messages to a topic.
   */
  def produceMany(
    producer: Producer,
    topic: String,
    kvs: Iterable[(String, String)]
  ): ZIO[Any, Throwable, Chunk[RecordMetadata]] =
    producer.produceChunk[Any, String, String](
      Chunk.fromIterable(kvs.map { case (k, v) =>
        new ProducerRecord(topic, k, v)
      }),
      Serde.string,
      Serde.string
    )

  /**
   * Produce many messages to a topic.
   *
   * @deprecated
   *   instead of using layers, use [[KafkaTestUtils.makeProducer]] to directly get a producer OR use
   *   `ZIO.service[Producer]` to get the producer from the environment. Then use the [[KafkaTestUtils.produceMany]]
   *   variant that accepts the `Producer` as a parameter.
   */
  @deprecated("Use method variant that accepts the Producer as parameter", since = "2.11.0")
  def produceMany(
    topic: String,
    kvs: Iterable[(String, String)]
  ): ZIO[Producer, Throwable, Chunk[RecordMetadata]] =
    ZIO.serviceWithZIO[Producer](produceMany(_, topic, kvs))

  /**
   * A stream that produces messages to a topic on a schedule for as long as it is running.
   */
  def scheduledProduce[R](
    producer: Producer,
    topic: String,
    schedule: Schedule[R, Any, Long]
  ): ZStream[R, Throwable, RecordMetadata] =
    ZStream
      .fromSchedule(schedule)
      .mapZIO { i =>
        produceOne(producer, topic, s"key$i", s"msg$i")
      }

  /**
   * A stream that produces messages to a topic on a schedule for as long as it is running.
   *
   * @deprecated
   *   instead of using layers, use [[KafkaTestUtils.makeProducer]] to directly get a producer OR use
   *   `ZIO.service[Producer]` to get the producer from the environment. Then use the
   *   [[KafkaTestUtils.scheduledProduce]] variant that accepts the `Producer` as a parameter.
   */
  @deprecated("Use method variant that accepts the Producer as parameter", since = "2.11.0")
  def scheduledProduce[R](
    topic: String,
    schedule: Schedule[R, Any, Long]
  ): ZStream[R with Producer, Throwable, RecordMetadata] =
    ZStream.serviceWithStream[Producer](scheduledProduce(_, topic, schedule))

  // -----------------------------------------------------------------------------------------
  //
  // Consumer construction
  //
  // -----------------------------------------------------------------------------------------

  /**
   * Makes `ConsumerSettings` for use in tests.
   */
  def consumerSettings(
    clientId: String,
    groupId: Option[String] = None,
    clientInstanceId: Option[String] = None,
    allowAutoCreateTopics: Boolean = true,
    offsetRetrieval: OffsetRetrieval = OffsetRetrieval.Auto(AutoOffsetStrategy.Earliest),
    // @deprecated
    // starting zio-kafka 3.0.0 `restartStreamOnRebalancing` is no longer available. As far as the zio-kafka
    // contributors know, this feature is only used for transactional producing. Zio-kafka 3.0.0 no longer needs it for
    // that.
    restartStreamOnRebalancing: Boolean = false,
    rebalanceSafeCommits: Boolean = false,
    maxRebalanceDuration: Duration = 3.minutes,
    maxPollInterval: Duration = 5.minutes,
    `max.poll.records`: Int = 100, // settings this higher can cause concurrency bugs to go unnoticed
    commitTimeout: Duration = ConsumerSettings.defaultCommitTimeout,
    properties: Map[String, String] = Map.empty
  ): URIO[Kafka, ConsumerSettings] =
    ZIO.serviceWith[Kafka] { (kafka: Kafka) =>
      @nowarn("msg=deprecated")
      val settings = ConsumerSettings(kafka.bootstrapServers)
        .withClientId(clientId)
        .withCloseTimeout(5.seconds)
        .withPollTimeout(100.millis)
        .withMaxPollInterval(maxPollInterval)
        .withMaxPollRecords(`max.poll.records`)
        .withCommitTimeout(commitTimeout)
        .withProperties(
          ConsumerConfig.METADATA_MAX_AGE_CONFIG         -> "100",
          ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG       -> "3000",
          ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG    -> "1000",
          ConsumerConfig.ALLOW_AUTO_CREATE_TOPICS_CONFIG -> allowAutoCreateTopics.toString
        )
        .withOffsetRetrieval(offsetRetrieval)
        .withRestartStreamOnRebalancing(restartStreamOnRebalancing)
        .withRebalanceSafeCommits(rebalanceSafeCommits)
        .withMaxRebalanceDuration(maxRebalanceDuration)
        .withProperties(properties)

      val withClientInstanceId = clientInstanceId.fold(settings)(settings.withGroupInstanceId)
      groupId.fold(withClientInstanceId)(withClientInstanceId.withGroupId)
    }

  /**
   * Makes a `Consumer` for use in tests.
   */
  def makeConsumer(
    clientId: String,
    groupId: Option[String] = None,
    clientInstanceId: Option[String] = None,
    offsetRetrieval: OffsetRetrieval = OffsetRetrieval.Auto(AutoOffsetStrategy.Earliest),
    allowAutoCreateTopics: Boolean = true,
    diagnostics: ConsumerDiagnostics = Diagnostics.NoOp,
    // @deprecated
    // starting zio-kafka 3.0.0 `restartStreamOnRebalancing` is no longer available. As far as the zio-kafka
    // contributors know, this feature is only used for transactional producing. Zio-kafka 3.0.0 no longer needs it for
    // that.
    restartStreamOnRebalancing: Boolean = false,
    rebalanceSafeCommits: Boolean = false,
    maxRebalanceDuration: Duration = 3.minutes,
    commitTimeout: Duration = ConsumerSettings.defaultCommitTimeout,
    properties: Map[String, String] = Map.empty
  ): ZIO[Scope & Kafka, Throwable, Consumer] =
    for {
      settings <- consumerSettings(
                    clientId = clientId,
                    groupId = groupId,
                    clientInstanceId = clientInstanceId,
                    allowAutoCreateTopics = allowAutoCreateTopics,
                    offsetRetrieval = offsetRetrieval,
                    restartStreamOnRebalancing = restartStreamOnRebalancing,
                    rebalanceSafeCommits = rebalanceSafeCommits,
                    maxRebalanceDuration = maxRebalanceDuration,
                    properties = properties,
                    commitTimeout = commitTimeout
                  )
      c <- Consumer.make(settings, diagnostics)
    } yield c

  /**
   * `Consumer` layer for use in tests, requires a `ConsumerSettings` layer.
   *
   * "minimal" because, unlike the other functions returning a `ZLayer[..., ..., Consumer]` of this file, you need to
   * provide the `ConsumerSettings` layer yourself.
   *
   * ℹ️ Instead of using a layer, consider using [[KafkaTestUtils.makeConsumer]] to directly get a consumer.
   */
  def minimalConsumer(
    diagnostics: ConsumerDiagnostics = Diagnostics.NoOp
  ): ZLayer[ConsumerSettings, Throwable, Consumer] =
    ZLayer.makeSome[ConsumerSettings, Consumer](
      ZLayer.succeed(diagnostics) >>> Consumer.live
    )

  @deprecated("Use [[KafkaTestUtils.minimalConsumer]] instead", "2.3.1")
  def simpleConsumer(
    diagnostics: ConsumerDiagnostics = Diagnostics.NoOp
  ): ZLayer[ConsumerSettings, Throwable, Consumer] =
    minimalConsumer(diagnostics)

  /**
   * `Consumer` layer for use in tests.
   *
   * ℹ️ Instead of using a layer, consider using [[makeConsumer]] to directly get a consumer.
   */
  def consumer(
    clientId: String,
    groupId: Option[String] = None,
    clientInstanceId: Option[String] = None,
    offsetRetrieval: OffsetRetrieval = OffsetRetrieval.Auto(AutoOffsetStrategy.Earliest),
    allowAutoCreateTopics: Boolean = true,
    diagnostics: ConsumerDiagnostics = Diagnostics.NoOp,
    // @deprecated
    // starting zio-kafka 3.0.0 `restartStreamOnRebalancing` is no longer available. As far as the zio-kafka
    // contributors know, this feature is only used for transactional producing. Zio-kafka 3.0.0 no longer needs it for
    // that.
    restartStreamOnRebalancing: Boolean = false,
    rebalanceSafeCommits: Boolean = false,
    maxRebalanceDuration: Duration = 3.minutes,
    commitTimeout: Duration = ConsumerSettings.defaultCommitTimeout,
    properties: Map[String, String] = Map.empty
  ): ZLayer[Kafka, Throwable, Consumer] =
    ZLayer.scoped {
      makeConsumer(
        clientId,
        groupId,
        clientInstanceId,
        offsetRetrieval,
        allowAutoCreateTopics,
        diagnostics,
        restartStreamOnRebalancing,
        rebalanceSafeCommits,
        maxRebalanceDuration,
        commitTimeout,
        properties
      )
    }

  // -----------------------------------------------------------------------------------------
  //
  // Transactional consumer construction
  //
  // -----------------------------------------------------------------------------------------

  /**
   * Makes `ConsumerSettings` for a transactional consumer, use in tests.
   */
  def transactionalConsumerSettings(
    groupId: String,
    clientId: String,
    clientInstanceId: Option[String] = None,
    allowAutoCreateTopics: Boolean = true,
    offsetRetrieval: OffsetRetrieval = OffsetRetrieval.Auto(AutoOffsetStrategy.Earliest),
    // @deprecated
    // starting zio-kafka 3.0.0 `restartStreamOnRebalancing` is no longer available. As far as the zio-kafka
    // contributors know, this feature is only used for transactional producing. Zio-kafka 3.0.0 no longer needs it for
    // that.
    restartStreamOnRebalancing: Boolean = false,
    rebalanceSafeCommits: Boolean = false,
    properties: Map[String, String] = Map.empty
  ): URIO[Kafka, ConsumerSettings] =
    consumerSettings(
      clientId = clientId,
      groupId = Some(groupId),
      clientInstanceId = clientInstanceId,
      allowAutoCreateTopics = allowAutoCreateTopics,
      offsetRetrieval = offsetRetrieval,
      restartStreamOnRebalancing = restartStreamOnRebalancing,
      rebalanceSafeCommits = rebalanceSafeCommits,
      properties = properties
    )
      .map(_.withProperties(ConsumerConfig.ISOLATION_LEVEL_CONFIG -> "read_committed"))

  /**
   * Makes a transactional `Consumer` for use in tests.
   */
  def makeTransactionalConsumer(
    clientId: String,
    groupId: String,
    clientInstanceId: Option[String] = None,
    offsetRetrieval: OffsetRetrieval = OffsetRetrieval.Auto(AutoOffsetStrategy.Earliest),
    allowAutoCreateTopics: Boolean = true,
    diagnostics: ConsumerDiagnostics = Diagnostics.NoOp,
    // @deprecated
    // starting zio-kafka 3.0.0 `restartStreamOnRebalancing` is no longer available. As far as the zio-kafka
    // contributors know, this feature is only used for transactional producing. Zio-kafka 3.0.0 no longer needs it for
    // that.
    restartStreamOnRebalancing: Boolean = false,
    rebalanceSafeCommits: Boolean = false,
    properties: Map[String, String] = Map.empty,
    rebalanceListener: RebalanceListener = RebalanceListener.noop
  ): ZIO[Scope & Kafka, Throwable, Consumer] =
    for {
      settings <- transactionalConsumerSettings(
                    groupId = groupId,
                    clientId = clientId,
                    clientInstanceId = clientInstanceId,
                    allowAutoCreateTopics = allowAutoCreateTopics,
                    offsetRetrieval = offsetRetrieval,
                    restartStreamOnRebalancing = restartStreamOnRebalancing,
                    rebalanceSafeCommits = rebalanceSafeCommits,
                    properties = properties
                  ).map(_.withRebalanceListener(rebalanceListener))
      consumer <- Consumer.make(settings, diagnostics)
    } yield consumer

  /**
   * A transactional `Consumer` layer for use in tests.
   *
   * ℹ️ Instead of using a layer, consider using [[KafkaTestUtils.makeTransactionalConsumer]] to directly get a
   * consumer.
   */
  def transactionalConsumer(
    clientId: String,
    groupId: String,
    clientInstanceId: Option[String] = None,
    offsetRetrieval: OffsetRetrieval = OffsetRetrieval.Auto(AutoOffsetStrategy.Earliest),
    allowAutoCreateTopics: Boolean = true,
    diagnostics: ConsumerDiagnostics = Diagnostics.NoOp,
    // @deprecated
    // starting zio-kafka 3.0.0 `restartStreamOnRebalancing` is no longer available. As far as the zio-kafka
    // contributors know, this feature is only used for transactional producing. Zio-kafka 3.0.0 no longer needs it for
    // that.
    restartStreamOnRebalancing: Boolean = false,
    rebalanceSafeCommits: Boolean = false,
    properties: Map[String, String] = Map.empty,
    rebalanceListener: RebalanceListener = RebalanceListener.noop
  ): ZLayer[Kafka, Throwable, Consumer] =
    ZLayer.scoped {
      makeTransactionalConsumer(
        clientId,
        groupId,
        clientInstanceId,
        offsetRetrieval,
        allowAutoCreateTopics,
        diagnostics,
        restartStreamOnRebalancing,
        rebalanceSafeCommits,
        properties,
        rebalanceListener
      )
    }

  // -----------------------------------------------------------------------------------------
  //
  // Consumer helpers
  //
  // -----------------------------------------------------------------------------------------

  /**
   * Consumes a stream of `ConsumerRecord`s for topics that have `String` keys and `String` values.
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

  // -----------------------------------------------------------------------------------------
  //
  // Admin client construction
  //
  // -----------------------------------------------------------------------------------------

  /**
   * Makes `AdminClientSettings` for use in tests.
   */
  val adminSettings: ZIO[Kafka, Nothing, AdminClientSettings] =
    ZIO.serviceWith[Kafka](_.bootstrapServers).map(bootstrapServers => AdminClientSettings(bootstrapServers))

  /**
   * Makes `AdminClientSettings` for use in tests, using SASL_PLAINTEXT security protocol.
   */
  def saslAdminSettings(username: String, password: String): ZIO[Kafka.Sasl, Nothing, AdminClientSettings] =
    ZIO
      .serviceWith[Kafka.Sasl](_.value.bootstrapServers)
      .map { bootstrapServers =>
        AdminClientSettings(bootstrapServers).withProperties(
          "sasl.mechanism"    -> "PLAIN",
          "security.protocol" -> "SASL_PLAINTEXT",
          "sasl.jaas.config" -> s"""org.apache.kafka.common.security.plain.PlainLoginModule required username="$username" password="$password";"""
        )
      }

  /**
   * Makes `AdminClientSettings` for use in tests, using SSL security protocol.
   */
  val sslAdminSettings: ZIO[Kafka, Nothing, AdminClientSettings] =
    ZIO
      .serviceWith[Kafka](_.bootstrapServers)
      .map { bootstrapServers =>
        AdminClientSettings(bootstrapServers).withProperties(
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
      }

  /**
   * Makes a `AdminClient` for use in tests.
   */
  def makeAdminClient: ZIO[Scope & Kafka, Throwable, AdminClient] =
    adminSettings.flatMap(AdminClient.make)

  /**
   * Makes a `AdminClient` for use in tests, using the `SASL_PLAINTEXT` security protocol.
   */
  def makeSaslAdminClient(
    username: String = "admin",
    password: String = "admin-secret"
  ): ZIO[Scope & Kafka.Sasl, Throwable, AdminClient] =
    saslAdminSettings(username, password).flatMap(AdminClient.make)

  /**
   * Makes a `AdminClient` for use in tests, using the `SSL` security protocol.
   */
  def makeSslAdminClient: ZIO[Scope & Kafka, Throwable, AdminClient] =
    sslAdminSettings.flatMap(AdminClient.make)

  /**
   * Run `f` with an `AdminClient`.
   *
   * @deprecated
   *   use [[KafkaTestUtils.makeAdminClient]] directly, for example:
   *   {{{
   *   for {
   *     adminClient <- KafkaTestUtils.makeAdminClient
   *     _           <- adminClient.createTopic(topic1, 1, 1)
   *   } yield ()
   *   }}}
   */
  @deprecated("Use KafkaTestUtils.makeAdminClient instead", since = "2.11.0")
  def withAdmin[A](f: AdminClient => UIO[A]): ZIO[Scope & Kafka, Throwable, A] =
    makeAdminClient.flatMap(f)

  /**
   * Run `f` with an `AdminClient`, using the SASL_PLAINTEXT security protocol.
   *
   * @deprecated
   *   use [[KafkaTestUtils.makeSaslAdminClient]] directly, for example:
   *   {{{
   *   for {
   *     adminClient <- KafkaTestUtils.makeSaslAdminClient
   *     _           <- adminClient.createTopic(topic1, 1, 1)
   *   } yield ()
   *   }}}
   */
  @deprecated("Use KafkaTestUtils.makeSaslAdminClient instead", since = "2.11.0")
  def withSaslAdmin[A](
    username: String = "admin",
    password: String = "admin-secret"
  )(f: AdminClient => UIO[A]): ZIO[Scope & Kafka.Sasl, Throwable, A] =
    makeSaslAdminClient(username, password).flatMap(f)

  /**
   * Run `f` with an `AdminClient`, using the `SSL` security protocol.
   *
   * @deprecated
   *   use [[KafkaTestUtils.makeSslAdminClient]] directly, for example:
   *   {{{
   *   for {
   *     adminClient <- KafkaTestUtils.makeSslAdminClient
   *     _           <- adminClient.createTopic(topic1, 1, 1)
   *   } yield ()
   *   }}}
   */
  @deprecated("Use KafkaTestUtils.makeSslAdminClient instead", since = "2.11.0")
  def withSslAdmin[A](f: AdminClient => UIO[A]): ZIO[Scope & Kafka, Throwable, A] =
    makeSslAdminClient.flatMap(f)

  // -----------------------------------------------------------------------------------------
  //
  // Admin client helpers
  //
  // -----------------------------------------------------------------------------------------

  /**
   * Create a topic.
   */
  def createCustomTopic(topic: String, partitionCount: Int = 1): ZIO[Kafka, Throwable, Unit] =
    ZIO.scoped {
      for {
        adminClient <- makeAdminClient
        _           <- adminClient.createTopic(AdminClient.NewTopic(topic, partitionCount, replicationFactor = 1))
      } yield ()
    }

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
