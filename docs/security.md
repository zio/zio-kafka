---
id: security
title: "Security: TLS and Credentials"
sidebar_label: "Security"
---

Zio-kafka clients connect to Kafka through the Java clients, so encryption (TLS) and authentication (mutual TLS or SASL) are configured with the usual Java client properties. Zio-kafka adds two things on top: `KafkaCredentialStore`, which turns PEM-encoded certificates into the TLS properties, and an SSL endpoint check that catches a client configured for plain text that connects to a TLS listener, a mistake that otherwise crashes the application with an `OutOfMemoryError`.

## Security Protocols

The Java property `security.protocol` (constant `CommonClientConfigs.SECURITY_PROTOCOL_CONFIG`) selects how a client talks to the brokers. It must match the broker listener the bootstrap servers point to:

| `security.protocol`   | Encryption | Authentication                                                            |
| --------------------- | ---------- | ------------------------------------------------------------------------- |
| `PLAINTEXT` (default) | None       | None                                                                      |
| `SSL`                 | TLS        | Optional: a client certificate (mutual TLS)                               |
| `SASL_PLAINTEXT`      | None       | SASL (`PLAIN`, `SCRAM-SHA-256`, `SCRAM-SHA-512`, `OAUTHBEARER`, `GSSAPI`) |
| `SASL_SSL`            | TLS        | SASL, as above                                                            |

Every zio-kafka settings type accepts arbitrary Java client properties, so any security setup the Java clients support also works in zio-kafka:

| Settings type                   | Generic properties                                                       | TLS credentials                          |
| ------------------------------- | ------------------------------------------------------------------------ | ---------------------------------------- |
| `ConsumerSettings`              | `ConsumerSettings#withProperty`, `ConsumerSettings#withProperties`       | `ConsumerSettings#withCredentials`       |
| `ProducerSettings`              | `ProducerSettings#withProperty`, `ProducerSettings#withProperties`       | `ProducerSettings#withCredentials`       |
| `AdminClientSettings`           | `AdminClientSettings#withProperty`, `AdminClientSettings#withProperties` | `AdminClientSettings#withCredentials`    |
| `TransactionalProducerSettings` | Configure the wrapped `ProducerSettings`                                 | Configure the wrapped `ProducerSettings` |

`TransactionalProducerSettings` has no `with` methods of its own: configure security on a `ProducerSettings` and pass it to `TransactionalProducerSettings.apply(producerSettings, transactionalId)`. In all settings types, a property set later replaces a property with the same key set earlier.

TLS is configured with the `ssl.*` properties (constants in `SslConfigs`), either from PEM strings, for which zio-kafka has `KafkaCredentialStore`, or from key store and trust store files. SASL is configured with `sasl.mechanism` (constant `SaslConfigs.SASL_MECHANISM`) and `sasl.jaas.config` (constant `SaslConfigs.SASL_JAAS_CONFIG`), which names the login module and holds the credentials. [Connect to a Secured Cluster](connecting-to-a-secured-cluster.md) shows complete settings for mutual TLS, one-way TLS and SASL/SCRAM.

## KafkaCredentialStore

`KafkaCredentialStore` holds a set of TLS properties. Its only constructor, `KafkaCredentialStore.fromPemStrings`, takes PEM-encoded strings, which is convenient when the certificates come from environment variables or a secret manager rather than from key store files:

```scala
sealed trait KafkaCredentialStore {
  def properties: Map[String, String]
}

object KafkaCredentialStore {
  def fromPemStrings(caCertificate: String, clientPrivateKey: String, clientCertificate: String): KafkaCredentialStore
}
```

`KafkaCredentialStore.fromPemStrings` sets the following properties. It replaces every newline in the PEM strings with a space, so the strings can be passed as single-line property values:

| Property                         | Value                                                    |
| -------------------------------- | -------------------------------------------------------- |
| `security.protocol`              | `SSL`                                                    |
| `ssl.truststore.type`            | `PEM`                                                    |
| `ssl.truststore.certificates`    | `caCertificate`: the CA certificate(s) the client trusts |
| `ssl.keystore.type`              | `PEM`                                                    |
| `ssl.keystore.key`               | `clientPrivateKey`: the client's private key             |
| `ssl.keystore.certificate.chain` | `clientCertificate`: the client's certificate chain      |

The result is a mutual TLS setup: the client verifies the brokers with the CA certificate, and the brokers authenticate the client by its certificate. The Java client only accepts a private key in PKCS#8 format; if the key is encrypted, also set its password with the `ssl.key.password` property. Because `KafkaCredentialStore.fromPemStrings` always includes a client key and certificate, set the `ssl.truststore.*` properties yourself for one-way TLS, where only the client verifies the brokers.

`ConsumerSettings#withCredentials`, `ProducerSettings#withCredentials` and `AdminClientSettings#withCredentials` add `KafkaCredentialStore#properties` to the settings:

```scala
final case class ConsumerSettings(...) {
  def withCredentials(credentialsStore: KafkaCredentialStore): ConsumerSettings
}

final case class ProducerSettings(...) {
  def withCredentials(credentialsStore: KafkaCredentialStore): ProducerSettings
}

final case class AdminClientSettings(...) {
  def withCredentials(credentialsStore: KafkaCredentialStore): AdminClientSettings
}
```

To add PEM credentials to producer settings:

```scala mdoc:compile-only
import zio.kafka.producer.ProducerSettings
import zio.kafka.security.KafkaCredentialStore

def securedSettings(caPem: String, keyPem: String, certPem: String): ProducerSettings =
  ProducerSettings(List("kafka.example.com:9093"))
    .withCredentials(KafkaCredentialStore.fromPemStrings(caPem, keyPem, certPem))
```

