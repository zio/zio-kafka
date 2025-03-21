package zio.kafka.admin

import org.apache.kafka.clients.admin.ConfigEntry.ConfigSource
import org.apache.kafka.clients.admin.{ ConfigEntry, RecordsToDelete }
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.{ Node => JNode }
import zio._
import zio.kafka.ZIOSpecDefaultSlf4j
import zio.kafka.admin.AdminClient.{
  AlterConfigOp,
  AlterConfigOpType,
  AlterConfigsOptions,
  ConfigResource,
  ConfigResourceType,
  ConsumerGroupDescription,
  GroupState,
  ListConsumerGroupOffsetsSpec,
  ListConsumerGroupsOptions,
  OffsetAndMetadata,
  OffsetSpec,
  TopicPartition
}
import zio.kafka.admin.acl._
import zio.kafka.admin.resource.{ PatternType, ResourcePattern, ResourcePatternFilter, ResourceType }
import zio.kafka.consumer.{ CommittableRecord, OffsetBatch, Subscription }
import zio.kafka.serde.Serde
import zio.kafka.testkit._
import zio.stream.ZSink
import zio.test.Assertion._
import zio.test.TestAspect._
import zio.test._

import java.util.UUID
import java.util.concurrent.TimeoutException

object AdminSpec extends ZIOSpecDefaultSlf4j with KafkaRandom {

  override val kafkaPrefix: String = "adminspec"

  private def listTopicsFiltered(
    client: AdminClient,
    prefix: String
  ): ZIO[Any, Throwable, Map[String, AdminClient.TopicListing]] =
    client.listTopics().map(_.filter { case (key, _) => key.startsWith(prefix) })

