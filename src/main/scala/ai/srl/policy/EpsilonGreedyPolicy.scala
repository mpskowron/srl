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
class EpsilonGreedyPolicy[Ac, E <: RlEnv[Ac, ?, ?], P <: Policy[Ac, E]](val basePolicy: P, val exploreRate: Tracker)extends Policy[Ac, E]:
  private var counter = 0
  private val random = Random()

  override def chooseAction(env: E): Ac =
    if random.nextDouble() < exploreRate.getNewValue(counterPlusPlus) then
      val actionSpace = env.getActionSpace()
      actionSpace(random.nextInt(actionSpace.size))
    else 
      basePolicy.chooseAction(env)

  private def counterPlusPlus =
    counter += 1
    counter - 1

object EpsilonGreedyPolicy:
  given [Ac, E <:RlEnv[Ac, ?, ?], P <: Policy[Ac, E] : Description : HasName]: Description[EpsilonGreedyPolicy[Ac, E, P]] 
  with
    extension (policy: EpsilonGreedyPolicy[Ac, E, P]) 
      def describe(): Seq[(String, String)] = Seq(
        ("policyName", policy.name)
      )
  
  given [Ac, E <:RlEnv[Ac, ?, ?], P <: Policy[Ac, E] : HasName]: HasName[EpsilonGreedyPolicy[Ac, E, P]] with
    extension (policy: EpsilonGreedyPolicy[Ac, E, P]) 
      def name = s"${policy.getClass.getSimpleName}(${policy.basePolicy.name})"

  given [Ac, E <:RlEnv[Ac, ?, ?], P <: Policy[Ac, E]: HasTrainer]: HasTrainer[EpsilonGreedyPolicy[Ac, E, P]] with
    extension (policy: EpsilonGreedyPolicy[Ac, E, P])
      def trainer = policy.basePolicy.trainer
