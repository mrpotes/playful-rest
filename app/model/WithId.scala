package model

import com.mongodb.casbah.Imports.ObjectId

abstract class WithId {
  def id: Option[ObjectId]
}
