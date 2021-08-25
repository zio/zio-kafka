package zio.kafka.producer

import org.apache.kafka.clients.producer.{ KafkaProducer, ProducerRecord, RecordMetadata }
import org.apache.kafka.common.serialization.ByteArraySerializer
import org.apache.kafka.common.{ Metric, MetricName }
import zio.blocking.Blocking
import zio.kafka.producer.Producer.Live
import zio.kafka.serde.Serializer
import zio.{ Chunk, Has, IO, RIO, RLayer, RManaged, RefM, Task, ZIO, ZManaged }

import scala.jdk.CollectionConverters._

trait TransactionalProducer extends Producer {
  def createTransaction: ZManaged[Any, Throwable, Transaction]
}

object TransactionalProducer {
  private case class LiveTransactionalProducer private (
    underlying: Producer.Live
  ) extends TransactionalProducer {
    override def createTransaction: ZManaged[Any, Throwable, Transaction] =
      ZManaged.make {
        for {
          _     <- IO(underlying.p.beginTransaction())
          state <- RefM.make(TransactionState())
        } yield new Transaction(producer = this, state)
      } { transaction =>
        transaction.state.get.flatMap(state =>
          if (state.abortScheduled) {
            IO(underlying.p.abortTransaction()).retryN(5).orDie
          } else {
            IO(underlying.p.commitTransaction()).retryN(5).orDie
          }
        )
      }

    /**
     * Produces a single record and await broker acknowledgement. See [[produceAsync[R,K,V](record*]] for
     * version that allows to avoid round-trip-time penalty for each record.
     */
    override def produce[R, K, V](
      record: ProducerRecord[K, V],
      keySerializer: Serializer[R, K],
      valueSerializer: Serializer[R, V]
    ): RIO[R, RecordMetadata] =
      underlying.produce(record, keySerializer, valueSerializer)

    /**
     * Produces a single record and await broker acknowledgement. See [[produceAsync[R,K,V](topic:String* ]] for
     * version that allows to avoid round-trip-time penalty for each record.
     */
    override def produce[R, K, V](
      topic: String,
      key: K,
      value: V,
      keySerializer: Serializer[R, K],
      valueSerializer: Serializer[R, V]
    ): RIO[R, RecordMetadata] =
      underlying.produce(topic, key, value, keySerializer, valueSerializer)

    /**
     * Produces a single record. The effect returned from this method has two layers and
     * describes the completion of two actions:
     * 1. The outer layer describes the enqueueing of the record to the Producer's internal
     * buffer.
     * 2. The inner layer describes receiving an acknowledgement from the broker for the
     * transmission of the record.
     *
     * It is usually recommended to not await the inner layer of every individual record,
     * but enqueue a batch of records and await all of their acknowledgements at once. That
     * amortizes the cost of sending requests to Kafka and increases throughput.
     * See [[produce[R,K,V](record*]] for version that awaits broker acknowledgement.
     */
    override def produceAsync[R, K, V](
      record: ProducerRecord[K, V],
      keySerializer: Serializer[R, K],
      valueSerializer: Serializer[R, V]
    ): RIO[R, Task[RecordMetadata]] =
      underlying.produceAsync(record, keySerializer, valueSerializer)

    /**
     * Produces a single record. The effect returned from this method has two layers and
     * describes the completion of two actions:
     * 1. The outer layer describes the enqueueing of the record to the Producer's internal
     * buffer.
     * 2. The inner layer describes receiving an acknowledgement from the broker for the
     * transmission of the record.
     *
     * It is usually recommended to not await the inner layer of every individual record,
     * but enqueue a batch of records and await all of their acknowledgements at once. That
     * amortizes the cost of sending requests to Kafka and increases throughput.
     * See [[produce[R,K,V](topic*]] for version that awaits broker acknowledgement.
     */
    override def produceAsync[R, K, V](
      topic: String,
      key: K,
      value: V,
      keySerializer: Serializer[R, K],
      valueSerializer: Serializer[R, V]
    ): RIO[R, Task[RecordMetadata]] =
      underlying.produceAsync(topic, key, value, keySerializer, valueSerializer)

    /**
     * Produces a chunk of records. The effect returned from this method has two layers
     * and describes the completion of two actions:
     * 1. The outer layer describes the enqueueing of all the records to the Producer's
     * internal buffer.
     * 2. The inner layer describes receiving an acknowledgement from the broker for the
     * transmission of the records.
     *
     * It is possible that for chunks that exceed the producer's internal buffer size, the
     * outer layer will also signal the transmission of part of the chunk. Regardless,
     * awaiting the inner layer guarantees the transmission of the entire chunk.
     */
    override def produceChunkAsync[R, K, V](
      records: Chunk[ProducerRecord[K, V]],
      keySerializer: Serializer[R, K],
      valueSerializer: Serializer[R, V]
    ): RIO[R, Task[Chunk[RecordMetadata]]] =
      underlying.produceChunkAsync(records, keySerializer, valueSerializer)

    /**
     * Produces a chunk of records. See [[produceChunkAsync]] for version that allows
     * to avoid round-trip-time penalty for each chunk.
     */
    override def produceChunk[R, K, V](
      records: Chunk[ProducerRecord[K, V]],
      keySerializer: Serializer[R, K],
      valueSerializer: Serializer[R, V]
    ): RIO[R, Chunk[RecordMetadata]] =
      underlying.produceChunk(records, keySerializer, valueSerializer)

    /**
     * Flushes the producer's internal buffer. This will guarantee that all records
     * currently buffered will be transmitted to the broker.
     */
    override def flush: Task[Unit] =
      underlying.flush

    /**
     * Expose internal producer metrics
     */
    override def metrics: Task[Map[MetricName, Metric]] =
      underlying.metrics
  }

  def createTransaction: RManaged[Has[TransactionalProducer], Transaction] =
    ZManaged.service[TransactionalProducer].flatMap(_.createTransaction)

  val live: RLayer[Has[TransactionalProducerSettings] with Blocking, Has[TransactionalProducer]] =
    (for {
      settings <- ZManaged.service[TransactionalProducerSettings]
      producer <- make(settings)
    } yield producer).toLayer

  def make(settings: TransactionalProducerSettings): RManaged[Blocking, TransactionalProducer] =
    (for {
      props       <- ZIO.effect(settings.driverSettings)
      blocking    <- ZIO.service[Blocking.Service]
      rawProducer <- ZIO.effect(
                       new KafkaProducer[Array[Byte], Array[Byte]](
                         props.asJava,
                         new ByteArraySerializer(),
                         new ByteArraySerializer()
                       )
                     )
      _           <- blocking.effectBlocking(rawProducer.initTransactions())
      live         = Live(rawProducer, settings, blocking)
    } yield LiveTransactionalProducer(live)).toManaged(_.underlying.close)
}
