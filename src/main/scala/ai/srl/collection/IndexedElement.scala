package ai.srl.collection

case class IndexedElement[+A](index: Int, element: A):
  def toTuple: (Int, A) = (index, element)
