package ai.srl.observation

import ai.djl.ndarray.{NDList, NDManager}

trait DJLObservation[Observation]:
  extension (o: Observation) def toNDList(manager: NDManager): NDList
