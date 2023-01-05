package ai.srl.policy

import zio.{Tag, ZIO}

trait Policy[PolicyObservation, Error, Action]:
  def action(observation: PolicyObservation): ZIO[Any, Error, Action]

object Policy:
  def action[PolicyObservation: Tag, Error: Tag, Action: Tag](
      observation: PolicyObservation
  ): ZIO[Policy[PolicyObservation, Error, Action], Error, Action] =
    ZIO.serviceWithZIO[Policy[PolicyObservation, Error, Action]](_.action(observation))
