import play.api.GlobalSettings
import play.api.mvc.RequestHeader
import play.api.mvc.Handler
import play.api.data.format.Formats._
import play.api.libs.json._
import com.mongodb.casbah.Imports._
import play.api.libs.functional.syntax.functionalCanBuildApplicative
import play.api.libs.functional.syntax.toFunctionalBuilderOps
import persistence.ObjectIdFormat
import potes.play.rest.Generator

object Global extends GlobalSettings {

  val regexesUsingMacro = Generator.macroPaths[ObjectIdFormat]

  override def onRouteRequest(request: RequestHeader): Option[Handler] = Generator.macroCase(request.path, request.method)

}
