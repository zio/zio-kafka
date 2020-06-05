package zio.kafka.admin

import org.apache.kafka.common.config.ConfigResource
import zio.kafka.KafkaTestUtils
import zio.kafka.embedded.Kafka
import zio.test.Assertion._
import zio.test.TestAspect._
import zio.test._
import zio.test.environment.TestEnvironment

object AdminSpec extends DefaultRunnableSpec {
  override def spec =
    suite("client admin test")(
      testM("create, list, delete single topic") {
        KafkaTestUtils.withAdmin { client =>
          for {
            list1 <- client.listTopics()
            _     <- client.createTopic(AdminClient.NewTopic("topic1", 1, 1))
            list2 <- client.listTopics()
            _     <- client.deleteTopic("topic1")
            list3 <- client.listTopics()
          } yield assert(list1.size)(equalTo(0)) &&
            assert(list2.size)(equalTo(1)) &&
            assert(list3.size)(equalTo(0))

        }
      },
      testM("create, list, delete multiple topic") {
        KafkaTestUtils.withAdmin {
          client =>
            for {
              list1 <- client.listTopics()
              _     <- client.createTopics(List(AdminClient.NewTopic("topic2", 1, 1), AdminClient.NewTopic("topic3", 4, 1)))
              list2 <- client.listTopics()
              _     <- client.deleteTopic("topic2")
              list3 <- client.listTopics()
              _     <- client.deleteTopic("topic3")
              list4 <- client.listTopics()
            } yield assert(list1.size)(equalTo(0)) &&
              assert(list2.size)(equalTo(2)) &&
              assert(list3.size)(equalTo(1)) &&
              assert(list4.size)(equalTo(0))

        }
      },
      testM("just list") {
        KafkaTestUtils.withAdmin { client =>
          for {
            list1 <- client.listTopics()
          } yield assert(list1.size)(equalTo(0))

        }
      },
      testM("create, describe, delete multiple topic") {
        KafkaTestUtils.withAdmin {
          client =>
            for {
              list1        <- client.listTopics()
              _            <- client.createTopics(List(AdminClient.NewTopic("topic4", 1, 1), AdminClient.NewTopic("topic5", 4, 1)))
              descriptions <- client.describeTopics(List("topic4", "topic5"))
              _            <- client.deleteTopics(List("topic4", "topic5"))
              list3        <- client.listTopics()
            } yield assert(list1.size)(equalTo(0)) &&
              assert(descriptions.size)(equalTo(2)) &&
              assert(list3.size)(equalTo(0))

        }
      },
      testM("create, describe topic config, delete multiple topic") {
        KafkaTestUtils.withAdmin {
          client =>
            for {
              list1 <- client.listTopics()
              _     <- client.createTopics(List(AdminClient.NewTopic("topic6", 1, 1), AdminClient.NewTopic("topic7", 4, 1)))
              configs <- client.describeConfigs(
                          List(
                            new ConfigResource(ConfigResource.Type.TOPIC, "topic6"),
                            new ConfigResource(ConfigResource.Type.TOPIC, "topic7")
                          )
                        )
              _     <- client.deleteTopics(List("topic6", "topic7"))
              list3 <- client.listTopics()
            } yield assert(list1.size)(equalTo(0)) &&
              assert(configs.size)(equalTo(2)) &&
              assert(list3.size)(equalTo(0))
        }
      },
      testM("describe broker config") {
        KafkaTestUtils.withAdmin { client =>
          for {
            configs <- client.describeConfigs(
                        List(
                          new ConfigResource(ConfigResource.Type.BROKER, "0")
                        )
                      )
          } yield assert(configs.size)(equalTo(1))
        }
      }
    ).provideSomeLayerShared[TestEnvironment](Kafka.embedded.mapError(TestFailure.fail)) @@ sequential
}
