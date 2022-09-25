package ai.srl.experience.replay
import ai.srl.experience.store.ExperienceStore

type PureReplayBuffer[S, T] = ExperienceStore[S, T, T]
