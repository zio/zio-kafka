package zio.kafka.consumer

import scala.collection.SeqView

package object internal {

  /** Provide scala 2.13 forward compatibility for the 2.12 build. */
  implicit class ExtraIterableOnceOpsOps[A, Coll](val s: SeqView[A, Coll]) {
    def maxByOption[B](f: A => B)(implicit cmp: Ordering[B]): Option[A] =
      if (s.isEmpty) None
      else Some(s.maxBy(f)(cmp))
  }

}
