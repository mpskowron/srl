package ai.srl.collection

import cats.{Monoid, Semigroup}
import ai.srl.collection.Combine

import scala.reflect.ClassTag
import scala.compiletime.ops.int.*
import zio.Chunk
import ai.srl.collection.Tuples.fmap
import cats.Functor
import shapeless.{Sized, Nat}
import shapeless.ops.nat.*
import shapeless.ops.fin.FromNat
import FixedSizeCollection.FixedSizeType

opaque type FixedSizeCollection[S <: FixedSizeType, T, C[_]] = C[T]

object FixedSizeCollection:
  type FixedSizeType                         = Singleton & Int
  type FixedSizeArray[S <: FixedSizeType, T] = FixedSizeCollection[S, T, Array]
  type FixedSizeChunk[S <: FixedSizeType, T] = FixedSizeCollection[S, T, Chunk]
  case class IncorrectSizeError(actual: Int, Expected: Int)

  def apply[S <: FixedSizeType: ValueOf, T, C[_]](c: C[T])(using
      Size[C[T]]
  ): Either[IncorrectSizeError, FixedSizeCollection[S, T, C]] =
    if c.size == valueOf[S] then Right(c) else Left(IncorrectSizeError(c.size, valueOf[S]))

  def apply[S <: FixedSizeType: ValueOf, T](c: Array[T]): Either[IncorrectSizeError, FixedSizeArray[S, T]] =
    FixedSizeCollection[S, T, Array](c)

  def apply[S <: FixedSizeType: ValueOf, T](c: Chunk[T]): Either[IncorrectSizeError, FixedSizeChunk[S, T]] =
    FixedSizeCollection[S, T, Chunk](c)

  given [S1 <: FixedSizeType, S2 <: FixedSizeType, T: ClassTag, C[_]](using
      Semigroup[C[T]]
  ): Combine[FixedSizeCollection[S1, T, C], FixedSizeCollection[S2, T, C], FixedSizeCollection[
    S1 + S2 & Singleton,
    T,
    C
  ]] with
    extension (a: FixedSizeCollection[S1, T, C])
      def combine(b: FixedSizeCollection[S2, T, C]): FixedSizeCollection[S1 + S2 & Singleton, T, C] =
        Semigroup[C[T]].combine(a, b)

  given [S1 <: FixedSizeType, S2 <: FixedSizeType, T: ClassTag]
      : Combine[FixedSizeArray[S1, T], FixedSizeArray[S2, T], FixedSizeArray[S1 + S2 & Singleton, T]] with
    extension (a: FixedSizeArray[S1, T])
      def combine(b: FixedSizeArray[S2, T]): FixedSizeArray[S1 + S2 & Singleton, T] = a ++ b

  given [S1 <: FixedSizeType, S2 <: FixedSizeType, T: ClassTag]
      : Combine[FixedSizeChunk[S1, T], FixedSizeChunk[S2, T], FixedSizeChunk[S1 + S2 & Singleton, T]] with
    extension (a: FixedSizeChunk[S1, T])
      def combine(b: FixedSizeChunk[S2, T]): FixedSizeChunk[S1 + S2 & Singleton, T] = a ++ b

  extension [S <: FixedSizeType, T, C[_]](c: FixedSizeCollection[S, T, C]) 
    def unwrap: C[T] = c

  extension [S <: FixedSizeType, T: ClassTag](chunk: FixedSizeChunk[S, T])
    def toArray: FixedSizeArray[S, T] = chunk.toArray

  extension [S <: FixedSizeType, T, C[_]: Functor](c: FixedSizeCollection[S, (T, T), C])
    def unzip(): (FixedSizeCollection[S, T, C], FixedSizeCollection[S, T, C]) = Functor[C].unzip(c)

  extension [S <: FixedSizeType, T: ClassTag](c: FixedSizeArray[S, (T, T)])
    def unzip(): (FixedSizeArray[S, T], FixedSizeArray[S, T]) = c.unzip

  extension [S <: FixedSizeType, T](c: FixedSizeChunk[S, (T, T)])
    def unzip(): (FixedSizeChunk[S, T], FixedSizeChunk[S, T]) = c.unzip
