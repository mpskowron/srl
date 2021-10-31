package ai.srl.collection

import RefinedExtensions.SInt
import zio.Chunk

/** @tparam C
  *   needs to be a collection with immutable size, otherwise this won't work correctly at all
  */
trait SizedCollection[C[_]] private:
  type S <: SInt
  type T
  val value: C[T]

object SizedCollection:
  type SizedCollectionChunk = SizedCollection[Chunk]
  type ChunkAuxT[TT]        = SizedCollectionChunk { type T = TT }
  type ChunkAux[SS, TT] = SizedCollectionChunk {
    type T = TT
    type S = SS
  }

  def apply[TT, CC[_]](c: CC[TT])(using Size[CC[TT]]) =
    val s = c.size
    new SizedCollection[CC]:
      type S = s.type
      type T = TT
      val value = c

  extension [T1, T2](c: ChunkAuxT[(T1, T2)])
    def unzip: (ChunkAuxT[T1] { type S = c.S }, ChunkAuxT[T2] { type S = c.S }) =
      val (t1s, t2s) = c.value.unzip
      (
        new SizedCollection[Chunk]:
          type S = c.S
          type T = T1
          val value = t1s
        ,
        new SizedCollection[Chunk]:
          type S = c.S
          type T = T2
          val value = t2s
      )

  type SizedCollectionT[C[_]] = [TT] =>> SizedCollection[C] { type T = TT }

  given [C[_]: GetIterator]: GetIterator[SizedCollectionT[C]] with
    extension [T](c: SizedCollectionT[C][T]) def iterator: Iterator[c.T] = c.value.iterator
