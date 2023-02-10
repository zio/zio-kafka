package zio.kafka.admin

import org.apache.kafka.clients.admin.ConfigEntry.ConfigSource
import org.apache.kafka.clients.admin.{ ConfigEntry, RecordsToDelete }
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.{ Node => JNode }
import zio.kafka.{ KafkaTestUtils, ZIOKafkaSpec }
import zio.kafka.KafkaTestUtils._
import zio.kafka.admin.AdminClient.{
  AlterConfigOp,
  AlterConfigOpType,
  AlterConfigsOptions,
  ConfigResource,
  ConfigResourceType,
  ConsumerGroupDescription,
  ConsumerGroupState,
  KafkaConfig,
  ListConsumerGroupOffsetsSpec,
  ListConsumerGroupsOptions,
  OffsetAndMetadata,
  OffsetSpec,
  TopicPartition
}
import zio.kafka.admin.acl._
import zio.kafka.consumer.{ CommittableRecord, Consumer, OffsetBatch, Subscription }
import zio.kafka.embedded.Kafka
import zio.kafka.serde.Serde
import zio.stream.ZSink
import zio.test.Assertion._
import zio.test.TestAspect._
import zio.test._
import zio._
import zio.kafka.admin.resource.{ PatternType, ResourcePattern, ResourcePatternFilter, ResourceType }

import java.util.UUID
import java.util.concurrent.TimeoutException

object AdminSpec extends ZIOKafkaSpec {

  override val kafkaPrefix: String = "adminspec"

  private def listTopicsFiltered(client: AdminClient): ZIO[Any, Throwable, Map[String, AdminClient.TopicListing]] =
    client.listTopics().map(_.filter { case (key, _) => key.startsWith("adminspec-") })

