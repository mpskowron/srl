package ai.srl.reward

import ai.srl.action.DJLAction
import ai.srl.env.RlEnv

trait Reward[R, Ac, E <: RlEnv[Ac, ?, ?]]:
  extension (r: R)
    /**
     * @param env Assummed to be in a state after executing an action
     * @param action Action which led environment ot its current state
     * @return
     */
    def reward(env: E, action: Ac): Double

    /**
     * @param env
     * @return false if this is the last step of env in which reward method can be invoked, true otherwise
     */
    def isDone(env: E): Boolean

    /**
     * Needs to be called as a part of env's reset method
     * @param env
     */
    def reset(env: E): Unit
