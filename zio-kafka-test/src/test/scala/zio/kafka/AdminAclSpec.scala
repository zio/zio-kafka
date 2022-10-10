package zio.kafka.admin

import zio.kafka.ZIOSpecWithKafka
import zio.kafka.KafkaTestUtils
import zio.test.Assertion._
import zio.test._

object AdminAclSpec extends ZIOSpecWithKafka {

  override val kafkaPrefix: String = "adminaclspec"

  private def aclFilter(topicPrefix: String) = AclBindingFilter(
    ResourcePatternFilter(ResourceType.Topic, topicPrefix, PatternType.Prefixed),
    AccessControlEntryFilter("User:ZIO", "*", AclOperation.Any, AclPermissionType.Allow)
  )


  override def spec =
    suite("client admin acl test")(test("create, describe, delete single acl") {
      KafkaTestUtils.withAdmin { client =>
        for {
          list1 <- client.describeAcls(aclFilter("adminspec-single-"))
          _ <- client.createAcl(
                 AclBinding(
                   ResourcePattern(ResourceType.Topic, "adminspec-single-", PatternType.Prefixed),
                   AccessControlEntry("User:ZIO", "*", AclOperation.Read, AclPermissionType.Allow)
                 )
               )
          list2 <- client.describeAcls(aclFilter("adminspec-single-"))
          _ <- client.deleteAcls(
                 Seq(aclFilter("adminspec-single-"))
               )          
          list3 <-client.describeAcls(aclFilter("adminspec-single-"))
        } yield assert(list1.size)(equalTo(0)) &&
          assert(list2.size)(equalTo(1)) &&
          assert(list3.size)(equalTo(0))
      }
    },
    (test("create, describe, delete multiple acls") {
      KafkaTestUtils.withAdmin { client =>
        for {
          list1 <- client.describeAcls(aclFilter("adminspec-multi-"))
          _ <- client.createAcls(Seq(
                 AclBinding(
                   ResourcePattern(ResourceType.Topic, "adminspec-multi-", PatternType.Prefixed),
                   AccessControlEntry("User:ZIO", "*", AclOperation.Read, AclPermissionType.Allow)
                 ),

                 AclBinding(
                   ResourcePattern(ResourceType.Topic, "adminspec-multi-", PatternType.Prefixed),
                   AccessControlEntry("User:ZIO", "*", AclOperation.Write, AclPermissionType.Allow)
                 )
               ))
          list2 <- client.describeAcls(aclFilter("adminspec-multi-"))
          _ <- client.deleteAcls(
                 Seq(aclFilter("adminspec-multi-"))
               )
          list3 <- client.describeAcls(aclFilter("adminspec-multi-"))
        } yield assert(list1.size)(equalTo(0)) &&
          assert(list2.size)(equalTo(2)) &&
          assert(list3.size)(equalTo(0))
      }
    }))
  }