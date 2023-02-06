package zio.kafka.utils

import org.apache.kafka.clients.{ ClientDnsLookup, ClientUtils }
import zio.{ Task, ZIO }

import java.nio.ByteBuffer
import java.nio.channels.SocketChannel
import scala.jdk.CollectionConverters._

/**
 * This function validates that your Kafka client (Admin, Consumer, or Producer) configurations are valid for the Kafka
 * Cluster you want to contact.
 *
 * This function protects you against this long standing bug in kafka-clients that leads to crash your app with an OOM.
 * More details, see: https://issues.apache.org/jira/browse/KAFKA-4090
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
                                // make a simple request here and validate a server response
                                val buf = ByteBuffer.allocate(5)
                                channel.write(buf)
                                buf.position(0)
                                channel.read(buf)
                                buf.position(0)
                                isTls(buf)
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

  private def isTls(buf: ByteBuffer): Boolean = {
    val tlsMessageType = buf.get()
    tlsMessageType match {
      case 20 | 21 | 22 | 23 | 255 =>
        true
      case _ => tlsMessageType >= 128
    }
  }
}
