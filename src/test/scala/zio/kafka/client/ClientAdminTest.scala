package zio.kafka.client

import zio.test._
import zio.test.Assertion._
import zio.test.TestAspect._

import ClientAdminTestHelper._

object ClientAdminTest
    extends DefaultRunnableSpec(
      suite("client admin test")(
        List(singleTopic, multiTopics, listOnly, describeTopics): _*
      ).provideManagedShared(KafkaTestUtils.embeddedKafkaEnvironment) @@ sequential
    )

object ClientAdminTestHelper {
  val singleTopic = testM("create, list, delete single topic") {
    KafkaTestUtils.withAdmin { client =>
      for {
        list1 <- client.listTopics()
        _     <- client.createTopic(AdminClient.NewTopic("topic1", 1, 1))
        list2 <- client.listTopics()
        _     <- client.deleteTopic("topic1")
        list3 <- client.listTopics()
      } yield assert(list1.size, equalTo(0)) &&
        assert(list2.size, equalTo(1)) &&
        assert(list3.size, equalTo(0))

    }
  }
  val multiTopics = testM("create, list, delete multiple topic") {
    KafkaTestUtils.withAdmin { client =>
      for {
        list1 <- client.listTopics()
        _     <- client.createTopics(List(AdminClient.NewTopic("topic2", 1, 1), AdminClient.NewTopic("topic3", 4, 1)))
        list2 <- client.listTopics()
        _     <- client.deleteTopic("topic2")
        list3 <- client.listTopics()
        _     <- client.deleteTopic("topic3")
        list4 <- client.listTopics()
      } yield assert(list1.size, equalTo(0)) &&
        assert(list2.size, equalTo(2)) &&
        assert(list3.size, equalTo(1)) &&
        assert(list4.size, equalTo(0))

    }
  }

  val listOnly = testM("just list") {
    KafkaTestUtils.withAdmin { client =>
      for {
        list1 <- client.listTopics()
      } yield assert(list1.size, equalTo(0))

    }
  }

  val describeTopics = testM("create, describe, delete multiple topic") {
    KafkaTestUtils.withAdmin { client =>
      for {
        list1        <- client.listTopics()
        _            <- client.createTopics(List(AdminClient.NewTopic("topic4", 1, 1), AdminClient.NewTopic("topic5", 4, 1)))
        descriptions <- client.describeTopics(List("topic4", "topic5"))
        _            <- client.deleteTopics(List("topic4", "topic5"))
        list3        <- client.listTopics()
      } yield assert(list1.size, equalTo(0)) &&
        assert(descriptions.size, equalTo(2)) &&
        assert(list3.size, equalTo(0))

    }
  }

}
