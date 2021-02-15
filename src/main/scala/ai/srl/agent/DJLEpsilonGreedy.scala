package ai.srl.agent

import ai.djl.training.dataset.Batch
import ai.djl.training.listener.TrainingListener.BatchData
import ai.djl.training.tracker.Tracker
import ai.srl.djl.HasTrainer
import ai.srl.env.RlEnv
import ai.srl.logging.{Description, HasName}

type DJLEpsilonGreedy[Ac, E <: RlEnv[Ac, ?, ?], A <: Agent[Ac, E, Batch, BatchData]] = EpsilonGreedy[Ac, E, Batch, BatchData, A]
