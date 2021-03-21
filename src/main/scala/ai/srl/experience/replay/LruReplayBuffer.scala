package ai.srl.experience.replay

import ai.srl.experience.replay.ReplayBuffer
import ai.srl.experience.config.ReplayConfig
import ai.srl.collection.{Extend, GetBatch, MaxSize, Size}

import scala.reflect.ClassTag
import scala.util.Random

class LruReplayBuffer[T: ClassTag](val batchSize: Int, val bufferSize: Int):
  assert(batchSize <= bufferSize)

  private val transitions = new Array[T](bufferSize)
  private var firstTransitionIndex = 0
  private var actualSize = 0
  private val random = Random()

object LruReplayBuffer:
  def apply[T: ClassTag](config: ReplayConfig): LruReplayBuffer[T] = new LruReplayBuffer[T](config.batchSize, config.bufferSize)

  given [T: ClassTag]: ReplayBuffer[LruReplayBuffer[T], T] with
    extension (rb: LruReplayBuffer[T])
      def getBatch(): Array[T] =
        (0 until rb.batchSize).map { _ =>
          val baseIndex: Int = rb.random.nextInt(rb.actualSize)
          val index: Int = Math.floorMod(rb.firstTransitionIndex + baseIndex, rb.transitions.length)
          rb.transitions(index)
        }.toArray
      def getBatchSize(): Int = rb.batchSize
  
      @throws(classOf[Exception])
      def addOne(step: T): Unit =
        if (rb.actualSize == rb.transitions.length)
          val stepToReplace: Int = Math.floorMod(rb.firstTransitionIndex - 1, rb.transitions.length)
          rb.transitions(stepToReplace) match {
            case closeable: AutoCloseable => closeable.close()
            case _ => ()
          }
          rb.transitions(stepToReplace) = step
          rb.firstTransitionIndex = Math.floorMod(rb.firstTransitionIndex + 1, rb.transitions.length)
        else
          rb.transitions(rb.actualSize) = step
          rb.actualSize += 1

      def size(): Int = rb.actualSize

      def maxSize(): Int = rb.bufferSize

      def clearAll(): Unit =
        rb.firstTransitionIndex = 0
        rb.actualSize = 0
