package ai.srl.collection

import RefinedExtensions.SInt

class SizedTypeWrapper[SizedType[_ <: SInt]](val size: Int, val c: SizedType[size.type])

object SizedTypeWrapper:
  type SizedType2[T, C[_ <: SInt, _]] = [S <: SInt] =>> C[S, T]
