package ai.srl.collection

object Closeable:
  def closeIfNeeded[T]: PartialFunction[T, Unit] =
    case closeable: AutoCloseable => closeable.close()
    case _ => ()
