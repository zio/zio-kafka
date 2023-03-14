package zio.kafka.security

import zio.test.Assertion._
import zio.test._
import zio.Scope

object KafkaCredentialStoreSpec extends ZIOSpecDefault {
  override def spec: Spec[Environment with TestEnvironment with Scope,Any] = suite("KafkaCredentialStore")(
    suite("fromPemStrigs")(
      test("KafkaCredentialStore.properties works properly") {
        val caCert =
          """-----BEGIN CERTIFICATE-----
            |RmFrZSBDQSBjZXJ0aWZpY2F0ZSBGYWtlIENBIGNlcnRpZmljYXRlIEZha2UgQ0EgY2VydGlmaWNh
            |dGUgRmFrZSBDQSBjZXJ0aWZpY2F0ZQ==
            |-----END CERTIFICATE-----""".stripMargin

        val privateKey =
          """-----BEGIN PRIVATE KEY-----
            |RmFrZSBwcml2YXRlIGtleSBGYWtlIHByaXZhdGUga2V5IEZha2UgcHJpdmF0ZSBrZXkgRmFrZSBw
            |cml2YXRlIGtleSBGYWtlIHByaXZhdGUga2V5IA==
            |-----END PRIVATE KEY-----""".stripMargin

        val clientCert =
          """-----BEGIN CERTIFICATE-----
            |RmFrZSBjbGllbnQgY2VydCBGYWtlIGNsaWVudCBjZXJ0IEZha2UgY2xpZW50IGNlcnQgRmFrZSBj
            |bGllbnQgY2VydCBGYWtlIGNsaWVudCBjZXJ0IA==
            |-----END CERTIFICATE-----""".stripMargin

        val store = KafkaCredentialStore.fromPemStrings(caCert, privateKey, clientCert)

        assert(store.properties)(
          equalTo(
            Map(
              "security.protocol"   -> "SSL",
              "ssl.truststore.type" -> "PEM",
              "ssl.truststore.certificates" -> "-----BEGIN CERTIFICATE----- RmFrZSBDQSBjZXJ0aWZpY2F0ZSBGYWtlIENBIGNlcnRpZmljYXRlIEZha2UgQ0EgY2VydGlmaWNh dGUgRmFrZSBDQSBjZXJ0aWZpY2F0ZQ== -----END CERTIFICATE-----",
              "ssl.keystore.type" -> "PEM",
              "ssl.keystore.key" -> "-----BEGIN PRIVATE KEY----- RmFrZSBwcml2YXRlIGtleSBGYWtlIHByaXZhdGUga2V5IEZha2UgcHJpdmF0ZSBrZXkgRmFrZSBw cml2YXRlIGtleSBGYWtlIHByaXZhdGUga2V5IA== -----END PRIVATE KEY-----",
              "ssl.keystore.certificate.chain" -> "-----BEGIN CERTIFICATE----- RmFrZSBjbGllbnQgY2VydCBGYWtlIGNsaWVudCBjZXJ0IEZha2UgY2xpZW50IGNlcnQgRmFrZSBj bGllbnQgY2VydCBGYWtlIGNsaWVudCBjZXJ0IA== -----END CERTIFICATE-----"
            )
          )
        )
      }
    )
  )
}
