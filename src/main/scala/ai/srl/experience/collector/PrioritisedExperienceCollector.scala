package ai.srl.experience.collector

import ai.srl.experience.replay.HasPrioritisedReplayBuffer

trait PrioritisedExperienceCollector[EC <: ExperienceCollector[?, ?, ?]]:
  extension (collector: EC)
    def updateLastBatch(newPriorities: IterableOnce[Float]): Unit

object PrioritisedExperienceCollector:
  given [EC <: ExperienceCollector[?, ?, ?]](using HasPrioritisedReplayBuffer[EC, ?]): PrioritisedExperienceCollector[EC] with
    extension (collector: EC)
      def updateLastBatch(newPriorities: IterableOnce[Float]): Unit = collector.prioritisedReplayBuffer.updateLastBatch(newPriorities)