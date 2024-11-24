window.BENCHMARK_DATA = {
  "lastUpdate": 1732464585010,
  "repoUrl": "https://github.com/zio/zio-kafka",
  "entries": {
    "JMH Benchmark": [
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
          "id": "4ca53ad3c3bee24d21ede7e8c74e607a7f614df7",
          "message": "Update zio-sbt-ci, zio-sbt-ecosystem, ... to 0.4.0-alpha.27 (#1247)\n\n## About this PR\r\n📦 Updates \r\n* [dev.zio:zio-sbt-ci](https://github.com/zio/zio-sbt)\r\n* [dev.zio:zio-sbt-ecosystem](https://github.com/zio/zio-sbt)\r\n* [dev.zio:zio-sbt-website](https://github.com/zio/zio-sbt)\r\n\r\n from `0.4.0-alpha.26` to `0.4.0-alpha.27`\r\n\r\n📜 [GitHub Release\r\nNotes](https://github.com/zio/zio-sbt/releases/tag/v0.4.0-alpha.27) -\r\n[Version\r\nDiff](https://github.com/zio/zio-sbt/compare/v0.4.0-alpha.26...v0.4.0-alpha.27)\r\n\r\n## Usage\r\n✅ **Please merge!**\r\n\r\nI'll automatically update this PR to resolve conflicts as long as you\r\ndon't change it yourself.\r\n\r\nIf you'd like to skip this version, you can just close this PR. If you\r\nhave any feedback, just mention me in the comments below.\r\n\r\nConfigure Scala Steward for your repository with a\r\n[`.scala-steward.conf`](https://github.com/scala-steward-org/scala-steward/blob/767fcfecbfd53c507152f6cf15c846176bae561d/docs/repo-specific-configuration.md)\r\nfile.\r\n\r\n_Have a fantastic day writing Scala!_\r\n\r\n<details>\r\n<summary>⚙ Adjust future updates</summary>\r\n\r\nAdd this to your `.scala-steward.conf` file to ignore future updates of\r\nthis dependency:\r\n```\r\nupdates.ignore = [ { groupId = \"dev.zio\" } ]\r\n```\r\nOr, add this to slow down future updates of this dependency:\r\n```\r\ndependencyOverrides = [{\r\n  pullRequests = { frequency = \"30 days\" },\r\n  dependency = { groupId = \"dev.zio\" }\r\n}]\r\n```\r\n</details>\r\n\r\n<sup>\r\nlabels: sbt-plugin-update, early-semver-pre-release,\r\nsemver-spec-pre-release, commit-count:1\r\n</sup>\r\n\r\nCo-authored-by: zio-scala-steward[bot] <145262613+zio-scala-steward[bot]@users.noreply.github.com>",
          "timestamp": "2024-05-29T11:32:41+08:00",
          "tree_id": "e06f732eb77798d176b22113e2d08d9b0e827018",
          "url": "https://github.com/zio/zio-kafka/commit/4ca53ad3c3bee24d21ede7e8c74e607a7f614df7"
        },
        "date": 1716954330575,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 600.3058877648417,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 589.4947971996648,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 544.1588351309616,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 530.5826194076923,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 490.7881606148108,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 509.46152969484075,
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
          "id": "3748125c449349953151d320a5df7c03c2c0ef05",
          "message": "Update zio-logging-slf4j, ... to 2.3.0 (#1246)\n\n## About this PR\r\n📦 Updates \r\n* [dev.zio:zio-logging-slf4j](https://github.com/zio/zio-logging)\r\n* [dev.zio:zio-logging-slf4j2](https://github.com/zio/zio-logging)\r\n\r\n from `2.2.4` to `2.3.0`\r\n\r\n📜 [GitHub Release\r\nNotes](https://github.com/zio/zio-logging/releases/tag/v2.3.0) -\r\n[Version\r\nDiff](https://github.com/zio/zio-logging/compare/v2.2.4...v2.3.0)\r\n\r\n## Usage\r\n✅ **Please merge!**\r\n\r\nI'll automatically update this PR to resolve conflicts as long as you\r\ndon't change it yourself.\r\n\r\nIf you'd like to skip this version, you can just close this PR. If you\r\nhave any feedback, just mention me in the comments below.\r\n\r\nConfigure Scala Steward for your repository with a\r\n[`.scala-steward.conf`](https://github.com/scala-steward-org/scala-steward/blob/767fcfecbfd53c507152f6cf15c846176bae561d/docs/repo-specific-configuration.md)\r\nfile.\r\n\r\n_Have a fantastic day writing Scala!_\r\n\r\n<details>\r\n<summary>⚙ Adjust future updates</summary>\r\n\r\nAdd this to your `.scala-steward.conf` file to ignore future updates of\r\nthis dependency:\r\n```\r\nupdates.ignore = [ { groupId = \"dev.zio\" } ]\r\n```\r\nOr, add this to slow down future updates of this dependency:\r\n```\r\ndependencyOverrides = [{\r\n  pullRequests = { frequency = \"30 days\" },\r\n  dependency = { groupId = \"dev.zio\" }\r\n}]\r\n```\r\n</details>\r\n\r\n<sup>\r\nlabels: library-update, early-semver-minor, semver-spec-minor,\r\ncommit-count:1\r\n</sup>\r\n\r\nCo-authored-by: zio-scala-steward[bot] <145262613+zio-scala-steward[bot]@users.noreply.github.com>",
          "timestamp": "2024-05-29T11:37:44+08:00",
          "tree_id": "6d6e871ff74c13411729378eb532bcd94b51a901",
          "url": "https://github.com/zio/zio-kafka/commit/3748125c449349953151d320a5df7c03c2c0ef05"
        },
        "date": 1716954768391,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 605.9623766902049,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 593.8541930309498,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 546.5096604681731,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 534.7251220413463,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 536.7759411668109,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 576.2100350802197,
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
          "id": "dce411a187c5ecbf1e120d3cc21300cbab34215e",
          "message": "Update zio, zio-streams, zio-test, ... to 2.1.2 (#1248)\n\n## About this PR\r\n📦 Updates \r\n* [dev.zio:zio](https://github.com/zio/zio)\r\n* [dev.zio:zio-streams](https://github.com/zio/zio)\r\n* [dev.zio:zio-test](https://github.com/zio/zio)\r\n* [dev.zio:zio-test-sbt](https://github.com/zio/zio)\r\n\r\n from `2.1.1` to `2.1.2`\r\n\r\n📜 [GitHub Release Notes](https://github.com/zio/zio/releases/tag/v2.1.2)\r\n- [Version Diff](https://github.com/zio/zio/compare/v2.1.1...v2.1.2)\r\n\r\n## Usage\r\n✅ **Please merge!**\r\n\r\nI'll automatically update this PR to resolve conflicts as long as you\r\ndon't change it yourself.\r\n\r\nIf you'd like to skip this version, you can just close this PR. If you\r\nhave any feedback, just mention me in the comments below.\r\n\r\nConfigure Scala Steward for your repository with a\r\n[`.scala-steward.conf`](https://github.com/scala-steward-org/scala-steward/blob/767fcfecbfd53c507152f6cf15c846176bae561d/docs/repo-specific-configuration.md)\r\nfile.\r\n\r\n_Have a fantastic day writing Scala!_\r\n\r\n<details>\r\n<summary>🔍 Files still referring to the old version number</summary>\r\n\r\nThe following files still refer to the old version number (2.1.1).\r\nYou might want to review and update them manually.\r\n```\r\nCLA.md\r\nbuild.sbt\r\n```\r\n</details>\r\n<details>\r\n<summary>⚙ Adjust future updates</summary>\r\n\r\nAdd this to your `.scala-steward.conf` file to ignore future updates of\r\nthis dependency:\r\n```\r\nupdates.ignore = [ { groupId = \"dev.zio\" } ]\r\n```\r\nOr, add this to slow down future updates of this dependency:\r\n```\r\ndependencyOverrides = [{\r\n  pullRequests = { frequency = \"30 days\" },\r\n  dependency = { groupId = \"dev.zio\" }\r\n}]\r\n```\r\n</details>\r\n\r\n<sup>\r\nlabels: library-update, early-semver-patch, semver-spec-patch,\r\nold-version-remains, commit-count:1\r\n</sup>\r\n\r\nCo-authored-by: zio-scala-steward[bot] <145262613+zio-scala-steward[bot]@users.noreply.github.com>",
          "timestamp": "2024-06-05T09:21:10+02:00",
          "tree_id": "480429198a23ef6790fa21b7149b8e410022374b",
          "url": "https://github.com/zio/zio-kafka/commit/dce411a187c5ecbf1e120d3cc21300cbab34215e"
        },
        "date": 1717572854056,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 602.6919829242458,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 591.8420566018993,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 546.2661865081731,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 534.7901380192308,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 541.6772306153513,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 557.2133220789462,
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
          "id": "b7c58944bae82f0fafdc8599eb082d489240c57b",
          "message": "Update zio-streams, zio-test-sbt to 2.1.2 (#1249)\n\n## About this PR\r\n📦 Updates \r\n* [dev.zio:zio-streams](https://github.com/zio/zio)\r\n* [dev.zio:zio-test-sbt](https://github.com/zio/zio)\r\n\r\n from `2.1.1` to `2.1.2`\r\n\r\n📜 [GitHub Release Notes](https://github.com/zio/zio/releases/tag/v2.1.2)\r\n- [Version Diff](https://github.com/zio/zio/compare/v2.1.1...v2.1.2)\r\n\r\n## Usage\r\n✅ **Please merge!**\r\n\r\nI'll automatically update this PR to resolve conflicts as long as you\r\ndon't change it yourself.\r\n\r\nIf you'd like to skip this version, you can just close this PR. If you\r\nhave any feedback, just mention me in the comments below.\r\n\r\nConfigure Scala Steward for your repository with a\r\n[`.scala-steward.conf`](https://github.com/scala-steward-org/scala-steward/blob/767fcfecbfd53c507152f6cf15c846176bae561d/docs/repo-specific-configuration.md)\r\nfile.\r\n\r\n_Have a fantastic day writing Scala!_\r\n\r\n<details>\r\n<summary>🔍 Files still referring to the old version number</summary>\r\n\r\nThe following files still refer to the old version number (2.1.1).\r\nYou might want to review and update them manually.\r\n```\r\nCLA.md\r\n```\r\n</details>\r\n<details>\r\n<summary>⚙ Adjust future updates</summary>\r\n\r\nAdd this to your `.scala-steward.conf` file to ignore future updates of\r\nthis dependency:\r\n```\r\nupdates.ignore = [ { groupId = \"dev.zio\" } ]\r\n```\r\nOr, add this to slow down future updates of this dependency:\r\n```\r\ndependencyOverrides = [{\r\n  pullRequests = { frequency = \"30 days\" },\r\n  dependency = { groupId = \"dev.zio\" }\r\n}]\r\n```\r\n</details>\r\n\r\n<sup>\r\nlabels: library-update, early-semver-patch, semver-spec-patch,\r\nold-version-remains, commit-count:1\r\n</sup>\r\n\r\nCo-authored-by: zio-scala-steward[bot] <145262613+zio-scala-steward[bot]@users.noreply.github.com>",
          "timestamp": "2024-06-06T09:19:56+02:00",
          "tree_id": "e1bbb95466307d6adc2020c9b0123620fc07d266",
          "url": "https://github.com/zio/zio-kafka/commit/b7c58944bae82f0fafdc8599eb082d489240c57b"
        },
        "date": 1717659166053,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 601.6105717603725,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 590.7155343992179,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 542.4112140833654,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 531.8612907905448,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 534.2642523836756,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 523.9942297529747,
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
          "id": "d399b849bb559cf6d3be6ac3ed563737025d1b15",
          "message": "Allow stream to be interrupted when there is no traffic (#1251)\n\nWhen a partition is lost while there is no traffic,\r\n`PartitionStreamControl` is blocked waiting for data. We fix this by\r\nracing with the `interruptPromise`. (Thanks @josdirksen for the\r\nanalysis! See #1250.)\r\n\r\nNote: this situation can only occur with lost partitions. Timeouts (the\r\nother reason for interrupts) do not occur when there is no traffic.\r\nCurrently, we have no way to test lost partitions. Therefore, there are\r\nno added tests.\r\n\r\nThis PR does _not_ change how lost partitions are handled. That is, the\r\nstream for the partition that is lost is interrupted, the other streams\r\nare closed gracefully, the consumer aborts with an error.",
          "timestamp": "2024-06-10T10:40:55+02:00",
          "tree_id": "37c816a0f197641fc8c215a30e2ab7607188b387",
          "url": "https://github.com/zio/zio-kafka/commit/d399b849bb559cf6d3be6ac3ed563737025d1b15"
        },
        "date": 1718009631357,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 604.5108514870017,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 592.5329874602235,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 543.1924812476923,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 533.741177957372,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 634.490641900216,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 581.5305363705818,
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
          "id": "6d2b8d100849df1f1d9d52c362a462d9859c3d6d",
          "message": "Update zio-kafka, zio-kafka-testkit to 2.7.5 (#1253)\n\n## About this PR\r\n📦 Updates \r\n* [dev.zio:zio-kafka](https://github.com/zio/zio-kafka)\r\n* [dev.zio:zio-kafka-testkit](https://github.com/zio/zio-kafka)\r\n\r\n from `2.7.4` to `2.7.5`\r\n\r\n📜 [GitHub Release\r\nNotes](https://github.com/zio/zio-kafka/releases/tag/v2.7.5) - [Version\r\nDiff](https://github.com/zio/zio-kafka/compare/v2.7.4...v2.7.5)\r\n\r\n## Usage\r\n✅ **Please merge!**\r\n\r\nI'll automatically update this PR to resolve conflicts as long as you\r\ndon't change it yourself.\r\n\r\nIf you'd like to skip this version, you can just close this PR. If you\r\nhave any feedback, just mention me in the comments below.\r\n\r\nConfigure Scala Steward for your repository with a\r\n[`.scala-steward.conf`](https://github.com/scala-steward-org/scala-steward/blob/767fcfecbfd53c507152f6cf15c846176bae561d/docs/repo-specific-configuration.md)\r\nfile.\r\n\r\n_Have a fantastic day writing Scala!_\r\n\r\n<details>\r\n<summary>🔍 Files still referring to the old version number</summary>\r\n\r\nThe following files still refer to the old version number (2.7.4).\r\nYou might want to review and update them manually.\r\n```\r\nREADME.md\r\n```\r\n</details>\r\n<details>\r\n<summary>⚙ Adjust future updates</summary>\r\n\r\nAdd this to your `.scala-steward.conf` file to ignore future updates of\r\nthis dependency:\r\n```\r\nupdates.ignore = [ { groupId = \"dev.zio\" } ]\r\n```\r\nOr, add this to slow down future updates of this dependency:\r\n```\r\ndependencyOverrides = [{\r\n  pullRequests = { frequency = \"30 days\" },\r\n  dependency = { groupId = \"dev.zio\" }\r\n}]\r\n```\r\n</details>\r\n\r\n<sup>\r\nlabels: library-update, early-semver-patch, semver-spec-patch,\r\nold-version-remains, commit-count:1\r\n</sup>\r\n\r\nCo-authored-by: zio-scala-steward[bot] <145262613+zio-scala-steward[bot]@users.noreply.github.com>",
          "timestamp": "2024-06-11T19:26:25+02:00",
          "tree_id": "777a25e1b8d08066b9e717623759d2f10e8d35cc",
          "url": "https://github.com/zio/zio-kafka/commit/6d2b8d100849df1f1d9d52c362a462d9859c3d6d"
        },
        "date": 1718127583640,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 603.6047490009684,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 592.4443148107263,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 545.9018026163462,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 534.5880418378202,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 606.740672224865,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 609.9447203344017,
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
          "id": "0a6cd99312523e735ad5a5f48a284177ce3686ff",
          "message": "Aborts builds when a newer build is ready (#1255)\n\nRight now, we run all the test suites, a full benchmark, and a\r\nmemory/CPU profiler on _every commit_. These builds continue, even when\r\na newer commit is available. This PR reduces this considerably by\r\ncancelling builds when a newer commit was created. Builds on the default\r\nbranch ('master') are never cancelled.\r\n\r\nThe `ci` workflow is generated and therefore not changed in the same\r\nway.",
          "timestamp": "2024-06-12T08:53:58+02:00",
          "tree_id": "639d59ae056444ea80e6c5f605a7d3bc7897b59f",
          "url": "https://github.com/zio/zio-kafka/commit/0a6cd99312523e735ad5a5f48a284177ce3686ff"
        },
        "date": 1718176053092,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 604.0132403497208,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 592.5584048184357,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 544.1149430138462,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 536.013343620032,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 583.4190359335136,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 638.8572595923602,
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
          "id": "57fb62c87b5a09431bc407b6e058a26566d4519f",
          "message": "Update zio, zio-streams, zio-test, ... to 2.1.3 (#1256)\n\n## About this PR\r\n📦 Updates \r\n* [dev.zio:zio](https://github.com/zio/zio)\r\n* [dev.zio:zio-streams](https://github.com/zio/zio)\r\n* [dev.zio:zio-test](https://github.com/zio/zio)\r\n* [dev.zio:zio-test-sbt](https://github.com/zio/zio)\r\n\r\n from `2.1.2` to `2.1.3`\r\n\r\n📜 [GitHub Release Notes](https://github.com/zio/zio/releases/tag/v2.1.3)\r\n- [Version Diff](https://github.com/zio/zio/compare/v2.1.2...v2.1.3)\r\n\r\n## Usage\r\n✅ **Please merge!**\r\n\r\nI'll automatically update this PR to resolve conflicts as long as you\r\ndon't change it yourself.\r\n\r\nIf you'd like to skip this version, you can just close this PR. If you\r\nhave any feedback, just mention me in the comments below.\r\n\r\nConfigure Scala Steward for your repository with a\r\n[`.scala-steward.conf`](https://github.com/scala-steward-org/scala-steward/blob/767fcfecbfd53c507152f6cf15c846176bae561d/docs/repo-specific-configuration.md)\r\nfile.\r\n\r\n_Have a fantastic day writing Scala!_\r\n\r\n<details>\r\n<summary>🔍 Files still referring to the old version number</summary>\r\n\r\nThe following files still refer to the old version number (2.1.2).\r\nYou might want to review and update them manually.\r\n```\r\nCLA.md\r\nbuild.sbt\r\n```\r\n</details>\r\n<details>\r\n<summary>⚙ Adjust future updates</summary>\r\n\r\nAdd this to your `.scala-steward.conf` file to ignore future updates of\r\nthis dependency:\r\n```\r\nupdates.ignore = [ { groupId = \"dev.zio\" } ]\r\n```\r\nOr, add this to slow down future updates of this dependency:\r\n```\r\ndependencyOverrides = [{\r\n  pullRequests = { frequency = \"30 days\" },\r\n  dependency = { groupId = \"dev.zio\" }\r\n}]\r\n```\r\n</details>\r\n\r\n<sup>\r\nlabels: library-update, early-semver-patch, semver-spec-patch,\r\nold-version-remains, commit-count:1\r\n</sup>\r\n\r\nCo-authored-by: zio-scala-steward[bot] <145262613+zio-scala-steward[bot]@users.noreply.github.com>",
          "timestamp": "2024-06-13T09:46:39+02:00",
          "tree_id": "0b109c3bf2a19c8ce3768b072140cd894896e76e",
          "url": "https://github.com/zio/zio-kafka/commit/57fb62c87b5a09431bc407b6e058a26566d4519f"
        },
        "date": 1718265581151,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 605.8276846272626,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 594.3470880654748,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 544.4923917989423,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 532.0526208514423,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 635.986928340973,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 585.5391733388365,
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
          "id": "bbce6b0f6b96346d9766dba3f3f78f914e4a2216",
          "message": "Update zio-streams, zio-test-sbt to 2.1.3 (#1257)\n\n## About this PR\r\n📦 Updates \r\n* [dev.zio:zio-streams](https://github.com/zio/zio)\r\n* [dev.zio:zio-test-sbt](https://github.com/zio/zio)\r\n\r\n from `2.1.2` to `2.1.3`\r\n\r\n📜 [GitHub Release Notes](https://github.com/zio/zio/releases/tag/v2.1.3)\r\n- [Version Diff](https://github.com/zio/zio/compare/v2.1.2...v2.1.3)\r\n\r\n## Usage\r\n✅ **Please merge!**\r\n\r\nI'll automatically update this PR to resolve conflicts as long as you\r\ndon't change it yourself.\r\n\r\nIf you'd like to skip this version, you can just close this PR. If you\r\nhave any feedback, just mention me in the comments below.\r\n\r\nConfigure Scala Steward for your repository with a\r\n[`.scala-steward.conf`](https://github.com/scala-steward-org/scala-steward/blob/767fcfecbfd53c507152f6cf15c846176bae561d/docs/repo-specific-configuration.md)\r\nfile.\r\n\r\n_Have a fantastic day writing Scala!_\r\n\r\n<details>\r\n<summary>🔍 Files still referring to the old version number</summary>\r\n\r\nThe following files still refer to the old version number (2.1.2).\r\nYou might want to review and update them manually.\r\n```\r\nCLA.md\r\n```\r\n</details>\r\n<details>\r\n<summary>⚙ Adjust future updates</summary>\r\n\r\nAdd this to your `.scala-steward.conf` file to ignore future updates of\r\nthis dependency:\r\n```\r\nupdates.ignore = [ { groupId = \"dev.zio\" } ]\r\n```\r\nOr, add this to slow down future updates of this dependency:\r\n```\r\ndependencyOverrides = [{\r\n  pullRequests = { frequency = \"30 days\" },\r\n  dependency = { groupId = \"dev.zio\" }\r\n}]\r\n```\r\n</details>\r\n\r\n<sup>\r\nlabels: library-update, early-semver-patch, semver-spec-patch,\r\nold-version-remains, commit-count:1\r\n</sup>\r\n\r\nCo-authored-by: zio-scala-steward[bot] <145262613+zio-scala-steward[bot]@users.noreply.github.com>",
          "timestamp": "2024-06-14T15:26:16+10:00",
          "tree_id": "165349e2346936fbff60d0fed7dab52df13f6626",
          "url": "https://github.com/zio/zio-kafka/commit/bbce6b0f6b96346d9766dba3f3f78f914e4a2216"
        },
        "date": 1718343593108,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 603.734737414823,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 592.2633344105028,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 542.7670446113461,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 533.636942106891,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 622.5202031627026,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 559.4761468401317,
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
          "id": "23a16d35ef64580fa5201f8f00e246a20de93d4c",
          "message": "Update scalafmt-core to 3.8.2 (#1259)\n\n## About this PR\r\n📦 Updates\r\n[org.scalameta:scalafmt-core](https://github.com/scalameta/scalafmt)\r\nfrom `3.8.1` to `3.8.2`\r\n\r\n📜 [GitHub Release\r\nNotes](https://github.com/scalameta/scalafmt/releases/tag/v3.8.2) -\r\n[Version\r\nDiff](https://github.com/scalameta/scalafmt/compare/v3.8.1...v3.8.2)\r\n\r\n## Usage\r\n✅ **Please merge!**\r\n\r\nI'll automatically update this PR to resolve conflicts as long as you\r\ndon't change it yourself.\r\n\r\nIf you'd like to skip this version, you can just close this PR. If you\r\nhave any feedback, just mention me in the comments below.\r\n\r\nConfigure Scala Steward for your repository with a\r\n[`.scala-steward.conf`](https://github.com/scala-steward-org/scala-steward/blob/767fcfecbfd53c507152f6cf15c846176bae561d/docs/repo-specific-configuration.md)\r\nfile.\r\n\r\n_Have a fantastic day writing Scala!_\r\n\r\n<details>\r\n<summary>⚙ Adjust future updates</summary>\r\n\r\nAdd this to your `.scala-steward.conf` file to ignore future updates of\r\nthis dependency:\r\n```\r\nupdates.ignore = [ { groupId = \"org.scalameta\", artifactId = \"scalafmt-core\" } ]\r\n```\r\nOr, add this to slow down future updates of this dependency:\r\n```\r\ndependencyOverrides = [{\r\n  pullRequests = { frequency = \"30 days\" },\r\n  dependency = { groupId = \"org.scalameta\", artifactId = \"scalafmt-core\" }\r\n}]\r\n```\r\n</details>\r\n\r\n<sup>\r\nlabels: library-update, early-semver-patch, semver-spec-patch,\r\ncommit-count:1\r\n</sup>\r\n\r\nCo-authored-by: zio-scala-steward[bot] <145262613+zio-scala-steward[bot]@users.noreply.github.com>",
          "timestamp": "2024-06-15T16:15:19+10:00",
          "tree_id": "a7f7368375b2e069f09767e39940d60049ef6318",
          "url": "https://github.com/zio/zio-kafka/commit/23a16d35ef64580fa5201f8f00e246a20de93d4c"
        },
        "date": 1718432910963,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 604.2295730966855,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 594.0999960280448,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 546.6708079878847,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 534.5018361903846,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 590.3821703902703,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 565.3561982108452,
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
          "id": "b8e87666fd7374dd812654c54d645cf2e33f4546",
          "message": "Update zio, zio-streams, zio-test, ... to 2.1.4 (#1260)\n\n## About this PR\r\n📦 Updates \r\n* [dev.zio:zio](https://github.com/zio/zio)\r\n* [dev.zio:zio-streams](https://github.com/zio/zio)\r\n* [dev.zio:zio-test](https://github.com/zio/zio)\r\n* [dev.zio:zio-test-sbt](https://github.com/zio/zio)\r\n\r\n from `2.1.3` to `2.1.4`\r\n\r\n📜 [GitHub Release Notes](https://github.com/zio/zio/releases/tag/2.1.4)\r\n- [GitHub Release Notes](https://github.com/zio/zio/releases/tag/v2.1.4)\r\n- [Version Diff](https://github.com/zio/zio/compare/v2.1.3...v2.1.4)\r\n\r\n## Usage\r\n✅ **Please merge!**\r\n\r\nI'll automatically update this PR to resolve conflicts as long as you\r\ndon't change it yourself.\r\n\r\nIf you'd like to skip this version, you can just close this PR. If you\r\nhave any feedback, just mention me in the comments below.\r\n\r\nConfigure Scala Steward for your repository with a\r\n[`.scala-steward.conf`](https://github.com/scala-steward-org/scala-steward/blob/767fcfecbfd53c507152f6cf15c846176bae561d/docs/repo-specific-configuration.md)\r\nfile.\r\n\r\n_Have a fantastic day writing Scala!_\r\n\r\n<details>\r\n<summary>🔍 Files still referring to the old version number</summary>\r\n\r\nThe following files still refer to the old version number (2.1.3).\r\nYou might want to review and update them manually.\r\n```\r\nbuild.sbt\r\n```\r\n</details>\r\n<details>\r\n<summary>⚙ Adjust future updates</summary>\r\n\r\nAdd this to your `.scala-steward.conf` file to ignore future updates of\r\nthis dependency:\r\n```\r\nupdates.ignore = [ { groupId = \"dev.zio\" } ]\r\n```\r\nOr, add this to slow down future updates of this dependency:\r\n```\r\ndependencyOverrides = [{\r\n  pullRequests = { frequency = \"30 days\" },\r\n  dependency = { groupId = \"dev.zio\" }\r\n}]\r\n```\r\n</details>\r\n\r\n<sup>\r\nlabels: library-update, early-semver-patch, semver-spec-patch,\r\nold-version-remains, commit-count:1\r\n</sup>\r\n\r\nCo-authored-by: zio-scala-steward[bot] <145262613+zio-scala-steward[bot]@users.noreply.github.com>",
          "timestamp": "2024-06-21T09:33:28+02:00",
          "tree_id": "973b6247b91503cb545e3891681a49a271fffc67",
          "url": "https://github.com/zio/zio-kafka/commit/b8e87666fd7374dd812654c54d645cf2e33f4546"
        },
        "date": 1718955996684,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 603.6781603786219,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 591.5975919721787,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 545.5168259890385,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 535.145431090545,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 575.5322809550269,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 610.6140301611415,
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
          "id": "74396057e14d7117b8a1723bd8856877d9806acf",
          "message": "Update zio-streams, zio-test-sbt to 2.1.4 (#1261)\n\n## About this PR\r\n📦 Updates \r\n* [dev.zio:zio-streams](https://github.com/zio/zio)\r\n* [dev.zio:zio-test-sbt](https://github.com/zio/zio)\r\n\r\n from `2.1.3` to `2.1.4`\r\n\r\n📜 [GitHub Release Notes](https://github.com/zio/zio/releases/tag/2.1.4)\r\n- [GitHub Release Notes](https://github.com/zio/zio/releases/tag/v2.1.4)\r\n- [Version Diff](https://github.com/zio/zio/compare/v2.1.3...v2.1.4)\r\n\r\n## Usage\r\n✅ **Please merge!**\r\n\r\nI'll automatically update this PR to resolve conflicts as long as you\r\ndon't change it yourself.\r\n\r\nIf you'd like to skip this version, you can just close this PR. If you\r\nhave any feedback, just mention me in the comments below.\r\n\r\nConfigure Scala Steward for your repository with a\r\n[`.scala-steward.conf`](https://github.com/scala-steward-org/scala-steward/blob/767fcfecbfd53c507152f6cf15c846176bae561d/docs/repo-specific-configuration.md)\r\nfile.\r\n\r\n_Have a fantastic day writing Scala!_\r\n\r\n<details>\r\n<summary>⚙ Adjust future updates</summary>\r\n\r\nAdd this to your `.scala-steward.conf` file to ignore future updates of\r\nthis dependency:\r\n```\r\nupdates.ignore = [ { groupId = \"dev.zio\" } ]\r\n```\r\nOr, add this to slow down future updates of this dependency:\r\n```\r\ndependencyOverrides = [{\r\n  pullRequests = { frequency = \"30 days\" },\r\n  dependency = { groupId = \"dev.zio\" }\r\n}]\r\n```\r\n</details>\r\n\r\n<sup>\r\nlabels: library-update, early-semver-patch, semver-spec-patch,\r\ncommit-count:1\r\n</sup>\r\n\r\nCo-authored-by: zio-scala-steward[bot] <145262613+zio-scala-steward[bot]@users.noreply.github.com>",
          "timestamp": "2024-06-22T10:22:25+02:00",
          "tree_id": "2d9a4a75b8a4c10c79c3596cabad33271ac3d4cd",
          "url": "https://github.com/zio/zio-kafka/commit/74396057e14d7117b8a1723bd8856877d9806acf"
        },
        "date": 1719045324239,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 602.6197340904283,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 593.247937769162,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 546.2614876785577,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 537.9000800386218,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 614.2869738791352,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 545.7385026808342,
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
          "id": "d52d9ff69ee517e907da10d6672532aea028df2a",
          "message": "Update zio-sbt-ci, zio-sbt-ecosystem, ... to 0.4.0-alpha.28 (#1263)\n\n## About this PR\r\n📦 Updates \r\n* [dev.zio:zio-sbt-ci](https://github.com/zio/zio-sbt)\r\n* [dev.zio:zio-sbt-ecosystem](https://github.com/zio/zio-sbt)\r\n* [dev.zio:zio-sbt-website](https://github.com/zio/zio-sbt)\r\n\r\n from `0.4.0-alpha.27` to `0.4.0-alpha.28`\r\n\r\n📜 [GitHub Release\r\nNotes](https://github.com/zio/zio-sbt/releases/tag/v0.4.0-alpha.28) -\r\n[Version\r\nDiff](https://github.com/zio/zio-sbt/compare/v0.4.0-alpha.27...v0.4.0-alpha.28)\r\n\r\n## Usage\r\n✅ **Please merge!**\r\n\r\nI'll automatically update this PR to resolve conflicts as long as you\r\ndon't change it yourself.\r\n\r\nIf you'd like to skip this version, you can just close this PR. If you\r\nhave any feedback, just mention me in the comments below.\r\n\r\nConfigure Scala Steward for your repository with a\r\n[`.scala-steward.conf`](https://github.com/scala-steward-org/scala-steward/blob/767fcfecbfd53c507152f6cf15c846176bae561d/docs/repo-specific-configuration.md)\r\nfile.\r\n\r\n_Have a fantastic day writing Scala!_\r\n\r\n<details>\r\n<summary>⚙ Adjust future updates</summary>\r\n\r\nAdd this to your `.scala-steward.conf` file to ignore future updates of\r\nthis dependency:\r\n```\r\nupdates.ignore = [ { groupId = \"dev.zio\" } ]\r\n```\r\nOr, add this to slow down future updates of this dependency:\r\n```\r\ndependencyOverrides = [{\r\n  pullRequests = { frequency = \"30 days\" },\r\n  dependency = { groupId = \"dev.zio\" }\r\n}]\r\n```\r\n</details>\r\n\r\n<sup>\r\nlabels: sbt-plugin-update, early-semver-pre-release,\r\nsemver-spec-pre-release, commit-count:1\r\n</sup>\r\n\r\n---------\r\n\r\nCo-authored-by: zio-scala-steward[bot] <145262613+zio-scala-steward[bot]@users.noreply.github.com>\r\nCo-authored-by: Erik van Oosten <e.vanoosten@grons.nl>",
          "timestamp": "2024-06-26T13:39:04+02:00",
          "tree_id": "450cb52f98090a024e22cfd79f56aa1c29b6975e",
          "url": "https://github.com/zio/zio-kafka/commit/d52d9ff69ee517e907da10d6672532aea028df2a"
        },
        "date": 1719402733054,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 607.0211243141155,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 595.8555846397766,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 545.7951080068269,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 536.9373057160255,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 606.4985900176216,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 562.0452415034028,
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
          "id": "3275e18f9d0683b6f56bcfe69bbfadf08565310f",
          "message": "Update zio, zio-streams, zio-test, ... to 2.1.5 (#1266)\n\n## About this PR\r\n📦 Updates \r\n* [dev.zio:zio](https://github.com/zio/zio)\r\n* [dev.zio:zio-streams](https://github.com/zio/zio)\r\n* [dev.zio:zio-test](https://github.com/zio/zio)\r\n* [dev.zio:zio-test-sbt](https://github.com/zio/zio)\r\n\r\n from `2.1.4` to `2.1.5`\r\n\r\n📜 [GitHub Release Notes](https://github.com/zio/zio/releases/tag/v2.1.5)\r\n- [Version Diff](https://github.com/zio/zio/compare/v2.1.4...v2.1.5)\r\n\r\n## Usage\r\n✅ **Please merge!**\r\n\r\nI'll automatically update this PR to resolve conflicts as long as you\r\ndon't change it yourself.\r\n\r\nIf you'd like to skip this version, you can just close this PR. If you\r\nhave any feedback, just mention me in the comments below.\r\n\r\nConfigure Scala Steward for your repository with a\r\n[`.scala-steward.conf`](https://github.com/scala-steward-org/scala-steward/blob/767fcfecbfd53c507152f6cf15c846176bae561d/docs/repo-specific-configuration.md)\r\nfile.\r\n\r\n_Have a fantastic day writing Scala!_\r\n\r\n<details>\r\n<summary>🔍 Files still referring to the old version number</summary>\r\n\r\nThe following files still refer to the old version number (2.1.4).\r\nYou might want to review and update them manually.\r\n```\r\nbuild.sbt\r\nzio-kafka/src/main/scala/zio/kafka/consumer/OffsetBatch.scala\r\n```\r\n</details>\r\n<details>\r\n<summary>⚙ Adjust future updates</summary>\r\n\r\nAdd this to your `.scala-steward.conf` file to ignore future updates of\r\nthis dependency:\r\n```\r\nupdates.ignore = [ { groupId = \"dev.zio\" } ]\r\n```\r\nOr, add this to slow down future updates of this dependency:\r\n```\r\ndependencyOverrides = [{\r\n  pullRequests = { frequency = \"30 days\" },\r\n  dependency = { groupId = \"dev.zio\" }\r\n}]\r\n```\r\n</details>\r\n\r\n<sup>\r\nlabels: library-update, early-semver-patch, semver-spec-patch,\r\nold-version-remains, commit-count:1\r\n</sup>\r\n\r\nCo-authored-by: zio-scala-steward[bot] <145262613+zio-scala-steward[bot]@users.noreply.github.com>",
          "timestamp": "2024-06-29T15:48:07+10:00",
          "tree_id": "7d27a1c115502dfb46756b00c543e7b7ed6621e7",
          "url": "https://github.com/zio/zio-kafka/commit/3275e18f9d0683b6f56bcfe69bbfadf08565310f"
        },
        "date": 1719640855592,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 601.5325208572813,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 589.3858727300559,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 542.6371611868269,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 535.3797987120192,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 571.3014223534054,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 581.0633153448957,
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
          "id": "091ab4cfe8e92845117bde97439fbd09a5a94baa",
          "message": "Update zio-streams, zio-test-sbt to 2.1.5 (#1267)\n\n## About this PR\r\n📦 Updates \r\n* [dev.zio:zio-streams](https://github.com/zio/zio)\r\n* [dev.zio:zio-test-sbt](https://github.com/zio/zio)\r\n\r\n from `2.1.4` to `2.1.5`\r\n\r\n📜 [GitHub Release Notes](https://github.com/zio/zio/releases/tag/v2.1.5)\r\n- [Version Diff](https://github.com/zio/zio/compare/v2.1.4...v2.1.5)\r\n\r\n## Usage\r\n✅ **Please merge!**\r\n\r\nI'll automatically update this PR to resolve conflicts as long as you\r\ndon't change it yourself.\r\n\r\nIf you'd like to skip this version, you can just close this PR. If you\r\nhave any feedback, just mention me in the comments below.\r\n\r\nConfigure Scala Steward for your repository with a\r\n[`.scala-steward.conf`](https://github.com/scala-steward-org/scala-steward/blob/767fcfecbfd53c507152f6cf15c846176bae561d/docs/repo-specific-configuration.md)\r\nfile.\r\n\r\n_Have a fantastic day writing Scala!_\r\n\r\n<details>\r\n<summary>🔍 Files still referring to the old version number</summary>\r\n\r\nThe following files still refer to the old version number (2.1.4).\r\nYou might want to review and update them manually.\r\n```\r\nzio-kafka/src/main/scala/zio/kafka/consumer/OffsetBatch.scala\r\n```\r\n</details>\r\n<details>\r\n<summary>⚙ Adjust future updates</summary>\r\n\r\nAdd this to your `.scala-steward.conf` file to ignore future updates of\r\nthis dependency:\r\n```\r\nupdates.ignore = [ { groupId = \"dev.zio\" } ]\r\n```\r\nOr, add this to slow down future updates of this dependency:\r\n```\r\ndependencyOverrides = [{\r\n  pullRequests = { frequency = \"30 days\" },\r\n  dependency = { groupId = \"dev.zio\" }\r\n}]\r\n```\r\n</details>\r\n\r\n<sup>\r\nlabels: library-update, early-semver-patch, semver-spec-patch,\r\nold-version-remains, commit-count:1\r\n</sup>\r\n\r\nCo-authored-by: zio-scala-steward[bot] <145262613+zio-scala-steward[bot]@users.noreply.github.com>",
          "timestamp": "2024-06-30T08:39:20+02:00",
          "tree_id": "80d810ddf7d149ce66eda4d5188f915d45f039ca",
          "url": "https://github.com/zio/zio-kafka/commit/091ab4cfe8e92845117bde97439fbd09a5a94baa"
        },
        "date": 1719730336485,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 603.1663264140037,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 591.068378435866,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 545.9606185489422,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 535.3196050903845,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 645.2216873127569,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 546.4595402488694,
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
          "id": "e87e14c22b5fdc29f4540654ab9c21186d7bbed3",
          "message": "Update kafka-clients to 3.7.1 (#1268)\n\n## About this PR\r\n📦 Updates [org.apache.kafka:kafka-clients](https://kafka.apache.org)\r\nfrom `3.7.0` to `3.7.1`\r\n\r\n## Usage\r\n✅ **Please merge!**\r\n\r\nI'll automatically update this PR to resolve conflicts as long as you\r\ndon't change it yourself.\r\n\r\nIf you'd like to skip this version, you can just close this PR. If you\r\nhave any feedback, just mention me in the comments below.\r\n\r\nConfigure Scala Steward for your repository with a\r\n[`.scala-steward.conf`](https://github.com/scala-steward-org/scala-steward/blob/767fcfecbfd53c507152f6cf15c846176bae561d/docs/repo-specific-configuration.md)\r\nfile.\r\n\r\n_Have a fantastic day writing Scala!_\r\n\r\n<details>\r\n<summary>🔍 Files still referring to the old version number</summary>\r\n\r\nThe following files still refer to the old version number (3.7.0).\r\nYou might want to review and update them manually.\r\n```\r\nbuild.sbt\r\n```\r\n</details>\r\n<details>\r\n<summary>⚙ Adjust future updates</summary>\r\n\r\nAdd this to your `.scala-steward.conf` file to ignore future updates of\r\nthis dependency:\r\n```\r\nupdates.ignore = [ { groupId = \"org.apache.kafka\", artifactId = \"kafka-clients\" } ]\r\n```\r\nOr, add this to slow down future updates of this dependency:\r\n```\r\ndependencyOverrides = [{\r\n  pullRequests = { frequency = \"30 days\" },\r\n  dependency = { groupId = \"org.apache.kafka\", artifactId = \"kafka-clients\" }\r\n}]\r\n```\r\n</details>\r\n\r\n<sup>\r\nlabels: library-update, early-semver-patch, semver-spec-patch,\r\nold-version-remains, commit-count:1\r\n</sup>\r\n\r\nCo-authored-by: zio-scala-steward[bot] <145262613+zio-scala-steward[bot]@users.noreply.github.com>",
          "timestamp": "2024-07-01T09:14:54+02:00",
          "tree_id": "9cf065de6cc35f70a3942bf83fdfc45f766013c7",
          "url": "https://github.com/zio/zio-kafka/commit/e87e14c22b5fdc29f4540654ab9c21186d7bbed3"
        },
        "date": 1719818888542,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 606.238383668082,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 595.0757272110615,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 545.3277421113461,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 535.4349773259614,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 589.4756081698379,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 608.8843151488912,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "peternatwork@gmail.com",
            "name": "Peter Nham",
            "username": "petern-sc"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "4fe355110e07e41b6c3f2d3380548b71b4da51a6",
          "message": "Document importance of using MaxValue when using partitionedStream (#1269)\n\nMention the importance of using `Int.MaxValue` or similar when using\r\n`partitionedStream` with `flatMapPar`, otherwise it will lead to a hung\r\nconsumer that gets evicted by Kafka.\r\n\r\nSolves #1239.",
          "timestamp": "2024-07-02T19:37:55+02:00",
          "tree_id": "a4d1755becffc01b9e52711f32c8058679c1f0c3",
          "url": "https://github.com/zio/zio-kafka/commit/4fe355110e07e41b6c3f2d3380548b71b4da51a6"
        },
        "date": 1719942649397,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 603.2945593196275,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 591.9477647988828,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 546.9489069520192,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 536.7655914644231,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 544.3091356624864,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 598.3946994820636,
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
          "id": "ff5b9fc30705c5ba845a42366396c036c8a9f39c",
          "message": "Update sbt to 1.10.1 (#1271)\n\n## About this PR\r\n📦 Updates [org.scala-sbt:sbt](https://github.com/sbt/sbt) from `1.10.0`\r\nto `1.10.1`\r\n\r\n📜 [GitHub Release\r\nNotes](https://github.com/sbt/sbt/releases/tag/v1.10.1) - [Version\r\nDiff](https://github.com/sbt/sbt/compare/v1.10.0...v1.10.1)\r\n\r\n## Usage\r\n✅ **Please merge!**\r\n\r\nI'll automatically update this PR to resolve conflicts as long as you\r\ndon't change it yourself.\r\n\r\nIf you'd like to skip this version, you can just close this PR. If you\r\nhave any feedback, just mention me in the comments below.\r\n\r\nConfigure Scala Steward for your repository with a\r\n[`.scala-steward.conf`](https://github.com/scala-steward-org/scala-steward/blob/767fcfecbfd53c507152f6cf15c846176bae561d/docs/repo-specific-configuration.md)\r\nfile.\r\n\r\n_Have a fantastic day writing Scala!_\r\n\r\n<details>\r\n<summary>🔍 Files still referring to the old version number</summary>\r\n\r\nThe following files still refer to the old version number (1.10.0).\r\nYou might want to review and update them manually.\r\n```\r\nproject/plugins.sbt\r\n```\r\n</details>\r\n<details>\r\n<summary>⚙ Adjust future updates</summary>\r\n\r\nAdd this to your `.scala-steward.conf` file to ignore future updates of\r\nthis dependency:\r\n```\r\nupdates.ignore = [ { groupId = \"org.scala-sbt\", artifactId = \"sbt\" } ]\r\n```\r\nOr, add this to slow down future updates of this dependency:\r\n```\r\ndependencyOverrides = [{\r\n  pullRequests = { frequency = \"30 days\" },\r\n  dependency = { groupId = \"org.scala-sbt\", artifactId = \"sbt\" }\r\n}]\r\n```\r\n</details>\r\n\r\n<sup>\r\nlabels: library-update, early-semver-patch, semver-spec-patch,\r\nversion-scheme:early-semver, old-version-remains, commit-count:1\r\n</sup>\r\n\r\nCo-authored-by: zio-scala-steward[bot] <145262613+zio-scala-steward[bot]@users.noreply.github.com>",
          "timestamp": "2024-07-08T08:44:20+02:00",
          "tree_id": "67f327197a51a98040e3fae6d6b26562209a222a",
          "url": "https://github.com/zio/zio-kafka/commit/ff5b9fc30705c5ba845a42366396c036c8a9f39c"
        },
        "date": 1720421876069,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 605.3836444722532,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 594.5398958614526,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 552.1518760066347,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 539.9809078725962,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 609.7769685888647,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 551.7845704881669,
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
          "id": "a8596f38fbce56a2dd10e5a38a8b18511a24e41d",
          "message": "A lost partition is no longer fatal (#1252)\n\nBefore 2.7.0 a lost partition was treated as a revoked partition. Since\r\nthe partition is already assigned to another node, this potentially\r\nleads to duplicate processing of records.\r\n\r\nZio-kafka 2.7.0 assumes that a lost partition is a fatal event. It leads\r\nto an interrupt in the stream that handles the partition. The other\r\nstreams are ended, and the consumer closes with an error. Usually, a\r\nfull program restart is needed to resume consuming.\r\n\r\nIt should be noted that stream processing is not interrupted\r\nimmediately. Only when the stream requests new records, the interrupt is\r\nobserved. Unfortunately, we have not found a clean way to interrupt the\r\nstream consumer directly.\r\n\r\nMeanwhile, from bug reports (#1233, #1250), we understand that\r\npartitions are usually lost when no records have been received for a\r\nlong time.\r\n\r\nIn conclusion, 1) it is not possible to immediately interrupt user\r\nstream processing, and 2) it is most likely not needed anyway because\r\nthe stream is already done processing and awaiting more records.\r\n\r\nWith this change, a lost partition no longer leads to an interrupt.\r\nInstead, we first drain the stream's internal queue (just to be sure, it\r\nis probably already empty), and then we end the stream gracefully (that\r\nis, without error, like we do with revoked partitions). Other streams\r\nare not affected, the consumer will continue to work.\r\n\r\nLost partitions do not affect the features `rebalanceSafeCommits` and\r\n`restartStreamsOnRebalancing`; they do _not_ hold up a rebalance waiting\r\nfor commits to complete, and they do _not_ lead to restarts of other\r\nstreams.\r\n\r\nClients that want to handle the partition lost event somehow, instead of\r\nhandling the failed stream they need to create their own `RebalanceListener`\r\nand handle the `onLost` call.\r\n\r\nFixes #1233 and #1250.",
          "timestamp": "2024-07-09T21:22:34+02:00",
          "tree_id": "c11cd9af71090896fd8528bd23ad9c39e5fb5df6",
          "url": "https://github.com/zio/zio-kafka/commit/a8596f38fbce56a2dd10e5a38a8b18511a24e41d"
        },
        "date": 1720553723295,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 604.098766030093,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 592.3762830288268,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 540.6338886732693,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 540.7408361735577,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 594.5001081707028,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 576.4919430570362,
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
          "id": "c71735948d58c171c19ca344795caaa0e2210141",
          "message": " Allow for some auth exceptions during poll (#1270)\n\nSome brokers are sometimes too slow to authorize or authenticate a poll.\r\nThis results in spurious exceptions. With this change we continue\r\npolling unless the error happens too often.\r\n\r\nWe test the change with a new type of unit test for Runloop.",
          "timestamp": "2024-07-10T10:09:35+02:00",
          "tree_id": "fa447d535c968e90348dea33cf7086ec535044dc",
          "url": "https://github.com/zio/zio-kafka/commit/c71735948d58c171c19ca344795caaa0e2210141"
        },
        "date": 1720599752316,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 605.3600239877096,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 593.0148916776536,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 545.8209577654807,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 539.4642567778845,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 630.1635916894054,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 578.7482499215806,
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
          "id": "e46bc04d3a1a038bf18cc15d27e3bab7c699b882",
          "message": "Update zio, zio-streams, zio-test, ... to 2.1.6 (#1273)\n\n## About this PR\r\n📦 Updates \r\n* [dev.zio:zio](https://github.com/zio/zio)\r\n* [dev.zio:zio-streams](https://github.com/zio/zio)\r\n* [dev.zio:zio-test](https://github.com/zio/zio)\r\n* [dev.zio:zio-test-sbt](https://github.com/zio/zio)\r\n\r\n from `2.1.5` to `2.1.6`\r\n\r\n📜 [GitHub Release Notes](https://github.com/zio/zio/releases/tag/v2.1.6)\r\n- [Version Diff](https://github.com/zio/zio/compare/v2.1.5...v2.1.6)\r\n\r\n## Usage\r\n✅ **Please merge!**\r\n\r\nI'll automatically update this PR to resolve conflicts as long as you\r\ndon't change it yourself.\r\n\r\nIf you'd like to skip this version, you can just close this PR. If you\r\nhave any feedback, just mention me in the comments below.\r\n\r\nConfigure Scala Steward for your repository with a\r\n[`.scala-steward.conf`](https://github.com/scala-steward-org/scala-steward/blob/767fcfecbfd53c507152f6cf15c846176bae561d/docs/repo-specific-configuration.md)\r\nfile.\r\n\r\n_Have a fantastic day writing Scala!_\r\n\r\n<details>\r\n<summary>🔍 Files still referring to the old version number</summary>\r\n\r\nThe following files still refer to the old version number (2.1.5).\r\nYou might want to review and update them manually.\r\n```\r\nbuild.sbt\r\n```\r\n</details>\r\n<details>\r\n<summary>⚙ Adjust future updates</summary>\r\n\r\nAdd this to your `.scala-steward.conf` file to ignore future updates of\r\nthis dependency:\r\n```\r\nupdates.ignore = [ { groupId = \"dev.zio\" } ]\r\n```\r\nOr, add this to slow down future updates of this dependency:\r\n```\r\ndependencyOverrides = [{\r\n  pullRequests = { frequency = \"30 days\" },\r\n  dependency = { groupId = \"dev.zio\" }\r\n}]\r\n```\r\n</details>\r\n\r\n<sup>\r\nlabels: library-update, early-semver-patch, semver-spec-patch,\r\nold-version-remains, commit-count:1\r\n</sup>\r\n\r\nCo-authored-by: zio-scala-steward[bot] <145262613+zio-scala-steward[bot]@users.noreply.github.com>",
          "timestamp": "2024-07-11T08:08:03+02:00",
          "tree_id": "b1a705cd69bee12bcfe2f80a4c2c9217e54aa116",
          "url": "https://github.com/zio/zio-kafka/commit/e46bc04d3a1a038bf18cc15d27e3bab7c699b882"
        },
        "date": 1720678853554,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 603.3783114878214,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 593.251668068715,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 544.5645521653845,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 532.4182859780448,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 612.6220796247568,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 584.7478967411637,
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
          "id": "0d2cc49740954f07312b019167579b7e4489d2fc",
          "message": "Update zio-kafka, zio-kafka-testkit to 2.8.0 (#1274)\n\n## About this PR\r\n📦 Updates \r\n* [dev.zio:zio-kafka](https://github.com/zio/zio-kafka)\r\n* [dev.zio:zio-kafka-testkit](https://github.com/zio/zio-kafka)\r\n\r\n from `2.7.5` to `2.8.0`\r\n\r\n📜 [GitHub Release\r\nNotes](https://github.com/zio/zio-kafka/releases/tag/v2.8.0) - [Version\r\nDiff](https://github.com/zio/zio-kafka/compare/v2.7.5...v2.8.0)\r\n\r\n## Usage\r\n✅ **Please merge!**\r\n\r\nI'll automatically update this PR to resolve conflicts as long as you\r\ndon't change it yourself.\r\n\r\nIf you'd like to skip this version, you can just close this PR. If you\r\nhave any feedback, just mention me in the comments below.\r\n\r\nConfigure Scala Steward for your repository with a\r\n[`.scala-steward.conf`](https://github.com/scala-steward-org/scala-steward/blob/767fcfecbfd53c507152f6cf15c846176bae561d/docs/repo-specific-configuration.md)\r\nfile.\r\n\r\n_Have a fantastic day writing Scala!_\r\n\r\n<details>\r\n<summary>🔍 Files still referring to the old version number</summary>\r\n\r\nThe following files still refer to the old version number (2.7.5).\r\nYou might want to review and update them manually.\r\n```\r\nREADME.md\r\n```\r\n</details>\r\n<details>\r\n<summary>⚙ Adjust future updates</summary>\r\n\r\nAdd this to your `.scala-steward.conf` file to ignore future updates of\r\nthis dependency:\r\n```\r\nupdates.ignore = [ { groupId = \"dev.zio\" } ]\r\n```\r\nOr, add this to slow down future updates of this dependency:\r\n```\r\ndependencyOverrides = [{\r\n  pullRequests = { frequency = \"30 days\" },\r\n  dependency = { groupId = \"dev.zio\" }\r\n}]\r\n```\r\n</details>\r\n\r\n<sup>\r\nlabels: library-update, early-semver-minor, semver-spec-minor,\r\nold-version-remains, commit-count:1\r\n</sup>\r\n\r\nCo-authored-by: zio-scala-steward[bot] <145262613+zio-scala-steward[bot]@users.noreply.github.com>",
          "timestamp": "2024-07-13T10:17:41+02:00",
          "tree_id": "7d16d3278669d6eec2fe92d212af4432fce7be49",
          "url": "https://github.com/zio/zio-kafka/commit/0d2cc49740954f07312b019167579b7e4489d2fc"
        },
        "date": 1720859433532,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 603.7559842256983,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 593.0045087211173,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 543.1455862883655,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 539.0550856913461,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 646.9318441141621,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 571.6957015269375,
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
          "id": "2028e31a6c909a2229df3a2e8a7edf0bf69066cd",
          "message": "Update zio-streams, zio-test-sbt to 2.1.6 (#1275)\n\n## About this PR\r\n📦 Updates \r\n* [dev.zio:zio-streams](https://github.com/zio/zio)\r\n* [dev.zio:zio-test-sbt](https://github.com/zio/zio)\r\n\r\n from `2.1.5` to `2.1.6`\r\n\r\n📜 [GitHub Release Notes](https://github.com/zio/zio/releases/tag/v2.1.6)\r\n- [Version Diff](https://github.com/zio/zio/compare/v2.1.5...v2.1.6)\r\n\r\n## Usage\r\n✅ **Please merge!**\r\n\r\nI'll automatically update this PR to resolve conflicts as long as you\r\ndon't change it yourself.\r\n\r\nIf you'd like to skip this version, you can just close this PR. If you\r\nhave any feedback, just mention me in the comments below.\r\n\r\nConfigure Scala Steward for your repository with a\r\n[`.scala-steward.conf`](https://github.com/scala-steward-org/scala-steward/blob/767fcfecbfd53c507152f6cf15c846176bae561d/docs/repo-specific-configuration.md)\r\nfile.\r\n\r\n_Have a fantastic day writing Scala!_\r\n\r\n<details>\r\n<summary>⚙ Adjust future updates</summary>\r\n\r\nAdd this to your `.scala-steward.conf` file to ignore future updates of\r\nthis dependency:\r\n```\r\nupdates.ignore = [ { groupId = \"dev.zio\" } ]\r\n```\r\nOr, add this to slow down future updates of this dependency:\r\n```\r\ndependencyOverrides = [{\r\n  pullRequests = { frequency = \"30 days\" },\r\n  dependency = { groupId = \"dev.zio\" }\r\n}]\r\n```\r\n</details>\r\n\r\n<sup>\r\nlabels: library-update, early-semver-patch, semver-spec-patch,\r\ncommit-count:1\r\n</sup>\r\n\r\nCo-authored-by: zio-scala-steward[bot] <145262613+zio-scala-steward[bot]@users.noreply.github.com>",
          "timestamp": "2024-07-13T10:17:10+02:00",
          "tree_id": "29d68be686a120e8e37049f0d1231f89bd50e7b5",
          "url": "https://github.com/zio/zio-kafka/commit/2028e31a6c909a2229df3a2e8a7edf0bf69066cd"
        },
        "date": 1720859435932,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 602.4896566209312,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 592.1364978644692,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 545.7342963055769,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 535.9591164144231,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 584.8936288323242,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 570.0810644743797,
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
          "id": "50f804c0478f1a81f39e15ae170fe12cc82dd344",
          "message": "Improve example app (#1279)\n\nThis gives consumer settings its own layer and extracts the streaming\r\nlogic. This makes the example app more modular and easier to adapt to\r\nlarger applications.",
          "timestamp": "2024-07-14T11:34:39+02:00",
          "tree_id": "03e5d10ea216978df758a862170c9ebe10a60ca1",
          "url": "https://github.com/zio/zio-kafka/commit/50f804c0478f1a81f39e15ae170fe12cc82dd344"
        },
        "date": 1720950463544,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 603.1694845037615,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 591.1773926010054,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 540.8072912852883,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 532.259100431891,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 602.215475811892,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 554.9224844929968,
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
          "id": "9a60ccdde0f4c5da69333731b8d22f9e3be9424b",
          "message": "Small improvements to the Producer (#1272)\n\nBy using ZIO.async, we no longer need a reference to the zio runtime,\r\nnor do we need the `exec` trickery anymore.",
          "timestamp": "2024-07-14T12:01:34+02:00",
          "tree_id": "459864acd848a98e17d9d0c55ce26829407ab6f7",
          "url": "https://github.com/zio/zio-kafka/commit/9a60ccdde0f4c5da69333731b8d22f9e3be9424b"
        },
        "date": 1720952078236,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 604.8168287602235,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 593.1608651558658,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 547.2285609324039,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 541.2325440078525,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 582.0283189303783,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 569.5321887662788,
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
          "id": "e6af87433d59e203845a93b8bce7a5d805c8f975",
          "message": "Document metrics and consumer tuning based on metrics (#1280)\n\nAlso: fix typo and make metric descriptions consistent.",
          "timestamp": "2024-07-14T19:52:21+02:00",
          "tree_id": "3bb549958b4996f9490a99e7cd5e3f87129580eb",
          "url": "https://github.com/zio/zio-kafka/commit/e6af87433d59e203845a93b8bce7a5d805c8f975"
        },
        "date": 1720980344859,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 603.8672108218994,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 593.1302576950839,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 548.3923204990385,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 534.7195127549679,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 600.0929207656217,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 550.2869195884962,
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
            "name": "zio",
            "username": "zio"
          },
          "committer": {
            "name": "zio",
            "username": "zio"
          },
          "id": "ab62660f43b13f0273a150b93046f00bd5ab578e",
          "message": "Require a semaphore in Consumer.fromJavaConsumer",
          "timestamp": "2024-11-10T08:47:43Z",
          "url": "https://github.com/zio/zio-kafka/pull/1368/commits/ab62660f43b13f0273a150b93046f00bd5ab578e"
        },
        "date": 1731229801574,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordPar",
            "value": 20.031479677068607,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeq",
            "value": 15.048652674165549,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 602.2293577848789,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 591.3364873797764,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 122.37846134433332,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 344.8716119466667,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 543.4638810711538,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 533.278752370673,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 549.1374941792433,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 571.0739380262569,
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
          "id": "3a3beab41b0615b9bc7826364efde2de8d855ff5",
          "message": "Automate pruning of benchmark history",
          "timestamp": "2024-11-10T08:47:43Z",
          "url": "https://github.com/zio/zio-kafka/pull/1370/commits/3a3beab41b0615b9bc7826364efde2de8d855ff5"
        },
        "date": 1731235215231,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordPar",
            "value": 13.177096663347152,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeq",
            "value": 10.443259277688076,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 602.2145443249165,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 592.6852373364245,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 229.75616097400004,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 508.21205360666664,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 543.2882607914423,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 534.1745283517628,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 552.540667680973,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 526.3545891797146,
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
          "id": "067c1eac61b562697e37f0918f55790e283d0433",
          "message": "Tests for PartitionStreamControl",
          "timestamp": "2024-11-10T08:47:43Z",
          "url": "https://github.com/zio/zio-kafka/pull/1371/commits/067c1eac61b562697e37f0918f55790e283d0433"
        },
        "date": 1731235388967,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordPar",
            "value": 11.071521132010087,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeq",
            "value": 7.8896704436517595,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 604.0807723734076,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 592.7022355309497,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 309.47239944,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 546.16283116,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 547.76325505125,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 540.5207090174679,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 613.2480903035676,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 530.6895972021954,
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
            "name": "zio",
            "username": "zio"
          },
          "committer": {
            "name": "zio",
            "username": "zio"
          },
          "id": "07dd08a49a43fe498d0182388db480332c0964df",
          "message": "Tests for PartitionStreamControl + fix issue with interruptionPromise",
          "timestamp": "2024-11-10T10:27:24Z",
          "url": "https://github.com/zio/zio-kafka/pull/1371/commits/07dd08a49a43fe498d0182388db480332c0964df"
        },
        "date": 1731238085533,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordPar",
            "value": 16.27744799939741,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeq",
            "value": 12.6023603382352,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 603.0441198310615,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 592.4290030242457,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 154.73040734738098,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 436.89607853666666,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 544.0199815204808,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 532.8530996145832,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 558.4697886152433,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 552.4817370300768,
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
          "id": "642950aa5543455102e917e57ab5ced865b9480b",
          "message": "Tests for PartitionStreamControl + fix issue with interruptionPromise",
          "timestamp": "2024-11-10T10:27:24Z",
          "url": "https://github.com/zio/zio-kafka/pull/1371/commits/642950aa5543455102e917e57ab5ced865b9480b"
        },
        "date": 1731239505721,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordPar",
            "value": 10.149333278065429,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeq",
            "value": 8.37383865165579,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 601.1128932932589,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 590.925639103352,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 255.77335407799998,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 587.7328970066666,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 541.254926979904,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 532.2992119197116,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 548.2063464989187,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 580.0479548912404,
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
            "name": "zio",
            "username": "zio"
          },
          "committer": {
            "name": "zio",
            "username": "zio"
          },
          "id": "7d8443814af9fe8be4abd10dda1a01abae2c11b1",
          "message": "Fix automatic pruning of benchmark history",
          "timestamp": "2024-11-10T11:56:19Z",
          "url": "https://github.com/zio/zio-kafka/pull/1372/commits/7d8443814af9fe8be4abd10dda1a01abae2c11b1"
        },
        "date": 1731241875804,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordPar",
            "value": 20.17278510778133,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeq",
            "value": 15.068305944480544,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 600.8102015051026,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 589.5013757890503,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 125.36250035288889,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 346.47141472999994,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 540.9387716693269,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 531.764300331891,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 546.0558266844324,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 588.8324603207022,
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
            "name": "zio",
            "username": "zio"
          },
          "committer": {
            "name": "zio",
            "username": "zio"
          },
          "id": "b4307cef97786ee4ef3785358e12b430d50ea950",
          "message": "Fix automatic pruning of benchmark history (attempt 2)",
          "timestamp": "2024-11-10T12:32:36Z",
          "url": "https://github.com/zio/zio-kafka/pull/1373/commits/b4307cef97786ee4ef3785358e12b430d50ea950"
        },
        "date": 1731244978495,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordPar",
            "value": 17.450573758676274,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeq",
            "value": 13.40873721796923,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 601.6690790978026,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 590.6777866239106,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 144.08641676142852,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 362.3735797613333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 542.2048927228847,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 532.9369878889422,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 583.4828299118918,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 537.1046133437542,
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
            "name": "zio",
            "username": "zio"
          },
          "committer": {
            "name": "zio",
            "username": "zio"
          },
          "id": "d3e0270f67f19fed7374c77474028ec6bae5fa5a",
          "message": "More logging around rebalancing when rebalanceSafeCommits is true",
          "timestamp": "2024-11-10T13:24:55Z",
          "url": "https://github.com/zio/zio-kafka/pull/1360/commits/d3e0270f67f19fed7374c77474028ec6bae5fa5a"
        },
        "date": 1731257758665,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordPar",
            "value": 11.49334292654747,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeq",
            "value": 8.376746257165534,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 603.1220797503166,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 592.6442418785475,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 242.85363101809526,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 472.3906820933333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 542.6353488895192,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 537.4558570895833,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 583.9452606184864,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 576.4926534623053,
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
          "id": "5917c1d96df55ca1d30e3d8c341cdc1f6d502f17",
          "message": "Extract Committer and RunloopRebalanceListener classes from Runloop",
          "timestamp": "2024-11-10T13:24:55Z",
          "url": "https://github.com/zio/zio-kafka/pull/1375/commits/5917c1d96df55ca1d30e3d8c341cdc1f6d502f17"
        },
        "date": 1731258936379,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordPar",
            "value": 21.721706950139787,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeq",
            "value": 16.518578972801727,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 600.6379330281566,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 588.9511050658099,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 110.63063882311113,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 312.19883336466665,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 540.3991758964423,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 539.478580751282,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 565.251759061946,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 645.8295372169484,
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
          "id": "6ad6eaf722a83795508dd6abe6397d3b5eae404e",
          "message": "Extract Committer and RunloopRebalanceListener classes from Runloop",
          "timestamp": "2024-11-10T13:24:55Z",
          "url": "https://github.com/zio/zio-kafka/pull/1375/commits/6ad6eaf722a83795508dd6abe6397d3b5eae404e"
        },
        "date": 1731262496720,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordPar",
            "value": 13.365503232904251,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeq",
            "value": 10.321198922173268,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 601.1568413559776,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 591.3624904773186,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 186.41443536290478,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 495.2000617699999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 543.2516673078846,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 533.9632642080127,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 501.4221535096216,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 530.1042425139846,
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
          "id": "a71e0289cc48912459172182d3df3e64f8c86eaf",
          "message": "Brag about being faster than raw java kafka",
          "timestamp": "2024-11-10T13:24:55Z",
          "url": "https://github.com/zio/zio-kafka/pull/1374/commits/a71e0289cc48912459172182d3df3e64f8c86eaf"
        },
        "date": 1731265492780,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordPar",
            "value": 17.124974721297825,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeq",
            "value": 13.262642674849655,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 602.2673542760521,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 591.1651649540783,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 145.6403618292857,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 423.03594150666663,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 545.306982003077,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 537.6721039866986,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 610.8626864215136,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 523.3212404583974,
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
          "id": "8a0222ed1be5b195322f5576c394e3e7ae4250a8",
          "message": "Decouple stream halt detection timeout from max poll interval",
          "timestamp": "2024-11-10T13:24:55Z",
          "url": "https://github.com/zio/zio-kafka/pull/1376/commits/8a0222ed1be5b195322f5576c394e3e7ae4250a8"
        },
        "date": 1731268857866,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordPar",
            "value": 13.287320132112061,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeq",
            "value": 10.370669665381637,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 603.8030641009311,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 593.3014670934078,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 208.32155386799997,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 492.9152378533332,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 547.1043844900962,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 537.4723747516026,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 574.9826052895135,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 563.3978649443687,
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
            "name": "zio",
            "username": "zio"
          },
          "committer": {
            "name": "zio",
            "username": "zio"
          },
          "id": "d67a2d9e8106937f7f98e9a824f7c153dcd65b71",
          "message": "Require a semaphore in Consumer.fromJavaConsumer",
          "timestamp": "2024-11-10T19:42:53Z",
          "url": "https://github.com/zio/zio-kafka/pull/1368/commits/d67a2d9e8106937f7f98e9a824f7c153dcd65b71"
        },
        "date": 1731269028463,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordPar",
            "value": 14.894225205662783,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeq",
            "value": 10.764031476491837,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 603.425934732514,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 591.1928549439107,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 190.93947645409528,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 487.97690103333343,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 542.1682783721152,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 534.3223893025641,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 586.5391961383784,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 555.784671844215,
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
          "id": "02a36ee049ebc891f1ce5b0f6d40c2469ab68781",
          "message": "Extract Committer and RunloopRebalanceListener classes from Runloop",
          "timestamp": "2024-11-10T19:42:53Z",
          "url": "https://github.com/zio/zio-kafka/pull/1375/commits/02a36ee049ebc891f1ce5b0f6d40c2469ab68781"
        },
        "date": 1731270201821,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordPar",
            "value": 11.185988216838542,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeq",
            "value": 10.763460781307087,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 602.1030509653633,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 591.3552829626814,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 235.65021331057142,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 538.2725688466667,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 542.8011929589422,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 535.2987224285255,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 535.7157823482163,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 587.8931586084742,
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
            "name": "zio",
            "username": "zio"
          },
          "committer": {
            "name": "zio",
            "username": "zio"
          },
          "id": "0390c2ca0090f22683634d5d21cdb17865399cf0",
          "message": "Decouple stream halt detection timeout from max poll interval",
          "timestamp": "2024-11-10T20:07:08Z",
          "url": "https://github.com/zio/zio-kafka/pull/1376/commits/0390c2ca0090f22683634d5d21cdb17865399cf0"
        },
        "date": 1731270502581,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordPar",
            "value": 12.309364594349573,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeq",
            "value": 7.925550938037976,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 602.5045401909125,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 591.7453506005588,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 273.250775684,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 653.3228147466667,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 546.1481219659615,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 536.5249837366987,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 594.4152833331892,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 550.941221782832,
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
          "id": "2c4fee2da484b730e3a3dd1d86371ee8f584c648",
          "message": "Bump scala-steward-org/scala-steward-action from 2.70.0 to 2.71.0",
          "timestamp": "2024-11-10T20:07:08Z",
          "url": "https://github.com/zio/zio-kafka/pull/1377/commits/2c4fee2da484b730e3a3dd1d86371ee8f584c648"
        },
        "date": 1731303593799,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordPar",
            "value": 20.640091444633516,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeq",
            "value": 14.782617374627605,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 600.9662932891622,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 589.2346798303912,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 122.9456802111111,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 356.41848317666665,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 543.1424153946155,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 535.3982463522436,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 540.5629438864864,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 501.1281711279913,
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
            "name": "zio",
            "username": "zio"
          },
          "committer": {
            "name": "zio",
            "username": "zio"
          },
          "id": "769a1a35602dd437ee3ed66eb5f9ebb7c2126bb1",
          "message": "Rename Serde methods for consistency with ZIO 2",
          "timestamp": "2024-11-11T06:38:44Z",
          "url": "https://github.com/zio/zio-kafka/pull/1378/commits/769a1a35602dd437ee3ed66eb5f9ebb7c2126bb1"
        },
        "date": 1731428780497,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordPar",
            "value": 13.19465984085843,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeq",
            "value": 11.148527532890357,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 603.0433229041341,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 591.0945113993296,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 205.27657967795236,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 520.23786056,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 546.4866792636539,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 532.6739408894231,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 576.4957396002162,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 592.5475164320527,
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
            "name": "zio",
            "username": "zio"
          },
          "committer": {
            "name": "zio",
            "username": "zio"
          },
          "id": "e5bb7f6b5aae704eeedc5164dd081e990819c403",
          "message": "Fix deprecation notice version on Consumer.fromJavaConsumer",
          "timestamp": "2024-11-12T16:15:57Z",
          "url": "https://github.com/zio/zio-kafka/pull/1379/commits/e5bb7f6b5aae704eeedc5164dd081e990819c403"
        },
        "date": 1731429576839,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordPar",
            "value": 12.108441969644161,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeq",
            "value": 9.550056604680025,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 603.1094713591806,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 592.6696336377653,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 208.00908747295233,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 564.0028872533334,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 546.3675467866346,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 535.3666282294872,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 623.3646053393514,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 588.2259260784195,
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
            "name": "zio",
            "username": "zio"
          },
          "committer": {
            "name": "zio",
            "username": "zio"
          },
          "id": "88840cca8586a0fa74a1876a0b0b481eb994e6f2",
          "message": "Decouple stream halt detection timeout from max poll interval",
          "timestamp": "2024-11-12T16:53:08Z",
          "url": "https://github.com/zio/zio-kafka/pull/1376/commits/88840cca8586a0fa74a1876a0b0b481eb994e6f2"
        },
        "date": 1731432576640,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordPar",
            "value": 17.89325012540831,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeq",
            "value": 14.43281464303278,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 601.5471761829423,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 591.0065183227933,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 175.66129096190477,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 498.3057863666667,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 541.9355782108654,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 534.6155938919871,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 556.3966840115676,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 538.232929781427,
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
          "id": "fae6d50f0a10ed702f91e5dec1f171793b6ada5f",
          "message": "Decouple stream halt detection timeout from max poll interval",
          "timestamp": "2024-11-12T16:53:08Z",
          "url": "https://github.com/zio/zio-kafka/pull/1376/commits/fae6d50f0a10ed702f91e5dec1f171793b6ada5f"
        },
        "date": 1731442671936,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordPar",
            "value": 15.791584834928628,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeq",
            "value": 12.50011845591273,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 601.4028631286407,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 590.2520413835755,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 159.9463058121429,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 446.0507569200001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 543.6613663249038,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 537.5737569086538,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 561.8873777302704,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 561.2851558744237,
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
          "id": "810362b63d0555ed9c826e946a5f61974025b882",
          "message": "Extract Committer and RunloopRebalanceListener classes from Runloop",
          "timestamp": "2024-11-12T16:53:08Z",
          "url": "https://github.com/zio/zio-kafka/pull/1375/commits/810362b63d0555ed9c826e946a5f61974025b882"
        },
        "date": 1731444505646,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordPar",
            "value": 20.050822040411337,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeq",
            "value": 15.240720173704881,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 602.2435566443203,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 590.9675291833519,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 121.65802917444444,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 343.86120122333335,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 564.8395546073078,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 535.7221272586538,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 537.727926205946,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 557.53574067618,
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
          "id": "dee9aaf82eac09e38bef50f774f44cc342c1bcdc",
          "message": "Decouple stream halt detection timeout from max poll interval",
          "timestamp": "2024-11-12T16:53:08Z",
          "url": "https://github.com/zio/zio-kafka/pull/1376/commits/dee9aaf82eac09e38bef50f774f44cc342c1bcdc"
        },
        "date": 1731488839018,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordPar",
            "value": 14.342036680178676,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeq",
            "value": 11.397481928501582,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 601.8802361245436,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 590.4466870464805,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 182.82604706266665,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 477.6546863,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 543.1483934069231,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 533.4348798774038,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 561.8857457763244,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 538.7332365140725,
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
            "name": "zio",
            "username": "zio"
          },
          "committer": {
            "name": "zio",
            "username": "zio"
          },
          "id": "2cd592d80c8febd1eca342d473611d62aef67d06",
          "message": "Use same amount of messages for all consumer benchmarks",
          "timestamp": "2024-11-13T10:04:54Z",
          "url": "https://github.com/zio/zio-kafka/pull/1382/commits/2cd592d80c8febd1eca342d473611d62aef67d06"
        },
        "date": 1731495808818,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordPar",
            "value": 12.635409039175181,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeq",
            "value": 10.922596071580235,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 537.38742716,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 537.06127754,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 181.31568258323813,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 486.7926981666666,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 511.7199011000001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 507.97440466000006,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 509.97416884,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 516.0193421199999,
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
          "id": "e86d757375479ac1fe7d4ff1689e3adfcbc90193",
          "message": "Extract Committer and RebalanceCoordinator classes from Runloop + unit tests",
          "timestamp": "2024-11-13T10:04:54Z",
          "url": "https://github.com/zio/zio-kafka/pull/1375/commits/e86d757375479ac1fe7d4ff1689e3adfcbc90193"
        },
        "date": 1731502990687,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordPar",
            "value": 17.00754051050015,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeq",
            "value": 13.004962476588819,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 604.4004443731097,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 595.6367808338548,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 152.67174843914282,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 420.4374996066667,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 547.2132093303845,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 537.4262629129806,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 574.8991423942703,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 571.0890781130187,
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
          "id": "a323d34d7fcd02140f83be52ecc7303cd172fdb7",
          "message": "Formatting",
          "timestamp": "2024-11-13T10:04:54Z",
          "url": "https://github.com/zio/zio-kafka/pull/1383/commits/a323d34d7fcd02140f83be52ecc7303cd172fdb7"
        },
        "date": 1731503005741,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordPar",
            "value": 19.61418140094142,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeq",
            "value": 14.339936170307562,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 603.8068880901303,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 591.6644824527375,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 124.01117169666666,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 356.6322013073334,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 545.0323968580768,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 537.8416403767627,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 565.1257850623782,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 559.917156791921,
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
            "name": "zio",
            "username": "zio"
          },
          "committer": {
            "name": "zio",
            "username": "zio"
          },
          "id": "631059faa4d26ca11db168f06d2eb5ae80d21b2e",
          "message": "Disable mima check for new minor releases",
          "timestamp": "2024-11-13T13:51:01Z",
          "url": "https://github.com/zio/zio-kafka/pull/1384/commits/631059faa4d26ca11db168f06d2eb5ae80d21b2e"
        },
        "date": 1731509938335,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordPar",
            "value": 15.64638793247844,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeq",
            "value": 11.932924357281825,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 604.0595643506146,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 593.8214431786591,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 171.60894934133336,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 428.8005793933333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 545.6585411399038,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 535.710127140064,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 556.8675883855136,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 581.1868777282107,
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
            "name": "zio",
            "username": "zio"
          },
          "committer": {
            "name": "zio",
            "username": "zio"
          },
          "id": "1dde4e9170e46ebeddee32b26d33be03d4abde20",
          "message": "Update README.md",
          "timestamp": "2024-11-13T17:08:41Z",
          "url": "https://github.com/zio/zio-kafka/pull/1385/commits/1dde4e9170e46ebeddee32b26d33be03d4abde20"
        },
        "date": 1731519087953,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordPar",
            "value": 13.762270837777278,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeq",
            "value": 11.275241761822292,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 602.7274106130353,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 591.87664962581,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 182.39723210476185,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 498.8985196333334,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 549.7588519291346,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 554.0129122562499,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 574.2527196129729,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 532.3744409751042,
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
          "id": "ea59926c427fdc37611792b1a6508fd2bd61d0c6",
          "message": "Extract Committer and RebalanceCoordinator classes from Runloop + unit tests",
          "timestamp": "2024-11-13T17:10:50Z",
          "url": "https://github.com/zio/zio-kafka/pull/1375/commits/ea59926c427fdc37611792b1a6508fd2bd61d0c6"
        },
        "date": 1731521399101,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordPar",
            "value": 12.874194986933537,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeq",
            "value": 10.229109871025699,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 603.8840881889759,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 591.7128772086033,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 235.5692432569524,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 492.7667318400001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 545.9550770947117,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 540.3670458544872,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 577.2759065662702,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 574.063291412865,
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
          "id": "6847651f1ec649b19f8bf757b73b7f92d23598da",
          "message": "Update zio-logging-slf4j, ... to 2.4.0",
          "timestamp": "2024-11-13T17:10:50Z",
          "url": "https://github.com/zio/zio-kafka/pull/1387/commits/6847651f1ec649b19f8bf757b73b7f92d23598da"
        },
        "date": 1731549372872,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordPar",
            "value": 14.033971438775975,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeq",
            "value": 11.404397455527628,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 602.6537867753444,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 591.0730945938548,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 149.2855427452222,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 468.9285395733332,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 567.47276225625,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 534.3470293791665,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 508.33090075199976,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 554.6293218611636,
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
            "name": "zio",
            "username": "zio"
          },
          "committer": {
            "name": "zio",
            "username": "zio"
          },
          "id": "1859be6229f2c6925b44bcbbb2c07cb2f7e41d2d",
          "message": "Use same amount of messages for all consumer benchmarks",
          "timestamp": "2024-11-14T08:28:51Z",
          "url": "https://github.com/zio/zio-kafka/pull/1382/commits/1859be6229f2c6925b44bcbbb2c07cb2f7e41d2d"
        },
        "date": 1731604965678,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordPar",
            "value": 14.433217969126288,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeq",
            "value": 11.281776306983446,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 546.7067613999999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 541.20900424,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 189.73266722038096,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 513.65031028,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 514.31709656,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 509.5301411599999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 513.69211906,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 520.7798761933333,
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
          "id": "5e2031e7b3fa09d4cac1cde726716a06dc08804c",
          "message": "Graceful shutdown of a stream for a single subscription",
          "timestamp": "2024-11-14T08:28:51Z",
          "url": "https://github.com/zio/zio-kafka/pull/1201/commits/5e2031e7b3fa09d4cac1cde726716a06dc08804c"
        },
        "date": 1731750753970,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordPar",
            "value": 14.66501407565104,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeq",
            "value": 11.158943708074869,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 603.9131694973557,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 592.5212034925139,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 190.06213269599996,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 458.50074323,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 545.4922211275,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 534.8455924668268,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 554.7588131944863,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 584.109151910955,
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
          "id": "5e37292c2f152e744a22c50da7b4e5aecbafd9aa",
          "message": "Prevent unlimited enqueueing of CommitAvailable commands",
          "timestamp": "2024-11-16T10:04:04Z",
          "url": "https://github.com/zio/zio-kafka/pull/1390/commits/5e37292c2f152e744a22c50da7b4e5aecbafd9aa"
        },
        "date": 1731753397936,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordPar",
            "value": 15.570054568691694,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeq",
            "value": 13.170598735077848,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 603.1414007665549,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 589.1506935357542,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 187.86401846304764,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 506.61857874666674,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 542.0719300532692,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 534.9355996150642,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 552.7891511536217,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 570.5033095267618,
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
          "id": "299e64c3c063d5971c02dd291ddad185215b418c",
          "message": "Make command and commit queues bounded",
          "timestamp": "2024-11-16T10:04:04Z",
          "url": "https://github.com/zio/zio-kafka/pull/1391/commits/299e64c3c063d5971c02dd291ddad185215b418c"
        },
        "date": 1731754506285,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordPar",
            "value": 14.367929020446162,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeq",
            "value": 11.601445511107695,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 602.2147308440225,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 591.1872635556424,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 161.20975916123808,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 463.3224406266667,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 543.3598414009615,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 536.3232741956729,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 500.3425364568648,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 547.4881315486281,
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
          "id": "4072daeddf528510cd10c01245a20d277af7c936",
          "message": "Extract Committer and RebalanceCoordinator classes from Runloop + unit tests",
          "timestamp": "2024-11-16T10:04:04Z",
          "url": "https://github.com/zio/zio-kafka/pull/1375/commits/4072daeddf528510cd10c01245a20d277af7c936"
        },
        "date": 1731755261158,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordPar",
            "value": 17.206208701149457,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeq",
            "value": 13.518722157411153,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 600.0754429271508,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 590.0389104072625,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 149.66875042619048,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 394.4184062999999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 541.4331248562501,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 531.9284760905449,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 615.5185435615135,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 598.3410008368825,
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
          "id": "454ded2fe360071af4d666d09b414b1f2d96816a",
          "message": "Run profiler only on release and on demand",
          "timestamp": "2024-11-16T10:04:04Z",
          "url": "https://github.com/zio/zio-kafka/pull/1392/commits/454ded2fe360071af4d666d09b414b1f2d96816a"
        },
        "date": 1731755548019,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordPar",
            "value": 16.148906760239488,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeq",
            "value": 12.625847851830509,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 601.3295589451023,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 590.0482355948602,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 165.3032352494603,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 465.1456951733334,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 546.4644476253845,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 535.1568652237179,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 525.3279263198917,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 590.6571581634246,
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
            "name": "zio",
            "username": "zio"
          },
          "committer": {
            "name": "zio",
            "username": "zio"
          },
          "id": "aaffb20fdfbde61116619272d276cb1a75d0d7a7",
          "message": "Use same amount of messages for all consumer benchmarks",
          "timestamp": "2024-11-16T11:27:59Z",
          "url": "https://github.com/zio/zio-kafka/pull/1382/commits/aaffb20fdfbde61116619272d276cb1a75d0d7a7"
        },
        "date": 1731758207721,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordPar",
            "value": 14.223304278832865,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeq",
            "value": 10.474371681851212,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 602.8819088,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 589.66926826,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 200.80145454609521,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 498.1859775933333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 543.4830694999999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 534.65314396,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 557.6220411200001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 565.9356379,
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
          "id": "ba86c5e031bf0a759546941886c61fa799ef8c26",
          "message": "Run profiler only on release and on demand",
          "timestamp": "2024-11-16T11:27:59Z",
          "url": "https://github.com/zio/zio-kafka/pull/1392/commits/ba86c5e031bf0a759546941886c61fa799ef8c26"
        },
        "date": 1731760837825,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordPar",
            "value": 14.323517237366074,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeq",
            "value": 10.851935484083235,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 601.9244748512477,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 590.1146745602235,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 183.6647631545714,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 518.9458305066667,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 543.0721901444232,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 537.3465676496795,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 524.5626993564325,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 526.5386244343358,
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
            "name": "zio",
            "username": "zio"
          },
          "committer": {
            "name": "zio",
            "username": "zio"
          },
          "id": "4c02d08e7f434d3eadba844dd9c4505165de40f3",
          "message": "Graceful shutdown of a stream for a single subscription",
          "timestamp": "2024-11-16T13:28:24Z",
          "url": "https://github.com/zio/zio-kafka/pull/1201/commits/4c02d08e7f434d3eadba844dd9c4505165de40f3"
        },
        "date": 1731766164112,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordPar",
            "value": 17.222122546261176,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeq",
            "value": 13.173915501552546,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 604.9303150424581,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 592.9942370310614,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 155.5251808354762,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 388.38059747999995,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 753.5243998736539,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 534.5457048357372,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 588.4436235755675,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 553.7290419110427,
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
          "id": "0bf5cd26b037a3c89200a6b35fc3060eb9f47bca",
          "message": "[Internal] Micro-optimisation: Reuse previously calculated `java.langSystem.nanoTime`",
          "timestamp": "2024-11-16T13:28:24Z",
          "url": "https://github.com/zio/zio-kafka/pull/1395/commits/0bf5cd26b037a3c89200a6b35fc3060eb9f47bca"
        },
        "date": 1731809301536,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordPar",
            "value": 13.472397263083035,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeq",
            "value": 10.433094557005598,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 603.0931724726256,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 591.7035586243576,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 188.309182582,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 541.6292619866667,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 544.6950297572115,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 531.7828308628204,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 546.0639406277837,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 559.5169986720528,
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
          "id": "ff245d468af059b56f37093042a73d030b61cb1b",
          "message": "[Internal] Micro-optimisation: Compute metrics only when needed && Avid instantiations of `Duration` by manipulating `Long` instead",
          "timestamp": "2024-11-16T13:28:24Z",
          "url": "https://github.com/zio/zio-kafka/pull/1396/commits/ff245d468af059b56f37093042a73d030b61cb1b"
        },
        "date": 1731809621128,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordPar",
            "value": 16.530756936628926,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeq",
            "value": 12.846017033317047,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 601.7220151214152,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 590.8818548738546,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 165.6687487047619,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 471.38303053999994,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 545.4718120374039,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 536.9787260676281,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 570.2945878245404,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 525.5185178699451,
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
            "name": "zio",
            "username": "zio"
          },
          "committer": {
            "name": "zio",
            "username": "zio"
          },
          "id": "49c5dc76e2f375922748d6f73d831e49c4e51f93",
          "message": "Update embedded-kafka to 3.9.0",
          "timestamp": "2024-11-19T10:12:33Z",
          "url": "https://github.com/zio/zio-kafka/pull/1398/commits/49c5dc76e2f375922748d6f73d831e49c4e51f93"
        },
        "date": 1732068041807,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordPar",
            "value": 12.670787350484465,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeq",
            "value": 10.404056440004345,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 602.9476448581006,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 592.1462594235754,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 205.24570732133333,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 566.9356119066666,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 542.5360308305769,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 534.5046562233974,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 558.4278647983783,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 527.1878370493962,
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
          "id": "12b7e4acec2dd150cc51c8309f70fc54eca76076",
          "message": "Update embedded-kafka to 3.9.0",
          "timestamp": "2024-11-19T10:12:33Z",
          "url": "https://github.com/zio/zio-kafka/pull/1398/commits/12b7e4acec2dd150cc51c8309f70fc54eca76076"
        },
        "date": 1732087470977,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordPar",
            "value": 16.260207880452416,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeq",
            "value": 12.032908995718664,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 602.6908658499069,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 591.8638953395531,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 169.35526327809524,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 419.4914527666668,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 544.240343573077,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 535.5692441325322,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 516.4070447610811,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 604.2317994783754,
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
            "name": "zio",
            "username": "zio"
          },
          "committer": {
            "name": "zio",
            "username": "zio"
          },
          "id": "880321433eca0e0bd81d832f5a7be9013ddbc8ad",
          "message": "Update zio, zio-streams, zio-test, ... to 2.1.13",
          "timestamp": "2024-11-20T07:16:49Z",
          "url": "https://github.com/zio/zio-kafka/pull/1399/commits/880321433eca0e0bd81d832f5a7be9013ddbc8ad"
        },
        "date": 1732154471757,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordPar",
            "value": 12.803517802691513,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeq",
            "value": 10.612669063135527,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 602.7694429256611,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 591.702464008715,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 162.2154400942857,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 449.34668056000004,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 557.4605372788462,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 533.1939186895834,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 532.1419910766485,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 598.250253640395,
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
            "name": "zio",
            "username": "zio"
          },
          "committer": {
            "name": "zio",
            "username": "zio"
          },
          "id": "2d450617e2686190b0e178c42c99d723a38af714",
          "message": "Revert \"Update zio, zio-streams, zio-test, ... to 2.1.13\"",
          "timestamp": "2024-11-21T08:03:12Z",
          "url": "https://github.com/zio/zio-kafka/pull/1400/commits/2d450617e2686190b0e178c42c99d723a38af714"
        },
        "date": 1732190892016,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordPar",
            "value": 13.308865976628935,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeq",
            "value": 9.87698369761147,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 601.325251988231,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 590.6073086522905,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 221.84269622799994,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 550.6868385533332,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 543.38490894375,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 533.9002132919871,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 552.346725491027,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 568.1730004798244,
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
          "id": "0d2cae65c6261914f219c822384d48a80de78ec7",
          "message": "Use same amount of messages for all consumer benchmarks",
          "timestamp": "2024-11-21T08:03:12Z",
          "url": "https://github.com/zio/zio-kafka/pull/1382/commits/0d2cae65c6261914f219c822384d48a80de78ec7"
        },
        "date": 1732303259480,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordPar",
            "value": 12.28337841690287,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeq",
            "value": 10.676181579222792,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 601.72424026,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 593.6042639799999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 206.66000926999993,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 555.97020328,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 543.0625163399999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 535.9328707199999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 558.38713788,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 570.39100514,
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
            "name": "zio",
            "username": "zio"
          },
          "committer": {
            "name": "zio",
            "username": "zio"
          },
          "id": "b6c9a26183d8e21d5876c1a3df53b3d18b2088bf",
          "message": "Extract Committer and RebalanceCoordinator classes from Runloop + unit tests",
          "timestamp": "2024-11-23T11:34:40Z",
          "url": "https://github.com/zio/zio-kafka/pull/1375/commits/b6c9a26183d8e21d5876c1a3df53b3d18b2088bf"
        },
        "date": 1732364924761,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordPar",
            "value": 13.168215289743012,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeq",
            "value": 10.149771537993104,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 598.4766508600001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 592.9177940799999,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 247.202188328,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 587.5393167866666,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 541.21656748,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 535.1231834199998,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 560.85008336,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 567.8230661399998,
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
          "id": "c517f46293766d7f24fa3d319d9a995c229c9286",
          "message": "Extract Committer and RebalanceCoordinator classes from Runloop + unit tests",
          "timestamp": "2024-11-23T11:34:40Z",
          "url": "https://github.com/zio/zio-kafka/pull/1375/commits/c517f46293766d7f24fa3d319d9a995c229c9286"
        },
        "date": 1732464582619,
        "tool": "jmh",
        "benches": [
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordPar",
            "value": 13.493423687529885,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeq",
            "value": 10.52265692337813,
            "unit": "ops/s",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput",
            "value": 601.6834485200001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits",
            "value": 590.33905138,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar",
            "value": 194.11009585523811,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq",
            "value": 516.4001426666666,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients",
            "value": 542.7244969800001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients",
            "value": 535.9362500200001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka",
            "value": 561.3965026800001,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          },
          {
            "name": "zio.kafka.bench.comparison.ZioKafkaBenchmarks.zioKafka",
            "value": 569.3761562400002,
            "unit": "ms/op",
            "extra": "iterations: 5\nforks: 5\nthreads: 1"
          }
        ]
      }
    ]
  }
}