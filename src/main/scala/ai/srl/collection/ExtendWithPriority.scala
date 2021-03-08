package ai.srl.collection

import ai.srl.collection

import scala.deriving._
import scala.compiletime.{erasedValue, summonFrom, summonInline, error}

trait ExtendWithPriority[T, Item]:
  extension (t: T)
    def addOnePrioritised(item: Item, priority: Float): Unit

object ExtendWithPriority:
  
  // TODO It doesn't work yet
  inline given derived[T, Item](using m: Mirror.ProductOf[T]): ExtendWithPriority[T, Item] = {
    val elemInstances = summonAll[m.MirroredElemTypes, Item]
    println("I am here")
    val childInstance = elemInstances.asInstanceOf[ExtendWithPriority[Any, Item]]
    new ExtendWithPriority[T, Item] {
      extension (t: T) def addOnePrioritised(item: Item, priority: Float) = {
        println("I am in add one prioritised method")
        childInstance.addOnePrioritised(t.asInstanceOf[Product].productIterator.next())(item, priority)
      }
    }
  }

  inline def summonAll[T <: Tuple, Item]: ExtendWithPriority[?, Item] = {
    inline erasedValue[T] match
      case _: EmptyTuple => error("Cannot derive for a class which doesn't have a child implementing ExtendWithPriority")
      case _: (t *: ts) => summonFrom {
        case given ExtendWithPriority[t, Item] => 
          println("summoning")
          summonInline[ExtendWithPriority[t, Item]]
        case _ =>
          println("summoning rec")
          summonAll[ts, Item]
      }
  }
