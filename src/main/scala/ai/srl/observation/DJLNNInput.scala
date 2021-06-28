package ai.srl.observation

import ai.djl.ndarray.{NDList, NDManager}

/**
 * Deep Java Library Neural Network Input
 */
trait DJLNNInput[NNInput]:
  extension (o: NNInput) def toNDList(manager: NDManager): NDList
