package ai.srl.policy

import ai.djl.ndarray.NDList
import ai.djl.training.dataset.Batch
import ai.djl.training.tracker.Tracker
import ai.djl.util.RandomUtils
import ai.srl.djl.{HasManager, HasTrainer}
import ai.srl.logging
import ai.srl.logging.{Description, HasName}
import ai.srl.action.DJLAction
import ai.srl.env.RlEnv
import ai.srl.policy.Policy

import scala.util.Random

// TODO Should be tested (Do I have tests for it in another project?)
class EpsilonGreedyPolicy[Ac, Obs, P](val basePolicy: P, val exploreRate: Tracker)(using Policy[P, Ac, Obs]):
  private var counter = 0
  private val random = Random()

  private def counterPlusPlus =
    counter += 1
    counter - 1

object EpsilonGreedyPolicy:
  given [Ac, Obs, P : Description : HasName](using Policy[P, Ac, Obs]) : Description[EpsilonGreedyPolicy[Ac, Obs, P]] 
  with
    extension (policy: EpsilonGreedyPolicy[Ac, Obs, P]) 
      def describe(): Seq[(String, String)] = Seq(
        ("policyName", policy.name)
      )
  
  given [Ac, Obs, P : HasName](using Policy[P, Ac, Obs]) : HasName[EpsilonGreedyPolicy[Ac, Obs, P]] with
    extension (policy: EpsilonGreedyPolicy[Ac, Obs, P]) 
      def name = s"${policy.getClass.getSimpleName}(${policy.basePolicy.name})"

  given [Ac, Obs, P: HasTrainer](using Policy[P, Ac, Obs]): HasTrainer[EpsilonGreedyPolicy[Ac, Obs, P]] with
    extension (policy: EpsilonGreedyPolicy[Ac, Obs, P])
      def trainer = policy.basePolicy.trainer

  given [Ac, Obs, P](using Policy[P, Ac, Obs]): Policy[EpsilonGreedyPolicy[Ac, Obs, P], Ac, Obs] with
    extension (p: EpsilonGreedyPolicy[Ac, Obs, P])
      def chooseAction(actionSpace: Vector[Ac], observation: Obs): Ac =
        if p.random.nextDouble() < p.exploreRate.getNewValue(p.counterPlusPlus) then
          actionSpace(p.random.nextInt(actionSpace.size))
        else
          p.basePolicy.chooseAction(actionSpace, observation)

        
