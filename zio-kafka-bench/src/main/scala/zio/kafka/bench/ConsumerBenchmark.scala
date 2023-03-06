package zio.kafka.bench

import io.github.embeddedkafka.EmbeddedKafka
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.openjdk.jmh.annotations.{ Benchmark, BenchmarkMode, Mode }
import org.openjdk.jmh.runner.{ Runner, RunnerException }
import org.openjdk.jmh.runner.options.OptionsBuilder
import zio.kafka.KafkaTestUtils.{ consumer, produceMany, producer }
import zio.kafka.consumer.{ Consumer, Subscription }
import zio.kafka.embedded.Kafka
import zio.kafka.serde.Serde
import zio.{ Task, Unsafe, ZIO }

import java.util.UUID

// Copied from https://raw.githubusercontent.com/openjdk/jmh/master/jmh-samples/src/main/java/org/openjdk/jmh/samples/JMHSample_01_HelloWorld.java
// TODO Jules: TO DELETE when first real benchmark is added

/*
 * Copyright (c) 2014, Oracle America, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 *  * Neither the name of Oracle nor the names of its contributors may be used
 *    to endorse or promote products derived from this software without
 *    specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */

object ConsumerBenchmark {
  /*
   * ============================== HOW TO RUN THIS TEST: ====================================
   *
   * You are expected to see the run with large number of iterations, and
   * very large throughput numbers. You can see that as the estimate of the
   * harness overheads per method call. In most of our measurements, it is
   * down to several cycles per call.
   *
   * a) Via command-line:
   *    $ mvn clean install
   *    $ java -jar target/benchmarks.jar JMHSample_01
   *
   * JMH generates self-contained JARs, bundling JMH together with it.
   * The runtime options for the JMH are available with "-h":
   *    $ java -jar target/benchmarks.jar -h
   *
   * b) Via the Java API:
   *    (see the JMH homepage for possible caveats when running from IDE:
   *      http://openjdk.java.net/projects/code-tools/jmh/)
   */
  @throws[RunnerException]
  def main(args: Array[String]): Unit = {
    val opt = new OptionsBuilder().include(classOf[ConsumerBenchmark].getSimpleName).forks(1).build
    new Runner(opt).run
    ()
  }
}

class ConsumerBenchmark {
  @Benchmark
  @BenchmarkMode(Array(Mode.AverageTime))
  def throughput(): Unit = {
    def program = {

      val nrPartitions = 6
      val nrMessages   = 50000
      val kvs          = (1 to nrMessages).toList.map(i => (s"key$i", s"msg$i"))
      for {
        topic  <- randomThing("topic")
        client <- randomThing("client")
        group  <- randomThing("group")

        _ <- ZIO.succeed(EmbeddedKafka.createCustomTopic(topic, partitions = nrPartitions))

        _ <- produceMany(topic, kvs)

        _ <- Consumer
               .plainStream(Subscription.Topics(Set(topic)), Serde.byteArray, Serde.byteArray)
               .take(nrMessages.toLong)
               .runDrain
               .provideSome[Kafka](
                 consumer(client, Some(group), properties = Map(ConsumerConfig.MAX_POLL_RECORDS_CONFIG -> "1000"))
               )
      } yield ()
    }

    Unsafe.unsafe { implicit unsafe =>
      zio.Runtime.default.unsafe.run(program.provide(producer, Kafka.embedded)).getOrThrow()
    }
  }

  private def randomThing(prefix: String): Task[String] =
    ZIO.attempt(UUID.randomUUID()).map(uuid => s"$prefix-$uuid")
}
