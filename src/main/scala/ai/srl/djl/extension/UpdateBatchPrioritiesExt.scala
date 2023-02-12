package ai.srl.djl.extension

import ai.djl.training.listener.TrainingListener.BatchData
import ai.srl.collection.{IndexedElement, UpdateBatchPriorities}
import ai.srl.djl.NDLists

import scala.collection.immutable.ArraySeq

// TODO rename those functions to sth with 'Predictions and labels difference' and move them to DJLBatchable
object UpdateBatchPrioritiesExt:

  def extractBatchPriorities(
      batchData: BatchData
  ): Iterable[Float] =
    val pred  = NDLists.concat(batchData.getPredictions.values())
    val label = NDLists.concatMap(batchData.getLabels.values(), _.reshape(-1, 1))
    ArraySeq.unsafeWrapArray(label.sub(pred).abs().toFloatArray)

  def extractBatchPriorities(
      batchData: BatchData,
      indexes: Iterable[Int]
  ): Either[IllegalArgumentException, Iterable[IndexedElement[Float]]] =
    val pred           = NDLists.concat(batchData.getPredictions.values())
    val label          = NDLists.concatMap(batchData.getLabels.values(), _.reshape(-1, 1))
    val labelPredDiffs = ArraySeq.unsafeWrapArray(label.sub(pred).abs().toFloatArray)
    if indexes.size != labelPredDiffs.length then
      Left(IllegalArgumentException(s"Size of training results ${labelPredDiffs.length} is different than expected ${indexes.size}"))
    else Right(indexes.zip(labelPredDiffs).map(IndexedElement.apply))

  extension [T](t: T)(using ubp: UpdateBatchPriorities[T])
    def updateLastBatch(batchData: BatchData): Unit =
      ubp.updateLastBatch(t)(extractBatchPriorities(batchData))
