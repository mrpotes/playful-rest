package persistence

import play.api.Play.current
import com.novus.salat.dao.ModelCompanion
import com.novus.salat._
import com.novus.salat.annotations._
import com.novus.salat.dao._
import com.mongodb.casbah.Imports._
import se.radley.plugin.salat._
import mongoContext._
import potes.play.rest.actions._
import model.WithId

abstract class MongoDb[T <: WithId](implicit m: Manifest[T]) extends ModelCompanion[T, ObjectId] {
  def collectionName: String
  val dao = new SalatDAO[T, ObjectId](collection = mongoCollection(collectionName)) {}
}

trait MongoDeleteAll extends DeleteAll {
  val dao: SalatDAO[_, ObjectId]
  def deleteAll = dao.ids(MongoDBObject()).foreach(dao.removeById(_))
}

trait MongoWrite[T <: WithId] extends Write[T] {
  val dao: SalatDAO[T, ObjectId]
  def write(t: T) = dao.findOneById(t.id.get) match {
    case Some(_) => {
      dao.save(t)
      true
    }
    case None => false
  }
}

trait MongoCreate[T <: AnyRef] extends Create[T] {
  val dao: SalatDAO[T, ObjectId]
  def create(t: T) = dao.insert(t).map(_.toString)
}

trait MongoReplaceAll[T <: AnyRef] extends ReplaceAll[T] {
  val dao: SalatDAO[T, ObjectId]
  def replaceAll(ts: TraversableOnce[T]) = {
    dao.find(MongoDBObject()).foreach(dao.remove)
    ts.foreach(dao.insert)
  }
}

trait MongoDelete extends Delete  {
  val dao: SalatDAO[_, ObjectId]
  def delete(id: String) = dao.findOneById(new ObjectId(id)) match {
    case Some(t) => {
      dao.removeById(new ObjectId(id))
      true
    }
    case None => false
  }
}

trait MongoRead[T <: AnyRef] extends Read[T]  {
  val dao: SalatDAO[T, ObjectId]
  def read(id: String) = dao.findOneById(new ObjectId(id))
}

trait MongoReadAll[T <: AnyRef] extends ReadAll[T]  {
  val dao: SalatDAO[T, ObjectId]
  def readAll = dao.find(MongoDBObject()).toList
}