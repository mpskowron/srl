package ai.srl.step

trait Step[Ac, Observation] extends BaseStep[Ac, Observation]:

  def getPostState(): Observation

  def getPostActionSpace(): Vector[Ac]
