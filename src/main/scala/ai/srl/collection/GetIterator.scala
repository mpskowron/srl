package ai.srl.collection

import zio.Chunk

trait GetIterator[I[_]]:
  extension [A](i: I[A])
    def iterator: Iterator[A]

object GetIterator:
  given GetIterator[IterableOnce] with
    extension [A](i: IterableOnce[A])
      def iterator: Iterator[A] = i.iterator

  given GetIterator[Chunk] with
    extension [A](i: Chunk[A])
      def iterator: Iterator[A] = i.iterator