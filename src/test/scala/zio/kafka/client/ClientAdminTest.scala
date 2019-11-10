package zio.kafka.client
import org.apache.kafka.clients.admin.NewTopic
import zio._
import zio.test._
import zio.duration._
import zio.test.Assertion._

object ClientAdminTest
    extends DefaultRunnableSpec(
      suite("client admin test")(
        testM("create, list, delete single topic") {
          KafkaTestUtils.withAdmin { client =>
            for {
              list1 <- client.listTopics()
              _     <- client.createTopic(new NewTopic("topic1", 1, 1))
              _     <- ZIO.sleep(3.seconds)
              list2 <- client.listTopics()
              _     <- client.deleteTopic("topic1")
              _     <- ZIO.sleep(3.seconds)
              list3 <- client.listTopics()
            } yield assert(list1.size, equalTo(0)) &&
              assert(list2.size, equalTo(1)) &&
              assert(list3.size, equalTo(0))
          }
        },
        testM("create, list, delete multiple topic") {
          KafkaTestUtils.withAdmin { client =>
            for {
              list1 <- client.listTopics()
              _     <- client.createTopics(
                List(new NewTopic("topic1", 1, 1),
                new NewTopic("topic4", 4, 1)))
              _     <- ZIO.sleep(3.seconds)
              list2 <- client.listTopics()
              _     <- client.deleteTopic("topic1")
              _     <- ZIO.sleep(3.seconds)
              list3 <- client.listTopics()
            } yield assert(list1.size, equalTo(0)) &&
              assert(list2.size, equalTo(2)) &&
              assert(list3.size, equalTo(1))
          }
        }
      ).provideManagedShared(KafkaTestUtils.kafkaEnvironment)
    )
