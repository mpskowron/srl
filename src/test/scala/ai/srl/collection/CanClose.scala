package ai.srl.collection

import alleycats.Empty

case class CanClose(var option: Option[Int]) extends AutoCloseable:
  override def close(): Unit = option = None

object CanClose:
  given Empty[CanClose] with
    def empty = CanClose(None)
    
