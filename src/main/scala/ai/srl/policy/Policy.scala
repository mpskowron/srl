package ai.srl.policy

import zio.ZIO

trait Policy[PolicyObservation, Action]:
  def reset(): ZIO[Any, Nothing, Unit]
  def action(observation: PolicyObservation): ZIO[Any, Nothing, Action]
