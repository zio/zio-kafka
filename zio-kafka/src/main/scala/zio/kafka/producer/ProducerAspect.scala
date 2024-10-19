package zio.kafka.producer

/**
 * A `ProducerAspect` transforms a Producer into another to augment an existing Producer with new capabilities or
 * features.
 */
trait ProducerAspect[+LowerR, -UpperR] { self =>

  def apply[R >: LowerR <: UpperR](wrapped: ProducerWithEnv[R]): ProducerWithEnv[R]

  def @@[LowerR1 >: LowerR, UpperR1 <: UpperR](
    other: ProducerAspect[LowerR1, UpperR1]
  ): ProducerAspect[LowerR1, UpperR1] =
    new ProducerAspect[LowerR1, UpperR1] {
      override def apply[R >: LowerR1 <: UpperR1](wrapped: ProducerWithEnv[R]): ProducerWithEnv[R] =
        other(self(wrapped))
    }
}
