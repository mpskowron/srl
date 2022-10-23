package ai.srl.collection

import ai.srl.collection.Combine
import ai.srl.collection.SizedChunk.FixedSizeType
import ai.srl.collection.Tuples.fmap
import cats.{Functor, Monoid, Semigroup}
import zio.Chunk

import scala.collection.SortedMap
import scala.compiletime.ops.int.*
import scala.reflect.ClassTag

class SizedSortedMap[S <: Int: ValueOf, K, +V](val map: SortedMap[K, V]) extends SortedMap[K, V]:
  require(
    valueOf[S] == map.size,
    s"[BUG] Attempt to create a sized map with incorrect size ${map.size} not equal to ${valueOf[S]}"
  )
  export map.*
