package ai.srl.step

trait Step[S, Ac, Observation] extends BaseStep[S, Ac, Observation]:
  extension (step: S)
    def getPostState(): Observation

    def getPostActionSpace(): Vector[Ac]
