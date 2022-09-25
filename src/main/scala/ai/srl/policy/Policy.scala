package ai.srl.policy

import zio.ZIO

trait Policy[PolicyObservation, Action]:
  def reset(): ZIO[Any, Throwable, Unit]
  def action(observation: PolicyObservation): ZIO[Any, Throwable, Action]
