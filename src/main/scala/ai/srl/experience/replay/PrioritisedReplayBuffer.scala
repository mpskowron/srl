package ai.srl.experience.replay

import ai.srl.collection.{CyclicArray, SumTree}
import ai.srl.experience.config.ReplayConfig
import ai.srl.experience.replay.ReplayBuffer

import scala.reflect.ClassTag
import alleycats.Empty
import scala.util.Random

class PrioritisedReplayBuffer[T: Empty](val batchSize: Int, val bufferSize: Int) extends ReplayBuffer[T]:
  assert(batchSize <= bufferSize)

  private val steps = new SumTree[T](bufferSize)
  private var firstStepIndex = 0
  private var stepsActualSize = 0
  private val random = Random()

  override def getBatch(): Array[T] = ???

  @throws(classOf[Exception])
  override def addElement(step: T): Unit = ???

  override def getBatchSize(): Int = batchSize

  override def getBufferSize(): Int = bufferSize

  override def getCurrentBufferSize(): Int = stepsActualSize


