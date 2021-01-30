package ai.srl.logging

import ai.djl.modality.rl.agent.QAgent

trait HasName[T]:
  extension (hasName: T) def name: String

object HasName:
  given HasName[QAgent] with 
    extension (agent: QAgent)
      def name: String = agent.getClass.getSimpleName
