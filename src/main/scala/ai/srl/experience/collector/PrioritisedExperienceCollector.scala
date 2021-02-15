package ai.srl.experience.collector

import ai.djl.ndarray.{NDArray, NDArrays, NDList}
import ai.djl.training.listener.TrainingListener.BatchData
import ai.srl.djl.NDLists
import ai.srl.experience.replay.HasPrioritisedReplayBuffer
import org.slf4j.LoggerFactory

import java.util.stream.Collectors
import scala.collection.immutable.ArraySeq
import scala.jdk.CollectionConverters._

trait PrioritisedExperienceCollector[EC <: ExperienceCollector[?, ?, ?]]:
  private val logger = LoggerFactory.getLogger(this.getClass)
  
  extension (collector: EC)
    def updateLastBatch(newPriorities: Seq[Float]): Unit

    /**
     * It won't work for a training which is parallelized among multiple GPUs/CPUs, because it breaks the requirement, that the new 
     * priorities must be in the same order as the batch items. It can be improved by using/modifying Batch's split method
     * @param batchData
     */
    // TODO Use/modify Batch split method to make it work for multiple GPUs
    def updateLastBatch(batchData: BatchData): Unit =
      val pred = NDLists.concat(batchData.getPredictions.values())
      val label = NDLists.concatMap(batchData.getLabels.values(), _.reshape(-1, 1))
      val newPriorities = ArraySeq.unsafeWrapArray(label.sub(pred).abs().toFloatArray)
      collector.updateLastBatch(newPriorities)

object PrioritisedExperienceCollector:
  given [EC <: ExperienceCollector[?, ?, ?]](using HasPrioritisedReplayBuffer[EC, ?]): PrioritisedExperienceCollector[EC] with
    extension (collector: EC)
      def updateLastBatch(newPriorities: Seq[Float]): Unit = collector.prioritisedReplayBuffer.updateLastBatch(newPriorities)
    