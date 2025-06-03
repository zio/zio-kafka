package zio.kafka.consumer
import zio.{ Trace, UIO }
import zio.stream.ZStream

trait StreamControl[-R, +E, +A] {

  /**
   * The stream associated with this subscription
   *
   * The stream should be run at most once. Running it more than once will result in chunks of records being divided
   * over the streams. After ending, running the stream another time will not produce records
   */
  def stream(implicit trace: Trace): ZStream[R, E, A]

  /**
   * Stops fetching data for all partitions associated with this subscription. The stream will end and the effect
   * running the stream will eventually complete.
   *
   * @return
   *   Effect that will complete immediately.
   */
  def end(implicit trace: Trace): UIO[Unit]
}

object StreamControl {
  implicit class StreamControlOps[-R, +E, +A](streamControl: StreamControl[R, E, A]) {
    def map[R1 <: R, E1 >: E, B](f: ZStream[R, E, A] => ZStream[R1, E1, B]): StreamControl[R1, E1, B] =
      new StreamControl[R1, E1, B] {
        def stream(implicit trace: Trace): ZStream[R1, E1, B] = f(streamControl.stream)
        def end(implicit trace: Trace): UIO[Unit]             = streamControl.end
      }
  }
}
