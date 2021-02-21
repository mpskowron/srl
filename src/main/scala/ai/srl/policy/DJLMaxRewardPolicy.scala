package ai.srl.policy

import ai.djl.ndarray.{NDArray, NDList}
import ai.djl.training.Trainer
import ai.djl.translate.Batchifier
import ai.srl.action.DJLAction
import ai.srl.action.DJLAction._
import ai.srl.env.RlEnv
import ai.srl.observation.DJLObservation

import java.util
import scala.util.Using

final class DJLMaxRewardPolicy[Action: DJLAction, Observation: DJLObservation, Environment <: RlEnv[Action, Observation, ?]]
(trainer: Trainer, batchifier: Batchifier) extends Policy[Action, Environment]:
  
  override def chooseAction(env: Environment): Action =
    val actionSpace = env.getActionSpace()
    Using.resource(trainer.getManager().newSubManager()) { manager =>
      val inputs = batchifier.batchify(
        buildInputs(env.getObservation().toNDList(manager), actionSpace.toActionSpace(manager))
      )
      val actionScores: NDArray = trainer
        .evaluate(inputs)
        .singletonOrThrow.squeeze(-1)
      val bestAction = Math.toIntExact(actionScores.argMax().getLong())
      actionSpace(bestAction)
    }

  // TODO Probably it can be changed to much more concise Scala implementation
  private def buildInputs(observation: NDList, actions: util.List[NDList]): Array[NDList] =
    val inputs: Array[NDList] = new Array[NDList](actions.size())
    for (i <- 0 until actions.size) {
      val nextData: NDList = new NDList().addAll(observation).addAll(actions.get(i))
      inputs(i) = nextData
    }
    inputs
