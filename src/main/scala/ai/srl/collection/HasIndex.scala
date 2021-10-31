package ai.srl.collection

trait HasIndex[T]:
  extension (t: T) def index: Int
