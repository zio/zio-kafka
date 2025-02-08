package zio.kafka.diagnostics

import zio.UIO

trait Diagnostics[DiagnosticEvent] {
  def emit(event: => DiagnosticEvent): UIO[Unit]
}
