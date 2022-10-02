package ai.srl.env

trait EnvStateMapper[-Observation, State, -Ac] {

  def mapState(observation: Observation, state: State, ac: Ac): State
}
