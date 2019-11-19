package zio.kafka.client
import java.time.Instant

import org.apache.kafka.clients.admin.NewTopic
import zio._
import zio.test._
import zio.duration._
import zio.test.Assertion._
import zio.test.TestAspect._
import ClientAdminTestHelper._
object ClientAdminTest
    extends DefaultRunnableSpec(
      suite("client admin test")(
//        0.to(4).map { i => ClientAdminTestHelper.listOnly(i)} :_*
        List(singleTopic, multiTopics, describeTopics): _*
      ).provideManagedShared(KafkaTestUtils.embeddedKafkaEnvironment) @@ sequential
    )

object ClientAdminTestHelper {
  val singleTopic = testM("create, list, delete single topic") {
    KafkaTestUtils.withAdmin { client =>
      println(s"start single")
      for {
        list1 <- client.listTopics()
        _     <- client.createTopic(new NewTopic("topic1", 1, 1))
        _     <- ZIO.sleep(3.seconds)
        list2 <- client.listTopics()
        _     <- client.deleteTopic("topic1")
        _     <- ZIO.sleep(3.seconds)
        list3 <- client.listTopics()
      } yield {
        println(s"end single list1 is ${list1}")
        assert(list1.size, equalTo(0)) &&
        assert(list2.size, equalTo(1)) &&
        assert(list3.size, equalTo(0))
      }
    }
  }
  val multiTopics = testM("create, list, delete multiple topic") {
    KafkaTestUtils.withAdmin { client =>
      println(s"start multi")
      for {
        list1 <- client.listTopics()
        _     <- client.createTopics(List(new NewTopic("topic2", 1, 1), new NewTopic("topic3", 4, 1)))
        _     <- ZIO.sleep(3.seconds)
        list2 <- client.listTopics()
        _     <- client.deleteTopic("topic2")
        _     <- ZIO.sleep(3.seconds)
        list3 <- client.listTopics()
        _     <- client.deleteTopic("topic3")
        _     <- ZIO.sleep(3.seconds)
        list4 <- client.listTopics()
      } yield {
        println(s"end multi\nlist1 $list1\nlist2 $list2\nlist3 $list3\nlist4 $list4")
        assert(list1.size, equalTo(0)) &&
        assert(list2.size, equalTo(2)) &&
        assert(list3.size, equalTo(1)) &&
        assert(list4.size, equalTo(0))
      }
    }
  }

  def listOnly(i: Int) = testM("just list") {
    KafkaTestUtils.withAdmin { client =>
      for {
        _     <- IO.succeed(println(s"start$i at ${Instant.now}"))
        list1 <- client.listTopics()
        _     <- ZIO.sleep(3.seconds)
      } yield {
        println(s"end$i  at ${Instant.now}")
        assert(list1.size, equalTo(0))
      }
    }
  }

  val describeTopics = testM("create, describe, delete multiple topic") {
    KafkaTestUtils.withAdmin { client =>
      println(s"start describe")
      for {
        list1        <- client.listTopics()
        _            <- client.createTopics(List(new NewTopic("topic4", 1, 1), new NewTopic("topic5", 4, 1)))
        _            <- ZIO.sleep(3.seconds)
        describer    <- client.describeTopics(List("topic4", "topic5"))
        descriptions <- describer.completed
        _            <- client.deleteTopics(List("topic4", "topic5"))
        _            <- ZIO.sleep(3.seconds)
        list3        <- client.listTopics()
      } yield {
        println(s"end describe")
        assert(list1.size, equalTo(0)) &&
        assert(descriptions.size, equalTo(2)) &&
        assert(list3.size, equalTo(0))
      }
    }
  }

}
