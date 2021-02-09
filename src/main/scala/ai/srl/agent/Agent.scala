package ai.srl.agent

import ai.djl.ndarray.{NDList, NDManager}
import ai.djl.training.Trainer
import ai.djl.training.dataset.Batch
import ai.srl.action.DJLAction
import ai.srl.env.RlEnv

trait Agent[Ac, E <: RlEnv[Ac, ?]]:

  def chooseAction(env: E, training: Boolean): Ac
  
  def trainBatch(batch: Batch): Unit
  
