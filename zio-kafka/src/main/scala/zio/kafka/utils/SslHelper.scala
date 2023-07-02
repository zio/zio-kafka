package zio.kafka.utils

import org.apache.kafka.clients.{ ClientDnsLookup, ClientUtils }
import org.apache.kafka.common.KafkaException
import org.apache.kafka.common.network.{ Send, TransferableChannel }
import org.apache.kafka.common.protocol.ApiKeys
import org.apache.kafka.common.requests.{ ApiVersionsRequest, RequestHeader }
import zio.{ durationInt, durationLong, BuildFrom, Duration, Exit, IO, Ref, Task, Trace, URIO, ZIO }

import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.{ FileChannel, SocketChannel }
import scala.jdk.CollectionConverters._
import scala.util.control.{ NoStackTrace, NonFatal }
import scala.util.{ Failure, Success, Try }

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
    unsafeOpenSocket: InetSocketAddress => SocketChannel // Handy for unit-tests
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
                          addresses.map(validateSslConfigOf(unsafeOpenSocket, socketTimeout = `request.timeout.ms`))
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

  /**
   * Let's take some time here to discuss the algorithm of this function as it's a bit tricky and uses obscure Java
   * APIs/features.
   *
   * The goal of this function is to validate that the SSL configuration of the client is correct for the Kafka cluster
   * we want to contact. (ie. that the Kafka cluster is not configured with SSL as we previously validated that the
   * client was not configured for an SSL server)
   *
   * The algorithm is the following:
   *   1. We open a socket to the Kafka node
   *   1. We send a simple request to the Kafka node to check if it's configured with SSL
   *   1. We read the answer from the Kafka node
   *   1. If the node is configured with SSL, we fail with an error
   *
   * This algorithm and its implementation, per se, are relatively simple. It's the ~4 lines of code inside the
   * `ZIO.attemptBlockingInterrupt` call.
   *
   * The tricky part is that we want to be able to timeout the whole process if it takes too long but timeouting a
   * `SocketChannel` is, theoretically, not possible.
   *
   * Also, we don't want to leak memory so we want to be sure that the socket is closed in all cases (success, failure,
   * or timeout/interruption).
   *
   * The socket might be closed in two possible cases:
   *   1. The socket is successfully opened and we send the request to the Kafka cluster. In this case, it's closed in
   *      the `release` part of `ZIO.acquireReleaseInterruptible`.
   *   1. The networking exchange takes too long and we timeout/interrupt the whole process. In this case, and that the
   *      most tricky/weird part, to close the socket, we interrupt the thread running it.
   *
   * Why does interrupting the thread running the networking exchange closes the socket? Because the `SocketChannel`
   * class implements the `InterruptibleChannel` interface, and that's a property of this interface.
   *
   * From the documentation of `InterruptibleChannel`, we can read:
   * {{{
   * > A channel that implements this interface is also interruptible:
   * > If a thread is blocked in an I/O operation on an interruptible channel then another thread may invoke the blocked thread's interrupt method.
   * > This will cause the channel to be closed, the blocked thread to receive a ClosedByInterruptException, and the blocked thread's interrupt status to be set.
   * }}}
   *
   * See: https://docs.oracle.com/javase/8/docs/api/java/nio/channels/InterruptibleChannel.html
   *
   * Let's recap.
   *
   * We use a `SocketChannel` to test the SSL configuration of the Kafka node. This `SocketChannel` is a resource and
   * needs to be closed to avoid leaking memory. We want to be able to timeout the whole process if it takes too long
   * but a `SocketChannel` is not timeoutable. So we implement a timeout mechanism that will interrupt the thread on
   * which the `SocketChannel` is running to close it as it's one of the properties of the `SocketChannel` class.
   *
   * Note that there are unit-tests proving that this works.
   *
   * Useful links which helped to make this work:
   *   - Discord discussion/thread with Vladimir Klyushnikov (https://github.com/vladimirkl) starting here:
   *     https://discord.com/channels/629491597070827530/1122924033827164282/1124719212246610023
   *   - https://stackoverflow.com/questions/2866557/timeout-for-socketchannel-doesnt-work#comment11463818_2866557
   *   - https://stackoverflow.com/a/18375293/2431728
   */
  private def validateSslConfigOf(
    unsafeOpenSocket: InetSocketAddress => SocketChannel,
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
        ref <- Ref.make[Option[(SocketChannel, Try[Boolean])]](None)
        result <-
          ZIO.acquireReleaseInterruptible(
            ZIO.attemptBlockingInterrupt {
              // Note about this algorithm:
              // We make all the networking exchanges (ie. `unsafeOpenSocket`, `unsafeSendTestRequest` and `unsafeReadAnswerFromTestRequest`) in this
              // interruptible blocking section so that we can easily timeout/interrupt the whole process if it takes too long.

              val channel = unsafeOpenSocket(address)
              // We need to wrap these calls in a Try to be sure to close the socket even if one these calls fail
              val safeIsTLS =
                Try {
                  unsafeSendTestRequest(channel)
                  val buffer = unsafeReadAnswerFromTestRequest(channel)
                  isTls(buffer)
                }
              channel -> safeIsTLS
            }
              .timeout(socketTimeout)
              // In order to avoid any leak of the `SocketChannel`, we need to be sure that the `Ref` is set, even in interruption cases.
              // That's why `.tap(ref.set)` wouldn't be not enough and we have to use `.onExit`.
              // More info, see discussion starting here: https://discord.com/channels/629491597070827530/1122924033827164282/1124995521774358578
              .onExit {
                case Exit.Success(x) => ref.set(x)
                case Exit.Failure(_) => ZIO.unit
              }
              .mapError(ConnectionError.apply)
          )(ref.get.map {
            case Some((channel, _)) => ZIO.attempt(channel.close()).orDie
            case None               => ZIO.unit
          })
        _ <-
          result match {
            case None => ZIO.fail(new java.util.concurrent.TimeoutException(s"Failed to contact $address"))
            case Some((_, Success(isTLS))) => if (isTLS) error else ZIO.unit
            case Some((_, Failure(e)))     => ZIO.fail(e)
          }
      } yield ()
    }
  }

  /**
   * Send a simple request to check if connection can be established with current configuration
   */
  private def unsafeSendTestRequest(channel: SocketChannel): Unit = {
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
    val send: Send = new ApiVersionsRequest.Builder()
      .build(ApiKeys.API_VERSIONS.latestVersion())
      .toSend(new RequestHeader(ApiKeys.API_VERSIONS, ApiKeys.API_VERSIONS.latestVersion(), null, 0))
    send.writeTo(transferableChannel)
    ()
  }

  /**
   * Reads the 5 first bytes of the channel to extract the record type of the answer
   */
  private def unsafeReadAnswerFromTestRequest(channel: SocketChannel): ByteBuffer = {
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
