package ai.srl.step

trait BaseStep[S, Ac, Observation]:
  extension (step: S)
    def getPreObservation(): Observation

    def getAction(): Ac

    def getReward(): Float

    def isDone(): Boolean
