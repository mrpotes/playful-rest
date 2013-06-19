package potes.play.rest

import scala.language.experimental.macros
import scala.reflect.macros.Context
import scala.collection.mutable.ListBuffer
import scala.util.matching.Regex
import play.api.mvc.Action
import play.api.mvc.AnyContent
import play.api.mvc.Handler

object Generator {
  def macroPaths[T] = macro macroPathsImpl[T]

  def macroPathsImpl[T: c.WeakTypeTag](c: Context): c.Expr[Any] = {
    val helper = new Helper[c.type](c)
    import c.universe._
    import nme.CONSTRUCTOR
    import tpnme.EMPTY
    
    println(implicitly[WeakTypeTag[T]].tpe.asInstanceOf[TypeRef].sym)
    println(implicitly[WeakTypeTag[T]].tpe.asInstanceOf[TypeRef].typeSymbol)
    
    println(showRaw(reify(new Object).tree))
    
    val playFormatter = helper.selectFullyQualified("play.api.libs.json.Json.format")

    val stmts = ListBuffer[Tree]()
    helper.foreachClass(names => {
      import names._
      val regexExpr = c.Expr[String](Literal(Constant("^/api/" + simpleName.toLowerCase + "/(.+)")))
      stmts += ValDef(NoMods, newTermName(fieldName+"$regex"), TypeTree(), reify(regexExpr.splice.r).tree)
      stmts += Import(helper.selectFullyQualified(packageName), List(ImportSelector(newTermName(simpleName), -1, newTermName(simpleName), -1)))
      stmts += ValDef(NoMods, newTermName(fieldName+"$jsonFormatter"), TypeTree(), 
          TypeApply(playFormatter, List(Ident(c.mirror.staticClass(fullyQualified).asType))))
    })

    val constructorBody = Block(List(Apply(Select(Super(This(EMPTY), EMPTY), CONSTRUCTOR), List())), Literal(Constant(())))
    val constructor = DefDef(NoMods, CONSTRUCTOR, List(), List(List()), TypeTree(), constructorBody)
    val objInheritance = List(Ident(newTypeName("AnyRef")))
    val implicitsObject = c.fresh
    val implicitsObjectValue = ValDef(NoMods, implicitsObject, TypeTree(), Apply(Select(New(Ident(implicitly[WeakTypeTag[T]].tpe.asInstanceOf[TypeRef].sym.asClass)), CONSTRUCTOR), List()))
    val importImplicits = Import(Ident(newTermName(implicitsObject)), List(ImportSelector(nme.WILDCARD, -1, nme.WILDCARD, -1)))
    val objectBody = List(constructor, implicitsObjectValue, importImplicits) ++ stmts

    val obj = ModuleDef(NoMods, newTermName("REGEXES"), Template(objInheritance, emptyValDef, objectBody))
    val block = Block(List(obj), Ident(newTermName("REGEXES")))

    println(showRaw(block))
    println(show(block))
    c.Expr[Any](block)
  }

  def macroCase(path: String, method: String): Option[Handler] = macro macroCaseImpl

  def macroCaseImpl(c: Context)(path: c.Expr[String], method: c.Expr[String]): c.Expr[Option[Handler]] = {
    val helper = new Helper[c.type](c)
    import c.universe._

    def pathsValueFinder(t: Tree) = t match {
      case ValDef(_, _, _, s: Select) => "restapi.Generator.macroPaths" == s.toString
      case ValDef(_, _, _, TypeApply(s: Select, _)) => "restapi.Generator.macroPaths" == s.toString
      case _ => false
    }
    val pathsValue = c.enclosingUnit.body.find(pathsValueFinder)

    pathsValue match {
      case Some(v: ValDef) => {
        val regexesField = Ident(newTermName(v.name.decoded))
	
	    val idField = c.Expr[String](Ident(newTermName("id")))
	    def singleEntryPathCase(name: String) = Apply(Select(regexesField, newTermName(name)), List(Bind(newTermName("id"), Ident(nme.WILDCARD))))
	
	    val cases = ListBuffer[CaseDef]()
	    helper.foreachClass(names => {
	      import potes.play.rest.actions._
	      
	      val companionObject = helper.selectFullyQualified(names.fullyQualified)
	      val collectionPath = c.literal("/api/"+names.simpleName.toLowerCase).tree
	      val itemPath = Apply(Select(regexesField, newTermName(names.fieldName + "$regex")), List(Bind(newTermName("id"), Ident(nme.WILDCARD))))
	      val get = c.literal("GET").tree
	      val post = c.literal("POST").tree
	      val delete = c.literal("DELETE").tree
	      val put = c.literal("PUT").tree
	      val id = Apply(Ident(newTermName("Some")), List(Ident(newTermName("id"))))
	      val formatter = Select(regexesField, newTermName(names.fieldName + "$jsonFormatter"))
	      
	      def getCaseForTrait(t: Symbol) = t.name.decoded match {
	        case "Create" => Apply(Ident(newTermName("Tuple2")), List(collectionPath, post))
	        case "Delete" => Apply(Ident(newTermName("Tuple2")), List(itemPath, delete))
	        case "DeleteAll" => Apply(Ident(newTermName("Tuple2")), List(collectionPath, delete))
	        case "Read" => Apply(Ident(newTermName("Tuple2")), List(itemPath, get))
	        case "ReadAll" => Apply(Ident(newTermName("Tuple2")), List(collectionPath, get))
	        case "ReplaceAll" => Apply(Ident(newTermName("Tuple2")), List(collectionPath, put))
	        case "Write" => Apply(Ident(newTermName("Tuple2")), List(itemPath, put))
	      }
	
	      def getResultForTrait(t: Symbol) = t.name.decoded match {
	        case "Create" => Apply(Select(companionObject, newTermName("handleWrite")), List(c.literal("Create").tree, Ident(newTermName("None")), formatter))
	        case "Delete" => Apply(Select(companionObject, newTermName("handleDelete")), List(c.literal("Delete").tree, id))
	        case "DeleteAll" => Apply(Select(companionObject, newTermName("handleDelete")), List(c.literal("DeleteAll").tree, Ident(newTermName("None"))))
	        case "Read" => Apply(Select(companionObject, newTermName("handleRead")), List(c.literal("Read").tree, id, formatter))
	        case "ReadAll" => Apply(Select(companionObject, newTermName("handleRead")), List(c.literal("ReadAll").tree, Ident(newTermName("None")), formatter))
	        case "ReplaceAll" => Apply(Select(companionObject, newTermName("handleWrite")), List(c.literal("ReplaceAll").tree, Ident(newTermName("None")), formatter))
	        case "Write" => Apply(Select(companionObject, newTermName("handleWrite")), List(c.literal("Write").tree, id, formatter))
	      }
	
	      helper.getImplementedTraits(names.fullyQualified).foreach(t => {
	        cases += CaseDef(
	          getCaseForTrait(t),
	          EmptyTree,
	          Apply(Ident(newTermName("Some")), List(getResultForTrait(t)))
	        )
	      })
	    })
	    cases += CaseDef(Ident(nme.WILDCARD), EmptyTree, Ident(newTermName("None")))
	
	    val block = Match(Apply(Ident(newTermName("Tuple2")), List(path.tree, method.tree)), cases.toList)
	
	    println(show(block))
	    c.Expr[Option[Handler]](block)
	  }
      case None => c.abort(c.enclosingPosition, "Could not find a supporting paths value. Have you got a value that equals potes.play.rest.Generator.macroPaths[T]?") 
    }
  }
}
