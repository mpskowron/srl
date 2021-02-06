package ai.srl.action

import ai.djl.modality.rl.ActionSpace
import ai.djl.ndarray.{NDList, NDManager}
import ai.djl.util.RandomUtils

import scala.jdk.CollectionConverters._

trait DJLAction:
  def toNDList(manager: NDManager): NDList

object DJLAction:
  extension[A <: DJLAction] (array: Vector[A])
    def randomAction(): A = array(RandomUtils.nextInt(array.size))
    def toActionSpace(manager: NDManager): ActionSpace =
      val actionSpace = new ActionSpace()
      actionSpace.addAll(array.map(_.toNDList(manager)).asJava)
      actionSpace