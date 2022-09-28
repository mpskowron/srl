package ai.srl.collection

import ai.srl.collection.Combine
import ai.srl.collection.SizedChunk.FixedSizeType
import ai.srl.collection.Tuples.fmap
import cats.{Functor, Monoid, Semigroup}
import zio.Chunk

import scala.compiletime.ops.int.*
import scala.reflect.ClassTag

//opaque type SizedChunk[S <: Int, T] = Chunk[T]

class SizedChunk[S <: Int: ValueOf, A](val chunk: Chunk[A]):
  require(
    valueOf[S] == chunk.size,
    s"[BUG] Attempt to create a sized chunk with incorrect size ${chunk.size} not equal to ${valueOf[S]}"
  )
  def map[B](f: A => B): SizedChunk[S, B] = SizedChunk(chunk.map(f))
  export chunk.{map as _, *}

object SizedChunk:
  type FixedSizeType = Singleton & Int
  case class IncorrectSizeError(actual: Int, Expected: Int)

  def fromIterable[S <: Int: ValueOf, A](it: Iterable[A]): SizedChunk[S, A] = SizedChunk(Chunk.fromIterable(it))
  def fromIterator[S <: Int: ValueOf, A](it: Iterator[A]): SizedChunk[S, A] = SizedChunk(Chunk.fromIterator(it))
//  def fromIterable[S <: Int: ValueOf, A](it: collection.mutable.Iterable[A]): SizedChunk[S, A] = SizedChunk(Chunk.fromIterable(it))

//  inline def apply[S <: Int, T](chunk: Chunk[T]): SizedChunk[S, T] =
//    require(valueOf[S] == chunk.size, "[BUG] Attempt to create a sized chunk with incorrect size")
//    new SizedChunk[S, T](chunk)
//    chunk

//FIXME it will probably not work as intended
//  given [S1 <: Int, S2 <: Int, T]: Semigroup[SizedChunk[S1 + S2, T]] with
//    def combine(x: SizedChunk[S1, T], y: SizedChunk[S2, T]): SizedChunk[S1 + S2, T] = x ++ y
