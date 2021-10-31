package ai.srl.collection

import ai.srl.collection.RefinedExtensions.{SInt, SizedChunk}
import cats.kernel.Semigroup
import zio.Chunk
import eu.timepit.refined.collection.Size
import eu.timepit.refined.generic.Equal
import eu.timepit.refined.api.Refined
import eu.timepit.refined.numeric.Positive
import eu.timepit.refined.refineV

import scala.compiletime.ops.int.*
import eu.timepit.refined.internal.WitnessAs
import cats.Functor
import cats.syntax.all.toFunctorOps
import SizedTypeWrapper.SizedType2

object RefinedExtensions:
  type SInt                     = Int & Singleton
  type SizedChunk[S <: SInt, T] = Chunk[T] Refined Size[Equal[S]]

  def newSizedChunk[T](chunk: Chunk[T]): SizedTypeWrapper[SizedType2[T, SizedChunk]] =
    val s = chunk.size
    new SizedTypeWrapper[SizedType2[T, SizedChunk]](s, Refined.unsafeApply[Chunk[T], Size[Equal[s.type]]](chunk))
  
  given [S1 <: SInt, S2 <: SInt, T]: Combine[SizedChunk[S1, T], SizedChunk[S2, T], SizedChunk[S1 + S2 & Singleton, T]]
    with
    extension (a: SizedChunk[S1, T])
      def combine(b: SizedChunk[S2, T]): SizedChunk[S1 + S2 & Singleton, T] = Refined.unsafeApply(a.value ++ b.value)

  inline given singletonWitnessAs[B, A <: B: ValueOf]: WitnessAs[A, B] =
    val a = valueOf[A]
    WitnessAs(a, a)

  extension [S <: SInt, T](chunk: SizedChunk[S, T]) def toList: List[T] = chunk.value.toList

  extension [S <: SInt, T1, T2](chunk: SizedChunk[S, (T1, T2)])
    def unzip: (SizedChunk[S, T1], SizedChunk[S, T2]) =
      val (t1s, t2s) = chunk.value.unzip
      (Refined.unsafeApply(t1s), Refined.unsafeApply(t2s))

  type FRefined[F[_], P] = [T] =>> Refined[F[T], Size[P]]
  given [F[_]: Functor, P]: Functor[FRefined[F, P]] with
    override def map[A, B](fa: Refined[F[A], Size[P]])(f: A => B): Refined[F[B], Size[P]] =
      Refined.unsafeApply(fa.value.map(f))
