package ai.srl.collection

trait GetNewest[T, Item]:
  extension (t: T)
    def getNewest(): Option[Item]
