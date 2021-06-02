package ai.srl.collection

trait Size[T]:
  extension (t: T) def size: Int

object Size:
  given [T, S <: Seq[T]]: Size[S] with
    extension (t: S) def size: Int = t.size

  given [T]: Size[Array[T]] with
    extension (arr: Array[T]) def size: Int = arr.length
