package ai.srl.collection

import scala.compiletime.ops.int.*
import ai.srl.compiletime.Ops.*
import cats.data.Chain.Singleton
import eu.timepit.refined.api.Refined
import eu.timepit.refined.numeric.Interval.ClosedOpen
import IndexedElement.Index
import ai.srl.collection.RefinedExtensions.SInt
import ai.srl.step.SimpleStep
import alleycats.Empty
import cats.kernel.Eq

case class IndexedElement[E, MIN <: SInt, MAX <: SInt](index: Index[MIN, MAX], element: E)

object IndexedElement:
  enum IndexingError:
    case IndexTooBigError(index: Int, maxValue: Int)
    case MinRefinementLowerThanZero(minValue: Int)
    case MinRefinementNotLowerThanMax(minValue: Int, maxValue: Int)

  type Index[MIN <: SInt, MAX <: SInt] = Int Refined ClosedOpen[MIN, MAX]

  extension [E, MIN <: SInt, MAX <: SInt](indexedElement: IndexedElement[E, MIN, MAX])
    def index: Index[MIN, MAX] = indexedElement._1
    def element: E             = indexedElement._2

  given indexedElementEqInstance[E: Eq, MIN <: SInt, MAX <: SInt]: Eq[IndexedElement[E, MIN, MAX]] with
    def eqv(x: IndexedElement[E, MIN, MAX], y: IndexedElement[E, MIN, MAX]): Boolean =
      if x.index.value == y.index.value then
        require(
          Eq.eqv[E](x.element, y.element),
          s"Found two indexed elements with same index but different values: (${x.element}, ${y.element})"
        )
        true
      else false
