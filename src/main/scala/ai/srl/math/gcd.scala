package ai.srl.math

import cats.data.NonEmptyList

import scala.annotation.tailrec

object gcd:

  @tailrec
  def gcd(a: Int, b: Int): Int = if b == 0 then a else gcd(b, a % b)

  def gcd(numbers: NonEmptyList[Int]): Int = numbers match
    case NonEmptyList(head, Nil)            => head
    case NonEmptyList(head, second :: tail) => gcd(head, gcd(NonEmptyList(second, tail)))
