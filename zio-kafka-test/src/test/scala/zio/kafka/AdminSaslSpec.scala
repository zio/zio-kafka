package zio.kafka

import zio.Scope
import zio.test.Assertion._
import zio.test.TestAspect._
import zio.test._
import zio._
import zio.kafka.admin.acl.AclBindingFilter
import zio.kafka.admin.resource.ResourcePatternFilter
import zio.kafka.admin.acl.AccessControlEntryFilter
import zio.kafka.admin.acl.AclBinding
import zio.kafka.admin.resource.ResourcePattern
import zio.kafka.admin.acl.AccessControlEntry
import zio.kafka.admin.acl.AclPermissionType
import zio.kafka.admin.acl.AclOperation
import zio.kafka.admin.resource.ResourceType
import zio.kafka.admin.resource.PatternType

object AdminSaslSpec extends ZIOSpecWithSaslKafka {

  override def kafkaPrefix: String = "adminsaslspec"

  override def spec: Spec[Environment with TestEnvironment with Scope, Any] =
    suite("client sasl admin test")(
      test("ACLs") {
        KafkaTestUtils.withSaslAdmin() { client =>
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
            _ <-
              client.createAcls(bindings)
            createdAcls <-
              client
                .describeAcls(AclBindingFilter(ResourcePatternFilter.Any, AccessControlEntryFilter.Any))
                .delay(50.millis) // because the createAcls is async
            deletedAcls <-
              client
                .deleteAcls(Set(AclBindingFilter(ResourcePatternFilter.Any, AccessControlEntryFilter.Any)))
            remainingAcls <-
              client
                .describeAcls(AclBindingFilter(ResourcePatternFilter.Any, AccessControlEntryFilter.Any))
                .delay(50.millis) // because the deleteAcls is async

          } yield assert(createdAcls)(equalTo(bindings)) &&
            assert(deletedAcls)(equalTo(bindings)) &&
            assert(remainingAcls)(equalTo(Set.empty[AclBinding]))
        }
      }
    ) @@ withLiveClock @@ sequential

}
