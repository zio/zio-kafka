package zio.kafka.utils

import org.apache.kafka.clients.{ ClientDnsLookup, ClientUtils }
import org.apache.kafka.common.KafkaException
import org.apache.kafka.common.network.TransferableChannel
import org.apache.kafka.common.protocol.ApiKeys
import org.apache.kafka.common.requests.{ ApiVersionsRequest, RequestHeader }
import zio.{ Task, ZIO }

import java.nio.ByteBuffer
import java.nio.channels.{ FileChannel, SocketChannel }
import scala.jdk.CollectionConverters._

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
object SslHelper {
  def validateEndpoint(bootstrapServers: List[String], props: Map[String, AnyRef]): Task[Unit] =
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
            address <- ZIO.attempt {
                         ClientUtils
                           .parseAndValidateAddresses(bootstrapServers.asJava, ClientDnsLookup.USE_ALL_DNS_IPS)
                           .asScala
                           .toList
                       }
            _ <- ZIO.foreachParDiscard(address) { addr =>
                   ZIO.scoped {
                     for {
                       channel <- ZIO.acquireRelease(
                                    ZIO.attempt(SocketChannel.open(addr))
                                  )(channel => ZIO.attempt(channel.close()).orDie)
                       tls <- ZIO.attempt {
                                // Send a simple request to check if the cluster accepts the connection
                                sendTestRequest(channel)
                                val buffer = readAnswerFromTestRequest(channel)
                                isTls(buffer)
                              }
                       _ <-
                         ZIO.when(tls)(
                           ZIO.fail(
                             new IllegalArgumentException(
                               s"Received an unexpected SSL packet from the server. Please ensure the client is properly configured with SSL enabled"
                             )
                           )
                         )
                     } yield ()
                   }
                 }
          } yield ()
        }
      }
      .unit
      .mapError { e =>
        // Mimic behaviour of KafkaAdminClient.createInternal
        new KafkaException("Failed to create new KafkaAdminClient", e)
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
}
