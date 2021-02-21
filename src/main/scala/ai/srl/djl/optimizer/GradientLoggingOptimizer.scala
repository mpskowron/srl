package ai.srl.djl.optimizer

import ai.djl.ndarray.NDArray
import ai.djl.training.optimizer.Optimizer
import ai.djl.training.optimizer.Optimizer.OptimizerBuilder
import org.slf4j.LoggerFactory

class GradientLoggingOptimizer(private val optimizer: Optimizer, private val builder: OptimizerBuilder[?], val loggingFrequency: Int)
  extends Optimizer(builder):
  private val logger = LoggerFactory.getLogger(this.getClass)
  private var callsAfterLastLog = 0

  override def update(parameterId: String, weight: NDArray, grad: NDArray): Unit =
    if callsAfterLastLog == 0 then
      val min = grad.min().getFloat()
      val max = grad.max().getFloat()
      val mean = grad.mean().getFloat()
      logger.info(s"GRADIENTS stats: min $min, max $max, mean $mean")
    callsAfterLastLog = (callsAfterLastLog + 1) % loggingFrequency
    optimizer.update(parameterId, weight, grad)
