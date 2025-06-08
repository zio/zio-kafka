window.BENCHMARK_DATA = {
  "lastUpdate": 1749371265997,
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
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordPar",
            "value": 70.70577253615788,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordSeq",
            "value": 79.37155633718436,
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
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordPar",
            "value": 57.43276060724755,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordSeq",
            "value": 73.40542316780169,
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
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordPar",
            "value": 71.6133684899823,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordSeq",
            "value": 93.22303452999992,
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
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordPar",
            "value": 87.31020163257395,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordSeq",
            "value": 105.48456214929296,
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
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordPar",
            "value": 87.18794269679644,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordSeq",
            "value": 105.58546318515975,
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
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordPar",
            "value": 59.48885946752911,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordSeq",
            "value": 75.07422682972906,
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
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordPar",
            "value": 59.80034096468439,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordSeq",
            "value": 75.68187306116502,
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
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordPar",
            "value": 67.12707666513232,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordSeq",
            "value": 81.39749217430827,
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
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordPar",
            "value": 54.0609286925847,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordSeq",
            "value": 64.89192934249836,
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
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordPar",
            "value": 76.346184984404,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordSeq",
            "value": 91.91418590054198,
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
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordPar",
            "value": 85.29647653737264,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordSeq",
            "value": 95.65062917005515,
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
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordPar",
            "value": 62.9832962384675,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordSeq",
            "value": 77.33960468758595,
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
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordPar",
            "value": 66.13663738779387,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordSeq",
            "value": 79.15589874041827,
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
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordPar",
            "value": 90.97262112985112,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordSeq",
            "value": 92.66527135136234,
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
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordPar",
            "value": 56.65286347648765,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordSeq",
            "value": 69.70154429334758,
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
          "id": "81b9f3e2b5a72a2b90504aa6755164bbcf9b8e88",
          "message": "Update scalafmt-core to 3.8.5 (#1441)\n\n## About this PR\n📦 Updates\n[org.scalameta:scalafmt-core](https://github.com/scalameta/scalafmt)\nfrom `3.8.4` to `3.8.5`\n\n📜 [GitHub Release\nNotes](https://github.com/scalameta/scalafmt/releases/tag/v3.8.5) -\n[Version\nDiff](https://github.com/scalameta/scalafmt/compare/v3.8.4...v3.8.5)\n\n## Usage\n✅ **Please merge!**\n\nI'll automatically update this PR to resolve conflicts as long as you\ndon't change it yourself.\n\nIf you'd like to skip this version, you can just close this PR. If you\nhave any feedback, just mention me in the comments below.\n\nConfigure Scala Steward for your repository with a\n[`.scala-steward.conf`](https://github.com/scala-steward-org/scala-steward/blob/8929fadb6f535472bd63397b1cb55a835fa1b5cb/docs/repo-specific-configuration.md)\nfile.\n\n_Have a fantastic day writing Scala!_\n\n<details>\n<summary>🔍 Files still referring to the old version number</summary>\n\nThe following files still refer to the old version number (3.8.4).\nYou might want to review and update them manually.\n```\n.git-blame-ignore-revs\n```\n</details>\n<details>\n<summary>⚙ Adjust future updates</summary>\n\nAdd this to your `.scala-steward.conf` file to ignore future updates of\nthis dependency:\n```\nupdates.ignore = [ { groupId = \"org.scalameta\", artifactId = \"scalafmt-core\" } ]\n```\nOr, add this to slow down future updates of this dependency:\n```\ndependencyOverrides = [{\n  pullRequests = { frequency = \"30 days\" },\n  dependency = { groupId = \"org.scalameta\", artifactId = \"scalafmt-core\" }\n}]\n```\n</details>\n\n<sup>\nlabels: library-update, early-semver-patch, semver-spec-patch,\nold-version-remains, commit-count:1\n</sup>\n\n<!-- scala-steward = {\n  \"Update\" : {\n    \"ForArtifactId\" : {\n      \"crossDependency\" : [\n        {\n          \"groupId\" : \"org.scalameta\",\n          \"artifactId\" : {\n            \"name\" : \"scalafmt-core\",\n            \"maybeCrossName\" : \"scalafmt-core_2.13\"\n          },\n          \"version\" : \"3.8.4\",\n          \"sbtVersion\" : null,\n          \"scalaVersion\" : null,\n          \"configurations\" : null\n        }\n      ],\n      \"newerVersions\" : [\n        \"3.8.5\"\n      ],\n      \"newerGroupId\" : null,\n      \"newerArtifactId\" : null\n    }\n  },\n  \"Labels\" : [\n    \"library-update\",\n    \"early-semver-patch\",\n    \"semver-spec-patch\",\n    \"old-version-remains\",\n    \"commit-count:1\"\n  ]\n} -->\n\nCo-authored-by: zio-scala-steward[bot] <145262613+zio-scala-steward[bot]@users.noreply.github.com>",
          "timestamp": "2025-01-17T09:22:02+01:00",
          "tree_id": "e3fbef06d9c7945a4a2424ac82032b2082e89d1f",
          "url": "https://github.com/zio/zio-kafka/commit/81b9f3e2b5a72a2b90504aa6755164bbcf9b8e88"
        },
        "date": 1737103251825,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 595.83479356,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 596.0686703799998,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 207.33486740133338,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 428.02183186,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordPar",
            "value": 84.44273444453957,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordSeq",
            "value": 93.50874720403867,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 549.1034145799999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 539.41172102,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 567.5438085000001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 574.5791538600001,
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
          "id": "645591bca2c1f466e82ae3b044a9457fe14b50d9",
          "message": "Producer fails faster, Retry publish after Auth error (#1437)\n\n1. When the producer fails to send a message from a batch of messages,\r\nit stops sending after the first error. This behavior is extended by\r\nalso stopping after a failure from Kafka's callback.\r\n\r\n2. Introduce producer setting `authErrorRetrySchedule` on which sending\r\nafter auth errors (`AuthorizationException` and\r\n`AuthenticationException`) can be retried. Auth error occur on some slow\r\nbrokers.\r\n\r\nBoth changes have been made possible by a new test framework in which we\r\nhave precise control over the order of callbacks.\r\n\r\nThis change is not binary compatible due to changes in\r\n`ProducerSettings`.\r\n\r\nAlso:\r\n - cleanup `Producer` scaladocs\r\n - add more producer tests\r\n\r\n---------\r\n\r\nCo-authored-by: Steven Vroonland <svroonland@gmail.com>",
          "timestamp": "2025-01-19T14:59:35+01:00",
          "tree_id": "0835ceb74931e30f6ac437d37668fe69a0bc2623",
          "url": "https://github.com/zio/zio-kafka/commit/645591bca2c1f466e82ae3b044a9457fe14b50d9"
        },
        "date": 1737296312181,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 595.3757236600001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 596.8187234600002,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 156.95331048285715,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 395.3586430566668,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordPar",
            "value": 62.17848783357353,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordSeq",
            "value": 75.75148422091539,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 545.0635847000001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 537.2367896999999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 566.2221059,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 573.7328851000001,
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
          "id": "7318cc9394dfa6f1da67dc363f778061c604639f",
          "message": "Make it easy to configure compression and linger (#1442)\n\nAdd methods `withCompression` and `withLinger` to `ProducerSettings`.",
          "timestamp": "2025-01-19T17:27:36+01:00",
          "tree_id": "e8ab4e17dbaf928889360b322cf8b15b6d0d2a61",
          "url": "https://github.com/zio/zio-kafka/commit/7318cc9394dfa6f1da67dc363f778061c604639f"
        },
        "date": 1737305206478,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 595.8163593600001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 598.37740112,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 222.8709996622381,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 500.87841886000007,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordPar",
            "value": 80.50314430526967,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordSeq",
            "value": 98.8708219540792,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 545.5107284,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 538.68993718,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 564.2985639200001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 575.43802196,
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
          "id": "5e7c78cbca46d1a331f8e478bbcf1fded3ed87e8",
          "message": "Improve `withSendBufferSize` docs (#1443)\n\nPerformance research from @lukestephenson suggests that a higher send\r\nbuffer size is good for performance in some use cases. (See #531.)",
          "timestamp": "2025-01-22T10:23:27+01:00",
          "tree_id": "52ae965470a0c51076468bc4798a575f69a528e2",
          "url": "https://github.com/zio/zio-kafka/commit/5e7c78cbca46d1a331f8e478bbcf1fded3ed87e8"
        },
        "date": 1737538928217,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 594.29604218,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 596.2431827199999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 115.50605511733333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 326.9627113433333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordPar",
            "value": 51.121407392600865,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordSeq",
            "value": 62.51698412503801,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 547.0898738,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 538.7492514600001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 566.88483632,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 573.8034230799999,
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
          "id": "ea22b74296170d195dfd21364907ca98c8ff0efb",
          "message": "Update scalafmt-core to 3.8.6 (#1445)\n\n📦 Updates\r\n[org.scalameta:scalafmt-core](https://github.com/scalameta/scalafmt)\r\nfrom `3.8.5` to `3.8.6`",
          "timestamp": "2025-01-24T17:42:35+01:00",
          "tree_id": "51ecda2da865cc41a8b4dbd0d78c45e4b0ab4370",
          "url": "https://github.com/zio/zio-kafka/commit/ea22b74296170d195dfd21364907ca98c8ff0efb"
        },
        "date": 1737738128665,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 591.6087956399999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 597.55028176,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 195.60694569238098,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 443.1917108200001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordPar",
            "value": 73.57963595441429,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordSeq",
            "value": 83.69893671714308,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 538.97234746,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 531.4632829199999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 558.6159572600001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 564.17002636,
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
          "id": "c8293b75e81056b3f2d162f9e26c34ea451b0a06",
          "message": "Harmonize consumer diagnostics handling (#1446)\n\nSome of the different `Consumer` builders were not using\r\n`ConcurrentDiagnostics` yet to protect the consumer from slow\r\ndiagnostics implementations. This is fixed.\r\n\r\nSince `ConcurrentDiagnostics` injects the final event, the `Consumer`\r\nbuilders do not need to do this.",
          "timestamp": "2025-01-29T12:32:30+01:00",
          "tree_id": "ea621206d5322c84912e334027c48380b6af8adf",
          "url": "https://github.com/zio/zio-kafka/commit/c8293b75e81056b3f2d162f9e26c34ea451b0a06"
        },
        "date": 1738151634448,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 586.43085226,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 588.91367648,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 147.84392876928575,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 413.96880448,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordPar",
            "value": 58.104630471263704,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordSeq",
            "value": 71.24855247071594,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 538.2353328199999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 530.62460016,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 555.06401248,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 563.8881312200001,
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
          "id": "3e0ff096cbc2552135d7e84d0d142af854d80d19",
          "message": "Update scala3-compiler, scala3-library, ... to 3.3.5 (#1447)\n\n📦 Updates \r\n* [org.scala-lang:scala3-compiler](https://github.com/scala/scala3)\r\n* [org.scala-lang:scala3-library](https://github.com/scala/scala3)\r\n* [org.scala-lang:tasty-core](https://github.com/scala/scala3)\r\n\r\n from `3.3.4` to `3.3.5`",
          "timestamp": "2025-01-30T09:11:04+01:00",
          "tree_id": "3b315c43f739b9b4e3703f16ecfa928ff9482bdc",
          "url": "https://github.com/zio/zio-kafka/commit/3e0ff096cbc2552135d7e84d0d142af854d80d19"
        },
        "date": 1738225761615,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 585.5980115,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 587.8222264599999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 110.03263158266665,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 314.85403011333335,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordPar",
            "value": 51.13760795099691,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordSeq",
            "value": 62.541053107465444,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 536.7688867600001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 530.05454422,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 554.9601657,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 561.10056232,
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
          "id": "29d1031018ee561b2bc1ceb88202b382cbaeaf2d",
          "message": "Avoid EmbeddedKafka.createCustomTopic (#1448)\n\n... because it has a very low timeout\r\n(https://github.com/embeddedkafka/embedded-kafka/issues/548) which\r\ndoesn't work well on a busy low-end build machine.\r\n\r\nAlso: move admin tests to correct package.",
          "timestamp": "2025-02-02T08:48:10+01:00",
          "tree_id": "bfffecb8c137621fc070e11b8d9b73e17f2e50d5",
          "url": "https://github.com/zio/zio-kafka/commit/29d1031018ee561b2bc1ceb88202b382cbaeaf2d"
        },
        "date": 1738483721934,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 585.2957663799999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 586.13507344,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 211.04098581214285,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 526.6994440733334,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordPar",
            "value": 69.4342136202791,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordSeq",
            "value": 99.42560298484783,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 537.0437589200001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 530.5442043999999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 557.0743673400001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 562.88990844,
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
          "id": "b66648d9cdf8678c1d13f9adc37fbd9e1d8dfbbc",
          "message": "Update sbt-native-packager to 1.11.1 (#1450)\n\n## About this PR\n📦 Updates\n[com.github.sbt:sbt-native-packager](https://github.com/sbt/sbt-native-packager)\nfrom `1.11.0` to `1.11.1`\n\n📜 [GitHub Release\nNotes](https://github.com/sbt/sbt-native-packager/releases/tag/v1.11.1)\n- [Version\nDiff](https://github.com/sbt/sbt-native-packager/compare/v1.11.0...v1.11.1)\n\n## Usage\n✅ **Please merge!**\n\nI'll automatically update this PR to resolve conflicts as long as you\ndon't change it yourself.\n\nIf you'd like to skip this version, you can just close this PR. If you\nhave any feedback, just mention me in the comments below.\n\nConfigure Scala Steward for your repository with a\n[`.scala-steward.conf`](https://github.com/scala-steward-org/scala-steward/blob/8929fadb6f535472bd63397b1cb55a835fa1b5cb/docs/repo-specific-configuration.md)\nfile.\n\n_Have a fantastic day writing Scala!_\n\n<details>\n<summary>⚙ Adjust future updates</summary>\n\nAdd this to your `.scala-steward.conf` file to ignore future updates of\nthis dependency:\n```\nupdates.ignore = [ { groupId = \"com.github.sbt\", artifactId = \"sbt-native-packager\" } ]\n```\nOr, add this to slow down future updates of this dependency:\n```\ndependencyOverrides = [{\n  pullRequests = { frequency = \"30 days\" },\n  dependency = { groupId = \"com.github.sbt\", artifactId = \"sbt-native-packager\" }\n}]\n```\n</details>\n\n<sup>\nlabels: sbt-plugin-update, early-semver-patch, semver-spec-patch,\ncommit-count:1\n</sup>\n\n<!-- scala-steward = {\n  \"Update\" : {\n    \"ForArtifactId\" : {\n      \"crossDependency\" : [\n        {\n          \"groupId\" : \"com.github.sbt\",\n          \"artifactId\" : {\n            \"name\" : \"sbt-native-packager\",\n            \"maybeCrossName\" : null\n          },\n          \"version\" : \"1.11.0\",\n          \"sbtVersion\" : \"1.0\",\n          \"scalaVersion\" : \"2.12\",\n          \"configurations\" : null\n        }\n      ],\n      \"newerVersions\" : [\n        \"1.11.1\"\n      ],\n      \"newerGroupId\" : null,\n      \"newerArtifactId\" : null\n    }\n  },\n  \"Labels\" : [\n    \"sbt-plugin-update\",\n    \"early-semver-patch\",\n    \"semver-spec-patch\",\n    \"commit-count:1\"\n  ]\n} -->\n\nCo-authored-by: zio-scala-steward[bot] <145262613+zio-scala-steward[bot]@users.noreply.github.com>",
          "timestamp": "2025-02-05T08:29:07+01:00",
          "tree_id": "87be6063756b0ec1befa56b7e8f9a07a113bf290",
          "url": "https://github.com/zio/zio-kafka/commit/b66648d9cdf8678c1d13f9adc37fbd9e1d8dfbbc"
        },
        "date": 1738741673932,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 583.9560726,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 584.18563378,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 206.57159077258726,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 506.29258636999975,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordPar",
            "value": 61.83734401757227,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordSeq",
            "value": 78.27829535748889,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 535.7093963599999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 529.6557226200001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 554.61401306,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 560.4869547599999,
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
          "id": "96996076bf5078a47c0a7002d1d8cdbc31f546ea",
          "message": "Update zio, zio-streams, zio-test, ... to 2.1.15 (#1453)\n\n📦 Updates\r\n* [dev.zio:zio](https://github.com/zio/zio)\r\n* [dev.zio:zio-streams](https://github.com/zio/zio)\r\n* [dev.zio:zio-test](https://github.com/zio/zio)\r\n* [dev.zio:zio-test-sbt](https://github.com/zio/zio)\r\n\r\n from `2.1.14` to `2.1.15`\r\n\r\n📜 [GitHub Release\r\nNotes](https://github.com/zio/zio/releases/tag/v2.1.15) - [Version\r\nDiff](https://github.com/zio/zio/compare/v2.1.14...v2.1.15)",
          "timestamp": "2025-02-05T14:06:00+01:00",
          "tree_id": "11bcced7bcd22dfd1887f86c01ff003422175b43",
          "url": "https://github.com/zio/zio-kafka/commit/96996076bf5078a47c0a7002d1d8cdbc31f546ea"
        },
        "date": 1738761894644,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 593.68848088,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 593.8467383599999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 195.8424902521905,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 423.6544691000001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordPar",
            "value": 69.80395057614021,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordSeq",
            "value": 90.59075480577332,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 540.5371171,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 532.9264703799998,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 561.60366234,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 567.8873484000001,
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
          "id": "9c72bffd470c3a71130684449b66186f3c40d26d",
          "message": "Deprecate accessor methods (#1449)\n\nAccessor methods are [deprecated by the ZIO\r\ncommunity](https://zio.dev/reference/service-pattern/accessor-methods).\r\nTherefore, all accessor methods for `Consumer`, `Producer` and\r\n`TransactionalProducer` have been deprecated so that they can be removed\r\nin zio-kafka 3.0.0.\r\n\r\nAccessor methods were heavily used in the zio-kafka unit tests.\r\nTherefore, all tests and benchmarks have been rewritten to use services\r\ndirectly. `KafkaTestUtils` has been extended with several methods to\r\nmake this easier. To nudge users of the zio-kafka-test-kit, anything\r\nthat promotes accessor methods is also deprecated.\r\n\r\nThe unit tests now have less layer trickery and are easier to\r\nunderstand.\r\n\r\nThe documentation has been extended with a migration guide:\r\nhttps://github.com/zio/zio-kafka/blob/accessors/docs/migrating-to-2.11.md",
          "timestamp": "2025-02-08T14:15:56+01:00",
          "tree_id": "0d5c4cfc6279b489078a369ffced5ba261fc3c36",
          "url": "https://github.com/zio/zio-kafka/commit/9c72bffd470c3a71130684449b66186f3c40d26d"
        },
        "date": 1739021759286,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 587.09147912,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 590.526631,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 228.92139804600006,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 559.8543951933333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordPar",
            "value": 79.00797234027269,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordSeq",
            "value": 100.79915543622727,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 538.94679062,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 531.6712505999999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 557.2623250000001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 563.1844018799999,
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
          "id": "b5d633fbbd3c06fec47b5bcd7f163aaa2680ee98",
          "message": "Report dependencies to GitHub (#1456)\n\nAdd a workflow for reporting the dependencies to GitHub, allowing\r\nsecurity warnings from GitHub.",
          "timestamp": "2025-02-08T14:32:08+01:00",
          "tree_id": "01c1c227f88bc58428aed4427fcad1a0de8ca71a",
          "url": "https://github.com/zio/zio-kafka/commit/b5d633fbbd3c06fec47b5bcd7f163aaa2680ee98"
        },
        "date": 1739022656064,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 588.0120078599999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 587.9441814800001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 196.74039744399997,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 483.0948053533333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordPar",
            "value": 77.61764347670177,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordSeq",
            "value": 92.13820205101547,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 538.09849422,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 530.1048896799999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 557.0771771399999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 565.86110908,
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
          "distinct": false,
          "id": "047bbdfa9c45808e9d08213b4f5a1b987f250259",
          "message": "Remove accessor methods from docs (#1455)\n\nIn preparation of removing the accessor method in zio-kafka 3.0.0, this\r\nchange already removes them from the user documentation.\r\n\r\nThe diff looks a bit large because how to create a consumer was split to\r\na different file.\r\n\r\nFile `writing-tests.md` is updated in a separate PR (#1449) as it\r\ninvolves code changes as well.",
          "timestamp": "2025-02-08T14:28:56+01:00",
          "tree_id": "f8ed362e32d33a8097d80e823134a58dbcf5dead",
          "url": "https://github.com/zio/zio-kafka/commit/047bbdfa9c45808e9d08213b4f5a1b987f250259"
        },
        "date": 1739023274806,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 586.3598740000001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 586.17559686,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 127.00413093166664,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 352.2438052666666,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordPar",
            "value": 54.43036704596146,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordSeq",
            "value": 67.05495910212773,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 538.0325896999999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 530.4927298599999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 556.22478138,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 563.7053685400001,
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
          "id": "d7fdf1a3204d6ee07684b718b0c711aefff1b3cd",
          "message": "Make benchmarks and flame graphs more visible (#1454)\n\nAlso: add summary in performance section",
          "timestamp": "2025-02-09T09:10:01+01:00",
          "tree_id": "dada5a6199d44784f651803d15e9085b2856fc6b",
          "url": "https://github.com/zio/zio-kafka/commit/d7fdf1a3204d6ee07684b718b0c711aefff1b3cd"
        },
        "date": 1739090000532,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 586.85247648,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 587.1141271,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 134.1833927974603,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 369.49897238000005,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordPar",
            "value": 60.7473399298222,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordSeq",
            "value": 71.86649588094022,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 537.9442359999999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 530.7398029200001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 556.2934697400001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 562.5222276599999,
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
          "id": "f1357cf23b5bef80528b38d72a950e0915955d6a",
          "message": "Remove accessor methods from docs (2) (#1459)\n\nIn preparation of removing the accessor method in zio-kafka 3.0.0, this\r\nchange already removes them from the user documentation.\r\n\r\nThis PR contains documentation that was missed in #1455.\r\n\r\nAlso: extend docs on stream consuming.",
          "timestamp": "2025-02-09T09:10:16+01:00",
          "tree_id": "7169b310ab3e37f86863ff1be32183711520e038",
          "url": "https://github.com/zio/zio-kafka/commit/f1357cf23b5bef80528b38d72a950e0915955d6a"
        },
        "date": 1739090030642,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 586.7954710199999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 589.46330658,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 167.85920236285713,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 423.0946765599999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordPar",
            "value": 67.16938209460936,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordSeq",
            "value": 83.79808163872204,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 537.7372699000002,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 531.2952531600001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 556.88214128,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 564.69474348,
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
          "id": "6caa472f75bf43108da169517ce329c45151050b",
          "message": "Bump scalacenter/sbt-dependency-submission from 2 to 3 (#1462)\n\nBumps\r\n[scalacenter/sbt-dependency-submission](https://github.com/scalacenter/sbt-dependency-submission)\r\nfrom 2 to 3.",
          "timestamp": "2025-02-10T09:34:27+01:00",
          "tree_id": "f4544e3288f28e2742f5c02c603a444f1baeb798",
          "url": "https://github.com/zio/zio-kafka/commit/6caa472f75bf43108da169517ce329c45151050b"
        },
        "date": 1739177575266,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 586.7860178200001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 588.0815754600001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 145.24994050499998,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 396.2297277500001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordPar",
            "value": 59.81021595100446,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordSeq",
            "value": 74.68144627023891,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 537.5416809200001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 531.2212366800002,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 556.86812042,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 561.98613914,
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
          "id": "37fed48acc5eb92fc6dd756addfd77f68e84509f",
          "message": "Update zio-sbt-ci, zio-sbt-ecosystem, ... to 0.4.0-alpha.31 (#1465)\n\n📦 Updates \r\n* [dev.zio:zio-sbt-ci](https://github.com/zio/zio-sbt)\r\n* [dev.zio:zio-sbt-ecosystem](https://github.com/zio/zio-sbt)\r\n* [dev.zio:zio-sbt-website](https://github.com/zio/zio-sbt)\r\n\r\n from `0.4.0-alpha.30` to `0.4.0-alpha.31`",
          "timestamp": "2025-02-13T10:02:25+01:00",
          "tree_id": "ef8a801e69478112aaee2bdc8ca90d119d952e41",
          "url": "https://github.com/zio/zio-kafka/commit/37fed48acc5eb92fc6dd756addfd77f68e84509f"
        },
        "date": 1739438477669,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 589.8318393599999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 591.2507358,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 172.5437411504762,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 439.56908231666677,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordPar",
            "value": 69.60133201304542,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordSeq",
            "value": 82.98423123063013,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 537.8122328599999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 532.67538668,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 558.6051092800001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 564.7247030600001,
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
          "id": "67fa94065e427152526f8d9b87c44186a63fe4f1",
          "message": "Add features to readme (#1466)\n\nAlso:\r\n- update docker-compose, following the updated tutorial\r\n- add source compatibility note in AdminClientSettings.",
          "timestamp": "2025-02-16T10:38:23+01:00",
          "tree_id": "d61f4aabd24f9bac6af6d7b80c3738e3df56c3e3",
          "url": "https://github.com/zio/zio-kafka/commit/67fa94065e427152526f8d9b87c44186a63fe4f1"
        },
        "date": 1739699826349,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 584.30230656,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 584.1115131800001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 114.6022368271111,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 325.5067137766667,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordPar",
            "value": 50.3898312701794,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordSeq",
            "value": 62.62038667354614,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 535.2650078400001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 530.43859826,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 557.3018175200002,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 562.7795645799999,
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
          "id": "3fdbed63811c3e161ee707f4064cceac8a93155a",
          "message": "Update scalafmt-core to 3.9.0 (#1469)\n\n📦 Updates\r\n[org.scalameta:scalafmt-core](https://github.com/scalameta/scalafmt)\r\nfrom `3.8.6` to `3.9.0`",
          "timestamp": "2025-02-17T08:02:05+01:00",
          "tree_id": "e7132437f1d22889d33ac8fa28496a298e466edc",
          "url": "https://github.com/zio/zio-kafka/commit/3fdbed63811c3e161ee707f4064cceac8a93155a"
        },
        "date": 1739776821668,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 585.5962384200002,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 588.17247822,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 137.48127806214288,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 379.6784049933334,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordPar",
            "value": 57.973336030497094,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordSeq",
            "value": 72.30598464184799,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 537.5371196200001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 530.42687952,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 555.36458736,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 563.0587913,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "40064529+vermas7988@users.noreply.github.com",
            "name": "Sachin verma",
            "username": "vermas7988"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "a51fbae9045fec69c8d558b21fe4228f36f36db3",
          "message": "Fetch consumerGroupMetaData less often (#1464)\n\nThe consumer group metadata only changes after a rebalance, yet\r\ncurrently we retrieve it after every poll. This change only fetches the\r\nconsumer group metadata after a rebalance.\r\n\r\nFixes #1431.",
          "timestamp": "2025-02-17T13:57:50+01:00",
          "tree_id": "0ce9d80fa4a6b169b40e01e93d99966b6ba31cda",
          "url": "https://github.com/zio/zio-kafka/commit/a51fbae9045fec69c8d558b21fe4228f36f36db3"
        },
        "date": 1739798229773,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 588.87036452,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 589.16099372,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 186.76044590438093,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 549.1545923066667,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordPar",
            "value": 84.93908034256145,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordSeq",
            "value": 100.50894233877395,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 539.2774251200001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 531.15787176,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 557.25912738,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 565.6672758,
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
          "id": "1dea69ad4a8f67f1a65e3a824756307cae380b09",
          "message": "Explore event sourced rebalance listener (#1472)\n\nWe remove even more logic from the rebalance\nlistener; it now reports which callbacks happened and no longer\ncollapses that information. This gives the runloop a full picture of\nwhat happened during the rebalance.\n\nFor now, the logic stays exactly the same, it just moved to the runloop.\n\nThis an alternative implementation to #1467 that requires less code to\nachieve the same goal.",
          "timestamp": "2025-02-19T13:19:31+01:00",
          "tree_id": "f67eb62d328b5591b2e742c9041e7da738110e25",
          "url": "https://github.com/zio/zio-kafka/commit/1dea69ad4a8f67f1a65e3a824756307cae380b09"
        },
        "date": 1739968653940,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 582.23069644,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 584.2622636000001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 118.23623125500002,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 321.55836865466665,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordPar",
            "value": 52.11870596580377,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordSeq",
            "value": 64.7210024738448,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 535.7899498599999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 530.9386526399999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 554.5509661000001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 561.11116654,
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
          "id": "9353d70f8860bdd5edfb6c8080010a1c64d6dba2",
          "message": "Prepare documentation for zio-kafka 3 (#1473)",
          "timestamp": "2025-02-19T16:03:35+01:00",
          "tree_id": "a1ca716c62c5ba1a03595fc8b9d2561c181a4def",
          "url": "https://github.com/zio/zio-kafka/commit/9353d70f8860bdd5edfb6c8080010a1c64d6dba2"
        },
        "date": 1739978989918,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 586.2869029399999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 590.9628883999999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 205.85224185104758,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 468.93912352666666,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordPar",
            "value": 79.81673638668472,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordSeq",
            "value": 90.00108288215924,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 538.5417008000001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 531.3431826999999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 557.01955854,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 565.8643534199999,
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
          "id": "886282d68c3cc7f933684e91b0f84474e3454532",
          "message": "Simplify transactional producing (#1434)\n\nIntroduce a much simpler way to use the transactional producer. It is no\nlonger needed to install a complex rebalance listener, coupling your\n`TransactionalProducer` to a `Consumer`is enough.\n\nSee #1432 for documentation.\n\n---------\n\nCo-authored-by: Erik van Oosten <e.vanoosten@grons.nl>",
          "timestamp": "2025-02-19T18:43:10+01:00",
          "tree_id": "e2969df9edd41a6234b976df833dd9ba8517daa9",
          "url": "https://github.com/zio/zio-kafka/commit/886282d68c3cc7f933684e91b0f84474e3454532"
        },
        "date": 1739988111165,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 582.7097903800001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 585.7943272800001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 142.88100673928574,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 401.02663390666663,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordPar",
            "value": 59.86747853460007,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordSeq",
            "value": 72.8124420528727,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 536.1090006600001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 530.6190981799999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 555.70596386,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 559.9415731799999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "40064529+vermas7988@users.noreply.github.com",
            "name": "Sachin verma",
            "username": "vermas7988"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "b9122d96620bbfa58eccb1bd7b6825a855b7596d",
          "message": "Simplify fetching consumer group metadata in runloop (#1474)\n\nSimplify `Runloop` by fetching consumer group metadata directly after the rebalance.\n\nFixes #1470.",
          "timestamp": "2025-02-19T19:36:29+01:00",
          "tree_id": "30dce23d1af57622edae4f07b70efcc0002b3838",
          "url": "https://github.com/zio/zio-kafka/commit/b9122d96620bbfa58eccb1bd7b6825a855b7596d"
        },
        "date": 1739991337521,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 589.3469916600001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 588.56033046,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 213.07880508866663,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 479.5498232399999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordPar",
            "value": 80.86650867557954,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordSeq",
            "value": 100.61929172368448,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 536.82109424,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 531.655227,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 557.7171699,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 563.13300632,
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
          "id": "507f5a6570ccad01afa5632d9b48b92f96e95f76",
          "message": "Remove deprecated features (#1476)",
          "timestamp": "2025-02-19T19:57:51+01:00",
          "tree_id": "5041863534784638de7e8e80b7c4f40a857978e4",
          "url": "https://github.com/zio/zio-kafka/commit/507f5a6570ccad01afa5632d9b48b92f96e95f76"
        },
        "date": 1739992592127,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 585.1792083800001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 588.3094874399999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 159.21862022809523,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 407.8078419933334,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordPar",
            "value": 66.0874556027589,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordSeq",
            "value": 82.4096363740453,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 537.40967434,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 530.5495621800001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 556.2762989199999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 565.9167536199999,
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
          "id": "f7c6122d70897ae832f84903d74cf3181ef6ffe0",
          "message": "Update sbt-scalafix to 0.14.2 (#1478)\n\n📦 Updates\n[ch.epfl.scala:sbt-scalafix](https://github.com/scalacenter/sbt-scalafix)\nfrom `0.14.0` to `0.14.2`",
          "timestamp": "2025-02-20T08:25:35+01:00",
          "tree_id": "57dd766c9d492c4b8a94431968cb1e77525bbe49",
          "url": "https://github.com/zio/zio-kafka/commit/f7c6122d70897ae832f84903d74cf3181ef6ffe0"
        },
        "date": 1740037481954,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 585.4870489200001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 584.2823373200001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 232.7637723655714,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 550.73992274,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordPar",
            "value": 78.31692240738106,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordSeq",
            "value": 96.10128419066892,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 536.63907694,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 530.43464912,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 555.1766118,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 565.59985474,
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
          "id": "2e9a6379a6aa0a104ca9786389c7742d34b435b8",
          "message": "Update scalafmt-core to 3.9.1 (#1479)\n\n📦 Updates\n[org.scalameta:scalafmt-core](https://github.com/scalameta/scalafmt)\nfrom `3.9.0` to `3.9.1`",
          "timestamp": "2025-02-22T07:46:09+01:00",
          "tree_id": "d4f1f67ff9ec86514d12effe6bcc8f01eb1c88b0",
          "url": "https://github.com/zio/zio-kafka/commit/2e9a6379a6aa0a104ca9786389c7742d34b435b8"
        },
        "date": 1740207871608,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 583.47448254,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 587.0477414000001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 141.30490057285712,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 388.89845593999996,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordPar",
            "value": 59.74353009008514,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordSeq",
            "value": 74.34303416404933,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 536.2985296400001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 531.17464542,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 556.98043422,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 564.4491551599999,
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
          "id": "5bddc0722ac47186dca311aadf8195db8b24d857",
          "message": "Simplify ProducerSpec (#1480)\n\nTest ProducerSpec contains a few heavily nested transaction tests. This\nchange removes most of that nesting.\n\nAlso:\n- add better timeout message\n- use non-transactional consumer",
          "timestamp": "2025-02-22T14:01:39+01:00",
          "tree_id": "504b994158b3bbc51afa5ea31e014033b1531486",
          "url": "https://github.com/zio/zio-kafka/commit/5bddc0722ac47186dca311aadf8195db8b24d857"
        },
        "date": 1740230398258,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 582.14288188,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 586.7567455799999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 132.03068579968254,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 382.36343547,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordPar",
            "value": 54.46097192171046,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordSeq",
            "value": 68.80249410865143,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 535.9679119600002,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 530.08948468,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 556.0500678999999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 561.02650532,
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
          "id": "f9b80d4f0a7e23b5202db833acd9e6dc3a3c969d",
          "message": "Simplify `ConsumerSpec` (#1481)\n\nUse a non-transactional consumer for consuming regular records.\n\nCreate and use consumer directly instead of via layer.\n\nLog number of records consumed in flaky test.",
          "timestamp": "2025-02-22T14:01:52+01:00",
          "tree_id": "38c66a66c7dc1024519989b56f8e10a26cd3064f",
          "url": "https://github.com/zio/zio-kafka/commit/f9b80d4f0a7e23b5202db833acd9e6dc3a3c969d"
        },
        "date": 1740230463674,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 589.93153976,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 590.4834457000001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 150.0926096607143,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 365.59344178000015,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordPar",
            "value": 61.615956359815144,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordSeq",
            "value": 76.53328193418957,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 538.1429142999999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 531.93695138,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 555.9299632000001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 564.8639591400001,
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
          "id": "3e8656daa132126417337c56fcf2050d8d3402ce",
          "message": "Document transactions (#1432)\n\nDocument the transactional producer introduced in #1434.",
          "timestamp": "2025-02-22T16:37:20+01:00",
          "tree_id": "0faf3c0199d65e00d73bda9d29037bc4dea43be9",
          "url": "https://github.com/zio/zio-kafka/commit/3e8656daa132126417337c56fcf2050d8d3402ce"
        },
        "date": 1740239916167,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 584.5028277599998,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 585.0086377,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 128.17733831880952,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 354.2973679366668,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordPar",
            "value": 55.06069964467872,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordSeq",
            "value": 68.00659393516351,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 537.5921488,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 530.19034156,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 555.3630821800001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 561.4446454,
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
          "id": "4d04f4a78a6d3f304ace34e5a70c6273873d3ad9",
          "message": "Update zio-logging-slf4j, ... to 2.5.0 (#1486)\n\n📦 Updates \n* [dev.zio:zio-logging-slf4j](https://github.com/zio/zio-logging)\n* [dev.zio:zio-logging-slf4j2](https://github.com/zio/zio-logging)\n\n from `2.4.0` to `2.5.0`",
          "timestamp": "2025-02-24T07:27:30+01:00",
          "tree_id": "f0e3ea607345f799716b0ecf028d8672c262296a",
          "url": "https://github.com/zio/zio-kafka/commit/4d04f4a78a6d3f304ace34e5a70c6273873d3ad9"
        },
        "date": 1740379574232,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 583.88433148,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 586.5992559000001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 175.23889064095238,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 439.8119160600001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordPar",
            "value": 67.85086897709141,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordSeq",
            "value": 83.33968373026221,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 536.5178971200002,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 530.43294308,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 555.6144849799999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 561.45318608,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "peter.nham@zendesk.com",
            "name": "Peter Nham",
            "username": "petern-sc"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "580366fa7725d5630f799148db6eca04e63ef2c1",
          "message": "Relax offset trait constraint (#1489)\n\nAllows the offset trait to be extended outside of this library.\n\nAddresses: https://github.com/zio/zio-kafka/issues/1487",
          "timestamp": "2025-02-26T08:43:13+01:00",
          "tree_id": "253ed070fd834b8975a3fdd3d250a4ba9144881c",
          "url": "https://github.com/zio/zio-kafka/commit/580366fa7725d5630f799148db6eca04e63ef2c1"
        },
        "date": 1740556885692,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 589.2008632799999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 589.1602171000001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 117.20306489888885,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 334.7120879713334,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordPar",
            "value": 51.58506090860788,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordSeq",
            "value": 64.5043384582415,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 539.09089628,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 532.16632936,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 558.6684835200001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 566.39774702,
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
          "id": "c0679fcbfd2aa3579c19ded3a7820bc86893c912",
          "message": "Update logback-classic to 1.5.17 (#1488)\n\n📦 Updates\n[ch.qos.logback:logback-classic](https://github.com/qos-ch/logback) from\n`1.5.16` to `1.5.17`",
          "timestamp": "2025-02-26T09:17:40+01:00",
          "tree_id": "e662b19279c6f0f5ca8c6177c6d30f9c1247100c",
          "url": "https://github.com/zio/zio-kafka/commit/c0679fcbfd2aa3579c19ded3a7820bc86893c912"
        },
        "date": 1740559016776,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 589.4427937199999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 588.66596458,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 155.48850310523812,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 412.73064136,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordPar",
            "value": 66.42054116230919,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordSeq",
            "value": 81.18632844061588,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 538.68908162,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 531.2243487400001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 557.7133915000001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 564.6122662399999,
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
          "id": "51b8340aeaad1eeb4b747a9930022b568c07b41b",
          "message": "Update zio, zio-streams, zio-test, ... to 2.1.16 (#1490)\n\n📦 Updates \n* [dev.zio:zio](https://github.com/zio/zio)\n* [dev.zio:zio-streams](https://github.com/zio/zio)\n* [dev.zio:zio-test](https://github.com/zio/zio)\n* [dev.zio:zio-test-sbt](https://github.com/zio/zio)\n\n from `2.1.15` to `2.1.16`",
          "timestamp": "2025-02-27T20:04:40+01:00",
          "tree_id": "32bf604e29db3adba4c500ddc3e0877bc48288ab",
          "url": "https://github.com/zio/zio-kafka/commit/51b8340aeaad1eeb4b747a9930022b568c07b41b"
        },
        "date": 1740684201154,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 587.4644633000001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 588.2426417399998,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 172.41543775733334,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 435.02009517999994,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordPar",
            "value": 67.82866622789567,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordSeq",
            "value": 89.61367617586264,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 538.2385007400001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 530.5846303599999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 556.7509282199999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 563.23713702,
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
          "id": "21bb4960f1e3779a29c1cc2759c8066ef386f173",
          "message": "Update scalafmt-core to 3.9.2 (#1493)\n\n## About this PR\n📦 Updates\n[org.scalameta:scalafmt-core](https://github.com/scalameta/scalafmt)\nfrom `3.9.1` to `3.9.2`\n\n📜 [GitHub Release\nNotes](https://github.com/scalameta/scalafmt/releases/tag/v3.9.2) -\n[Version\nDiff](https://github.com/scalameta/scalafmt/compare/v3.9.1...v3.9.2)\n\n## Usage\n✅ **Please merge!**\n\nI'll automatically update this PR to resolve conflicts as long as you\ndon't change it yourself.\n\nIf you'd like to skip this version, you can just close this PR. If you\nhave any feedback, just mention me in the comments below.\n\nConfigure Scala Steward for your repository with a\n[`.scala-steward.conf`](https://github.com/scala-steward-org/scala-steward/blob/8929fadb6f535472bd63397b1cb55a835fa1b5cb/docs/repo-specific-configuration.md)\nfile.\n\n_Have a fantastic day writing Scala!_\n\n<details>\n<summary>⚙ Adjust future updates</summary>\n\nAdd this to your `.scala-steward.conf` file to ignore future updates of\nthis dependency:\n```\nupdates.ignore = [ { groupId = \"org.scalameta\", artifactId = \"scalafmt-core\" } ]\n```\nOr, add this to slow down future updates of this dependency:\n```\ndependencyOverrides = [{\n  pullRequests = { frequency = \"30 days\" },\n  dependency = { groupId = \"org.scalameta\", artifactId = \"scalafmt-core\" }\n}]\n```\n</details>\n\n<sup>\nlabels: library-update, early-semver-patch, semver-spec-patch,\ncommit-count:1\n</sup>\n\n<!-- scala-steward = {\n  \"Update\" : {\n    \"ForArtifactId\" : {\n      \"crossDependency\" : [\n        {\n          \"groupId\" : \"org.scalameta\",\n          \"artifactId\" : {\n            \"name\" : \"scalafmt-core\",\n            \"maybeCrossName\" : \"scalafmt-core_2.13\"\n          },\n          \"version\" : \"3.9.1\",\n          \"sbtVersion\" : null,\n          \"scalaVersion\" : null,\n          \"configurations\" : null\n        }\n      ],\n      \"newerVersions\" : [\n        \"3.9.2\"\n      ],\n      \"newerGroupId\" : null,\n      \"newerArtifactId\" : null\n    }\n  },\n  \"Labels\" : [\n    \"library-update\",\n    \"early-semver-patch\",\n    \"semver-spec-patch\",\n    \"commit-count:1\"\n  ]\n} -->\n\nCo-authored-by: zio-scala-steward[bot] <145262613+zio-scala-steward[bot]@users.noreply.github.com>",
          "timestamp": "2025-03-01T10:24:33+01:00",
          "tree_id": "8bd10a19ea21ffa288307cf6410b087e809fc92e",
          "url": "https://github.com/zio/zio-kafka/commit/21bb4960f1e3779a29c1cc2759c8066ef386f173"
        },
        "date": 1740822299382,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 583.65468602,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 585.9821679400001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 174.1721017431746,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 444.45278734,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordPar",
            "value": 69.99226347414731,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordSeq",
            "value": 88.18706619656848,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 536.83996702,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 531.3112505799999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 556.32478284,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 561.79116478,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "40064529+vermas7988@users.noreply.github.com",
            "name": "Sachin verma",
            "username": "vermas7988"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "242bc58f8f6c4039ff8ce7e3d9f121f33309e9d3",
          "message": "Check consumer settings for transactional producer (#1492)\n\nThe transactional producer requires rebalance-safe-commits from the\nconsumer it is connected to. When rebalance-safe-commits is disabled,\nthis now results in an error.\n\nFixes #1483.",
          "timestamp": "2025-03-01T15:54:01+01:00",
          "tree_id": "c9c9280ec13a347e53a30724369d3d6eb8685c06",
          "url": "https://github.com/zio/zio-kafka/commit/242bc58f8f6c4039ff8ce7e3d9f121f33309e9d3"
        },
        "date": 1740841986783,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 585.02731266,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 586.89542336,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 149.84896804761902,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 406.6351403333333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordPar",
            "value": 58.86698770308834,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordSeq",
            "value": 73.5633061171896,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 537.51267936,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 531.04101968,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 555.18498632,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 561.66150494,
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
          "id": "94e88231312efb8877acfe700dc59fabaf14d521",
          "message": "Update sbt, scripted-plugin to 1.10.9 (#1495)\n\n📦 Updates \n* [org.scala-sbt:sbt](https://github.com/sbt/sbt)\n* [org.scala-sbt:scripted-plugin](https://github.com/sbt/sbt)\n\n from `1.10.7` to `1.10.9`",
          "timestamp": "2025-03-04T08:15:38+01:00",
          "tree_id": "5ac8b931cbb4a38070c1c0884e40ce2c06976a33",
          "url": "https://github.com/zio/zio-kafka/commit/94e88231312efb8877acfe700dc59fabaf14d521"
        },
        "date": 1741073650943,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 587.4093972000001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 585.07205674,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 161.13872717523813,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 393.91954280666664,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordPar",
            "value": 55.83218211366604,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordSeq",
            "value": 68.54267502074639,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 536.6209148799999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 530.8300417999999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 556.64997828,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 562.2362449600001,
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
          "id": "5560d61ffb57df174bb315daadadb29913433af9",
          "message": "Update sbt, scripted-plugin to 1.10.10 (#1496)\n\n📦 Updates \n* [org.scala-sbt:sbt](https://github.com/sbt/sbt)\n* [org.scala-sbt:scripted-plugin](https://github.com/sbt/sbt)\n\n from `1.10.9` to `1.10.10`",
          "timestamp": "2025-03-05T11:33:56+01:00",
          "tree_id": "d272b718d4d13ddf49507c6fe8af106f4310335b",
          "url": "https://github.com/zio/zio-kafka/commit/5560d61ffb57df174bb315daadadb29913433af9"
        },
        "date": 1741171904269,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 581.28510608,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 584.3666506400001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 108.57240616622222,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 304.8734448433333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordPar",
            "value": 50.2898544675661,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordSeq",
            "value": 60.018802243985675,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 537.07094898,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 529.8789917199999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 553.7916148200001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 559.66424266,
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
          "id": "fea8b644c3a620fcf20899fd5970db30fd5d09a2",
          "message": "Update scalafmt-core to 3.9.3 (#1497)\n\n📦 Updates\n[org.scalameta:scalafmt-core](https://github.com/scalameta/scalafmt)\nfrom `3.9.2` to `3.9.3`",
          "timestamp": "2025-03-07T09:16:49+01:00",
          "tree_id": "a2fd0f995a3cbd8f83330e9af6147da65883deb1",
          "url": "https://github.com/zio/zio-kafka/commit/fea8b644c3a620fcf20899fd5970db30fd5d09a2"
        },
        "date": 1741336546229,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 586.1773413200001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 588.6068820999999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 165.0418056038095,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 457.19130448666675,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordPar",
            "value": 70.12400602529317,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordSeq",
            "value": 87.04636144497871,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 537.37500576,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 530.40724184,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 560.5724989400001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 563.4218375599999,
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
          "id": "29f168108b17b2a051b91938f874bb985241accc",
          "message": "More tests for rebalanceSafeCommit mode and Small stuff (#1494)\n\nMore tests for rebalanceSafeCommit mode.\n\nAlso:\n- Improve structure of zio-kafka 3 migration docs.\n- Remove redundant integration test. The behavior that should be tested\nis already covered in a unit test in `CommitterSpec`. The removed\nintegration test probably doesn't test the wanted behavior anyway.\n- Use `forkScoped` for endless producing in a unit test instead of a\n`Ref`.\n- Use ConsumerSettings helper method where possible.\n- Small spelling fixes.",
          "timestamp": "2025-03-08T12:33:58+01:00",
          "tree_id": "dcf6d83900e9ec5dba31f61aa518bd6dc1cb3bc6",
          "url": "https://github.com/zio/zio-kafka/commit/29f168108b17b2a051b91938f874bb985241accc"
        },
        "date": 1741434740630,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 583.0307051199999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 583.21996928,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 139.07345989785716,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 389.49556728333323,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordPar",
            "value": 57.37704040456405,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordSeq",
            "value": 70.89145016356392,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 538.0565217799999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 529.16439882,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 554.6829808800001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 561.2274079800002,
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
          "id": "be172a137a46429c190eb2feae642cb5a7799232",
          "message": "Remove flaky test (#1499)\n\nRemove a flaky test, it is not entirely clear what it is testing anyway.",
          "timestamp": "2025-03-08T14:32:12+01:00",
          "tree_id": "26ce73ed55675ed79fecf632184de87f354fcae1",
          "url": "https://github.com/zio/zio-kafka/commit/be172a137a46429c190eb2feae642cb5a7799232"
        },
        "date": 1741441827486,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 583.8021946199999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 587.4383062800001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 144.05216880285715,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 383.19385345666655,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordPar",
            "value": 62.000442143408016,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordSeq",
            "value": 75.85239671990595,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 537.79581586,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 531.5552965400001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 557.8731040800001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 565.2973841200001,
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
          "id": "c40a10f7a2c30b21c28807a8307167bb9b93398b",
          "message": "Update scalafmt-core to 3.9.4 (#1500)\n\n## About this PR\n📦 Updates\n[org.scalameta:scalafmt-core](https://github.com/scalameta/scalafmt)\nfrom `3.9.3` to `3.9.4`\n\n📜 [GitHub Release\nNotes](https://github.com/scalameta/scalafmt/releases/tag/v3.9.4) -\n[Version\nDiff](https://github.com/scalameta/scalafmt/compare/v3.9.3...v3.9.4)\n\n## Usage\n✅ **Please merge!**\n\nI'll automatically update this PR to resolve conflicts as long as you\ndon't change it yourself.\n\nIf you'd like to skip this version, you can just close this PR. If you\nhave any feedback, just mention me in the comments below.\n\nConfigure Scala Steward for your repository with a\n[`.scala-steward.conf`](https://github.com/scala-steward-org/scala-steward/blob/8929fadb6f535472bd63397b1cb55a835fa1b5cb/docs/repo-specific-configuration.md)\nfile.\n\n_Have a fantastic day writing Scala!_\n\n<details>\n<summary>⚙ Adjust future updates</summary>\n\nAdd this to your `.scala-steward.conf` file to ignore future updates of\nthis dependency:\n```\nupdates.ignore = [ { groupId = \"org.scalameta\", artifactId = \"scalafmt-core\" } ]\n```\nOr, add this to slow down future updates of this dependency:\n```\ndependencyOverrides = [{\n  pullRequests = { frequency = \"30 days\" },\n  dependency = { groupId = \"org.scalameta\", artifactId = \"scalafmt-core\" }\n}]\n```\n</details>\n\n<sup>\nlabels: library-update, early-semver-patch, semver-spec-patch,\ncommit-count:1\n</sup>\n\n<!-- scala-steward = {\n  \"Update\" : {\n    \"ForArtifactId\" : {\n      \"crossDependency\" : [\n        {\n          \"groupId\" : \"org.scalameta\",\n          \"artifactId\" : {\n            \"name\" : \"scalafmt-core\",\n            \"maybeCrossName\" : \"scalafmt-core_2.13\"\n          },\n          \"version\" : \"3.9.3\",\n          \"sbtVersion\" : null,\n          \"scalaVersion\" : null,\n          \"configurations\" : null\n        }\n      ],\n      \"newerVersions\" : [\n        \"3.9.4\"\n      ],\n      \"newerGroupId\" : null,\n      \"newerArtifactId\" : null\n    }\n  },\n  \"Labels\" : [\n    \"library-update\",\n    \"early-semver-patch\",\n    \"semver-spec-patch\",\n    \"commit-count:1\"\n  ]\n} -->\n\nCo-authored-by: zio-scala-steward[bot] <145262613+zio-scala-steward[bot]@users.noreply.github.com>",
          "timestamp": "2025-03-13T13:25:05+01:00",
          "tree_id": "ab1ae946909b2e1caa85b28c17cb1855e02c8080",
          "url": "https://github.com/zio/zio-kafka/commit/c40a10f7a2c30b21c28807a8307167bb9b93398b"
        },
        "date": 1741869839384,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 590.19844546,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 591.57337732,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 192.54513432580953,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 427.9914622333333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordPar",
            "value": 74.31090840227483,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordSeq",
            "value": 87.09952101073115,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 538.34633056,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 531.26498012,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 556.1070469399999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 561.37932082,
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
          "id": "675044d3eb6c05881fdbf65349179cd683960555",
          "message": "Fix rebalance coordinator spec (#1485)\n\nPreviously, some of the rebalanceSafeCommits tests passed regardless of\nwhether rebalanceSafeCommits was enabled or not. This is now fixed.\n\nAlso:\n- added test outlines\n- made assertions more precise\n- removed unnecessary code\n- add a test for when rebalanceSafeCommits is disabled",
          "timestamp": "2025-03-16T14:23:56+01:00",
          "tree_id": "caf15bdae787d19be112fb85ceaaae34230f57e1",
          "url": "https://github.com/zio/zio-kafka/commit/675044d3eb6c05881fdbf65349179cd683960555"
        },
        "date": 1742132552292,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 585.2801745799999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 585.8036083200001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 168.5655812847619,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 426.5164173000001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordPar",
            "value": 66.71352172957982,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordSeq",
            "value": 81.17731624134922,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 537.7962678199999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 530.40181098,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 556.2934041800002,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 563.7952067800002,
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
          "id": "9f4b1fe4e6defefd1ac3cf80c0db333612d06091",
          "message": "Update sbt, scripted-plugin to 1.10.11 (#1502)\n\n## About this PR\n📦 Updates \n* [org.scala-sbt:sbt](https://github.com/sbt/sbt)\n* [org.scala-sbt:scripted-plugin](https://github.com/sbt/sbt)\n\n from `1.10.10` to `1.10.11`\n\n📜 [GitHub Release\nNotes](https://github.com/sbt/sbt/releases/tag/v1.10.11) - [Version\nDiff](https://github.com/sbt/sbt/compare/v1.10.10...v1.10.11)\n\n## Usage\n✅ **Please merge!**\n\nI'll automatically update this PR to resolve conflicts as long as you\ndon't change it yourself.\n\nIf you'd like to skip this version, you can just close this PR. If you\nhave any feedback, just mention me in the comments below.\n\nConfigure Scala Steward for your repository with a\n[`.scala-steward.conf`](https://github.com/scala-steward-org/scala-steward/blob/f0dfaa6d8e24b261aeafbc7f99c5325ed9365cf2/docs/repo-specific-configuration.md)\nfile.\n\n_Have a fantastic day writing Scala!_\n\n<details>\n<summary>⚙ Adjust future updates</summary>\n\nAdd this to your `.scala-steward.conf` file to ignore future updates of\nthis dependency:\n```\nupdates.ignore = [ { groupId = \"org.scala-sbt\" } ]\n```\nOr, add this to slow down future updates of this dependency:\n```\ndependencyOverrides = [{\n  pullRequests = { frequency = \"30 days\" },\n  dependency = { groupId = \"org.scala-sbt\" }\n}]\n```\n</details>\n\n<sup>\nlabels: library-update, early-semver-patch, semver-spec-patch,\nversion-scheme:early-semver, commit-count:1\n</sup>\n\n<!-- scala-steward = {\n  \"Update\" : {\n    \"ForGroupId\" : {\n      \"forArtifactIds\" : [\n        {\n          \"ForArtifactId\" : {\n            \"crossDependency\" : [\n              {\n                \"groupId\" : \"org.scala-sbt\",\n                \"artifactId\" : {\n                  \"name\" : \"sbt\",\n                  \"maybeCrossName\" : null\n                },\n                \"version\" : \"1.10.10\",\n                \"sbtVersion\" : null,\n                \"scalaVersion\" : null,\n                \"configurations\" : null\n              }\n            ],\n            \"newerVersions\" : [\n              \"1.10.11\"\n            ],\n            \"newerGroupId\" : null,\n            \"newerArtifactId\" : null\n          }\n        },\n        {\n          \"ForArtifactId\" : {\n            \"crossDependency\" : [\n              {\n                \"groupId\" : \"org.scala-sbt\",\n                \"artifactId\" : {\n                  \"name\" : \"scripted-plugin\",\n                  \"maybeCrossName\" : \"scripted-plugin_2.12\"\n                },\n                \"version\" : \"1.10.10\",\n                \"sbtVersion\" : null,\n                \"scalaVersion\" : null,\n                \"configurations\" : null\n              }\n            ],\n            \"newerVersions\" : [\n              \"1.10.11\"\n            ],\n            \"newerGroupId\" : null,\n            \"newerArtifactId\" : null\n          }\n        }\n      ]\n    }\n  },\n  \"Labels\" : [\n    \"library-update\",\n    \"early-semver-patch\",\n    \"semver-spec-patch\",\n    \"version-scheme:early-semver\",\n    \"commit-count:1\"\n  ]\n} -->\n\nCo-authored-by: zio-scala-steward[bot] <145262613+zio-scala-steward[bot]@users.noreply.github.com>",
          "timestamp": "2025-03-18T08:42:19+01:00",
          "tree_id": "981d7238c000c94395f275918eeb626214008339",
          "url": "https://github.com/zio/zio-kafka/commit/9f4b1fe4e6defefd1ac3cf80c0db333612d06091"
        },
        "date": 1742284849738,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 584.8855660599999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 584.64243518,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 148.8442552147619,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 419.81448848,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordPar",
            "value": 63.92534544022607,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordSeq",
            "value": 82.92504309398063,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 537.7515152,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 530.7198705599999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 557.1579728,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 567.6660710800002,
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
          "id": "9d95f67cfa8f7187014b5f9ad6aa101fed3ef16e",
          "message": "Update logback-classic to 1.5.18 (#1503)\n\n## About this PR\n📦 Updates\n[ch.qos.logback:logback-classic](https://github.com/qos-ch/logback) from\n`1.5.17` to `1.5.18`\n\n## Usage\n✅ **Please merge!**\n\nI'll automatically update this PR to resolve conflicts as long as you\ndon't change it yourself.\n\nIf you'd like to skip this version, you can just close this PR. If you\nhave any feedback, just mention me in the comments below.\n\nConfigure Scala Steward for your repository with a\n[`.scala-steward.conf`](https://github.com/scala-steward-org/scala-steward/blob/f0dfaa6d8e24b261aeafbc7f99c5325ed9365cf2/docs/repo-specific-configuration.md)\nfile.\n\n_Have a fantastic day writing Scala!_\n\n<details>\n<summary>⚙ Adjust future updates</summary>\n\nAdd this to your `.scala-steward.conf` file to ignore future updates of\nthis dependency:\n```\nupdates.ignore = [ { groupId = \"ch.qos.logback\", artifactId = \"logback-classic\" } ]\n```\nOr, add this to slow down future updates of this dependency:\n```\ndependencyOverrides = [{\n  pullRequests = { frequency = \"30 days\" },\n  dependency = { groupId = \"ch.qos.logback\", artifactId = \"logback-classic\" }\n}]\n```\n</details>\n\n<sup>\nlabels: library-update, early-semver-patch, semver-spec-patch,\ncommit-count:1\n</sup>\n\n<!-- scala-steward = {\n  \"Update\" : {\n    \"ForArtifactId\" : {\n      \"crossDependency\" : [\n        {\n          \"groupId\" : \"ch.qos.logback\",\n          \"artifactId\" : {\n            \"name\" : \"logback-classic\",\n            \"maybeCrossName\" : null\n          },\n          \"version\" : \"1.5.17\",\n          \"sbtVersion\" : null,\n          \"scalaVersion\" : null,\n          \"configurations\" : null\n        },\n        {\n          \"groupId\" : \"ch.qos.logback\",\n          \"artifactId\" : {\n            \"name\" : \"logback-classic\",\n            \"maybeCrossName\" : null\n          },\n          \"version\" : \"1.5.17\",\n          \"sbtVersion\" : null,\n          \"scalaVersion\" : null,\n          \"configurations\" : \"test\"\n        }\n      ],\n      \"newerVersions\" : [\n        \"1.5.18\"\n      ],\n      \"newerGroupId\" : null,\n      \"newerArtifactId\" : null\n    }\n  },\n  \"Labels\" : [\n    \"library-update\",\n    \"early-semver-patch\",\n    \"semver-spec-patch\",\n    \"commit-count:1\"\n  ]\n} -->\n\nCo-authored-by: zio-scala-steward[bot] <145262613+zio-scala-steward[bot]@users.noreply.github.com>",
          "timestamp": "2025-03-19T07:24:45+01:00",
          "tree_id": "2080d2187e98e7b62d8ab3b1468abe46a99fe9d6",
          "url": "https://github.com/zio/zio-kafka/commit/9d95f67cfa8f7187014b5f9ad6aa101fed3ef16e"
        },
        "date": 1742366624065,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 583.8443953799999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 586.55424738,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 261.2823888286667,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 510.11787094666676,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordPar",
            "value": 93.05433777102796,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordSeq",
            "value": 109.27758859319523,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 539.3619994000001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 530.93957764,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 557.13199852,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 562.49770174,
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
          "id": "2e39bead833070f793f88d3ff016a4dafea0f701",
          "message": "Improve documentation about chunk-breakers (#1506)",
          "timestamp": "2025-03-21T09:31:33+01:00",
          "tree_id": "cd1d1a49c75e31d3953a010f26e356e26cdf0bfa",
          "url": "https://github.com/zio/zio-kafka/commit/2e39bead833070f793f88d3ff016a4dafea0f701"
        },
        "date": 1742546993557,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 585.0122007800001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 586.4830808,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 117.55163653333331,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 327.2049212560001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordPar",
            "value": 52.97212626748108,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordSeq",
            "value": 65.1222836211526,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 538.19253568,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 529.9811642200001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 555.6984739999998,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 563.55819896,
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
          "id": "735c752ff793d342a06f3042beebb432d963191a",
          "message": "Improve documentation about duration of rebalances (#1505)\n\nThe documentation now documents that commit-time also affects rebalances\nwhen rebalanceSafeCommits is enabled.",
          "timestamp": "2025-03-21T20:13:38+01:00",
          "tree_id": "e092eeca3e313dedfe678aefa574ac0807752e1b",
          "url": "https://github.com/zio/zio-kafka/commit/735c752ff793d342a06f3042beebb432d963191a"
        },
        "date": 1742585507256,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 586.36057776,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 587.89935602,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 112.20031383955553,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 317.88451201333334,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordPar",
            "value": 50.704875636523425,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordSeq",
            "value": 62.87884554641042,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 538.4771502200002,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 531.1395340800001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 559.2179630599999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 565.3577909600001,
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
          "id": "5304babe6e27e2b429133210cfebb0918567c7ec",
          "message": "Fix documentation typo (#1508)",
          "timestamp": "2025-03-23T11:49:10+01:00",
          "tree_id": "1bf9d9a330e82c5c5b8906b3ee0a013d81607346",
          "url": "https://github.com/zio/zio-kafka/commit/5304babe6e27e2b429133210cfebb0918567c7ec"
        },
        "date": 1742728077211,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 583.8060325399998,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 584.93714526,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 133.96809115396826,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 365.62778940000004,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordPar",
            "value": 56.98528915130427,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordSeq",
            "value": 70.09249610347226,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 537.0970418,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 530.88188642,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 554.9133997399999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 564.1802576,
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
          "id": "c32ff85643fe018646d5cedf2ed9c8f04d60d059",
          "message": "Preparing for producer diagnostics (3.x) (#1452)\n\nIt has been a long wish to have producer diagnostics. Although these are\nnot really hard to add, the consumer diagnostics are a bit in the way;\nthese already claim some names and make code sharing awkward. Now that\nwe are moving towards zio-kafka 3.0 we can fix these at the cost of some\nbackward incompatibilities.\n\nThis commit has the same changes as commit\n1779b27bb06e2ffd5693b6f9674b5616e5f63b32 in the series/2.x branch, but\nthen with all deprecated code removed.",
          "timestamp": "2025-03-23T12:19:08+01:00",
          "tree_id": "eceffcf7be66fa22053259bb45c01905a6476eb8",
          "url": "https://github.com/zio/zio-kafka/commit/c32ff85643fe018646d5cedf2ed9c8f04d60d059"
        },
        "date": 1742729912336,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 588.02301712,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 591.1350519399999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 238.68670714876188,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 561.24648872,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordPar",
            "value": 87.58980865265734,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordSeq",
            "value": 101.88081411925134,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 539.02320946,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 531.0783158200001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 558.3762620000001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 564.8498249400001,
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
          "id": "c4b3d4fa911d2e0648c953502546d3c5ccc5b689",
          "message": "Update zio, zio-streams, zio-test, ... to 2.1.17 (#1517)\n\n📦 Updates\n* [dev.zio:zio](https://github.com/zio/zio)\n* [dev.zio:zio-streams](https://github.com/zio/zio)\n* [dev.zio:zio-test](https://github.com/zio/zio)\n* [dev.zio:zio-test-sbt](https://github.com/zio/zio)\n\nfrom `2.1.16` to `2.1.17`",
          "timestamp": "2025-04-05T10:59:22+02:00",
          "tree_id": "841ae44167fc142c9b3865fbbbd26cf477d73acd",
          "url": "https://github.com/zio/zio-kafka/commit/c4b3d4fa911d2e0648c953502546d3c5ccc5b689"
        },
        "date": 1743844665379,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 583.4939248000001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 584.4954846600001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 166.102856192381,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 426.7864898066666,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordPar",
            "value": 65.41059595423677,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordSeq",
            "value": 83.15141568690964,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 537.87366602,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 530.2653731,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 557.1719497400001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 565.32273028,
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
            "name": "zio",
            "username": "zio"
          },
          "committer": {
            "name": "zio",
            "username": "zio"
          },
          "id": "208326cbac873cc6656569b03a151cb734cdd9ff",
          "message": "Improve type inference for `runWithGracefulShutdown`",
          "timestamp": "2025-05-25T07:31:54Z",
          "url": "https://github.com/zio/zio-kafka/pull/1536/commits/208326cbac873cc6656569b03a151cb734cdd9ff"
        },
        "date": 1748161904231,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 595.08124788,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 595.3601006600001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 112.06387178274748,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 343.36831317,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeqAsync",
            "value": 155.40717079894867,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeqAsync",
            "value": 0.3781480538041043,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordPar",
            "value": 86.79237791888114,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordSeq",
            "value": 112.43814728612122,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 549.0710953999999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 536.7888900199999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 562.36204428,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 574.27726804,
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
            "name": "zio",
            "username": "zio"
          },
          "committer": {
            "name": "zio",
            "username": "zio"
          },
          "id": "3871301338ba4235c5389ed894b2338d404b420b",
          "message": "Update zio, zio-streams, zio-test, ... to 2.1.19",
          "timestamp": "2025-05-27T19:36:53Z",
          "url": "https://github.com/zio/zio-kafka/pull/1539/commits/3871301338ba4235c5389ed894b2338d404b420b"
        },
        "date": 1748657462721,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 592.4995052799999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 594.6096668,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 81.68821330179486,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 257.57984127,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeqAsync",
            "value": 138.7695171339961,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeqAsync",
            "value": 0.3763723651772661,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordPar",
            "value": 51.29127406092663,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordSeq",
            "value": 70.3705059835403,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 546.3572931599999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 536.9313369600001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 561.81150574,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 573.8121065600001,
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
          "id": "14de3b56dade0cf9ea0ca68533e378ea1a44fa90",
          "message": "Update embedded-kafka to 4.0.1.1",
          "timestamp": "2025-05-27T19:36:53Z",
          "url": "https://github.com/zio/zio-kafka/pull/1540/commits/14de3b56dade0cf9ea0ca68533e378ea1a44fa90"
        },
        "date": 1748657518747,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 592.7752712,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 596.5370221000001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 90.5976297557576,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 283.04812085000003,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeqAsync",
            "value": 36.42751499622483,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeqAsync",
            "value": 0.3774691183565791,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordPar",
            "value": 61.305146921614835,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordSeq",
            "value": 83.22991972072329,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 546.9043866000002,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 537.2089013400001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 562.9245209599999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 573.89021182,
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
            "name": "zio",
            "username": "zio"
          },
          "committer": {
            "name": "zio",
            "username": "zio"
          },
          "id": "c4e4456095f7f2583edd20b83d250e89892b8aa5",
          "message": "Update sbt, scripted-plugin to 1.11.1",
          "timestamp": "2025-05-31T18:27:51Z",
          "url": "https://github.com/zio/zio-kafka/pull/1541/commits/c4e4456095f7f2583edd20b83d250e89892b8aa5"
        },
        "date": 1748917001447,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 591.37936266,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 591.5957867400002,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 117.35109130777779,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 324.16740859666663,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeqAsync",
            "value": 91.75492175231412,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeqAsync",
            "value": 0.38322137523567357,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordPar",
            "value": 66.5176922860675,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordSeq",
            "value": 94.12855125260609,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 547.8354264799999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 535.09489254,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 560.5679204,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 573.3883660800001,
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
            "name": "zio",
            "username": "zio"
          },
          "committer": {
            "name": "zio",
            "username": "zio"
          },
          "id": "1efdd18ee50fe3db28f6eff6bd2356b4d0dc4c81",
          "message": "Add implicit trace parameter",
          "timestamp": "2025-06-03T06:52:28Z",
          "url": "https://github.com/zio/zio-kafka/pull/1543/commits/1efdd18ee50fe3db28f6eff6bd2356b4d0dc4c81"
        },
        "date": 1748978305401,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 591.7774247599999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 592.22359396,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 107.52664039999999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 335.37471265333335,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeqAsync",
            "value": 30.365716507932586,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeqAsync",
            "value": 0.39528044168917115,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordPar",
            "value": 85.52669260934799,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordSeq",
            "value": 107.16784038973739,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 545.1215739,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 534.119377,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 559.36660718,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 571.9657474800001,
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
          "id": "106bfff349a06ca09f7b99d579d92cc5b68ebf78",
          "message": "Update zio-sbt-ci, zio-sbt-ecosystem, ... to 0.4.0-alpha.32",
          "timestamp": "2025-06-03T06:52:28Z",
          "url": "https://github.com/zio/zio-kafka/pull/1544/commits/106bfff349a06ca09f7b99d579d92cc5b68ebf78"
        },
        "date": 1749003549779,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 592.95685246,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 594.9954592600001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 109.83016528191922,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 331.22355395000005,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeqAsync",
            "value": 54.23615238291051,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeqAsync",
            "value": 0.41777559108087586,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordPar",
            "value": 80.15198132573312,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordSeq",
            "value": 100.53360196880807,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 549.5899551599999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 538.3963481199999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 566.72943136,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 577.8047404599998,
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
            "name": "zio",
            "username": "zio"
          },
          "committer": {
            "name": "zio",
            "username": "zio"
          },
          "id": "91229d9988cba98070056351634429615aaa0b85",
          "message": "Add implicit trace parameter",
          "timestamp": "2025-06-04T07:37:24Z",
          "url": "https://github.com/zio/zio-kafka/pull/1543/commits/91229d9988cba98070056351634429615aaa0b85"
        },
        "date": 1749033924078,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 599.0171895,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 599.9369303,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 106.56952842472728,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 344.8084374733334,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeqAsync",
            "value": 32.61622054885754,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeqAsync",
            "value": 0.37950071545287467,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordPar",
            "value": 80.92315896111724,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordSeq",
            "value": 100.83561936363638,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 548.2931563000002,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 539.09223462,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 564.1444749,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 577.8101117600002,
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
          "id": "b74b0ae09017a02d5cac396aa20a23c469177c4f",
          "message": "Add implicit trace parameter",
          "timestamp": "2025-06-04T07:37:24Z",
          "url": "https://github.com/zio/zio-kafka/pull/1543/commits/b74b0ae09017a02d5cac396aa20a23c469177c4f"
        },
        "date": 1749036196790,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 597.5206728400001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 600.5380269600001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 105.81969104452524,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 337.32648470999993,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeqAsync",
            "value": 38.348902588675536,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeqAsync",
            "value": 0.4138359732412853,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordPar",
            "value": 100.00688416691843,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordSeq",
            "value": 116.82229988675758,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 549.7810547600001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 538.86676176,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 563.2556988800002,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 574.1391578200002,
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
          "id": "66032827f49bba00fe1729585c5ce594b2832291",
          "message": "Await acknowledgements in async benchmarks",
          "timestamp": "2025-06-06T16:08:26Z",
          "url": "https://github.com/zio/zio-kafka/pull/1545/commits/66032827f49bba00fe1729585c5ce594b2832291"
        },
        "date": 1749320372559,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 595.08807572,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 596.84719204,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 97.20455615236364,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 315.01927842000003,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeqAsync",
            "value": 61.05880516638448,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeqAsync",
            "value": 0.4150216455106623,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordPar",
            "value": 77.24006906915552,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordSeq",
            "value": 96.76893289020046,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 548.5435971599999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 538.1713943600001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 564.05229806,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 575.8116767800001,
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
          "id": "f6479a09f14fae4dcbf897bda6fd446f10dde593",
          "message": "Simplify code, use uniform type names",
          "timestamp": "2025-06-06T16:08:26Z",
          "url": "https://github.com/zio/zio-kafka/pull/1546/commits/f6479a09f14fae4dcbf897bda6fd446f10dde593"
        },
        "date": 1749322196025,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 592.9249400200001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 594.77624662,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 98.63198361309088,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 331.5606241833333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeqAsync",
            "value": 32.85432127494809,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeqAsync",
            "value": 0.37740257080558487,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordPar",
            "value": 74.61010769597253,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordSeq",
            "value": 102.09329703862622,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 545.7594146,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 536.66914374,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 560.2833515000001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 575.617394,
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
          "id": "66032827f49bba00fe1729585c5ce594b2832291",
          "message": "Await acknowledgements in async benchmarks",
          "timestamp": "2025-06-06T16:08:26Z",
          "url": "https://github.com/zio/zio-kafka/pull/1545/commits/66032827f49bba00fe1729585c5ce594b2832291"
        },
        "date": 1749322302822,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 592.71577814,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 593.45753334,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 107.68588160987879,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 349.17319867,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeqAsync",
            "value": 33.25306571363498,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeqAsync",
            "value": 0.41809953575725894,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordPar",
            "value": 93.14488160124009,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordSeq",
            "value": 114.21574557753536,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 546.75996094,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 534.9547254599998,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 563.2114207,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 574.0936567800001,
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
          "id": "9a3e4867b5e1fca6d6aaf1d24c68a08facfe2aa8",
          "message": "Update sbt, scripted-plugin to 1.11.2",
          "timestamp": "2025-06-06T16:08:26Z",
          "url": "https://github.com/zio/zio-kafka/pull/1547/commits/9a3e4867b5e1fca6d6aaf1d24c68a08facfe2aa8"
        },
        "date": 1749349508422,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 594.0489145600001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 594.4866988800001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 96.9823223921616,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 291.64676243666673,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeqAsync",
            "value": 27.037670979373118,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeqAsync",
            "value": 0.38515798767327253,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordPar",
            "value": 77.01522903989141,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordSeq",
            "value": 94.84642544025638,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 547.9310987999999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 539.8091062999999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 562.1849464999999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 576.8122887200001,
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
            "name": "zio",
            "username": "zio"
          },
          "committer": {
            "name": "zio",
            "username": "zio"
          },
          "id": "a8d1552b9eef197cfbfcf0d0a04cc9e912659563",
          "message": "Await acknowledgements in async benchmarks",
          "timestamp": "2025-06-08T06:54:09Z",
          "url": "https://github.com/zio/zio-kafka/pull/1545/commits/a8d1552b9eef197cfbfcf0d0a04cc9e912659563"
        },
        "date": 1749368757104,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 595.49856752,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 597.08197064,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 104.47076951963635,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 317.42566224666666,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeqAsync",
            "value": 31.318586470349036,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeqAsync",
            "value": 6.998055810172749,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordPar",
            "value": 83.84228673530203,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordSeq",
            "value": 102.86073618777776,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 547.2869829800001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 536.6514882600001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 562.5534997000001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 574.62955978,
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
          "id": "db3760ddad0953c61bf2fd0cd4dd7e041e99a632",
          "message": "Simplify code, use uniform type names",
          "timestamp": "2025-06-08T06:54:09Z",
          "url": "https://github.com/zio/zio-kafka/pull/1546/commits/db3760ddad0953c61bf2fd0cd4dd7e041e99a632"
        },
        "date": 1749369697022,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 595.05375628,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 596.0588349599999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 112.3009018757071,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 324.57193901333335,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeqAsync",
            "value": 34.12071851883855,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeqAsync",
            "value": 0.3866494786967836,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordPar",
            "value": 90.30474064780884,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordSeq",
            "value": 105.4189393059596,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 547.47669394,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 536.8255726399999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 562.5376234599998,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 575.99689654,
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
          "id": "a8d1552b9eef197cfbfcf0d0a04cc9e912659563",
          "message": "Await acknowledgements in async benchmarks",
          "timestamp": "2025-06-08T06:54:09Z",
          "url": "https://github.com/zio/zio-kafka/pull/1545/commits/a8d1552b9eef197cfbfcf0d0a04cc9e912659563"
        },
        "date": 1749370838211,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 596.75946592,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 597.3266735000001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 100.92486852400002,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 309.98186165333334,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeqAsync",
            "value": 31.20386125447628,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeqAsync",
            "value": 6.98239262271721,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordPar",
            "value": 76.45523083499998,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordSeq",
            "value": 102.339395353899,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 548.24347972,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 537.09669874,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 561.4420207799999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 575.20126712,
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
      }
    ]
  }
}