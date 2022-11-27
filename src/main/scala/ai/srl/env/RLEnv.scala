package ai.srl.env

import ai.srl.collection.SizedChunk
import ai.srl.env.RLEnv.RLEnvObservation
import ai.srl.policy.Policy
import ai.srl.step.{EnvStep, SimpleStep}
import zio.ZIO.{fail, when}
import zio.{Chunk, Tag, ZIO, ZLayer}

/** @tparam Action
  *   Action that Agent can perform. As an alternative implementation AgentState could be deleted and moved as a part of the Action
  * @tparam EnvState
  *   See [[RLEnv.RLEnvObservation.envState]]
  * @tparam AgentState
  *   See [[RLEnv.RLEnvObservation.agentState]]
  */
trait RLEnv[Action, +EnvState, AgentState]:
//  def reset(): ZIO[Any, Throwable, Unit]
//  def step(action: Ac): ZIO[Any, Throwable, EnvStep[Ac, Observation]]

  def foldZIO[E](initialState: AgentState)(
      policy: (EnvState, AgentState) => ZIO[Any, Nothing, Action],
      stateMapper: (EnvState, AgentState, Action) => Either[E, AgentState]
  ): ZIO[Any, E, Chunk[AgentState]]

object RLEnv:

  /** @param envState
    *   State of the environment if the Agent wasn't there (e.g. position of the crossroad)
    * @param agentState
    *   A state of the agent in environment, part of state agent can directly influence (e.g. number of bullets in the pocket); if envState
    *   is bandit, this might be a nonbandit "part of"/"addition to" the envState
    */
  case class RLEnvObservation[+EnvState, AgentState](envState: EnvState, agentState: AgentState)

  enum TimeseriesRLEnvError:
    case EnvSizeBiggerThanTimeseriesHistorySizes(envSize: Int, thSize: Int)
    case TimeseriesHistorySizeTooSmall(size: Int, minSize: Int)
    case EnvSizeTooSmallError(size: Int, minSize: Int)

  /** @tparam Ac
    *   Action
    * @tparam EnvState
    * @tparam AgentState
    * @tparam THS
    *   Timeseries history size
    * @return
    */
  // TODO You should probably just change it to a trait TimeseriesBanditRLEnv for simplicity
  def timeseriesRlEnvLayer[Ac: Tag, EnvState: Tag, AgentState: Tag, THS <: Int: ValueOf: Tag]: ZLayer[
    IndexedObservations[EnvState] & Policy[(SizedChunk[THS, EnvState], AgentState), Ac],
    TimeseriesRLEnvError,
    RLEnv[Ac, SizedChunk[THS, EnvState], AgentState]
  ] = ZLayer {
    for
      banditEnv  <- ZIO.service[IndexedObservations[EnvState]]
      iterations <- validateTimeseriesRlEnvArguments(banditEnv.size(), valueOf[THS])
    yield new RLEnv[Ac, SizedChunk[THS, EnvState], AgentState]:
      override def foldZIO[E](initialState: AgentState)(
          policy: (SizedChunk[THS, EnvState], AgentState) => ZIO[Any, Nothing, Ac],
          stateMapper: (SizedChunk[THS, EnvState], AgentState, Ac) => Either[E, AgentState]
      ): ZIO[Any, E, Chunk[AgentState]] = Chunk
        .range(0, iterations)
        .map(banditEnv.observations[THS])
        .mapAccumZIO(initialState)((agentState, envState) =>
          for
            action        <- policy(envState, agentState)
            newAgentState <- ZIO.fromEither(stateMapper(envState, agentState, action))
          yield (newAgentState, newAgentState)
        )
        .map(_._2)
  }

  /** @param banditEnvSize
    * @param timeseriesHistorySize
    * @return
    *   size of RLEnvironment
    */
  private def validateTimeseriesRlEnvArguments(
      banditEnvSize: Int,
      timeseriesHistorySize: Int
  ): ZIO[Any, TimeseriesRLEnvError, Int] = for
    rlEnvSize: Int <- ZIO.succeed(banditEnvSize - timeseriesHistorySize)
    _              <- when(timeseriesHistorySize < 1)(fail(TimeseriesRLEnvError.TimeseriesHistorySizeTooSmall(timeseriesHistorySize, 1)))
    _              <- when(banditEnvSize < 1)(fail(TimeseriesRLEnvError.EnvSizeTooSmallError(banditEnvSize, 1)))
    _ <- when(rlEnvSize < 0)(fail(TimeseriesRLEnvError.EnvSizeBiggerThanTimeseriesHistorySizes(banditEnvSize, timeseriesHistorySize)))
  yield rlEnvSize

  def foldZIO[Ac: Tag, EnvState: Tag, AgentState: Tag, E: Tag](
      initialState: AgentState
  )(
      policy: (EnvState, AgentState) => ZIO[Any, Nothing, Ac],
      stateMapper: (EnvState, AgentState, Ac) => Either[E, AgentState]
  ): ZIO[RLEnv[Ac, EnvState, AgentState], E, Chunk[AgentState]] =
    ZIO.serviceWithZIO[RLEnv[Ac, EnvState, AgentState]](_.foldZIO(initialState)(policy, stateMapper))

opaque type ActionReward[-State, -Ac] = (State, Ac) => Float

trait IndexedObservations[Observation]:

  /** @param startIndex
    *   inclusive >= 0
    * @return
    *   SizedChunk containing S observations starting from startIndex which does not allocate new observations but uses a view on BanditEnv
    *   internal Observation collection
    */
  def observations[S <: Int](startIndex: Int): SizedChunk[S, Observation]

  def size(): Int

trait IndexedActionRewards[-Ac, -State]:
  /** @param index
    *   0 to size()
    * @return
    */
  def actionReward(index: Int): ActionReward[State, Ac]

  def size(): Int

trait IndexedBanditEnv[-Action, EnvState, -AgentState] extends IndexedObservations[EnvState] with IndexedActionRewards[Action, AgentState]
