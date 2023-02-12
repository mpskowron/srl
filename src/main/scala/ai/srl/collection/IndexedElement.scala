package ai.srl.collection

import ai.djl.ndarray.{NDList, NDManager}
import ai.srl.action.DJLAction
import ai.srl.batch.{DJLBatchData, DJLBatchLabel}
import ai.srl.observation.DJLNNInput
import ai.srl.step.EnvStep

case class IndexedElement[+A](index: Int, element: A):
  def toTuple: (Int, A) = (index, element)

object IndexedElement:
  given [A: DJLBatchData]: DJLBatchData[IndexedElement[A]] with
    extension (e: IndexedElement[A])
      def toDJLBatchDataItem(using NDManager): NDList = e.element.toDJLBatchDataItem

  given [A: DJLBatchLabel]: DJLBatchLabel[IndexedElement[A]] with
    extension (e: IndexedElement[A])
      def toDJLLabelItem(using manager: NDManager): NDList = e.element.toDJLLabelItem

