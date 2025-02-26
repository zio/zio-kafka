# Benchmarks and Flame graphs

## Benchmark results

The benchmarks are run from a GitHub action on every commit. The results are published
on https://zio.github.io/zio-kafka/dev/bench/.

The results are automatically pruned by [a scala script](https://github.com/zio/zio-kafka/blob/gh-pages/scripts/prune-benchmark-history.sc) on the `gh-pages` branch.

## Flame graphs results

Flame graphs are created by the `profile` GitHub action which runs on every release (it can also be started manually).
The results are published on https://zio.github.io/zio-kafka/dev/profile/.

The results are automatically pruned by [a scala script](https://github.com/zio/zio-kafka/blob/gh-pages/scripts/prune-flame-graph.sc) on the `gh-pages` branch.

# Benchmark descriptions

## The consumer benchmarks

When comparing the zio-kafka benchmarks against the regular Kafka clients, keep in mind that these benchmarks represent
the worst possible case for zio-kafka. This is because these consumers only count the received records, there is no
processing. This makes the comparison look bad for zio-kafka because zio-kafka programs normally process records in
parallel, while other Kafka consumers process records serially.

All benchmarks use [embedded-kafka](https://github.com/embeddedkafka/embedded-kafka). This means that the network
overhead of reaching out to Kafka is almost zero. Because of that, an extra network call has no impact on these
benchmarks, even though it does impact real applications. To make extra network calls visible, we need to simulate the
network.<br>
Because there is no easy way to simulate network behavior within the JVM, we add an
[artificial delay of 5ms](//zio-kafka-bench/src/main/scala/zio/kafka/bench/SlowKafkaConsumer.scala)
to each call to the underlying java consumer (also in the non-zio benchmarks), except when we know the call does not
result in network traffic. This is only a crude approximation since the calls of the underlying java consumer do not correspond
1:1 to network traffic. Nevertheless, we expect that a change in the number of consumer calls will become visible in
the benchmark results.

All consumer benchmarks send 50k ~512 byte records per run.

#### zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput

Uses zio-kafka's `plainStream` with a topic subscription. The offsets of the consumed records are _not_ committed.

#### zio.kafka.bench.ZioKafkaConsumerBenchmark.throughputWithCommits

Same as above, but now the offsets of the consumed records are committed.

#### zio.kafka.bench.comparison.KafkaClientBenchmarks.kafkaClients

The simplest possible Kafka client that subscribes to a topic. It directly calls the poll method in a tight loop.

#### zio.kafka.bench.comparison.KafkaClientBenchmarks.manualKafkaClients

Same as above, but now using partition assignment instead of topic subscription.

#### zio.kafka.bench.comparison.ZioKafkaBenchmarks.manualZioKafka

Does the same as `zio.kafka.bench.ZioKafkaConsumerBenchmark.throughput`, but uses a partition assignment instead of a
topic subscription.

## The producer benchmarks

#### zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkSeq

Sequentially produces 30 batches, where each batch contains 500 small records.

#### zio.kafka.bench.ZioKafkaProducerBenchmark.produceChunkPar

Produces the same batches as the above, but from 4 fibers.

#### zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordSeq

Sequentially produces 100 small records.

#### zio.kafka.bench.ZioKafkaProducerBenchmark.produceSingleRecordPar

Produces 100 small records from 4 fibers.

# How to run the benchmarks

To run these "comparison" benchmarks, in a sbt console, run:

```scala
clean
Test/compile
zioKafkaBench/Jmh/run -wi 10 -i 10 -r 1 -w 1 -t 1 -f 5 -foe true .*comparison.*
```

The `.*comparison.*` part is the selector telling to JMH which benchmarks to run.
Here, we're only selecting the ones living in the `comparison` package.

# Tuning JMH runs

To list all possible options and understand these configurations, see run `sbt "zioKafkaBench/Jmh/run -h"`

Used options meaning:

 - "-wi 10": 10 warmup iterations
 - "-i 10": 10 benchmark iterations
 - "-r 1": Minimum time to spend at each measurement iteration. 1 second
 - "-w 1": Minimum time to spend at each warmup iteration. 1 second
 - "-t 1": Number of worker threads to run with. 1 thread
 - "-f 5": How many times to fork a single benchmark. 5 forks
 - "-foe true": Should JMH fail immediately if any benchmark had experienced an unrecoverable error?. True