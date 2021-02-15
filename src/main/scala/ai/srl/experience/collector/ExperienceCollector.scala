package ai.srl.experience.collector

import ai.djl.ndarray.NDManager
import ai.djl.training.dataset.Batch
import ai.srl.action.DJLAction
import ai.srl.agent.Agent
import ai.srl.env.RlEnv

import scala.annotation.tailrec

trait ExperienceCollector[Ac, E <: RlEnv[Ac, ?, ?], A <: Agent[Ac, E, ?, ?]]:

  /**
   * Executes one action on the environment and saves obtained transition
   * @param env
   * @param agent
   * @return Some(reward) or None if environment has already ended
   */
  def collect(env: E, agent: A): Option[Float]

  /**
   * Executes n actions on the environment and saves obtained transitions
   * @param env
   * @param agent
   * @param n >0 number of transitions to collect
   * @return Some(summed reward) or None if no steps were collected
   */
  def collectN(env: E, agent: A, n: Int): Option[Float] =
    assert(n > 0)
    collect(env, agent).map(collectNRec(env, agent, n-1, _))

  @tailrec
  private def collectNRec(env: E, agent: A, n: Int, accumulator: Float): Float =
    if n > 0 then
      val collected = collect(env, agent)
      if collected.isDefined then
        collectNRec(env, agent, n-1, accumulator + collected.get)
      else
        accumulator
    else
      accumulator

        /**
   * Executes n (expected to be one of the class parameters) actions on the environment and saves obtained transitions
   * @param env
   * @param agent
   * @return Some(summed reward) or None if no steps were collected
   */
  def collectBatch(env: E, agent: A): Option[Float]

  /**
   * 
   * @param manager
   * @return None if collector haven't collected enough samples yet, Some(batch) otherwise
   */
  def getBatch(manager: NDManager): Option[Batch]

  def getCurrentBufferSize(): Int

  def getBatchSize(): Int
