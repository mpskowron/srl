package ai.srl.policy

import ai.djl.ndarray.{NDArray, NDList}
import ai.djl.training.Trainer
import ai.djl.translate.Batchifier
import ai.srl.action.DJLAction
import ai.srl.action.DJLAction.*
import ai.srl.env.RlEnv
import ai.srl.observation.DJLNNInput
import ai.srl.djl.Manager
import ai.srl.djl.Manager.ManagerCreationError
import zio.{Layer, Unsafe, ZIO, ZLayer}

import java.util
import scala.util.Using

final case class DJLMaxRewardPolicy[Action: DJLAction, Observation: DJLNNInput](
    // TODO change to some kind of ZIOTrainer which will pack the trainer inside a ZIO trait
    trainer: Trainer,
    batchifier: Batchifier
) extends Policy[Observation, ManagerCreationError, Action]:

  // TODO Probably it can be changed to much more concise Scala implementation
  private def buildInputs(observation: NDList, actions: util.List[NDList]): Array[NDList] =
    val inputs: Array[NDList] = new Array[NDList](actions.size())
    for i <- 0 until actions.size do
      val nextData: NDList = new NDList().addAll(observation).addAll(actions.get(i))
      inputs(i) = nextData
    inputs

  def action(observation: Observation): ZIO[Any, ManagerCreationError, Action] =
    ZIO.scoped {
      for
        manager <- Manager.subManager(trainer.getManager)
        actionSpace  = summon[DJLAction[Action]].getActionSpace
        inputs       = batchifier.batchify(buildInputs(observation.toNDList(manager), actionSpace.toActionSpace(manager)))
        actionScores = trainer.evaluate(inputs).singletonOrThrow.squeeze(-1)
        bestAction   = Math.toIntExact(actionScores.argMax().getLong())
      yield actionSpace(bestAction)
    }

object DJLMaxRewardPolicy:
  // TODO Make sure that the observation passed here doesn't already include the action in it

  given [Ac: DJLAction, Obs: DJLNNInput]: PurePolicy[DJLMaxRewardPolicy[Ac, Obs], Ac, Obs] with
    extension (p: DJLMaxRewardPolicy[Ac, Obs])
      def chooseAction(actionSpace: Vector[Ac], observation: Obs): Ac =
        Unsafe.unsafe { implicit unsafe =>
          zio.Runtime.default.unsafe.run(p.action(observation)).getOrThrowFiberFailure()
        }

  import zio.config.ReadError
  import zio.Tag
  def layer[Action: DJLAction: Tag, Observation: DJLNNInput: Tag]: ZLayer[Trainer, Nothing, DJLMaxRewardPolicy[Action, Observation]] =
    ZLayer.fromFunction((trainer: Trainer) => DJLMaxRewardPolicy(trainer = trainer, batchifier = Batchifier.STACK))
