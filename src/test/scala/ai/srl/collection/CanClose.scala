package ai.srl.collection

import alleycats.Empty
import cats.kernel.Eq

case class CanClose(var option: Option[Int]) extends AutoCloseable:
  override def close(): Unit = option = None

object CanClose:
  given Empty[CanClose] with
    def empty = CanClose(None)
  given Eq[CanClose] with
    def eqv(x: CanClose, y: CanClose) = x == y
