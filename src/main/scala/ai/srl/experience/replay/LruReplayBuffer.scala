package ai.srl.experience.replay

import ai.srl.experience.replay.ReplayBuffer
import ai.srl.experience.config.ReplayConfig

import scala.reflect.ClassTag
import scala.util.Random

class LruReplayBuffer[T: ClassTag](val batchSize: Int, val bufferSize: Int) extends ReplayBuffer[T]:
  assert(batchSize <= bufferSize)

  private val steps = new Array[T](bufferSize)
  private var firstStepIndex = 0
  private var stepsActualSize = 0
  private val random = Random()

  override def getBatch(): Array[T] =
    (0 until batchSize).map { _ =>
      val baseIndex: Int = random.nextInt(stepsActualSize)
      val index: Int = Math.floorMod(firstStepIndex + baseIndex, steps.length)
      steps(index)
    }.toArray

  @throws(classOf[Exception])
  override def addElement(step: T): Unit =
    if (stepsActualSize == steps.length)
      val stepToReplace: Int = Math.floorMod(firstStepIndex - 1, steps.length)
      steps(stepToReplace) match {
        case closeable: AutoCloseable => closeable.close()
        case _ => ()
      }
      steps(stepToReplace) = step
      firstStepIndex = Math.floorMod(firstStepIndex + 1, steps.length)
    else
      steps(stepsActualSize) = step
      stepsActualSize += 1

  override def getBatchSize(): Int = batchSize

  override def getBufferSize(): Int = bufferSize

  override def getCurrentBufferSize(): Int = stepsActualSize

object LruReplayBuffer:
  def apply[T: ClassTag](config: ReplayConfig): LruReplayBuffer[T] = new LruReplayBuffer[T](config.batchSize, config.bufferSize)
