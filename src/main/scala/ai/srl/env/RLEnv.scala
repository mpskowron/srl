package ai.srl.env

import ai.srl.policy.Policy
import ai.srl.step.{EnvStep, SimpleStep}
import zio.{Chunk, ZIO, Tag}

trait RLEnv[-Ac, +Observation, +StepOutput]:
//  def reset(): ZIO[Any, Throwable, Unit]
//  def step(action: Ac): ZIO[Any, Throwable, EnvStep[Ac, Observation]]

  def mapZIO(policy: Observation => ZIO[Any, Throwable, Ac]): ZIO[Any, Nothing, Chunk[StepOutput]]

object RLEnv:
  def mapZIO[Ac: Tag, Observation: Tag, StepOutput: Tag](
      policy: Observation => ZIO[Any, Throwable, Ac]
  ): ZIO[RLEnv[Ac, Observation, StepOutput], Nothing, Chunk[StepOutput]] =
    ZIO.serviceWithZIO[RLEnv[Ac, Observation, StepOutput]](_.mapZIO(policy))

opaque type ActionReward[-Ac] = Ac => Float

trait BanditEnv[-Ac, +Observation]:
  /** @param index
    *   0 to size()
    * @return
    */
  def actionReward(index: Int): ActionReward[Ac]

  /** @param startIndex
    *   inclusive >= 0
    * @param endIndex
    *   exclusive <= size()
    * @return
    */
  def observations(startIndex: Int, endIndex: Int): Chunk[Observation]

  def size(): Int
