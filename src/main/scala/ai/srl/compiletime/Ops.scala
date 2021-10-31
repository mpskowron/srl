package ai.srl.compiletime

object Ops {
  type REF[Refinement] = =:=[Refinement, true]
  def unsafeSummonEq[From, To]: =:=[From, To] = summon[To =:= To].asInstanceOf[From =:= To]
  def unsafeSummon[Refinement]: =:=[Refinement, true] = summon[true =:= true].asInstanceOf[Refinement =:= true]
}
