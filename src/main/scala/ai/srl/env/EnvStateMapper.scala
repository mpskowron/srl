package ai.srl.env

trait EnvStateMapper[-Observation, State, -Ac, E] {

  def mapState(observation: Observation, state: State, ac: Ac): Either[E, State]
}
