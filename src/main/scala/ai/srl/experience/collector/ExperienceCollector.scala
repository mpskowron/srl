package ai.srl.experience.collector

import ai.djl.ndarray.NDManager
import ai.djl.training.dataset.Batch
import ai.srl.action.DJLAction
import ai.srl.agent.Agent
import ai.srl.env.RlEnv
import ai.srl.policy.Policy

import scala.annotation.tailrec

trait ExperienceCollector[Ac, E <: RlEnv[Ac, ?, ?], P <: Policy[Ac, E]]:

  /**
   * Executes one action on the environment and saves obtained transition
   * @param env
   * @param policy
   * @return Some(reward) or None if environment has already ended
   */
  def collect(env: E, policy: P): Option[Float]
  
  def collectAll(env: E, policy: P): Option[Float] =
    collect(env, policy).map(collectAllRec(env, policy, _))

  @tailrec
  private def collectAllRec(env: E, policy: P, accumulator: Float): Float =
    val collected = collect(env, policy)
    if collected.isDefined then
      collectAllRec(env, policy, accumulator + collected.get)
    else
      accumulator
  
  /**
   * Executes n actions on the environment and saves obtained transitions
   * @param env
   * @param policy
   * @param n >0 number of transitions to collect
   * @return Some(summed reward) or None if no steps were collected
   */
  def collectN(env: E, policy: P, n: Int): Option[Float] =
    assert(n > 0)
    collect(env, policy).map(collectNRec(env, policy, n-1, _))

  @tailrec
  private def collectNRec(env: E, policy: P, n: Int, accumulator: Float): Float =
    if n > 0 then
      val collected = collect(env, policy)
      if collected.isDefined then
        collectNRec(env, policy, n-1, accumulator + collected.get)
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
  def collectBatch(env: E, policy: P): Option[Float]

  /**
   * 
   * @param manager
   * @return None if collector haven't collected enough samples yet, Some(batch) otherwise
   */
  def getBatch(manager: NDManager): Option[Batch]

  def getCurrentBufferSize(): Int

  def getBatchSize(): Int
