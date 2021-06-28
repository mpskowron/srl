package ai.srl.collection

trait Size[C]:
  extension (t: C) def size: Int

object Size:
  given [T, C <: Seq[T]]: Size[C] with
    extension (t: C) def size: Int = t.size

  given [T]: Size[Array[T]] with
    extension (arr: Array[T]) def size: Int = arr.length
