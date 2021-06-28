package ai.srl.collection
import ai.srl.collection.FixedSizeCollection.FixedSizeType

opaque type SameSizeCollections[S <: FixedSizeType, T, C[_]] =
  (FixedSizeCollection[S, T, C], FixedSizeCollection[S, T, C])

object SameSizeCollections:
  def apply[S <: FixedSizeType, T, C[_]](
      sameSizeCollections: (FixedSizeCollection[S, T, C], FixedSizeCollection[S, T, C])
  ): SameSizeCollections[S, T, C] = sameSizeCollections

  extension [S <: FixedSizeType, T, C[_]](sameSizeCollections: SameSizeCollections[S, T, C])
    def unwrap: (FixedSizeCollection[S, T, C], FixedSizeCollection[S, T, C]) = sameSizeCollections
    def _1: FixedSizeCollection[S, T, C] = unwrap._1
    def _2: FixedSizeCollection[S, T, C] = unwrap._2
