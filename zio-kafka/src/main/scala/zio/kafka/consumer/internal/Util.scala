package zio.kafka.consumer.internal
import org.apache.kafka.common.TopicPartition
import zio.{ Duration, LogAnnotation, UIO, ZIO }

private[consumer] object Util {

  /**
   * Executes `log` after f has been running for `duration`
   */
  def logAfterTime[R, E, A](f: ZIO[R, E, A], duration: Duration, log: UIO[Unit]): ZIO[R, E, A] =
    ZIO.acquireReleaseWith(log.delay(duration).fork)(_.interrupt)(_ => f)

  def annotateTopicPartition[R, E, A](topicPartition: TopicPartition)(f: ZIO[R, E, A]): ZIO[R, E, A] =
    ZIO.logAnnotate(
      LogAnnotation("topic", topicPartition.topic),
      LogAnnotation("partition", topicPartition.partition().toString)
    )(f)

}
