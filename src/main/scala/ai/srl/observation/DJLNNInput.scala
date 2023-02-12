package ai.srl.observation

import ai.djl.ndarray.{NDList, NDManager}

/**
 * Deep Java Library Neural Network Input
 */
// TODO Maybe replace this by DJLListable or sth like that as implementors of this trait usually aren't used as a sole input
trait DJLNNInput[NNInput]:
  extension (o: NNInput) def toNDList(manager: NDManager): NDList
