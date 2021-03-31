package ai.srl.experience.collector

import ai.djl.ndarray.NDManager
import ai.djl.training.dataset.Batch
import ai.srl.action.DJLAction
import ai.srl.agent.Agent
import ai.srl.env.RlEnv
import ai.srl.policy.Policy
import ai.srl.collection.GetBatch

import scala.annotation.tailrec

trait ExperienceCollector[Action, Observation]:

  def reset(): Unit
  
  /**
   * Executes one action on the environment and saves obtained transition
   * @param env
   * @param policy
   * @return Some(reward) or None if environment has already ended
   */
  def collect[P](policy: P)(using Policy[P, Action, Observation]): Option[Float]
  
  def collectAll[P](policy: P)(using Policy[P, Action, Observation]): (Float, Int) =
    collect(policy).map(collectAllRec(policy, _, 1)).getOrElse((0f, 0))

  @tailrec
  private def collectAllRec[P](policy: P, accumulator: Float, collectedN: Int)(using Policy[P, Action, Observation]): (Float, Int) =
    val collectedItem = collect(policy)
    if collectedItem.isDefined then
      collectAllRec(policy, accumulator + collectedItem.get, collectedN + 1)
    else
      (accumulator, collectedN)

  /**
   * Used for validation and testing phases
   * @param policy
   * @param _
   * @tparam P
   * @return
   */
  def step[P](policy: P)(using Policy[P, Action, Observation]): Option[Float]
  
  def stepAll[P](policy: P)(using Policy[P, Action, Observation]): (Float, Int) =
    step(policy).map(stepAllRec(policy, _, 1)).getOrElse((0f, 0))

  @tailrec
  private def stepAllRec[P](policy: P, accumulator: Float, collectedN: Int)(using Policy[P, Action, Observation]): (Float, Int) =
    val reward = step(policy)
    if reward.isDefined then
      stepAllRec(policy, accumulator + reward.get, collectedN + 1)
    else
      (accumulator, collectedN)

  /**
   * Executes n actions on the environment and saves obtained transitions
   * @param env
   * @param policy
   * @param n >0 number of transitions to collect
   * @return Some(summed reward) or None if no steps were collected
   */
  def collectN[P](policy: P, n: Int)(using Policy[P, Action, Observation]): Option[Float] =
    assert(n > 0)
    collect(policy).map(collectNRec(policy, n-1, _))

  @tailrec
  private def collectNRec[P](policy: P, n: Int, accumulator: Float)(using Policy[P, Action, Observation]): Float =
    if n > 0 then
      val collected = collect(policy)
      if collected.isDefined then
        collectNRec(policy, n-1, accumulator + collected.get)
      else
        accumulator
    else
      accumulator

        /**
   * Executes n (expected to be one of the class parameters) actions on the environment and saves obtained transitions
   * @param env
   * @param policy
   * @return Some(summed reward) or None if no steps were collected
   */
  def collectBatch[P](policy: P)(using Policy[P, Action, Observation]): Option[Float]

  /**
   * 
   * @param manager
   * @return None if collector haven't collected enough samples yet, Some(batch) otherwise
   */
  def getBatch(manager: NDManager): Option[Batch]

  def getCurrentBufferSize(): Int

  def getBatchSize(): Int