## SSL Endpoint Check

A long-standing Kafka client bug ([KAFKA-4090](https://issues.apache.org/jira/browse/KAFKA-4090)) makes a client that uses a plain-text protocol (`PLAINTEXT` or `SASL_PLAINTEXT`) against a TLS listener read the broker's TLS handshake bytes as the size of a response. It then tries to allocate a buffer of that size, and the application dies with an `OutOfMemoryError`. Zio-kafka protects against this with `zio.kafka.utils.SslHelper.validateEndpoint`, which probes the brokers before the Java client is created and turns the mismatch into an ordinary error.

### When the Check Runs

The check runs while a client is created, before the Java client exists, in these constructors:

- `Consumer.make` and `Consumer.live`.
- `Producer.make` and `Producer.live`.
- `TransactionalProducer.make` and `TransactionalProducer.live`, using the properties of the wrapped `ProducerSettings`.
- `AdminClient.make`, `AdminClient.live` and `AdminClient.javaClientFromSettings`.

It is skipped when the `security.protocol` property is a `String` that contains `SSL`, ignoring case, that is for `SSL` and `SASL_SSL`: such a client speaks TLS and is not affected by the bug. A value of another type, for example the Java `SecurityProtocol.SSL` enum, does not skip the check, so set the protocol as a string. It does not run in `Producer.fromJavaProducer`, `Consumer.fromJavaConsumerWithPermit`, `AdminClient.fromJavaClient` and `AdminClient.fromScopedJavaClient`, because these wrap a Java client that you create yourself. There is no setting to turn the check off.

### What the Check Does

For a client with a plain-text protocol, the check goes through these steps:

1. It reads `bootstrap.servers`, and fails with a `KafkaException` caused by `IllegalArgumentException("Empty bootstrapServers list")` when the property is missing. This step runs for every protocol, before the `SSL` skip described above. An empty value, such as the one `AdminClientSettings(Nil)` produces, passes this step and fails at the address lookup in step 2 instead.
2. It resolves every bootstrap server to all of its IP addresses and opens a plain socket to each address, in parallel, on a blocking thread.
3. On each socket it sends a Kafka `ApiVersions` request, reads the first 5 bytes of the answer, and closes the socket.
4. If the first byte is a TLS record type, the broker answered with TLS, and the check fails with a `KafkaException` caused by `IllegalArgumentException("Received an unexpected SSL packet from the server. Please ensure the client is properly configured with SSL enabled")`.

Each probe times out after the client's `request.timeout.ms` (30 seconds when it is not set or not a positive number). The overall outcome is:

| Situation                                                   | Outcome                                                                                                |
| ----------------------------------------------------------- | ------------------------------------------------------------------------------------------------------ |
| At least one address answers without TLS, none with TLS     | Succeeds; unreachable addresses are ignored                                                            |
| Any address answers with TLS                                | Fails with the "unexpected SSL packet" error                                                           |
| Any non-timeout error after a connection is open            | Fails with that error                                                                                  |
| No address can be reached (connection refused or timed out) | Fails with the first connection error, for example a `TimeoutException("Failed to contact <address>")` |

Every failure is wrapped in an `org.apache.kafka.common.KafkaException` with the message `"Failed to connect to Kafka"`, for every kind of client; the actual reason is the exception's cause. Because the check needs at least one reachable broker, `Consumer.make`, `Producer.make`, `TransactionalProducer.make` and `AdminClient.make` with a plain-text protocol fail when none of the bootstrap servers is reachable at startup.

[Connect to a Secured Cluster](connecting-to-a-secured-cluster.md#verify-the-connection) shows how to log the cause when a client cannot be created.

### Checking a Java Client You Create Yourself

`SslHelper.validateEndpoint` is public, so you can run the same check before you create a Java client for `Producer.fromJavaProducer`, `Consumer.fromJavaConsumerWithPermit` or `AdminClient.fromJavaClient`:

```scala
object SslHelper {
  def validateEndpoint(props: Map[String, AnyRef]): IO[KafkaException, Unit]
}
```

Pass the same properties you give the Java client. To check the properties before creating a Java producer from them:

```scala mdoc:compile-only
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.common.serialization.ByteArraySerializer
import zio._
import zio.kafka.utils.SslHelper
import scala.jdk.CollectionConverters._

def checkedJavaProducer(props: Map[String, AnyRef]): ZIO[Scope, Throwable, KafkaProducer[Array[Byte], Array[Byte]]] =
  SslHelper.validateEndpoint(props) *>
    ZIO.acquireRelease(
      ZIO.attemptBlocking(new KafkaProducer[Array[Byte], Array[Byte]](props.asJava, new ByteArraySerializer(), new ByteArraySerializer()))
    )(p => ZIO.attemptBlocking(p.close()).orDie)
```

## See Also

- [Admin Client](admin-client.md) for managing ACLs with `AdminClient#createAcls` and related operations.
- [Connect to a Secured Cluster](connecting-to-a-secured-cluster.md) for complete TLS and SASL settings and how to verify a connection.
- [Consumer Settings](consumer-settings.md#authorization-errors) for how a consumer retries authorization errors, and [Producer](producer.md#error-handling) for the producer's retry schedule.
- [Detect Silent Authorization Failures](detecting-authorization-failures.md) for making a consumer notice that its read access was revoked.
- [Test Against a Secured Broker](writing-tests.md#test-against-a-secured-broker) for the testkit's embedded brokers with SASL and TLS listeners.
- The Kafka documentation on [security](https://kafka.apache.org/documentation/#security) for broker-side setup and every client property.
