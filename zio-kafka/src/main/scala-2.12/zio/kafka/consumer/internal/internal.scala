package zio.kafka.consumer

import scala.collection.SeqView

/** Provides scala 2.13 forward compatibility for scala 2.12. */
package object internal {

  implicit class ExtraSeqViewOps[A, Coll](val s: SeqView[A, Coll]) {
    def maxByOption[B](f: A => B)(implicit cmp: Ordering[B]): Option[A] =
      if (s.isEmpty) None
      else Some(s.maxBy(f)(cmp))
  }

}