  override def spec: Spec[TestEnvironment with Scope, Throwable] =
    suite("client admin test")(
      test("create, list, delete single topic") {
        val prefix = "adminspec1"
        for {
          client <- KafkaTestUtils.makeAdminClient
          list1  <- listTopicsFiltered(client, prefix)
          _      <- client.createTopic(AdminClient.NewTopic(s"$prefix-topic1", 1, 1))
          list2  <- listTopicsFiltered(client, prefix)
          _      <- client.deleteTopic(s"$prefix-topic1")
          list3  <- listTopicsFiltered(client, prefix)
        } yield assertTrue(list1.isEmpty, list2.size == 1, list3.isEmpty)
      },
      test("create, list, delete multiple topic") {
        val prefix = "adminspec2"
        for {
          client <- KafkaTestUtils.makeAdminClient
          list1  <- listTopicsFiltered(client, prefix)
          _ <- client.createTopics(
                 List(AdminClient.NewTopic(s"$prefix-topic2", 1, 1), AdminClient.NewTopic(s"$prefix-topic3", 4, 1))
               )
          list2 <- listTopicsFiltered(client, prefix)
          _     <- client.deleteTopic(s"$prefix-topic2")
          list3 <- listTopicsFiltered(client, prefix)
          _     <- client.deleteTopic(s"$prefix-topic3")
          list4 <- listTopicsFiltered(client, prefix)
        } yield assertTrue(list1.isEmpty, list2.size == 2, list3.size == 1, list4.isEmpty)
      },
      test("just list") {
        for {
          client <- KafkaTestUtils.makeAdminClient
          list1  <- listTopicsFiltered(client, "adminspec3")
        } yield assertTrue(list1.isEmpty)
      },
      test("create, describe, delete multiple topic") {
        val prefix = "adminspec4"
        for {
          client <- KafkaTestUtils.makeAdminClient
          list1  <- listTopicsFiltered(client, prefix)
          _ <- client.createTopics(
                 List(AdminClient.NewTopic(s"$prefix-topic4", 1, 1), AdminClient.NewTopic(s"$prefix-topic5", 4, 1))
               )
          descriptions <- client.describeTopics(List(s"$prefix-topic4", s"$prefix-topic5"))
          _            <- client.deleteTopics(List(s"$prefix-topic4", s"$prefix-topic5"))
          list3        <- listTopicsFiltered(client, prefix)
        } yield assertTrue(list1.isEmpty, descriptions.size == 2, list3.isEmpty)
      },
      test("create, describe topic config, delete multiple topic") {
        val prefix = "adminspec5"
        for {
          client <- KafkaTestUtils.makeAdminClient
          list1  <- listTopicsFiltered(client, prefix)
          _ <- client.createTopics(
                 List(AdminClient.NewTopic(s"$prefix-topic6", 1, 1), AdminClient.NewTopic(s"$prefix-topic7", 4, 1))
               )
          configResources = List(
                              ConfigResource(ConfigResourceType.Topic, s"$prefix-topic6"),
                              ConfigResource(ConfigResourceType.Topic, s"$prefix-topic7")
                            )
          configs <- client.describeConfigs(configResources) <&>
                       client.describeConfigsAsync(configResources).flatMap { configs =>
                         ZIO.foreachPar(configs) { case (resource, configTask) =>
                           configTask.map(config => (resource, config))
                         }
                       }
          _     <- client.deleteTopics(List(s"$prefix-topic6", s"$prefix-topic7"))
          list3 <- listTopicsFiltered(client, prefix)
        } yield assertTrue(list1.isEmpty, configs._1.size == 2, configs._2.size == 2, list3.isEmpty)
      },
      test("list cluster nodes") {
        for {
          client <- KafkaTestUtils.makeAdminClient
          nodes  <- client.describeClusterNodes()
        } yield assert(nodes.size)(equalTo(1))
      },
      test("get cluster controller") {
        for {
          client     <- KafkaTestUtils.makeAdminClient
          controller <- client.describeClusterController()
        } yield assert(controller.map(_.id))(isSome(equalTo(0)))
      },
      test("get cluster id") {
        for {
          client       <- KafkaTestUtils.makeAdminClient
          controllerId <- client.describeClusterId()
        } yield assert(controllerId.nonEmpty)(isTrue)
      },
      test("get cluster authorized operations") {
        for {
          client     <- KafkaTestUtils.makeAdminClient
          operations <- client.describeClusterAuthorizedOperations()
        } yield assertTrue(operations == Set.empty[AclOperation])
      },
      test("describe broker config") {
        for {
          client <- KafkaTestUtils.makeAdminClient
          configs <- client.describeConfigs(
                       List(ConfigResource(ConfigResourceType.Broker, "0"))
                     )
        } yield assertTrue(configs.size == 1)
      },
      test("describe broker config async") {
        for {
          client <- KafkaTestUtils.makeAdminClient
          configTasks <- client.describeConfigsAsync(
                           List(
                             ConfigResource(ConfigResourceType.Broker, "0")
                           )
                         )
          configs <- ZIO.foreachPar(configTasks) { case (resource, configTask) =>
                       configTask.map(config => (resource, config))
                     }
        } yield assertTrue(configs.size == 1)
      },
      test("list offsets") {
        val topic          = "adminspec-topic8"
        val partitionCount = 3
        val msgCount       = 20
        val kvs            = Seq.tabulate(msgCount)(i => (s"key$i", s"msg$i"))

        for {
          client   <- KafkaTestUtils.makeAdminClient
          _        <- client.createTopics(List(AdminClient.NewTopic("adminspec-topic8", partitionCount, 1)))
          producer <- KafkaTestUtils.makeProducer
          _        <- KafkaTestUtils.produceMany(producer, topic, kvs)
          offsets <- client.listOffsets(
                       (0 until partitionCount).map(i => TopicPartition(topic, i) -> OffsetSpec.LatestSpec).toMap
                     )
        } yield assertTrue(offsets.values.map(_.offset).sum == msgCount.toLong)
      },
      test("list offsets async") {
        val topic    = "adminspec-topic9"
        val msgCount = 20
        val kvs      = (1 to msgCount).toList.map(i => (s"key$i", s"msg$i"))

        for {
          client   <- KafkaTestUtils.makeAdminClient
          _        <- client.createTopics(List(AdminClient.NewTopic("adminspec-topic9", 3, 1)))
          producer <- KafkaTestUtils.makeProducer
          _        <- KafkaTestUtils.produceMany(producer, topic, kvs)
          offsetTasks <- client.listOffsetsAsync(
                           (0 until 3).map(i => TopicPartition(topic, i) -> OffsetSpec.LatestSpec).toMap
                         )
          offsets <- ZIO.foreachPar(offsetTasks) { case (topicPartition, offsetTask) =>
                       offsetTask.map((topicPartition, _))
                     }
        } yield assert(offsets.values.map(_.offset).sum)(equalTo(msgCount.toLong))
      },
      test("alter offsets") {
        val topic            = "adminspec-topic10"
        val consumerGroupID  = "adminspec-topic10"
        val partitionCount   = 3
        val msgCount         = 20
        val partitionResetBy = 2

        val p   = (0 until partitionCount).map(i => TopicPartition("adminspec-topic10", i))
        val kvs = (1 to msgCount).toList.map(i => (s"key$i", s"msg$i"))

        def consumeAndCommit(count: Long) =
          ZIO.scoped {
            for {
              consumer <- KafkaTestUtils.makeConsumer("adminspec-topic10", Some(consumerGroupID))
              records <-
                consumer
                  .partitionedStream[Kafka, String, String](
                    Subscription.Topics(Set(topic)),
                    Serde.string,
                    Serde.string
                  )
                  .flatMapPar(partitionCount)(_._2)
                  .take(count)
                  .transduce(ZSink.collectAllN[CommittableRecord[String, String]](20))
                  .mapConcatZIO { committableRecords =>
                    val records     = committableRecords.map(_.record)
                    val offsetBatch = OffsetBatch(committableRecords.map(_.offset))

                    offsetBatch.commit.as(records)
                  }
                  .runCollect
            } yield records
          }

        def toMap(records: Chunk[ConsumerRecord[String, String]]): Map[Int, List[(Long, String, String)]] =
          records.toList
            .map(r => (r.partition(), (r.offset(), r.key(), r.value())))
            .groupBy(_._1)
            .map { case (k, vs) => (k, vs.map(_._2).sortBy(_._1)) }

        for {
          client     <- KafkaTestUtils.makeAdminClient
          _          <- client.createTopics(List(AdminClient.NewTopic(topic, partitionCount, 1)))
          producer   <- KafkaTestUtils.makeProducer
          _          <- KafkaTestUtils.produceMany(producer, topic, kvs)
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
          recordsAfterAltering <- consumeAndCommit(expectedMsgsToConsume).map(toMap)
        } yield assertTrue(
          recordsAfterAltering(0) == records(0),
          records(1).take(endOffsets(p(1)).offset.toInt - partitionResetBy) ++ recordsAfterAltering(1) == records(1),
          recordsAfterAltering.get(2) == None
        )
      },
      test("create, produce, delete records from a topic") {
        val topicName      = UUID.randomUUID().toString
        val topicPartition = TopicPartition(topicName, 0)

        for {
          client        <- KafkaTestUtils.makeAdminClient
          _             <- client.createTopic(AdminClient.NewTopic(topicName, 1, 1))
          producer      <- KafkaTestUtils.makeProducer
          _             <- KafkaTestUtils.produceOne(producer, topicName, "key", "message")
          offsetsBefore <- client.listOffsets(Map(topicPartition -> OffsetSpec.EarliestSpec))
          _             <- client.deleteRecords(Map(topicPartition -> RecordsToDelete.beforeOffset(1L)))
          offsetsAfter  <- client.listOffsets(Map(topicPartition -> OffsetSpec.EarliestSpec))
          _             <- client.deleteTopic(topicName)
        } yield assertTrue(
          offsetsBefore.get(topicPartition).map(_.offset).get == 0L,
          offsetsAfter.get(topicPartition).map(_.offset).get == 1L
        )
      },
      test("list consumer groups") {
        for {
          topicName <- randomTopic
          client    <- KafkaTestUtils.makeAdminClient
          _         <- client.createTopic(AdminClient.NewTopic(topicName, numPartitions = 10, replicationFactor = 1))
          groupId   <- randomGroup
          _         <- consumeNoop(topicName, groupId, "consumer1", Some("instance1")).fork
          _         <- getStableConsumerGroupDescription(client, groupId)
          list      <- client.listConsumerGroups(Some(ListConsumerGroupsOptions(Set(GroupState.Stable))))
        } yield assert(list)(exists(hasField("groupId", _.groupId, equalTo(groupId))))
      },
      test("list consumer group offsets") {

        def consumeAndCommit(count: Long, topic: String, groupId: String) =
          ZIO.scoped {
            for {
              consumer <- KafkaTestUtils.makeConsumer(topic, Some(groupId))
              _ <- consumer
                     .plainStream[Kafka, String, String](Subscription.Topics(Set(topic)), Serde.string, Serde.string)
                     .take(count)
                     .foreach(_.offset.commit)
            } yield ()
          }

        for {
          topic          <- randomTopic
          groupId        <- randomGroup
          invalidTopic   <- randomTopic
          invalidGroupId <- randomGroup
          msgCount   = 20
          msgConsume = 15
          kvs        = (1 to msgCount).toList.map(i => (s"key$i", s"msg$i"))
          client   <- KafkaTestUtils.makeAdminClient
          _        <- client.createTopics(List(AdminClient.NewTopic(topic, 1, 1)))
          producer <- KafkaTestUtils.makeProducer
          _        <- KafkaTestUtils.produceMany(producer, topic, kvs)
          _        <- consumeAndCommit(msgConsume.toLong, topic, groupId)
          offsets <- client.listConsumerGroupOffsets(
                       Map(groupId -> ListConsumerGroupOffsetsSpec(Chunk.single(TopicPartition(topic, 0))))
                     )
          invalidTopicOffsets <-
            client.listConsumerGroupOffsets(
              Map(groupId -> ListConsumerGroupOffsetsSpec(Chunk.single(TopicPartition(invalidTopic, 0))))
            )
          invalidTpOffsets <- client.listConsumerGroupOffsets(
                                Map(groupId -> ListConsumerGroupOffsetsSpec(Chunk.single(TopicPartition(topic, 1))))
                              )
          invalidGroupIdOffsets <-
            client.listConsumerGroupOffsets(
              Map(invalidGroupId -> ListConsumerGroupOffsetsSpec(Chunk.single(TopicPartition(topic, 0))))
            )
        } yield assertTrue(
          offsets.get(groupId).flatMap(_.get(TopicPartition(topic, 0))).map(_.offset).get == msgConsume.toLong,
          invalidTopicOffsets.get(groupId).exists(_.isEmpty),
          invalidTpOffsets.get(groupId).exists(_.isEmpty),
          invalidGroupIdOffsets.get(invalidGroupId).exists(_.isEmpty)
        )
      },
      test("delete consumer group offsets") {

        def consumeAndCommit(count: Long, topic: String, groupId: String) =
          ZIO.scoped {
            for {
              consumer <- KafkaTestUtils.makeConsumer(topic, Some(groupId))
              _ <- consumer
                     .plainStream[Kafka, String, String](Subscription.Topics(Set(topic)), Serde.string, Serde.string)
                     .take(count)
                     .foreach(_.offset.commit)
            } yield ()
          }

        for {
          topic   <- randomTopic
          groupId <- randomGroup
          msgCount   = 20
          msgConsume = 15
          kvs        = (1 to msgCount).toList.map(i => (s"key$i", s"msg$i"))
          client   <- KafkaTestUtils.makeAdminClient
          _        <- client.createTopics(List(AdminClient.NewTopic(topic, 1, 1)))
          producer <- KafkaTestUtils.makeProducer
          _        <- KafkaTestUtils.produceMany(producer, topic, kvs)
          _        <- consumeAndCommit(msgConsume.toLong, topic, groupId)
          _        <- client.deleteConsumerGroups(Chunk.single(groupId))
          offsets <- client.listConsumerGroupOffsets(
                       Map(groupId -> ListConsumerGroupOffsetsSpec(Chunk.single(TopicPartition(topic, 0))))
                     )
        } yield assert(offsets.get(groupId).flatMap(_.get(TopicPartition(topic, 0))).map(_.offset))(isNone)
      },
      test("describe consumer groups") {
        for {
          topicName   <- randomTopic
          client      <- KafkaTestUtils.makeAdminClient
          _           <- client.createTopic(AdminClient.NewTopic(topicName, numPartitions = 10, replicationFactor = 1))
          groupId     <- randomGroup
          _           <- consumeNoop(topicName, groupId, "consumer1").fork
          _           <- consumeNoop(topicName, groupId, "consumer2").fork
          description <- getStableConsumerGroupDescription(client, groupId)
        } yield assertTrue(
          description.groupId == groupId,
          description.members.length == 2
        )
      },
      test("remove members from consumer groups") {
        for {
          topicName   <- randomTopic
          client      <- KafkaTestUtils.makeAdminClient
          _           <- client.createTopic(AdminClient.NewTopic(topicName, numPartitions = 10, replicationFactor = 1))
          groupId     <- randomGroup
          _           <- consumeNoop(topicName, groupId, "consumer1", Some("instance1")).fork
          consumer2   <- consumeNoop(topicName, groupId, "consumer2", Some("instance2")).fork
          _           <- getStableConsumerGroupDescription(client, groupId)
          _           <- consumer2.interrupt
          _           <- client.removeMembersFromConsumerGroup(groupId, Set("instance2"))
          description <- getStableConsumerGroupDescription(client, groupId)
        } yield assertTrue(
          description.groupId == groupId,
          description.members.length == 1
        )
      },
      test("describe log dirs") {
        for {
          topicName <- randomTopic
          client    <- KafkaTestUtils.makeAdminClient
          _         <- client.createTopic(AdminClient.NewTopic(topicName, numPartitions = 1, replicationFactor = 1))
          node      <- client.describeClusterNodes().head.orElseFail(new NoSuchElementException())
          logDirs   <- client.describeLogDirs(List(node.id))
        } yield assert(logDirs)(
          hasKey(
            node.id,
            hasValues(exists(hasField("replicaInfos", _.replicaInfos, hasKey(TopicPartition(topicName, 0)))))
          )
        )
      },
      test("describe log dirs async") {
        for {
          topicName <- randomTopic
          client    <- KafkaTestUtils.makeAdminClient
          _         <- client.createTopic(AdminClient.NewTopic(topicName, numPartitions = 1, replicationFactor = 1))
          node      <- client.describeClusterNodes().head.orElseFail(new NoSuchElementException())
          logDirs <-
            client.describeLogDirsAsync(List(node.id)).flatMap { descriptions =>
              ZIO.foreachPar(descriptions) { case (brokerId, descriptionAsync) =>
                descriptionAsync.map(description => (brokerId, description))
              }
            }
        } yield assert(logDirs)(
          hasKey(
            node.id,
            hasValues(exists(hasField("replicaInfos", _.replicaInfos, hasKey(TopicPartition(topicName, 0)))))
          )
        )
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
          assertTrue(AdminClient.Node.apply(jNode).map(_.asJava) == Some(jNode))
        }
      },
      test("will replace invalid port by None") {
        val posIntGen = Gen.int(0, Int.MaxValue)
        check(posIntGen, Gen.string1(Gen.char), Gen.int, Gen.option(Gen.string)) { (id, host, port, rack) =>
          val jNode = new JNode(id, host, port, rack.orNull)
          assertTrue(AdminClient.Node.apply(jNode).map(_.port.isEmpty) == Some(port < 0))
        }
      },
      test("will replace empty host by None") {
        val jNode = new JNode(0, "", 9092, null)
        assertTrue(AdminClient.Node.apply(jNode).map(_.host.isEmpty) == Some(true))
      },
      test("incremental alter configs") {
        for {
          topicName <- randomTopic
          client    <- KafkaTestUtils.makeAdminClient
          _         <- client.createTopic(AdminClient.NewTopic(topicName, numPartitions = 1, replicationFactor = 1))

          configEntry    = new ConfigEntry("retention.ms", "1")
          configResource = ConfigResource(ConfigResourceType.Topic, topicName)

          setAlterConfigOp = AlterConfigOp(configEntry, AlterConfigOpType.Set)
          _ <- client.incrementalAlterConfigs(Map(configResource -> Seq(setAlterConfigOp)), AlterConfigsOptions())
          updatedConfigsWithUpdate <- client.describeConfigs(Seq(ConfigResource(ConfigResourceType.Topic, topicName)))

          deleteAlterConfigOp = AlterConfigOp(configEntry, AlterConfigOpType.Delete)
          _ <- client.incrementalAlterConfigs(Map(configResource -> Seq(deleteAlterConfigOp)), AlterConfigsOptions())
          updatedConfigsWithDelete <- client.describeConfigs(Seq(ConfigResource(ConfigResourceType.Topic, topicName)))
        } yield {
          val updatedRetentionMsConfig =
            updatedConfigsWithUpdate.get(configResource).flatMap(_.entries.get("retention.ms"))
          val deleteRetentionMsConfig =
            updatedConfigsWithDelete.get(configResource).flatMap(_.entries.get("retention.ms"))
          assert(updatedRetentionMsConfig.map(_.value()))(isSome(equalTo("1"))) &&
          assert(updatedRetentionMsConfig.map(_.source()))(isSome(equalTo(ConfigSource.DYNAMIC_TOPIC_CONFIG))) &&
          assert(deleteRetentionMsConfig.map(_.value()))(isSome(equalTo("604800000"))) &&
          assert(deleteRetentionMsConfig.map(_.source()))(isSome(equalTo(ConfigSource.DEFAULT_CONFIG)))
        }
      },
      test("incremental alter configs async") {
        for {
          topicName <- randomTopic
          client    <- KafkaTestUtils.makeAdminClient
          _         <- client.createTopic(AdminClient.NewTopic(topicName, numPartitions = 1, replicationFactor = 1))

          configEntry    = new ConfigEntry("retention.ms", "1")
          configResource = ConfigResource(ConfigResourceType.Topic, topicName)

          setAlterConfigOp = AlterConfigOp(configEntry, AlterConfigOpType.Set)
          setResult <-
            client
              .incrementalAlterConfigsAsync(Map(configResource -> Seq(setAlterConfigOp)), AlterConfigsOptions())
              .flatMap { configsAsync =>
                ZIO.foreachPar(configsAsync) { case (configResource, unitAsync) =>
                  unitAsync.map(unit => (configResource, unit))
                }
              }
          updatedConfigsWithUpdate <- client.describeConfigs(Seq(ConfigResource(ConfigResourceType.Topic, topicName)))

          deleteAlterConfigOp = AlterConfigOp(configEntry, AlterConfigOpType.Delete)
          deleteResult <-
            client
              .incrementalAlterConfigsAsync(Map(configResource -> Seq(deleteAlterConfigOp)), AlterConfigsOptions())
              .flatMap { configsAsync =>
                ZIO.foreachPar(configsAsync) { case (configResource, unitAsync) =>
                  unitAsync.map(unit => (configResource, unit))
                }
              }
          updatedConfigsWithDelete <- client.describeConfigs(Seq(ConfigResource(ConfigResourceType.Topic, topicName)))
        } yield {
          val updatedRetentionMsConfig =
            updatedConfigsWithUpdate.get(configResource).flatMap(_.entries.get("retention.ms"))
          val deleteRetentionMsConfig =
            updatedConfigsWithDelete.get(configResource).flatMap(_.entries.get("retention.ms"))
          assert(updatedRetentionMsConfig.map(_.value()))(isSome(equalTo("1"))) &&
          assert(updatedRetentionMsConfig.map(_.source()))(isSome(equalTo(ConfigSource.DYNAMIC_TOPIC_CONFIG))) &&
          assert(deleteRetentionMsConfig.map(_.value()))(isSome(equalTo("604800000"))) &&
          assert(deleteRetentionMsConfig.map(_.source()))(isSome(equalTo(ConfigSource.DEFAULT_CONFIG))) &&
          assert(setResult)(equalTo(Map((configResource, ())))) &&
          assert(deleteResult)(equalTo(Map((configResource, ()))))
        }
      },
      test("ACLs") {
        for {
          client <- KafkaTestUtils.makeAdminClient
          topic  <- randomTopic
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
          _ <- client.createAcls(bindings)
          createdAcls <-
            client
              .describeAcls(AclBindingFilter(ResourcePatternFilter.Any, AccessControlEntryFilter.Any))
              .repeatWhile(_.isEmpty) // because the createAcls is executed async by the broker
              .timeoutFail(new TimeoutException())(100.millis)
          deletedAcls <-
            client
              .deleteAcls(Set(AclBindingFilter(ResourcePatternFilter.Any, AccessControlEntryFilter.Any)))
          remainingAcls <-
            client
              .describeAcls(AclBindingFilter(ResourcePatternFilter.Any, AccessControlEntryFilter.Any))
              .repeatWhile(_.nonEmpty) // because the deleteAcls is executed async by the broker
              .timeoutFail(new TimeoutException())(100.millis)

        } yield assert(createdAcls)(equalTo(bindings)) &&
          assert(deletedAcls)(equalTo(bindings)) &&
          assert(remainingAcls)(equalTo(Set.empty[AclBinding]))
      }
    ).provideSomeShared[Scope](Kafka.embedded) @@ withLiveClock @@ timeout(2.minutes)

  private def consumeNoop(
    topicName: String,
    groupId: String,
    clientId: String,
    groupInstanceId: Option[String] = None
  ): ZIO[Kafka, Throwable, Unit] =
    ZIO.scoped {
      for {
        consumer <- KafkaTestUtils.makeConsumer(clientId, Some(groupId), groupInstanceId)
        _ <- consumer
               .plainStream(Subscription.topics(topicName), Serde.string, Serde.string)
               .foreach(_.offset.commit)
      } yield ()
    }

  private def getStableConsumerGroupDescription(
    adminClient: AdminClient,
    groupId: String
  ): ZIO[Any, Throwable, ConsumerGroupDescription] =
    adminClient
      .describeConsumerGroups(groupId)
      .map(_.head._2)
      .repeat(
        (Schedule.recurs(5) && Schedule.fixed(Duration.fromMillis(500)) && Schedule
          .recurUntil[ConsumerGroupDescription](
            _.state == AdminClient.GroupState.Stable
          )).map(_._3)
      )
      .flatMap(desc =>
        if (desc.state == AdminClient.GroupState.Stable) {
          ZIO.succeed(desc)
        } else {
          ZIO.fail(new IllegalStateException(s"Client is not in stable state: $desc"))
        }
      )
}
