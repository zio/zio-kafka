package zio.kafka.utils

import org.apache.kafka.clients.{ ClientDnsLookup, ClientUtils }
import org.apache.kafka.common.KafkaException
import org.apache.kafka.common.network.TransferableChannel
import org.apache.kafka.common.protocol.ApiKeys
import org.apache.kafka.common.requests.{ ApiVersionsRequest, RequestHeader }
import zio.{ durationInt, durationLong, BuildFrom, Duration, IO, Ref, Task, Trace, URIO, ZIO }

import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.{ FileChannel, SocketChannel }
import scala.jdk.CollectionConverters._
import scala.util.control.{ NoStackTrace, NonFatal }

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

  /**
   * A private exception that we use to "tag" some exceptions that we potentially want to ignore.
   */
  private final case class ConnectionError(cause: Throwable) extends NoStackTrace

  // ⚠️ Must not do anything else than calling `doValidateEndpoint`. The algorithm of this function must be completely contained in `doValidateEndpoint`.
  def validateEndpoint(bootstrapServers: List[String], props: Map[String, AnyRef]): IO[KafkaException, Unit] =
    doValidateEndpoint(SocketChannel.open)(bootstrapServers, props)

  /**
   * We use this private function so that we can easily manipulate the `openSocket` function in unit-tests.
   */
  private[utils] def doValidateEndpoint(
    openSocket: InetSocketAddress => SocketChannel // Handy for unit-tests
  )(bootstrapServers: List[String], props: Map[String, AnyRef]): IO[KafkaException, Unit] = {
    @inline def `request.timeout.ms`: Duration = {
      val defaultValue = 30.seconds

      props.get("request.timeout.ms") match {
        case None => defaultValue
        case Some(raw) =>
          try {
            val v = raw.toString.toLong
            if (v <= 0) defaultValue else v.millis
          } catch {
            case NonFatal(_) => defaultValue
          }
      }
    }

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
              errors <- ZIO.collectAllFailuresPar(
                          addresses.map(validateSslConfigOf(openSocket, socketTimeout = `request.timeout.ms`))
                        )
              atLeastOneBootstrapServerIsUp = errors.size < addresses.size
              _ <- errors.partition(_.isInstanceOf[ConnectionError]) match {
                     // If we have at least one "real error" (not a "connection error"), we fail with the first one
                     case (_, head :: _) => ZIO.fail(head)
                     // If we have no errors or if we have some "connection errors" but at least one bootstrap server is up, we succeed
                     case (Nil, Nil)                         => ZIO.unit
                     case _ if atLeastOneBootstrapServerIsUp => ZIO.unit
                     // If we have only "connection errors" and no bootstrap server is up, we fail with the first one
                     // Note that we don't propagate the internal `ConnectionError` wrapper
                     case (head :: _, _) => ZIO.fail(head.asInstanceOf[ConnectionError].cause)
                   }
            } yield ()
          }
        }
        .unit
        .mapError(kafkaException)
  }

  /**
   * Mimic behaviour of KafkaAdminClient.createInternal
   */
  private def kafkaException(e: Throwable): KafkaException =
    new KafkaException("Failed to create new KafkaAdminClient", e)

  private def validateSslConfigOf(
    openSocket: InetSocketAddress => SocketChannel,
    socketTimeout: Duration
  )(address: InetSocketAddress): Task[Unit] = {
    @inline def error: IO[IllegalArgumentException, Nothing] =
      ZIO.fail(
        new IllegalArgumentException(
          s"Received an unexpected SSL packet from the server. Please ensure the client is properly configured with SSL enabled"
        )
      )

    ZIO.scoped {
      for {
        ref <- Ref.make[SocketChannel](null)
        channel <-
          ZIO.acquireReleaseInterruptible(
            ZIO.uninterruptible {
              // It's imperative that this algorithm is not interruptible because of the timeout mechanism.
              // Otherwise, we might leak a socket channel.
              // If the timeout interruption happens between the `openSocket` and the `ref.set`, we would leak the socket channel.
              ZIO.attempt(openSocket(address)).tap(ref.set)
            }.timeoutFail(new java.util.concurrent.TimeoutException(s"Failed to contact $address"))(socketTimeout)
              .mapError(ConnectionError.apply)
          )(
            ref.update { socket =>
              if (socket != null) {
                // Wrapped in a try as the `ref.update` algorithm might retry the code
                // and the `.close` function, being Java code, might throw
                // Better be safe than sorry
                try socket.close()
                catch {
                  case _: Throwable => // ignore
                }
              }
              null
            }
          )
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
