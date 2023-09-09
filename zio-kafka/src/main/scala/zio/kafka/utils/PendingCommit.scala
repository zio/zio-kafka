package zio.kafka.utils

import zio.{ IO, Promise, Trace, ZIO }

/**
 * Less powerful interface than Promise
 *
 * Avoid leaking to the users the Promise methods we don't want them to have access to.
 */
// noinspection ConvertExpressionToSAM
trait PendingCommit[E, A] { self =>

  def awaitCommit(implicit trace: Trace): IO[E, A]

  final def combineDiscard(other: PendingCommit[E, _]): PendingCommit[E, Unit] =
    new PendingCommit[E, Unit] {
      override def awaitCommit(implicit trace: Trace): IO[E, Unit] =
        for {
          _ <- self.awaitCommit
          _ <- other.awaitCommit
        } yield ()
    }
}

//noinspection ConvertExpressionToSAM
object PendingCommit {
  private[zio] def apply[E, A](p: Promise[E, A]): PendingCommit[E, A] =
    new PendingCommit[E, A] {
      override def awaitCommit(implicit trace: Trace): IO[E, A] = p.await
    }

  val unit: PendingCommit[Nothing, Unit] =
    new PendingCommit[Nothing, Unit] {
      override def awaitCommit(implicit trace: Trace): IO[Nothing, Unit] = ZIO.unit
    }
}
