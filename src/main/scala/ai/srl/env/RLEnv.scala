package ai.srl.env

import ai.srl.collection.SizedChunk
import ai.srl.policy.Policy
import ai.srl.step.{EnvStep, SimpleStep}
import zio.ZIO.{fail, when}
import zio.{Chunk, Tag, ZIO, ZLayer}

trait RLEnv[Ac, +Observation, State]:
//  def reset(): ZIO[Any, Throwable, Unit]
//  def step(action: Ac): ZIO[Any, Throwable, EnvStep[Ac, Observation]]

  def foldZIO[E](
      initialState: State
  )(policy: Observation => ZIO[Any, Nothing, Ac], stateMapper: (Observation, State, Ac) => Either[E, State]): ZIO[Any, E, Chunk[State]]

object RLEnv:

  enum TimeseriesRLEnvError:
    case EnvSizeBiggerThanTimeseriesHistorySizes(envSize: Int, thSize: Int)
    case TimeseriesHistorySizeTooSmall(size: Int, minSize: Int)
    case EnvSizeTooSmallError(size: Int, minSize: Int)

  /** @tparam Ac
    *   Action
    * @tparam Observation
    * @tparam State
    * @tparam THS
    *   Timeseries history size
    * @return
    */
  // TODO You should probably just change it to a trait TimeseriesBanditRLEnv for simplicity
  def timeseriesRlEnvLayer[Ac: Tag, Observation: Tag, State: Tag, THS <: Int: ValueOf: Tag]: ZLayer[
    BanditEnv[Ac, Observation] & Policy[SizedChunk[THS, Observation], Ac],
    TimeseriesRLEnvError,
    RLEnv[Ac, SizedChunk[THS, Observation], State]
  ] = ZLayer {
    for
      banditEnv  <- ZIO.service[BanditEnv[Ac, Observation]]
      iterations <- validateTimeseriesRlEnvArguments(banditEnv.size(), valueOf[THS])
    yield new RLEnv[Ac, SizedChunk[THS, Observation], State]:
      override def foldZIO[E](initialState: State)(
          policy: SizedChunk[THS, Observation] => ZIO[Any, Nothing, Ac],
          stateMapper: (SizedChunk[THS, Observation], State, Ac) => Either[E, State]
      ): ZIO[Any, E, Chunk[State]] =
        val observations: Chunk[SizedChunk[THS, Observation]] = Chunk.range(0, iterations).map(banditEnv.observations[THS])
        for
          actions: Chunk[Ac] <- observations.mapZIO(policy)
          states <- observations.zip(actions).mapAccumZIO(initialState) { case (state, (observation, action)) =>
            ZIO.fromEither(stateMapper(observation, state, action)).map(newState => (newState, newState))
          }
        yield states._2
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

  def foldZIO[Ac: Tag, Observation: Tag, State: Tag, E: Tag](
      initialState: State
  )(
      policy: Observation => ZIO[Any, Nothing, Ac],
      stateMapper: (Observation, State, Ac) => Either[E, State]
  ): ZIO[RLEnv[Ac, Observation, State], E, Chunk[State]] =
    ZIO.serviceWithZIO[RLEnv[Ac, Observation, State]](_.foldZIO(initialState)(policy, stateMapper))

opaque type ActionReward[-Ac] = Ac => Float

trait BanditEnv[-Ac, Observation]:
  /** @param index
    *   0 to size()
    * @return
    */
  def actionReward(index: Int): ActionReward[Ac]

  /** @param startIndex
    *   inclusive >= 0
    * @return
    *   SizedChunk containing S observations starting from startIndex which does not allocate new observations but uses a view on BanditEnv
    *   internal Observation collection
    */
  def observations[S <: Int](startIndex: Int): SizedChunk[S, Observation]

  def size(): Int
