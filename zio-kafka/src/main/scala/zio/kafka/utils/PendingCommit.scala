package zio.kafka.utils

import zio.{ Promise, Task, Trace, ZIO }

/**
 * Represent a commit that is pending to be committed. It can be awaited to be completed.
 *
 * Less powerful interface than Promise. Avoid leaking to the users the Promise methods we don't want them to have
 * access to.
 */
// noinspection ConvertExpressionToSAM
trait PendingCommit { self =>

  def awaitCommit(implicit trace: Trace): Task[Unit]

  final def combine(other: PendingCommit): PendingCommit =
    new PendingCommit {
      override def awaitCommit(implicit trace: Trace): Task[Unit] =
        for {
          _ <- self.awaitCommit
          _ <- other.awaitCommit
        } yield ()
    }
}

//noinspection ConvertExpressionToSAM
object PendingCommit {
  private[zio] def apply(p: Promise[Throwable, Unit]): PendingCommit =
    new PendingCommit {
      override def awaitCommit(implicit trace: Trace): Task[Unit] = p.await
    }

  val unit: PendingCommit =
    new PendingCommit {
      override def awaitCommit(implicit trace: Trace): Task[Unit] = ZIO.unit
    }
}
