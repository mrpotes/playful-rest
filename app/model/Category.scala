package model

import play.api.Play.current
import com.novus.salat.dao.ModelCompanion
import com.novus.salat._
import com.novus.salat.annotations._
import com.novus.salat.dao._
import com.mongodb.casbah.Imports._
import se.radley.plugin.salat._
import persistence.MongoDb
import persistence.MongoReadAll
import persistence.MongoRead

case class Category(id: Option[ObjectId], name: String) extends WithId 

object Category extends MongoDb[Category] with MongoRead[Category] with MongoReadAll[Category] {
  override def collectionName = "categories" 
}