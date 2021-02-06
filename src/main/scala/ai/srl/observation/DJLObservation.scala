package ai.srl.observation

import ai.djl.ndarray.{NDList, NDManager}

trait DJLObservation:
  def toNDList(manager: NDManager): NDList
