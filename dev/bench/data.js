window.BENCHMARK_DATA = {
  "lastUpdate": 1757052553810,
  "repoUrl": "https://github.com/zio/zio-kafka",
  "entries": {
    "JMH Benchmark": [
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
            "name": "zio",
            "username": "zio"
          },
          "committer": {
            "name": "zio",
            "username": "zio"
          },
          "id": "0e8fdd1c94a61e0d47bed8d540258c33a9c8fec2",
          "message": "Update sbt-native-packager to 1.11.3",
          "timestamp": "2025-08-22T09:04:29Z",
          "url": "https://github.com/zio/zio-kafka/pull/1560/commits/0e8fdd1c94a61e0d47bed8d540258c33a9c8fec2"
        },
        "date": 1756001991458,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 596.84398698,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 597.64520058,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 100.65214149802021,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 306.72855908,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeqAsync",
            "value": 28.170102242661848,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeqAsync",
            "value": 7.090568601194892,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordPar",
            "value": 67.04297524339208,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordSeq",
            "value": 90.3048925587506,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 549.5491939,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 539.59278026,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 565.31458816,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 577.5227652399999,
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
          "id": "61503941d7fbf017cabb2d830127a9e385b9102d",
          "message": "Update sbt, scripted-plugin to 1.11.5",
          "timestamp": "2025-08-22T09:04:29Z",
          "url": "https://github.com/zio/zio-kafka/pull/1561/commits/61503941d7fbf017cabb2d830127a9e385b9102d"
        },
        "date": 1756088101695,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 594.2906720400001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 594.44647582,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 83.99559963897437,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 265.51205959,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeqAsync",
            "value": 23.021251426478845,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeqAsync",
            "value": 6.684227700410774,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordPar",
            "value": 55.77604819102648,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordSeq",
            "value": 74.2929160861392,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 548.4358741400001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 536.51752286,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 563.5890278200001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 576.2789527,
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
            "name": "zio",
            "username": "zio"
          },
          "committer": {
            "name": "zio",
            "username": "zio"
          },
          "id": "97af5e2e5ce28b2d5212b4dec9d205f1cfea1d80",
          "message": "Update zio-sbt-ci, zio-sbt-ecosystem, ... to 0.4.0-alpha.34",
          "timestamp": "2025-08-25T15:46:54Z",
          "url": "https://github.com/zio/zio-kafka/pull/1563/commits/97af5e2e5ce28b2d5212b4dec9d205f1cfea1d80"
        },
        "date": 1756260458295,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 595.8578182599999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 598.9926054199999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 101.47417404977776,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 317.8571568366666,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeqAsync",
            "value": 30.56216414107228,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeqAsync",
            "value": 6.98616939816707,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordPar",
            "value": 78.22743701737642,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordSeq",
            "value": 104.78221022670705,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 549.4768659199998,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 536.46168698,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 564.68625764,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 577.140515,
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
          "id": "b756978473d36f77ac005eaf92179bf576835763",
          "message": "Update zio-sbt-ci, zio-sbt-ecosystem, ... to 0.4.0-alpha.34",
          "timestamp": "2025-08-25T15:46:54Z",
          "url": "https://github.com/zio/zio-kafka/pull/1563/commits/b756978473d36f77ac005eaf92179bf576835763"
        },
        "date": 1756553284336,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 591.55959522,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 594.27068378,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 91.55565225515153,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 289.75401385000004,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeqAsync",
            "value": 27.26815198622281,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeqAsync",
            "value": 6.783880163707323,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordPar",
            "value": 64.77050740000034,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordSeq",
            "value": 86.21716459214919,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 546.58096676,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 535.47593488,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 562.33220742,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 573.85797154,
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
          "id": "a9847ce2a95cd8ac3a0b5f911d2ec994fcc51efc",
          "message": "Update embedded-kafka to 4.1.0",
          "timestamp": "2025-09-01T02:07:44Z",
          "url": "https://github.com/zio/zio-kafka/pull/1566/commits/a9847ce2a95cd8ac3a0b5f911d2ec994fcc51efc"
        },
        "date": 1757037786439,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 590.37052504,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 594.32314676,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 80.62051174743588,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 256.631413198,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeqAsync",
            "value": 21.175415530719093,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeqAsync",
            "value": 6.6793174147328305,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordPar",
            "value": 52.94225377844846,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordSeq",
            "value": 72.91088483698168,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 545.79530888,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 535.22706422,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 561.92889594,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 574.9625366199999,
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
          "id": "1b8204b14937b861e6b21fec8256dc85efd2765c",
          "message": "Update kafka-clients to 4.1.0",
          "timestamp": "2025-09-01T02:07:44Z",
          "url": "https://github.com/zio/zio-kafka/pull/1564/commits/1b8204b14937b861e6b21fec8256dc85efd2765c"
        },
        "date": 1757052553278,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 587.7489940800002,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 588.8537965799999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 94.07940606519347,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 288.01046611999993,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeqAsync",
            "value": 27.135815731992874,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeqAsync",
            "value": 6.810268325576692,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordPar",
            "value": 65.14089274800106,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaSeqProducerBenchmark.produceSingleRecordSeq",
            "value": 84.7971510862804,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 545.30405088,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 536.8492948400002,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 564.96662746,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 573.78039032,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          }
        ]
      }
    ]
  }
}