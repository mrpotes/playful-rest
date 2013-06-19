package model

import com.mongodb.casbah.Imports._
import persistence.MongoDb
import persistence.MongoRead
import persistence.MongoReadAll

case class Author(id: Option[ObjectId], name: String) extends WithId

object Author extends MongoDb[Author] with MongoRead[Author] with MongoReadAll[Author] {
  override def collectionName = "authors"
}