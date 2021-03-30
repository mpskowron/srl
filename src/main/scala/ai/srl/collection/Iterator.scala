package ai.srl.collection

trait GetIterator[I, A]:
  extension (i: I)
    // Returns iterator over non empty elements of the collection I
    def iterator: Iterator[A]
