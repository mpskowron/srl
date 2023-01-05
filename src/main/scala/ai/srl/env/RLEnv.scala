package ai.srl.env

import ai.srl.collection.SizedChunk
import ai.srl.env.RLEnv.RLEnvObs
import ai.srl.policy.Policy
import ai.srl.step.{EnvStep, SimpleStep}
import zio.ZIO.{fail, fromEither, when}
import zio.{Chunk, Tag, ZIO, ZLayer}

/** @tparam Action
  *   Action that Agent can perform. As an alternative implementation AgentState could be deleted and moved as a part of the Action
  * @tparam EnvState
  *   See [[RLEnv.RLEnvObs.envState]]
  * @tparam AgentState
  *   See [[RLEnv.RLEnvObs.agentState]]
  */
trait RLEnv[Action, +EnvState, AgentState]:
//  def reset(): ZIO[Any, Throwable, Unit]
//  def step(action: Ac): ZIO[Any, Throwable, EnvStep[Ac, Observation]]

  def foldZIO[PolicyError, StateMapperError](initialState: AgentState)(
      policy: RLEnvObs[EnvState, AgentState] => ZIO[Any, PolicyError, Action],
      stateMapper: (EnvState, AgentState, Action) => Either[StateMapperError, AgentState]
  ): ZIO[Any, PolicyError | StateMapperError, Chunk[AgentState]]

object RLEnv:

  /** @param envState
    *   State of the environment if the Agent wasn't there (e.g. position of the crossroad)
    * @param agentState
    *   A state of the agent in environment, part of state agent can directly influence (e.g. number of bullets in the pocket); if envState
    *   is bandit, this might be a nonbandit "part of"/"addition to" the envState
    */
  case class RLEnvObs[+EnvState, +AgentState](envState: EnvState, agentState: AgentState)

  /** Timeseries Observation
    * @tparam TS
    *   Timeseries size
    */
  type TsObs[EnvState, +AgentState, TS <: Int] = RLEnvObs[SizedChunk[TS, EnvState], AgentState]

  enum TimeseriesRLEnvError:
    case EnvSizeBiggerThanTimeseriesHistorySizes(envSize: Int, thSize: Int)
    case TimeseriesHistorySizeTooSmall(size: Int, minSize: Int)
    case EnvSizeTooSmallError(size: Int, minSize: Int)

  /** @tparam Ac
    *   Action
    * @tparam EnvState
    * @tparam AgentState
    * @tparam TS
    *   Timeseries size
    * @return
    */
  // TODO You should probably just change it to a trait TimeseriesBanditRLEnv for simplicity
  def timeseriesRlEnvLayer[Ac: Tag, EnvState: Tag, AgentState: Tag, TS <: Int: ValueOf: Tag]
      : ZLayer[IndexedObservations[EnvState], TimeseriesRLEnvError, RLEnv[Ac, SizedChunk[TS, EnvState], AgentState]] = ZLayer {
    for
      banditEnv     <- ZIO.service[IndexedObservations[EnvState]]
      rlEnvMaxIndex <- validateTimeseriesRlEnvArguments(banditEnv.size(), valueOf[TS])
      indexedObservations: Chunk[SizedChunk[TS, EnvState]] <- Chunk
        .range(0, rlEnvMaxIndex)
        .mapZIO(index => fromEither(banditEnv.observations[TS](index)))
        .orDie
    yield new RLEnv[Ac, SizedChunk[TS, EnvState], AgentState]:
      override def foldZIO[PE, SME](initialState: AgentState)(
          policy: RLEnvObs[SizedChunk[TS, EnvState], AgentState] => ZIO[Any, PE, Ac],
          stateMapper: (SizedChunk[TS, EnvState], AgentState, Ac) => Either[SME, AgentState]
      ): ZIO[Any, PE | SME, Chunk[AgentState]] =
        indexedObservations.mapAccumZIO(initialState)((agentState, envState) =>
          for
            action        <- policy(RLEnvObs(envState, agentState))
            newAgentState <- ZIO.fromEither[PE | SME, AgentState](stateMapper(envState, agentState, action))
          yield (newAgentState, newAgentState)
        ) map (_._2)
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

  def foldZIO[Ac: Tag, EnvState: Tag, AgentState: Tag, PErr, MErr](initialState: AgentState)(
      policy: RLEnvObs[EnvState, AgentState] => ZIO[Any, PErr, Ac],
      stateMapper: (EnvState, AgentState, Ac) => Either[MErr, AgentState]
  ): ZIO[RLEnv[Ac, EnvState, AgentState], MErr | PErr, Chunk[AgentState]] =
    ZIO.serviceWithZIO[RLEnv[Ac, EnvState, AgentState]](_.foldZIO(initialState)(policy, stateMapper))

opaque type ActionReward[-State, -Ac] <: (State, Ac) => Float = (State, Ac) => Float

object ActionReward:
  def apply[State, Ac](f: (State, Ac) => Float): ActionReward[State, Ac] = f

trait IndexedObservations[Observation]:

  /** @param startIndex
    *   inclusive >= 0
    * @return
    *   SizedChunk containing S observations starting from startIndex which does not allocate new observations but uses a view on BanditEnv
    *   internal Observation collection
    */
  def observations[S <: Int: ValueOf](startIndex: Int): Either[IndexOutOfBoundsException, SizedChunk[S, Observation]]

  def size(): Int

trait IndexedActionRewards[-Ac, -State]:
  /** @param index
    *   0 to size()
    * @return
    */
  def actionReward(index: Int): Either[IndexOutOfBoundsException, ActionReward[State, Ac]]

  def size(): Int

trait IndexedBanditEnv[-Action, EnvState, -AgentState] extends IndexedObservations[EnvState] with IndexedActionRewards[Action, AgentState]
