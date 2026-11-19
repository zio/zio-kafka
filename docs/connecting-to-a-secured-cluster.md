---
id: connecting-to-a-secured-cluster
title: "Connect to a Secured Cluster"
---

In this guide, we connect zio-kafka consumers, producers and admin clients to a Kafka cluster that requires TLS, SASL or both. By the end, you will have settings for the security setup your cluster uses and a startup check that proves the connection works, or logs why it does not.

We start with the settings for each common setup, mutual TLS from PEM strings, one-way TLS with a private CA, and SASL/SCRAM, and finish by verifying the connection.

## The Problem

A client whose settings do not match the broker's listener cannot talk to the cluster. A client that uses `PLAINTEXT` against a TLS listener would read the TLS handshake as a huge response size and crash with an `OutOfMemoryError`; zio-kafka's [SSL endpoint check](security.md#ssl-endpoint-check) turns that into a `KafkaException` with the message `"Failed to connect to Kafka"`. A client with the right protocol but missing or wrong credentials fails with an `SslAuthenticationException` or a `SaslAuthenticationException` as soon as it talks to a broker. In both cases, nothing gets consumed or produced until the settings carry the right properties.

## Prerequisites

This guide assumes a project that already depends on zio-kafka (see [Getting Started](getting-started.md)), and that you know from your cluster's operators or your managed Kafka service:

- The bootstrap servers of the listener you connect to, and its security protocol: `SSL`, `SASL_SSL` or `SASL_PLAINTEXT` (see [Security Protocols](security.md#security-protocols)).
- For TLS, the CA certificate that signed the broker certificates, unless a public CA signed them, and for mutual TLS also a client certificate and private key in PEM format.
- For SASL, the mechanism, such as `SCRAM-SHA-512` or `PLAIN`, and a user name and password.

## Connect With Mutual TLS From PEM Strings

With mutual TLS, the client verifies the brokers with a CA certificate, and the brokers authenticate the client by its certificate. `KafkaCredentialStore.fromPemStrings` turns the three PEM strings into the TLS properties, and `ConsumerSettings#withCredentials`, `ProducerSettings#withCredentials` and `AdminClientSettings#withCredentials` add them. To read the PEM strings from environment variables and use the same credentials for a consumer, a producer and an admin client:

```scala mdoc:compile-only
import zio._
import zio.kafka.admin.AdminClientSettings
import zio.kafka.consumer.ConsumerSettings
import zio.kafka.producer.ProducerSettings
import zio.kafka.security.KafkaCredentialStore

val bootstrapServers = List("kafka-1.example.com:9093", "kafka-2.example.com:9093")

def requiredEnv(name: String): Task[String] =
  System.env(name).someOrFail(new IllegalStateException(s"Missing environment variable $name"))

val credentials: Task[KafkaCredentialStore] =
  for {
    ca   <- requiredEnv("KAFKA_CA_PEM")
    key  <- requiredEnv("KAFKA_CLIENT_KEY_PEM")
    cert <- requiredEnv("KAFKA_CLIENT_CERT_PEM")
  } yield KafkaCredentialStore.fromPemStrings(caCertificate = ca, clientPrivateKey = key, clientCertificate = cert)

val settings: Task[(ConsumerSettings, ProducerSettings, AdminClientSettings)] =
  credentials.map { creds =>
    (
      ConsumerSettings(bootstrapServers).withGroupId("billing").withCredentials(creds),
      ProducerSettings(bootstrapServers).withCredentials(creds),
      AdminClientSettings(bootstrapServers).withCredentials(creds)
    )
  }
```

The private key must be in PKCS#8 format. If it is encrypted, also set its password with the `ssl.key.password` property. For a transactional producer, add the credentials to the `ProducerSettings` you pass to `TransactionalProducerSettings`.

## Connect With One-Way TLS

With one-way TLS, only the client verifies the brokers, and the brokers do not ask for a client certificate. `KafkaCredentialStore.fromPemStrings` always includes a client certificate, so set the trust store properties yourself. To trust a private CA from a PEM string:

```scala mdoc:compile-only
import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.common.config.SslConfigs
import zio.kafka.producer.ProducerSettings

val caPem: String = "-----BEGIN CERTIFICATE-----\n...\n-----END CERTIFICATE-----"

val settings: ProducerSettings =
  ProducerSettings(List("kafka.example.com:9093")).withProperties(
    CommonClientConfigs.SECURITY_PROTOCOL_CONFIG    -> "SSL",
    SslConfigs.SSL_TRUSTSTORE_TYPE_CONFIG           -> "PEM",
    SslConfigs.SSL_TRUSTSTORE_CERTIFICATES_CONFIG   -> caPem.replace('\n', ' ')
  )
```

When the brokers use certificates from a public CA that the JVM already trusts, setting `security.protocol` to `SSL` is enough. Key store and trust store files (JKS or PKCS12) work too, through `ssl.truststore.location`, `ssl.truststore.password`, `ssl.keystore.location` and the related properties.

## Connect With SASL/SCRAM

SASL authenticates the client with a user name and password instead of a certificate. It takes three properties: `security.protocol` set to `SASL_SSL` (or `SASL_PLAINTEXT` on an unencrypted listener), `sasl.mechanism`, and `sasl.jaas.config` with the login module and the credentials. To connect a consumer with SCRAM over TLS:

```scala mdoc:compile-only
import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.common.config.SaslConfigs
import zio.kafka.consumer.ConsumerSettings

val username = "billing-service"
val password = "change-me" // load from a secret store in real code

val settings: ConsumerSettings =
  ConsumerSettings(List("kafka.example.com:9094"))
    .withGroupId("billing")
    .withProperties(
      CommonClientConfigs.SECURITY_PROTOCOL_CONFIG -> "SASL_SSL",
      SaslConfigs.SASL_MECHANISM                   -> "SCRAM-SHA-512",
      SaslConfigs.SASL_JAAS_CONFIG ->
        s"""org.apache.kafka.common.security.scram.ScramLoginModule required username="$username" password="$password";"""
    )
```

The `PLAIN` mechanism works the same way, with `sasl.mechanism` set to `PLAIN` and the login module `org.apache.kafka.common.security.plain.PlainLoginModule`. Managed Kafka services usually document the exact `sasl.mechanism` and `sasl.jaas.config` values to use; copy them into the settings as shown. To combine SASL with a private CA, add the `ssl.truststore.*` properties from [Connect With One-Way TLS](#connect-with-one-way-tls).

## Verify the Connection

To find out at startup whether the settings work, create an admin client with the same properties and list the topics. A successful call proves that the client reached a broker, completed the TLS handshake and authenticated. When the endpoint check fails, zio-kafka wraps the reason in a `KafkaException` with the message `"Failed to connect to Kafka"`; other failures, such as an authentication error from `listTopics`, arrive without that wrapper. So we log the exception's cause when it has one, and the exception itself otherwise:

```scala mdoc:compile-only
import zio._
import zio.kafka.admin.{ AdminClient, AdminClientSettings }

def verifyConnection(settings: AdminClientSettings): Task[Unit] =
  ZIO.scoped {
    for {
      admin  <- AdminClient.make(settings)
      topics <- admin.listTopics()
      _      <- ZIO.logInfo(s"Connected, ${topics.size} topics visible")
    } yield ()
  }.tapError { e =>
    ZIO.logError(s"Cannot connect to Kafka: ${Option(e.getCause).getOrElse(e).getMessage}")
  }
```

`listTopics` only returns the topics your principal may describe, so a small count is not an error. The same `tapError` works around `Producer.make` and `Consumer.make`. When the check passes but consuming or producing later fails with an `AuthorizationException`, the connection works and the principal is missing an ACL; [Admin Client](admin-client.md#access-control-lists) shows how to grant one.

## Going Further

- [Security](security.md) is the reference for the security protocols, `KafkaCredentialStore` and the SSL endpoint check.
- [Consumer Settings](consumer-settings.md#authorization-errors) and [Producer](producer.md#error-handling) describe how the clients retry authorization errors.
- [Detect Silent Authorization Failures](detecting-authorization-failures.md) makes a consumer notice that its read access was revoked.
- [Test Against a Secured Broker](writing-tests.md#test-against-a-secured-broker) shows the testkit's embedded broker with a SASL listener.
