package ai.srl.agent

import ai.djl.Device
import ai.djl.ndarray.{NDArray, NDList, NDManager}
import ai.djl.training.{GradientCollector, Trainer}
import ai.djl.training.dataset.Batch
import ai.djl.training.listener.EvaluatorTrainingListener
import ai.djl.training.listener.TrainingListener.BatchData
import ai.djl.translate.Batchifier
import ai.srl.action.DJLAction
import ai.srl.agent.DJLOptimalLabelsAgent.trainBatch
import ai.srl.env.RlEnv
import ai.srl.observation.DJLNNInput
import ai.srl.policy.PurePolicy
import ai.srl.step.EnvStep
import zio.{Chunk, UIO, ZIO}

import java.util
import scala.util.Using

case class DJLOptimalLabelsAgent(trainer: Trainer) extends Agent[Batch, Nothing, BatchData]:

  override def train(trainContext: Batch): ZIO[Any, Nothing, BatchData] = DJLOptimalLabelsAgent.train(trainContext, trainer)

object DJLOptimalLabelsAgent:
  // TODO Whenever you will get an error from running this one, change the signature so it outputs all possible errors eventually
  private def trainBatch(trainer: Trainer, batch: Batch): BatchData =
    val batchData = BatchData(batch, util.HashMap(), util.HashMap())
    Using.resource(trainer.newGradientCollector) { collector =>
      val data        = batch.getData
      val labels      = batch.getLabels
      val predictions = trainer.forward(data, labels)
      val time        = System.nanoTime
      val lossValue   = trainer.getLoss.evaluate(labels, predictions)
      collector.backward(lossValue)
      trainer.addMetric("backward", time)
      batchData.getLabels.put(labels.get(0).getDevice, labels)
      batchData.getPredictions.put(predictions.get(0).getDevice, predictions)
    }
    trainer.notifyListeners(_.onTrainingBatch(trainer, batchData))
    batchData

  def logLoss(trainer: Trainer): UIO[Unit] =
    (for
      metrics <- ZIO.fromOption(Option(trainer.getMetrics))
      latestMetric = metrics.latestMetric(EvaluatorTrainingListener.metricName(trainer.getLoss, EvaluatorTrainingListener.TRAIN_ALL))
      _ <- ZIO.logInfo(latestMetric.getMetricName + " -> " + latestMetric.getValue)
    yield ()).catchAll { _ => ZIO.unit }

  def train(trainContext: Batch, trainer: Trainer): ZIO[Any, Nothing, BatchData] = for
    batchData <- ZIO.succeed(trainBatch(trainer, trainContext))
    _ = trainer.step()
    _ <- DJLOptimalLabelsAgent.logLoss(trainer)
  yield batchData
