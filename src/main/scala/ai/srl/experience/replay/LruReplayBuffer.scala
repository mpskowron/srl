package ai.srl.experience.replay

import ai.srl.experience.replay.ReplayBuffer
import ai.srl.experience.config.ReplayConfig

import scala.reflect.ClassTag
import scala.util.Random

class LruReplayBuffer[T: ClassTag](val batchSize: Int, val bufferSize: Int) extends ReplayBuffer[T]:
  assert(batchSize <= bufferSize)

  private val transitions = new Array[T](bufferSize)
  private var firstTransitionIndex = 0
  private var actualSize = 0
  private val random = Random()

  override def getBatch(): Array[T] =
    (0 until batchSize).map { _ =>
      val baseIndex: Int = random.nextInt(actualSize)
      val index: Int = Math.floorMod(firstTransitionIndex + baseIndex, transitions.length)
      transitions(index)
    }.toArray

  @throws(classOf[Exception])
  override def addElement(step: T): Unit =
    if (actualSize == transitions.length)
      val stepToReplace: Int = Math.floorMod(firstTransitionIndex - 1, transitions.length)
      transitions(stepToReplace) match {
        case closeable: AutoCloseable => closeable.close()
        case _ => ()
      }
      transitions(stepToReplace) = step
      firstTransitionIndex = Math.floorMod(firstTransitionIndex + 1, transitions.length)
    else
      transitions(actualSize) = step
      actualSize += 1

  override def getBatchSize(): Int = batchSize

  override def getBufferSize(): Int = bufferSize

  override def getCurrentBufferSize(): Int = actualSize

object LruReplayBuffer:
  def apply[T: ClassTag](config: ReplayConfig): LruReplayBuffer[T] = new LruReplayBuffer[T](config.batchSize, config.bufferSize)
