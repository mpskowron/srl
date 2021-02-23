package ai.srl.collection

trait IndexedEntity[T]:
  extension (t: T) def index: Int
