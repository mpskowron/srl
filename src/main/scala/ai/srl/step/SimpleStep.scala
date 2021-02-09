package ai.srl.step

case class SimpleStep[Ac, Ob](observation: Ob, action: Ac, reward: Float, done: Boolean) extends BaseStep[Ac, Ob]:
  override def getPreObservation(): Ob = observation

  override def getAction(): Ac = action

  override def getReward(): Float = reward

  override def isDone(): Boolean = done
