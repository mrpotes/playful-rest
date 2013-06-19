package model

import com.mongodb.casbah.Imports._
import persistence._

case class Book(id: Option[ObjectId], title: String) extends WithId

object Book extends MongoDb[Book] with MongoRead[Book] with MongoReadAll[Book] {
  override def collectionName = "books"
}