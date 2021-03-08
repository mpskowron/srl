package ai.srl.collection

trait Size[T]:
  extension (t: T)
    def size(): Int
