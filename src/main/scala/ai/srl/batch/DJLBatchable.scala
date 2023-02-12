package ai.srl.batch

import ai.djl.ndarray.{NDList, NDManager}
import ai.djl.training.dataset.Batch
import ai.djl.translate.Batchifier
import ai.srl.action.DJLAction
import ai.srl.observation.DJLNNInput
import ai.srl.step.EnvStep

trait DJLBatchable[C]:
  extension (b: C) def toDJLBatch(using Batchifier, NDManager): Batch

trait DJLBatchData[BD]:
  extension (b: BD) def toDJLBatchDataItem(using NDManager): NDList

trait DJLBatchLabel[L]:
  extension (l: L) def toDJLLabelItem(using NDManager): NDList

object DJLBatchable:
  def buildBatch[Item: DJLBatchLabel: DJLBatchData](
      observations: Iterable[Item]
  )(using batchifier: Batchifier, manager: NDManager): Batch =
    // TODO avoid toArray to speed up execution time (would it actually be a significant speed up?)
    val data   = batchifier.batchify(observations.map(step => step.toDJLBatchDataItem).toArray)
    val labels = batchifier.batchify(observations.map(step => step.toDJLLabelItem).toArray)
    new Batch(manager, data, labels, observations.size, batchifier, batchifier, 0, 0)

  def buildDataRow[Obs: DJLNNInput, Ac: DJLAction](obs: Obs, action: Ac)(using manager: NDManager): NDList =
    new NDList().addAll(obs.toNDList(manager)).addAll(action.toNDList(manager))

  given djlIterableBatchable[Item: DJLBatchLabel: DJLBatchData, C <: Iterable[Item]]: DJLBatchable[C] with
    extension (c: C) def toDJLBatch(using Batchifier, NDManager): Batch = buildBatch(c)

  given djlArrayBatchable[Item: DJLBatchLabel: DJLBatchData, C <: Array[Item]]: DJLBatchable[C] with
    extension (c: C) def toDJLBatch(using Batchifier, NDManager): Batch = buildBatch(c)

  given djlBatchBatchable: DJLBatchable[Batch] with
    extension (batch: Batch) def toDJLBatch(using Batchifier, NDManager): Batch = batch
