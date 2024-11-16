# Comparison Benchmarks

## Results

The benchmark are run from a GitHub action on every commit. The results are published on https://zio.github.io/zio-kafka/dev/bench/.

The results are automatically pruned by [a scala script](https://github.com/zio/zio-kafka/blob/gh-pages/scripts/prune-benchmark-history.sc) on the `gh-pages` branch.

## Interpreting the benchmarks

To do!

## How to run the benchmarks

To run these "comparison" benchmarks, in a sbt console, run:
```scala
clean
Test/compile
zioKafkaBench/Jmh/run -wi 10 -i 10 -r 1 -w 1 -t 1 -f 5 -foe true .*comparison.*
```

The `.*comparison.*` part is the selector telling to JMH which benchmarks to run.
Here, we're only selecting the ones living in the `comparison` package.

## Tuning JMH runs

To list all possible options and understand these configurations, see run `sbt "zioKafkaBench/Jmh/run -h"`

Used options meaning:
 - "-wi 10": 10 warmup iterations
 - "-i 10": 10 benchmark iterations
 - "-r 1": Minimum time to spend at each measurement iteration. 1 second
 - "-w 1": Minimum time to spend at each warmup iteration. 1 second
 - "-t 1": Number of worker threads to run with. 1 thread
 - "-f 5": How many times to fork a single benchmark. 5 forks
 - "-foe true": Should JMH fail immediately if any benchmark had experienced an unrecoverable error?. True