package ai.srl.policy

import ai.djl.ndarray.{NDArray, NDList}
import ai.djl.training.Trainer
import ai.djl.translate.Batchifier
import ai.srl.action.DJLAction
import ai.srl.action.DJLAction.*
import ai.srl.env.RlEnv
import ai.srl.observation.DJLNNInput

import java.util
import scala.util.Using

final case class DJLMaxRewardPolicy[Action: DJLAction, Observation: DJLNNInput](
    trainer: Trainer,
    batchifier: Batchifier
):

  // TODO Probably it can be changed to much more concise Scala implementation
  private def buildInputs(observation: NDList, actions: util.List[NDList]): Array[NDList] =
    val inputs: Array[NDList] = new Array[NDList](actions.size())
    for i <- 0 until actions.size do
      val nextData: NDList = new NDList().addAll(observation).addAll(actions.get(i))
      inputs(i) = nextData
    inputs

object DJLMaxRewardPolicy:
  // TODO Make sure that the observation passed here doesn't already include the action in it
  given [Ac: DJLAction, Obs: DJLNNInput]: PurePolicy[DJLMaxRewardPolicy[Ac, Obs], Ac, Obs] with
    extension (p: DJLMaxRewardPolicy[Ac, Obs])
      def chooseAction(actionSpace: Vector[Ac], observation: Obs): Ac =
        Using.resource(p.trainer.getManager.newSubManager()) { manager =>
          val inputs = p.batchifier.batchify(
            p.buildInputs(observation.toNDList(manager), actionSpace.toActionSpace(manager))
          )
          val actionScores: NDArray = p.trainer
            .evaluate(inputs)
            .singletonOrThrow
            .squeeze(-1)
          val bestAction = Math.toIntExact(actionScores.argMax().getLong())
          actionSpace(bestAction)
        }
