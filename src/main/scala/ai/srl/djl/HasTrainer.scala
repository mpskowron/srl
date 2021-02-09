package ai.srl.djl

import ai.djl.ndarray.NDManager
import ai.djl.training.Trainer

trait HasTrainer[T]:
  extension (t: T) 
    def trainer: Trainer

object HasTrainer:
  given [T: HasTrainer]: HasManager[T] with
    extension (t: T)
      def manager: NDManager = t.trainer.getManager()