package ai.srl.step

trait Step[Ac, Ob] extends BaseStep[Ac, Ob]:

  def getPostObservation(): Ob

  def getPostActionSpace(): Vector[Ac]
