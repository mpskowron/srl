package ai.srl.collection

trait MaxSize[T]:
  extension (t: T)
    def maxSize(): Int
