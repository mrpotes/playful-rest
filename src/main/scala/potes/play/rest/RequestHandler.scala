package potes.play.rest

import play.api.libs.json.Format
import play.api.libs.json.JsValue
import play.api.libs.json.Json
import play.api.mvc.Action
import play.api.mvc.Controller
import play.api.mvc.Request
import play.api.mvc.Result
import potes.play.rest.actions.Create
import potes.play.rest.actions.Delete
import potes.play.rest.actions.DeleteAll
import potes.play.rest.actions.Read
import potes.play.rest.actions.ReadAll
import potes.play.rest.actions.ReplaceAll
import potes.play.rest.actions.Write

trait RequestHandler extends Controller {

  def handleWrite[T](action: String, id: Option[String], jsonFormatter: Format[T]) = Action(parse.json) { implicit request =>
    implicit val formatter = jsonFormatter

    action match {
      case "Create" => handleReadJson((o: T) => {
        asInstanceOf[Create[T]].create(o) match {
          case Some(s) => Ok(Json.toJson(s))
          case None => InternalServerError
        }
      })
      case "ReplaceAll" => handleReadJson((o: List[T]) => {
        asInstanceOf[ReplaceAll[T]].replaceAll(o)
        NoContent
      })
      case "Write" => handleReadJson((o: T) => if (asInstanceOf[Write[T]].write(o)) NoContent else NotFound)
    }
  }

  private def handleReadJson[T](f: T => Result)(implicit request: Request[JsValue], jsonFormatter: Format[T]) = {
    request.body.validate[T].fold(
      valid = (o => f(o)),
      invalid = (e => BadRequest(e.toString))
    )
  }

  def handleRead[T](action: String, id: Option[String], jsonFormatter: Format[T]) = Action { request =>
    implicit val formatter = jsonFormatter
    action match {
      case "Read" => asInstanceOf[Read[T]].read(id.get) match {
        case Some(o) => Ok(Json.toJson(o))
        case None => NotFound
      }
      case "ReadAll" => Ok(Json.toJson(asInstanceOf[ReadAll[T]].readAll))
    }
  }

  def handleDelete(action: String, id: Option[String]) = Action { request =>
    action match {
      case "Delete" => if (asInstanceOf[Delete].delete(id.get)) NoContent else NotFound
      case "DeleteAll" => asInstanceOf[DeleteAll].deleteAll; NoContent
    }
  }

}


