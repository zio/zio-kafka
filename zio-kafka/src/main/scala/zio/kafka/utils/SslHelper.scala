package zio.kafka.utils

import org.apache.kafka.clients.{ ClientDnsLookup, ClientUtils }
import org.apache.kafka.common.KafkaException
import org.apache.kafka.common.network.TransferableChannel
import org.apache.kafka.common.protocol.ApiKeys
import org.apache.kafka.common.requests.{ ApiVersionsRequest, RequestHeader }
import zio.{ BuildFrom, IO, Task, Trace, URIO, ZIO }

import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.{ FileChannel, SocketChannel }
import scala.jdk.CollectionConverters._
import scala.util.control.NoStackTrace

/**
 * This function validates that your Kafka client (Admin, Consumer, or Producer) configurations are valid for the Kafka
 * Cluster you want to contact.
 *
 * This function protects you against this long standing bug in kafka-clients that leads to crash your app with an OOM.
 * More details, see: https://issues.apache.org/jira/browse/KAFKA-4090
 *
 * Credits for this work go to Nick Pavlov (https://github.com/gurinderu), Guillaume Bécan (https://github.com/gbecan)
 * and the Conduktor (https://www.conduktor.io/) devs team.
 */
//noinspection SimplifyUnlessInspection,SimplifyWhenInspection
object SslHelper {
  private final case class ConnectExceptionWrapper(cause: Throwable) extends NoStackTrace

  // ⚠️ Must not do anything else than calling `_validateEndpoint`.
  def validateEndpoint(bootstrapServers: List[String], props: Map[String, AnyRef]): IO[KafkaException, Unit] =
    _validateEndpoint(SocketChannel.open)(bootstrapServers, props)

  /**
   * We use this private function so that we can easily manipulate the `openSocket` function in unit-tests.
   */
  private[utils] def _validateEndpoint(
    openSocket: InetSocketAddress => SocketChannel // Handy for unit-tests
  )(bootstrapServers: List[String], props: Map[String, AnyRef]): IO[KafkaException, Unit] =
    if (bootstrapServers.isEmpty) ZIO.fail(kafkaException(new IllegalArgumentException("Empty bootstrapServers list")))
    else
      ZIO
        .unless(
          props
            .get("security.protocol")
            .exists {
              case x: String if x.toUpperCase().contains("SSL") => true
              case _                                            => false
            }
        ) {
          ZIO.blocking {
            for {
              addresses <- ZIO.attempt {
                             ClientUtils
                               .parseAndValidateAddresses(bootstrapServers.asJava, ClientDnsLookup.USE_ALL_DNS_IPS)
                               .asScala
                               .toList
                           }
              errors <- ZIO.collectAllFailuresPar(addresses.map(validateSslConfigOf(openSocket)))
              allCallsFailed = errors.size == addresses.size
              _ <- errors match {
                     case Nil => ZIO.unit // No calls failed
                     case head :: _ if allCallsFailed => // All calls failed
                       head match {
                         // We don't propagate the internal wrapper
                         case error: ConnectExceptionWrapper => ZIO.fail(error.cause)
                         case e                              => ZIO.fail(e)
                       }
                     case _ => // Some calls failed
                       errors.collect { case e if !e.isInstanceOf[ConnectExceptionWrapper] => e } match {
                         // No "real errors", we ignore the internally wrapped errors
                         case Nil => ZIO.unit
                         // Some "real errors", we propagate the first one
                         case realError :: _ => ZIO.fail(realError)
                       }
                   }
            } yield ()
          }
        }
        .unit
        .mapError(kafkaException)

  /**
   * Mimic behaviour of KafkaAdminClient.createInternal
   */
  private def kafkaException(e: Throwable): KafkaException =
    new KafkaException("Failed to create new KafkaAdminClient", e)

  private def validateSslConfigOf(
    openSocket: InetSocketAddress => SocketChannel
  )(address: InetSocketAddress): Task[Unit] = {
    @inline def error: IO[IllegalArgumentException, Nothing] =
      ZIO.fail(
        new IllegalArgumentException(
          s"Received an unexpected SSL packet from the server. Please ensure the client is properly configured with SSL enabled"
        )
      )

    ZIO.scoped {
      for {
        channel <- ZIO.acquireRelease(
                     ZIO.attempt(openSocket(address)).mapError(ConnectExceptionWrapper)
                   )(channel => ZIO.attempt(channel.close()).orDie)
        tls <- ZIO.attempt {
                 // Send a simple request to check if the cluster accepts the connection
                 sendTestRequest(channel)
                 val buffer = readAnswerFromTestRequest(channel)
                 isTls(buffer)
               }
        _ <- if (tls) error else ZIO.unit
      } yield ()
    }
  }

  /**
   * Send a simple request to check if connection can be established with current configuration
   */
  private def sendTestRequest(channel: SocketChannel): Unit = {
    val transferableChannel = new TransferableChannel {
      override def hasPendingWrites: Boolean = false

      override def transferFrom(fileChannel: FileChannel, position: Long, count: Long): Long =
        throw new UnsupportedOperationException()

      override def write(srcs: Array[ByteBuffer], offset: Int, length: Int): Long = channel.write(srcs, offset, length)

      override def write(srcs: Array[ByteBuffer]): Long = channel.write(srcs)

      override def write(src: ByteBuffer): Int = channel.write(src)

      override def isOpen: Boolean = channel.isOpen

      override def close(): Unit = channel.close()
    }

    // We send an API version request as a minimal, valid and fast request
    val send = new ApiVersionsRequest.Builder()
      .build(ApiKeys.API_VERSIONS.latestVersion())
      .toSend(new RequestHeader(ApiKeys.API_VERSIONS, ApiKeys.API_VERSIONS.latestVersion(), null, 0))
    send.writeTo(transferableChannel)
    ()
  }

  /**
   * Reads the 5 first bytes of the channel to extract the record type of the answer
   */
  private def readAnswerFromTestRequest(channel: SocketChannel): ByteBuffer = {
    val buf = ByteBuffer.allocate(5)
    channel.read(buf)
    buf.position(0)
    buf
  }

  /**
   * Check if first byte of buffer corresponds to a record type from a TLS server
   */
  private def isTls(buf: ByteBuffer): Boolean = {
    val tlsMessageType = buf.get()
    tlsMessageType match {
      case 20 | 21 | 22 | 23 | 255 =>
        true
      case _ => tlsMessageType >= 128
    }
  }

  private implicit final class ZIOTypeOps(private val dummy: ZIO.type) extends AnyVal {

    /**
     * Adapted from [[ZIO.collectAllSuccessesPar]]
     */
    def collectAllFailuresPar[R, E, A, Collection[+Element] <: Iterable[Element]](
      in: Collection[ZIO[R, E, A]]
    )(implicit bf: BuildFrom[Collection[ZIO[R, E, A]], E, Collection[E]], trace: Trace): URIO[R, Collection[E]] =
      ZIO.collectAllWithPar(in.map(_.either)) { case Left(a) => a }.map(bf.fromSpecific(in))
  }
}
