package persistence

import play.api.libs.json.Format
import com.mongodb.casbah.Imports._
import play.api.libs.json._

class ObjectIdFormat {
 
  implicit val objectIdFormat = new Format[ObjectId] {

    def reads(json: JsValue): JsResult[ObjectId] = {
      val s = json.as[String]
      if (org.bson.types.ObjectId.isValid(s)) JsSuccess(new ObjectId(s)) else JsError("Not a valid ObjectId: " + s)
    }

    def writes(o: ObjectId) = JsString(o.toString())
  }
  
}
