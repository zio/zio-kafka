package zio.kafka.security

/**
 * Credentials for connecting to a Kafka cluster over TLS. Create one with `KafkaCredentialStore.fromPemStrings` and add
 * it to the settings of a client with `withCredentials`.
 *
 * @see
 *   [[https://zio.dev/zio-kafka/connecting-to-a-secured-cluster/ Connect to a Secured Cluster]]
 */
sealed trait KafkaCredentialStore {

  /**
   * The properties of the Java client that these credentials set.
   */
  def properties: Map[String, String]
}

/**
 * Constructors of [[KafkaCredentialStore]].
 */
object KafkaCredentialStore {

  /**
   * Credentials for mutual TLS, from PEM strings. The properties set `security.protocol` to `SSL`, trust the
   * certificates in `caCertificate`, and identify the client with `clientPrivateKey` and `clientCertificate`. Newlines
   * in the strings are replaced by spaces.
   *
   * @param caCertificate
   *   the PEM certificates that are trusted (`ssl.truststore.certificates`)
   * @param clientPrivateKey
   *   the PEM private key of the client (`ssl.keystore.key`)
   * @param clientCertificate
   *   the PEM certificate chain of the client (`ssl.keystore.certificate.chain`)
   */
  final def fromPemStrings(
    caCertificate: String,
    clientPrivateKey: String,
    clientCertificate: String
  ): KafkaCredentialStore =
    new KafkaCredentialStore {
      override val properties: Map[String, String] =
        Map(
          "security.protocol"              -> "SSL",
          "ssl.truststore.type"            -> "PEM",
          "ssl.truststore.certificates"    -> caCertificate.replace('\n', ' '),
          "ssl.keystore.type"              -> "PEM",
          "ssl.keystore.key"               -> clientPrivateKey.replace('\n', ' '),
          "ssl.keystore.certificate.chain" -> clientCertificate.replace('\n', ' ')
        )
    }
}
