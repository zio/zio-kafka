package zio.kafka.admin

import org.apache.kafka.clients.admin.RecordsToDelete
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.{ Node => JNode }
import zio.kafka.KafkaTestUtils
import zio.kafka.KafkaTestUtils._
import zio.kafka.admin.AdminClient.{
  ConfigResource,
  ConfigResourceType,
  ConsumerGroupDescription,
  ConsumerGroupState,
  ListConsumerGroupOffsetsOptions,
  ListConsumerGroupsOptions,
  OffsetAndMetadata,
  OffsetSpec,
  TopicPartition
}
import zio.kafka.consumer.{ CommittableRecord, Consumer, OffsetBatch, Subscription }
import zio.kafka.embedded.Kafka
import zio.kafka.serde.Serde
import zio.stream.ZSink
import zio.test.Assertion._
import zio.test.TestAspect._
import zio.test._
import zio.{ Chunk, Clock, Duration, Schedule, ZIO }

import java.util.UUID

object AdminSpec extends DefaultRunnableSpec {
  override def spec =
    suite("client admin test")(
      test("create, list, delete single topic") {
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
      test("create, list, delete multiple topic") {
        KafkaTestUtils.withAdmin { client =>
          for {
            list1 <- client.listTopics()
            _ <- client.createTopics(List(AdminClient.NewTopic("topic2", 1, 1), AdminClient.NewTopic("topic3", 4, 1)))
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
      test("just list") {
        KafkaTestUtils.withAdmin { client =>
          for {
            list1 <- client.listTopics()
          } yield assert(list1.size)(equalTo(0))

        }
      },
      test("create, describe, delete multiple topic") {
        KafkaTestUtils.withAdmin { client =>
          for {
            list1 <- client.listTopics()
            _ <- client.createTopics(List(AdminClient.NewTopic("topic4", 1, 1), AdminClient.NewTopic("topic5", 4, 1)))
            descriptions <- client.describeTopics(List("topic4", "topic5"))
            _            <- client.deleteTopics(List("topic4", "topic5"))
            list3        <- client.listTopics()
          } yield assert(list1.size)(equalTo(0)) &&
            assert(descriptions.size)(equalTo(2)) &&
            assert(list3.size)(equalTo(0))

        }
      },
      test("create, describe topic config, delete multiple topic") {
        KafkaTestUtils.withAdmin { client =>
          for {
            list1 <- client.listTopics()
            _ <- client.createTopics(List(AdminClient.NewTopic("topic6", 1, 1), AdminClient.NewTopic("topic7", 4, 1)))
            configs <- client.describeConfigs(
                         List(
                           ConfigResource(ConfigResourceType.Topic, "topic6"),
                           ConfigResource(ConfigResourceType.Topic, "topic7")
                         )
                       )
            _     <- client.deleteTopics(List("topic6", "topic7"))
            list3 <- client.listTopics()
          } yield assert(list1.size)(equalTo(0)) &&
            assert(configs.size)(equalTo(2)) &&
            assert(list3.size)(equalTo(0))
        }
      },
      test("list cluster nodes") {
        KafkaTestUtils.withAdmin { client =>
          for {
            nodes <- client.describeClusterNodes()
          } yield assert(nodes.size)(equalTo(1))
        }
      },
      test("get cluster controller") {
        KafkaTestUtils.withAdmin { client =>
          for {
            controller <- client.describeClusterController()
          } yield assert(controller.map(_.id))(isSome(equalTo(0)))
        }
      },
      test("get cluster id") {
        KafkaTestUtils.withAdmin { client =>
          for {
            controllerId <- client.describeClusterId()
          } yield assert(controllerId.nonEmpty)(isTrue)
        }
      },
      test("get cluster authorized operations") {
        KafkaTestUtils.withAdmin { client =>
          for {
            operations <- client.describeClusterAuthorizedOperations()
          } yield assert(operations)(equalTo(Set.empty[AclOperation]))
        }
      },
      test("describe broker config") {
        KafkaTestUtils.withAdmin { client =>
          for {
            configs <- client.describeConfigs(
                         List(
                           ConfigResource(ConfigResourceType.Broker, "0")
                         )
                       )
          } yield assert(configs.size)(equalTo(1))
        }
      },
      test("list offsets") {
        KafkaTestUtils.withAdmin { client =>
          val topic    = "topic8"
          val msgCount = 20
          val kvs      = (1 to msgCount).toList.map(i => (s"key$i", s"msg$i"))

          for {
            _ <- client.createTopics(List(AdminClient.NewTopic("topic8", 3, 1)))
            _ <- produceMany(topic, kvs).provideSome[Kafka with Clock](KafkaTestUtils.producer)
            offsets <- client.listOffsets(
                         (0 until 3).map(i => TopicPartition(topic, i) -> OffsetSpec.LatestSpec).toMap
                       )
          } yield assert(offsets.values.map(_.offset).sum)(equalTo(msgCount.toLong))
        }
      },
      test("alter offsets") {
        KafkaTestUtils.withAdmin { client =>
          val topic            = "topic9"
          val consumerGroupID  = "topic9"
          val partitionCount   = 3
          val msgCount         = 20
          val partitionResetBy = 2

          val p   = (0 until partitionCount).map(i => TopicPartition("topic9", i))
          val kvs = (1 to msgCount).toList.map(i => (s"key$i", s"msg$i"))

          def consumeAndCommit(count: Long) =
            Consumer
              .subscribeAnd(Subscription.Topics(Set(topic)))
              .partitionedStream[Kafka with Clock, String, String](Serde.string, Serde.string)
              .flatMapPar(partitionCount)(_._2)
              .take(count)
              .transduce(ZSink.collectAllN[CommittableRecord[String, String]](Int.MaxValue))
              .mapConcatZIO { committableRecords =>
                val records = committableRecords.map(_.record)
                val offsetBatch =
                  committableRecords.foldLeft(OffsetBatch.empty)(_ merge _.offset)

                offsetBatch.commit.as(records)
              }
              .runCollect
              .provideSome[Kafka with Clock](consumer("topic9", Some(consumerGroupID)))

          def toMap(records: Chunk[ConsumerRecord[String, String]]): Map[Int, List[(Long, String, String)]] =
            records.toList
              .map(r => (r.partition(), (r.offset(), r.key(), r.value())))
              .groupBy(_._1)
              .map { case (k, vs) => (k, vs.map(_._2).sortBy(_._1)) }

          for {
            _          <- client.createTopics(List(AdminClient.NewTopic(topic, partitionCount, 1)))
            _          <- produceMany(topic, kvs).provideSome[Kafka with Clock](KafkaTestUtils.producer)
            records    <- consumeAndCommit(msgCount.toLong).map(toMap)
            endOffsets <- client.listOffsets((0 until partitionCount).map(i => p(i) -> OffsetSpec.LatestSpec).toMap)
            _ <- client.alterConsumerGroupOffsets(
                   consumerGroupID,
                   Map(
                     p(0) -> OffsetAndMetadata(0),                                         // from the beginning
                     p(1) -> OffsetAndMetadata(endOffsets(p(1)).offset - partitionResetBy) // re-read two messages
                   )
                 )
            expectedMsgsToConsume = endOffsets(p(0)).offset + partitionResetBy
            recordsAfterAltering <- consumeAndCommit(expectedMsgsToConsume.toLong).map(toMap)
          } yield assert(recordsAfterAltering(0))(equalTo(records(0))) &&
            assert(records(1).take(endOffsets(p(1)).offset.toInt - partitionResetBy) ++ recordsAfterAltering(1))(
              equalTo(records(1))
            ) &&
            assert(recordsAfterAltering.get(2))(isNone)
        }
      },
      test("create, produce, delete records from a topic") {
        KafkaTestUtils.withAdmin { client =>
          type Env = Kafka with Clock
          val topicName      = UUID.randomUUID().toString
          val topicPartition = TopicPartition(topicName, 0)

          for {
            _             <- client.createTopic(AdminClient.NewTopic(topicName, 1, 1))
            _             <- produceOne(topicName, "key", "message").provideSomeLayer[Env](KafkaTestUtils.producer)
            offsetsBefore <- client.listOffsets(Map(topicPartition -> OffsetSpec.EarliestSpec))
            _             <- client.deleteRecords(Map(topicPartition -> RecordsToDelete.beforeOffset(1L)))
            offsetsAfter  <- client.listOffsets(Map(topicPartition -> OffsetSpec.EarliestSpec))
            _             <- client.deleteTopic(topicName)
          } yield assert(offsetsBefore.get(topicPartition).map(_.offset))(isSome(equalTo(0L))) &&
            assert(offsetsAfter.get(topicPartition).map(_.offset))(isSome(equalTo(1L)))

        }
      },
      test("list consumer groups") {
        KafkaTestUtils.withAdmin { implicit admin =>
          for {
            topicName <- randomTopic
            _         <- admin.createTopic(AdminClient.NewTopic(topicName, numPartitions = 10, replicationFactor = 1))
            groupId   <- randomGroup
            _         <- consumeNoop(topicName, groupId, "consumer1", Some("instance1")).fork
            _         <- getStableConsumerGroupDescription(groupId)
            list      <- admin.listConsumerGroups(Some(ListConsumerGroupsOptions(Set(ConsumerGroupState.Stable))))
          } yield assert(list)(exists(hasField("groupId", _.groupId, equalTo(groupId))))
        }
      },
      test("list consumer group offsets") {

        def consumeAndCommit(count: Long, topic: String, groupId: String) =
          Consumer
            .subscribeAnd(Subscription.Topics(Set(topic)))
            .plainStream[Kafka with Clock, String, String](Serde.string, Serde.string)
            .take(count)
            .foreach(_.offset.commit)
            .provideSome[Kafka with Clock](consumer(topic, Some(groupId)))

        KafkaTestUtils.withAdmin { client =>
          for {
            topic          <- randomTopic
            groupId        <- randomGroup
            invalidTopic   <- randomTopic
            invalidGroupId <- randomGroup
            msgCount   = 20
            msgConsume = 15
            kvs        = (1 to msgCount).toList.map(i => (s"key$i", s"msg$i"))
            _ <- client.createTopics(List(AdminClient.NewTopic(topic, 1, 1)))
            _ <- produceMany(topic, kvs).provideSome[Kafka](KafkaTestUtils.producer)
            _ <- consumeAndCommit(msgConsume.toLong, topic, groupId)
            offsets <- client.listConsumerGroupOffsets(
                         groupId,
                         Some(ListConsumerGroupOffsetsOptions(Chunk.single(TopicPartition(topic, 0))))
                       )
            invalidTopicOffsets <- client.listConsumerGroupOffsets(
                                     groupId,
                                     Some(
                                       ListConsumerGroupOffsetsOptions(Chunk.single(TopicPartition(invalidTopic, 0)))
                                     )
                                   )
            invalidTpOffsets <- client.listConsumerGroupOffsets(
                                  groupId,
                                  Some(
                                    ListConsumerGroupOffsetsOptions(Chunk.single(TopicPartition(topic, 1)))
                                  )
                                )
            invalidGroupIdOffsets <- client.listConsumerGroupOffsets(
                                       invalidGroupId,
                                       Some(ListConsumerGroupOffsetsOptions(Chunk.single(TopicPartition(topic, 0))))
                                     )
          } yield assert(offsets.get(TopicPartition(topic, 0)).map(_.offset))(isSome(equalTo(msgConsume.toLong))) &&
            assert(invalidTopicOffsets)(isEmpty) &&
            assert(invalidTpOffsets)(isEmpty) &&
            assert(invalidGroupIdOffsets)(isEmpty)
        }
      },
      test("describe consumer groups") {
        KafkaTestUtils.withAdmin { implicit admin =>
          for {
            topicName   <- randomTopic
            _           <- admin.createTopic(AdminClient.NewTopic(topicName, numPartitions = 10, replicationFactor = 1))
            groupId     <- randomGroup
            _           <- consumeNoop(topicName, groupId, "consumer1").fork
            _           <- consumeNoop(topicName, groupId, "consumer2").fork
            description <- getStableConsumerGroupDescription(groupId)
          } yield assert(description.groupId)(equalTo(groupId)) && assert(description.members.length)(equalTo(2))
        }
      },
      test("remove members from consumer groups") {
        KafkaTestUtils.withAdmin { implicit admin =>
          for {
            topicName   <- randomTopic
            _           <- admin.createTopic(AdminClient.NewTopic(topicName, numPartitions = 10, replicationFactor = 1))
            groupId     <- randomGroup
            _           <- consumeNoop(topicName, groupId, "consumer1", Some("instance1")).fork
            consumer2   <- consumeNoop(topicName, groupId, "consumer2", Some("instance2")).fork
            _           <- getStableConsumerGroupDescription(groupId)
            _           <- consumer2.interrupt
            _           <- admin.removeMembersFromConsumerGroup(groupId, Set("instance2"))
            description <- getStableConsumerGroupDescription(groupId)
          } yield assert(description.groupId)(equalTo(groupId)) && assert(description.members.length)(equalTo(1))
        }
      },
      test("describe log dirs") {
        KafkaTestUtils.withAdmin { implicit admin =>
          for {
            topicName <- randomTopic
            _         <- admin.createTopic(AdminClient.NewTopic(topicName, numPartitions = 1, replicationFactor = 1))
            node      <- admin.describeClusterNodes().head.orElseFail(new NoSuchElementException())
            logDirs   <- admin.describeLogDirs(List(node.id))
          } yield assert(logDirs)(
            hasKey(
              node.id,
              hasValues(exists(hasField("replicaInfos", _.replicaInfos, hasKey(TopicPartition(topicName, 0)))))
            )
          )
        }
      },
      test("should correctly handle no node (null) when converting JNode to Node") {
        assert(AdminClient.Node.apply(null))(isNone)
      },
      test("should correctly handle noNode when converting JNode to Node") {
        assert(AdminClient.Node.apply(JNode.noNode()))(isNone)
      },
      test("should correctly keep all information when converting a valid jNode to Node") {
        val posIntGen = Gen.int(0, Int.MaxValue)
        check(posIntGen, Gen.string1(Gen.char), posIntGen, Gen.option(Gen.string)) { (id, host, port, rack) =>
          val jNode = new JNode(id, host, port, rack.orNull)
          assert(AdminClient.Node.apply(jNode).map(_.asJava))(
            equalTo(Some(jNode))
          )
        }
      },
      test("will replace invalid port by None") {
        val posIntGen = Gen.int(0, Int.MaxValue)
        check(posIntGen, Gen.string1(Gen.char), Gen.int, Gen.option(Gen.string)) { (id, host, port, rack) =>
          val jNode = new JNode(id, host, port, rack.orNull)
          assert(AdminClient.Node.apply(jNode).map(_.port.isEmpty))(
            equalTo(Some(port < 0))
          )
        }
      },
      test("will replace empty host by None") {
        val jNode = new JNode(0, "", 9092, null)
        assert(AdminClient.Node.apply(jNode).map(_.host.isEmpty))(
          equalTo(Some(true))
        )
      }
    ).provideSomeShared[TestEnvironment](Kafka.embedded.mapError(TestFailure.fail) ++ Clock.live) @@ sequential

  private def consumeNoop(
    topicName: String,
    groupId: String,
    clientId: String,
    groupInstanceId: Option[String] = None
  ): ZIO[Kafka with Clock, Throwable, Unit] = Consumer
    .subscribeAnd(Subscription.topics(topicName))
    .plainStream(Serde.string, Serde.string)
    .foreach(_.offset.commit)
    // NOTE: layer error on Scala 3, reverting to provideSomeLayer fixes it... :thinking:
    .provideSome(consumer(clientId, Some(groupId), groupInstanceId))

  private def getStableConsumerGroupDescription(
    groupId: String
  )(implicit adminClient: AdminClient): ZIO[Clock, Throwable, ConsumerGroupDescription] =
    adminClient
      .describeConsumerGroups(groupId)
      .map(_.head._2)
      .repeat(
        (Schedule.recurs(5) && Schedule.fixed(Duration.fromMillis(500)) && Schedule
          .recurUntil[ConsumerGroupDescription](
            _.state == AdminClient.ConsumerGroupState.Stable
          )).map(_._3)
      )
      .flatMap(desc =>
        if (desc.state == AdminClient.ConsumerGroupState.Stable) {
          ZIO.succeed(desc)
        } else {
          ZIO.fail(new IllegalStateException(s"Client is not in stable state: $desc"))
        }
      )
}
