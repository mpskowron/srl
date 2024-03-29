package ai.srl.action

import ai.djl.modality.rl.ActionSpace
import ai.djl.ndarray.{NDList, NDManager}
import ai.djl.util.RandomUtils

import scala.jdk.CollectionConverters.*

trait DJLAction[Ac]:
  def getActionSpace: IndexedSeq[Ac]

  extension (action: Ac) def toNDList(manager: NDManager): NDList

object DJLAction:
  extension [Ac: DJLAction](array: IndexedSeq[Ac])
    def randomAction(): Ac = array(RandomUtils.nextInt(array.size))
    def toActionSpace(manager: NDManager): ActionSpace =
      val actionSpace = new ActionSpace()
      actionSpace.addAll(array.map(_.toNDList(manager)).asJava)
      actionSpace
