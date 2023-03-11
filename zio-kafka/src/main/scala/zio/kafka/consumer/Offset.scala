package zio.kafka.consumer

import org.apache.kafka.clients.consumer.RetriableCommitFailedException
import org.apache.kafka.common.TopicPartition
import zio.{ RIO, Schedule, Task }

final case class Offset(
  topicPartition: TopicPartition,
  offset: Long,
  private[zio] val commitHandle: Map[TopicPartition, Long] => Task[Unit]
) {

  def commit: Task[Unit] = commitHandle(Map(topicPartition -> offset))

  /**
   * Attempts to commit and retries according to the given policy when the commit fails with a
   * RetriableCommitFailedException
   */
  def commitOrRetry[R](policy: Schedule[R, Throwable, Any]): RIO[R, Unit] =
    Offset.commitOrRetry(commit, policy)
}

object Offset {
  private[consumer] def commitOrRetry[R, B](
    commit: Task[Unit],
    policy: Schedule[R, Throwable, B]
  ): RIO[R, Unit] =
    commit.retry(
      Schedule.recurWhile[Throwable] {
        case _: RetriableCommitFailedException => true
        case _                                 => false
      } && policy
    )
}
