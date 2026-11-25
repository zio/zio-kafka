package zio.kafka.consumer
import zio.UIO
import zio.stream.ZStream

/**
 * Gives access to a stream and lets you end it gracefully.
 *
 * It is returned by the `*WithControl` methods of [[Consumer]], for example [[Consumer#plainStreamWithControl]].
 *
 * @see
 *   [[https://zio.dev/zio-kafka/consumer/ Consumer]]
 */
trait StreamControl[-R, +E, +A] {

  /**
   * The stream associated with this subscription
   *
   * The stream should be run at most once. Runs that overlap take from one shared queue, so the partition streams are
   * divided over them, and the first run to end shuts that queue down for the others. A run that starts after an
   * earlier run ended, or after `end` was called, produces no records.
   */
  def stream: ZStream[R, E, A]

  /**
   * Stops fetching data for all partitions associated with this subscription. The stream will end and the effect
   * running the stream will eventually complete.
   *
   * @return
   *   Effect that completes when the consumer has processed the request to end the streams. It does not wait for the
   *   stream to finish.
   */
  def end: UIO[Unit]
}

/** Operations on [[StreamControl]]. */
object StreamControl {

  /** Adds `map` to [[StreamControl]]. */
  implicit class StreamControlOps[-R, +E, +A](streamControl: StreamControl[R, E, A]) {

    /**
     * Returns a control whose `stream` is the result of applying `f` to the stream of this control. Its `end` is the
     * `end` of this control.
     */
    def map[R1 <: R, E1 >: E, B](f: ZStream[R, E, A] => ZStream[R1, E1, B]): StreamControl[R1, E1, B] =
      new StreamControl[R1, E1, B] {
        def stream = f(streamControl.stream)
        def end    = streamControl.end
      }
  }
}
