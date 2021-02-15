package ai.srl.agent

import ai.djl.ndarray.NDList
import ai.djl.training.dataset.Batch
import ai.djl.training.tracker.Tracker
import ai.djl.util.RandomUtils
import ai.srl.djl.{HasManager, HasTrainer}
import ai.srl.logging.Description
import ai.srl.logging.HasName
import ai.srl.logging.HasName
import ai.srl.observation.DJLObservation
import ai.srl.logging.given
import scala.util.Random
import ai.srl.env.RlEnv

import ai.srl.action.DJLAction

// TODO Should be tested (Do I have tests for it in another project?)
class EpsilonGreedy[Ac, E <: RlEnv[Ac, ?, ?], TC, TR, A <: Agent[Ac, E, TC, TR]](val baseAgent: A, val exploreRate: Tracker)extends Agent[Ac, E, TC, TR]:
  private var counter = 0
  private val random = Random()

  override def chooseAction(env: E, training: Boolean): Ac =
    if training && random.nextDouble() < exploreRate.getNewValue(counterPlusPlus) then
      val actionSpace = env.getActionSpace()
      actionSpace(random.nextInt(actionSpace.size))
    else 
      baseAgent.chooseAction(env, training)

  override def trainBatch(trainContext: TC): TR =
    baseAgent.trainBatch(trainContext)
  
  private def counterPlusPlus =
    counter += 1
    counter - 1

object EpsilonGreedy:
  given [Ac, E <:RlEnv[Ac, ?, ?], TC, TR, A <: Agent[Ac, E, TC, TR] : Description : HasName]: Description[EpsilonGreedy[Ac, E, TC, TR, A]] 
  with
    extension (agent: EpsilonGreedy[Ac, E, TC, TR, A]) 
      def describe(): Seq[(String, String)] = Seq(
        ("agentName", agent.name)
      )
  
  given [Ac, E <:RlEnv[Ac, ?, ?], TC, TR, A <: Agent[Ac, E, TC, TR] : HasName]: HasName[EpsilonGreedy[Ac, E, TC, TR, A]] with
    extension (agent: EpsilonGreedy[Ac, E, TC, TR, A]) 
      def name = s"${agent.getClass.getSimpleName}(${agent.baseAgent.name})"

  given [Ac, E <:RlEnv[Ac, ?, ?], TC, TR, A <: Agent[Ac, E, TC, TR]: HasTrainer]: HasTrainer[EpsilonGreedy[Ac, E, TC, TR, A]] with
    extension (agent: EpsilonGreedy[Ac, E, TC, TR, A])
      def trainer = agent.baseAgent.trainer
