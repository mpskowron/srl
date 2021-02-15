package ai.srl.djl

import ai.djl.ndarray.{NDArray, NDArrays, NDList}
import org.slf4j.LoggerFactory

import java.util
import java.util.stream.Collectors

object NDLists:
  private val logger = LoggerFactory.getLogger(this.getClass)

  def concat(nDLists: util.Collection[NDList]): NDArray = concatMap(nDLists, identity)

  def concatMap(nDLists: util.Collection[NDList], f: NDArray => NDArray): NDArray =
    val arrays = nDLists
      .stream()
      .flatMap(
        _.stream()
          .map(f(_))
      )
      .collect(Collectors.toList[NDArray])
    if arrays.size() > 1 then
      NDArrays.concat(new NDList(arrays))
    else
      arrays.iterator().next()
