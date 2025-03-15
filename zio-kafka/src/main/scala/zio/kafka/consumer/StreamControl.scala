package zio.kafka.consumer
import zio.UIO
import zio.stream.ZStream

trait StreamControl[-R, +E, +A] {

  /**
   * The stream associated with this subscription
   *
   * The stream should be run at most once. Running it more than once will result in chunks of records being divided
   * over the streams.
   */
  def stream: ZStream[R, E, A]

  /**
   * Pauses fetching data for all partitions associated with this subscription. The streams themselves do not end but
   * will simply wait for more data.
   *
   * TODO what about currently buffered data? What about the max pull timeout..?
   *
   * Use [[resume]] to resume the partitions. Pause has no effect when already paused or [[end]] was called previously.
   *
   * @return
   *   Effect that will complete immediately.
   */
  def pause: UIO[Unit]

  /**
   * Resumes fetching data for all partitions associated with this subscription
   *
   * Does nothing if the stream was not paused or [[end]] was called previously
   *
   * @return
   *   Effect that will complete immediately.
   */
  def resume: UIO[Unit]

  /**
   * Stops fetching data for all partitions associated with this subscription. The stream will end and the effect
   * running the stream will eventually complete.
   *
   * @return
   *   Effect that will complete immediately.
   */
  def end: UIO[Unit]
}

object StreamControl {
  implicit class StreamControlOps[-R, +E, +A](streamControl: StreamControl[R, E, A]) {
    def map[R1 <: R, E1 >: E, B](f: ZStream[R, E, A] => ZStream[R1, E1, B]): StreamControl[R1, E1, B] =
      new StreamControl[R1, E1, B] {
        def stream = f(streamControl.stream)
        def pause  = streamControl.pause
        def resume = streamControl.resume
        def end    = streamControl.end
      }
  }
}
