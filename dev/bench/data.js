window.BENCHMARK_DATA = {
  "lastUpdate": 1759735371625,
  "repoUrl": "https://github.com/zio/zio-kafka",
  "entries": {
    "JMH Benchmark": [
      {
        "commit": {
          "author": {
            "email": "e.vanoosten@grons.nl",
            "name": "Erik van Oosten",
            "username": "erikvanoosten"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "5be9d97ecfa897a0ef9606f73aa889bfcaaf6ab5",
          "message": "Provide diagnostics via `ConsumerSettings` (#1515)\n\nSince most people do not use consumer diagnostics, it is a bit annoying\nthat everybody has to provide a (dummy) diagnostics layer when the\nzio-kafka `Consumer` is constructed via a layer.\n\nThis is solved by moving the configuration of diagnostics to\n`ConsumerSettings`.\n\nThis approach also makes it consistent with the to-be-introduced\n`ProducerDiagnostics`.",
          "timestamp": "2025-04-20T09:06:10+02:00",
          "tree_id": "099a9566f695d4263e945afdc812ae052954a3c5",
          "url": "https://github.com/zio/zio-kafka/commit/5be9d97ecfa897a0ef9606f73aa889bfcaaf6ab5"
        },
        "date": 1745133909567,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 584.77873774,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 586.1322713999999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 251.55885628200002,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 477.9895686733335,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordPar",
            "value": 72.20893132268799,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordSeq",
            "value": 85.86833977792081,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 538.64449394,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 532.62852108,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 558.2812813000002,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 563.6081307599999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "e.vanoosten@grons.nl",
            "name": "Erik van Oosten",
            "username": "erikvanoosten"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "8b264a81d9945d3e7927b5ec11a7af43d60129fd",
          "message": "Add producer diagnostics (#1518)\n\nProducer diagnostics allows the user to keep track of what happens in\nthe producer. It gives information on where records are produced to, and\nthe size of each record.\n\nBenchmarks show no overhead when these new producer diagnostics are\ndisabled. Enabling the new producer diagnostics should increase heap\nusage somewhat, but we did not see any effects in the benchmark.\n\nAlso: improve scaladoc of `ProducerSettings`",
          "timestamp": "2025-04-20T10:38:37+02:00",
          "tree_id": "e7598a21122f79c186db5e98c11ad44a2e488aeb",
          "url": "https://github.com/zio/zio-kafka/commit/8b264a81d9945d3e7927b5ec11a7af43d60129fd"
        },
        "date": 1745139432197,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 585.84376164,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 587.5323649000001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 222.54628813609528,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 530.2360483600002,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordPar",
            "value": 83.71238466042107,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordSeq",
            "value": 101.55228633001244,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 537.0041852000002,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 530.7148279799999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 555.79224878,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 565.9688816999999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "svroonland@users.noreply.github.com",
            "name": "svroonland",
            "username": "svroonland"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "98bd1649e8ae590c806eda24bd83614f0d8ca9c3",
          "message": "Graceful shutdown of a stream for a single subscription (#1201)\n\nImplements functionality for gracefully stopping a stream for a single\nsubscription: stop fetching records for the assigned topic-partitions\nbut keep being subscribed so that offsets can still be committed.\nIntended to replace `stopConsumption`, which did not support\nmultiple-subscription use cases.\n\nA new command `EndStreamsBySubscription` is introduced, which calls the\n`end` method on the `PartitionStreamControl` of streams matching a\nsubscription. In the method `Consumer#runWithGracefulShutdown` we then\nwait for the user's stream to complete, before removing the\nsubscription.\n\nThis is experimental functionality, intended to replace\n`stopConsumption` at some point. Methods with this new functionality are\noffered besides existing methods to maintain compatibility.\n\n~All the fiber and scope trickery proved to be very hard to get right\n(the lifetime of this PR is a testimony to that), and there may still be\nsubtle issues here.~ This is now traced back to issue\nhttps://github.com/zio/zio/issues/9288\n\nImplements some of #941.\n\n---------\n\nCo-authored-by: Erik van Oosten <e.vanoosten@grons.nl>\nCo-authored-by: zio-scala-steward[bot] <145262613+zio-scala-steward[bot]@users.noreply.github.com>",
          "timestamp": "2025-04-20T12:55:05+02:00",
          "tree_id": "c391957cdf5418525223b392dc8f2ce43510d05d",
          "url": "https://github.com/zio/zio-kafka/commit/98bd1649e8ae590c806eda24bd83614f0d8ca9c3"
        },
        "date": 1745147591644,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 585.66080766,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 585.4197404800001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 141.4737039135714,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 388.3990154366667,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordPar",
            "value": 63.275494722911816,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordSeq",
            "value": 77.76491910087833,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 535.6925727800001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 530.3008597999999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 555.81538304,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 562.8834029000002,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "145262613+zio-scala-steward[bot]@users.noreply.github.com",
            "name": "zio-scala-steward[bot]",
            "username": "zio-scala-steward[bot]"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "8e012e7cdd1b44c242ccbde157e5737cf0a93797",
          "message": "Update kafka-clients and embedded-kafka to 4.0.0 (#1504)\n\nUpdate zio-kafka to kafka 4.0.0.\n\nThis has some consequences:\n- Java 11 support is dropped. Even though the kafka client library still\nsupports java 11, zio-kafka's unit tests depend on an embedded kafka\nbroker and the kafka brokers no longer support it.\n- `ConsumerGroupState` (used in the admin client) was renamed to\n`GroupState`.\n- Due to\n[KAFKA-18818](https://issues.apache.org/jira/browse/KAFKA-18818) we add\na 550ms delay after operations like `createTopic`, `deleteTopic` and\n`createACL` in the admin client.\n- Two deprecated methods of kafka's admin client were removed. Their\nequivalent in zio-kafka were unfortunately not deprecated yet but had to\nbe removed as well:\n- `AdminClient.alterConfigs` was removed, use\n`AdminClient.incrementalAlterConfigs` instead.\n- `AdminClient.alterConfigsAsync` was removed, use\n`AdminClient.incrementalAlterConfigs` instead.\n- Please check the kafka release notes for more changes.\n\n📦 Updates [org.apache.kafka:kafka-clients](https://kafka.apache.org)\nfrom `3.9.0` to `4.0.0`\n📦 Updates\n[io.github.embeddedkafka:embedded-kafka](https://github.com/embeddedkafka/embedded-kafka)\nfrom `3.9.0` to `4.0.1`\n\n---------\n\nCo-authored-by: zio-scala-steward[bot] <145262613+zio-scala-steward[bot]@users.noreply.github.com>\nCo-authored-by: Steven Vroonland <svroonland@gmail.com>\nCo-authored-by: Erik van Oosten <e.vanoosten@grons.nl>\nCo-authored-by: Gaël Jourdan-Weil <gjourdanweil@gmail.com>",
          "timestamp": "2025-04-21T12:26:25+02:00",
          "tree_id": "1f64f38e31621012b740b14f53d0910763c5835d",
          "url": "https://github.com/zio/zio-kafka/commit/8e012e7cdd1b44c242ccbde157e5737cf0a93797"
        },
        "date": 1745232245853,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 600.4673682000001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 597.62803638,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 154.04943034285714,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 366.92449467333324,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordPar",
            "value": 63.271131834438116,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordSeq",
            "value": 76.88695064975117,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 545.08309468,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 532.93585768,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 561.26295294,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 570.1550366800001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "e.vanoosten@grons.nl",
            "name": "Erik van Oosten",
            "username": "erikvanoosten"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "17d6042d953b87c5bfb7955a7ea55dc3ec90e506",
          "message": "Make all benchmarks report average time (#1524)\n\nThere are 2 benchmarks that deviate from the others by reporting their\nresults in throughput (ops/s) i.s.o. average time (ms/op). With this\nchange all benchmarks use average time.",
          "timestamp": "2025-04-24T20:41:50+02:00",
          "tree_id": "c03592b3255d8cbe51f7ff3f49e32ad2d26e8517",
          "url": "https://github.com/zio/zio-kafka/commit/17d6042d953b87c5bfb7955a7ea55dc3ec90e506"
        },
        "date": 1745521188431,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 593.8499556800001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 607.61133822,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 250.7530167295238,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 366.4386969866666,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordPar",
            "value": 69.0291251014374,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordSeq",
            "value": 88.4312228861725,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 546.93629922,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 537.3463943800002,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 565.61782756,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 575.1267437399999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "talashkoy@gmail.com",
            "name": "Yurii Talashko",
            "username": "ytalashko"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "030dd17fd4120c68e36e90f86d60f8d15c9bf5fc",
          "message": "Add comment motivating use of lazy in publisher (#1527)\n\nhttps://github.com/zio/zio-kafka/pull/1437 introduced a clever way to\nretry records publication after specific errors (more details in the\nreferred PR).\nThere is a place of `lazy val` usage in that implementation, which may\nraise questions of why we need it, from the folks unfamiliar with the\nimplementation.\nTo add a bit more clarity, this PR adds a comment with the reasoning for\nusing `lazy val`.",
          "timestamp": "2025-05-03T20:55:54+02:00",
          "tree_id": "3a5b44adf320dce6218fd1bf67e70b271b9cc894",
          "url": "https://github.com/zio/zio-kafka/commit/030dd17fd4120c68e36e90f86d60f8d15c9bf5fc"
        },
        "date": 1746299591265,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 595.1808989599999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 594.16663716,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 115.69471552088888,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 318.96857423800003,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordPar",
            "value": 53.86245894309993,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordSeq",
            "value": 65.05232283384593,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 546.2591688,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 536.3902158,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 562.76863898,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 573.596646,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "e.vanoosten@grons.nl",
            "name": "Erik van Oosten",
            "username": "erikvanoosten"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "adfff39bdd86b75f6035bdfb90935f7800d30b52",
          "message": "Micro optimization by using `Chunk` (#1528)\n\nSmall optimization in Prodcuer by using `Chunk` instead of `Seq`.\n`Chunk` has much more efficient constructors.\n\nInspired by @ytalashko's work in #1527.",
          "timestamp": "2025-05-04T09:50:00+02:00",
          "tree_id": "508a442cbeae216084914a4be502b071b61f36a1",
          "url": "https://github.com/zio/zio-kafka/commit/adfff39bdd86b75f6035bdfb90935f7800d30b52"
        },
        "date": 1746346058332,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 592.92597972,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 595.04755142,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 168.04185136238095,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 410.2623221366666,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordPar",
            "value": 68.72234097982052,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordSeq",
            "value": 85.7966382236104,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 546.36513266,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 535.2837955,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 561.9196011,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 575.13743854,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "145262613+zio-scala-steward[bot]@users.noreply.github.com",
            "name": "zio-scala-steward[bot]",
            "username": "zio-scala-steward[bot]"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "f852d78432889b14749a41b4194da3899930e4b4",
          "message": "Update sbt-scalafix to 0.14.3 (#1532)\n\n📦 Updates\n[ch.epfl.scala:sbt-scalafix](https://github.com/scalacenter/sbt-scalafix)\nfrom `0.14.2` to `0.14.3`",
          "timestamp": "2025-05-09T10:20:06+02:00",
          "tree_id": "6137e905441f7e41ab017eaeb639b4f778a64a82",
          "url": "https://github.com/zio/zio-kafka/commit/f852d78432889b14749a41b4194da3899930e4b4"
        },
        "date": 1746779848844,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 594.31410728,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 600.1296850800001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 124.42239423222217,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 314.01961916,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordPar",
            "value": 54.97504735549776,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordSeq",
            "value": 68.25684676010363,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 547.03055026,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 536.32421288,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 563.10192128,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 576.2157516399999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "145262613+zio-scala-steward[bot]@users.noreply.github.com",
            "name": "zio-scala-steward[bot]",
            "username": "zio-scala-steward[bot]"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "a26b443765e5d308fba56350ab5d22a57ccc3af5",
          "message": "Update scala3-compiler, scala3-library, ... to 3.3.6 (#1533)\n\n📦 Updates \n* [org.scala-lang:scala3-compiler](https://github.com/scala/scala3)\n* [org.scala-lang:scala3-library](https://github.com/scala/scala3)\n* [org.scala-lang:tasty-core](https://github.com/scala/scala3)\n\n from `3.3.5` to `3.3.6`\n\nCo-authored-by: Erik van Oosten <e.vanoosten@grons.nl>",
          "timestamp": "2025-05-10T09:38:17+02:00",
          "tree_id": "0fb03d32130410fc79335e756f2026d0c10411f8",
          "url": "https://github.com/zio/zio-kafka/commit/a26b443765e5d308fba56350ab5d22a57ccc3af5"
        },
        "date": 1746863734058,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 595.4980472599999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 594.8545432799999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 122.05015928999997,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 308.832715148,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordPar",
            "value": 53.59116118614063,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordSeq",
            "value": 66.88816165238828,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 546.61270724,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 535.7297204,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 562.67401302,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 573.0450887999999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "145262613+zio-scala-steward[bot]@users.noreply.github.com",
            "name": "zio-scala-steward[bot]",
            "username": "zio-scala-steward[bot]"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "5ceaba11f474138c11bf0390beab19f81defb5a2",
          "message": "Update zio, zio-streams, zio-test, ... to 2.1.18 (#1534)\n\n📦 Updates \n* [dev.zio:zio](https://github.com/zio/zio)\n* [dev.zio:zio-streams](https://github.com/zio/zio)\n* [dev.zio:zio-test](https://github.com/zio/zio)\n* [dev.zio:zio-test-sbt](https://github.com/zio/zio)\n\n from `2.1.17` to `2.1.18`",
          "timestamp": "2025-05-17T08:07:06+02:00",
          "tree_id": "81343fd8bd246777f5a474552ffc2b2ecd246012",
          "url": "https://github.com/zio/zio-kafka/commit/5ceaba11f474138c11bf0390beab19f81defb5a2"
        },
        "date": 1747463067643,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 595.2441681200002,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 595.3539163600001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 124.92540397222223,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 336.6876946,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordPar",
            "value": 54.20039830703483,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordSeq",
            "value": 68.83025445785715,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 546.1315755600001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 535.4401534200001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 561.4554264799999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 572.7612698600001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "e.vanoosten@grons.nl",
            "name": "Erik van Oosten",
            "username": "erikvanoosten"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "1ca2542bd8fc96b47bef10af3e25fc830f12f5d9",
          "message": "Producer metrics (#1530)\n\nIntroduce zio-kafka specific metrics for the producer.\n\nSee the documentation for details about the added metrics.",
          "timestamp": "2025-05-22T21:56:00+02:00",
          "tree_id": "0231eb3b7949cf587fda2c1034c417e7c13b21fc",
          "url": "https://github.com/zio/zio-kafka/commit/1ca2542bd8fc96b47bef10af3e25fc830f12f5d9"
        },
        "date": 1747944839447,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 594.3228905999999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 595.5600056999999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 165.77967734523813,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 376.03243365000003,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordPar",
            "value": 68.53822581157644,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordSeq",
            "value": 89.06522517476989,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 548.1527232799999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 536.5598818600001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 563.6299258,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 575.88232416,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "e.vanoosten@grons.nl",
            "name": "Erik van Oosten",
            "username": "erikvanoosten"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "5c67a5eba570e6d3f693946542aaa70626f0d7b3",
          "message": "More and better producer benchmarks (#1531)\n\nThe producer benchmarks that do record by record synchronous producing,\ndo not benefit from lingering in the kafka layer. These are therefore\nsplit of to a separate file in which lingering is disabled.\n\nSecondly: add asynchronous producer benchmarks.\n\nAlso: use `schedule` instead of `repeatN`.",
          "timestamp": "2025-05-23T17:42:56+02:00",
          "tree_id": "88ae67de1a2c2a48893f606cbe45b7da560c78e1",
          "url": "https://github.com/zio/zio-kafka/commit/5c67a5eba570e6d3f693946542aaa70626f0d7b3"
        },
        "date": 1748016618603,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 595.228806,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 595.3982720799999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 93.85150695530538,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 278.58524813,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeqAsync",
            "value": 140.55640091388975,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeqAsync",
            "value": 0.38297874301594603,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordPar",
            "value": 65.2757976764513,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordSeq",
            "value": 84.30762444887446,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 546.4446944000001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 538.0900236399999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 562.2185700399999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 575.11117054,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "145262613+zio-scala-steward[bot]@users.noreply.github.com",
            "name": "zio-scala-steward[bot]",
            "username": "zio-scala-steward[bot]"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "13b46be91f976db7e9dab4ba81eddaded8f744f4",
          "message": "Update sbt, scripted-plugin to 1.11.0 (#1535)\n\n## About this PR\n📦 Updates \n* [org.scala-sbt:sbt](https://github.com/sbt/sbt)\n* [org.scala-sbt:scripted-plugin](https://github.com/sbt/sbt)\n\n from `1.10.11` to `1.11.0`\n\n📜 [GitHub Release\nNotes](https://github.com/sbt/sbt/releases/tag/v1.11.0) - [Version\nDiff](https://github.com/sbt/sbt/compare/v1.10.11...v1.11.0)\n\n## Usage\n✅ **Please merge!**\n\nI'll automatically update this PR to resolve conflicts as long as you\ndon't change it yourself.\n\nIf you'd like to skip this version, you can just close this PR. If you\nhave any feedback, just mention me in the comments below.\n\nConfigure Scala Steward for your repository with a\n[`.scala-steward.conf`](https://github.com/scala-steward-org/scala-steward/blob/f0dfaa6d8e24b261aeafbc7f99c5325ed9365cf2/docs/repo-specific-configuration.md)\nfile.\n\n_Have a fantastic day writing Scala!_\n\n<details>\n<summary>⚙ Adjust future updates</summary>\n\nAdd this to your `.scala-steward.conf` file to ignore future updates of\nthis dependency:\n```\nupdates.ignore = [ { groupId = \"org.scala-sbt\" } ]\n```\nOr, add this to slow down future updates of this dependency:\n```\ndependencyOverrides = [{\n  pullRequests = { frequency = \"30 days\" },\n  dependency = { groupId = \"org.scala-sbt\" }\n}]\n```\n</details>\n\n<sup>\nlabels: library-update, early-semver-minor, semver-spec-minor,\nversion-scheme:early-semver, commit-count:1\n</sup>\n\n<!-- scala-steward = {\n  \"Update\" : {\n    \"ForGroupId\" : {\n      \"forArtifactIds\" : [\n        {\n          \"ForArtifactId\" : {\n            \"crossDependency\" : [\n              {\n                \"groupId\" : \"org.scala-sbt\",\n                \"artifactId\" : {\n                  \"name\" : \"sbt\",\n                  \"maybeCrossName\" : null\n                },\n                \"version\" : \"1.10.11\",\n                \"sbtVersion\" : null,\n                \"scalaVersion\" : null,\n                \"configurations\" : null\n              }\n            ],\n            \"newerVersions\" : [\n              \"1.11.0\"\n            ],\n            \"newerGroupId\" : null,\n            \"newerArtifactId\" : null\n          }\n        },\n        {\n          \"ForArtifactId\" : {\n            \"crossDependency\" : [\n              {\n                \"groupId\" : \"org.scala-sbt\",\n                \"artifactId\" : {\n                  \"name\" : \"scripted-plugin\",\n                  \"maybeCrossName\" : \"scripted-plugin_2.12\"\n                },\n                \"version\" : \"1.10.11\",\n                \"sbtVersion\" : null,\n                \"scalaVersion\" : null,\n                \"configurations\" : null\n              }\n            ],\n            \"newerVersions\" : [\n              \"1.11.0\"\n            ],\n            \"newerGroupId\" : null,\n            \"newerArtifactId\" : null\n          }\n        }\n      ]\n    }\n  },\n  \"Labels\" : [\n    \"library-update\",\n    \"early-semver-minor\",\n    \"semver-spec-minor\",\n    \"version-scheme:early-semver\",\n    \"commit-count:1\"\n  ]\n} -->\n\nCo-authored-by: zio-scala-steward[bot] <145262613+zio-scala-steward[bot]@users.noreply.github.com>",
          "timestamp": "2025-05-25T09:31:49+02:00",
          "tree_id": "be58468dc5b4bf0f6b4669576515c4cf374d4747",
          "url": "https://github.com/zio/zio-kafka/commit/13b46be91f976db7e9dab4ba81eddaded8f744f4"
        },
        "date": 1748159594798,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 597.97390838,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 601.7504667600001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 95.96336592727275,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 301.35918642,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeqAsync",
            "value": 45.9829493597527,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeqAsync",
            "value": 0.39119077209914577,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordPar",
            "value": 63.40290756644581,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordSeq",
            "value": 82.64305715853479,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 550.15741186,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 538.66789766,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 566.61350578,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 577.48185732,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "e.vanoosten@grons.nl",
            "name": "Erik van Oosten",
            "username": "erikvanoosten"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "6a063974fc0bb4c11143742c3b2888273575046d",
          "message": "Improve type inference for `runWithGracefulShutdown` (#1536)\n\nParameter `withStream` is allowed to have a different environment type\nthan `streamControl` parameter. A `Scope` is always provided to\n`withStream`.\n\nAlso: better scaladoc layout.\n\nFixes #1526.",
          "timestamp": "2025-05-25T10:59:11+02:00",
          "tree_id": "96fb347502e05df224b663b7e55858c71ef73b18",
          "url": "https://github.com/zio/zio-kafka/commit/6a063974fc0bb4c11143742c3b2888273575046d"
        },
        "date": 1748164813339,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 593.5012779399999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 596.0319306199999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 89.46825426333334,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 281.08608896000004,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeqAsync",
            "value": 92.94928890070989,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeqAsync",
            "value": 0.3718516681424863,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordPar",
            "value": 62.42843484399487,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordSeq",
            "value": 82.17927634103229,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 547.0669617,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 537.4289311,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 563.04422822,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 573.74056158,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "145262613+zio-scala-steward[bot]@users.noreply.github.com",
            "name": "zio-scala-steward[bot]",
            "username": "zio-scala-steward[bot]"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "034d740819ff8ae21d29062fb7bc2d49f52b9ca6",
          "message": "Update zio, zio-streams, zio-test, ... to 2.1.19 (#1539)\n\n📦 Updates \n* [dev.zio:zio](https://github.com/zio/zio)\n* [dev.zio:zio-streams](https://github.com/zio/zio)\n* [dev.zio:zio-test](https://github.com/zio/zio)\n* [dev.zio:zio-test-sbt](https://github.com/zio/zio)\n\n from `2.1.18` to `2.1.19`",
          "timestamp": "2025-05-31T10:40:09+02:00",
          "tree_id": "9e725987d3419e9d2aad7c9b00a8e2d6a311ad34",
          "url": "https://github.com/zio/zio-kafka/commit/034d740819ff8ae21d29062fb7bc2d49f52b9ca6"
        },
        "date": 1748682550250,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 595.6030335800001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 595.9378027200001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 92.73069330242423,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 289.14800038,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeqAsync",
            "value": 34.45920887907757,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeqAsync",
            "value": 0.38757409265426906,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordPar",
            "value": 66.1901148974309,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordSeq",
            "value": 86.1266441288951,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 548.2551972,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 537.91480236,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 562.9027429399998,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 576.10492494,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "145262613+zio-scala-steward[bot]@users.noreply.github.com",
            "name": "zio-scala-steward[bot]",
            "username": "zio-scala-steward[bot]"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "3fdaf77800ca84a14edef20707b700c542c586f1",
          "message": "Update embedded-kafka to 4.0.1.1 (#1540)\n\n📦 Updates\n[io.github.embeddedkafka:embedded-kafka](https://github.com/embeddedkafka/embedded-kafka)\nfrom `4.0.1` to `4.0.1.1`",
          "timestamp": "2025-05-31T10:41:16+02:00",
          "tree_id": "a7ec4f6fe6edc9168e5010d9626e195210cde084",
          "url": "https://github.com/zio/zio-kafka/commit/3fdaf77800ca84a14edef20707b700c542c586f1"
        },
        "date": 1748682571376,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 597.74044366,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 596.38283842,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 96.51480022581818,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 295.65803312,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeqAsync",
            "value": 38.75289398418904,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeqAsync",
            "value": 0.3975969783759358,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordPar",
            "value": 73.40300285042717,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordSeq",
            "value": 95.93817648224241,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 546.5206301799999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 536.5795155800001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 561.1356842999999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 574.04552168,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "145262613+zio-scala-steward[bot]@users.noreply.github.com",
            "name": "zio-scala-steward[bot]",
            "username": "zio-scala-steward[bot]"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "5bc291110c344d3ac79977629af580d155fa1128",
          "message": "Update sbt, scripted-plugin to 1.11.1 (#1541)\n\n📦 Updates \n* [org.scala-sbt:sbt](https://github.com/sbt/sbt)\n* [org.scala-sbt:scripted-plugin](https://github.com/sbt/sbt)\n\n from `1.11.0` to `1.11.1`",
          "timestamp": "2025-06-03T08:38:05+02:00",
          "tree_id": "09e2c386e63b7e552248234e654b1dba8c7352d9",
          "url": "https://github.com/zio/zio-kafka/commit/5bc291110c344d3ac79977629af580d155fa1128"
        },
        "date": 1748933961717,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 595.02711022,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 601.9946654800001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 92.62850915848485,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 287.92197692999997,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeqAsync",
            "value": 143.2819449913536,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeqAsync",
            "value": 0.37581035771084836,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordPar",
            "value": 63.272967114423324,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordSeq",
            "value": 86.28777565831835,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 549.70314644,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 538.6388156399998,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 564.9174673800001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 578.12366408,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "145262613+zio-scala-steward[bot]@users.noreply.github.com",
            "name": "zio-scala-steward[bot]",
            "username": "zio-scala-steward[bot]"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "7443089d52bf2ff4de73a04015fe79e65ae46e43",
          "message": "Update zio-sbt-ci, zio-sbt-ecosystem, ... to 0.4.0-alpha.32 (#1544)\n\n📦 Updates \n* [dev.zio:zio-sbt-ci](https://github.com/zio/zio-sbt)\n* [dev.zio:zio-sbt-ecosystem](https://github.com/zio/zio-sbt)\n* [dev.zio:zio-sbt-website](https://github.com/zio/zio-sbt)\n\n from `0.4.0-alpha.31` to `0.4.0-alpha.32`",
          "timestamp": "2025-06-04T09:37:20+02:00",
          "tree_id": "370c63beb1f7531d4c8837c9593459ae37e9e128",
          "url": "https://github.com/zio/zio-kafka/commit/7443089d52bf2ff4de73a04015fe79e65ae46e43"
        },
        "date": 1749023891630,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 593.8537509600001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 598.4222019000001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 84.08411088641023,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 268.05258157000003,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeqAsync",
            "value": 91.19285497783359,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeqAsync",
            "value": 0.3866222753687183,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordPar",
            "value": 57.352576986389124,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordSeq",
            "value": 75.72788885364837,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 547.0308326999999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 535.9367569000001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 563.3412324800001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 575.2833603800002,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "145262613+zio-scala-steward[bot]@users.noreply.github.com",
            "name": "zio-scala-steward[bot]",
            "username": "zio-scala-steward[bot]"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "b5da0cc7a05a97e17f1f9e2116c1cd25d7504710",
          "message": "Update sbt, scripted-plugin to 1.11.2 (#1547)\n\n## About this PR\n📦 Updates \n* [org.scala-sbt:sbt](https://github.com/sbt/sbt)\n* [org.scala-sbt:scripted-plugin](https://github.com/sbt/sbt)\n\n from `1.11.1` to `1.11.2`\n\n📜 [GitHub Release\nNotes](https://github.com/sbt/sbt/releases/tag/v1.11.2) - [Version\nDiff](https://github.com/sbt/sbt/compare/v1.11.1...v1.11.2)\n\n## Usage\n✅ **Please merge!**\n\nI'll automatically update this PR to resolve conflicts as long as you\ndon't change it yourself.\n\nIf you'd like to skip this version, you can just close this PR. If you\nhave any feedback, just mention me in the comments below.\n\nConfigure Scala Steward for your repository with a\n[`.scala-steward.conf`](https://github.com/scala-steward-org/scala-steward/blob/31f8cb648396db258f0ae31b31be0fef04a641dc/docs/repo-specific-configuration.md)\nfile.\n\n_Have a fantastic day writing Scala!_\n\n<details>\n<summary>🔍 Files still referring to the old version number</summary>\n\nThe following files still refer to the old version number (1.11.1).\nYou might want to review and update them manually.\n```\nproject/plugins.sbt\n```\n</details>\n<details>\n<summary>⚙ Adjust future updates</summary>\n\nAdd this to your `.scala-steward.conf` file to ignore future updates of\nthis dependency:\n```\nupdates.ignore = [ { groupId = \"org.scala-sbt\" } ]\n```\nOr, add this to slow down future updates of this dependency:\n```\ndependencyOverrides = [{\n  pullRequests = { frequency = \"30 days\" },\n  dependency = { groupId = \"org.scala-sbt\" }\n}]\n```\n</details>\n\n<sup>\nlabels: library-update, early-semver-patch, semver-spec-patch,\nversion-scheme:early-semver, old-version-remains, commit-count:1\n</sup>\n\n<!-- scala-steward = {\n  \"Update\" : {\n    \"ForGroupId\" : {\n      \"forArtifactIds\" : [\n        {\n          \"ForArtifactId\" : {\n            \"crossDependency\" : [\n              {\n                \"groupId\" : \"org.scala-sbt\",\n                \"artifactId\" : {\n                  \"name\" : \"sbt\",\n                  \"maybeCrossName\" : null\n                },\n                \"version\" : \"1.11.1\",\n                \"sbtVersion\" : null,\n                \"scalaVersion\" : null,\n                \"configurations\" : null\n              }\n            ],\n            \"newerVersions\" : [\n              \"1.11.2\"\n            ],\n            \"newerGroupId\" : null,\n            \"newerArtifactId\" : null\n          }\n        },\n        {\n          \"ForArtifactId\" : {\n            \"crossDependency\" : [\n              {\n                \"groupId\" : \"org.scala-sbt\",\n                \"artifactId\" : {\n                  \"name\" : \"scripted-plugin\",\n                  \"maybeCrossName\" : \"scripted-plugin_2.12\"\n                },\n                \"version\" : \"1.11.1\",\n                \"sbtVersion\" : null,\n                \"scalaVersion\" : null,\n                \"configurations\" : null\n              }\n            ],\n            \"newerVersions\" : [\n              \"1.11.2\"\n            ],\n            \"newerGroupId\" : null,\n            \"newerArtifactId\" : null\n          }\n        }\n      ]\n    }\n  },\n  \"Labels\" : [\n    \"library-update\",\n    \"early-semver-patch\",\n    \"semver-spec-patch\",\n    \"version-scheme:early-semver\",\n    \"old-version-remains\",\n    \"commit-count:1\"\n  ]\n} -->\n\nCo-authored-by: zio-scala-steward[bot] <145262613+zio-scala-steward[bot]@users.noreply.github.com>",
          "timestamp": "2025-06-08T08:54:05+02:00",
          "tree_id": "a8b02b334ef7539783636da1ae9063efd77d274b",
          "url": "https://github.com/zio/zio-kafka/commit/b5da0cc7a05a97e17f1f9e2116c1cd25d7504710"
        },
        "date": 1749366877165,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 591.7344465199999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 592.80751866,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 79.27273891384614,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 254.23598915799997,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeqAsync",
            "value": 34.498359571894255,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeqAsync",
            "value": 0.38435451308675694,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordPar",
            "value": 53.64138469576369,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordSeq",
            "value": 67.39813409885294,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 545.39743456,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 534.8829502000001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 560.0064954,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 572.0233807200001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "e.vanoosten@grons.nl",
            "name": "Erik van Oosten",
            "username": "erikvanoosten"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "01192c84fad53ae4cca7046e80410a59b21c2fe9",
          "message": "Simplify code, use uniform type names (#1546)\n\nThis collects a few small improvements that were made in the disbanded\ntracing PR:\n- replace function `val`s with `def`\n- use consisten type parameter names (`A` instead of `T`, etc.)\n- small scaladoc fixes\n- small type improvements",
          "timestamp": "2025-06-08T10:03:52+02:00",
          "tree_id": "66edec0fef482447a9cd2dd772e35f4110f316ea",
          "url": "https://github.com/zio/zio-kafka/commit/01192c84fad53ae4cca7046e80410a59b21c2fe9"
        },
        "date": 1749371265467,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 591.4240369599999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 593.2850302000002,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 105.70105917301012,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 336.9109922633333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeqAsync",
            "value": 44.26271017065552,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeqAsync",
            "value": 0.3830584040182242,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordPar",
            "value": 88.21103895023312,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordSeq",
            "value": 101.76174563628282,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 545.6547605,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 536.66277108,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 560.22616014,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 571.5417759799999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "e.vanoosten@grons.nl",
            "name": "Erik van Oosten",
            "username": "erikvanoosten"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "d96d7b07c9b17befa3cccdba17ccd1f869d19bb5",
          "message": "Await acknowledgements in async benchmarks (#1545)\n\nBy waiting for the acknowledgements before closing the async benchmark,\nwe expect these benchmarks to become more stable.\n\nFixes #1542.",
          "timestamp": "2025-06-08T10:26:20+02:00",
          "tree_id": "89597fedaa6da7a0abe90ea92a901132e80bc4b0",
          "url": "https://github.com/zio/zio-kafka/commit/d96d7b07c9b17befa3cccdba17ccd1f869d19bb5"
        },
        "date": 1749372415657,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 591.26773498,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 594.6889507399999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 141.06176895992064,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 347.60423049,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeqAsync",
            "value": 34.0350120504799,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeqAsync",
            "value": 7.205802206261036,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordPar",
            "value": 94.61033080385347,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordSeq",
            "value": 127.56455687306205,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 548.4690705600001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 539.8480321600001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 564.12676908,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 574.19695162,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "e.vanoosten@grons.nl",
            "name": "Erik van Oosten",
            "username": "erikvanoosten"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "432f17f959660e347227e6670e46619966e4ce98",
          "message": "Remove workaround for kafka 4.1.0 (#1550)\n\nThis removes the workaround for\nhttps://issues.apache.org/jira/browse/KAFKA-18818 for kakfa 4.1.0 and\nlater, but keeps it for kafka 4.0.x.",
          "timestamp": "2025-06-18T10:47:56+02:00",
          "tree_id": "697d16ae3a005b603a22c55c1109412663bdef35",
          "url": "https://github.com/zio/zio-kafka/commit/432f17f959660e347227e6670e46619966e4ce98"
        },
        "date": 1750237678544,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 595.2827353,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 594.9743580800001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 110.71555267555557,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 334.35090489999993,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeqAsync",
            "value": 30.10930189855313,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeqAsync",
            "value": 6.862544027471449,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordPar",
            "value": 74.68231996353114,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordSeq",
            "value": 96.72551346969698,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 547.10998002,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 536.7679833400001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 562.28620774,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 575.0236186200001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "145262613+zio-scala-steward[bot]@users.noreply.github.com",
            "name": "zio-scala-steward[bot]",
            "username": "zio-scala-steward[bot]"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "4a08d00ca594feb5eb9d47bc045c43b9e9b8cc51",
          "message": "Update sbt, scripted-plugin to 1.11.3 (#1551)\n\n📦 Updates \n* [org.scala-sbt:sbt](https://github.com/sbt/sbt)\n* [org.scala-sbt:scripted-plugin](https://github.com/sbt/sbt)\n\n from `1.11.2` to `1.11.3`",
          "timestamp": "2025-07-06T08:22:44+02:00",
          "tree_id": "10a4fb693bd1933627b8188242c6cd666b3bafe1",
          "url": "https://github.com/zio/zio-kafka/commit/4a08d00ca594feb5eb9d47bc045c43b9e9b8cc51"
        },
        "date": 1751784169565,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 591.2889129,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 596.43380576,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 104.50644942456566,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 325.06761523000006,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeqAsync",
            "value": 34.230339510784475,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeqAsync",
            "value": 7.007945738186604,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordPar",
            "value": 83.08166559485515,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordSeq",
            "value": 105.79530827309092,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 546.00120462,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 535.65471362,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 562.7455238799998,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 574.1095156599999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "145262613+zio-scala-steward[bot]@users.noreply.github.com",
            "name": "zio-scala-steward[bot]",
            "username": "zio-scala-steward[bot]"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "18bfd5fd41c2074d4d43ee86e904c2ec6d3262b0",
          "message": "Update zio-logging-slf4j, ... to 2.5.1 (#1552)\n\n## About this PR\n📦 Updates \n* [dev.zio:zio-logging-slf4j](https://github.com/zio/zio-logging)\n* [dev.zio:zio-logging-slf4j2](https://github.com/zio/zio-logging)\n\n from `2.5.0` to `2.5.1`\n\n📜 [GitHub Release\nNotes](https://github.com/zio/zio-logging/releases/tag/v2.5.1) -\n[Version\nDiff](https://github.com/zio/zio-logging/compare/v2.5.0...v2.5.1)\n\n## Usage\n✅ **Please merge!**\n\nI'll automatically update this PR to resolve conflicts as long as you\ndon't change it yourself.\n\nIf you'd like to skip this version, you can just close this PR. If you\nhave any feedback, just mention me in the comments below.\n\nConfigure Scala Steward for your repository with a\n[`.scala-steward.conf`](https://github.com/scala-steward-org/scala-steward/blob/0c7991de8224eff8436230a3e11b89520bbf94c8/docs/repo-specific-configuration.md)\nfile.\n\n_Have a fantastic day writing Scala!_\n\n<details>\n<summary>🔍 Files still referring to the old version number</summary>\n\nThe following files still refer to the old version number (2.5.0).\nYou might want to review and update them manually.\n```\ndocs/consumer-tuning.md\n```\n</details>\n<details>\n<summary>⚙ Adjust future updates</summary>\n\nAdd this to your `.scala-steward.conf` file to ignore future updates of\nthis dependency:\n```\nupdates.ignore = [ { groupId = \"dev.zio\" } ]\n```\nOr, add this to slow down future updates of this dependency:\n```\ndependencyOverrides = [{\n  pullRequests = { frequency = \"30 days\" },\n  dependency = { groupId = \"dev.zio\" }\n}]\n```\n</details>\n\n<sup>\nlabels: library-update, early-semver-patch, semver-spec-patch,\nold-version-remains, commit-count:1\n</sup>\n\n<!-- scala-steward = {\n  \"Update\" : {\n    \"ForGroupId\" : {\n      \"forArtifactIds\" : [\n        {\n          \"ForArtifactId\" : {\n            \"crossDependency\" : [\n              {\n                \"groupId\" : \"dev.zio\",\n                \"artifactId\" : {\n                  \"name\" : \"zio-logging-slf4j\",\n                  \"maybeCrossName\" : \"zio-logging-slf4j_2.13\"\n                },\n                \"version\" : \"2.5.0\",\n                \"sbtVersion\" : null,\n                \"scalaVersion\" : null,\n                \"configurations\" : \"test\"\n              },\n              {\n                \"groupId\" : \"dev.zio\",\n                \"artifactId\" : {\n                  \"name\" : \"zio-logging-slf4j\",\n                  \"maybeCrossName\" : \"zio-logging-slf4j_3\"\n                },\n                \"version\" : \"2.5.0\",\n                \"sbtVersion\" : null,\n                \"scalaVersion\" : null,\n                \"configurations\" : \"test\"\n              }\n            ],\n            \"newerVersions\" : [\n              \"2.5.1\"\n            ],\n            \"newerGroupId\" : null,\n            \"newerArtifactId\" : null\n          }\n        },\n        {\n          \"ForArtifactId\" : {\n            \"crossDependency\" : [\n              {\n                \"groupId\" : \"dev.zio\",\n                \"artifactId\" : {\n                  \"name\" : \"zio-logging-slf4j2\",\n                  \"maybeCrossName\" : \"zio-logging-slf4j2_2.13\"\n                },\n                \"version\" : \"2.5.0\",\n                \"sbtVersion\" : null,\n                \"scalaVersion\" : null,\n                \"configurations\" : null\n              }\n            ],\n            \"newerVersions\" : [\n              \"2.5.1\"\n            ],\n            \"newerGroupId\" : null,\n            \"newerArtifactId\" : null\n          }\n        }\n      ]\n    }\n  },\n  \"Labels\" : [\n    \"library-update\",\n    \"early-semver-patch\",\n    \"semver-spec-patch\",\n    \"old-version-remains\",\n    \"commit-count:1\"\n  ]\n} -->\n\nCo-authored-by: zio-scala-steward[bot] <145262613+zio-scala-steward[bot]@users.noreply.github.com>",
          "timestamp": "2025-07-13T08:02:09+02:00",
          "tree_id": "913e6d9da70a2b536d2fbc5a96c0da2a2cbe3c7a",
          "url": "https://github.com/zio/zio-kafka/commit/18bfd5fd41c2074d4d43ee86e904c2ec6d3262b0"
        },
        "date": 1752387745992,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 598.8963694400001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 597.6674973199999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 102.00290635608081,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 317.3030236733334,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeqAsync",
            "value": 33.1102337105463,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeqAsync",
            "value": 7.179341741214164,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordPar",
            "value": 80.74000262659007,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordSeq",
            "value": 103.15140708347475,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 549.21066148,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 539.50734528,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 565.70834264,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 578.8680690599999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "145262613+zio-scala-steward[bot]@users.noreply.github.com",
            "name": "zio-scala-steward[bot]",
            "username": "zio-scala-steward[bot]"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "4812adb02606c83ccd40cc19483f72e9d05e7771",
          "message": "Update zio, zio-streams, zio-test, ... to 2.1.20 (#1556)\n\n📦 Updates \n* [dev.zio:zio](https://github.com/zio/zio)\n* [dev.zio:zio-streams](https://github.com/zio/zio)\n* [dev.zio:zio-test](https://github.com/zio/zio)\n* [dev.zio:zio-test-sbt](https://github.com/zio/zio)\n\n from `2.1.19` to `2.1.20`",
          "timestamp": "2025-07-22T11:37:10+02:00",
          "tree_id": "2c0b312c2c483ddeb86ebbf570c734229a1c5bfa",
          "url": "https://github.com/zio/zio-kafka/commit/4812adb02606c83ccd40cc19483f72e9d05e7771"
        },
        "date": 1753178211344,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 594.42341602,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 595.32542064,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 84.33949924358973,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 271.44019807,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeqAsync",
            "value": 24.331543866030973,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeqAsync",
            "value": 6.66253956390094,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordPar",
            "value": 59.225618919695634,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordSeq",
            "value": 74.50993287216677,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 547.8900368000001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 537.91074524,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 562.83237906,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 574.865448,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "145262613+zio-scala-steward[bot]@users.noreply.github.com",
            "name": "zio-scala-steward[bot]",
            "username": "zio-scala-steward[bot]"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "bd07a02545d49ad8d437b8bf8de2bd1fe8814cd6",
          "message": "Update sbt, scripted-plugin to 1.11.4 (#1557)\n\n📦 Updates \n* [org.scala-sbt:sbt](https://github.com/sbt/sbt)\n* [org.scala-sbt:scripted-plugin](https://github.com/sbt/sbt)\n\n from `1.11.3` to `1.11.4`",
          "timestamp": "2025-08-09T12:34:44+02:00",
          "tree_id": "7eb261af5279d71111a413b85b998b9cb41d5be2",
          "url": "https://github.com/zio/zio-kafka/commit/bd07a02545d49ad8d437b8bf8de2bd1fe8814cd6"
        },
        "date": 1754736858477,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 594.2342109799997,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 595.5934772599998,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 83.32024239102562,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 263.78568064399997,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeqAsync",
            "value": 21.13835261804494,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeqAsync",
            "value": 6.613526227986174,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordPar",
            "value": 52.12843902018118,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordSeq",
            "value": 70.32533899609524,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 547.5851307199999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 537.7711973199999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 562.35637084,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 574.60999572,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "145262613+zio-scala-steward[bot]@users.noreply.github.com",
            "name": "zio-scala-steward[bot]",
            "username": "zio-scala-steward[bot]"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "fd8ea23f135a054bccc0f6726d4bdcec34074fba",
          "message": "Update zio-sbt-ci, zio-sbt-ecosystem, ... to 0.4.0-alpha.33 (#1559)\n\n📦 Updates \n* [dev.zio:zio-sbt-ci](https://github.com/zio/zio-sbt)\n* [dev.zio:zio-sbt-ecosystem](https://github.com/zio/zio-sbt)\n* [dev.zio:zio-sbt-website](https://github.com/zio/zio-sbt)\n\n from `0.4.0-alpha.32` to `0.4.0-alpha.33`",
          "timestamp": "2025-08-22T11:04:24+02:00",
          "tree_id": "f349a72d8b78a71f72f952a064dbb6c6d21175bc",
          "url": "https://github.com/zio/zio-kafka/commit/fd8ea23f135a054bccc0f6726d4bdcec34074fba"
        },
        "date": 1755854658345,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 595.9686598800001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 596.6060599599999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 88.92052066303029,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 280.00399947999995,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeqAsync",
            "value": 23.905532949747077,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeqAsync",
            "value": 6.992360169395297,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordPar",
            "value": 60.4702067684235,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordSeq",
            "value": 79.92309966432232,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 549.1483695799999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 537.9848459599999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 565.8779928600001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 576.6945324200001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "145262613+zio-scala-steward[bot]@users.noreply.github.com",
            "name": "zio-scala-steward[bot]",
            "username": "zio-scala-steward[bot]"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "2e11be8dcf6372278550754d60479450628526c0",
          "message": "Update sbt-native-packager to 1.11.3 (#1560)\n\n📦 Updates\n[com.github.sbt:sbt-native-packager](https://github.com/sbt/sbt-native-packager)\nfrom `1.11.1` to `1.11.3`",
          "timestamp": "2025-08-25T17:46:24+02:00",
          "tree_id": "f438afd0065319f8d0a7a2f6f70d4e8988db27f0",
          "url": "https://github.com/zio/zio-kafka/commit/2e11be8dcf6372278550754d60479450628526c0"
        },
        "date": 1756137976718,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 592.4317635200001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 592.73800826,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 90.06753488121213,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 282.21657430999994,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeqAsync",
            "value": 25.143494698334006,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeqAsync",
            "value": 6.8034769727646855,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordPar",
            "value": 58.94703621856157,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordSeq",
            "value": 78.19116491141392,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 547.7871473600001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 539.35007264,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 564.0683493800001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 576.09489012,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "145262613+zio-scala-steward[bot]@users.noreply.github.com",
            "name": "zio-scala-steward[bot]",
            "username": "zio-scala-steward[bot]"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "e4433f6ad7703561bf0c708fdca67e2d357014f3",
          "message": "Update sbt, scripted-plugin to 1.11.5 (#1561)\n\n📦 Updates \n* [org.scala-sbt:sbt](https://github.com/sbt/sbt)\n* [org.scala-sbt:scripted-plugin](https://github.com/sbt/sbt)\n\n from `1.11.4` to `1.11.5`",
          "timestamp": "2025-08-25T17:46:49+02:00",
          "tree_id": "2e8af6ddf137f709ca6b3e7242def06fb1ba7a48",
          "url": "https://github.com/zio/zio-kafka/commit/e4433f6ad7703561bf0c708fdca67e2d357014f3"
        },
        "date": 1756138446052,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 598.77671462,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 599.6977171,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 91.9280741248485,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 283.16207177000007,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeqAsync",
            "value": 24.273333584129627,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeqAsync",
            "value": 7.089501822962645,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordPar",
            "value": 58.35595881229029,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordSeq",
            "value": 77.0715476521099,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 548.0177810199999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 539.0910617999999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 567.6073043,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 577.60142482,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "145262613+zio-scala-steward[bot]@users.noreply.github.com",
            "name": "zio-scala-steward[bot]",
            "username": "zio-scala-steward[bot]"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "abe2678fa5c13f7c3ac9a2998a672fc493b4b891",
          "message": "Update zio, zio-streams, zio-test, ... to 2.1.21 (#1565)\n\n📦 Updates \n* [dev.zio:zio](https://github.com/zio/zio)\n* [dev.zio:zio-streams](https://github.com/zio/zio)\n* [dev.zio:zio-test](https://github.com/zio/zio)\n* [dev.zio:zio-test-sbt](https://github.com/zio/zio)\n\n from `2.1.20` to `2.1.21`\n\nCo-authored-by: Steven Vroonland <svroonland@gmail.com>",
          "timestamp": "2025-09-05T19:56:57+02:00",
          "tree_id": "6ec7cb667c8f9a140151a1712274f23e74c282ea",
          "url": "https://github.com/zio/zio-kafka/commit/abe2678fa5c13f7c3ac9a2998a672fc493b4b891"
        },
        "date": 1757096228283,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 598.1014001400002,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 599.4037096,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 89.7120414281818,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 283.7475687,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeqAsync",
            "value": 24.761776842212633,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeqAsync",
            "value": 7.022844610263986,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordPar",
            "value": 59.801439217526,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordSeq",
            "value": 80.73676426175092,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 548.93218476,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 540.4411054399999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 564.34916406,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 579.0265438600001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "145262613+zio-scala-steward[bot]@users.noreply.github.com",
            "name": "zio-scala-steward[bot]",
            "username": "zio-scala-steward[bot]"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "14c8bd4480220a09a4d30f82b1fa7046b8a7b14c",
          "message": "Update zio-sbt-ci, zio-sbt-ecosystem, ... to 0.4.0-alpha.34 (#1563)\n\n📦 Updates \n* [dev.zio:zio-sbt-ci](https://github.com/zio/zio-sbt)\n* [dev.zio:zio-sbt-ecosystem](https://github.com/zio/zio-sbt)\n* [dev.zio:zio-sbt-website](https://github.com/zio/zio-sbt)\n\n from `0.4.0-alpha.33` to `0.4.0-alpha.34`",
          "timestamp": "2025-09-05T20:07:03+02:00",
          "tree_id": "21bc4a50df9b11b833b858cb8694714a41d40b9b",
          "url": "https://github.com/zio/zio-kafka/commit/14c8bd4480220a09a4d30f82b1fa7046b8a7b14c"
        },
        "date": 1757096833253,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 596.51727334,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 597.4779727000001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 99.46435283094951,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 334.12784769666666,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeqAsync",
            "value": 32.25149442126085,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeqAsync",
            "value": 7.191326758805649,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordPar",
            "value": 80.86999412161971,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordSeq",
            "value": 102.35632286254548,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 549.09079408,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 538.3767360800001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 564.99964526,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 577.2423304800001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "145262613+zio-scala-steward[bot]@users.noreply.github.com",
            "name": "zio-scala-steward[bot]",
            "username": "zio-scala-steward[bot]"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "d16d94ce6a76889b51b0cf5be874c67f0ac8e590",
          "message": "Update kafka-clients to 4.1.0 (#1564)\n\n📦 Updates [org.apache.kafka:kafka-clients](https://kafka.apache.org)\nfrom `4.0.0` to `4.1.0`\n\nIn addition:\n- In the admin client, use `fromJava` i.s.o. `apply` on the companion\nobjects of the Scala case classes that can be used instead of the Java\nequivalents. The function name `fromJava` is clearer and prevents\ncollisions with the case class constructors. Note that this is _not_\nbackward compatible. However, the library users should never need these\nfunctions.\n- Re-instated the workaround for\nhttps://issues.apache.org/jira/browse/KAFKA-18818 even though it is\nmarked as solved in kafka 4.1 (it isn't).\n\n---------\n\nCo-authored-by: zio-scala-steward[bot] <145262613+zio-scala-steward[bot]@users.noreply.github.com>\nCo-authored-by: Erik van Oosten <e.vanoosten@grons.nl>",
          "timestamp": "2025-09-06T12:15:38+02:00",
          "tree_id": "58468970eadec43252d084ed581b365794bcf25f",
          "url": "https://github.com/zio/zio-kafka/commit/d16d94ce6a76889b51b0cf5be874c67f0ac8e590"
        },
        "date": 1757154938980,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 592.8427535399999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 592.8564948199999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 90.27361641333334,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 280.4232167,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeqAsync",
            "value": 25.41066361658555,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeqAsync",
            "value": 6.8201734684573125,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordPar",
            "value": 61.75895225254797,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordSeq",
            "value": 83.42314587429904,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 546.9022645,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 537.1850391600001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 563.76328682,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 575.05382674,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "145262613+zio-scala-steward[bot]@users.noreply.github.com",
            "name": "zio-scala-steward[bot]",
            "username": "zio-scala-steward[bot]"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "03aeb7b76ce6a3c937309c18972214a38dcd13ef",
          "message": "Update sbt, scripted-plugin to 1.11.6 (#1568)\n\n📦 Updates \n* [org.scala-sbt:sbt](https://github.com/sbt/sbt)\n* [org.scala-sbt:scripted-plugin](https://github.com/sbt/sbt)\n\n from `1.11.5` to `1.11.6`",
          "timestamp": "2025-09-07T16:05:14+02:00",
          "tree_id": "f9befebd1c3bee1b21151d1e85e123e833cdc04e",
          "url": "https://github.com/zio/zio-kafka/commit/03aeb7b76ce6a3c937309c18972214a38dcd13ef"
        },
        "date": 1757255132823,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 589.49329608,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 594.11794044,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 101.21023927096968,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 314.48029647666675,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeqAsync",
            "value": 29.85415272122035,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeqAsync",
            "value": 6.943580364307387,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordPar",
            "value": 73.88562544985844,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordSeq",
            "value": 94.92900467194804,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 549.0672953800001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 538.51055046,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 564.26994874,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 577.75312536,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "e.vanoosten@grons.nl",
            "name": "Erik van Oosten",
            "username": "erikvanoosten"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "3fd9790b05bc5d6628acf02d6c8249d80c320931",
          "message": "Make rebalances faster, support slow consumers (#1576)\n\nThis PR prevents a slow consumer from timing out on a rebalance when the\npre-fetch buffers are full, and `rebalanceSafeCommits` is enabled. In\naddition, it makes rebalances go even faster for fast consumers.\n\nAfter this PR, consumers should run fine when they are configured such\nthat they can process `max.poll.records` within `maxRebalanceDuration`,\nand (in some cases) use the `RangeAssignor` as partition assignor.\n\n#### Details\n\n**Context:** Each zstream of a zio-kafka consumer (there is one stream\nper partition) consumes from an internal partition queue for that\npartition. These queues also act as a pre-fetch buffer. For performance\nreasons, record are pulled from the queue in chunks. These chunks then\nbecome the consumer zstream chunks.\n\n**The problem:** When `rebalanceSafeCommits` is enabled, during a\nrebalance or shutdown, we wait until the consumer's zstreams have\nprocessed the records that were pulled from the queue. Before this\nchange, the zstream pulled _all_ available records from the queue. Due\nto pre-fetching the number of records pulled, could be 2, 3 or more\ntimes higher than `max.poll.records` (depending on the pre-fetching\nsettings and data availability). Slow consumers may need more time than\n`maxRebalanceDuration` to process that many records. This is a problem;\nwhen the deadline is missed the entire consumer fails.\n\n**Solution part 1:** Before this change the partition queues contained\nrecords, now they contain chunks of records, where each chunk contains\nrecords fetched together in a single broker poll. This has the advantage\nthat the consumer can never get more than `max.poll.records` records in\na zstream chunk. As long as the consumer can process `max.poll.records`\nwithin `maxRebalanceDuration` we can avoid time-outs. This solution\ndoesn't affect throughput too much; `max.poll.records` is also the\nmaximum that a very fast consumer can get in each zstream chunk.\n\nNot all broker polls may result in `max.poll.records` records. When the\nbroker gives smaller batches, the consumer also gets smaller chunks,\nwhich can lower throughput. Luckily, the Kafka broker tries to make the\nbatches as large as possible, making this situation unlikely to happen.\nHowever, if needed, we can write smart chunk-merging logic that respects\nthe maximum chunk size in a separate PR.\n\n**Solution part 2:** With solution part 1, the consumer's zstreams no\nlonger contains very large chunks. However they still continue pulling\nchunks from the queue until the end-of-stream marker, even though the\npartition was just revoked. It is fine to drop those records; they will\nbe picked up by another consumer after the rebalance. Therefore, we now\ndiscard these pre-fetched records as soon as the stream ends.\n\nDue to this change method `PartitionStreamControl.lost` is now\neffectively the same as `PartitionStreamControl.end`. Method `lost` has\nbeen removed.\n\n**Solution part 3:** Partitions that are _not_ revoked during a\nrebalance can still time out. There is no solution for this yet.\nHowever, as a workaround you can use the RangeAssignor. The\nRangeAssignor revokes all partitions during a rebalance.\n\nAlso:\n- Collect the runloop configuration in a separate class. This has the\nadditional advantage that it can be tested better.\n- The stream partition queues now contain 'Exit's instead of 'Take's.\n'Take's can represent multiple values. Since we want to queue chunks\nexplicitly, the slightly simpler `Exit` is sufficient and leads to\nslightly simpler code. The streaming is simplified further by directly\nusing `ZStream.repeatZIOOption` instead of `ZSteam.repeatX` and\n`ZStream.flattenX`.\n\n### Considered alternatives\n\nAn alternative is to make the consumer stream poll no more than\n`max.poll.records` from the partition queue. However, this breaks\nzio-kafka's guarantee that all records (for a given partition) fetched\ntogether from the broker are grouped into one chunk.\n\nFixes #1572\nFixes #1573\nFixes #1574",
          "timestamp": "2025-09-24T10:44:46+02:00",
          "tree_id": "6761da140fae20eb6868db257d952df8b9fa7e70",
          "url": "https://github.com/zio/zio-kafka/commit/3fd9790b05bc5d6628acf02d6c8249d80c320931"
        },
        "date": 1758704913179,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 588.86441262,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 588.6295093000001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 84.83526768923076,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 272.28843655000003,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeqAsync",
            "value": 23.649739655060294,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeqAsync",
            "value": 6.69065891089134,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordPar",
            "value": 55.44626277556895,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordSeq",
            "value": 76.8800353658608,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 545.5311286200001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 535.8294298999999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 560.6828568,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 571.77989004,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "name": "zio",
            "username": "zio"
          },
          "committer": {
            "name": "zio",
            "username": "zio"
          },
          "id": "ef438723d2da4af69cd12bbfd2c06b6bc3017a3f",
          "message": "Fix small stuff",
          "timestamp": "2025-09-24T08:53:58Z",
          "url": "https://github.com/zio/zio-kafka/pull/1577/commits/ef438723d2da4af69cd12bbfd2c06b6bc3017a3f"
        },
        "date": 1758705405166,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 593.3654237400001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 592.3489055199999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 80.69738926076923,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 255.55776639466666,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeqAsync",
            "value": 19.132415688464096,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeqAsync",
            "value": 6.561930590987904,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordPar",
            "value": 41.923686528399706,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordSeq",
            "value": 60.56987081942105,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 545.8851165600001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 534.9381245799999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 562.8226848800001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 573.3990755399999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "e.vanoosten@grons.nl",
            "name": "Erik van Oosten",
            "username": "erikvanoosten"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "b0c6cdb1cd79fd55c98e0a0441b82bc1487f2968",
          "message": "Fix small stuff (#1577)\n\nFix small stuff like spelling mistakes, typos, file rename, removal of\ndeprecated sonatype repository, redundant parenthesis, and small code\nstructure refactorings.",
          "timestamp": "2025-09-24T10:57:29+02:00",
          "tree_id": "b2bc62c2ed7cf08b6bc023e5b4a27c501915e999",
          "url": "https://github.com/zio/zio-kafka/commit/b0c6cdb1cd79fd55c98e0a0441b82bc1487f2968"
        },
        "date": 1758705441048,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 591.47963678,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 595.56264464,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 83.90321476384617,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 261.0526974020001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeqAsync",
            "value": 21.87866146798207,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeqAsync",
            "value": 6.957933831871719,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordPar",
            "value": 52.71905355366134,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordSeq",
            "value": 71.98900126691757,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 548.27732308,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 536.9218370000001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 564.6511385199999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 575.7121622,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "145262613+zio-scala-steward[bot]@users.noreply.github.com",
            "name": "zio-scala-steward[bot]",
            "username": "zio-scala-steward[bot]"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "bcf78ec79ded3e17e45e434e19870b4887526173",
          "message": "Update zio-sbt-ci, zio-sbt-ecosystem, ... to 0.4.0-alpha.35 (#1582)\n\n📦 Updates \n* [dev.zio:zio-sbt-ci](https://github.com/zio/zio-sbt)\n* [dev.zio:zio-sbt-ecosystem](https://github.com/zio/zio-sbt)\n* [dev.zio:zio-sbt-website](https://github.com/zio/zio-sbt)\n\n from `0.4.0-alpha.34` to `0.4.0-alpha.35`",
          "timestamp": "2025-09-24T10:59:22+02:00",
          "tree_id": "0d252695fa6bf0a4c187e9d665f4b1e5c4eb6df2",
          "url": "https://github.com/zio/zio-kafka/commit/bcf78ec79ded3e17e45e434e19870b4887526173"
        },
        "date": 1758705621684,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 594.7514913800001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 595.69535348,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 83.82054871307692,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 264.55990367,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeqAsync",
            "value": 22.4606917510531,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeqAsync",
            "value": 6.623114957882583,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordPar",
            "value": 52.954848687366564,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordSeq",
            "value": 69.85488811199818,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 546.6652795599999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 536.7684598799999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 561.5136740199999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 572.44825908,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "name": "zio",
            "username": "zio"
          },
          "committer": {
            "name": "zio",
            "username": "zio"
          },
          "id": "e57b003cd7ba81a725509939ff3d613f5b5b7a1a",
          "message": "Update zio-sbt-ci, zio-sbt-ecosystem, ... to 0.4.0-alpha.35",
          "timestamp": "2025-09-24T08:57:34Z",
          "url": "https://github.com/zio/zio-kafka/pull/1582/commits/e57b003cd7ba81a725509939ff3d613f5b5b7a1a"
        },
        "date": 1758705642100,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 590.72346706,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 596.17606476,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 104.49851321733333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 310.59016369666665,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeqAsync",
            "value": 27.10636098739551,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeqAsync",
            "value": 6.857311513768399,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordPar",
            "value": 58.228637422727125,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordSeq",
            "value": 80.77147152681319,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 548.5937922,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 537.37329464,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 562.44256094,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 573.20145182,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "name": "zio",
            "username": "zio"
          },
          "committer": {
            "name": "zio",
            "username": "zio"
          },
          "id": "f5ceb489f989befe5831ca0c08d71e9aa53d1361",
          "message": "Validate consumer settings",
          "timestamp": "2025-09-24T08:59:27Z",
          "url": "https://github.com/zio/zio-kafka/pull/1580/commits/f5ceb489f989befe5831ca0c08d71e9aa53d1361"
        },
        "date": 1758705835102,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 589.4645247599999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 592.82833144,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 78.60084950153845,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 252.70073618000004,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeqAsync",
            "value": 20.500169462442503,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeqAsync",
            "value": 6.620199470583889,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordPar",
            "value": 51.56654597764021,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordSeq",
            "value": 67.89132266069609,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 546.1533148600001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 535.1550124399998,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 562.0505683599999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 573.50887942,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "name": "zio",
            "username": "zio"
          },
          "committer": {
            "name": "zio",
            "username": "zio"
          },
          "id": "37a8cd84f5b8a8c3f5eb1c000b3810bdb183888b",
          "message": "Update the readme on every push to master",
          "timestamp": "2025-09-24T08:59:27Z",
          "url": "https://github.com/zio/zio-kafka/pull/1584/commits/37a8cd84f5b8a8c3f5eb1c000b3810bdb183888b"
        },
        "date": 1758712776734,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 590.49142566,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 590.6854883000001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 84.10000206487183,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 265.29128582,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeqAsync",
            "value": 22.513941265570047,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeqAsync",
            "value": 6.661572412703167,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordPar",
            "value": 55.48100421651686,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordSeq",
            "value": 75.2459225007326,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 546.5299402800001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 537.5640058600001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 561.0107221000001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 573.57244482,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "e.vanoosten@grons.nl",
            "name": "Erik van Oosten",
            "username": "erikvanoosten"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "1cf9c9ba80d11592cf75d64644e13db63b79c108",
          "message": "Update the readme on every push to master (#1584)\n\nAlso: use correct scala syntax",
          "timestamp": "2025-09-24T13:00:46+02:00",
          "tree_id": "ea097ad5ae37d5411998127a675cb0e6ce05b0bc",
          "url": "https://github.com/zio/zio-kafka/commit/1cf9c9ba80d11592cf75d64644e13db63b79c108"
        },
        "date": 1758712831920,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 590.3578163000002,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 593.4905250800001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 80.95704603641026,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 258.78412942599994,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeqAsync",
            "value": 21.249877236854463,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeqAsync",
            "value": 6.606082368530436,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordPar",
            "value": 51.44808984647686,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordSeq",
            "value": 70.8970359188022,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 546.4872945800001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 535.1716529200002,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 560.7667211599999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 576.2678672599999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "name": "zio",
            "username": "zio"
          },
          "committer": {
            "name": "zio",
            "username": "zio"
          },
          "id": "7be63064b4bc4013d34097828937081fc6e1c308",
          "message": "Update sbt-native-packager to 1.11.4",
          "timestamp": "2025-09-24T11:02:58Z",
          "url": "https://github.com/zio/zio-kafka/pull/1587/commits/7be63064b4bc4013d34097828937081fc6e1c308"
        },
        "date": 1759284927450,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 592.61443298,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 595.5303172199999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 114.06928687820204,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 362.07259725333347,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeqAsync",
            "value": 35.471380918286016,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeqAsync",
            "value": 7.212457180885654,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordPar",
            "value": 85.52209120228189,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordSeq",
            "value": 92.23178861060607,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 548.1363853,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 536.25796244,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 563.2459223000001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 576.7553930999999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "name": "zio",
            "username": "zio"
          },
          "committer": {
            "name": "zio",
            "username": "zio"
          },
          "id": "b965dabbb65f14888a56bed540acc4a88b66f2bb",
          "message": "Update logback-classic to 1.5.19",
          "timestamp": "2025-09-24T11:02:58Z",
          "url": "https://github.com/zio/zio-kafka/pull/1586/commits/b965dabbb65f14888a56bed540acc4a88b66f2bb"
        },
        "date": 1759284948408,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 588.7383268599999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 593.7078404,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 97.46292345921212,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 310.5942840833333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeqAsync",
            "value": 28.011141168727786,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeqAsync",
            "value": 6.855874037060814,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordPar",
            "value": 74.47737633290006,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordSeq",
            "value": 97.65919445132245,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 544.5379349,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 535.00047954,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 560.0883015399999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 572.6600020199999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "145262613+zio-scala-steward[bot]@users.noreply.github.com",
            "name": "zio-scala-steward[bot]",
            "username": "zio-scala-steward[bot]"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "6fdde5108d0e303a1989cd5a02d4b0025a66a75a",
          "message": "Update sbt-native-packager to 1.11.4 (#1587)\n\n📦 Updates\n[com.github.sbt:sbt-native-packager](https://github.com/sbt/sbt-native-packager)\nfrom `1.11.3` to `1.11.4`",
          "timestamp": "2025-10-01T09:58:25+02:00",
          "tree_id": "1eb80b2fded16740c49e858616cf0d583cc2bb9a",
          "url": "https://github.com/zio/zio-kafka/commit/6fdde5108d0e303a1989cd5a02d4b0025a66a75a"
        },
        "date": 1759306735917,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 590.0984641,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 593.0160180799999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 97.68373009222222,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 293.9810289800001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeqAsync",
            "value": 27.190459570429958,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeqAsync",
            "value": 6.8484591715503855,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordPar",
            "value": 74.33287173516177,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordSeq",
            "value": 93.93307211205949,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 547.1615806599999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 537.0837176800001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 562.7701479599999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 575.4449735999999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "145262613+zio-scala-steward[bot]@users.noreply.github.com",
            "name": "zio-scala-steward[bot]",
            "username": "zio-scala-steward[bot]"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "2c8502c955da66113b4ebe45e8381a4fba0f5602",
          "message": "Update logback-classic to 1.5.19 (#1586)\n\n📦 Updates\n[ch.qos.logback:logback-classic](https://github.com/qos-ch/logback) from\n`1.5.18` to `1.5.19`",
          "timestamp": "2025-10-01T10:14:18+02:00",
          "tree_id": "7ba18b3e21a839e8aa496097eb5e2e6f4ee8294b",
          "url": "https://github.com/zio/zio-kafka/commit/2c8502c955da66113b4ebe45e8381a4fba0f5602"
        },
        "date": 1759307713062,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 594.21774598,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 595.55686302,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 84.54898726435897,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 263.05885692999993,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeqAsync",
            "value": 21.680653172952738,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeqAsync",
            "value": 6.809185734176489,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordPar",
            "value": 54.75260895560336,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordSeq",
            "value": 70.23678992135717,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 550.1147949000001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 536.99428942,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 563.5430824000001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 576.70033002,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "name": "zio",
            "username": "zio"
          },
          "committer": {
            "name": "zio",
            "username": "zio"
          },
          "id": "2e655a8a83e638aeda4cbcee570d1d98ededc6ba",
          "message": "Update sbt, scripted-plugin to 1.11.7",
          "timestamp": "2025-10-01T08:14:23Z",
          "url": "https://github.com/zio/zio-kafka/pull/1589/commits/2e655a8a83e638aeda4cbcee570d1d98ededc6ba"
        },
        "date": 1759716416973,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 597.4648493199999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 597.04257978,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 87.26767797999999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 282.57308558,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeqAsync",
            "value": 23.225325415956128,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeqAsync",
            "value": 7.304574828827599,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordPar",
            "value": 56.70205488535052,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordSeq",
            "value": 77.95654907249818,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 551.96395826,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 539.4924082800001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 568.1449233000001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 578.9254251,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "145262613+zio-scala-steward[bot]@users.noreply.github.com",
            "name": "zio-scala-steward[bot]",
            "username": "zio-scala-steward[bot]"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "6b973cd028e754cae0573720f68a09d8e3d49450",
          "message": "Update sbt, scripted-plugin to 1.11.7 (#1589)\n\n📦 Updates \n* [org.scala-sbt:sbt](https://github.com/sbt/sbt)\n* [org.scala-sbt:scripted-plugin](https://github.com/sbt/sbt)\n\n from `1.11.6` to `1.11.7`",
          "timestamp": "2025-10-06T09:02:13+02:00",
          "tree_id": "ae5c2bc6a8aed3e4889be378dcb6d399009dc7a9",
          "url": "https://github.com/zio/zio-kafka/commit/6b973cd028e754cae0573720f68a09d8e3d49450"
        },
        "date": 1759735371134,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 589.38550992,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 589.3087843199999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 112.3553656444444,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 321.0882765766666,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeqAsync",
            "value": 27.86892011003382,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeqAsync",
            "value": 6.943710872903232,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordPar",
            "value": 80.25372568388747,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordSeq",
            "value": 97.88414945548053,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 547.5238554800001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 535.2216759199999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 562.0692013200002,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 575.24871476,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          }
        ]
      }
    ]
  }
}