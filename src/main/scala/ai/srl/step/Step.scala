package ai.srl.step

trait Step[S, Ac, Observation] extends BaseStep[S, Ac, Observation]:

  def getPostState(): Observation

  def getPostActionSpace(): Vector[Ac]
