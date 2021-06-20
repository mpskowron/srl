package ai.srl.collection

import cats.{Monoid, Semigroup}
import ai.srl.collection.Combine

import scala.reflect.ClassTag
import scala.compiletime.ops.int.*
import zio.Chunk

type FixedSize = Singleton & Int

opaque type FixedSizeCollection[S <: Int, T, C[_]] = C[T]

object FixedSizeCollection:

  type FixedSizeArray[S <: Int, T] = FixedSizeCollection[S, T, Array]
  type FixedSizeChunk[S <: Int, T] = FixedSizeCollection[S, T, Chunk]
  case class IncorrectSizeError(actual: Int, Expected: Int)

  def apply[S <: FixedSize: ValueOf, T, C[_]](c: C[T])(using
      Size[C[T]]
  ): Either[IncorrectSizeError, FixedSizeCollection[S, T, C]] =
    if c.size == valueOf[S] then Right(c) else Left(IncorrectSizeError(c.size, valueOf[S]))

  def apply[S <: FixedSize: ValueOf, T](c: Array[T]): Either[IncorrectSizeError, FixedSizeArray[S, T]] =
    FixedSizeCollection[S, T, Array](c)

  def apply[S <: FixedSize: ValueOf, T](c: Chunk[T]): Either[IncorrectSizeError, FixedSizeChunk[S, T]] =
    FixedSizeCollection[S, T, Chunk](c)

  given [S1 <: FixedSize, S2 <: FixedSize, T: ClassTag, C[_]](using
      Semigroup[C[T]]
  ): Combine[FixedSizeCollection[S1, T, C], FixedSizeCollection[S2, T, C], FixedSizeCollection[S1 + S2, T, C]] with
    extension (a: FixedSizeCollection[S1, T, C])
      def combine(b: FixedSizeCollection[S2, T, C]): FixedSizeCollection[S1 + S2, T, C] = Semigroup[C[T]].combine(a, b)

  given [S1 <: FixedSize, S2 <: FixedSize, T: ClassTag]
      : Combine[FixedSizeArray[S1, T], FixedSizeArray[S2, T], FixedSizeArray[S1 + S2, T]] with
    extension (a: FixedSizeArray[S1, T]) def combine(b: FixedSizeArray[S2, T]): FixedSizeArray[S1 + S2, T] = a ++ b

  given [S1 <: FixedSize, S2 <: FixedSize, T: ClassTag]
      : Combine[FixedSizeChunk[S1, T], FixedSizeChunk[S2, T], FixedSizeChunk[S1 + S2, T]] with
    extension (a: FixedSizeChunk[S1, T]) def combine(b: FixedSizeChunk[S2, T]): FixedSizeChunk[S1 + S2, T] = a ++ b

  extension [S <: Int, T, C[_]](c: FixedSizeCollection[S, T, C]) def unwrap: C[T] = c

  extension [S <: Int, T: ClassTag](chunk: FixedSizeChunk[S, T]) def toArray: FixedSizeArray[S, T] = chunk.toArray