  override def spec: Spec[TestEnvironment with Kafka with Scope, Throwable] =
    suite("client admin test")(
      test("create, list, delete single topic") {
        KafkaTestUtils.withAdmin { client =>
          for {
            list1 <- listTopicsFiltered(client)
            _     <- client.createTopic(AdminClient.NewTopic("adminspec-topic1", 1, 1))
            list2 <- listTopicsFiltered(client)
            _     <- client.deleteTopic("adminspec-topic1")
            list3 <- listTopicsFiltered(client)
          } yield assert(list1.size)(equalTo(0)) &&
            assert(list2.size)(equalTo(1)) &&
            assert(list3.size)(equalTo(0))

        }
      },
      test("create, list, delete multiple topic") {
        KafkaTestUtils.withAdmin { client =>
          for {
            list1 <- listTopicsFiltered(client)
            _ <- client.createTopics(
                   List(AdminClient.NewTopic("adminspec-topic2", 1, 1), AdminClient.NewTopic("adminspec-topic3", 4, 1))
                 )
            list2 <- listTopicsFiltered(client)
            _     <- client.deleteTopic("adminspec-topic2")
            list3 <- listTopicsFiltered(client)
            _     <- client.deleteTopic("adminspec-topic3")
            list4 <- listTopicsFiltered(client)
          } yield assert(list1.size)(equalTo(0)) &&
            assert(list2.size)(equalTo(2)) &&
            assert(list3.size)(equalTo(1)) &&
            assert(list4.size)(equalTo(0))

        }
      },
      test("just list") {
        KafkaTestUtils.withAdmin { client =>
          for {
            list1 <- listTopicsFiltered(client)
          } yield assert(list1.size)(equalTo(0))

        }
      },
      test("create, describe, delete multiple topic") {
        KafkaTestUtils.withAdmin { client =>
          for {
            list1 <- listTopicsFiltered(client)
            _ <- client.createTopics(
                   List(AdminClient.NewTopic("adminspec-topic4", 1, 1), AdminClient.NewTopic("adminspec-topic5", 4, 1))
                 )
            descriptions <- client.describeTopics(List("adminspec-topic4", "adminspec-topic5"))
            _            <- client.deleteTopics(List("adminspec-topic4", "adminspec-topic5"))
            list3        <- listTopicsFiltered(client)
          } yield assert(list1.size)(equalTo(0)) &&
            assert(descriptions.size)(equalTo(2)) &&
            assert(list3.size)(equalTo(0))

        }
      },
      test("create, describe topic config, delete multiple topic") {
        KafkaTestUtils.withAdmin { client =>
          for {
            list1 <- listTopicsFiltered(client)
            _ <- client.createTopics(
                   List(AdminClient.NewTopic("adminspec-topic6", 1, 1), AdminClient.NewTopic("adminspec-topic7", 4, 1))
                 )
            configResources = List(
                                ConfigResource(ConfigResourceType.Topic, "adminspec-topic6"),
                                ConfigResource(ConfigResourceType.Topic, "adminspec-topic7")
                              )
            configs <- client.describeConfigs(configResources) <&>
                         client.describeConfigsAsync(configResources).flatMap { configs =>
                           ZIO.foreachPar(configs) { case (resource, configTask) =>
                             configTask.map(config => (resource, config))
                           }
                         }
            _     <- client.deleteTopics(List("adminspec-topic6", "adminspec-topic7"))
            list3 <- listTopicsFiltered(client)
          } yield assert(list1.size)(equalTo(0)) &&
            assert(configs._1.size)(equalTo(2)) &&
            assert(configs._2.size)(equalTo(2)) &&
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
      test("describe broker config async") {
        KafkaTestUtils.withAdmin { client =>
          for {
            configTasks <- client.describeConfigsAsync(
                             List(
                               ConfigResource(ConfigResourceType.Broker, "0")
                             )
                           )
            configs <- ZIO.foreachPar(configTasks) { case (resource, configTask) =>
                         configTask.map(config => (resource, config))
                       }
          } yield assertTrue(configs.size == 1)
        }
      },
      test("list offsets") {
        KafkaTestUtils.withAdmin { client =>
          val topic    = "adminspec-topic8"
          val msgCount = 20
          val kvs      = (1 to msgCount).toList.map(i => (s"key$i", s"msg$i"))

          for {
            _ <- client.createTopics(List(AdminClient.NewTopic("adminspec-topic8", 3, 1)))
            _ <- produceMany(topic, kvs).provideSomeLayer[Kafka](KafkaTestUtils.producer)
            offsets <- client.listOffsets(
                         (0 until 3).map(i => TopicPartition(topic, i) -> OffsetSpec.LatestSpec).toMap
                       )
          } yield assert(offsets.values.map(_.offset).sum)(equalTo(msgCount.toLong))
        }
      },
      test("list offsets async") {
        KafkaTestUtils.withAdmin { client =>
          val topic    = "adminspec-topic9"
          val msgCount = 20
          val kvs      = (1 to msgCount).toList.map(i => (s"key$i", s"msg$i"))

          for {
            _ <- client.createTopics(List(AdminClient.NewTopic("adminspec-topic9", 3, 1)))
            _ <- produceMany(topic, kvs).provideSomeLayer[Kafka](KafkaTestUtils.producer)
            offsetTasks <- client.listOffsetsAsync(
                             (0 until 3).map(i => TopicPartition(topic, i) -> OffsetSpec.LatestSpec).toMap
                           )
            offsets <- ZIO.foreachPar(offsetTasks) { case (topicPartition, offsetTask) =>
                         offsetTask.map((topicPartition, _))
                       }
          } yield assert(offsets.values.map(_.offset).sum)(equalTo(msgCount.toLong))
        }
      },
      test("alter offsets") {
        KafkaTestUtils.withAdmin { client =>
          val topic            = "adminspec-topic10"
          val consumerGroupID  = "adminspec-topic10"
          val partitionCount   = 3
          val msgCount         = 20
          val partitionResetBy = 2

          val p   = (0 until partitionCount).map(i => TopicPartition("adminspec-topic10", i))
          val kvs = (1 to msgCount).toList.map(i => (s"key$i", s"msg$i"))

          def consumeAndCommit(count: Long) =
            Consumer
              .subscribeAnd(Subscription.Topics(Set(topic)))
              .partitionedStream[Kafka, String, String](Serde.string, Serde.string)
              .flatMapPar(partitionCount)(_._2)
              .take(count)
              .transduce(ZSink.collectAllN[CommittableRecord[String, String]](20))
              .mapConcatZIO { committableRecords =>
                val records = committableRecords.map(_.record)
                val offsetBatch =
                  committableRecords.foldLeft(OffsetBatch.empty)(_ merge _.offset)

                offsetBatch.commit.as(records)
              }
              .runCollect
              .provideSomeLayer[Kafka](consumer("adminspec-topic10", Some(consumerGroupID)))

          def toMap(records: Chunk[ConsumerRecord[String, String]]): Map[Int, List[(Long, String, String)]] =
            records.toList
              .map(r => (r.partition(), (r.offset(), r.key(), r.value())))
              .groupBy(_._1)
              .map { case (k, vs) => (k, vs.map(_._2).sortBy(_._1)) }

          for {
            _          <- client.createTopics(List(AdminClient.NewTopic(topic, partitionCount, 1)))
            _          <- produceMany(topic, kvs).provideSomeLayer[Kafka](KafkaTestUtils.producer)
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
          } yield assert(recordsAfterAltering(0))(equalTo(records(0))) &&
            assert(records(1).take(endOffsets(p(1)).offset.toInt - partitionResetBy) ++ recordsAfterAltering(1))(
              equalTo(records(1))
            ) &&
            assert(recordsAfterAltering.get(2))(isNone)
        }
      },
      test("create, produce, delete records from a topic") {
        KafkaTestUtils.withAdmin { client =>
          type Env = Kafka
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
            .plainStream[Kafka, String, String](Serde.string, Serde.string)
            .take(count)
            .foreach(_.offset.commit)
            .provideSomeLayer[Kafka](consumer(topic, Some(groupId)))

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
            _ <- produceMany(topic, kvs).provideSomeLayer[Kafka](KafkaTestUtils.producer)
            _ <- consumeAndCommit(msgConsume.toLong, topic, groupId)
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
          } yield assert(offsets.get(TopicPartition(topic, 0)).map(_.offset))(isSome(equalTo(msgConsume.toLong))) &&
            assert(invalidTopicOffsets)(isEmpty) &&
            assert(invalidTpOffsets)(isEmpty) &&
            assert(invalidGroupIdOffsets)(isEmpty)
        }
      },
      test("delete consumer group offsets") {

        def consumeAndCommit(count: Long, topic: String, groupId: String) =
          Consumer
            .subscribeAnd(Subscription.Topics(Set(topic)))
            .plainStream[Kafka, String, String](Serde.string, Serde.string)
            .take(count)
            .foreach(_.offset.commit)
            .provideSomeLayer[Kafka](consumer(topic, Some(groupId)))

        KafkaTestUtils.withAdmin { client =>
          for {
            topic   <- randomTopic
            groupId <- randomGroup
            msgCount   = 20
            msgConsume = 15
            kvs        = (1 to msgCount).toList.map(i => (s"key$i", s"msg$i"))
            _ <- client.createTopics(List(AdminClient.NewTopic(topic, 1, 1)))
            _ <- produceMany(topic, kvs).provideSomeLayer[Kafka](KafkaTestUtils.producer)
            _ <- consumeAndCommit(msgConsume.toLong, topic, groupId)
            _ <- client.deleteConsumerGroups(Chunk.single(groupId))
            offsets <- client.listConsumerGroupOffsets(
                         Map(groupId -> ListConsumerGroupOffsetsSpec(Chunk.single(TopicPartition(topic, 0))))
                       )
          } yield assert(offsets.get(TopicPartition(topic, 0)).map(_.offset))(isNone)
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
      test("describe log dirs async") {
        KafkaTestUtils.withAdmin { implicit admin =>
          for {
            topicName <- randomTopic
            _         <- admin.createTopic(AdminClient.NewTopic(topicName, numPartitions = 1, replicationFactor = 1))
            node      <- admin.describeClusterNodes().head.orElseFail(new NoSuchElementException())
            logDirs <-
              admin.describeLogDirsAsync(List(node.id)).flatMap { descriptions =>
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
      },
      test("incremental alter configs") {
        KafkaTestUtils.withAdmin { implicit admin =>
          for {
            topicName <- randomTopic
            _         <- admin.createTopic(AdminClient.NewTopic(topicName, numPartitions = 1, replicationFactor = 1))

            configEntry    = new ConfigEntry("retention.ms", "1")
            configResource = ConfigResource(ConfigResourceType.Topic, topicName)

            setAlterConfigOp = AlterConfigOp(configEntry, AlterConfigOpType.Set)
            _ <- admin.incrementalAlterConfigs(Map(configResource -> Seq(setAlterConfigOp)), AlterConfigsOptions())
            updatedConfigsWithUpdate <- admin.describeConfigs(Seq(ConfigResource(ConfigResourceType.Topic, topicName)))

            deleteAlterConfigOp = AlterConfigOp(configEntry, AlterConfigOpType.Delete)
            _ <- admin.incrementalAlterConfigs(Map(configResource -> Seq(deleteAlterConfigOp)), AlterConfigsOptions())
            updatedConfigsWithDelete <- admin.describeConfigs(Seq(ConfigResource(ConfigResourceType.Topic, topicName)))
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
        }
      },
      test("incremental alter configs async") {
        KafkaTestUtils.withAdmin { implicit admin =>
          for {
            topicName <- randomTopic
            _         <- admin.createTopic(AdminClient.NewTopic(topicName, numPartitions = 1, replicationFactor = 1))

            configEntry    = new ConfigEntry("retention.ms", "1")
            configResource = ConfigResource(ConfigResourceType.Topic, topicName)

            setAlterConfigOp = AlterConfigOp(configEntry, AlterConfigOpType.Set)
            setResult <-
              admin
                .incrementalAlterConfigsAsync(Map(configResource -> Seq(setAlterConfigOp)), AlterConfigsOptions())
                .flatMap { configsAsync =>
                  ZIO.foreachPar(configsAsync) { case (configResource, unitAsync) =>
                    unitAsync.map(unit => (configResource, unit))
                  }
                }
            updatedConfigsWithUpdate <- admin.describeConfigs(Seq(ConfigResource(ConfigResourceType.Topic, topicName)))

            deleteAlterConfigOp = AlterConfigOp(configEntry, AlterConfigOpType.Delete)
            deleteResult <-
              admin
                .incrementalAlterConfigsAsync(Map(configResource -> Seq(deleteAlterConfigOp)), AlterConfigsOptions())
                .flatMap { configsAsync =>
                  ZIO.foreachPar(configsAsync) { case (configResource, unitAsync) =>
                    unitAsync.map(unit => (configResource, unit))
                  }
                }
            updatedConfigsWithDelete <- admin.describeConfigs(Seq(ConfigResource(ConfigResourceType.Topic, topicName)))
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
        }
      },
      test("alter configs") {
        KafkaTestUtils.withAdmin { implicit admin =>
          for {
            topicName <- randomTopic
            _         <- admin.createTopic(AdminClient.NewTopic(topicName, numPartitions = 1, replicationFactor = 1))

            configEntry    = new ConfigEntry("retention.ms", "1")
            configResource = ConfigResource(ConfigResourceType.Topic, topicName)

            kafkaConfig = KafkaConfig(Map(topicName -> configEntry))
            _                        <- admin.alterConfigs(Map(configResource -> kafkaConfig), AlterConfigsOptions())
            updatedConfigsWithUpdate <- admin.describeConfigs(Seq(ConfigResource(ConfigResourceType.Topic, topicName)))

            emptyKafkaConfig = KafkaConfig(Map.empty[String, ConfigEntry])
            _ <- admin.alterConfigs(Map(configResource -> emptyKafkaConfig), AlterConfigsOptions())
            updatedConfigsWithDelete <- admin.describeConfigs(Seq(ConfigResource(ConfigResourceType.Topic, topicName)))
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
        }
      },
      test("alter configs async") {
        KafkaTestUtils.withAdmin { implicit admin =>
          for {
            topicName <- randomTopic
            _         <- admin.createTopic(AdminClient.NewTopic(topicName, numPartitions = 1, replicationFactor = 1))

            configEntry    = new ConfigEntry("retention.ms", "1")
            configResource = ConfigResource(ConfigResourceType.Topic, topicName)

            kafkaConfig = KafkaConfig(Map(topicName -> configEntry))
            setResult <-
              admin
                .alterConfigsAsync(Map(configResource -> kafkaConfig), AlterConfigsOptions())
                .flatMap { configsAsync =>
                  ZIO.foreachPar(configsAsync) { case (configResource, unitAsync) =>
                    unitAsync.map(unit => (configResource, unit))
                  }
                }
            updatedConfigsWithUpdate <- admin.describeConfigs(Seq(ConfigResource(ConfigResourceType.Topic, topicName)))

            emptyKafkaConfig = KafkaConfig(Map.empty[String, ConfigEntry])
            deleteResult <-
              admin
                .alterConfigsAsync(Map(configResource -> emptyKafkaConfig), AlterConfigsOptions())
                .flatMap { configsAsync =>
                  ZIO.foreachPar(configsAsync) { case (configResource, unitAsync) =>
                    unitAsync.map(unit => (configResource, unit))
                  }
                }
            updatedConfigsWithDelete <- admin.describeConfigs(Seq(ConfigResource(ConfigResourceType.Topic, topicName)))
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
        }
      },
      test("ACLs") {
        KafkaTestUtils.withAdmin { client =>
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
      }
    ) @@ withLiveClock @@ sequential

  private def consumeNoop(
    topicName: String,
    groupId: String,
    clientId: String,
    groupInstanceId: Option[String] = None
  ): ZIO[Kafka, Throwable, Unit] = Consumer
    .subscribeAnd(Subscription.topics(topicName))
    .plainStream(Serde.string, Serde.string)
    .foreach(_.offset.commit)
    .provideSomeLayer(consumer(clientId, Some(groupId), groupInstanceId))

  private def getStableConsumerGroupDescription(
    groupId: String
  )(implicit adminClient: AdminClient): ZIO[Any, Throwable, ConsumerGroupDescription] =
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
