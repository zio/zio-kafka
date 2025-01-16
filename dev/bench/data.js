window.BENCHMARK_DATA = {
  "lastUpdate": 1737014359437,
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
          "id": "7452812c5410c61b03c8cda5614f69067c075ed6",
          "message": "Add alternative fetch strategy for many partitions (#1281)\n\nWhen many hundreds of partitions need to be consumed, an excessive\r\namount of heap can be used for pre-fetching. The\r\n`ManyPartitionsQueueSizeBasedFetchStrategy` works similarly as the\r\ndefault `QueueSizeBasedFetchStrategy` but limits total memory usage.",
          "timestamp": "2024-07-16T19:15:59+02:00",
          "tree_id": "e148608e0e464c0cf343e869dfea5923570ce5a2",
          "url": "https://github.com/zio/zio-kafka/commit/7452812c5410c61b03c8cda5614f69067c075ed6"
        },
        "date": 1721150927838,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 603.0446847245438,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 592.6038282640225,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 547.3497861523078,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 534.0946336294871,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 647.729057759027,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 578.4607693223271,
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
          "id": "fd40816423b1b0ae81ee5487ab69e6f7a3cd5c27",
          "message": "Alternative producer implementation (#1285)\n\nRefactoring of the producer so that it handles errors per record.",
          "timestamp": "2024-07-18T10:42:50+02:00",
          "tree_id": "947556873df71da9cfc638945d5c7868bc271db5",
          "url": "https://github.com/zio/zio-kafka/commit/fd40816423b1b0ae81ee5487ab69e6f7a3cd5c27"
        },
        "date": 1721292955490,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 603.6799464689013,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 592.4829653082681,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 544.8437062899038,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 534.7722479450321,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 608.7594083147028,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 560.1613826636226,
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
          "id": "092de5365acd4c7758bd77e5884fe44d223eedae",
          "message": "Prevent users from enabling auto commit (#1290)\n\nZio-kafka applications always pre-fetch data so that user streams can\r\nprocess the data asynchronously. This is not compatible with auto\r\ncommit. When auto commit is enabled, the consumer will automatically\r\ncommit batches _before_ they are processed by the user streams.\r\n\r\nAn unaware user might accidentally enable auto commit and lose data\r\nduring rebalances.\r\n\r\nSolves #1289.",
          "timestamp": "2024-07-24T10:21:44+02:00",
          "tree_id": "9d6068ec8ddedbba1f5b130584fc9c7e8dee487a",
          "url": "https://github.com/zio/zio-kafka/commit/092de5365acd4c7758bd77e5884fe44d223eedae"
        },
        "date": 1721810126117,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 605.2145875363873,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 592.9493825082681,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 565.0656514186538,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 535.3807284459936,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 635.2262009387027,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 565.7158081387486,
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
          "id": "e4da64b6a398846484571d866af83d7ddf29f2d3",
          "message": "Update scalafmt-core to 3.8.3 (#1291)\n\n## About this PR\r\n📦 Updates\r\n[org.scalameta:scalafmt-core](https://github.com/scalameta/scalafmt)\r\nfrom `3.8.2` to `3.8.3`\r\n\r\n📜 [GitHub Release\r\nNotes](https://github.com/scalameta/scalafmt/releases/tag/v3.8.3) -\r\n[Version\r\nDiff](https://github.com/scalameta/scalafmt/compare/v3.8.2...v3.8.3)\r\n\r\n## Usage\r\n✅ **Please merge!**\r\n\r\nI'll automatically update this PR to resolve conflicts as long as you\r\ndon't change it yourself.\r\n\r\nIf you'd like to skip this version, you can just close this PR. If you\r\nhave any feedback, just mention me in the comments below.\r\n\r\nConfigure Scala Steward for your repository with a\r\n[`.scala-steward.conf`](https://github.com/scala-steward-org/scala-steward/blob/767fcfecbfd53c507152f6cf15c846176bae561d/docs/repo-specific-configuration.md)\r\nfile.\r\n\r\n_Have a fantastic day writing Scala!_\r\n\r\n<details>\r\n<summary>⚙ Adjust future updates</summary>\r\n\r\nAdd this to your `.scala-steward.conf` file to ignore future updates of\r\nthis dependency:\r\n```\r\nupdates.ignore = [ { groupId = \"org.scalameta\", artifactId = \"scalafmt-core\" } ]\r\n```\r\nOr, add this to slow down future updates of this dependency:\r\n```\r\ndependencyOverrides = [{\r\n  pullRequests = { frequency = \"30 days\" },\r\n  dependency = { groupId = \"org.scalameta\", artifactId = \"scalafmt-core\" }\r\n}]\r\n```\r\n</details>\r\n\r\n<sup>\r\nlabels: library-update, early-semver-patch, semver-spec-patch,\r\ncommit-count:1\r\n</sup>\r\n\r\nCo-authored-by: zio-scala-steward[bot] <145262613+zio-scala-steward[bot]@users.noreply.github.com>",
          "timestamp": "2024-07-26T09:15:32+02:00",
          "tree_id": "194640fb18b6708e556c62a06bd0d9b3db4e9d44",
          "url": "https://github.com/zio/zio-kafka/commit/e4da64b6a398846484571d866af83d7ddf29f2d3"
        },
        "date": 1721978942007,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 604.742168496015,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 594.0538180174301,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 546.7021517404808,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 537.5153275866986,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 624.802280566054,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 541.5398505838859,
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
          "id": "78fe019c97c487f409087585a6efed1099f60e28",
          "message": "Update zio, zio-streams, zio-test, ... to 2.1.7 (#1295)\n\n## About this PR\r\n📦 Updates \r\n* [dev.zio:zio](https://github.com/zio/zio)\r\n* [dev.zio:zio-streams](https://github.com/zio/zio)\r\n* [dev.zio:zio-test](https://github.com/zio/zio)\r\n* [dev.zio:zio-test-sbt](https://github.com/zio/zio)\r\n\r\n from `2.1.6` to `2.1.7`\r\n\r\n📜 [GitHub Release Notes](https://github.com/zio/zio/releases/tag/v2.1.7)\r\n- [Version Diff](https://github.com/zio/zio/compare/v2.1.6...v2.1.7)\r\n\r\n## Usage\r\n✅ **Please merge!**\r\n\r\nI'll automatically update this PR to resolve conflicts as long as you\r\ndon't change it yourself.\r\n\r\nIf you'd like to skip this version, you can just close this PR. If you\r\nhave any feedback, just mention me in the comments below.\r\n\r\nConfigure Scala Steward for your repository with a\r\n[`.scala-steward.conf`](https://github.com/scala-steward-org/scala-steward/blob/767fcfecbfd53c507152f6cf15c846176bae561d/docs/repo-specific-configuration.md)\r\nfile.\r\n\r\n_Have a fantastic day writing Scala!_\r\n\r\n<details>\r\n<summary>🔍 Files still referring to the old version number</summary>\r\n\r\nThe following files still refer to the old version number (2.1.6).\r\nYou might want to review and update them manually.\r\n```\r\nbuild.sbt\r\n```\r\n</details>\r\n<details>\r\n<summary>⚙ Adjust future updates</summary>\r\n\r\nAdd this to your `.scala-steward.conf` file to ignore future updates of\r\nthis dependency:\r\n```\r\nupdates.ignore = [ { groupId = \"dev.zio\" } ]\r\n```\r\nOr, add this to slow down future updates of this dependency:\r\n```\r\ndependencyOverrides = [{\r\n  pullRequests = { frequency = \"30 days\" },\r\n  dependency = { groupId = \"dev.zio\" }\r\n}]\r\n```\r\n</details>\r\n\r\n<sup>\r\nlabels: library-update, early-semver-patch, semver-spec-patch,\r\nold-version-remains, commit-count:1\r\n</sup>\r\n\r\nCo-authored-by: zio-scala-steward[bot] <145262613+zio-scala-steward[bot]@users.noreply.github.com>",
          "timestamp": "2024-08-13T21:24:18+02:00",
          "tree_id": "515cc8b45f0769077730fcdbcd6a7a6ba684c748",
          "url": "https://github.com/zio/zio-kafka/commit/78fe019c97c487f409087585a6efed1099f60e28"
        },
        "date": 1723577850861,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 602.6221757889758,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 593.1498081071509,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 549.0663975315385,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 533.7569170057691,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 595.1479303757837,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 573.3633616850055,
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
          "id": "f18a00abbe8990d727aa1771617707e5def581d7",
          "message": "Update zio-streams, zio-test-sbt to 2.1.7 (#1297)\n\n## About this PR\r\n📦 Updates \r\n* [dev.zio:zio-streams](https://github.com/zio/zio)\r\n* [dev.zio:zio-test-sbt](https://github.com/zio/zio)\r\n\r\n from `2.1.6` to `2.1.7`\r\n\r\n📜 [GitHub Release Notes](https://github.com/zio/zio/releases/tag/v2.1.7)\r\n- [Version Diff](https://github.com/zio/zio/compare/v2.1.6...v2.1.7)\r\n\r\n## Usage\r\n✅ **Please merge!**\r\n\r\nI'll automatically update this PR to resolve conflicts as long as you\r\ndon't change it yourself.\r\n\r\nIf you'd like to skip this version, you can just close this PR. If you\r\nhave any feedback, just mention me in the comments below.\r\n\r\nConfigure Scala Steward for your repository with a\r\n[`.scala-steward.conf`](https://github.com/scala-steward-org/scala-steward/blob/767fcfecbfd53c507152f6cf15c846176bae561d/docs/repo-specific-configuration.md)\r\nfile.\r\n\r\n_Have a fantastic day writing Scala!_\r\n\r\n<details>\r\n<summary>⚙ Adjust future updates</summary>\r\n\r\nAdd this to your `.scala-steward.conf` file to ignore future updates of\r\nthis dependency:\r\n```\r\nupdates.ignore = [ { groupId = \"dev.zio\" } ]\r\n```\r\nOr, add this to slow down future updates of this dependency:\r\n```\r\ndependencyOverrides = [{\r\n  pullRequests = { frequency = \"30 days\" },\r\n  dependency = { groupId = \"dev.zio\" }\r\n}]\r\n```\r\n</details>\r\n\r\n<sup>\r\nlabels: library-update, early-semver-patch, semver-spec-patch,\r\ncommit-count:1\r\n</sup>\r\n\r\nCo-authored-by: zio-scala-steward[bot] <145262613+zio-scala-steward[bot]@users.noreply.github.com>",
          "timestamp": "2024-08-14T08:32:59+02:00",
          "tree_id": "d4359ade0901f82f2bd69736191c7b8a98fd3d4f",
          "url": "https://github.com/zio/zio-kafka/commit/f18a00abbe8990d727aa1771617707e5def581d7"
        },
        "date": 1723617983159,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 603.7502056534825,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 592.1721541601117,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 545.8296597581731,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 533.8233733682691,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 565.5624092944865,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 559.7477026272667,
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
          "id": "f2de848cfebc137b38959ff208d261112863e7f0",
          "message": "Fix assign followed by revoke during same rebalance (#1294)\n\nWhen an assign and revoke for the same partition follow each other very quickly, in the same poll, right now we disregard the revoke and start a stream for the partition this is a problem because the partition is no longer assigned. With this change the partition will be ignored. The same change is made for an assign followed by a 'lost'.",
          "timestamp": "2024-08-15T20:04:07+02:00",
          "tree_id": "b4f542531dbd9a8a72542b5788b845bbb8a05f78",
          "url": "https://github.com/zio/zio-kafka/commit/f2de848cfebc137b38959ff208d261112863e7f0"
        },
        "date": 1723745880910,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 601.7056994192178,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 590.8142897288268,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 542.4746501279807,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 535.4501217698717,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 625.1880440532973,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 590.5786882713502,
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
          "id": "2f1affef11b3c6df4c9cd0d6ec07e843b0452add",
          "message": "Update logback-classic to 1.5.7 (#1299)\n\n## About this PR\r\n📦 Updates\r\n[ch.qos.logback:logback-classic](https://github.com/qos-ch/logback) from\r\n`1.5.6` to `1.5.7`\r\n\r\n## Usage\r\n✅ **Please merge!**\r\n\r\nI'll automatically update this PR to resolve conflicts as long as you\r\ndon't change it yourself.\r\n\r\nIf you'd like to skip this version, you can just close this PR. If you\r\nhave any feedback, just mention me in the comments below.\r\n\r\nConfigure Scala Steward for your repository with a\r\n[`.scala-steward.conf`](https://github.com/scala-steward-org/scala-steward/blob/767fcfecbfd53c507152f6cf15c846176bae561d/docs/repo-specific-configuration.md)\r\nfile.\r\n\r\n_Have a fantastic day writing Scala!_\r\n\r\n<details>\r\n<summary>⚙ Adjust future updates</summary>\r\n\r\nAdd this to your `.scala-steward.conf` file to ignore future updates of\r\nthis dependency:\r\n```\r\nupdates.ignore = [ { groupId = \"ch.qos.logback\", artifactId = \"logback-classic\" } ]\r\n```\r\nOr, add this to slow down future updates of this dependency:\r\n```\r\ndependencyOverrides = [{\r\n  pullRequests = { frequency = \"30 days\" },\r\n  dependency = { groupId = \"ch.qos.logback\", artifactId = \"logback-classic\" }\r\n}]\r\n```\r\n</details>\r\n\r\n<sup>\r\nlabels: library-update, early-semver-patch, semver-spec-patch,\r\ncommit-count:1\r\n</sup>\r\n\r\nCo-authored-by: zio-scala-steward[bot] <145262613+zio-scala-steward[bot]@users.noreply.github.com>",
          "timestamp": "2024-08-16T08:27:21+02:00",
          "tree_id": "0140761e1402954ec291c16d9eb1a0ed896c2d82",
          "url": "https://github.com/zio/zio-kafka/commit/2f1affef11b3c6df4c9cd0d6ec07e843b0452add"
        },
        "date": 1723790473446,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 604.3780234940782,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 591.4537160267038,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 545.2776826280771,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 534.5191830509615,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 574.7442629490811,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 587.9437983211415,
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
          "id": "8614f3f21d87b45743118a4574d7172b47fa754b",
          "message": "Update embedded-kafka to 3.7.1.1 (#1300)\n\n## About this PR\r\n📦 Updates\r\n[io.github.embeddedkafka:embedded-kafka](https://github.com/embeddedkafka/embedded-kafka)\r\nfrom `3.7.0` to `3.7.1.1`\r\n\r\n📜 [GitHub Release\r\nNotes](https://github.com/embeddedkafka/embedded-kafka/releases/tag/v3.7.1.1)\r\n- [Version\r\nDiff](https://github.com/embeddedkafka/embedded-kafka/compare/v3.7.0...v3.7.1.1)\r\n\r\n## Usage\r\n✅ **Please merge!**\r\n\r\nI'll automatically update this PR to resolve conflicts as long as you\r\ndon't change it yourself.\r\n\r\nIf you'd like to skip this version, you can just close this PR. If you\r\nhave any feedback, just mention me in the comments below.\r\n\r\nConfigure Scala Steward for your repository with a\r\n[`.scala-steward.conf`](https://github.com/scala-steward-org/scala-steward/blob/767fcfecbfd53c507152f6cf15c846176bae561d/docs/repo-specific-configuration.md)\r\nfile.\r\n\r\n_Have a fantastic day writing Scala!_\r\n\r\n<details>\r\n<summary>⚙ Adjust future updates</summary>\r\n\r\nAdd this to your `.scala-steward.conf` file to ignore future updates of\r\nthis dependency:\r\n```\r\nupdates.ignore = [ { groupId = \"io.github.embeddedkafka\", artifactId = \"embedded-kafka\" } ]\r\n```\r\nOr, add this to slow down future updates of this dependency:\r\n```\r\ndependencyOverrides = [{\r\n  pullRequests = { frequency = \"30 days\" },\r\n  dependency = { groupId = \"io.github.embeddedkafka\", artifactId = \"embedded-kafka\" }\r\n}]\r\n```\r\n</details>\r\n\r\n<sup>\r\nlabels: library-update, version-scheme:semver-spec, commit-count:1\r\n</sup>\r\n\r\nCo-authored-by: zio-scala-steward[bot] <145262613+zio-scala-steward[bot]@users.noreply.github.com>",
          "timestamp": "2024-08-17T09:54:59+02:00",
          "tree_id": "35d25d49557b56a5e53de404ae6382017b3ac275",
          "url": "https://github.com/zio/zio-kafka/commit/8614f3f21d87b45743118a4574d7172b47fa754b"
        },
        "date": 1723882095110,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 600.1128082936312,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 588.6567068350839,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 543.3747501273077,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 529.613143661378,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 578.0133209597835,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 591.6802979600441,
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
          "id": "b842a18a452364b24b93e1418377efdec94049a0",
          "message": "Update sbt-tpolecat to 0.5.2 (#1301)\n\n## About this PR\r\n📦 Updates\r\n[org.typelevel:sbt-tpolecat](https://github.com/typelevel/sbt-tpolecat)\r\nfrom `0.5.1` to `0.5.2`\r\n\r\n📜 [GitHub Release\r\nNotes](https://github.com/typelevel/sbt-tpolecat/releases/tag/v0.5.2) -\r\n[Version\r\nDiff](https://github.com/typelevel/sbt-tpolecat/compare/v0.5.1...v0.5.2)\r\n\r\n## Usage\r\n✅ **Please merge!**\r\n\r\nI'll automatically update this PR to resolve conflicts as long as you\r\ndon't change it yourself.\r\n\r\nIf you'd like to skip this version, you can just close this PR. If you\r\nhave any feedback, just mention me in the comments below.\r\n\r\nConfigure Scala Steward for your repository with a\r\n[`.scala-steward.conf`](https://github.com/scala-steward-org/scala-steward/blob/767fcfecbfd53c507152f6cf15c846176bae561d/docs/repo-specific-configuration.md)\r\nfile.\r\n\r\n_Have a fantastic day writing Scala!_\r\n\r\n<details>\r\n<summary>⚙ Adjust future updates</summary>\r\n\r\nAdd this to your `.scala-steward.conf` file to ignore future updates of\r\nthis dependency:\r\n```\r\nupdates.ignore = [ { groupId = \"org.typelevel\", artifactId = \"sbt-tpolecat\" } ]\r\n```\r\nOr, add this to slow down future updates of this dependency:\r\n```\r\ndependencyOverrides = [{\r\n  pullRequests = { frequency = \"30 days\" },\r\n  dependency = { groupId = \"org.typelevel\", artifactId = \"sbt-tpolecat\" }\r\n}]\r\n```\r\n</details>\r\n\r\n<sup>\r\nlabels: sbt-plugin-update, early-semver-minor, semver-spec-patch,\r\nversion-scheme:early-semver, commit-count:1\r\n</sup>\r\n\r\nCo-authored-by: zio-scala-steward[bot] <145262613+zio-scala-steward[bot]@users.noreply.github.com>",
          "timestamp": "2024-08-20T19:39:00+02:00",
          "tree_id": "26ad1ddaae27992ee8efb9759bbf6a01dc6d0068",
          "url": "https://github.com/zio/zio-kafka/commit/b842a18a452364b24b93e1418377efdec94049a0"
        },
        "date": 1724176319751,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 601.6452700314339,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 590.2847778970951,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 543.7020730682692,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 533.2796696556088,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 548.3089496990268,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 533.873287355785,
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
          "id": "3393fbf4487bb3cc34962a304edf792dd832deee",
          "message": "Revert back to 2.8.0 sendFromQueue implementation in producer (#1304)\n\nDuring running services with the new version of library 2.8.1, I noticed\r\nhuge increase in messages production time to kafka.\r\nSome quick rough tests shoving me around 40x-100x times increase in\r\namount of time taken to `produceChunk` on even 1-10 records chunks\r\ncomparing to version 2.8.0 (or also to version 2.8.1 with the changes in\r\nthis MR). Please, note, it is not a proper benchmarks. Also, to note, Im\r\nusing Scala version 3.3.1.\r\n\r\nThis MR just reverts two MRs updating `ProducersendFromQueue`\r\nimplementation:\r\n - https://github.com/zio/zio-kafka/pull/1272\r\n - https://github.com/zio/zio-kafka/pull/1285\r\n \r\nNot sure if it's possible to revert two MRs at a time (with a single one\r\nfor revert), so created this one.\r\n \r\nI haven't researched yet which exact change/changes are causing such\r\nperformance degradation.\r\n I would suggest the next steps:\r\n  - confirm the problem exists\r\n- reverting to the previous implementation (the one from 2.8.0/this MR)\r\n  - release fixed version (to allow users have a nicely working version)\r\n- investigate & fix problem from the\r\nhttps://github.com/zio/zio-kafka/pull/1272 and/or\r\nhttps://github.com/zio/zio-kafka/pull/1285\r\n\r\nIt is only suggestions on the approach, feel free to ignore them.\r\nAlso, feel free to modify/ignore this MR and treat it as an issue.",
          "timestamp": "2024-08-21T18:44:17+02:00",
          "tree_id": "d365d0277cdc4e715e235a0844c1fa0663fff2a2",
          "url": "https://github.com/zio/zio-kafka/commit/3393fbf4487bb3cc34962a304edf792dd832deee"
        },
        "date": 1724259450321,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 603.3860061464433,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 592.236814952849,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 545.3001686235577,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 532.0928062790063,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 559.370983509081,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 564.0630198168167,
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
          "id": "c607e669b3936e6397e3bc1af2089449e7357783",
          "message": "Update zio-logging-slf4j, ... to 2.3.1 (#1307)\n\n## About this PR\r\n📦 Updates \r\n* [dev.zio:zio-logging-slf4j](https://github.com/zio/zio-logging)\r\n* [dev.zio:zio-logging-slf4j2](https://github.com/zio/zio-logging)\r\n\r\n from `2.3.0` to `2.3.1`\r\n\r\n📜 [GitHub Release\r\nNotes](https://github.com/zio/zio-logging/releases/tag/v2.3.1) -\r\n[Version\r\nDiff](https://github.com/zio/zio-logging/compare/v2.3.0...v2.3.1)\r\n\r\n## Usage\r\n✅ **Please merge!**\r\n\r\nI'll automatically update this PR to resolve conflicts as long as you\r\ndon't change it yourself.\r\n\r\nIf you'd like to skip this version, you can just close this PR. If you\r\nhave any feedback, just mention me in the comments below.\r\n\r\nConfigure Scala Steward for your repository with a\r\n[`.scala-steward.conf`](https://github.com/scala-steward-org/scala-steward/blob/767fcfecbfd53c507152f6cf15c846176bae561d/docs/repo-specific-configuration.md)\r\nfile.\r\n\r\n_Have a fantastic day writing Scala!_\r\n\r\n<details>\r\n<summary>🔍 Files still referring to the old version number</summary>\r\n\r\nThe following files still refer to the old version number (2.3.0).\r\nYou might want to review and update them manually.\r\n```\r\nzio-kafka/src/main/scala/zio/kafka/admin/AdminClient.scala\r\n```\r\n</details>\r\n<details>\r\n<summary>⚙ Adjust future updates</summary>\r\n\r\nAdd this to your `.scala-steward.conf` file to ignore future updates of\r\nthis dependency:\r\n```\r\nupdates.ignore = [ { groupId = \"dev.zio\" } ]\r\n```\r\nOr, add this to slow down future updates of this dependency:\r\n```\r\ndependencyOverrides = [{\r\n  pullRequests = { frequency = \"30 days\" },\r\n  dependency = { groupId = \"dev.zio\" }\r\n}]\r\n```\r\n</details>\r\n\r\n<sup>\r\nlabels: library-update, early-semver-patch, semver-spec-patch,\r\nold-version-remains, commit-count:1\r\n</sup>\r\n\r\nCo-authored-by: zio-scala-steward[bot] <145262613+zio-scala-steward[bot]@users.noreply.github.com>",
          "timestamp": "2024-08-24T09:05:58+02:00",
          "tree_id": "98e72f5ff9eaeaa801c68dcee5f70073e74ba3de",
          "url": "https://github.com/zio/zio-kafka/commit/c607e669b3936e6397e3bc1af2089449e7357783"
        },
        "date": 1724483919520,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 602.5248535318435,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 590.996363622123,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 555.149516974327,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 534.8892174471155,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 593.6087186237837,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 535.33290602292,
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
          "id": "6006cac2929d2f8a32e051c5971f0b3f89168ec4",
          "message": "Update sbt-native-packager to 1.10.4 (#1293)\n\n## About this PR\r\n📦 Updates\r\n[com.github.sbt:sbt-native-packager](https://github.com/sbt/sbt-native-packager)\r\nfrom `1.10.0` to `1.10.4`\r\n\r\n📜 [GitHub Release\r\nNotes](https://github.com/sbt/sbt-native-packager/releases/tag/v1.10.4)\r\n-\r\n[Changelog](https://github.com/sbt/sbt-native-packager/blob/master/CHANGELOG.md)\r\n- [Version\r\nDiff](https://github.com/sbt/sbt-native-packager/compare/v1.10.0...v1.10.4)\r\n\r\n## Usage\r\n✅ **Please merge!**\r\n\r\nI'll automatically update this PR to resolve conflicts as long as you\r\ndon't change it yourself.\r\n\r\nIf you'd like to skip this version, you can just close this PR. If you\r\nhave any feedback, just mention me in the comments below.\r\n\r\nConfigure Scala Steward for your repository with a\r\n[`.scala-steward.conf`](https://github.com/scala-steward-org/scala-steward/blob/767fcfecbfd53c507152f6cf15c846176bae561d/docs/repo-specific-configuration.md)\r\nfile.\r\n\r\n_Have a fantastic day writing Scala!_\r\n\r\n<details>\r\n<summary>⚙ Adjust future updates</summary>\r\n\r\nAdd this to your `.scala-steward.conf` file to ignore future updates of\r\nthis dependency:\r\n```\r\nupdates.ignore = [ { groupId = \"com.github.sbt\", artifactId = \"sbt-native-packager\" } ]\r\n```\r\nOr, add this to slow down future updates of this dependency:\r\n```\r\ndependencyOverrides = [{\r\n  pullRequests = { frequency = \"30 days\" },\r\n  dependency = { groupId = \"com.github.sbt\", artifactId = \"sbt-native-packager\" }\r\n}]\r\n```\r\n</details>\r\n\r\n<sup>\r\nlabels: sbt-plugin-update, early-semver-patch, semver-spec-patch,\r\ncommit-count:1\r\n</sup>\r\n\r\nCo-authored-by: zio-scala-steward[bot] <145262613+zio-scala-steward[bot]@users.noreply.github.com>\r\nCo-authored-by: svroonland <svroonland@users.noreply.github.com>",
          "timestamp": "2024-08-24T09:06:27+02:00",
          "tree_id": "6d658c620ce86d9e2e08c044bb96dfaeaeeec499",
          "url": "https://github.com/zio/zio-kafka/commit/6006cac2929d2f8a32e051c5971f0b3f89168ec4"
        },
        "date": 1724483966301,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 601.9125169340039,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 590.2826309492738,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 545.5972188098077,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 534.8067547248398,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 591.2177621591351,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 600.6852709497257,
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
          "id": "5674dbd0e3e87f40e696439bb47dbda64b1e3bf0",
          "message": "Simple producer benchmark (#1310)\n\nIt's not much, but it gives us something to compare against.\r\n\r\nRelates to #1304",
          "timestamp": "2024-08-25T09:12:33+02:00",
          "tree_id": "6cf54d46cb91f024358bd8f07037f99f94e5224e",
          "url": "https://github.com/zio/zio-kafka/commit/5674dbd0e3e87f40e696439bb47dbda64b1e3bf0"
        },
        "date": 1724570905817,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 603.9740674586218,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 591.3232555170949,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 543.6581362232694,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 533.8808389357373,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 612.1674813278919,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 552.1709013931065,
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
          "id": "35530e86c7e6b5db89fb781c14217c121e54c6ee",
          "message": "Update zio-kafka, zio-kafka-testkit to 2.8.2 (#1306)\n\n## About this PR\r\n📦 Updates \r\n* [dev.zio:zio-kafka](https://github.com/zio/zio-kafka)\r\n* [dev.zio:zio-kafka-testkit](https://github.com/zio/zio-kafka)\r\n\r\n from `2.8.0` to `2.8.2`\r\n\r\n📜 [GitHub Release\r\nNotes](https://github.com/zio/zio-kafka/releases/tag/v2.8.2) - [Version\r\nDiff](https://github.com/zio/zio-kafka/compare/v2.8.0...v2.8.2)\r\n\r\n## Usage\r\n✅ **Please merge!**\r\n\r\nI'll automatically update this PR to resolve conflicts as long as you\r\ndon't change it yourself.\r\n\r\nIf you'd like to skip this version, you can just close this PR. If you\r\nhave any feedback, just mention me in the comments below.\r\n\r\nConfigure Scala Steward for your repository with a\r\n[`.scala-steward.conf`](https://github.com/scala-steward-org/scala-steward/blob/767fcfecbfd53c507152f6cf15c846176bae561d/docs/repo-specific-configuration.md)\r\nfile.\r\n\r\n_Have a fantastic day writing Scala!_\r\n\r\n<details>\r\n<summary>🔍 Files still referring to the old version number</summary>\r\n\r\nThe following files still refer to the old version number (2.8.0).\r\nYou might want to review and update them manually.\r\n```\r\nbuild.sbt\r\n```\r\n</details>\r\n<details>\r\n<summary>⚙ Adjust future updates</summary>\r\n\r\nAdd this to your `.scala-steward.conf` file to ignore future updates of\r\nthis dependency:\r\n```\r\nupdates.ignore = [ { groupId = \"dev.zio\" } ]\r\n```\r\nOr, add this to slow down future updates of this dependency:\r\n```\r\ndependencyOverrides = [{\r\n  pullRequests = { frequency = \"30 days\" },\r\n  dependency = { groupId = \"dev.zio\" }\r\n}]\r\n```\r\n</details>\r\n\r\n<sup>\r\nlabels: library-update, early-semver-patch, semver-spec-patch,\r\nold-version-remains, commit-count:1\r\n</sup>\r\n\r\nCo-authored-by: zio-scala-steward[bot] <145262613+zio-scala-steward[bot]@users.noreply.github.com>",
          "timestamp": "2024-08-25T09:19:36+02:00",
          "tree_id": "f90771f115f67f8c46c8b964b3b59eece3811795",
          "url": "https://github.com/zio/zio-kafka/commit/35530e86c7e6b5db89fb781c14217c121e54c6ee"
        },
        "date": 1724571341434,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 604.9276611305029,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 594.0140477894972,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 545.731456189327,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 533.5744953979166,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 565.2020859924324,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 577.9534222327991,
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
          "id": "4983ef9ede40a19449191e54253adee013e9b6a5",
          "message": "Update to Kafka 3.8.0 (#1313)\n\n📦 Updates [org.apache.kafka:kafka-clients](https://kafka.apache.org/)\r\nfrom 3.7.1 to 3.8.0\r\n\r\n📦 Updates\r\n[io.github.embeddedkafka:embedded-kafka](https://github.com/embeddedkafka/embedded-kafka)\r\nfrom `3.7.1.1` to `3.8.0`\r\n\r\n📜 [GitHub Release\r\nNotes](https://github.com/embeddedkafka/embedded-kafka/releases/tag/v3.8.0)\r\n- [Version\r\nDiff](https://github.com/embeddedkafka/embedded-kafka/compare/v3.7.1.1...v3.8.0)\r\n\r\n---------\r\n\r\nCo-authored-by: zio-scala-steward[bot] <145262613+zio-scala-steward[bot]@users.noreply.github.com>",
          "timestamp": "2024-08-25T11:04:31+02:00",
          "tree_id": "2117de215606ca9b288ece89ae0a5d846b4e5a7e",
          "url": "https://github.com/zio/zio-kafka/commit/4983ef9ede40a19449191e54253adee013e9b6a5"
        },
        "date": 1724577658260,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 602.2700282098324,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 591.0026234677094,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 547.0905912980769,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 537.6121300408654,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 605.9356680856216,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 578.6663030340286,
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
          "id": "55ea0747c8b7bcc092077a7d49c9b2ce13105187",
          "message": "Update zio, zio-streams, zio-test, ... to 2.1.8 (#1312)\n\n## About this PR\r\n📦 Updates \r\n* [dev.zio:zio](https://github.com/zio/zio)\r\n* [dev.zio:zio-streams](https://github.com/zio/zio)\r\n* [dev.zio:zio-test](https://github.com/zio/zio)\r\n* [dev.zio:zio-test-sbt](https://github.com/zio/zio)\r\n\r\n from `2.1.7` to `2.1.8`\r\n\r\n📜 [GitHub Release Notes](https://github.com/zio/zio/releases/tag/v2.1.8)\r\n- [Version Diff](https://github.com/zio/zio/compare/v2.1.7...v2.1.8)\r\n\r\n## Usage\r\n✅ **Please merge!**\r\n\r\nI'll automatically update this PR to resolve conflicts as long as you\r\ndon't change it yourself.\r\n\r\nIf you'd like to skip this version, you can just close this PR. If you\r\nhave any feedback, just mention me in the comments below.\r\n\r\nConfigure Scala Steward for your repository with a\r\n[`.scala-steward.conf`](https://github.com/scala-steward-org/scala-steward/blob/767fcfecbfd53c507152f6cf15c846176bae561d/docs/repo-specific-configuration.md)\r\nfile.\r\n\r\n_Have a fantastic day writing Scala!_\r\n\r\n<details>\r\n<summary>🔍 Files still referring to the old version number</summary>\r\n\r\nThe following files still refer to the old version number (2.1.7).\r\nYou might want to review and update them manually.\r\n```\r\nbuild.sbt\r\n```\r\n</details>\r\n<details>\r\n<summary>⚙ Adjust future updates</summary>\r\n\r\nAdd this to your `.scala-steward.conf` file to ignore future updates of\r\nthis dependency:\r\n```\r\nupdates.ignore = [ { groupId = \"dev.zio\" } ]\r\n```\r\nOr, add this to slow down future updates of this dependency:\r\n```\r\ndependencyOverrides = [{\r\n  pullRequests = { frequency = \"30 days\" },\r\n  dependency = { groupId = \"dev.zio\" }\r\n}]\r\n```\r\n</details>\r\n\r\n<sup>\r\nlabels: library-update, early-semver-patch, semver-spec-patch,\r\nold-version-remains, commit-count:1\r\n</sup>\r\n\r\nCo-authored-by: zio-scala-steward[bot] <145262613+zio-scala-steward[bot]@users.noreply.github.com>\r\nCo-authored-by: Erik van Oosten <e.vanoosten@grons.nl>",
          "timestamp": "2024-08-25T11:32:44+02:00",
          "tree_id": "0e5f56b9ec07183de96cd4f6fb61345a9dc5d753",
          "url": "https://github.com/zio/zio-kafka/commit/55ea0747c8b7bcc092077a7d49c9b2ce13105187"
        },
        "date": 1724579286288,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 601.2546518945251,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 590.5296507020113,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 541.7260649849999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 533.8182791565704,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 546.5524542852971,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 625.977221836224,
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
          "id": "26ff52b5ae45e0881609bea6969a7815cdd38b41",
          "message": "Update sbt-mima-plugin to 1.1.4 (#1296)\n\n## About this PR\r\n📦 Updates\r\n[com.typesafe:sbt-mima-plugin](https://github.com/lightbend-labs/mima)\r\nfrom `1.1.3` to `1.1.4`\r\n\r\n📜 [GitHub Release\r\nNotes](https://github.com/lightbend-labs/mima/releases/tag/1.1.4) -\r\n[Version\r\nDiff](https://github.com/lightbend-labs/mima/compare/1.1.3...1.1.4) -\r\n[Version\r\nDiff](https://github.com/lightbend-labs/mima/compare/release-1.1.3...release-1.1.4)\r\n\r\n## Usage\r\n✅ **Please merge!**\r\n\r\nI'll automatically update this PR to resolve conflicts as long as you\r\ndon't change it yourself.\r\n\r\nIf you'd like to skip this version, you can just close this PR. If you\r\nhave any feedback, just mention me in the comments below.\r\n\r\nConfigure Scala Steward for your repository with a\r\n[`.scala-steward.conf`](https://github.com/scala-steward-org/scala-steward/blob/767fcfecbfd53c507152f6cf15c846176bae561d/docs/repo-specific-configuration.md)\r\nfile.\r\n\r\n_Have a fantastic day writing Scala!_\r\n\r\n<details>\r\n<summary>⚙ Adjust future updates</summary>\r\n\r\nAdd this to your `.scala-steward.conf` file to ignore future updates of\r\nthis dependency:\r\n```\r\nupdates.ignore = [ { groupId = \"com.typesafe\", artifactId = \"sbt-mima-plugin\" } ]\r\n```\r\nOr, add this to slow down future updates of this dependency:\r\n```\r\ndependencyOverrides = [{\r\n  pullRequests = { frequency = \"30 days\" },\r\n  dependency = { groupId = \"com.typesafe\", artifactId = \"sbt-mima-plugin\" }\r\n}]\r\n```\r\n</details>\r\n\r\n<sup>\r\nlabels: sbt-plugin-update, early-semver-patch, semver-spec-patch,\r\nversion-scheme:early-semver, commit-count:1\r\n</sup>\r\n\r\nCo-authored-by: zio-scala-steward[bot] <145262613+zio-scala-steward[bot]@users.noreply.github.com>\r\nCo-authored-by: Erik van Oosten <e.vanoosten@grons.nl>",
          "timestamp": "2024-08-25T12:22:25+02:00",
          "tree_id": "1d12175023b98c2e16f54f16a4d6cb9b64972594",
          "url": "https://github.com/zio/zio-kafka/commit/26ff52b5ae45e0881609bea6969a7815cdd38b41"
        },
        "date": 1724582290343,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 602.4250391740782,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 592.0946562750837,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 545.606045588077,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 536.8804572676282,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 588.2585470002161,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 578.6541162978705,
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
          "id": "41d059d18b76b18875d4434004b614cf4ce514e8",
          "message": "Update zio-streams, zio-test-sbt to 2.1.8 (#1316)\n\n## About this PR\r\n📦 Updates \r\n* [dev.zio:zio-streams](https://github.com/zio/zio)\r\n* [dev.zio:zio-test-sbt](https://github.com/zio/zio)\r\n\r\n from `2.1.7` to `2.1.8`\r\n\r\n📜 [GitHub Release Notes](https://github.com/zio/zio/releases/tag/v2.1.8)\r\n- [Version Diff](https://github.com/zio/zio/compare/v2.1.7...v2.1.8)\r\n\r\n## Usage\r\n✅ **Please merge!**\r\n\r\nI'll automatically update this PR to resolve conflicts as long as you\r\ndon't change it yourself.\r\n\r\nIf you'd like to skip this version, you can just close this PR. If you\r\nhave any feedback, just mention me in the comments below.\r\n\r\nConfigure Scala Steward for your repository with a\r\n[`.scala-steward.conf`](https://github.com/scala-steward-org/scala-steward/blob/767fcfecbfd53c507152f6cf15c846176bae561d/docs/repo-specific-configuration.md)\r\nfile.\r\n\r\n_Have a fantastic day writing Scala!_\r\n\r\n<details>\r\n<summary>⚙ Adjust future updates</summary>\r\n\r\nAdd this to your `.scala-steward.conf` file to ignore future updates of\r\nthis dependency:\r\n```\r\nupdates.ignore = [ { groupId = \"dev.zio\" } ]\r\n```\r\nOr, add this to slow down future updates of this dependency:\r\n```\r\ndependencyOverrides = [{\r\n  pullRequests = { frequency = \"30 days\" },\r\n  dependency = { groupId = \"dev.zio\" }\r\n}]\r\n```\r\n</details>\r\n\r\n<sup>\r\nlabels: library-update, early-semver-patch, semver-spec-patch,\r\ncommit-count:1\r\n</sup>\r\n\r\nCo-authored-by: zio-scala-steward[bot] <145262613+zio-scala-steward[bot]@users.noreply.github.com>",
          "timestamp": "2024-08-26T08:05:56+02:00",
          "tree_id": "b7a4bf78f950aa919f60e6337f35b6c75b77e3b9",
          "url": "https://github.com/zio/zio-kafka/commit/41d059d18b76b18875d4434004b614cf4ce514e8"
        },
        "date": 1724653294051,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 604.4982225359403,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 592.8705985544134,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 545.7364097242307,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 533.2391566091346,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 554.5039734382701,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 568.6849839706697,
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
          "id": "0231c5d30f6c52b0c9e8beeafefebaf0da6147eb",
          "message": "Use AtomicInteger instead of AtomicLong inside Producer.sendFromQueue implementation (#1315)\n\nMinor change (alignment), in case we gonna keep current\r\n`Producer.sendFromQueue` implementation\r\n(https://github.com/zio/zio-kafka/pull/1311#discussion_r1730449835),\r\notherwise, this PR can be closed.",
          "timestamp": "2024-08-26T08:07:12+02:00",
          "tree_id": "cf937291e5044dd73734b7ae56c88b34b087b635",
          "url": "https://github.com/zio/zio-kafka/commit/0231c5d30f6c52b0c9e8beeafefebaf0da6147eb"
        },
        "date": 1724653441722,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 602.3809903520298,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 591.2668406601117,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 542.0421507324038,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 533.4681468633012,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 562.6073348374053,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 546.3490382867179,
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
          "id": "cbed94ce81961e0d5e1d4eeb4d2c84c17413ce72",
          "message": "Produce sequentially and in parallel in benchmark (#1314)\n\nTests `Producer` in sequential and parallel settings.\r\n\r\nAlso, changes the number of iterations to reduce runtime. Because the\r\nresults are now no longer comparable to the previous tests, the existing\r\ntests have been renamed.",
          "timestamp": "2024-08-26T09:00:34+02:00",
          "tree_id": "60655c1878f6647a863399134ed650b80e3bdd21",
          "url": "https://github.com/zio/zio-kafka/commit/cbed94ce81961e0d5e1d4eeb4d2c84c17413ce72"
        },
        "date": 1724656756039,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordPar",
            "value": 16.054540374031305,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeq",
            "value": 13.34200440900119,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 602.2088315354936,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 590.4996908319553,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 158.67168565809527,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 381.91708952666676,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 546.3162375157691,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 536.0608192285255,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 526.9440876090811,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 588.8622620274863,
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
          "id": "381b3a0947db4154fd8d722e716a307fde5fbfa7",
          "message": "Update zio, zio-streams, zio-test, ... to 2.1.9 (#1317)\n\n## About this PR\r\n📦 Updates \r\n* [dev.zio:zio](https://github.com/zio/zio)\r\n* [dev.zio:zio-streams](https://github.com/zio/zio)\r\n* [dev.zio:zio-test](https://github.com/zio/zio)\r\n* [dev.zio:zio-test-sbt](https://github.com/zio/zio)\r\n\r\n from `2.1.8` to `2.1.9`\r\n\r\n📜 [GitHub Release Notes](https://github.com/zio/zio/releases/tag/v2.1.9)\r\n- [Version Diff](https://github.com/zio/zio/compare/v2.1.8...v2.1.9)\r\n\r\n## Usage\r\n✅ **Please merge!**\r\n\r\nI'll automatically update this PR to resolve conflicts as long as you\r\ndon't change it yourself.\r\n\r\nIf you'd like to skip this version, you can just close this PR. If you\r\nhave any feedback, just mention me in the comments below.\r\n\r\nConfigure Scala Steward for your repository with a\r\n[`.scala-steward.conf`](https://github.com/scala-steward-org/scala-steward/blob/767fcfecbfd53c507152f6cf15c846176bae561d/docs/repo-specific-configuration.md)\r\nfile.\r\n\r\n_Have a fantastic day writing Scala!_\r\n\r\n<details>\r\n<summary>🔍 Files still referring to the old version number</summary>\r\n\r\nThe following files still refer to the old version number (2.1.8).\r\nYou might want to review and update them manually.\r\n```\r\nbuild.sbt\r\n```\r\n</details>\r\n<details>\r\n<summary>⚙ Adjust future updates</summary>\r\n\r\nAdd this to your `.scala-steward.conf` file to ignore future updates of\r\nthis dependency:\r\n```\r\nupdates.ignore = [ { groupId = \"dev.zio\" } ]\r\n```\r\nOr, add this to slow down future updates of this dependency:\r\n```\r\ndependencyOverrides = [{\r\n  pullRequests = { frequency = \"30 days\" },\r\n  dependency = { groupId = \"dev.zio\" }\r\n}]\r\n```\r\n</details>\r\n\r\n<sup>\r\nlabels: library-update, early-semver-patch, semver-spec-patch,\r\nold-version-remains, commit-count:1\r\n</sup>\r\n\r\nCo-authored-by: zio-scala-steward[bot] <145262613+zio-scala-steward[bot]@users.noreply.github.com>",
          "timestamp": "2024-08-30T08:43:35+02:00",
          "tree_id": "740f99146b8b06c2df3eeb9eec8ac49ca9a3d315",
          "url": "https://github.com/zio/zio-kafka/commit/381b3a0947db4154fd8d722e716a307fde5fbfa7"
        },
        "date": 1725001344080,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordPar",
            "value": 11.968528562121186,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeq",
            "value": 9.90182604390042,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 603.8309801077096,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 592.6706441927373,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 207.4421896329524,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 518.9782645066666,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 548.4593211646153,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 535.0088454362178,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 576.8124848617297,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 549.7765421722064,
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
          "id": "1ecadba47d5c425247d03edb0f35216f7b6223e7",
          "message": "Update zio-streams, zio-test-sbt to 2.1.9 (#1320)\n\n## About this PR\r\n📦 Updates \r\n* [dev.zio:zio-streams](https://github.com/zio/zio)\r\n* [dev.zio:zio-test-sbt](https://github.com/zio/zio)\r\n\r\n from `2.1.8` to `2.1.9`\r\n\r\n📜 [GitHub Release Notes](https://github.com/zio/zio/releases/tag/v2.1.9)\r\n- [Version Diff](https://github.com/zio/zio/compare/v2.1.8...v2.1.9)\r\n\r\n## Usage\r\n✅ **Please merge!**\r\n\r\nI'll automatically update this PR to resolve conflicts as long as you\r\ndon't change it yourself.\r\n\r\nIf you'd like to skip this version, you can just close this PR. If you\r\nhave any feedback, just mention me in the comments below.\r\n\r\nConfigure Scala Steward for your repository with a\r\n[`.scala-steward.conf`](https://github.com/scala-steward-org/scala-steward/blob/767fcfecbfd53c507152f6cf15c846176bae561d/docs/repo-specific-configuration.md)\r\nfile.\r\n\r\n_Have a fantastic day writing Scala!_\r\n\r\n<details>\r\n<summary>⚙ Adjust future updates</summary>\r\n\r\nAdd this to your `.scala-steward.conf` file to ignore future updates of\r\nthis dependency:\r\n```\r\nupdates.ignore = [ { groupId = \"dev.zio\" } ]\r\n```\r\nOr, add this to slow down future updates of this dependency:\r\n```\r\ndependencyOverrides = [{\r\n  pullRequests = { frequency = \"30 days\" },\r\n  dependency = { groupId = \"dev.zio\" }\r\n}]\r\n```\r\n</details>\r\n\r\n<sup>\r\nlabels: library-update, early-semver-patch, semver-spec-patch,\r\ncommit-count:1\r\n</sup>\r\n\r\nCo-authored-by: zio-scala-steward[bot] <145262613+zio-scala-steward[bot]@users.noreply.github.com>",
          "timestamp": "2024-08-31T09:21:04+02:00",
          "tree_id": "1400e52923e751b9315d0ab8a21172f4553eef2e",
          "url": "https://github.com/zio/zio-kafka/commit/1ecadba47d5c425247d03edb0f35216f7b6223e7"
        },
        "date": 1725089944460,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordPar",
            "value": 15.706038787436507,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeq",
            "value": 12.626890696422896,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 602.331518283501,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 589.8159582875978,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 160.98510526476193,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 372.1928821566666,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 542.5551535850003,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 537.4122402371794,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 580.4815112299459,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 538.7484418304282,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "ashwinbhskr@gmail.com",
            "name": "Ashwin Bhaskar",
            "username": "ashwinbhaskar"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "99e32003aa48aa686b97a31fdf0235b49fd930d7",
          "message": "Fixes #1318 Adds documentation about auto commit behaviour (#1319)",
          "timestamp": "2024-08-31T09:24:41+02:00",
          "tree_id": "d94efdd9ee8bfb181e08726be3de2a0ecd8f2dbf",
          "url": "https://github.com/zio/zio-kafka/commit/99e32003aa48aa686b97a31fdf0235b49fd930d7"
        },
        "date": 1725090170572,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordPar",
            "value": 19.033387849040206,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeq",
            "value": 15.464496338059002,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 600.8977516318809,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 589.7780525601116,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 119.12972916500001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 303.4522818879999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 545.6915915658655,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 533.6476706745193,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 582.2419400007566,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 572.5722481675522,
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
          "id": "03832dae9c415501bace3bbd54469183bcae0404",
          "message": "Update logback-classic to 1.5.8 (#1322)\n\n## About this PR\r\n📦 Updates\r\n[ch.qos.logback:logback-classic](https://github.com/qos-ch/logback) from\r\n`1.5.7` to `1.5.8`\r\n\r\n## Usage\r\n✅ **Please merge!**\r\n\r\nI'll automatically update this PR to resolve conflicts as long as you\r\ndon't change it yourself.\r\n\r\nIf you'd like to skip this version, you can just close this PR. If you\r\nhave any feedback, just mention me in the comments below.\r\n\r\nConfigure Scala Steward for your repository with a\r\n[`.scala-steward.conf`](https://github.com/scala-steward-org/scala-steward/blob/767fcfecbfd53c507152f6cf15c846176bae561d/docs/repo-specific-configuration.md)\r\nfile.\r\n\r\n_Have a fantastic day writing Scala!_\r\n\r\n<details>\r\n<summary>⚙ Adjust future updates</summary>\r\n\r\nAdd this to your `.scala-steward.conf` file to ignore future updates of\r\nthis dependency:\r\n```\r\nupdates.ignore = [ { groupId = \"ch.qos.logback\", artifactId = \"logback-classic\" } ]\r\n```\r\nOr, add this to slow down future updates of this dependency:\r\n```\r\ndependencyOverrides = [{\r\n  pullRequests = { frequency = \"30 days\" },\r\n  dependency = { groupId = \"ch.qos.logback\", artifactId = \"logback-classic\" }\r\n}]\r\n```\r\n</details>\r\n\r\n<sup>\r\nlabels: library-update, early-semver-patch, semver-spec-patch,\r\ncommit-count:1\r\n</sup>\r\n\r\nCo-authored-by: zio-scala-steward[bot] <145262613+zio-scala-steward[bot]@users.noreply.github.com>",
          "timestamp": "2024-09-07T12:35:55+02:00",
          "tree_id": "a0f721ddba7005b17fd666b1b826aca4e30d1ed5",
          "url": "https://github.com/zio/zio-kafka/commit/03832dae9c415501bace3bbd54469183bcae0404"
        },
        "date": 1725706427470,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordPar",
            "value": 17.345953234682266,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeq",
            "value": 13.979166371525155,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 599.9256880618996,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 589.4884513564245,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 142.1806511521429,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 355.97733826,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 543.3106992744231,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 535.2967224716346,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 608.8528457766487,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 580.7738718972558,
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
          "id": "023f11ef52d97abb940866cc211e658199ba2952",
          "message": "More precise batch publish errors (#1321)\n\nThis increases the precision of the result of producer method\r\n`produceChunkAsyncWithFailures` for cases where something went wrong.\r\nThere are two changes compared to before:\r\n\r\n1. Each entry in the result of`produceChunkAsyncWithFailures` now\r\naccurately corresponds to each record in the input chunk. Previously, if\r\nsending fails directly (*) on any of the given records, the error would\r\nbe used for _all_ records in the batch, ignoring the send-outcome of the\r\nother records. An advantage of this change is that if sending some\r\nrecords failed, but some other records were actually sent, you can now\r\ncorrectly see all of that in the method's response.\r\n2. In addition, if sending fails directly for a record (*), we no longer\r\nattempt to send subsequent records from the input. The result contains a\r\n`PublishOmittedException` for each record that is not sent. When\r\nimplementing retries, this change makes it easier to publish records in\r\nthe original order.\r\n\r\nIn addition, we introduce unit-level tests for the producer.\r\n\r\n(*) By 'sending' we mean offering a record to the underlying java Kafka\r\nproducer. Sending can fail directly (when we call the method), or later\r\non (from a callback).",
          "timestamp": "2024-09-11T10:22:08+02:00",
          "tree_id": "4f3a022f577d0d9e93be58b184f412fb0061e7f7",
          "url": "https://github.com/zio/zio-kafka/commit/023f11ef52d97abb940866cc211e658199ba2952"
        },
        "date": 1726044049181,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordPar",
            "value": 17.13433186132887,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeq",
            "value": 13.924870537399018,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 602.0613631491993,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 590.1024869736314,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 144.63028519214285,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 395.8452982733334,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 549.4278737105768,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 537.7782492384615,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 596.6006044112434,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 535.5096790951482,
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
          "id": "956c2f3eee0dfe06b02f4da82f9cbaaa23c956a7",
          "message": "Optimize Producer sendFromQueue implementation (#1326)\n\nFrom java's producer send method doc: `Note that callbacks will\r\ngenerally execute in the I/O thread of the producer and so should be\r\nreasonably fast or they will delay the sending of messages from other\r\nthreads`.\r\nI believe we should try to keep callback implementation fast, but with\r\nrecent changes we added to provide proper error responses, we make the\r\ncallback slower. In general we are talking approximately about 1-3\r\nmicroseconds (current master) instead of 0,1-0,2 microseconds (version\r\n2.8.2) for single callback execution time on my laptop. I believe it was\r\nreasonable, but at the same time wanted to keep implementation fast, and\r\nprovided the fastest version I come up with at that time.\r\nNow, I got with an idea of faster implementation (with keeping the same\r\nproper error semantics). With this implementation single callback\r\nexecution takes approximately 0,05-0,2 microseconds (even a bit faster\r\nthan version 2.8.2).",
          "timestamp": "2024-09-17T18:49:58+02:00",
          "tree_id": "fb8629bdb9510fee9a85d9791ce495847bb4483a",
          "url": "https://github.com/zio/zio-kafka/commit/956c2f3eee0dfe06b02f4da82f9cbaaa23c956a7"
        },
        "date": 1726592902213,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordPar",
            "value": 15.420746769103467,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeq",
            "value": 11.449144902204758,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 601.8374048895346,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 591.7316189301677,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 169.40162406838098,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 457.47760698000013,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 545.952880938173,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 534.4991753884616,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 567.2240873977296,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 539.2125736219539,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "49699333+dependabot[bot]@users.noreply.github.com",
            "name": "dependabot[bot]",
            "username": "dependabot[bot]"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "26118a81d31c9834d24201236979766be40bd6e5",
          "message": "Bump scala-steward-org/scala-steward-action from 2.65.0 to 2.69.0 (#1328)\n\nBumps\r\n[scala-steward-org/scala-steward-action](https://github.com/scala-steward-org/scala-steward-action)\r\nfrom 2.65.0 to 2.69.0.\r\n<details>\r\n<summary>Release notes</summary>\r\n<p><em>Sourced from <a\r\nhref=\"https://github.com/scala-steward-org/scala-steward-action/releases\">scala-steward-org/scala-steward-action's\r\nreleases</a>.</em></p>\r\n<blockquote>\r\n<h2>v2.69.0</h2>\r\n<!-- raw HTML omitted -->\r\n<h2>What's Changed</h2>\r\n<h3>Other Changes</h3>\r\n<ul>\r\n<li>Cancel GitHub token refresh at the end by <a\r\nhref=\"https://github.com/EnviousSwan\"><code>@​EnviousSwan</code></a> in\r\n<a\r\nhref=\"https://redirect.github.com/scala-steward-org/scala-steward-action/pull/635\">scala-steward-org/scala-steward-action#635</a></li>\r\n</ul>\r\n<p><strong>Full Changelog</strong>: <a\r\nhref=\"https://github.com/scala-steward-org/scala-steward-action/compare/v2.68.0...v2.69.0\">https://github.com/scala-steward-org/scala-steward-action/compare/v2.68.0...v2.69.0</a></p>\r\n<h2>v2.68.0</h2>\r\n<!-- raw HTML omitted -->\r\n<h2>What's Changed</h2>\r\n<h3>Other Changes</h3>\r\n<ul>\r\n<li>Refresh token every 50 minutes by <a\r\nhref=\"https://github.com/EnviousSwan\"><code>@​EnviousSwan</code></a> in\r\n<a\r\nhref=\"https://redirect.github.com/scala-steward-org/scala-steward-action/pull/633\">scala-steward-org/scala-steward-action#633</a></li>\r\n</ul>\r\n<p><strong>Full Changelog</strong>: <a\r\nhref=\"https://github.com/scala-steward-org/scala-steward-action/compare/v2.67.0...v2.68.0\">https://github.com/scala-steward-org/scala-steward-action/compare/v2.67.0...v2.68.0</a></p>\r\n<h2>v2.67.0</h2>\r\n<!-- raw HTML omitted -->\r\n<h2>What's Changed</h2>\r\n<h3>Other Changes</h3>\r\n<ul>\r\n<li>Save workspace cache when run failed by <a\r\nhref=\"https://github.com/EnviousSwan\"><code>@​EnviousSwan</code></a> in\r\n<a\r\nhref=\"https://redirect.github.com/scala-steward-org/scala-steward-action/pull/631\">scala-steward-org/scala-steward-action#631</a></li>\r\n</ul>\r\n<h2>New Contributors</h2>\r\n<ul>\r\n<li><a\r\nhref=\"https://github.com/EnviousSwan\"><code>@​EnviousSwan</code></a>\r\nmade their first contribution in <a\r\nhref=\"https://redirect.github.com/scala-steward-org/scala-steward-action/pull/631\">scala-steward-org/scala-steward-action#631</a></li>\r\n</ul>\r\n<p><strong>Full Changelog</strong>: <a\r\nhref=\"https://github.com/scala-steward-org/scala-steward-action/compare/v2.66.0...v2.67.0\">https://github.com/scala-steward-org/scala-steward-action/compare/v2.66.0...v2.67.0</a></p>\r\n<h2>v2.66.0</h2>\r\n<!-- raw HTML omitted -->\r\n<h2>What's Changed</h2>\r\n<h3>Other Changes</h3>\r\n<ul>\r\n<li>Update README.md by <a\r\nhref=\"https://github.com/counter2015\"><code>@​counter2015</code></a> in\r\n<a\r\nhref=\"https://redirect.github.com/scala-steward-org/scala-steward-action/pull/580\">scala-steward-org/scala-steward-action#580</a></li>\r\n<li>Document JAVA_OPTS by <a\r\nhref=\"https://github.com/mdedetrich\"><code>@​mdedetrich</code></a> in <a\r\nhref=\"https://redirect.github.com/scala-steward-org/scala-steward-action/pull/592\">scala-steward-org/scala-steward-action#592</a></li>\r\n<li>Clarify GitHub App workflow in README by <a\r\nhref=\"https://github.com/SpecialThing44\"><code>@​SpecialThing44</code></a>\r\nin <a\r\nhref=\"https://redirect.github.com/scala-steward-org/scala-steward-action/pull/618\">scala-steward-org/scala-steward-action#618</a></li>\r\n</ul>\r\n<h2>New Contributors</h2>\r\n<ul>\r\n<li><a\r\nhref=\"https://github.com/counter2015\"><code>@​counter2015</code></a>\r\nmade their first contribution in <a\r\nhref=\"https://redirect.github.com/scala-steward-org/scala-steward-action/pull/580\">scala-steward-org/scala-steward-action#580</a></li>\r\n<li><a\r\nhref=\"https://github.com/mdedetrich\"><code>@​mdedetrich</code></a> made\r\ntheir first contribution in <a\r\nhref=\"https://redirect.github.com/scala-steward-org/scala-steward-action/pull/592\">scala-steward-org/scala-steward-action#592</a></li>\r\n<li><a\r\nhref=\"https://github.com/SpecialThing44\"><code>@​SpecialThing44</code></a>\r\nmade their first contribution in <a\r\nhref=\"https://redirect.github.com/scala-steward-org/scala-steward-action/pull/618\">scala-steward-org/scala-steward-action#618</a></li>\r\n</ul>\r\n<p><strong>Full Changelog</strong>: <a\r\nhref=\"https://github.com/scala-steward-org/scala-steward-action/compare/v2.65.0...v2.66.0\">https://github.com/scala-steward-org/scala-steward-action/compare/v2.65.0...v2.66.0</a></p>\r\n</blockquote>\r\n</details>\r\n<details>\r\n<summary>Commits</summary>\r\n<ul>\r\n<li><a\r\nhref=\"https://github.com/scala-steward-org/scala-steward-action/commit/7d71624a38186e82b082a3583e14502cb4b77177\"><code>7d71624</code></a>\r\nRelease v2.69.0</li>\r\n<li><a\r\nhref=\"https://github.com/scala-steward-org/scala-steward-action/commit/7292c0b7085eb26ad8f043fc79ce21f7cfb02ffe\"><code>7292c0b</code></a>\r\nMerge pull request <a\r\nhref=\"https://redirect.github.com/scala-steward-org/scala-steward-action/issues/635\">#635</a>\r\nfrom EnviousSwan/cancel-token-refresh-at-the-end</li>\r\n<li><a\r\nhref=\"https://github.com/scala-steward-org/scala-steward-action/commit/ba67ff8d62ee53380ac3ac4479d66765c540a6bb\"><code>ba67ff8</code></a>\r\nCancel GitHub token refresh at the end</li>\r\n<li><a\r\nhref=\"https://github.com/scala-steward-org/scala-steward-action/commit/74a9a6721857590f393c53be6d1aa6461846a805\"><code>74a9a67</code></a>\r\nMerge pull request <a\r\nhref=\"https://redirect.github.com/scala-steward-org/scala-steward-action/issues/633\">#633</a>\r\nfrom EnviousSwan/use-installation-octokit</li>\r\n<li><a\r\nhref=\"https://github.com/scala-steward-org/scala-steward-action/commit/e2d6f01edcbbf17df47067be88e85122d2e5d7a2\"><code>e2d6f01</code></a>\r\nRefresh token every 50 minutes</li>\r\n<li><a\r\nhref=\"https://github.com/scala-steward-org/scala-steward-action/commit/d9619bf92fdefed8b339f7ee929ed83ba55525d5\"><code>d9619bf</code></a>\r\nMerge pull request <a\r\nhref=\"https://redirect.github.com/scala-steward-org/scala-steward-action/issues/631\">#631</a>\r\nfrom EnviousSwan/save-workspace-cache-when-failed</li>\r\n<li><a\r\nhref=\"https://github.com/scala-steward-org/scala-steward-action/commit/f84b59a89e7b7621e8200f588ac5276a029b011f\"><code>f84b59a</code></a>\r\nSave workspace cache when run failed</li>\r\n<li><a\r\nhref=\"https://github.com/scala-steward-org/scala-steward-action/commit/2763313e6149238002decdaa10ce630868ab2822\"><code>2763313</code></a>\r\nBump actions/upload-artifact from 4.3.4 to 4.4.0 (<a\r\nhref=\"https://redirect.github.com/scala-steward-org/scala-steward-action/issues/627\">#627</a>)</li>\r\n<li><a\r\nhref=\"https://github.com/scala-steward-org/scala-steward-action/commit/7ac15432ba517ba88f84922f35adce8c5c9cb462\"><code>7ac1543</code></a>\r\nBump <code>@​types/node</code> from 20.14.9 to 22.5.1 (<a\r\nhref=\"https://redirect.github.com/scala-steward-org/scala-steward-action/issues/629\">#629</a>)</li>\r\n<li><a\r\nhref=\"https://github.com/scala-steward-org/scala-steward-action/commit/880a26d6fa970fc89b760acc7fd98e0de91fd3ed\"><code>880a26d</code></a>\r\nBump ts-pattern from 5.2.0 to 5.3.1 (<a\r\nhref=\"https://redirect.github.com/scala-steward-org/scala-steward-action/issues/628\">#628</a>)</li>\r\n<li>Additional commits viewable in <a\r\nhref=\"https://github.com/scala-steward-org/scala-steward-action/compare/v2.65.0...v2.69.0\">compare\r\nview</a></li>\r\n</ul>\r\n</details>\r\n<br />\r\n\r\n\r\n[![Dependabot compatibility\r\nscore](https://dependabot-badges.githubapp.com/badges/compatibility_score?dependency-name=scala-steward-org/scala-steward-action&package-manager=github_actions&previous-version=2.65.0&new-version=2.69.0)](https://docs.github.com/en/github/managing-security-vulnerabilities/about-dependabot-security-updates#about-compatibility-scores)\r\n\r\nDependabot will resolve any conflicts with this PR as long as you don't\r\nalter it yourself. You can also trigger a rebase manually by commenting\r\n`@dependabot rebase`.\r\n\r\n[//]: # (dependabot-automerge-start)\r\n[//]: # (dependabot-automerge-end)\r\n\r\n---\r\n\r\n<details>\r\n<summary>Dependabot commands and options</summary>\r\n<br />\r\n\r\nYou can trigger Dependabot actions by commenting on this PR:\r\n- `@dependabot rebase` will rebase this PR\r\n- `@dependabot recreate` will recreate this PR, overwriting any edits\r\nthat have been made to it\r\n- `@dependabot merge` will merge this PR after your CI passes on it\r\n- `@dependabot squash and merge` will squash and merge this PR after\r\nyour CI passes on it\r\n- `@dependabot cancel merge` will cancel a previously requested merge\r\nand block automerging\r\n- `@dependabot reopen` will reopen this PR if it is closed\r\n- `@dependabot close` will close this PR and stop Dependabot recreating\r\nit. You can achieve the same result by closing it manually\r\n- `@dependabot show <dependency name> ignore conditions` will show all\r\nof the ignore conditions of the specified dependency\r\n- `@dependabot ignore this major version` will close this PR and stop\r\nDependabot creating any more for this major version (unless you reopen\r\nthe PR or upgrade to it yourself)\r\n- `@dependabot ignore this minor version` will close this PR and stop\r\nDependabot creating any more for this minor version (unless you reopen\r\nthe PR or upgrade to it yourself)\r\n- `@dependabot ignore this dependency` will close this PR and stop\r\nDependabot creating any more for this dependency (unless you reopen the\r\nPR or upgrade to it yourself)\r\n\r\n\r\n</details>\r\n\r\nSigned-off-by: dependabot[bot] <support@github.com>\r\nCo-authored-by: dependabot[bot] <49699333+dependabot[bot]@users.noreply.github.com>",
          "timestamp": "2024-09-17T18:50:57+02:00",
          "tree_id": "486e31323e63977e9169d67afbb05a159fe028db",
          "url": "https://github.com/zio/zio-kafka/commit/26118a81d31c9834d24201236979766be40bd6e5"
        },
        "date": 1726592955462,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordPar",
            "value": 15.234205255810664,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeq",
            "value": 12.951484468107262,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 603.638454812812,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 591.3398718177655,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 176.32026805714287,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 426.96135387000004,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 541.6103280331731,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 537.1041577778846,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 558.3442476538377,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 506.92771603670684,
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
          "id": "8c2d98cf6d73d0dea340bc497d0049f9659bfdbe",
          "message": "Update sbt to 1.10.2 (#1327)\n\n## About this PR\r\n📦 Updates [org.scala-sbt:sbt](https://github.com/sbt/sbt) from `1.10.1`\r\nto `1.10.2`\r\n\r\n## Usage\r\n✅ **Please merge!**\r\n\r\nI'll automatically update this PR to resolve conflicts as long as you\r\ndon't change it yourself.\r\n\r\nIf you'd like to skip this version, you can just close this PR. If you\r\nhave any feedback, just mention me in the comments below.\r\n\r\nConfigure Scala Steward for your repository with a\r\n[`.scala-steward.conf`](https://github.com/scala-steward-org/scala-steward/blob/767fcfecbfd53c507152f6cf15c846176bae561d/docs/repo-specific-configuration.md)\r\nfile.\r\n\r\n_Have a fantastic day writing Scala!_\r\n\r\n<details>\r\n<summary>⚙ Adjust future updates</summary>\r\n\r\nAdd this to your `.scala-steward.conf` file to ignore future updates of\r\nthis dependency:\r\n```\r\nupdates.ignore = [ { groupId = \"org.scala-sbt\", artifactId = \"sbt\" } ]\r\n```\r\nOr, add this to slow down future updates of this dependency:\r\n```\r\ndependencyOverrides = [{\r\n  pullRequests = { frequency = \"30 days\" },\r\n  dependency = { groupId = \"org.scala-sbt\", artifactId = \"sbt\" }\r\n}]\r\n```\r\n</details>\r\n\r\n<sup>\r\nlabels: library-update, early-semver-patch, semver-spec-patch,\r\nversion-scheme:early-semver, commit-count:1\r\n</sup>\r\n\r\nCo-authored-by: zio-scala-steward[bot] <145262613+zio-scala-steward[bot]@users.noreply.github.com>",
          "timestamp": "2024-09-17T18:50:22+02:00",
          "tree_id": "fc77bce35241900ffd644462995cc512a5203eb9",
          "url": "https://github.com/zio/zio-kafka/commit/8c2d98cf6d73d0dea340bc497d0049f9659bfdbe"
        },
        "date": 1726592971316,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordPar",
            "value": 11.00744860669301,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeq",
            "value": 9.529121059628018,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 605.8230506621974,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 594.1997869833519,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 240.24437155333334,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 536.8434998266666,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 545.7020443854808,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 534.2089459306089,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 565.4330671667027,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 624.9317329292645,
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
          "id": "c22ed31c7e762f036b5a498b2ab6394920702d31",
          "message": "Document scala 3 plus `zio-kafka-testkit` (#1329)\n\nThis solves #1193.",
          "timestamp": "2024-09-18T11:12:54+02:00",
          "tree_id": "619ea128d6b18f1c2534cfde9c84e3ba4ad123db",
          "url": "https://github.com/zio/zio-kafka/commit/c22ed31c7e762f036b5a498b2ab6394920702d31"
        },
        "date": 1726651851041,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordPar",
            "value": 16.25705111759862,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeq",
            "value": 12.661772806236108,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 602.8237897592553,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 591.4208427137429,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 148.4790146342857,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 374.8274649166667,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 549.0715749495192,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 533.8005535605769,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 592.458223848,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 582.719419090933,
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
          "id": "b3440ed9cc9a411ce927c0372c7a7561c0a42acd",
          "message": "Update scala-library to 2.13.15 (#1334)\n\n## About this PR\r\n📦 Updates [org.scala-lang:scala-library](https://github.com/scala/scala)\r\nfrom `2.13.14` to `2.13.15`\r\n\r\n📜 [GitHub Release\r\nNotes](https://github.com/scala/scala/releases/tag/v2.13.15) - [Version\r\nDiff](https://github.com/scala/scala/compare/v2.13.14...v2.13.15)\r\n\r\n## Usage\r\n✅ **Please merge!**\r\n\r\nI'll automatically update this PR to resolve conflicts as long as you\r\ndon't change it yourself.\r\n\r\nIf you'd like to skip this version, you can just close this PR. If you\r\nhave any feedback, just mention me in the comments below.\r\n\r\nConfigure Scala Steward for your repository with a\r\n[`.scala-steward.conf`](https://github.com/scala-steward-org/scala-steward/blob/767fcfecbfd53c507152f6cf15c846176bae561d/docs/repo-specific-configuration.md)\r\nfile.\r\n\r\n_Have a fantastic day writing Scala!_\r\n\r\n<details>\r\n<summary>⚙ Adjust future updates</summary>\r\n\r\nAdd this to your `.scala-steward.conf` file to ignore future updates of\r\nthis dependency:\r\n```\r\nupdates.ignore = [ { groupId = \"org.scala-lang\", artifactId = \"scala-library\" } ]\r\n```\r\nOr, add this to slow down future updates of this dependency:\r\n```\r\ndependencyOverrides = [{\r\n  pullRequests = { frequency = \"30 days\" },\r\n  dependency = { groupId = \"org.scala-lang\", artifactId = \"scala-library\" }\r\n}]\r\n```\r\n</details>\r\n\r\n<sup>\r\nlabels: library-update, early-semver-patch, semver-spec-patch,\r\ncommit-count:1\r\n</sup>\r\n\r\nCo-authored-by: zio-scala-steward[bot] <145262613+zio-scala-steward[bot]@users.noreply.github.com>",
          "timestamp": "2024-09-26T08:53:58+02:00",
          "tree_id": "5db5a44286f54be41154f73f6841578240853e98",
          "url": "https://github.com/zio/zio-kafka/commit/b3440ed9cc9a411ce927c0372c7a7561c0a42acd"
        },
        "date": 1727334804689,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordPar",
            "value": 14.634238913204019,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeq",
            "value": 11.74645641237375,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 603.9243199722905,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 593.7503378225698,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 174.31416052952383,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 461.71848604666667,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 565.8946669747114,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 538.8388403032051,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 518.1533527738379,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 491.1347312813173,
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
          "id": "b6200b39ebe599f32f68cb46c302ab54fc0c7d1f",
          "message": "Update sbt-scalafix to 0.13.0 (#1335)\n\n## About this PR\r\n📦 Updates\r\n[ch.epfl.scala:sbt-scalafix](https://github.com/scalacenter/sbt-scalafix)\r\nfrom `0.12.1` to `0.13.0`\r\n\r\n📜 [GitHub Release\r\nNotes](https://github.com/scalacenter/sbt-scalafix/releases/tag/v0.13.0)\r\n- [Version\r\nDiff](https://github.com/scalacenter/sbt-scalafix/compare/v0.12.1...v0.13.0)\r\n\r\n## Usage\r\n✅ **Please merge!**\r\n\r\nI'll automatically update this PR to resolve conflicts as long as you\r\ndon't change it yourself.\r\n\r\nIf you'd like to skip this version, you can just close this PR. If you\r\nhave any feedback, just mention me in the comments below.\r\n\r\nConfigure Scala Steward for your repository with a\r\n[`.scala-steward.conf`](https://github.com/scala-steward-org/scala-steward/blob/767fcfecbfd53c507152f6cf15c846176bae561d/docs/repo-specific-configuration.md)\r\nfile.\r\n\r\n_Have a fantastic day writing Scala!_\r\n\r\n<details>\r\n<summary>⚙ Adjust future updates</summary>\r\n\r\nAdd this to your `.scala-steward.conf` file to ignore future updates of\r\nthis dependency:\r\n```\r\nupdates.ignore = [ { groupId = \"ch.epfl.scala\", artifactId = \"sbt-scalafix\" } ]\r\n```\r\nOr, add this to slow down future updates of this dependency:\r\n```\r\ndependencyOverrides = [{\r\n  pullRequests = { frequency = \"30 days\" },\r\n  dependency = { groupId = \"ch.epfl.scala\", artifactId = \"sbt-scalafix\" }\r\n}]\r\n```\r\n</details>\r\n\r\n<sup>\r\nlabels: sbt-plugin-update, early-semver-major, semver-spec-minor,\r\ncommit-count:1\r\n</sup>\r\n\r\nCo-authored-by: zio-scala-steward[bot] <145262613+zio-scala-steward[bot]@users.noreply.github.com>",
          "timestamp": "2024-09-28T10:56:35+02:00",
          "tree_id": "b4bfa08fda70012ff64cd1d8bec3b935c190d1ce",
          "url": "https://github.com/zio/zio-kafka/commit/b6200b39ebe599f32f68cb46c302ab54fc0c7d1f"
        },
        "date": 1727514883805,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordPar",
            "value": 14.480700584127728,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeq",
            "value": 10.7905577421133,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 602.7187908250281,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 591.0500302520669,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 212.36558617333333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 477.0550631366668,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 543.9121662773077,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 538.148571540064,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 530.6817703375135,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 516.5400729617563,
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
          "id": "3b0cce0000967d1fa02d86ce922d5e8ad0a455d1",
          "message": "Update scala3-library to 3.3.4 (#1336)\n\n## About this PR\r\n📦 Updates\r\n[org.scala-lang:scala3-library](https://github.com/scala/scala3) from\r\n`3.3.3` to `3.3.4`\r\n\r\n📜 [GitHub Release\r\nNotes](https://github.com/scala/scala3/releases/tag/3.3.4) - [Version\r\nDiff](https://github.com/scala/scala3/compare/3.3.3...3.3.4) - [Version\r\nDiff](https://github.com/scala/scala3/compare/release-3.3.3...release-3.3.4)\r\n\r\n## Usage\r\n✅ **Please merge!**\r\n\r\nI'll automatically update this PR to resolve conflicts as long as you\r\ndon't change it yourself.\r\n\r\nIf you'd like to skip this version, you can just close this PR. If you\r\nhave any feedback, just mention me in the comments below.\r\n\r\nConfigure Scala Steward for your repository with a\r\n[`.scala-steward.conf`](https://github.com/scala-steward-org/scala-steward/blob/767fcfecbfd53c507152f6cf15c846176bae561d/docs/repo-specific-configuration.md)\r\nfile.\r\n\r\n_Have a fantastic day writing Scala!_\r\n\r\n<details>\r\n<summary>⚙ Adjust future updates</summary>\r\n\r\nAdd this to your `.scala-steward.conf` file to ignore future updates of\r\nthis dependency:\r\n```\r\nupdates.ignore = [ { groupId = \"org.scala-lang\", artifactId = \"scala3-library\" } ]\r\n```\r\nOr, add this to slow down future updates of this dependency:\r\n```\r\ndependencyOverrides = [{\r\n  pullRequests = { frequency = \"30 days\" },\r\n  dependency = { groupId = \"org.scala-lang\", artifactId = \"scala3-library\" }\r\n}]\r\n```\r\n</details>\r\n\r\n<sup>\r\nlabels: library-update, early-semver-patch, semver-spec-patch,\r\nversion-scheme:semver-spec, commit-count:1\r\n</sup>\r\n\r\nCo-authored-by: zio-scala-steward[bot] <145262613+zio-scala-steward[bot]@users.noreply.github.com>\r\nCo-authored-by: Erik van Oosten <e.vanoosten@grons.nl>",
          "timestamp": "2024-09-28T12:23:53+02:00",
          "tree_id": "9d7ab6813e06bb07e6baf42bbbc5296ee2d6f530",
          "url": "https://github.com/zio/zio-kafka/commit/3b0cce0000967d1fa02d86ce922d5e8ad0a455d1"
        },
        "date": 1727520160844,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordPar",
            "value": 11.275312064870668,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeq",
            "value": 8.58216588136407,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 604.9870458633147,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 594.2309979024581,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 252.19388451666669,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 582.7103290999999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 547.2747495740384,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 539.572053516346,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 579.4106054419459,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 562.8285946943578,
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
          "id": "9cc273a4e0b9d31a117547da203360ab0dfecfcf",
          "message": "Document Serdes in more depth (#1337)\n\nFixes #1331.",
          "timestamp": "2024-10-08T19:31:53+02:00",
          "tree_id": "9346dad6b683c7f1bd73572f72f0c85e34294916",
          "url": "https://github.com/zio/zio-kafka/commit/9cc273a4e0b9d31a117547da203360ab0dfecfcf"
        },
        "date": 1728409784119,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordPar",
            "value": 18.83999852821456,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeq",
            "value": 15.944494427357629,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 602.451609122309,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 590.4960883732961,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 119.1094685362222,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 332.66632978333325,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 543.8266262516346,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 536.216419554487,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 480.0173062456216,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 546.9147699029638,
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
          "id": "a0f45cacc3b2f4513866f9e9c572ac7126adb47c",
          "message": "Update logback-classic to 1.5.9 (#1340)\n\n## About this PR\r\n📦 Updates\r\n[ch.qos.logback:logback-classic](https://github.com/qos-ch/logback) from\r\n`1.5.8` to `1.5.9`\r\n\r\n## Usage\r\n✅ **Please merge!**\r\n\r\nI'll automatically update this PR to resolve conflicts as long as you\r\ndon't change it yourself.\r\n\r\nIf you'd like to skip this version, you can just close this PR. If you\r\nhave any feedback, just mention me in the comments below.\r\n\r\nConfigure Scala Steward for your repository with a\r\n[`.scala-steward.conf`](https://github.com/scala-steward-org/scala-steward/blob/767fcfecbfd53c507152f6cf15c846176bae561d/docs/repo-specific-configuration.md)\r\nfile.\r\n\r\n_Have a fantastic day writing Scala!_\r\n\r\n<details>\r\n<summary>⚙ Adjust future updates</summary>\r\n\r\nAdd this to your `.scala-steward.conf` file to ignore future updates of\r\nthis dependency:\r\n```\r\nupdates.ignore = [ { groupId = \"ch.qos.logback\", artifactId = \"logback-classic\" } ]\r\n```\r\nOr, add this to slow down future updates of this dependency:\r\n```\r\ndependencyOverrides = [{\r\n  pullRequests = { frequency = \"30 days\" },\r\n  dependency = { groupId = \"ch.qos.logback\", artifactId = \"logback-classic\" }\r\n}]\r\n```\r\n</details>\r\n\r\n<sup>\r\nlabels: library-update, early-semver-patch, semver-spec-patch,\r\ncommit-count:1\r\n</sup>\r\n\r\nCo-authored-by: zio-scala-steward[bot] <145262613+zio-scala-steward[bot]@users.noreply.github.com>",
          "timestamp": "2024-10-09T09:23:09+02:00",
          "tree_id": "8a5843dbc75c1e394c734d77da027b126292fc49",
          "url": "https://github.com/zio/zio-kafka/commit/a0f45cacc3b2f4513866f9e9c572ac7126adb47c"
        },
        "date": 1728459664988,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordPar",
            "value": 18.969860077496786,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeq",
            "value": 16.396903014562767,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 602.0488525129981,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 590.210246722011,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 115.49064666755558,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 321.9952571233334,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 541.798992721827,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 535.1884794025642,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 508.5002469755676,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 528.8595756220417,
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
          "id": "a719525d36763b41c4027f516b10508e55288f1a",
          "message": "Update zio, zio-streams, zio-test, ... to 2.1.11 (#1342)\n\n## About this PR\r\n📦 Updates \r\n* [dev.zio:zio](https://github.com/zio/zio)\r\n* [dev.zio:zio-streams](https://github.com/zio/zio)\r\n* [dev.zio:zio-test](https://github.com/zio/zio)\r\n* [dev.zio:zio-test-sbt](https://github.com/zio/zio)\r\n\r\n from `2.1.9` to `2.1.11`\r\n\r\n📜 [GitHub Release\r\nNotes](https://github.com/zio/zio/releases/tag/v2.1.11) - [Version\r\nDiff](https://github.com/zio/zio/compare/v2.1.9...v2.1.11)\r\n\r\n## Usage\r\n✅ **Please merge!**\r\n\r\nI'll automatically update this PR to resolve conflicts as long as you\r\ndon't change it yourself.\r\n\r\nIf you'd like to skip this version, you can just close this PR. If you\r\nhave any feedback, just mention me in the comments below.\r\n\r\nConfigure Scala Steward for your repository with a\r\n[`.scala-steward.conf`](https://github.com/scala-steward-org/scala-steward/blob/767fcfecbfd53c507152f6cf15c846176bae561d/docs/repo-specific-configuration.md)\r\nfile.\r\n\r\n_Have a fantastic day writing Scala!_\r\n\r\n<details>\r\n<summary>🔍 Files still referring to the old version number</summary>\r\n\r\nThe following files still refer to the old version number (2.1.9).\r\nYou might want to review and update them manually.\r\n```\r\nbuild.sbt\r\n```\r\n</details>\r\n<details>\r\n<summary>⚙ Adjust future updates</summary>\r\n\r\nAdd this to your `.scala-steward.conf` file to ignore future updates of\r\nthis dependency:\r\n```\r\nupdates.ignore = [ { groupId = \"dev.zio\" } ]\r\n```\r\nOr, add this to slow down future updates of this dependency:\r\n```\r\ndependencyOverrides = [{\r\n  pullRequests = { frequency = \"30 days\" },\r\n  dependency = { groupId = \"dev.zio\" }\r\n}]\r\n```\r\n</details>\r\n\r\n<sup>\r\nlabels: library-update, early-semver-patch, semver-spec-patch,\r\nold-version-remains, commit-count:1\r\n</sup>\r\n\r\nCo-authored-by: zio-scala-steward[bot] <145262613+zio-scala-steward[bot]@users.noreply.github.com>",
          "timestamp": "2024-10-17T10:44:53+02:00",
          "tree_id": "0dfe91dd21e4cec61787154ab88df2ec5c7114b9",
          "url": "https://github.com/zio/zio-kafka/commit/a719525d36763b41c4027f516b10508e55288f1a"
        },
        "date": 1729155830281,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordPar",
            "value": 13.930778251398149,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeq",
            "value": 11.333622359068132,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 603.7540762556425,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 592.3217653368714,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 150.51822007228571,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 472.44526651000007,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 543.8388681344231,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 535.6078841988781,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 605.8277502924324,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 530.6454201573657,
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
          "id": "da9bab5174b7a90d8be176033532e301d0697027",
          "message": "Update zio-logging-slf4j, ... to 2.3.2 (#1343)\n\n## About this PR\r\n📦 Updates \r\n* [dev.zio:zio-logging-slf4j](https://github.com/zio/zio-logging)\r\n* [dev.zio:zio-logging-slf4j2](https://github.com/zio/zio-logging)\r\n\r\n from `2.3.1` to `2.3.2`\r\n\r\n📜 [GitHub Release\r\nNotes](https://github.com/zio/zio-logging/releases/tag/v2.3.2) -\r\n[Version\r\nDiff](https://github.com/zio/zio-logging/compare/v2.3.1...v2.3.2)\r\n\r\n## Usage\r\n✅ **Please merge!**\r\n\r\nI'll automatically update this PR to resolve conflicts as long as you\r\ndon't change it yourself.\r\n\r\nIf you'd like to skip this version, you can just close this PR. If you\r\nhave any feedback, just mention me in the comments below.\r\n\r\nConfigure Scala Steward for your repository with a\r\n[`.scala-steward.conf`](https://github.com/scala-steward-org/scala-steward/blob/767fcfecbfd53c507152f6cf15c846176bae561d/docs/repo-specific-configuration.md)\r\nfile.\r\n\r\n_Have a fantastic day writing Scala!_\r\n\r\n<details>\r\n<summary>🔍 Files still referring to the old version number</summary>\r\n\r\nThe following files still refer to the old version number (2.3.1).\r\nYou might want to review and update them manually.\r\n```\r\nzio-kafka-testkit/src/main/scala/zio/kafka/testkit/KafkaTestUtils.scala\r\n```\r\n</details>\r\n<details>\r\n<summary>⚙ Adjust future updates</summary>\r\n\r\nAdd this to your `.scala-steward.conf` file to ignore future updates of\r\nthis dependency:\r\n```\r\nupdates.ignore = [ { groupId = \"dev.zio\" } ]\r\n```\r\nOr, add this to slow down future updates of this dependency:\r\n```\r\ndependencyOverrides = [{\r\n  pullRequests = { frequency = \"30 days\" },\r\n  dependency = { groupId = \"dev.zio\" }\r\n}]\r\n```\r\n</details>\r\n\r\n<sup>\r\nlabels: library-update, early-semver-patch, semver-spec-patch,\r\nold-version-remains, commit-count:1\r\n</sup>\r\n\r\nCo-authored-by: zio-scala-steward[bot] <145262613+zio-scala-steward[bot]@users.noreply.github.com>",
          "timestamp": "2024-10-17T10:45:04+02:00",
          "tree_id": "6aebb804899081ae56aa1fc7ce4d2dde05be9487",
          "url": "https://github.com/zio/zio-kafka/commit/da9bab5174b7a90d8be176033532e301d0697027"
        },
        "date": 1729155831494,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordPar",
            "value": 15.300406418662016,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeq",
            "value": 12.361777684707297,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 608.2043039255122,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 599.4905395165363,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 174.2742802657143,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 381.3095119666666,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 550.7352147991347,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 543.5023077091345,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 569.8720551132973,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 580.1321774075082,
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
          "id": "8d6e84d19fb770e26284e9f4f3e0c778b596e38f",
          "message": "Update logback-classic to 1.5.11 (#1341)\n\n## About this PR\r\n📦 Updates\r\n[ch.qos.logback:logback-classic](https://github.com/qos-ch/logback) from\r\n`1.5.9` to `1.5.11`\r\n\r\n## Usage\r\n✅ **Please merge!**\r\n\r\nI'll automatically update this PR to resolve conflicts as long as you\r\ndon't change it yourself.\r\n\r\nIf you'd like to skip this version, you can just close this PR. If you\r\nhave any feedback, just mention me in the comments below.\r\n\r\nConfigure Scala Steward for your repository with a\r\n[`.scala-steward.conf`](https://github.com/scala-steward-org/scala-steward/blob/767fcfecbfd53c507152f6cf15c846176bae561d/docs/repo-specific-configuration.md)\r\nfile.\r\n\r\n_Have a fantastic day writing Scala!_\r\n\r\n<details>\r\n<summary>⚙ Adjust future updates</summary>\r\n\r\nAdd this to your `.scala-steward.conf` file to ignore future updates of\r\nthis dependency:\r\n```\r\nupdates.ignore = [ { groupId = \"ch.qos.logback\", artifactId = \"logback-classic\" } ]\r\n```\r\nOr, add this to slow down future updates of this dependency:\r\n```\r\ndependencyOverrides = [{\r\n  pullRequests = { frequency = \"30 days\" },\r\n  dependency = { groupId = \"ch.qos.logback\", artifactId = \"logback-classic\" }\r\n}]\r\n```\r\n</details>\r\n\r\n<sup>\r\nlabels: library-update, early-semver-patch, semver-spec-patch,\r\ncommit-count:1\r\n</sup>\r\n\r\nCo-authored-by: zio-scala-steward[bot] <145262613+zio-scala-steward[bot]@users.noreply.github.com>",
          "timestamp": "2024-10-17T10:54:59+02:00",
          "tree_id": "b749d8ebbe96fdcc9344c44e9b60dd0f71d2b6e1",
          "url": "https://github.com/zio/zio-kafka/commit/8d6e84d19fb770e26284e9f4f3e0c778b596e38f"
        },
        "date": 1729156412624,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordPar",
            "value": 13.809885447324255,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeq",
            "value": 8.604523375454185,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 605.3230541475606,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 592.576843776313,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 191.9363601506667,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 533.32006456,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 544.225413109327,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 533.8682798278845,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 565.6942370659458,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 554.7968827465203,
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
          "id": "66e57a0ae8a91c6f7d7affd27dd1f892df43c553",
          "message": "Update zio-streams, zio-test-sbt to 2.1.11 (#1344)\n\n## About this PR\r\n📦 Updates \r\n* [dev.zio:zio-streams](https://github.com/zio/zio)\r\n* [dev.zio:zio-test-sbt](https://github.com/zio/zio)\r\n\r\n from `2.1.9` to `2.1.11`\r\n\r\n📜 [GitHub Release\r\nNotes](https://github.com/zio/zio/releases/tag/v2.1.11) - [Version\r\nDiff](https://github.com/zio/zio/compare/v2.1.9...v2.1.11)\r\n\r\n## Usage\r\n✅ **Please merge!**\r\n\r\nI'll automatically update this PR to resolve conflicts as long as you\r\ndon't change it yourself.\r\n\r\nIf you'd like to skip this version, you can just close this PR. If you\r\nhave any feedback, just mention me in the comments below.\r\n\r\nConfigure Scala Steward for your repository with a\r\n[`.scala-steward.conf`](https://github.com/scala-steward-org/scala-steward/blob/767fcfecbfd53c507152f6cf15c846176bae561d/docs/repo-specific-configuration.md)\r\nfile.\r\n\r\n_Have a fantastic day writing Scala!_\r\n\r\n<details>\r\n<summary>⚙ Adjust future updates</summary>\r\n\r\nAdd this to your `.scala-steward.conf` file to ignore future updates of\r\nthis dependency:\r\n```\r\nupdates.ignore = [ { groupId = \"dev.zio\" } ]\r\n```\r\nOr, add this to slow down future updates of this dependency:\r\n```\r\ndependencyOverrides = [{\r\n  pullRequests = { frequency = \"30 days\" },\r\n  dependency = { groupId = \"dev.zio\" }\r\n}]\r\n```\r\n</details>\r\n\r\n<sup>\r\nlabels: library-update, early-semver-patch, semver-spec-patch,\r\ncommit-count:1\r\n</sup>\r\n\r\nCo-authored-by: zio-scala-steward[bot] <145262613+zio-scala-steward[bot]@users.noreply.github.com>",
          "timestamp": "2024-10-18T07:29:25+02:00",
          "tree_id": "52a0f1f78b9dd547232fb1cdbe49efa5270d1513",
          "url": "https://github.com/zio/zio-kafka/commit/66e57a0ae8a91c6f7d7affd27dd1f892df43c553"
        },
        "date": 1729230474800,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordPar",
            "value": 18.10919921056032,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeq",
            "value": 14.857980554053078,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 603.6571506010429,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 592.2333520890502,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 145.34746407028575,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 373.08770238,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 544.457063923077,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 542.988084022596,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 536.5210264877838,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 547.8013767401098,
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
          "id": "c1e4def762223bbd3514cf881b30595335266c5d",
          "message": "Update sbt to 1.10.3 (#1346)\n\n## About this PR\r\n📦 Updates [org.scala-sbt:sbt](https://github.com/sbt/sbt) from `1.10.2`\r\nto `1.10.3`\r\n\r\n## Usage\r\n✅ **Please merge!**\r\n\r\nI'll automatically update this PR to resolve conflicts as long as you\r\ndon't change it yourself.\r\n\r\nIf you'd like to skip this version, you can just close this PR. If you\r\nhave any feedback, just mention me in the comments below.\r\n\r\nConfigure Scala Steward for your repository with a\r\n[`.scala-steward.conf`](https://github.com/scala-steward-org/scala-steward/blob/767fcfecbfd53c507152f6cf15c846176bae561d/docs/repo-specific-configuration.md)\r\nfile.\r\n\r\n_Have a fantastic day writing Scala!_\r\n\r\n<details>\r\n<summary>⚙ Adjust future updates</summary>\r\n\r\nAdd this to your `.scala-steward.conf` file to ignore future updates of\r\nthis dependency:\r\n```\r\nupdates.ignore = [ { groupId = \"org.scala-sbt\", artifactId = \"sbt\" } ]\r\n```\r\nOr, add this to slow down future updates of this dependency:\r\n```\r\ndependencyOverrides = [{\r\n  pullRequests = { frequency = \"30 days\" },\r\n  dependency = { groupId = \"org.scala-sbt\", artifactId = \"sbt\" }\r\n}]\r\n```\r\n</details>\r\n\r\n<sup>\r\nlabels: library-update, early-semver-patch, semver-spec-patch,\r\nversion-scheme:early-semver, commit-count:1\r\n</sup>\r\n\r\nCo-authored-by: zio-scala-steward[bot] <145262613+zio-scala-steward[bot]@users.noreply.github.com>",
          "timestamp": "2024-10-20T10:04:16+02:00",
          "tree_id": "95965f9e25aff7eb15cdc991265f6f399ed953df",
          "url": "https://github.com/zio/zio-kafka/commit/c1e4def762223bbd3514cf881b30595335266c5d"
        },
        "date": 1729412627300,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordPar",
            "value": 11.579604230056136,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeq",
            "value": 7.234282114208163,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 611.3501010003725,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 602.4561668411175,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 245.77621862933336,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 436.09759095000004,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 547.8104049943269,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 542.20971283125,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 668.1059054837837,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 571.0698743260592,
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
          "id": "eabda823c65223775b94eb5af3aa513225329e4e",
          "message": "Update logback-classic to 1.5.12 (#1347)\n\n## About this PR\r\n📦 Updates\r\n[ch.qos.logback:logback-classic](https://github.com/qos-ch/logback) from\r\n`1.5.11` to `1.5.12`\r\n\r\n## Usage\r\n✅ **Please merge!**\r\n\r\nI'll automatically update this PR to resolve conflicts as long as you\r\ndon't change it yourself.\r\n\r\nIf you'd like to skip this version, you can just close this PR. If you\r\nhave any feedback, just mention me in the comments below.\r\n\r\nConfigure Scala Steward for your repository with a\r\n[`.scala-steward.conf`](https://github.com/scala-steward-org/scala-steward/blob/767fcfecbfd53c507152f6cf15c846176bae561d/docs/repo-specific-configuration.md)\r\nfile.\r\n\r\n_Have a fantastic day writing Scala!_\r\n\r\n<details>\r\n<summary>⚙ Adjust future updates</summary>\r\n\r\nAdd this to your `.scala-steward.conf` file to ignore future updates of\r\nthis dependency:\r\n```\r\nupdates.ignore = [ { groupId = \"ch.qos.logback\", artifactId = \"logback-classic\" } ]\r\n```\r\nOr, add this to slow down future updates of this dependency:\r\n```\r\ndependencyOverrides = [{\r\n  pullRequests = { frequency = \"30 days\" },\r\n  dependency = { groupId = \"ch.qos.logback\", artifactId = \"logback-classic\" }\r\n}]\r\n```\r\n</details>\r\n\r\n<sup>\r\nlabels: library-update, early-semver-patch, semver-spec-patch,\r\ncommit-count:1\r\n</sup>\r\n\r\nCo-authored-by: zio-scala-steward[bot] <145262613+zio-scala-steward[bot]@users.noreply.github.com>",
          "timestamp": "2024-10-26T09:21:00+02:00",
          "tree_id": "c10edf61a52f709435c8b533095459683b2b8e59",
          "url": "https://github.com/zio/zio-kafka/commit/eabda823c65223775b94eb5af3aa513225329e4e"
        },
        "date": 1729928415356,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordPar",
            "value": 13.035693062465143,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeq",
            "value": 9.412816084798973,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 601.3484608482682,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 591.5252388246927,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 184.3772214199524,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 629.77467992,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 545.5948003713463,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 536.5831292857372,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 583.2750287915676,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 559.8375596661251,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "49699333+dependabot[bot]@users.noreply.github.com",
            "name": "dependabot[bot]",
            "username": "dependabot[bot]"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "4cd5733a847f947dac7a7e759b23a879f06133e0",
          "message": "Bump benchmark-action/github-action-benchmark from 1.20.3 to 1.20.4 (#1348)\n\nBumps\r\n[benchmark-action/github-action-benchmark](https://github.com/benchmark-action/github-action-benchmark)\r\nfrom 1.20.3 to 1.20.4.\r\n<details>\r\n<summary>Release notes</summary>\r\n<p><em>Sourced from <a\r\nhref=\"https://github.com/benchmark-action/github-action-benchmark/releases\">benchmark-action/github-action-benchmark's\r\nreleases</a>.</em></p>\r\n<blockquote>\r\n<h2>v1.20.4</h2>\r\n<ul>\r\n<li><strong>feat</strong> add typings and validation workflow (<a\r\nhref=\"https://redirect.github.com/benchmark-action/github-action-benchmark/issues/257\">#257</a>)</li>\r\n</ul>\r\n<p><strong>Full Changelog</strong>: <a\r\nhref=\"https://github.com/benchmark-action/github-action-benchmark/compare/v1.20.3...v1.20.4\">https://github.com/benchmark-action/github-action-benchmark/compare/v1.20.3...v1.20.4</a></p>\r\n</blockquote>\r\n</details>\r\n<details>\r\n<summary>Changelog</summary>\r\n<p><em>Sourced from <a\r\nhref=\"https://github.com/benchmark-action/github-action-benchmark/blob/master/CHANGELOG.md\">benchmark-action/github-action-benchmark's\r\nchangelog</a>.</em></p>\r\n<blockquote>\r\n<h1><a\r\nhref=\"https://github.com/benchmark-action/github-action-benchmark/releases/tag/v1.20.4\">v1.20.4</a>\r\n- 23 Oct 2024</h1>\r\n<ul>\r\n<li><strong>feat</strong> add typings and validation workflow (<a\r\nhref=\"https://redirect.github.com/benchmark-action/github-action-benchmark/issues/257\">#257</a>)</li>\r\n</ul>\r\n<p><!-- raw HTML omitted --><!-- raw HTML omitted --></p>\r\n</blockquote>\r\n</details>\r\n<details>\r\n<summary>Commits</summary>\r\n<ul>\r\n<li><a\r\nhref=\"https://github.com/benchmark-action/github-action-benchmark/commit/d48d326b4ca9ba73ca0cd0d59f108f9e02a381c7\"><code>d48d326</code></a>\r\nrelease v1.20.4</li>\r\n<li>See full diff in <a\r\nhref=\"https://github.com/benchmark-action/github-action-benchmark/compare/v1.20.3...v1.20.4\">compare\r\nview</a></li>\r\n</ul>\r\n</details>\r\n<br />\r\n\r\n\r\n[![Dependabot compatibility\r\nscore](https://dependabot-badges.githubapp.com/badges/compatibility_score?dependency-name=benchmark-action/github-action-benchmark&package-manager=github_actions&previous-version=1.20.3&new-version=1.20.4)](https://docs.github.com/en/github/managing-security-vulnerabilities/about-dependabot-security-updates#about-compatibility-scores)\r\n\r\nDependabot will resolve any conflicts with this PR as long as you don't\r\nalter it yourself. You can also trigger a rebase manually by commenting\r\n`@dependabot rebase`.\r\n\r\n[//]: # (dependabot-automerge-start)\r\n[//]: # (dependabot-automerge-end)\r\n\r\n---\r\n\r\n<details>\r\n<summary>Dependabot commands and options</summary>\r\n<br />\r\n\r\nYou can trigger Dependabot actions by commenting on this PR:\r\n- `@dependabot rebase` will rebase this PR\r\n- `@dependabot recreate` will recreate this PR, overwriting any edits\r\nthat have been made to it\r\n- `@dependabot merge` will merge this PR after your CI passes on it\r\n- `@dependabot squash and merge` will squash and merge this PR after\r\nyour CI passes on it\r\n- `@dependabot cancel merge` will cancel a previously requested merge\r\nand block automerging\r\n- `@dependabot reopen` will reopen this PR if it is closed\r\n- `@dependabot close` will close this PR and stop Dependabot recreating\r\nit. You can achieve the same result by closing it manually\r\n- `@dependabot show <dependency name> ignore conditions` will show all\r\nof the ignore conditions of the specified dependency\r\n- `@dependabot ignore this major version` will close this PR and stop\r\nDependabot creating any more for this major version (unless you reopen\r\nthe PR or upgrade to it yourself)\r\n- `@dependabot ignore this minor version` will close this PR and stop\r\nDependabot creating any more for this minor version (unless you reopen\r\nthe PR or upgrade to it yourself)\r\n- `@dependabot ignore this dependency` will close this PR and stop\r\nDependabot creating any more for this dependency (unless you reopen the\r\nPR or upgrade to it yourself)\r\n\r\n\r\n</details>\r\n\r\nSigned-off-by: dependabot[bot] <support@github.com>\r\nCo-authored-by: dependabot[bot] <49699333+dependabot[bot]@users.noreply.github.com>",
          "timestamp": "2024-10-28T07:43:20+01:00",
          "tree_id": "6733bb7e9dbacd6bd3a23716d9e67d5ae596a380",
          "url": "https://github.com/zio/zio-kafka/commit/4cd5733a847f947dac7a7e759b23a879f06133e0"
        },
        "date": 1730098914001,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordPar",
            "value": 16.25467553985262,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeq",
            "value": 13.088898250273692,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 607.39273039419,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 596.088343243352,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 153.2041222647619,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 338.24033290333335,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 550.2689121046153,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 544.0465719371795,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 549.1764292073512,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 572.2845538481668,
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
          "id": "aa3bc7c5bdec0e4998f5b2e8f95751ea7880babd",
          "message": "Update sbt to 1.10.4 (#1349)\n\n## About this PR\r\n📦 Updates [org.scala-sbt:sbt](https://github.com/sbt/sbt) from `1.10.3`\r\nto `1.10.4`\r\n\r\n📜 [GitHub Release\r\nNotes](https://github.com/sbt/sbt/releases/tag/v1.10.4) - [Version\r\nDiff](https://github.com/sbt/sbt/compare/v1.10.3...v1.10.4)\r\n\r\n## Usage\r\n✅ **Please merge!**\r\n\r\nI'll automatically update this PR to resolve conflicts as long as you\r\ndon't change it yourself.\r\n\r\nIf you'd like to skip this version, you can just close this PR. If you\r\nhave any feedback, just mention me in the comments below.\r\n\r\nConfigure Scala Steward for your repository with a\r\n[`.scala-steward.conf`](https://github.com/scala-steward-org/scala-steward/blob/767fcfecbfd53c507152f6cf15c846176bae561d/docs/repo-specific-configuration.md)\r\nfile.\r\n\r\n_Have a fantastic day writing Scala!_\r\n\r\n<details>\r\n<summary>⚙ Adjust future updates</summary>\r\n\r\nAdd this to your `.scala-steward.conf` file to ignore future updates of\r\nthis dependency:\r\n```\r\nupdates.ignore = [ { groupId = \"org.scala-sbt\", artifactId = \"sbt\" } ]\r\n```\r\nOr, add this to slow down future updates of this dependency:\r\n```\r\ndependencyOverrides = [{\r\n  pullRequests = { frequency = \"30 days\" },\r\n  dependency = { groupId = \"org.scala-sbt\", artifactId = \"sbt\" }\r\n}]\r\n```\r\n</details>\r\n\r\n<sup>\r\nlabels: library-update, early-semver-patch, semver-spec-patch,\r\nversion-scheme:early-semver, commit-count:1\r\n</sup>\r\n\r\nCo-authored-by: zio-scala-steward[bot] <145262613+zio-scala-steward[bot]@users.noreply.github.com>",
          "timestamp": "2024-10-29T18:24:38+01:00",
          "tree_id": "a55547cc184e0df6215dba10d626919188c87d3b",
          "url": "https://github.com/zio/zio-kafka/commit/aa3bc7c5bdec0e4998f5b2e8f95751ea7880babd"
        },
        "date": 1730223773703,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordPar",
            "value": 11.960004673951953,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeq",
            "value": 11.661436375156956,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 602.7141375780261,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 591.203156679218,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 175.66739154323807,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 415.6621208333334,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 548.8462585962501,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 539.7536885198717,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 598.9494049511351,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 594.9217789650055,
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
          "id": "5e4b28715bb6301e0937080a9079fb0b268bed06",
          "message": "Remove lost partitions from assigned streams (#1350)\n\nFixes #1288. See also #1233 and #1250.\r\n\r\nWhen all partitions are lost after some connection issue to the broker,\r\nthe streams for lost partitions are ended but polling stops, due to the\r\nconditions in `Runloop.State#shouldPoll`. This PR fixes this by removing\r\nthe lost partition streams from the `assignedStreams` in the state,\r\nthereby not disabling polling.\r\n\r\nAlso adds a warning that is logged whenever the assigned partitions\r\n(according to the apache kafka consumer) are different from the assigned\r\nstreams, which helps to identify other issues or any future regressions\r\nof this issue.\r\n\r\n~Still needs a good test, the `MockConsumer` used in other tests\r\nunfortunately does not allow simulating lost partitions, and the exact\r\nbehavior of the kafka client in this situation is hard to predict..~\r\nIncludes a test that fails when undoing the change to Runloop",
          "timestamp": "2024-10-29T20:16:03+01:00",
          "tree_id": "11f490e39cc62dee5fff60954b25fe765dc5fc05",
          "url": "https://github.com/zio/zio-kafka/commit/5e4b28715bb6301e0937080a9079fb0b268bed06"
        },
        "date": 1730230442456,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordPar",
            "value": 15.92051577065514,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeq",
            "value": 12.59351484243552,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 604.2259337432403,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 594.0737644985476,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 161.61903602690478,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 440.17385820666675,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 545.0146760008654,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 531.7228469200319,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 593.1982080778379,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 556.6729954878595,
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
          "id": "fbaa2d582211d02055fb06ca9d055c350d4170a8",
          "message": "Enable mima check during release (#1351)\n\nThis automatically enables a binary compatibility check using Mima when\r\nwe build a release (when we build from a tag).",
          "timestamp": "2024-10-29T20:51:45+01:00",
          "tree_id": "a87d64316eea7ec8ede25afa0d672b6d0f13970d",
          "url": "https://github.com/zio/zio-kafka/commit/fbaa2d582211d02055fb06ca9d055c350d4170a8"
        },
        "date": 1730232612463,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordPar",
            "value": 13.60114114631265,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeq",
            "value": 11.343804104335653,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 603.5492198483427,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 590.9975858365362,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 190.45533936885712,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 471.93016126000003,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 542.264449589423,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 531.8618356160256,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 574.934255413946,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 545.3871007706695,
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
          "id": "84eabfe75c78300a30a442bfe2253671148bb2bc",
          "message": "Update zio-kafka, zio-kafka-testkit to 2.8.3 (#1353)\n\n## About this PR\r\n📦 Updates \r\n* [dev.zio:zio-kafka](https://github.com/zio/zio-kafka)\r\n* [dev.zio:zio-kafka-testkit](https://github.com/zio/zio-kafka)\r\n\r\n from `2.8.2` to `2.8.3`\r\n\r\n📜 [GitHub Release\r\nNotes](https://github.com/zio/zio-kafka/releases/tag/v2.8.3) - [Version\r\nDiff](https://github.com/zio/zio-kafka/compare/v2.8.2...v2.8.3)\r\n\r\n## Usage\r\n✅ **Please merge!**\r\n\r\nI'll automatically update this PR to resolve conflicts as long as you\r\ndon't change it yourself.\r\n\r\nIf you'd like to skip this version, you can just close this PR. If you\r\nhave any feedback, just mention me in the comments below.\r\n\r\nConfigure Scala Steward for your repository with a\r\n[`.scala-steward.conf`](https://github.com/scala-steward-org/scala-steward/blob/767fcfecbfd53c507152f6cf15c846176bae561d/docs/repo-specific-configuration.md)\r\nfile.\r\n\r\n_Have a fantastic day writing Scala!_\r\n\r\n<details>\r\n<summary>🔍 Files still referring to the old version number</summary>\r\n\r\nThe following files still refer to the old version number (2.8.2).\r\nYou might want to review and update them manually.\r\n```\r\nREADME.md\r\nbuild.sbt\r\n```\r\n</details>\r\n<details>\r\n<summary>⚙ Adjust future updates</summary>\r\n\r\nAdd this to your `.scala-steward.conf` file to ignore future updates of\r\nthis dependency:\r\n```\r\nupdates.ignore = [ { groupId = \"dev.zio\" } ]\r\n```\r\nOr, add this to slow down future updates of this dependency:\r\n```\r\ndependencyOverrides = [{\r\n  pullRequests = { frequency = \"30 days\" },\r\n  dependency = { groupId = \"dev.zio\" }\r\n}]\r\n```\r\n</details>\r\n\r\n<sup>\r\nlabels: library-update, early-semver-patch, semver-spec-patch,\r\nold-version-remains, commit-count:1\r\n</sup>\r\n\r\nCo-authored-by: zio-scala-steward[bot] <145262613+zio-scala-steward[bot]@users.noreply.github.com>",
          "timestamp": "2024-10-31T08:24:06+01:00",
          "tree_id": "14b1f5f4e873397b6d2934fd8c83868a96946298",
          "url": "https://github.com/zio/zio-kafka/commit/84eabfe75c78300a30a442bfe2253671148bb2bc"
        },
        "date": 1730360551950,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordPar",
            "value": 10.958079824924189,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeq",
            "value": 8.771132896956875,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 601.8890418935569,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 590.6019465324023,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 243.10440270104763,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 652.66719256,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 543.4695354589422,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 534.3759369209935,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 601.4864312776216,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 550.3310172754335,
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
          "id": "79f1d3eb2626169d9a4ea448534888dcb132b755",
          "message": "Update kafka-clients and embedded-kafka to 3.8.1 (#1352)\n\nMake `zio-kafka-example` depend zio-kafka using `dependsOn` instead of `libraryDependencies` with a specific version to prevent dependency conflicts.\r\n\r\nAlso updates:\r\n\r\n📦 Updates [org.apache.kafka:kafka-clients](https://kafka.apache.org)\r\nfrom `3.8.0` to `3.8.1`\r\n📦 Updates [io.github.embeddedkafka:embedded-kafka](https://github.com/embeddedkafka/embedded-kafka) from `3.8.0` to `3.8.1`\r\n📜 [GitHub Release Notes](https://github.com/embeddedkafka/embedded-kafka/releases/tag/v3.8.1) - [Version Diff](https://github.com/embeddedkafka/embedded-kafka/compare/v3.8.0...v3.8.1)\r\n\r\n\r\nCo-authored-by: zio-scala-steward[bot] <145262613+zio-scala-steward[bot]@users.noreply.github.com>\r\nCo-authored-by: Erik van Oosten <e.vanoosten@grons.nl>",
          "timestamp": "2024-10-31T09:29:08+01:00",
          "tree_id": "7aba96481aaccc4da0a335cb834999c32c29b68e",
          "url": "https://github.com/zio/zio-kafka/commit/79f1d3eb2626169d9a4ea448534888dcb132b755"
        },
        "date": 1730364426187,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordPar",
            "value": 16.983742691308663,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeq",
            "value": 14.061428897838205,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 604.7712343115829,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 594.4711349374302,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 148.79133642499994,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 378.1764428066667,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 548.6785199441347,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 536.0698052051282,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 574.7209058130811,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 573.3676438189243,
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
          "id": "9f316f902027d081c6f1d40b8d171ea4b52a3499",
          "message": "Add example dashboard to docs (#1357)",
          "timestamp": "2024-11-02T10:48:00+01:00",
          "tree_id": "dc82996068728e877e3fb0436c8ba22f42420d15",
          "url": "https://github.com/zio/zio-kafka/commit/9f316f902027d081c6f1d40b8d171ea4b52a3499"
        },
        "date": 1730541990385,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordPar",
            "value": 13.612189896362478,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeq",
            "value": 10.111904621926168,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 601.7985592756797,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 593.5412392271509,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 192.07826350533335,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 443.9432356199999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 548.5468400946153,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 533.2526257485578,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 538.5982524794595,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 526.9919341611416,
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
          "id": "d59e8f6e23eea56f2887bdf47aac70107afe581c",
          "message": "Fix slow rebalanceSafeCommits behavior (#1358)\n\nAfter consumer 1 is shutdown (using stopConsumption), rebalances happen\r\nand partitions from consumer 2 are assigned. These streams are never\r\nstarted, so the finalizer completing completedPromise is never called.\r\nWaiting for these to complete takes 3 minutes (default\r\nmaxRebalanceDuration).\r\n\r\nIn case that streams were assigned and no record was ever put in their\r\nqueues, there's no use in waiting for the stream to complete by\r\ncommitting some offset.",
          "timestamp": "2024-11-02T18:40:30+01:00",
          "tree_id": "7a07cdff34c03e896321e42ad99de03daa691dc4",
          "url": "https://github.com/zio/zio-kafka/commit/d59e8f6e23eea56f2887bdf47aac70107afe581c"
        },
        "date": 1730570325556,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordPar",
            "value": 17.252752584564142,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeq",
            "value": 14.158982278194236,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 601.6604595788455,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 591.9374073092738,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 140.91965291214282,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 377.68259388999996,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 545.2592971179809,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 538.8054410248396,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 520.7391588771892,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 548.7785188367947,
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
          "id": "42b5401d7772ff1149566a0b0333e4c448efa919",
          "message": "Remove attemptBlocking from AdminClient (#1359)\n\nFixes #492",
          "timestamp": "2024-11-03T11:06:25+01:00",
          "tree_id": "250c0e8aa527d06111c792587c685619656c74e9",
          "url": "https://github.com/zio/zio-kafka/commit/42b5401d7772ff1149566a0b0333e4c448efa919"
        },
        "date": 1730629459506,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordPar",
            "value": 19.707892789946737,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeq",
            "value": 16.72888110481531,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 600.0672850984731,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 589.8867858846927,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 109.98207068266667,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 301.321077474,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 545.956694294904,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 536.7153260881411,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 546.6309867528647,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 548.7289716418879,
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
          "id": "c121eaa6337886fccd17017dc65c92f1f53139da",
          "message": "Update sbt to 1.10.5 (#1362)\n\n## About this PR\r\n📦 Updates [org.scala-sbt:sbt](https://github.com/sbt/sbt) from `1.10.4`\r\nto `1.10.5`\r\n\r\n📜 [GitHub Release\r\nNotes](https://github.com/sbt/sbt/releases/tag/v1.10.5) - [Version\r\nDiff](https://github.com/sbt/sbt/compare/v1.10.4...v1.10.5)\r\n\r\n## Usage\r\n✅ **Please merge!**\r\n\r\nI'll automatically update this PR to resolve conflicts as long as you\r\ndon't change it yourself.\r\n\r\nIf you'd like to skip this version, you can just close this PR. If you\r\nhave any feedback, just mention me in the comments below.\r\n\r\nConfigure Scala Steward for your repository with a\r\n[`.scala-steward.conf`](https://github.com/scala-steward-org/scala-steward/blob/767fcfecbfd53c507152f6cf15c846176bae561d/docs/repo-specific-configuration.md)\r\nfile.\r\n\r\n_Have a fantastic day writing Scala!_\r\n\r\n<details>\r\n<summary>🔍 Files still referring to the old version number</summary>\r\n\r\nThe following files still refer to the old version number (1.10.4).\r\nYou might want to review and update them manually.\r\n```\r\nproject/plugins.sbt\r\n```\r\n</details>\r\n<details>\r\n<summary>⚙ Adjust future updates</summary>\r\n\r\nAdd this to your `.scala-steward.conf` file to ignore future updates of\r\nthis dependency:\r\n```\r\nupdates.ignore = [ { groupId = \"org.scala-sbt\", artifactId = \"sbt\" } ]\r\n```\r\nOr, add this to slow down future updates of this dependency:\r\n```\r\ndependencyOverrides = [{\r\n  pullRequests = { frequency = \"30 days\" },\r\n  dependency = { groupId = \"org.scala-sbt\", artifactId = \"sbt\" }\r\n}]\r\n```\r\n</details>\r\n\r\n<sup>\r\nlabels: library-update, early-semver-patch, semver-spec-patch,\r\nversion-scheme:early-semver, old-version-remains, commit-count:1\r\n</sup>\r\n\r\nCo-authored-by: zio-scala-steward[bot] <145262613+zio-scala-steward[bot]@users.noreply.github.com>",
          "timestamp": "2024-11-04T07:39:46+01:00",
          "tree_id": "7b2941217a02b32cb959392d455f2bbf1a7386be",
          "url": "https://github.com/zio/zio-kafka/commit/c121eaa6337886fccd17017dc65c92f1f53139da"
        },
        "date": 1730703507515,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordPar",
            "value": 17.970548484134984,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeq",
            "value": 14.321166849040507,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 605.8042562221974,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 593.423291123017,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 142.2105987114286,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 349.27477988666664,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 543.253277250673,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 536.779850277564,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 578.2004633516757,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 581.4774084562898,
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
          "id": "8ae5d791b30f96ef66fb54a941c60881e082b4b0",
          "message": "Update zio, zio-streams, zio-test, ... to 2.1.12 (#1364)\n\n## About this PR\r\n📦 Updates \r\n* [dev.zio:zio](https://github.com/zio/zio)\r\n* [dev.zio:zio-streams](https://github.com/zio/zio)\r\n* [dev.zio:zio-test](https://github.com/zio/zio)\r\n* [dev.zio:zio-test-sbt](https://github.com/zio/zio)\r\n\r\n from `2.1.11` to `2.1.12`\r\n\r\n📜 [GitHub Release\r\nNotes](https://github.com/zio/zio/releases/tag/v2.1.12) - [Version\r\nDiff](https://github.com/zio/zio/compare/v2.1.11...v2.1.12)\r\n\r\n## Usage\r\n✅ **Please merge!**\r\n\r\nI'll automatically update this PR to resolve conflicts as long as you\r\ndon't change it yourself.\r\n\r\nIf you'd like to skip this version, you can just close this PR. If you\r\nhave any feedback, just mention me in the comments below.\r\n\r\nConfigure Scala Steward for your repository with a\r\n[`.scala-steward.conf`](https://github.com/scala-steward-org/scala-steward/blob/67e3c3e7d88448dd99efc39b1b517c53b359677f/docs/repo-specific-configuration.md)\r\nfile.\r\n\r\n_Have a fantastic day writing Scala!_\r\n\r\n<details>\r\n<summary>⚙ Adjust future updates</summary>\r\n\r\nAdd this to your `.scala-steward.conf` file to ignore future updates of\r\nthis dependency:\r\n```\r\nupdates.ignore = [ { groupId = \"dev.zio\" } ]\r\n```\r\nOr, add this to slow down future updates of this dependency:\r\n```\r\ndependencyOverrides = [{\r\n  pullRequests = { frequency = \"30 days\" },\r\n  dependency = { groupId = \"dev.zio\" }\r\n}]\r\n```\r\n</details>\r\n\r\n<sup>\r\nlabels: library-update, early-semver-patch, semver-spec-patch,\r\ncommit-count:1\r\n</sup>\r\n\r\nCo-authored-by: zio-scala-steward[bot] <145262613+zio-scala-steward[bot]@users.noreply.github.com>",
          "timestamp": "2024-11-08T08:51:47+01:00",
          "tree_id": "cfa10c5e719a57567c0350ae347ed2e08955a9f3",
          "url": "https://github.com/zio/zio-kafka/commit/8ae5d791b30f96ef66fb54a941c60881e082b4b0"
        },
        "date": 1731053443724,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordPar",
            "value": 15.836996759953628,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeq",
            "value": 12.64058187332937,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 606.2699677718435,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 593.2496230986593,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 166.12433870761907,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 410.2014329533333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 550.65516173625,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 541.9531550044871,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 629.0523176678917,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 550.825843674731,
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
          "id": "c8f11733e9e6bfe7c60d936b1aa879ee8209b372",
          "message": "Fix concurrent modification exception in Consumer release (#1365)\n\nBy using a semaphore also for the close operation we guard against\r\nconcurrent access during release. This reverts a change made in in\r\n#1011.\r\n\r\nThe cause of this concurrent access after the Consumer has shutdown is\r\nunfortunately unknown and I haven't been able to reproduce it.\r\n\r\nFixes #1238.",
          "timestamp": "2024-11-10T09:47:39+01:00",
          "tree_id": "0b89b00f8b53ee535b3b049b798b9b5cbdea45b6",
          "url": "https://github.com/zio/zio-kafka/commit/c8f11733e9e6bfe7c60d936b1aa879ee8209b372"
        },
        "date": 1731229530263,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordPar",
            "value": 20.661145435976405,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeq",
            "value": 16.18506704424852,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 602.4237470825326,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 591.1015830830167,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 115.3989380382222,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 294.65829690733335,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 543.7406885654807,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 536.3769755408654,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 604.0808701838918,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 551.1197483477497,
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
          "id": "0f46a928b30bd0806db69457f5767c7c9cc7331e",
          "message": "Automate pruning of benchmark history (#1370)\n\nThe script that is called from the benchmark workflow can be seen at:\r\nhttps://github.com/zio/zio-kafka/blob/gh-pages/scripts/prune-benchmark-history.sc\r\n\r\nAlso:\r\n - Use java 21 for bench and profile workflows.\r\n - Use latest minor version of actions in bench and profile workflows.",
          "timestamp": "2024-11-10T11:27:20+01:00",
          "tree_id": "562977bafb3aba8669b7036155ecc36510341474",
          "url": "https://github.com/zio/zio-kafka/commit/0f46a928b30bd0806db69457f5767c7c9cc7331e"
        },
        "date": 1731235528899,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordPar",
            "value": 17.05057938981727,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeq",
            "value": 13.1608561949384,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 602.6280410402235,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 591.9621349033521,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 149.6481850226984,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 415.2959821033333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 544.1896569596154,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 531.627253120032,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 537.5409693515676,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 584.3183253242591,
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
          "id": "727e7d7acce4cb0a6f301451e1b25df3dd887e2b",
          "message": "Tests for PartitionStreamControl + fix issue with interruptionPromise (#1371)\n\nThe issue became apparent from the tests. It unfortunately means that\r\n#1251 did not fix the issue of blocking when partitions are lost when\r\nthere is no more data in the queue.\r\n\r\n* Fix: race should be raceFirst, because interruptionPromise will only\r\never fail (relates to #1251)\r\n* Reset queue size on lost\r\n* Abstract requesting data for testing purposes",
          "timestamp": "2024-11-10T12:56:15+01:00",
          "tree_id": "ad750c75d2a8762b7e0e8807bd7563daa9aaf3b4",
          "url": "https://github.com/zio/zio-kafka/commit/727e7d7acce4cb0a6f301451e1b25df3dd887e2b"
        },
        "date": 1731240900790,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordPar",
            "value": 15.037439163390525,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeq",
            "value": 11.309724006545686,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 601.1577257018251,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 589.2136165463687,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 185.90322979580952,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 462.9915408933333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 543.8873812163462,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 534.5268733330129,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 551.7111893299459,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 534.7770909260154,
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
          "id": "b7fc0ae606d880d5fd13d74b0230640878bc9c3a",
          "message": "Fix automatic pruning of benchmark history (#1372)\n\nShould fix the failing build\r\nhttps://github.com/zio/zio-kafka/actions/runs/11764359080/job/32769417421",
          "timestamp": "2024-11-10T13:32:32+01:00",
          "tree_id": "c29b90921ce90ccc029f69eebd2712b37ab534e2",
          "url": "https://github.com/zio/zio-kafka/commit/b7fc0ae606d880d5fd13d74b0230640878bc9c3a"
        },
        "date": 1731243047994,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordPar",
            "value": 13.70004360904564,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeq",
            "value": 10.638679214292706,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 601.8578136984731,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 591.3459275977656,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 202.81256358400003,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 537.5432667399999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 540.6250430814424,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 533.0801341179487,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 550.4878722765405,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 546.8518645077497,
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
          "id": "16a4482ce382a7f4f9e23f37cb742cb35f959bdf",
          "message": "Fix automatic pruning of benchmark history (attempt 2) (#1373)",
          "timestamp": "2024-11-10T14:24:51+01:00",
          "tree_id": "a85c64f8c7f9eac3315c4068994a4845a8662be8",
          "url": "https://github.com/zio/zio-kafka/commit/16a4482ce382a7f4f9e23f37cb742cb35f959bdf"
        },
        "date": 1731246173930,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordPar",
            "value": 15.972747754615131,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeq",
            "value": 12.27306060719717,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 600.3543197970951,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 589.0191751031284,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 156.44116561238093,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 460.75815933333325,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 542.8002309439423,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 531.7408049152244,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 544.2770155537297,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 578.527591585686,
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
          "id": "37ad5e733496ea01949cc473701f06a968500f12",
          "message": "More logging around rebalancing when rebalanceSafeCommits is true (#1360)\n\nFollow up of #1358 \r\n\r\nSee also #1132\r\n\r\n---------\r\n\r\nCo-authored-by: Erik van Oosten <e.vanoosten@grons.nl>",
          "timestamp": "2024-11-10T20:42:49+01:00",
          "tree_id": "2a35a4191be618979aa5165a6974b310bdf149d9",
          "url": "https://github.com/zio/zio-kafka/commit/37ad5e733496ea01949cc473701f06a968500f12"
        },
        "date": 1731268879355,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordPar",
            "value": 14.841431485095292,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeq",
            "value": 11.915470295550211,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 602.0260612189944,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 590.2180270774302,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 181.6661929007619,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 477.7351269433333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 540.221230549904,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 534.1702508038461,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 591.336537295135,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 554.2783443860375,
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
          "id": "1f8d590463dbd08ae27e708dccf43554321226c3",
          "message": "Require a semaphore in Consumer.fromJavaConsumer (#1368)\n\nFixes #1276\r\n\r\n---------\r\n\r\nCo-authored-by: Erik van Oosten <e.vanoosten@grons.nl>",
          "timestamp": "2024-11-10T21:07:04+01:00",
          "tree_id": "57dcbb26184242c30b5a67a78fd6f7ed7eb6f745",
          "url": "https://github.com/zio/zio-kafka/commit/1f8d590463dbd08ae27e708dccf43554321226c3"
        },
        "date": 1731270345473,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordPar",
            "value": 19.034204590015367,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeq",
            "value": 14.128024400982545,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 601.3210776283055,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 591.1081384726258,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 143.94750778142856,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 361.40643403666667,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 541.4409799951924,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 534.3587570592948,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 565.706508813081,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 515.345790974314,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "49699333+dependabot[bot]@users.noreply.github.com",
            "name": "dependabot[bot]",
            "username": "dependabot[bot]"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "18aa941d995197301450de86b73a2293eff59b54",
          "message": "Bump scala-steward-org/scala-steward-action from 2.70.0 to 2.71.0 (#1377)\n\nBumps\r\n[scala-steward-org/scala-steward-action](https://github.com/scala-steward-org/scala-steward-action)\r\nfrom 2.70.0 to 2.71.0.\r\n<details>\r\n<summary>Release notes</summary>\r\n<p><em>Sourced from <a\r\nhref=\"https://github.com/scala-steward-org/scala-steward-action/releases\">scala-steward-org/scala-steward-action's\r\nreleases</a>.</em></p>\r\n<blockquote>\r\n<h2>v2.71.0</h2>\r\n<!-- raw HTML omitted -->\r\n<h2>What's Changed</h2>\r\n<h3>Other Changes</h3>\r\n<ul>\r\n<li>Fix loss of <code>run-summary.md</code> in GHA Job Summary by <a\r\nhref=\"https://github.com/rtyley\"><code>@​rtyley</code></a> in <a\r\nhref=\"https://redirect.github.com/scala-steward-org/scala-steward-action/pull/652\">scala-steward-org/scala-steward-action#652</a></li>\r\n</ul>\r\n<p><strong>Full Changelog</strong>: <a\r\nhref=\"https://github.com/scala-steward-org/scala-steward-action/compare/v2.70.0...v2.71.0\">https://github.com/scala-steward-org/scala-steward-action/compare/v2.70.0...v2.71.0</a></p>\r\n</blockquote>\r\n</details>\r\n<details>\r\n<summary>Commits</summary>\r\n<ul>\r\n<li><a\r\nhref=\"https://github.com/scala-steward-org/scala-steward-action/commit/a7eeaa3abb040622ec80032cd3b09c547f6b97ed\"><code>a7eeaa3</code></a>\r\nRelease v2.71.0</li>\r\n<li><a\r\nhref=\"https://github.com/scala-steward-org/scala-steward-action/commit/7aeabbe29dc35ea2e8eab45e3f27bedd6590d41a\"><code>7aeabbe</code></a>\r\nMerge pull request <a\r\nhref=\"https://redirect.github.com/scala-steward-org/scala-steward-action/issues/652\">#652</a>\r\nfrom rtyley/fix-logging-of-run-summary</li>\r\n<li><a\r\nhref=\"https://github.com/scala-steward-org/scala-steward-action/commit/14e5aef38a8d5331a473250f29fb4567920b02e9\"><code>14e5aef</code></a>\r\nFix loss of <code>run-summary.md</code> in GHA Job Summary</li>\r\n<li><a\r\nhref=\"https://github.com/scala-steward-org/scala-steward-action/commit/6661cf3aa71eb2fd0d9ca4afb3c63ace38ac283f\"><code>6661cf3</code></a>\r\nBump <code>@​types/node</code> from 22.7.4 to 22.8.6 (<a\r\nhref=\"https://redirect.github.com/scala-steward-org/scala-steward-action/issues/650\">#650</a>)</li>\r\n<li><a\r\nhref=\"https://github.com/scala-steward-org/scala-steward-action/commit/b862dee52c82e476eb5221bc170cc61b1ef3ab12\"><code>b862dee</code></a>\r\nBump actions/checkout from 4.2.0 to 4.2.2 (<a\r\nhref=\"https://redirect.github.com/scala-steward-org/scala-steward-action/issues/645\">#645</a>)</li>\r\n<li><a\r\nhref=\"https://github.com/scala-steward-org/scala-steward-action/commit/e5ba01f4c070ef25dd4439667df50c7837328b69\"><code>e5ba01f</code></a>\r\nMerge pull request <a\r\nhref=\"https://redirect.github.com/scala-steward-org/scala-steward-action/issues/642\">#642</a>\r\nfrom scala-steward-org/dependabot/github_actions/int1...</li>\r\n<li><a\r\nhref=\"https://github.com/scala-steward-org/scala-steward-action/commit/8bf505a7eb2f16e81f372a2a93a6b7adf553f177\"><code>8bf505a</code></a>\r\nMerge branch 'master' into\r\ndependabot/github_actions/int128/release-typescrip...</li>\r\n<li><a\r\nhref=\"https://github.com/scala-steward-org/scala-steward-action/commit/82dc740a4d7c6e34280c6b335524063a57e825c5\"><code>82dc740</code></a>\r\nBump actions/upload-artifact from 4.4.0 to 4.4.3 (<a\r\nhref=\"https://redirect.github.com/scala-steward-org/scala-steward-action/issues/646\">#646</a>)</li>\r\n<li><a\r\nhref=\"https://github.com/scala-steward-org/scala-steward-action/commit/317f79ee93a71a30033f5302dfdf0838f05f2931\"><code>317f79e</code></a>\r\nBump actions/setup-node from 4.0.3 to 4.0.4 (<a\r\nhref=\"https://redirect.github.com/scala-steward-org/scala-steward-action/issues/641\">#641</a>)</li>\r\n<li><a\r\nhref=\"https://github.com/scala-steward-org/scala-steward-action/commit/95fcefb3494775269de7509b7e5b40618f30ef68\"><code>95fcefb</code></a>\r\nBump ava from 6.1.3 to 6.2.0 (<a\r\nhref=\"https://redirect.github.com/scala-steward-org/scala-steward-action/issues/651\">#651</a>)</li>\r\n<li>Additional commits viewable in <a\r\nhref=\"https://github.com/scala-steward-org/scala-steward-action/compare/v2.70.0...v2.71.0\">compare\r\nview</a></li>\r\n</ul>\r\n</details>\r\n<br />\r\n\r\n\r\n[![Dependabot compatibility\r\nscore](https://dependabot-badges.githubapp.com/badges/compatibility_score?dependency-name=scala-steward-org/scala-steward-action&package-manager=github_actions&previous-version=2.70.0&new-version=2.71.0)](https://docs.github.com/en/github/managing-security-vulnerabilities/about-dependabot-security-updates#about-compatibility-scores)\r\n\r\nDependabot will resolve any conflicts with this PR as long as you don't\r\nalter it yourself. You can also trigger a rebase manually by commenting\r\n`@dependabot rebase`.\r\n\r\n[//]: # (dependabot-automerge-start)\r\n[//]: # (dependabot-automerge-end)\r\n\r\n---\r\n\r\n<details>\r\n<summary>Dependabot commands and options</summary>\r\n<br />\r\n\r\nYou can trigger Dependabot actions by commenting on this PR:\r\n- `@dependabot rebase` will rebase this PR\r\n- `@dependabot recreate` will recreate this PR, overwriting any edits\r\nthat have been made to it\r\n- `@dependabot merge` will merge this PR after your CI passes on it\r\n- `@dependabot squash and merge` will squash and merge this PR after\r\nyour CI passes on it\r\n- `@dependabot cancel merge` will cancel a previously requested merge\r\nand block automerging\r\n- `@dependabot reopen` will reopen this PR if it is closed\r\n- `@dependabot close` will close this PR and stop Dependabot recreating\r\nit. You can achieve the same result by closing it manually\r\n- `@dependabot show <dependency name> ignore conditions` will show all\r\nof the ignore conditions of the specified dependency\r\n- `@dependabot ignore this major version` will close this PR and stop\r\nDependabot creating any more for this major version (unless you reopen\r\nthe PR or upgrade to it yourself)\r\n- `@dependabot ignore this minor version` will close this PR and stop\r\nDependabot creating any more for this minor version (unless you reopen\r\nthe PR or upgrade to it yourself)\r\n- `@dependabot ignore this dependency` will close this PR and stop\r\nDependabot creating any more for this dependency (unless you reopen the\r\nPR or upgrade to it yourself)\r\n\r\n\r\n</details>\r\n\r\nSigned-off-by: dependabot[bot] <support@github.com>\r\nCo-authored-by: dependabot[bot] <49699333+dependabot[bot]@users.noreply.github.com>",
          "timestamp": "2024-11-11T07:38:39+01:00",
          "tree_id": "1cba6ec657d4855b5229f86554bf85d31c26cb07",
          "url": "https://github.com/zio/zio-kafka/commit/18aa941d995197301450de86b73a2293eff59b54"
        },
        "date": 1731308275386,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordPar",
            "value": 15.05127958837682,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeq",
            "value": 10.642781412354738,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 601.908368582272,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 590.564647542905,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 190.69566814157145,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 422.6021594833333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 548.3912228384615,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 531.5880176432692,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 609.4057270264865,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 558.1029564870252,
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
          "id": "90f297338c3adcdd6ddcc7c2721b56be57814743",
          "message": "Rename Serde methods for consistency with ZIO 2 (#1378)\n\nRenames:\r\n* mapM -> mapZIO\r\n* contramapM -> contramapZIO\r\n* inmapM -> inmapZIO\r\n\r\nThe existing methods are now deprecated.\r\n\r\nSee also https://github.com/scala-steward-org/scala-steward/pull/3474.\r\n\r\nFixes #1325.\r\n\r\n---------\r\n\r\nCo-authored-by: Erik van Oosten <e.vanoosten@grons.nl>",
          "timestamp": "2024-11-12T17:15:53+01:00",
          "tree_id": "737183cb1be0b9778aa35e873ccbbc56bcd53609",
          "url": "https://github.com/zio/zio-kafka/commit/90f297338c3adcdd6ddcc7c2721b56be57814743"
        },
        "date": 1731429258411,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordPar",
            "value": 14.013751568893083,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeq",
            "value": 10.953686150475106,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 603.4440078262942,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 593.3533735998884,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 197.16223076666668,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 512.4282124666667,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 542.0775023297116,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 535.6785759094552,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 547.4804414536214,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 548.8564056154555,
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
          "id": "cdbf5133d0bdfa765f6376123608eca957732d2e",
          "message": "Fix deprecation notice version on Consumer.fromJavaConsumer (#1379)",
          "timestamp": "2024-11-12T17:53:03+01:00",
          "tree_id": "cd08055bc108674715dfef2ddd57a53fd97d33b5",
          "url": "https://github.com/zio/zio-kafka/commit/cdbf5133d0bdfa765f6376123608eca957732d2e"
        },
        "date": 1731431479626,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordPar",
            "value": 17.15381982580785,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeq",
            "value": 13.126427092873753,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 602.600923305624,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 591.2700031814526,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 151.56930484857148,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 383.7870298666667,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 544.488926484327,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 534.4598916073718,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 546.8219249020542,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 555.0493920533919,
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
          "id": "e1a41448a090f6628f7bda4fc0480d979a458bac",
          "message": "Use same `max.poll.records` in all benchmarks (#1381)",
          "timestamp": "2024-11-13T10:27:39+01:00",
          "tree_id": "b1c4a807078debcebc836a71f0a793c5122971c0",
          "url": "https://github.com/zio/zio-kafka/commit/e1a41448a090f6628f7bda4fc0480d979a458bac"
        },
        "date": 1731491177219,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordPar",
            "value": 15.629001762570041,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeq",
            "value": 12.25857209158661,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 602.034933690056,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 590.8599705730727,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 177.2645040485714,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 452.4624718533334,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 545.7992508531731,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 530.6166686081729,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 618.3196166140541,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 556.8719353617563,
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
          "id": "504074f6e121bdd7ed28038faaac297bbd5a331d",
          "message": "Decouple stream halt detection timeout from max poll interval (#1376)\n\nFixes #1262\r\n\r\n---------\r\n\r\nCo-authored-by: Erik van Oosten <e.vanoosten@grons.nl>",
          "timestamp": "2024-11-13T11:04:50+01:00",
          "tree_id": "8a3b354f3ae6cbc2130aa3b77f2d5073cd6d6924",
          "url": "https://github.com/zio/zio-kafka/commit/504074f6e121bdd7ed28038faaac297bbd5a331d"
        },
        "date": 1731493437763,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordPar",
            "value": 12.702520293539251,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeq",
            "value": 10.362977320167271,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 607.2616308026816,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 595.7592309272625,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 213.88533829199997,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 433.97005975333343,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 548.1711086340384,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 538.9444831522437,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 551.8654607260539,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 525.5475419092866,
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
          "id": "ae4fd29d955b826805510c1678bd21ed7f15bfa2",
          "message": "Formatting (#1383)",
          "timestamp": "2024-11-13T14:50:57+01:00",
          "tree_id": "b7ea3b010e9aad24ecb3c9507bdf8b84289e9ab6",
          "url": "https://github.com/zio/zio-kafka/commit/ae4fd29d955b826805510c1678bd21ed7f15bfa2"
        },
        "date": 1731506993102,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordPar",
            "value": 19.45246502850548,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeq",
            "value": 14.376774013471836,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 605.0850362843948,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 595.9310021827933,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 129.23151329277778,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 345.43276771666666,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 546.6187384125963,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 536.9419124261218,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 577.2596616982702,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 573.234335652953,
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
          "id": "d5d8ccdc2af869732786e3a8188c7a7d0f1ecae0",
          "message": "Disable mima check for new minor releases (#1384)",
          "timestamp": "2024-11-13T18:08:37+01:00",
          "tree_id": "462ed3bdd09b3a72ba2731420e9aff40245da6fa",
          "url": "https://github.com/zio/zio-kafka/commit/d5d8ccdc2af869732786e3a8188c7a7d0f1ecae0"
        },
        "date": 1731518810390,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordPar",
            "value": 17.865237094077585,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeq",
            "value": 14.16109289919045,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 601.7766736497581,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 591.4455487368714,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 135.4803442057143,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 384.49241470000015,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 538.504947024327,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 532.1233976990384,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 514.0558964487568,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 541.0455399610099,
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
          "id": "b8a4e06547eb9a8372092d4f55a9b359329720f2",
          "message": "Update zio-logging-slf4j, ... to 2.4.0 (#1387)\n\n## About this PR\r\n📦 Updates \r\n* [dev.zio:zio-logging-slf4j](https://github.com/zio/zio-logging)\r\n* [dev.zio:zio-logging-slf4j2](https://github.com/zio/zio-logging)\r\n\r\n from `2.3.2` to `2.4.0`\r\n\r\n📜 [GitHub Release\r\nNotes](https://github.com/zio/zio-logging/releases/tag/v2.4.0) -\r\n[Version\r\nDiff](https://github.com/zio/zio-logging/compare/v2.3.2...v2.4.0)\r\n\r\n## Usage\r\n✅ **Please merge!**\r\n\r\nI'll automatically update this PR to resolve conflicts as long as you\r\ndon't change it yourself.\r\n\r\nIf you'd like to skip this version, you can just close this PR. If you\r\nhave any feedback, just mention me in the comments below.\r\n\r\nConfigure Scala Steward for your repository with a\r\n[`.scala-steward.conf`](https://github.com/scala-steward-org/scala-steward/blob/f89466c2b9d28c5029de11ecd442329caf0f5b70/docs/repo-specific-configuration.md)\r\nfile.\r\n\r\n_Have a fantastic day writing Scala!_\r\n\r\n<details>\r\n<summary>⚙ Adjust future updates</summary>\r\n\r\nAdd this to your `.scala-steward.conf` file to ignore future updates of\r\nthis dependency:\r\n```\r\nupdates.ignore = [ { groupId = \"dev.zio\" } ]\r\n```\r\nOr, add this to slow down future updates of this dependency:\r\n```\r\ndependencyOverrides = [{\r\n  pullRequests = { frequency = \"30 days\" },\r\n  dependency = { groupId = \"dev.zio\" }\r\n}]\r\n```\r\n</details>\r\n\r\n<sup>\r\nlabels: library-update, early-semver-minor, semver-spec-minor,\r\ncommit-count:1\r\n</sup>\r\n\r\n<!-- scala-steward = {\r\n  \"Update\" : {\r\n    \"ForGroupId\" : {\r\n      \"forArtifactIds\" : [\r\n        {\r\n          \"ForArtifactId\" : {\r\n            \"crossDependency\" : [\r\n              {\r\n                \"groupId\" : \"dev.zio\",\r\n                \"artifactId\" : {\r\n                  \"name\" : \"zio-logging-slf4j\",\r\n                  \"maybeCrossName\" : \"zio-logging-slf4j_2.13\"\r\n                },\r\n                \"version\" : \"2.3.2\",\r\n                \"sbtVersion\" : null,\r\n                \"scalaVersion\" : null,\r\n                \"configurations\" : \"test\"\r\n              },\r\n              {\r\n                \"groupId\" : \"dev.zio\",\r\n                \"artifactId\" : {\r\n                  \"name\" : \"zio-logging-slf4j\",\r\n                  \"maybeCrossName\" : \"zio-logging-slf4j_3\"\r\n                },\r\n                \"version\" : \"2.3.2\",\r\n                \"sbtVersion\" : null,\r\n                \"scalaVersion\" : null,\r\n                \"configurations\" : \"test\"\r\n              }\r\n            ],\r\n            \"newerVersions\" : [\r\n              \"2.4.0\"\r\n            ],\r\n            \"newerGroupId\" : null,\r\n            \"newerArtifactId\" : null\r\n          }\r\n        },\r\n        {\r\n          \"ForArtifactId\" : {\r\n            \"crossDependency\" : [\r\n              {\r\n                \"groupId\" : \"dev.zio\",\r\n                \"artifactId\" : {\r\n                  \"name\" : \"zio-logging-slf4j2\",\r\n                  \"maybeCrossName\" : \"zio-logging-slf4j2_2.13\"\r\n                },\r\n                \"version\" : \"2.3.2\",\r\n                \"sbtVersion\" : null,\r\n                \"scalaVersion\" : null,\r\n                \"configurations\" : null\r\n              }\r\n            ],\r\n            \"newerVersions\" : [\r\n              \"2.4.0\"\r\n            ],\r\n            \"newerGroupId\" : null,\r\n            \"newerArtifactId\" : null\r\n          }\r\n        }\r\n      ]\r\n    }\r\n  },\r\n  \"Labels\" : [\r\n    \"library-update\",\r\n    \"early-semver-minor\",\r\n    \"semver-spec-minor\",\r\n    \"commit-count:1\"\r\n  ]\r\n} -->\r\n\r\nCo-authored-by: zio-scala-steward[bot] <145262613+zio-scala-steward[bot]@users.noreply.github.com>",
          "timestamp": "2024-11-14T09:28:47+01:00",
          "tree_id": "1c00609685e187fa507259adb935817be0230cb0",
          "url": "https://github.com/zio/zio-kafka/commit/b8a4e06547eb9a8372092d4f55a9b359329720f2"
        },
        "date": 1731574043281,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordPar",
            "value": 14.222158417655125,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeq",
            "value": 11.318375189822234,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 604.4833697049534,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 595.1565855198883,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 184.36306651333334,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 489.35459155999996,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 546.6072887180769,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 542.7902577756411,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 607.5995237562161,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 583.3209585494621,
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
          "id": "9cddaeb56e044ab05e3131916b39848c56e92a0f",
          "message": "Remove 'experimental' label from `FetchStrategy` (#1393)\n\nThe `FetchStrategy` trait has not seen any change since its introduction\r\nover a year ago. It is time to remove the experimental label.",
          "timestamp": "2024-11-16T12:27:54+01:00",
          "tree_id": "6581f705e965b26f8519de853e510ba98ad1cf61",
          "url": "https://github.com/zio/zio-kafka/commit/9cddaeb56e044ab05e3131916b39848c56e92a0f"
        },
        "date": 1731757614600,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordPar",
            "value": 15.151408928147482,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeq",
            "value": 11.35035997793795,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 602.6116053640225,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 593.0824227273745,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 192.77952377390477,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 498.46926606666653,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 544.2831099050002,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 531.075783145032,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 534.328567916108,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 580.0699326255982,
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
          "id": "1c5041e3eecf3ea3b9a30248206bf977cc69cf08",
          "message": "Remove unnecessary workflow input (#1394)\n\nWhen you start a workflow from the GitHub UI, it already asks for a\r\nbranch/tag/commit. There is no need to add our own input for that.\r\n\r\nThis is a follow-up of #1392.",
          "timestamp": "2024-11-16T14:28:19+01:00",
          "tree_id": "cca694892d5a69958996816ecebe22ff1625b234",
          "url": "https://github.com/zio/zio-kafka/commit/1c5041e3eecf3ea3b9a30248206bf977cc69cf08"
        },
        "date": 1731764820804,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordPar",
            "value": 17.23212277186022,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeq",
            "value": 13.026513030720546,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 600.8099654134822,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 589.0288773421231,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 148.07074106190473,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 435.74269293666674,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 545.4700954264423,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 532.1128684875,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 540.9937058610811,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 548.9698143285179,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "adrielcasellas@gmail.com",
            "name": "Adriel Casellas",
            "username": "AdrielC"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "78cfa03673b0bd4b8fe36487de9c06f915f9f2e0",
          "message": "Fix timing issue in ConsumerSpec test by adding delay in consumer stream (#1388)\n\nThis PR addresses a timing issue in the ConsumerSpec test: \r\n\r\n`“it’s possible to start a new consumption session from a Consumer that\r\nhad a consumption session stopped previously”`\r\n\r\n# Issue:\r\n\r\nWhen running the entire test suite, this test occasionally fails with\r\nthe following assertion error:\r\n\r\n```\r\nAssertion failed:\r\n  ✗ 100000 was not less than 100000\r\n  consumed0 did not satisfy isGreaterThan(0L) && isLessThan(numberOfMessages.toLong)\r\n  consumed0 = 100000\r\n```\r\n\r\n# Cause:\r\n- The failure occurs because the first consumer sometimes consumes all\r\nmessages before consumer.stopConsumption is called.\r\n- This happens due to timing variations when the test suite is run in\r\nfull, possibly because of system performance or resource contention.\r\n- As a result, consumed0 equals numberOfMessages, causing the assertion\r\nconsumed0 < numberOfMessages.toLong to fail.\r\n\r\n# Solution:\r\n- Introduce a slight delay in the consumer stream to prevent it from\r\nconsuming all messages too quickly.\r\n- This ensures that consumer.stopConsumption is called before all\r\nmessages are consumed.\r\n- The test can now reliably check that the consumer can be stopped and\r\nrestarted.\r\n\r\n# Testing:\r\n- Ran the full test suite multiple times to confirm that the issue is\r\nresolved.\r\n- Verified that consumed0 is greater than 0 and less than\r\nnumberOfMessages, satisfying the original assertions.",
          "timestamp": "2024-11-19T11:12:29+01:00",
          "tree_id": "f6a79d0944bcf1e20c863748e09148bfd91c9b5e",
          "url": "https://github.com/zio/zio-kafka/commit/78cfa03673b0bd4b8fe36487de9c06f915f9f2e0"
        },
        "date": 1732012286602,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordPar",
            "value": 12.746712946685127,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeq",
            "value": 10.590472084832241,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 602.5294922501303,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 592.2200032810056,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 201.40254415200002,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 512.1650271866667,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 543.0456563275962,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 532.2643647461537,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 545.5412819649729,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 597.5868084167728,
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
          "id": "9f3e6dc1bdcd80e3e089f7bb727a3de6d3821a1f",
          "message": "Update embedded-kafka to 3.9.0 (#1398)\n\n## About this PR\r\n📦 Updates\r\n[io.github.embeddedkafka:embedded-kafka](https://github.com/embeddedkafka/embedded-kafka)\r\nfrom `3.8.1` to `3.9.0`\r\n\r\n📜 [GitHub Release\r\nNotes](https://github.com/embeddedkafka/embedded-kafka/releases/tag/v3.9.0)\r\n- [Version\r\nDiff](https://github.com/embeddedkafka/embedded-kafka/compare/v3.8.1...v3.9.0)\r\n\r\n## Usage\r\n✅ **Please merge!**\r\n\r\nI'll automatically update this PR to resolve conflicts as long as you\r\ndon't change it yourself.\r\n\r\nIf you'd like to skip this version, you can just close this PR. If you\r\nhave any feedback, just mention me in the comments below.\r\n\r\nConfigure Scala Steward for your repository with a\r\n[`.scala-steward.conf`](https://github.com/scala-steward-org/scala-steward/blob/f89466c2b9d28c5029de11ecd442329caf0f5b70/docs/repo-specific-configuration.md)\r\nfile.\r\n\r\n_Have a fantastic day writing Scala!_\r\n\r\n<details>\r\n<summary>🔍 Files still referring to the old version number</summary>\r\n\r\nThe following files still refer to the old version number (3.8.1).\r\nYou might want to review and update them manually.\r\n```\r\nbuild.sbt\r\n```\r\n</details>\r\n<details>\r\n<summary>⚙ Adjust future updates</summary>\r\n\r\nAdd this to your `.scala-steward.conf` file to ignore future updates of\r\nthis dependency:\r\n```\r\nupdates.ignore = [ { groupId = \"io.github.embeddedkafka\", artifactId = \"embedded-kafka\" } ]\r\n```\r\nOr, add this to slow down future updates of this dependency:\r\n```\r\ndependencyOverrides = [{\r\n  pullRequests = { frequency = \"30 days\" },\r\n  dependency = { groupId = \"io.github.embeddedkafka\", artifactId = \"embedded-kafka\" }\r\n}]\r\n```\r\n</details>\r\n\r\n<sup>\r\nlabels: library-update, early-semver-minor, semver-spec-minor,\r\nversion-scheme:semver-spec, old-version-remains, commit-count:1\r\n</sup>\r\n\r\n<!-- scala-steward = {\r\n  \"Update\" : {\r\n    \"ForArtifactId\" : {\r\n      \"crossDependency\" : [\r\n        {\r\n          \"groupId\" : \"io.github.embeddedkafka\",\r\n          \"artifactId\" : {\r\n            \"name\" : \"embedded-kafka\",\r\n            \"maybeCrossName\" : \"embedded-kafka_2.13\"\r\n          },\r\n          \"version\" : \"3.8.1\",\r\n          \"sbtVersion\" : null,\r\n          \"scalaVersion\" : null,\r\n          \"configurations\" : null\r\n        }\r\n      ],\r\n      \"newerVersions\" : [\r\n        \"3.9.0\"\r\n      ],\r\n      \"newerGroupId\" : null,\r\n      \"newerArtifactId\" : null\r\n    }\r\n  },\r\n  \"Labels\" : [\r\n    \"library-update\",\r\n    \"early-semver-minor\",\r\n    \"semver-spec-minor\",\r\n    \"version-scheme:semver-spec\",\r\n    \"old-version-remains\",\r\n    \"commit-count:1\"\r\n  ]\r\n} -->\r\n\r\n---------\r\n\r\nCo-authored-by: zio-scala-steward[bot] <145262613+zio-scala-steward[bot]@users.noreply.github.com>\r\nCo-authored-by: svroonland <svroonland@users.noreply.github.com>",
          "timestamp": "2024-11-20T08:16:45+01:00",
          "tree_id": "30d8db12bb97d28c92f6e7e608d243a3618c7e60",
          "url": "https://github.com/zio/zio-kafka/commit/9f3e6dc1bdcd80e3e089f7bb727a3de6d3821a1f"
        },
        "date": 1732088104981,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordPar",
            "value": 18.705379517432654,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeq",
            "value": 14.32059411016089,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 601.6985583763873,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 591.1551738184357,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 138.08583281603177,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 376.10717549000003,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 541.8558361547116,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 533.5899557661859,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 560.5596962377297,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 542.3764041512184,
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
          "id": "ee4bff8314c39fd993f1187acdc1e7b670a3e5ea",
          "message": "Update zio, zio-streams, zio-test, ... to 2.1.13 (#1399)\n\n## About this PR\r\n📦 Updates \r\n* [dev.zio:zio](https://github.com/zio/zio)\r\n* [dev.zio:zio-streams](https://github.com/zio/zio)\r\n* [dev.zio:zio-test](https://github.com/zio/zio)\r\n* [dev.zio:zio-test-sbt](https://github.com/zio/zio)\r\n\r\n from `2.1.12` to `2.1.13`\r\n\r\n📜 [GitHub Release\r\nNotes](https://github.com/zio/zio/releases/tag/v2.1.13) - [Version\r\nDiff](https://github.com/zio/zio/compare/v2.1.12...v2.1.13)\r\n\r\n## Usage\r\n✅ **Please merge!**\r\n\r\nI'll automatically update this PR to resolve conflicts as long as you\r\ndon't change it yourself.\r\n\r\nIf you'd like to skip this version, you can just close this PR. If you\r\nhave any feedback, just mention me in the comments below.\r\n\r\nConfigure Scala Steward for your repository with a\r\n[`.scala-steward.conf`](https://github.com/scala-steward-org/scala-steward/blob/f89466c2b9d28c5029de11ecd442329caf0f5b70/docs/repo-specific-configuration.md)\r\nfile.\r\n\r\n_Have a fantastic day writing Scala!_\r\n\r\n<details>\r\n<summary>⚙ Adjust future updates</summary>\r\n\r\nAdd this to your `.scala-steward.conf` file to ignore future updates of\r\nthis dependency:\r\n```\r\nupdates.ignore = [ { groupId = \"dev.zio\" } ]\r\n```\r\nOr, add this to slow down future updates of this dependency:\r\n```\r\ndependencyOverrides = [{\r\n  pullRequests = { frequency = \"30 days\" },\r\n  dependency = { groupId = \"dev.zio\" }\r\n}]\r\n```\r\n</details>\r\n\r\n<sup>\r\nlabels: library-update, early-semver-patch, semver-spec-patch,\r\ncommit-count:1\r\n</sup>\r\n\r\n<!-- scala-steward = {\r\n  \"Update\" : {\r\n    \"ForGroupId\" : {\r\n      \"forArtifactIds\" : [\r\n        {\r\n          \"ForArtifactId\" : {\r\n            \"crossDependency\" : [\r\n              {\r\n                \"groupId\" : \"dev.zio\",\r\n                \"artifactId\" : {\r\n                  \"name\" : \"zio\",\r\n                  \"maybeCrossName\" : \"zio_2.13\"\r\n                },\r\n                \"version\" : \"2.1.12\",\r\n                \"sbtVersion\" : null,\r\n                \"scalaVersion\" : null,\r\n                \"configurations\" : null\r\n              },\r\n              {\r\n                \"groupId\" : \"dev.zio\",\r\n                \"artifactId\" : {\r\n                  \"name\" : \"zio\",\r\n                  \"maybeCrossName\" : \"zio_3\"\r\n                },\r\n                \"version\" : \"2.1.12\",\r\n                \"sbtVersion\" : null,\r\n                \"scalaVersion\" : null,\r\n                \"configurations\" : null\r\n              }\r\n            ],\r\n            \"newerVersions\" : [\r\n              \"2.1.13\"\r\n            ],\r\n            \"newerGroupId\" : null,\r\n            \"newerArtifactId\" : null\r\n          }\r\n        },\r\n        {\r\n          \"ForArtifactId\" : {\r\n            \"crossDependency\" : [\r\n              {\r\n                \"groupId\" : \"dev.zio\",\r\n                \"artifactId\" : {\r\n                  \"name\" : \"zio-streams\",\r\n                  \"maybeCrossName\" : \"zio-streams_2.13\"\r\n                },\r\n                \"version\" : \"2.1.12\",\r\n                \"sbtVersion\" : null,\r\n                \"scalaVersion\" : null,\r\n                \"configurations\" : null\r\n              },\r\n              {\r\n                \"groupId\" : \"dev.zio\",\r\n                \"artifactId\" : {\r\n                  \"name\" : \"zio-streams\",\r\n                  \"maybeCrossName\" : \"zio-streams_3\"\r\n                },\r\n                \"version\" : \"2.1.12\",\r\n                \"sbtVersion\" : null,\r\n                \"scalaVersion\" : null,\r\n                \"configurations\" : null\r\n              }\r\n            ],\r\n            \"newerVersions\" : [\r\n              \"2.1.13\"\r\n            ],\r\n            \"newerGroupId\" : null,\r\n            \"newerArtifactId\" : null\r\n          }\r\n        },\r\n        {\r\n          \"ForArtifactId\" : {\r\n            \"crossDependency\" : [\r\n              {\r\n                \"groupId\" : \"dev.zio\",\r\n                \"artifactId\" : {\r\n                  \"name\" : \"zio-test\",\r\n                  \"maybeCrossName\" : \"zio-test_2.13\"\r\n                },\r\n                \"version\" : \"2.1.12\",\r\n                \"sbtVersion\" : null,\r\n                \"scalaVersion\" : null,\r\n                \"configurations\" : null\r\n              },\r\n              {\r\n                \"groupId\" : \"dev.zio\",\r\n                \"artifactId\" : {\r\n                  \"name\" : \"zio-test\",\r\n                  \"maybeCrossName\" : \"zio-test_2.13\"\r\n                },\r\n                \"version\" : \"2.1.12\",\r\n                \"sbtVersion\" : null,\r\n                \"scalaVersion\" : null,\r\n                \"configurations\" : \"test\"\r\n              },\r\n              {\r\n                \"groupId\" : \"dev.zio\",\r\n                \"artifactId\" : {\r\n                  \"name\" : \"zio-test\",\r\n                  \"maybeCrossName\" : \"zio-test_3\"\r\n                },\r\n                \"version\" : \"2.1.12\",\r\n                \"sbtVersion\" : null,\r\n                \"scalaVersion\" : null,\r\n                \"configurations\" : null\r\n              },\r\n              {\r\n                \"groupId\" : \"dev.zio\",\r\n                \"artifactId\" : {\r\n                  \"name\" : \"zio-test\",\r\n                  \"maybeCrossName\" : \"zio-test_3\"\r\n                },\r\n                \"version\" : \"2.1.12\",\r\n                \"sbtVersion\" : null,\r\n                \"scalaVersion\" : null,\r\n                \"configurations\" : \"test\"\r\n              }\r\n            ],\r\n            \"newerVersions\" : [\r\n              \"2.1.13\"\r\n            ],\r\n            \"newerGroupId\" : null,\r\n            \"newerArtifactId\" : null\r\n          }\r\n        },\r\n        {\r\n          \"ForArtifactId\" : {\r\n            \"crossDependency\" : [\r\n              {\r\n                \"groupId\" : \"dev.zio\",\r\n                \"artifactId\" : {\r\n                  \"name\" : \"zio-test-sbt\",\r\n                  \"maybeCrossName\" : \"zio-test-sbt_2.13\"\r\n                },\r\n                \"version\" : \"2.1.12\",\r\n                \"sbtVersion\" : null,\r\n                \"scalaVersion\" : null,\r\n                \"configurations\" : \"test\"\r\n              },\r\n              {\r\n                \"groupId\" : \"dev.zio\",\r\n                \"artifactId\" : {\r\n                  \"name\" : \"zio-test-sbt\",\r\n                  \"maybeCrossName\" : \"zio-test-sbt_3\"\r\n                },\r\n                \"version\" : \"2.1.12\",\r\n                \"sbtVersion\" : null,\r\n                \"scalaVersion\" : null,\r\n                \"configurations\" : \"test\"\r\n              }\r\n            ],\r\n            \"newerVersions\" : [\r\n              \"2.1.13\"\r\n            ],\r\n            \"newerGroupId\" : null,\r\n            \"newerArtifactId\" : null\r\n          }\r\n        }\r\n      ]\r\n    }\r\n  },\r\n  \"Labels\" : [\r\n    \"library-update\",\r\n    \"early-semver-patch\",\r\n    \"semver-spec-patch\",\r\n    \"commit-count:1\"\r\n  ]\r\n} -->\r\n\r\nCo-authored-by: zio-scala-steward[bot] <145262613+zio-scala-steward[bot]@users.noreply.github.com>",
          "timestamp": "2024-11-21T09:03:08+01:00",
          "tree_id": "b3415d598a8b6d13d782e2ceaf86e4e8ed9ab715",
          "url": "https://github.com/zio/zio-kafka/commit/ee4bff8314c39fd993f1187acdc1e7b670a3e5ea"
        },
        "date": 1732177315064,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordPar",
            "value": 15.831851994147137,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeq",
            "value": 11.750021901238583,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 602.3542477183614,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 591.920579591285,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 177.48686702038097,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 459.88086215999994,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 545.2849087920191,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 533.1304903070512,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 537.044947628108,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 531.2414017951262,
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
          "id": "34fcfed18fdf7fdc8fbf699faa0c0897dc58abf7",
          "message": "Revert \"Update zio, zio-streams, zio-test, ... to 2.1.13\" (#1400)\n\nReverts zio/zio-kafka#1399 since the tests are failing...",
          "timestamp": "2024-11-22T22:12:37+01:00",
          "tree_id": "30d8db12bb97d28c92f6e7e608d243a3618c7e60",
          "url": "https://github.com/zio/zio-kafka/commit/34fcfed18fdf7fdc8fbf699faa0c0897dc58abf7"
        },
        "date": 1732311094972,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordPar",
            "value": 14.546667862508764,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeq",
            "value": 11.254933290587722,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 607.8465728131843,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 593.1004813101677,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 181.45218063695236,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 468.74536913333327,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 549.0302877704809,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 536.2000000786858,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 552.2316216230271,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 592.6091770406147,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "adrielcasellas@gmail.com",
            "name": "Adriel Casellas",
            "username": "AdrielC"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "e23d64a58159943a416be04c45edd92ba7cd6bd2",
          "message": "Improve tests by removing sleeps (#1401)\n\nThis change improves the reliability of tests in\r\n`ConsumerSpec.scala` by replacing the use of `ZIO.sleep` with\r\nsynchronization primitives such as `Promise` and `Ref`. The previous\r\nimplementation relied on arbitrary sleep durations to wait for certain\r\nconditions, which could lead to non-deterministic behavior and flakiness\r\nin the tests.\r\n\r\nFollow up of #1389.",
          "timestamp": "2024-11-23T09:57:01+01:00",
          "tree_id": "4eebb158f49c48bc489460a82af01c4d2cfe1a23",
          "url": "https://github.com/zio/zio-kafka/commit/e23d64a58159943a416be04c45edd92ba7cd6bd2"
        },
        "date": 1732353368292,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordPar",
            "value": 14.060232978846546,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeq",
            "value": 8.310061492726287,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 606.0619073568715,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 595.0872110124023,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 239.90897964457145,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 569.63812928,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 546.3206408976922,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 536.0571825488782,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 573.7692544478917,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 537.2627116982655,
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
          "id": "b713170549372f411cb833bdcdd81842d91f7810",
          "message": "Use same amount of messages for all consumer benchmarks (#1382)\n\nIn this change common benchmark code is moved into `ZioBenchmark`,\r\n`ConsumerZioBenchmark` and `ProducerZioBenchmark` so that it becomes\r\neasier to make the different benchmark comparable.\r\n\r\nAfter running the consumer benchmarks with different number of records,\r\nand different records sizes per run, this PR settled on 50k records of\r\n~512 bytes per run for all consumer benchmarks. With these amounts the\r\nzio-kafka based benchmarks and the 'comparison' benchmarks have roughly\r\nthe same scaling elasticity (where 'scaling elasticity' is defined as\r\nthe throughput growth factor divided by the number of records growth\r\nfactor).\r\n\r\nAfter this PR is merged, the benchmark history will be rewritten with\r\nlinear scaling so that we can compare historic runs against new runs.",
          "timestamp": "2024-11-23T10:18:59+01:00",
          "tree_id": "77cee7a94c6bf72fc620ed60340300b440316bd8",
          "url": "https://github.com/zio/zio-kafka/commit/b713170549372f411cb833bdcdd81842d91f7810"
        },
        "date": 1732354644690,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordPar",
            "value": 14.850742675622904,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeq",
            "value": 9.998585606788009,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 602.4253151800001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 588.44204398,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 182.0037417336984,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 458.6636302199999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 541.77196438,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 534.7352009000001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 557.6656158799999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 565.65875002,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "jules.ivanic@gmail.com",
            "name": "Jules Ivanic",
            "username": "guizmaii"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "b861c991127d67ebd9a404ef32efdb719cc2e675",
          "message": "[Internal] Micro-optimisation: Reuse previously calculated `java.langSystem.nanoTime` (#1407)\n\nHey :)\r\n\r\nI'm not contributing much anymore. Sorry about that. I lost the\r\nmotivation, TBH, but I really enjoy following what you continue to do 👏\r\n\r\nI was passing by and was reading some of the new code when I saw this.\r\nLMKWYT :)\r\n\r\nWith this change, we lose some nanoseconds of precision, so it might not\r\nbe the expected behaviour. 🤔",
          "timestamp": "2024-11-26T09:27:51+01:00",
          "tree_id": "17976dbd719e94ce3a60dd0f24349ff4d012b942",
          "url": "https://github.com/zio/zio-kafka/commit/b861c991127d67ebd9a404ef32efdb719cc2e675"
        },
        "date": 1732613970904,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordPar",
            "value": 16.763396701856472,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeq",
            "value": 12.103725215835984,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 602.8850480799999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 595.5846142400001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 195.20839404600002,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 424.7472702766666,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 545.2936539799999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 538.1217801199999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 561.1051536,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 570.5256354199998,
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
          "id": "bdde6e3f089e5841e4286ea32065708b9f8f6ac8",
          "message": "Extract Committer and RebalanceCoordinator classes from Runloop + unit tests (#1375)\n\nThe code is mostly just moved to a different place, the logic was left\r\nmostly intact. The 'interface' between the components has been decoupled\r\nmore, i.e. the rebalance listener no longer access the full Runloop's\r\nState and the pending commits are stored internally in the Committer.\r\n\r\nCare has been taken to make the Committer usable during rebalancing as\r\nwell, with the proper access to the Consumer for example. The part that\r\nwaits the end of the streams and their commits has been changed to use\r\nthe Committer.\r\n\r\nIncludes unit tests for the two new components.\r\n\r\n---------\r\n\r\nCo-authored-by: Erik van Oosten <e.vanoosten@grons.nl>",
          "timestamp": "2024-11-26T19:05:45+01:00",
          "tree_id": "8e7d11ef86a3667d4f0d13d5e32a637d8959b67d",
          "url": "https://github.com/zio/zio-kafka/commit/bdde6e3f089e5841e4286ea32065708b9f8f6ac8"
        },
        "date": 1732645467399,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordPar",
            "value": 17.07224467596996,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeq",
            "value": 13.240935868857692,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 600.6555744799999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 593.3365627999999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 150.10426051238096,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 398.46946953666674,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 542.41244544,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 535.91663806,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 562.3573253999999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 570.2762930600001,
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
          "id": "7f45603e54410dde73059368a8991b933fe08520",
          "message": "ZioKafkaConsumerbenchmark.throughput: increase count per batch (#1409)\n\nThis is more consistent with the manual kafka client benchmark\r\nequivalent\r\n\r\n---------\r\n\r\nCo-authored-by: Erik van Oosten <e.vanoosten@grons.nl>",
          "timestamp": "2024-11-30T20:04:11+01:00",
          "tree_id": "74dbd560a45df234f67b388c243c07fb1698591e",
          "url": "https://github.com/zio/zio-kafka/commit/7f45603e54410dde73059368a8991b933fe08520"
        },
        "date": 1732994583333,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordPar",
            "value": 11.859757257182734,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeq",
            "value": 10.062185252694938,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 592.07755786,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 593.6287387799999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 212.86431984933333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 523.8223203466666,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 543.46769086,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 536.5576318399999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 561.0236337799998,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 568.60735638,
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
          "id": "60f266e982e12f8a86d27f1e36d41e7ea3ac3e04",
          "message": "Update sbt, scripted-plugin to 1.10.6 (#1410)\n\n## About this PR\r\n📦 Updates \r\n* [org.scala-sbt:sbt](https://github.com/sbt/sbt)\r\n* [org.scala-sbt:scripted-plugin](https://github.com/sbt/sbt)\r\n\r\n from `1.10.5` to `1.10.6`\r\n\r\n📜 [GitHub Release\r\nNotes](https://github.com/sbt/sbt/releases/tag/v1.10.6) - [Version\r\nDiff](https://github.com/sbt/sbt/compare/v1.10.5...v1.10.6)",
          "timestamp": "2024-12-01T07:35:22+01:00",
          "tree_id": "2004edca1819bc42f1b9bc90c23dc83c4970a3ab",
          "url": "https://github.com/zio/zio-kafka/commit/60f266e982e12f8a86d27f1e36d41e7ea3ac3e04"
        },
        "date": 1733036045181,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordPar",
            "value": 15.229263114716634,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeq",
            "value": 11.909093764649661,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 591.7938359599999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 591.0075299599999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 176.08167733042853,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 461.3928140733333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 543.1175583799999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 535.6304295,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 562.3153167400002,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 567.9063960800001,
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
          "id": "4a0176f9b42cdfda81449195fdc195da809f435a",
          "message": "Convert commitAsync callback handling to ZIO sooner (#1404)\n\nKafkaConsumer's `commitAsync` takes a callback, which we program against\r\nwith complicated followup code. This PR attempts to convert everything\r\nto ZIO's earlier on, making chaining followup effects easier to reason\r\nabout.\r\n\r\nAs this changes some functionality around locking and same / single\r\nthreads, here's a summary of what do we need to ensure:\r\n* We have an exclusive lock on the consumer when calling `commitAsync`.\r\nIn `Runloop.run` this is done using `ConsumerAccess`. In the rebalance\r\ncoordinator (while rebalancing) we already have the lock as we're\r\ncalling `poll()` so no need for extra locking.\r\n* The consumer is not used from more than one thread at the same time.\r\nFor use in `Runloop.run` we get this for free by guaranteeing exclusive\r\naccess. In the rebalance coordinator a `poll()` call is in the middle of\r\nbeing executed and we need to call `commitAsync` on the same thread as\r\nthe rebalance listener is invoked.\r\n\r\nAnything that is not calling commitAsync is free to run on any thread as\r\nexecuted by the default ZIO runtime.\r\n\r\n---------\r\n\r\nCo-authored-by: Erik van Oosten <e.vanoosten@grons.nl>",
          "timestamp": "2024-12-07T10:19:07+01:00",
          "tree_id": "d38fd8d6303fe5e1606ab16cc839b22ad0f5841c",
          "url": "https://github.com/zio/zio-kafka/commit/4a0176f9b42cdfda81449195fdc195da809f435a"
        },
        "date": 1733564222522,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordPar",
            "value": 17.518005565529254,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeq",
            "value": 13.378731001023699,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 588.6155519000002,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 590.46289604,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 145.70628543714287,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 385.85365128000007,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 541.82316086,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 535.8322105,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 559.98366222,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 566.9750527199999,
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
          "id": "bb540bc34efd07aa427d9ac6badba14d3b233456",
          "message": "Add sbt to github action workflows, use ubuntu-latest (#1416)\n\nThe new ubuntu images no longer install sbt by default. Also: stop using\r\nancient ubuntu versions.\r\n\r\nThis is a stop-gap until zio-sbt plugin is fixed.",
          "timestamp": "2024-12-12T21:30:16+01:00",
          "tree_id": "42b7ff94a350094f3d653bc39eaef878f6ba3fee",
          "url": "https://github.com/zio/zio-kafka/commit/bb540bc34efd07aa427d9ac6badba14d3b233456"
        },
        "date": 1734036550255,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordPar",
            "value": 14.143116808300409,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeq",
            "value": 12.598971799819873,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 595.15155414,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 595.2136877199999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 175.82842316485713,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 423.07522445333336,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 547.0900650799999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 538.4456009,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 566.83057366,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 574.82085844,
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
          "id": "8ac327b1d69f7f7e22b9458b5b928d0fb5131aa1",
          "message": "Update logback-classic to 1.5.13 (#1420)\n\n## About this PR\n📦 Updates\n[ch.qos.logback:logback-classic](https://github.com/qos-ch/logback) from\n`1.5.12` to `1.5.13`\n\n## Usage\n✅ **Please merge!**\n\nI'll automatically update this PR to resolve conflicts as long as you\ndon't change it yourself.\n\nIf you'd like to skip this version, you can just close this PR. If you\nhave any feedback, just mention me in the comments below.\n\nConfigure Scala Steward for your repository with a\n[`.scala-steward.conf`](https://github.com/scala-steward-org/scala-steward/blob/e55a877397969e431e5107098941e0c7ed13ea26/docs/repo-specific-configuration.md)\nfile.\n\n_Have a fantastic day writing Scala!_\n\n<details>\n<summary>⚙ Adjust future updates</summary>\n\nAdd this to your `.scala-steward.conf` file to ignore future updates of\nthis dependency:\n```\nupdates.ignore = [ { groupId = \"ch.qos.logback\", artifactId = \"logback-classic\" } ]\n```\nOr, add this to slow down future updates of this dependency:\n```\ndependencyOverrides = [{\n  pullRequests = { frequency = \"30 days\" },\n  dependency = { groupId = \"ch.qos.logback\", artifactId = \"logback-classic\" }\n}]\n```\n</details>\n\n<sup>\nlabels: library-update, early-semver-patch, semver-spec-patch,\ncommit-count:1\n</sup>\n\n<!-- scala-steward = {\n  \"Update\" : {\n    \"ForArtifactId\" : {\n      \"crossDependency\" : [\n        {\n          \"groupId\" : \"ch.qos.logback\",\n          \"artifactId\" : {\n            \"name\" : \"logback-classic\",\n            \"maybeCrossName\" : null\n          },\n          \"version\" : \"1.5.12\",\n          \"sbtVersion\" : null,\n          \"scalaVersion\" : null,\n          \"configurations\" : null\n        },\n        {\n          \"groupId\" : \"ch.qos.logback\",\n          \"artifactId\" : {\n            \"name\" : \"logback-classic\",\n            \"maybeCrossName\" : null\n          },\n          \"version\" : \"1.5.12\",\n          \"sbtVersion\" : null,\n          \"scalaVersion\" : null,\n          \"configurations\" : \"test\"\n        }\n      ],\n      \"newerVersions\" : [\n        \"1.5.13\"\n      ],\n      \"newerGroupId\" : null,\n      \"newerArtifactId\" : null\n    }\n  },\n  \"Labels\" : [\n    \"library-update\",\n    \"early-semver-patch\",\n    \"semver-spec-patch\",\n    \"commit-count:1\"\n  ]\n} -->\n\nCo-authored-by: zio-scala-steward[bot] <145262613+zio-scala-steward[bot]@users.noreply.github.com>",
          "timestamp": "2024-12-19T09:31:38+01:00",
          "tree_id": "ea8be02763b4e615d7e8b7ef6c880d8a39cd278b",
          "url": "https://github.com/zio/zio-kafka/commit/8ac327b1d69f7f7e22b9458b5b928d0fb5131aa1"
        },
        "date": 1734598251137,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordPar",
            "value": 17.411665213839782,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeq",
            "value": 13.622971666739694,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 591.64239478,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 594.58158414,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 139.85122854642853,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 362.7896026666668,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 545.72482788,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 538.7824551400001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 564.9955114999999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 571.6079136000001,
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
          "id": "9499ba2d00a6506f5f245f7b42246fb2ed5d59e6",
          "message": "Update logback-classic to 1.5.14 (#1421)\n\n📦 Updates\r\n[ch.qos.logback:logback-classic](https://github.com/qos-ch/logback) from\r\n`1.5.13` to `1.5.14`",
          "timestamp": "2024-12-20T08:59:16+01:00",
          "tree_id": "6f582601d938296be4172c8c6450fa17c4f2c88c",
          "url": "https://github.com/zio/zio-kafka/commit/9499ba2d00a6506f5f245f7b42246fb2ed5d59e6"
        },
        "date": 1734682681002,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordPar",
            "value": 13.96387324162647,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeq",
            "value": 10.726962547847464,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 588.0065914599999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 589.3550181,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 198.88831751200004,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 543.9768436866667,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 543.98197532,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 537.2626309999999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 562.7660241,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 569.3224812599999,
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
          "id": "3e54282e6729e9ccf63dd251aae5efe15ff451fa",
          "message": "Update logback-classic to 1.5.15 (#1423)\n\n## About this PR\n📦 Updates\n[ch.qos.logback:logback-classic](https://github.com/qos-ch/logback) from\n`1.5.14` to `1.5.15`\n\n## Usage\n✅ **Please merge!**\n\nI'll automatically update this PR to resolve conflicts as long as you\ndon't change it yourself.\n\nIf you'd like to skip this version, you can just close this PR. If you\nhave any feedback, just mention me in the comments below.\n\nConfigure Scala Steward for your repository with a\n[`.scala-steward.conf`](https://github.com/scala-steward-org/scala-steward/blob/e55a877397969e431e5107098941e0c7ed13ea26/docs/repo-specific-configuration.md)\nfile.\n\n_Have a fantastic day writing Scala!_\n\n<details>\n<summary>⚙ Adjust future updates</summary>\n\nAdd this to your `.scala-steward.conf` file to ignore future updates of\nthis dependency:\n```\nupdates.ignore = [ { groupId = \"ch.qos.logback\", artifactId = \"logback-classic\" } ]\n```\nOr, add this to slow down future updates of this dependency:\n```\ndependencyOverrides = [{\n  pullRequests = { frequency = \"30 days\" },\n  dependency = { groupId = \"ch.qos.logback\", artifactId = \"logback-classic\" }\n}]\n```\n</details>\n\n<sup>\nlabels: library-update, early-semver-patch, semver-spec-patch,\ncommit-count:1\n</sup>\n\n<!-- scala-steward = {\n  \"Update\" : {\n    \"ForArtifactId\" : {\n      \"crossDependency\" : [\n        {\n          \"groupId\" : \"ch.qos.logback\",\n          \"artifactId\" : {\n            \"name\" : \"logback-classic\",\n            \"maybeCrossName\" : null\n          },\n          \"version\" : \"1.5.14\",\n          \"sbtVersion\" : null,\n          \"scalaVersion\" : null,\n          \"configurations\" : null\n        },\n        {\n          \"groupId\" : \"ch.qos.logback\",\n          \"artifactId\" : {\n            \"name\" : \"logback-classic\",\n            \"maybeCrossName\" : null\n          },\n          \"version\" : \"1.5.14\",\n          \"sbtVersion\" : null,\n          \"scalaVersion\" : null,\n          \"configurations\" : \"test\"\n        }\n      ],\n      \"newerVersions\" : [\n        \"1.5.15\"\n      ],\n      \"newerGroupId\" : null,\n      \"newerArtifactId\" : null\n    }\n  },\n  \"Labels\" : [\n    \"library-update\",\n    \"early-semver-patch\",\n    \"semver-spec-patch\",\n    \"commit-count:1\"\n  ]\n} -->\n\nCo-authored-by: zio-scala-steward[bot] <145262613+zio-scala-steward[bot]@users.noreply.github.com>",
          "timestamp": "2024-12-22T08:51:25+01:00",
          "tree_id": "b541a6762f4c9764f3188ee34ae60107a72d3daf",
          "url": "https://github.com/zio/zio-kafka/commit/3e54282e6729e9ccf63dd251aae5efe15ff451fa"
        },
        "date": 1734855000376,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordPar",
            "value": 11.453415308880892,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeq",
            "value": 9.480060206200541,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 588.02993538,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 590.6157715600001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 231.78050562333337,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 568.1335030533335,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 544.1345668800001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 536.79744414,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 563.1355802000002,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 571.2147830199999,
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
          "id": "4fdcc15b44877a22071dcea76a28c0b7fffee08a",
          "message": "Update zio, zio-streams, zio-test, ... to 2.1.14 (#1422)\n\n## About this PR\n📦 Updates \n* [dev.zio:zio](https://github.com/zio/zio)\n* [dev.zio:zio-streams](https://github.com/zio/zio)\n* [dev.zio:zio-test](https://github.com/zio/zio)\n* [dev.zio:zio-test-sbt](https://github.com/zio/zio)\n\n from `2.1.12` to `2.1.14`\n\n📜 [GitHub Release\nNotes](https://github.com/zio/zio/releases/tag/v2.1.14) - [Version\nDiff](https://github.com/zio/zio/compare/v2.1.12...v2.1.14)\n\n## Usage\n✅ **Please merge!**\n\nI'll automatically update this PR to resolve conflicts as long as you\ndon't change it yourself.\n\nIf you'd like to skip this version, you can just close this PR. If you\nhave any feedback, just mention me in the comments below.\n\nConfigure Scala Steward for your repository with a\n[`.scala-steward.conf`](https://github.com/scala-steward-org/scala-steward/blob/e55a877397969e431e5107098941e0c7ed13ea26/docs/repo-specific-configuration.md)\nfile.\n\n_Have a fantastic day writing Scala!_\n\n<details>\n<summary>⚙ Adjust future updates</summary>\n\nAdd this to your `.scala-steward.conf` file to ignore future updates of\nthis dependency:\n```\nupdates.ignore = [ { groupId = \"dev.zio\" } ]\n```\nOr, add this to slow down future updates of this dependency:\n```\ndependencyOverrides = [{\n  pullRequests = { frequency = \"30 days\" },\n  dependency = { groupId = \"dev.zio\" }\n}]\n```\n</details>\n\n<sup>\nlabels: library-update, early-semver-patch, semver-spec-patch,\ncommit-count:1\n</sup>\n\n<!-- scala-steward = {\n  \"Update\" : {\n    \"ForGroupId\" : {\n      \"forArtifactIds\" : [\n        {\n          \"ForArtifactId\" : {\n            \"crossDependency\" : [\n              {\n                \"groupId\" : \"dev.zio\",\n                \"artifactId\" : {\n                  \"name\" : \"zio\",\n                  \"maybeCrossName\" : \"zio_2.13\"\n                },\n                \"version\" : \"2.1.12\",\n                \"sbtVersion\" : null,\n                \"scalaVersion\" : null,\n                \"configurations\" : null\n              },\n              {\n                \"groupId\" : \"dev.zio\",\n                \"artifactId\" : {\n                  \"name\" : \"zio\",\n                  \"maybeCrossName\" : \"zio_3\"\n                },\n                \"version\" : \"2.1.12\",\n                \"sbtVersion\" : null,\n                \"scalaVersion\" : null,\n                \"configurations\" : null\n              }\n            ],\n            \"newerVersions\" : [\n              \"2.1.14\"\n            ],\n            \"newerGroupId\" : null,\n            \"newerArtifactId\" : null\n          }\n        },\n        {\n          \"ForArtifactId\" : {\n            \"crossDependency\" : [\n              {\n                \"groupId\" : \"dev.zio\",\n                \"artifactId\" : {\n                  \"name\" : \"zio-streams\",\n                  \"maybeCrossName\" : \"zio-streams_2.13\"\n                },\n                \"version\" : \"2.1.12\",\n                \"sbtVersion\" : null,\n                \"scalaVersion\" : null,\n                \"configurations\" : null\n              },\n              {\n                \"groupId\" : \"dev.zio\",\n                \"artifactId\" : {\n                  \"name\" : \"zio-streams\",\n                  \"maybeCrossName\" : \"zio-streams_3\"\n                },\n                \"version\" : \"2.1.12\",\n                \"sbtVersion\" : null,\n                \"scalaVersion\" : null,\n                \"configurations\" : null\n              }\n            ],\n            \"newerVersions\" : [\n              \"2.1.14\"\n            ],\n            \"newerGroupId\" : null,\n            \"newerArtifactId\" : null\n          }\n        },\n        {\n          \"ForArtifactId\" : {\n            \"crossDependency\" : [\n              {\n                \"groupId\" : \"dev.zio\",\n                \"artifactId\" : {\n                  \"name\" : \"zio-test\",\n                  \"maybeCrossName\" : \"zio-test_2.13\"\n                },\n                \"version\" : \"2.1.12\",\n                \"sbtVersion\" : null,\n                \"scalaVersion\" : null,\n                \"configurations\" : null\n              },\n              {\n                \"groupId\" : \"dev.zio\",\n                \"artifactId\" : {\n                  \"name\" : \"zio-test\",\n                  \"maybeCrossName\" : \"zio-test_2.13\"\n                },\n                \"version\" : \"2.1.12\",\n                \"sbtVersion\" : null,\n                \"scalaVersion\" : null,\n                \"configurations\" : \"test\"\n              },\n              {\n                \"groupId\" : \"dev.zio\",\n                \"artifactId\" : {\n                  \"name\" : \"zio-test\",\n                  \"maybeCrossName\" : \"zio-test_3\"\n                },\n                \"version\" : \"2.1.12\",\n                \"sbtVersion\" : null,\n                \"scalaVersion\" : null,\n                \"configurations\" : null\n              },\n              {\n                \"groupId\" : \"dev.zio\",\n                \"artifactId\" : {\n                  \"name\" : \"zio-test\",\n                  \"maybeCrossName\" : \"zio-test_3\"\n                },\n                \"version\" : \"2.1.12\",\n                \"sbtVersion\" : null,\n                \"scalaVersion\" : null,\n                \"configurations\" : \"test\"\n              }\n            ],\n            \"newerVersions\" : [\n              \"2.1.14\"\n            ],\n            \"newerGroupId\" : null,\n            \"newerArtifactId\" : null\n          }\n        },\n        {\n          \"ForArtifactId\" : {\n            \"crossDependency\" : [\n              {\n                \"groupId\" : \"dev.zio\",\n                \"artifactId\" : {\n                  \"name\" : \"zio-test-sbt\",\n                  \"maybeCrossName\" : \"zio-test-sbt_2.13\"\n                },\n                \"version\" : \"2.1.12\",\n                \"sbtVersion\" : null,\n                \"scalaVersion\" : null,\n                \"configurations\" : \"test\"\n              },\n              {\n                \"groupId\" : \"dev.zio\",\n                \"artifactId\" : {\n                  \"name\" : \"zio-test-sbt\",\n                  \"maybeCrossName\" : \"zio-test-sbt_3\"\n                },\n                \"version\" : \"2.1.12\",\n                \"sbtVersion\" : null,\n                \"scalaVersion\" : null,\n                \"configurations\" : \"test\"\n              }\n            ],\n            \"newerVersions\" : [\n              \"2.1.14\"\n            ],\n            \"newerGroupId\" : null,\n            \"newerArtifactId\" : null\n          }\n        }\n      ]\n    }\n  },\n  \"Labels\" : [\n    \"library-update\",\n    \"early-semver-patch\",\n    \"semver-spec-patch\",\n    \"commit-count:1\"\n  ]\n} -->\n\n---------\n\nCo-authored-by: zio-scala-steward[bot] <145262613+zio-scala-steward[bot]@users.noreply.github.com>\nCo-authored-by: Erik van Oosten <e.vanoosten@grons.nl>\nCo-authored-by: Steven Vroonland <svroonland@gmail.com>\nCo-authored-by: svroonland <svroonland@users.noreply.github.com>",
          "timestamp": "2024-12-22T09:24:03+01:00",
          "tree_id": "78777631e9fca1dcf73c9b7f16d96f22ef9a3ca7",
          "url": "https://github.com/zio/zio-kafka/commit/4fdcc15b44877a22071dcea76a28c0b7fffee08a"
        },
        "date": 1734857015491,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordPar",
            "value": 11.469475813617784,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeq",
            "value": 9.471000740379875,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 593.7406848400001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 596.46159596,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 251.76764203000002,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 518.0415030066666,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 545.5395961199999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 540.0575827399999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 565.0630989599999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 572.4283952999999,
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
          "id": "299303d9cf4df3324234a7cd80f41260b8e2a827",
          "message": "Update sbt, scripted-plugin to 1.10.7 (#1424)\n\n## About this PR\n📦 Updates \n* [org.scala-sbt:sbt](https://github.com/sbt/sbt)\n* [org.scala-sbt:scripted-plugin](https://github.com/sbt/sbt)\n\n from `1.10.6` to `1.10.7`\n\n## Usage\n✅ **Please merge!**\n\nI'll automatically update this PR to resolve conflicts as long as you\ndon't change it yourself.\n\nIf you'd like to skip this version, you can just close this PR. If you\nhave any feedback, just mention me in the comments below.\n\nConfigure Scala Steward for your repository with a\n[`.scala-steward.conf`](https://github.com/scala-steward-org/scala-steward/blob/e55a877397969e431e5107098941e0c7ed13ea26/docs/repo-specific-configuration.md)\nfile.\n\n_Have a fantastic day writing Scala!_\n\n<details>\n<summary>⚙ Adjust future updates</summary>\n\nAdd this to your `.scala-steward.conf` file to ignore future updates of\nthis dependency:\n```\nupdates.ignore = [ { groupId = \"org.scala-sbt\" } ]\n```\nOr, add this to slow down future updates of this dependency:\n```\ndependencyOverrides = [{\n  pullRequests = { frequency = \"30 days\" },\n  dependency = { groupId = \"org.scala-sbt\" }\n}]\n```\n</details>\n\n<sup>\nlabels: library-update, early-semver-patch, semver-spec-patch,\nversion-scheme:early-semver, commit-count:1\n</sup>\n\n<!-- scala-steward = {\n  \"Update\" : {\n    \"ForGroupId\" : {\n      \"forArtifactIds\" : [\n        {\n          \"ForArtifactId\" : {\n            \"crossDependency\" : [\n              {\n                \"groupId\" : \"org.scala-sbt\",\n                \"artifactId\" : {\n                  \"name\" : \"sbt\",\n                  \"maybeCrossName\" : null\n                },\n                \"version\" : \"1.10.6\",\n                \"sbtVersion\" : null,\n                \"scalaVersion\" : null,\n                \"configurations\" : null\n              }\n            ],\n            \"newerVersions\" : [\n              \"1.10.7\"\n            ],\n            \"newerGroupId\" : null,\n            \"newerArtifactId\" : null\n          }\n        },\n        {\n          \"ForArtifactId\" : {\n            \"crossDependency\" : [\n              {\n                \"groupId\" : \"org.scala-sbt\",\n                \"artifactId\" : {\n                  \"name\" : \"scripted-plugin\",\n                  \"maybeCrossName\" : \"scripted-plugin_2.12\"\n                },\n                \"version\" : \"1.10.6\",\n                \"sbtVersion\" : null,\n                \"scalaVersion\" : null,\n                \"configurations\" : null\n              }\n            ],\n            \"newerVersions\" : [\n              \"1.10.7\"\n            ],\n            \"newerGroupId\" : null,\n            \"newerArtifactId\" : null\n          }\n        }\n      ]\n    }\n  },\n  \"Labels\" : [\n    \"library-update\",\n    \"early-semver-patch\",\n    \"semver-spec-patch\",\n    \"version-scheme:early-semver\",\n    \"commit-count:1\"\n  ]\n} -->\n\nCo-authored-by: zio-scala-steward[bot] <145262613+zio-scala-steward[bot]@users.noreply.github.com>",
          "timestamp": "2024-12-23T09:24:51+01:00",
          "tree_id": "3d8648d48e68ba1382e0c08cbf6c58de969bb51b",
          "url": "https://github.com/zio/zio-kafka/commit/299303d9cf4df3324234a7cd80f41260b8e2a827"
        },
        "date": 1734943406568,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordPar",
            "value": 16.809870099221374,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeq",
            "value": 13.320150499425516,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 589.0218938200001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 594.5895751,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 143.74012666134925,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 381.1819884733334,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 545.04170466,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 537.1872573,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 565.36209566,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 570.5441795200001,
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
          "id": "b34d320e00bcaaedf4418ca580599150c776406e",
          "message": "Update README.md",
          "timestamp": "2025-01-03T20:13:53Z",
          "url": "https://github.com/zio/zio-kafka/pull/1427/commits/b34d320e00bcaaedf4418ca580599150c776406e"
        },
        "date": 1735936518359,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordPar",
            "value": 14.843235421121848,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeq",
            "value": 12.650780658134899,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 593.57553382,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 596.1325196399999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 185.8847094173333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 444.2899047199999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 545.3909590799999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 538.7326205799999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 566.5040978999999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 573.47933348,
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
          "id": "9426c9984a67918adb5a68f62c6f19646a65100b",
          "message": "Transactional committing and producing with rebalanceSafeCommits",
          "timestamp": "2025-01-03T20:16:00Z",
          "url": "https://github.com/zio/zio-kafka/pull/1425/commits/9426c9984a67918adb5a68f62c6f19646a65100b"
        },
        "date": 1736007855368,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordPar",
            "value": 14.845907961981858,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeq",
            "value": 10.856195294566133,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 588.8090080000001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 590.92988638,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 122.03129908133332,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 398.8565516433333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 543.64181498,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 536.7427503600001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 562.0742332799999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 568.1854729400001,
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
          "id": "545743c49a0e55587f44b7d3ae5f293a95af7e93",
          "message": "Transactional committing and producing with rebalanceSafeCommits",
          "timestamp": "2025-01-04T19:49:35Z",
          "url": "https://github.com/zio/zio-kafka/pull/1425/commits/545743c49a0e55587f44b7d3ae5f293a95af7e93"
        },
        "date": 1736074823936,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordPar",
            "value": 19.39198957230231,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeq",
            "value": 15.173934068350865,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 591.2765999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 593.9436603999999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 119.30946573388884,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 311.93851960866664,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 543.9331248599999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 538.2424572599999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 564.2682371999999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 571.0734379400001,
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
          "id": "bcc6ce121edcbcb0f013a3a7e66f3ac13842dd29",
          "message": "Update logback-classic to 1.5.16",
          "timestamp": "2025-01-04T19:49:35Z",
          "url": "https://github.com/zio/zio-kafka/pull/1428/commits/bcc6ce121edcbcb0f013a3a7e66f3ac13842dd29"
        },
        "date": 1736128815601,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordPar",
            "value": 11.739688201630123,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeq",
            "value": 9.700971660016844,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 593.7642325,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 593.4793058,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 194.2508018316191,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 537.5222421999999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 545.0058582400001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 538.39534862,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 564.6519566600001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 569.58531278,
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
          "id": "9abcf11a717f50e9048a2fdfa4a90f81d508ded5",
          "message": "Update zio-sbt-ci, zio-sbt-ecosystem, ... to 0.4.0-alpha.30",
          "timestamp": "2025-01-04T19:49:35Z",
          "url": "https://github.com/zio/zio-kafka/pull/1430/commits/9abcf11a717f50e9048a2fdfa4a90f81d508ded5"
        },
        "date": 1736128828482,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordPar",
            "value": 18.286920705161656,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeq",
            "value": 14.777566900730887,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 591.7774650199999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 592.7412202600001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 145.81432589214285,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 384.0420190333334,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 545.94868188,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 538.54772578,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 565.3907444199999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 574.13111328,
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
          "id": "5da672f6e720e2cfea576253bfcd4c6499d6944f",
          "message": "Update sbt-native-packager to 1.11.0",
          "timestamp": "2025-01-04T19:49:35Z",
          "url": "https://github.com/zio/zio-kafka/pull/1429/commits/5da672f6e720e2cfea576253bfcd4c6499d6944f"
        },
        "date": 1736128840094,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordPar",
            "value": 11.081826801925127,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeq",
            "value": 9.55024547199792,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 591.9186349600001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 592.26674304,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 243.19261382066668,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 554.7080395333332,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 544.2471166800002,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 537.9593923400001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 563.55976644,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 572.1920674200001,
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
          "id": "f95706490b078a7da0459058d37fee57c5f8ba0e",
          "message": "Update zio-sbt-ci, zio-sbt-ecosystem, ... to 0.4.0-alpha.30",
          "timestamp": "2025-01-04T19:49:35Z",
          "url": "https://github.com/zio/zio-kafka/pull/1430/commits/f95706490b078a7da0459058d37fee57c5f8ba0e"
        },
        "date": 1736137657579,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordPar",
            "value": 15.716846295028729,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeq",
            "value": 13.578051628922397,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 608.9255010200001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 612.70839956,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 159.71650669395237,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 485.35963372000015,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 554.4439443000001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 545.0010481400001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 575.8140746800001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 583.41336706,
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
          "id": "dc5abd6fa31131f1c12769ec728a7188fb8ffc72",
          "message": "Update sbt-native-packager to 1.11.0 (#1429)\n\n## About this PR\n📦 Updates\n[com.github.sbt:sbt-native-packager](https://github.com/sbt/sbt-native-packager)\nfrom `1.10.4` to `1.11.0`\n\n📜 [GitHub Release\nNotes](https://github.com/sbt/sbt-native-packager/releases/tag/v1.11.0)\n- [Version\nDiff](https://github.com/sbt/sbt-native-packager/compare/v1.10.4...v1.11.0)\n\n## Usage\n✅ **Please merge!**\n\nI'll automatically update this PR to resolve conflicts as long as you\ndon't change it yourself.\n\nIf you'd like to skip this version, you can just close this PR. If you\nhave any feedback, just mention me in the comments below.\n\nConfigure Scala Steward for your repository with a\n[`.scala-steward.conf`](https://github.com/scala-steward-org/scala-steward/blob/8929fadb6f535472bd63397b1cb55a835fa1b5cb/docs/repo-specific-configuration.md)\nfile.\n\n_Have a fantastic day writing Scala!_\n\n<details>\n<summary>⚙ Adjust future updates</summary>\n\nAdd this to your `.scala-steward.conf` file to ignore future updates of\nthis dependency:\n```\nupdates.ignore = [ { groupId = \"com.github.sbt\", artifactId = \"sbt-native-packager\" } ]\n```\nOr, add this to slow down future updates of this dependency:\n```\ndependencyOverrides = [{\n  pullRequests = { frequency = \"30 days\" },\n  dependency = { groupId = \"com.github.sbt\", artifactId = \"sbt-native-packager\" }\n}]\n```\n</details>\n\n<sup>\nlabels: sbt-plugin-update, early-semver-minor, semver-spec-minor,\ncommit-count:1\n</sup>\n\n<!-- scala-steward = {\n  \"Update\" : {\n    \"ForArtifactId\" : {\n      \"crossDependency\" : [\n        {\n          \"groupId\" : \"com.github.sbt\",\n          \"artifactId\" : {\n            \"name\" : \"sbt-native-packager\",\n            \"maybeCrossName\" : null\n          },\n          \"version\" : \"1.10.4\",\n          \"sbtVersion\" : \"1.0\",\n          \"scalaVersion\" : \"2.12\",\n          \"configurations\" : null\n        }\n      ],\n      \"newerVersions\" : [\n        \"1.11.0\"\n      ],\n      \"newerGroupId\" : null,\n      \"newerArtifactId\" : null\n    }\n  },\n  \"Labels\" : [\n    \"sbt-plugin-update\",\n    \"early-semver-minor\",\n    \"semver-spec-minor\",\n    \"commit-count:1\"\n  ]\n} -->\n\nCo-authored-by: zio-scala-steward[bot] <145262613+zio-scala-steward[bot]@users.noreply.github.com>",
          "timestamp": "2025-01-06T09:12:33+01:00",
          "tree_id": "c4ef25c2aaa9546fc2a2954b707c8b8bf4b90624",
          "url": "https://github.com/zio/zio-kafka/commit/dc5abd6fa31131f1c12769ec728a7188fb8ffc72"
        },
        "date": 1736152258705,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordPar",
            "value": 16.72231268029991,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeq",
            "value": 13.213203631889689,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 590.97556618,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 589.98704222,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 151.24913257357142,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 382.3765878133334,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 542.4963643599999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 537.90758434,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 563.13497314,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 569.34115274,
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
          "id": "80e153757222dc688fd43f7d01243420b45a3b57",
          "message": "Update logback-classic to 1.5.16 (#1428)\n\n## About this PR\n📦 Updates\n[ch.qos.logback:logback-classic](https://github.com/qos-ch/logback) from\n`1.5.15` to `1.5.16`\n\n## Usage\n✅ **Please merge!**\n\nI'll automatically update this PR to resolve conflicts as long as you\ndon't change it yourself.\n\nIf you'd like to skip this version, you can just close this PR. If you\nhave any feedback, just mention me in the comments below.\n\nConfigure Scala Steward for your repository with a\n[`.scala-steward.conf`](https://github.com/scala-steward-org/scala-steward/blob/8929fadb6f535472bd63397b1cb55a835fa1b5cb/docs/repo-specific-configuration.md)\nfile.\n\n_Have a fantastic day writing Scala!_\n\n<details>\n<summary>⚙ Adjust future updates</summary>\n\nAdd this to your `.scala-steward.conf` file to ignore future updates of\nthis dependency:\n```\nupdates.ignore = [ { groupId = \"ch.qos.logback\", artifactId = \"logback-classic\" } ]\n```\nOr, add this to slow down future updates of this dependency:\n```\ndependencyOverrides = [{\n  pullRequests = { frequency = \"30 days\" },\n  dependency = { groupId = \"ch.qos.logback\", artifactId = \"logback-classic\" }\n}]\n```\n</details>\n\n<sup>\nlabels: library-update, early-semver-patch, semver-spec-patch,\ncommit-count:1\n</sup>\n\n<!-- scala-steward = {\n  \"Update\" : {\n    \"ForArtifactId\" : {\n      \"crossDependency\" : [\n        {\n          \"groupId\" : \"ch.qos.logback\",\n          \"artifactId\" : {\n            \"name\" : \"logback-classic\",\n            \"maybeCrossName\" : null\n          },\n          \"version\" : \"1.5.15\",\n          \"sbtVersion\" : null,\n          \"scalaVersion\" : null,\n          \"configurations\" : null\n        },\n        {\n          \"groupId\" : \"ch.qos.logback\",\n          \"artifactId\" : {\n            \"name\" : \"logback-classic\",\n            \"maybeCrossName\" : null\n          },\n          \"version\" : \"1.5.15\",\n          \"sbtVersion\" : null,\n          \"scalaVersion\" : null,\n          \"configurations\" : \"test\"\n        }\n      ],\n      \"newerVersions\" : [\n        \"1.5.16\"\n      ],\n      \"newerGroupId\" : null,\n      \"newerArtifactId\" : null\n    }\n  },\n  \"Labels\" : [\n    \"library-update\",\n    \"early-semver-patch\",\n    \"semver-spec-patch\",\n    \"commit-count:1\"\n  ]\n} -->\n\nCo-authored-by: zio-scala-steward[bot] <145262613+zio-scala-steward[bot]@users.noreply.github.com>",
          "timestamp": "2025-01-06T09:12:11+01:00",
          "tree_id": "8d3981504199e7764ef993c911f5009a5f002e5c",
          "url": "https://github.com/zio/zio-kafka/commit/80e153757222dc688fd43f7d01243420b45a3b57"
        },
        "date": 1736152283738,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordPar",
            "value": 14.897118267023059,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeq",
            "value": 12.285390781555712,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 594.52968492,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 600.41637154,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 176.97034938285714,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 398.6117201333333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 548.65178216,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 539.84846634,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 569.0217873400001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 576.97128948,
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
          "id": "8233bc0e735b9506d3df3d781e33493248c0a399",
          "message": "Document transactions",
          "timestamp": "2025-01-09T12:32:38Z",
          "url": "https://github.com/zio/zio-kafka/pull/1432/commits/8233bc0e735b9506d3df3d781e33493248c0a399"
        },
        "date": 1736433996133,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordPar",
            "value": 17.481675875236505,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeq",
            "value": 14.279657745421236,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 593.88193906,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 591.5383910400001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 132.46188192309523,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 350.82092292,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 543.72126668,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 537.3428161200001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 565.9803913800001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 575.9955734600001,
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
          "id": "3dbb021aad4aafa9eb64ec354780cd8011e38f26",
          "message": "Add config for transactional consuming",
          "timestamp": "2025-01-09T12:32:38Z",
          "url": "https://github.com/zio/zio-kafka/pull/1433/commits/3dbb021aad4aafa9eb64ec354780cd8011e38f26"
        },
        "date": 1736583851782,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordPar",
            "value": 16.905984176646278,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeq",
            "value": 13.797643314669195,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 594.1788004000002,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 595.38337276,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 137.77527965095237,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 360.86737949333326,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 544.9587453400001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 538.85613538,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 564.5823992999999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 571.2130270000001,
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
          "id": "1cb2065d2144eb35891f214f850e3e50dd099782",
          "message": "Document transactions",
          "timestamp": "2025-01-09T12:32:38Z",
          "url": "https://github.com/zio/zio-kafka/pull/1432/commits/1cb2065d2144eb35891f214f850e3e50dd099782"
        },
        "date": 1736585671366,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordPar",
            "value": 13.713423413026858,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeq",
            "value": 11.020933890012516,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 594.6155609000001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 593.3359061400001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 208.1967762666667,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 455.0263667733333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 544.6915546800001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 538.6100695,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 566.8872354,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 571.4271132399999,
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
          "id": "396657cd98132ccc2b1ec521a52cae519217fc02",
          "message": "Add config for transactional consuming",
          "timestamp": "2025-01-09T12:32:38Z",
          "url": "https://github.com/zio/zio-kafka/pull/1433/commits/396657cd98132ccc2b1ec521a52cae519217fc02"
        },
        "date": 1736585943838,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordPar",
            "value": 16.618815721619523,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeq",
            "value": 13.227384436125769,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 591.8562799399999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 590.2623105399999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 139.05924425793654,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 347.8429394033334,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 543.3602787000001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 537.52676926,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 565.6361341800001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 570.4914464199999,
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
          "id": "1fca16675332170588c20b7c92438d5c4717214c",
          "message": "Add config for transactional consuming",
          "timestamp": "2025-01-09T12:32:38Z",
          "url": "https://github.com/zio/zio-kafka/pull/1433/commits/1fca16675332170588c20b7c92438d5c4717214c"
        },
        "date": 1736588063750,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordPar",
            "value": 18.77820605082977,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeq",
            "value": 15.815284679777731,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 591.99977784,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 594.04058174,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 118.5015217561111,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 325.9138747933333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 544.27350248,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 537.49918392,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 562.5927261800002,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 570.3129143199999,
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
          "id": "3155a0a1700a585e3d100d7b27aa4e9be400219e",
          "message": "Update zio-sbt-ci, zio-sbt-ecosystem, ... to 0.4.0-alpha.30 (#1430)\n\n## About this PR\n📦 Updates \n* [dev.zio:zio-sbt-ci](https://github.com/zio/zio-sbt)\n* [dev.zio:zio-sbt-ecosystem](https://github.com/zio/zio-sbt)\n* [dev.zio:zio-sbt-website](https://github.com/zio/zio-sbt)\n\n from `0.4.0-alpha.29` to `0.4.0-alpha.30`\n\n📜 [GitHub Release\nNotes](https://github.com/zio/zio-sbt/releases/tag/v0.4.0-alpha.30) -\n[Version\nDiff](https://github.com/zio/zio-sbt/compare/v0.4.0-alpha.29...v0.4.0-alpha.30)\n\n## Usage\n✅ **Please merge!**\n\nI'll automatically update this PR to resolve conflicts as long as you\ndon't change it yourself.\n\nIf you'd like to skip this version, you can just close this PR. If you\nhave any feedback, just mention me in the comments below.\n\nConfigure Scala Steward for your repository with a\n[`.scala-steward.conf`](https://github.com/scala-steward-org/scala-steward/blob/8929fadb6f535472bd63397b1cb55a835fa1b5cb/docs/repo-specific-configuration.md)\nfile.\n\n_Have a fantastic day writing Scala!_\n\n<details>\n<summary>⚙ Adjust future updates</summary>\n\nAdd this to your `.scala-steward.conf` file to ignore future updates of\nthis dependency:\n```\nupdates.ignore = [ { groupId = \"dev.zio\" } ]\n```\nOr, add this to slow down future updates of this dependency:\n```\ndependencyOverrides = [{\n  pullRequests = { frequency = \"30 days\" },\n  dependency = { groupId = \"dev.zio\" }\n}]\n```\n</details>\n\n<sup>\nlabels: sbt-plugin-update, early-semver-pre-release,\nsemver-spec-pre-release, commit-count:1\n</sup>\n\n<!-- scala-steward = {\n  \"Update\" : {\n    \"ForGroupId\" : {\n      \"forArtifactIds\" : [\n        {\n          \"ForArtifactId\" : {\n            \"crossDependency\" : [\n              {\n                \"groupId\" : \"dev.zio\",\n                \"artifactId\" : {\n                  \"name\" : \"zio-sbt-ci\",\n                  \"maybeCrossName\" : null\n                },\n                \"version\" : \"0.4.0-alpha.29\",\n                \"sbtVersion\" : \"1.0\",\n                \"scalaVersion\" : \"2.12\",\n                \"configurations\" : null\n              }\n            ],\n            \"newerVersions\" : [\n              \"0.4.0-alpha.30\"\n            ],\n            \"newerGroupId\" : null,\n            \"newerArtifactId\" : null\n          }\n        },\n        {\n          \"ForArtifactId\" : {\n            \"crossDependency\" : [\n              {\n                \"groupId\" : \"dev.zio\",\n                \"artifactId\" : {\n                  \"name\" : \"zio-sbt-ecosystem\",\n                  \"maybeCrossName\" : null\n                },\n                \"version\" : \"0.4.0-alpha.29\",\n                \"sbtVersion\" : \"1.0\",\n                \"scalaVersion\" : \"2.12\",\n                \"configurations\" : null\n              }\n            ],\n            \"newerVersions\" : [\n              \"0.4.0-alpha.30\"\n            ],\n            \"newerGroupId\" : null,\n            \"newerArtifactId\" : null\n          }\n        },\n        {\n          \"ForArtifactId\" : {\n            \"crossDependency\" : [\n              {\n                \"groupId\" : \"dev.zio\",\n                \"artifactId\" : {\n                  \"name\" : \"zio-sbt-website\",\n                  \"maybeCrossName\" : null\n                },\n                \"version\" : \"0.4.0-alpha.29\",\n                \"sbtVersion\" : \"1.0\",\n                \"scalaVersion\" : \"2.12\",\n                \"configurations\" : null\n              }\n            ],\n            \"newerVersions\" : [\n              \"0.4.0-alpha.30\"\n            ],\n            \"newerGroupId\" : null,\n            \"newerArtifactId\" : null\n          }\n        }\n      ]\n    }\n  },\n  \"Labels\" : [\n    \"sbt-plugin-update\",\n    \"early-semver-pre-release\",\n    \"semver-spec-pre-release\",\n    \"commit-count:1\"\n  ]\n} -->\n\n---------\n\nCo-authored-by: zio-scala-steward[bot] <145262613+zio-scala-steward[bot]@users.noreply.github.com>\nCo-authored-by: Jules Ivanic <jules.ivanic@gmail.com>",
          "timestamp": "2025-01-11T11:18:49+01:00",
          "tree_id": "0358c26e98e49c666a13d603b13f0b2ba2afb92e",
          "url": "https://github.com/zio/zio-kafka/commit/3155a0a1700a585e3d100d7b27aa4e9be400219e"
        },
        "date": 1736591823103,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordPar",
            "value": 18.497647454161577,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeq",
            "value": 15.410236837342579,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 592.2423315600001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 591.26194388,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 121.35714652833336,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 336.13012721,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 544.6051876800001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 536.8621635000001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 563.270269,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 570.4012299799999,
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
          "id": "c22a55f4ba510cdc17a660f155e962eb714cc12d",
          "message": "Transactional committing and producing with rebalanceSafeCommits",
          "timestamp": "2025-01-09T12:32:38Z",
          "url": "https://github.com/zio/zio-kafka/pull/1425/commits/c22a55f4ba510cdc17a660f155e962eb714cc12d"
        },
        "date": 1736591828699,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordPar",
            "value": 13.115372917479583,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeq",
            "value": 11.53354396563964,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 591.13509498,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 593.2772767599998,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 200.11613663419047,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 531.1648403866668,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 545.0111219400001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 537.8957200600003,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 565.9581855,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 572.0212802999998,
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
          "id": "43840ddba34a21369ba62b67d0f2056ce449a73e",
          "message": "Add config for transactional consuming",
          "timestamp": "2025-01-11T10:18:53Z",
          "url": "https://github.com/zio/zio-kafka/pull/1433/commits/43840ddba34a21369ba62b67d0f2056ce449a73e"
        },
        "date": 1736592405025,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordPar",
            "value": 19.617083776539374,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeq",
            "value": 16.00735475656903,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 589.34922182,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 591.3344164599998,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 112.97004715200002,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 317.30733030333334,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 545.1089693800001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 537.0808858400001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 563.61398872,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 569.83767936,
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
          "id": "077c67a2ac403e3d9649532fc6cfe43cf7da8f16",
          "message": "Add config for transactional consuming",
          "timestamp": "2025-01-11T10:18:53Z",
          "url": "https://github.com/zio/zio-kafka/pull/1433/commits/077c67a2ac403e3d9649532fc6cfe43cf7da8f16"
        },
        "date": 1736595410212,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordPar",
            "value": 18.986376319400915,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeq",
            "value": 15.180738920764895,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 593.31581608,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 591.14290536,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 125.51624813611112,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 334.59785809,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 544.90430272,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 538.4581796800001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 565.8692550799999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 570.7064114,
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
          "id": "8ee33ac083b31fcb804439af5cc754decc912496",
          "message": "Add config for transactional consuming (#1433)\n\nMake it easier to configure read-committed.",
          "timestamp": "2025-01-11T16:04:42+01:00",
          "tree_id": "6c2f26f732f29bf7dfa2c60ecfa482b9c05f57ba",
          "url": "https://github.com/zio/zio-kafka/commit/8ee33ac083b31fcb804439af5cc754decc912496"
        },
        "date": 1736609307269,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordPar",
            "value": 13.09823143362409,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeq",
            "value": 10.87971340008467,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 591.7818382199999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 591.632756,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 209.21635449000001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 570.8545010266668,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 544.22067068,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 537.0249531200001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 562.9456693999999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 568.70126838,
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
          "id": "442c970c63bcac4ed3336c0f7a47305821819a70",
          "message": "Support rebalance-safe-commits with external commits",
          "timestamp": "2025-01-11T15:04:47Z",
          "url": "https://github.com/zio/zio-kafka/pull/1425/commits/442c970c63bcac4ed3336c0f7a47305821819a70"
        },
        "date": 1736679615139,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordPar",
            "value": 13.418268289109685,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeq",
            "value": 11.20370384983877,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 593.4914656,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 593.5748892200002,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 186.23372690380947,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 464.6450257466666,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 547.02015406,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 538.2261942599999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 563.92457602,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 571.4820967800001,
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
          "id": "ef2d73d9bb9f56031f51cb9210b85e10b06a7f16",
          "message": "Support rebalance-safe-commits with external commits",
          "timestamp": "2025-01-11T15:04:47Z",
          "url": "https://github.com/zio/zio-kafka/pull/1425/commits/ef2d73d9bb9f56031f51cb9210b85e10b06a7f16"
        },
        "date": 1736687263011,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordPar",
            "value": 12.775284779986107,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeq",
            "value": 8.64802132728761,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 590.00144778,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 592.99069148,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 212.19568972257136,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 627.3854417266667,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 544.88537082,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 538.1498602799999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 563.8010882399999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 570.73993692,
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
          "id": "e700fc48a3205ad9d4ea69e77c42302181423514",
          "message": "Transaction rebalance safe commits followup",
          "timestamp": "2025-01-11T15:04:47Z",
          "url": "https://github.com/zio/zio-kafka/pull/1434/commits/e700fc48a3205ad9d4ea69e77c42302181423514"
        },
        "date": 1736713916258,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordPar",
            "value": 16.63665696200508,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeq",
            "value": 13.393502760562932,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 589.7002446399999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 589.4228292399999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 146.0595597871429,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 387.43757823333317,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 543.4894803,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 537.2111013199999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 561.8733275,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 569.96152006,
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
          "id": "dc2ad4083ccced2f29ad3998bf398d1b7f19515d",
          "message": "Update scalafmt-core to 3.8.4",
          "timestamp": "2025-01-11T15:04:47Z",
          "url": "https://github.com/zio/zio-kafka/pull/1435/commits/dc2ad4083ccced2f29ad3998bf398d1b7f19515d"
        },
        "date": 1736733726543,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordPar",
            "value": 16.38248711909336,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeq",
            "value": 13.248581078020159,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 595.15742736,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 595.5684845999999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 147.12201532714286,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 378.8985777966666,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 544.1975388999998,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 538.7231718400002,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 564.65055498,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 571.4475234399998,
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
          "id": "e60eca1c651ccf599ae53e4377d8d68db19d6df8",
          "message": "Support rebalance-safe-commits with external commits",
          "timestamp": "2025-01-13T06:37:29Z",
          "url": "https://github.com/zio/zio-kafka/pull/1425/commits/e60eca1c651ccf599ae53e4377d8d68db19d6df8"
        },
        "date": 1736755076519,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordPar",
            "value": 18.2980694734078,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeq",
            "value": 14.872700635363463,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 598.6543540400002,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 599.0975825999999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 121.85256729722221,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 325.8293586266666,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 546.9884861,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 540.4772730000001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 568.62852614,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 573.9433252800001,
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
          "id": "c5da73e89a861ef88fdf1910fc06ce733733ad57",
          "message": "Support rebalance-safe-commits with external commits (#1425)\n\nAllow rebalance-safe-commits to be used in combination with external\r\ncommits. External commits are commits done to some other system than the\r\nkafka broker, e.g. a relational database.\r\n\r\nThis new capability also supports the improved `TransactionalProducer`\r\nthat will be introduced in zio-kafka 3.0.0.\r\n\r\n---------\r\n\r\nCo-authored-by: Erik van Oosten <e.vanoosten@grons.nl>",
          "timestamp": "2025-01-13T08:41:04+01:00",
          "tree_id": "f659867c79316f7358d1ca39f54a6c006bb3dcce",
          "url": "https://github.com/zio/zio-kafka/commit/c5da73e89a861ef88fdf1910fc06ce733733ad57"
        },
        "date": 1736755211631,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordPar",
            "value": 11.7238136977657,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeq",
            "value": 10.454714293850822,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 592.1219241,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 591.51549256,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 212.61975618942859,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 538.46339502,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 543.9304669200001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 537.7214099199999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 564.3324810600002,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 570.6507670999998,
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
          "id": "e9fb148e7e3a96382fbab1cd80aea0d465cbddd8",
          "message": "Update README.md",
          "timestamp": "2025-01-13T07:41:09Z",
          "url": "https://github.com/zio/zio-kafka/pull/1436/commits/e9fb148e7e3a96382fbab1cd80aea0d465cbddd8"
        },
        "date": 1736755302680,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordPar",
            "value": 18.608375572949395,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeq",
            "value": 14.782518272073837,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 592.1893072999999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 593.14349832,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 118.92216066388887,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 329.63912542666657,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 545.1266007400002,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 537.9807215400001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 563.8442508999998,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 570.8928719200001,
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
          "id": "77c631a237e4a702d7c22e7e1954f00a0697854b",
          "message": "Update scalafmt-core to 3.8.4 (#1435)\n\n📦 Updates\r\n[org.scalameta:scalafmt-core](https://github.com/scalameta/scalafmt)\r\nfrom `3.8.3` to `3.8.4`",
          "timestamp": "2025-01-13T09:25:08+01:00",
          "tree_id": "1551a3e74715cd88f33324c26326a79ebe4fe00f",
          "url": "https://github.com/zio/zio-kafka/commit/77c631a237e4a702d7c22e7e1954f00a0697854b"
        },
        "date": 1736757821473,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordPar",
            "value": 15.877225545862155,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeq",
            "value": 12.929985924281736,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 589.09743318,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 590.2294333200002,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 146.3222659996032,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 395.80370676,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 544.0072002400001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 537.7847871600001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 563.2187559199999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 570.70703746,
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
          "id": "65dec9dcf06b2ff7fb1a9d3147afd94940f1fdbf",
          "message": "Transaction rebalance safe commits followup",
          "timestamp": "2025-01-13T08:25:12Z",
          "url": "https://github.com/zio/zio-kafka/pull/1434/commits/65dec9dcf06b2ff7fb1a9d3147afd94940f1fdbf"
        },
        "date": 1736759266830,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordPar",
            "value": 12.856475515944503,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeq",
            "value": 11.171894006689916,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 592.36868878,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 593.8937044200002,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 212.58461651666664,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 527.8830751266665,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 543.1212159999999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 536.8703594599999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 563.4785270800002,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 571.59808146,
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
          "id": "ad2ae68fc6901903c3cc6c9b5ae648b046fb338d",
          "message": "Transaction rebalance safe commits followup",
          "timestamp": "2025-01-13T08:25:12Z",
          "url": "https://github.com/zio/zio-kafka/pull/1434/commits/ad2ae68fc6901903c3cc6c9b5ae648b046fb338d"
        },
        "date": 1736789295572,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordPar",
            "value": 14.107669951895334,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeq",
            "value": 11.6041629419295,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 592.16004698,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 592.39592564,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 185.96267015619048,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 429.74104365333335,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 544.2685701999999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 538.0722438000001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 563.6797918999999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 572.8121974,
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
          "id": "6a632f862882ddae0983530b679da34672b50cc2",
          "message": "Simplify transactional producing",
          "timestamp": "2025-01-13T08:25:12Z",
          "url": "https://github.com/zio/zio-kafka/pull/1434/commits/6a632f862882ddae0983530b679da34672b50cc2"
        },
        "date": 1736794208196,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordPar",
            "value": 16.52710328910639,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeq",
            "value": 13.072537792029959,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 592.6585952400001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 594.5265597399999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 145.36713408928574,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 389.2487872833333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 544.23603238,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 538.80020946,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 563.85668788,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 572.4187578400001,
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
          "id": "b426b3711bf4772fb55b3a806351c4c7f93c96f6",
          "message": "Update sbt-scalafix to 0.14.0",
          "timestamp": "2025-01-13T08:25:12Z",
          "url": "https://github.com/zio/zio-kafka/pull/1438/commits/b426b3711bf4772fb55b3a806351c4c7f93c96f6"
        },
        "date": 1736905618433,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordPar",
            "value": 12.216815272623471,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeq",
            "value": 11.54830285524432,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 594.1471166400001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 593.5308323200001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 163.6430472005238,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 354.29407889333334,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 546.94897718,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 540.17481336,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 566.15278334,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 575.52427812,
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
          "id": "5c579cc266b9de2abf1c5a5a2dd8a0152f6f0d3f",
          "message": "Update sbt-scalafix to 0.14.0 (#1438)\n\n## About this PR\n📦 Updates\n[ch.epfl.scala:sbt-scalafix](https://github.com/scalacenter/sbt-scalafix)\nfrom `0.13.0` to `0.14.0`\n\n📜 [GitHub Release\nNotes](https://github.com/scalacenter/sbt-scalafix/releases/tag/v0.14.0)\n- [Version\nDiff](https://github.com/scalacenter/sbt-scalafix/compare/v0.13.0...v0.14.0)\n\n## Usage\n✅ **Please merge!**\n\nI'll automatically update this PR to resolve conflicts as long as you\ndon't change it yourself.\n\nIf you'd like to skip this version, you can just close this PR. If you\nhave any feedback, just mention me in the comments below.\n\nConfigure Scala Steward for your repository with a\n[`.scala-steward.conf`](https://github.com/scala-steward-org/scala-steward/blob/8929fadb6f535472bd63397b1cb55a835fa1b5cb/docs/repo-specific-configuration.md)\nfile.\n\n_Have a fantastic day writing Scala!_\n\n<details>\n<summary>⚙ Adjust future updates</summary>\n\nAdd this to your `.scala-steward.conf` file to ignore future updates of\nthis dependency:\n```\nupdates.ignore = [ { groupId = \"ch.epfl.scala\", artifactId = \"sbt-scalafix\" } ]\n```\nOr, add this to slow down future updates of this dependency:\n```\ndependencyOverrides = [{\n  pullRequests = { frequency = \"30 days\" },\n  dependency = { groupId = \"ch.epfl.scala\", artifactId = \"sbt-scalafix\" }\n}]\n```\n</details>\n\n<sup>\nlabels: sbt-plugin-update, early-semver-major, semver-spec-minor,\ncommit-count:1\n</sup>\n\n<!-- scala-steward = {\n  \"Update\" : {\n    \"ForArtifactId\" : {\n      \"crossDependency\" : [\n        {\n          \"groupId\" : \"ch.epfl.scala\",\n          \"artifactId\" : {\n            \"name\" : \"sbt-scalafix\",\n            \"maybeCrossName\" : null\n          },\n          \"version\" : \"0.13.0\",\n          \"sbtVersion\" : \"1.0\",\n          \"scalaVersion\" : \"2.12\",\n          \"configurations\" : null\n        }\n      ],\n      \"newerVersions\" : [\n        \"0.14.0\"\n      ],\n      \"newerGroupId\" : null,\n      \"newerArtifactId\" : null\n    }\n  },\n  \"Labels\" : [\n    \"sbt-plugin-update\",\n    \"early-semver-major\",\n    \"semver-spec-minor\",\n    \"commit-count:1\"\n  ]\n} -->\n\nCo-authored-by: zio-scala-steward[bot] <145262613+zio-scala-steward[bot]@users.noreply.github.com>",
          "timestamp": "2025-01-15T07:59:32+01:00",
          "tree_id": "ba873343e15147c99ff3fe54e2b6aba4792351a4",
          "url": "https://github.com/zio/zio-kafka/commit/5c579cc266b9de2abf1c5a5a2dd8a0152f6f0d3f"
        },
        "date": 1736925473119,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordPar",
            "value": 15.120212328553603,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeq",
            "value": 12.633297276800219,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 591.5851481000001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 591.886919,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 155.74478245880954,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 397.27710849333334,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 546.18120336,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 539.03657246,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 564.4433887199999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 571.25625408,
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
          "id": "13fdaa1ad1cef2e4202236971e4cf49774566e1a",
          "message": "Retry publish after Auth error",
          "timestamp": "2025-01-15T06:59:34Z",
          "url": "https://github.com/zio/zio-kafka/pull/1437/commits/13fdaa1ad1cef2e4202236971e4cf49774566e1a"
        },
        "date": 1736934246807,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordPar",
            "value": 14.838281125519238,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeq",
            "value": 12.45835114179669,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 597.8133379,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 599.1841699199999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 169.69524127200003,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 358.58110033666674,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 546.6947178,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 540.6285955399998,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 567.3619671800001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 575.27161404,
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
          "id": "5612ba1f2b1310298e2a0f65b2c6cbd305c359ba",
          "message": "Deprecate `restartStreamOnRebalancing`",
          "timestamp": "2025-01-15T06:59:34Z",
          "url": "https://github.com/zio/zio-kafka/pull/1439/commits/5612ba1f2b1310298e2a0f65b2c6cbd305c359ba"
        },
        "date": 1736971709897,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordPar",
            "value": 12.56714692709488,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeq",
            "value": 10.49460776321591,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 592.9487323200001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 597.5437615600001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 205.59326816885718,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 475.12840278666664,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 548.8939933399998,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 540.9047364799999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 567.102194,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 573.5885748800001,
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
          "id": "5ac9b0fb3a05de2701db4bc3d36df40574e98a45",
          "message": "Update scala-library to 2.13.16",
          "timestamp": "2025-01-15T06:59:34Z",
          "url": "https://github.com/zio/zio-kafka/pull/1440/commits/5ac9b0fb3a05de2701db4bc3d36df40574e98a45"
        },
        "date": 1736992009689,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordPar",
            "value": 15.163119941905741,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeq",
            "value": 12.43634311394069,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 591.2223039599999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 594.4246640800001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 173.98875709695238,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 422.6013180133333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 544.7675642799999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 537.67495694,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 564.69959492,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 572.0574954200002,
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
          "id": "2de285c889fe368574723ea852e3ef6eda46d5aa",
          "message": "Deprecate `restartStreamOnRebalancing` (#1439)\n\nThe feature `restartStreamOnRebalancing` is no longer needed in\r\nzio-kafka 3.x as transactional producing will work without it.",
          "timestamp": "2025-01-16T08:39:41+01:00",
          "tree_id": "8521612652f7f8927dc9529c6a3fdf551c98ede1",
          "url": "https://github.com/zio/zio-kafka/commit/2de285c889fe368574723ea852e3ef6eda46d5aa"
        },
        "date": 1737014312673,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordPar",
            "value": 10.992318211570877,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeq",
            "value": 10.79152939841144,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 596.48983808,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 594.4737309399999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 203.7020222872381,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 509.21504854000005,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 546.7118673,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 539.9926619600001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 566.9715929399999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 575.2749145999999,
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
          "id": "507007f4b56f29a0e8dad2046fec0c9c103232d3",
          "message": "Update scala-library to 2.13.16 (#1440)\n\n📦 Updates [org.scala-lang:scala-library](https://github.com/scala/scala)\r\nfrom `2.13.15` to `2.13.16`",
          "timestamp": "2025-01-16T08:40:40+01:00",
          "tree_id": "9f159d8fc3e02deec04457e08ce6642e20d368f9",
          "url": "https://github.com/zio/zio-kafka/commit/507007f4b56f29a0e8dad2046fec0c9c103232d3"
        },
        "date": 1737014357649,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordPar",
            "value": 17.651358442191096,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeq",
            "value": 14.346884421833987,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 594.60498928,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 595.6438927800001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 131.16324753888892,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 357.15690637333336,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 546.31714348,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 538.2509961799999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 565.33615848,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 572.11167918,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          }
        ]
      }
    ]
  }
}