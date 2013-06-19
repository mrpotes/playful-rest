# playful-rest

This is a plugin for the Play Framework that generates RESTful API endpoints 
for a domain model defined as case classes.

## To use

* Add the following resolver to your Build.scala:

        resolvers += "playful-rest on GitHub" at "https://raw.github.com/mrpotes/playful-rest/repository"

* And then add the following dependency:
 
        "playful-rest" %% "playful-rest" % "1.0"

* Implement some serialization traits, or ad a dependency on one of the predefined libraries
* Create your model case classes in the `model` package.
* If needed, create some Play JSON formatters in a separate class
* Create a Play `Global.scala`:

        import play.api.GlobalSettings
        import play.api.mvc.RequestHeader
        import play.api.mvc.Handler
        import play.api.data.format.Formats._
        import play.api.libs.json._
        import com.mongodb.casbah.Imports._
        import play.api.libs.functional.syntax.functionalCanBuildApplicative
        import play.api.libs.functional.syntax.toFunctionalBuilderOps
        import persistence.ObjectIdFormat
        
        object Global extends GlobalSettings {
          val restSupport = potes.play.rest.Generator.macroPaths[ObjectIdFormat] 
          override def onRouteRequest(request: RequestHeader): Option[Handler] = potes.play.rest.Generator.macroCase(request.path, request.method)
        }

One **GOTCHA** to watch out for - when you change one of the case classes in your model, you'll need to do 
a full build if you want the change to be reflected in the REST API. I'm looking for a way around this, and
will publish here if/when I find one.

## More Information

Have a read of the series of blog posts on 
[ScottLogic's blog](http://www.scottlogic.co.uk/blog/2013/06/05/scala-macros-part-1.html),
or take a look at the [example branch](tree/example).
