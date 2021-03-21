package ai.srl.collection

trait ClearAll[T]:
  extension (t: T)
    def clearAll(): Unit
