package ai.srl.djl.extension

import ai.djl.training.listener.TrainingListener.BatchData
import ai.srl.collection.UpdateBatchPriorities
import ai.srl.djl.NDLists

import scala.collection.immutable.ArraySeq

object UpdateBatchPrioritiesExt:
  extension [T] (t: T)(using ubp: UpdateBatchPriorities[T])
    def updateLastBatch(batchData: BatchData): Unit =
      val pred = NDLists.concat(batchData.getPredictions.values())
      val label = NDLists.concatMap(batchData.getLabels.values(), _.reshape(-1, 1))
      val newPriorities = ArraySeq.unsafeWrapArray(label.sub(pred).abs().toFloatArray)
      ubp.updateLastBatch(t)(newPriorities)
