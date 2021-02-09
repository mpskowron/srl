package ai.srl.djl

import ai.djl.ndarray.NDManager

trait HasManager[T]:
  extension (t: T) def manager: NDManager
