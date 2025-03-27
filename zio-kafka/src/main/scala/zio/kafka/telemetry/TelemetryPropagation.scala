package zio.kafka.telemetry

import zio._

import scala.util.Try

/**
 * Gets and sets values that are used for telemetry from/to the current fiber.
 *
 * This is used by zio-kafka in 2 ways:
 *   - in the consumer: the values ar read after calling the java client, and set on the fiber that executes the
 *     consuming ZStream,
 *   - in the producer: the values are read from the fiber that produces records, and set before invoking the java
 *     client.
 */
trait TelemetryPropagation {

  /** Extract values that are used for telemetry from the current fiber. */
  def getFiberRefValues: ZIO[Any, Nothing, Seq[(FiberRef[_], Any)]]

  /**
   * Sets values that are used for telemetry on the current fiber.
   *
   * @param fiberRefValues
   *   SHOULD be a result of [[getFiberRefValues]]
   */
  def setFiberRefValues(fiberRefValues: Seq[(FiberRef[_], Any)]): ZIO[Any, Nothing, Unit]

}

object TelemetryPropagationLive {

  val DefaultTracingFiberRefValueTypeNames: Seq[String] = Seq(
    "io.opentelemetry.context.Context",
    "io.opencensus.trace.span"
  )

  def live: ZLayer[Any, Nothing, TelemetryPropagation] =
    ZLayer.succeed(ValueTypeBasedTelemetryPropagation(DefaultTracingFiberRefValueTypeNames))
}

case class ValueTypeBasedTelemetryPropagation(tracingFiberRefValueTypeNames: Seq[String]) extends TelemetryPropagation {

  private val tracingFiberRefValueTypes: Seq[Class[_]] = tracingFiberRefValueTypeNames
    .flatMap(name => Try(Class.forName(name)).toOption)
  private val skip  = tracingFiberRefValueTypes.isEmpty
  private val empty = ZIO.succeed(Nil)

  private def isTracingValue(value: Any): Boolean = {
    val valueType = value.getClass
    tracingFiberRefValueTypes.exists(_.isAssignableFrom(valueType))
  }

  override def getFiberRefValues: ZIO[Any, Nothing, Seq[(FiberRef[_], Any)]] =
    if (skip) empty
    else
      ZIO.getFiberRefs.map { fiberRefs =>
        fiberRefs.fiberRefs
          .foldLeft(Seq.newBuilder[(FiberRef[_], Any)]) { case (acc, ref) =>
            acc ++= fiberRefs
              .get(ref)
              .flatMap { value =>
                if (isTracingValue(value)) Some((ref, value)) else None
              }
          }
          .result()
      }

  override def setFiberRefValues(fiberRefValues: Seq[(FiberRef[_], Any)]): ZIO[Any, Nothing, Unit] =
    ZIO.foreachParDiscard(fiberRefValues) { case (ref, value) =>
      ref.asInstanceOf[FiberRef[Any]].set(value)
    }

}
