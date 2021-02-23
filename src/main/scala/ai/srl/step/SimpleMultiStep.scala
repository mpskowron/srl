package ai.srl.step

final case class SimpleMultiStep[Ac, Observation](observation: Observation, actionsWithRewards: Seq[(Ac, Float)], index: Int, done: Boolean) extends MultiStep[Ac, Observation]:
  override def getActionsWithRewards(): Seq[(Ac, Float)] = actionsWithRewards

  override def getPreObservation(): Observation = observation

  override def isDone(): Boolean = done
  
