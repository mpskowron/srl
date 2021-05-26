package ai.srl.collection

import cats.data.{NonEmptyList, NonEmptySet}

import scala.collection.immutable.SortedSet
import eu.timepit.refined.*
import eu.timepit.refined.collection.*
import eu.timepit.refined.api.Refined
//import eu.timepit.refined.auto.*
//import eu.timepit.refined.numeric.*

import scala.collection.immutable.TreeSet

class NonEmptySortedSetTest extends munit.FunSuite:
  import scala.language.implicitConversions

  test("Construct only valid non empty sorted sets with refined") {
    val set        = SortedSet(4, 3, 8)
    val sortedSet  = refineV[NonEmpty](set)
    val emptyError = refineV[NonEmpty](SortedSet.empty[Int])

    assert(emptyError.isLeft)
    val sortedRefined: SortedSet[Int] Refined NonEmpty = sortedSet.toOption.get

    assertEquals(sortedRefined.value, set)
    val head = sortedRefined.value.head
    assertEquals(head, 3)
  }

  test("Construct only valid non empty sorted sets with cats") {
    val set            = SortedSet(4, 3, 8)
    val nonEmptySetOpt = NonEmptySet.fromSet(set)
    val emptySetOpt    = NonEmptySet.fromSet(SortedSet.empty[Int])
    val nonEmptySet    = nonEmptySetOpt.get

    assert(emptySetOpt.isEmpty)
    assertEquals(nonEmptySet.toSortedSet, set)
    assertEquals(nonEmptySet.head, 3)
  }
