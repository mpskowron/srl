package ai.srl.agent

import ai.djl.Device
import ai.djl.ndarray.{NDArray, NDList}
import ai.djl.training.{GradientCollector, Trainer}
import ai.djl.training.dataset.Batch
import ai.djl.training.listener.TrainingListener.BatchData
import ai.srl.env.RlEnv
import ai.srl.policy.Policy

import java.util
import scala.util.Using

type DJLAgent[Ac, Obs, P] = Agent[Ac, Obs, P, Batch, BatchData]

object DJLAgent:
  def trainBatch(trainer: Trainer, batch: Batch): BatchData =
    val batchData = BatchData(batch, util.HashMap(), util.HashMap())
    Using.resource(trainer.newGradientCollector) { collector =>
        val data = trainer.getDataManager.getData(batch)
        val labels = trainer.getDataManager.getLabels(batch)
        val predictions = trainer.forward(data, labels)
        val time = System.nanoTime
        val lossValue = trainer.getLoss.evaluate(labels, predictions)
        collector.backward(lossValue)
        trainer.addMetric("backward", time)
        batchData.getLabels.put(labels.get(0).getDevice, labels)
        batchData.getPredictions.put(predictions.get(0).getDevice, predictions)
    }
    trainer.notifyListeners(_.onTrainingBatch(trainer, batchData))
    batchData
