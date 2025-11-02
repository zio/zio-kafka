package zio.kafka.admin

import zio._
import zio.kafka.ZIOSpecDefaultSlf4j
import zio.kafka.admin.acl._
import zio.kafka.admin.resource.{ PatternType, ResourcePattern, ResourcePatternFilter, ResourceType }
import zio.kafka.testkit._
import zio.test.TestAspect._
import zio.test._

import java.util.concurrent.TimeoutException

object AdminSaslSpec extends ZIOSpecDefaultSlf4j with KafkaRandom {

  override def kafkaPrefix: String = "adminsaslspec"

  override def spec: Spec[TestEnvironment with Scope, Any] =
    suite("client sasl admin test")(
      test("ACLs") {
        for {
          topic <- randomTopic
          bindings =
            Set(
              AclBinding(
                ResourcePattern(ResourceType.Topic, name = topic, patternType = PatternType.Literal),
                AccessControlEntry(
                  principal = "User:*",
                  host = "*",
                  operation = AclOperation.Write,
                  permissionType = AclPermissionType.Allow
                )
              )
            )
          client      <- KafkaTestUtils.makeSaslAdminClient()
          _           <- client.createAcls(bindings)
          createdAcls <-
            client
              .describeAcls(AclBindingFilter(ResourcePatternFilter.Any, AccessControlEntryFilter.Any))
              .repeatWhile(_.isEmpty) // because the createAcls is executed async by the broker
              .timeoutFail(new TimeoutException())(200.millis)
          deletedAcls <-
            client
              .deleteAcls(Set(AclBindingFilter(ResourcePatternFilter.Any, AccessControlEntryFilter.Any)))
          remainingAcls <-
            client
              .describeAcls(AclBindingFilter(ResourcePatternFilter.Any, AccessControlEntryFilter.Any))
              .repeatWhile(_.nonEmpty) // because the deleteAcls is executed async by the broker
              .timeoutFail(new TimeoutException())(200.millis)

        } yield assertTrue(
          createdAcls == bindings,
          deletedAcls == bindings,
          remainingAcls.isEmpty
        )
      }
    ).provideSomeShared[Scope](Kafka.saslEmbedded) @@ withLiveClock

}
