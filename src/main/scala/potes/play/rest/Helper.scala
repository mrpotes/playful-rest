package potes.play.rest

import scala.reflect.macros.Context
import java.io.File
import java.util.zip.ZipFile
import scala.collection.JavaConversions.enumerationAsScalaIterator
import java.util.zip.ZipEntry
import scala.collection.GenTraversableOnce
import java.lang.Throwable
import scala.util.control.Exception

class Helper[C <: Context](val c: C) {
  import c.universe._

  case class ClassNames(simpleName : String, packageName : String, fullyQualified : String, fieldName : String)
  
  lazy val modelPackage = "model"

  private def packageFinder(t: Tree, packageChecker: String => Boolean) = t match {
    case PackageDef(id, _) => packageChecker(id.name.decoded)
    case _ => false
  }

  private def classFinder(t: Tree) = t match {
    case ClassDef(_, _, _, _) => true
    case _ => false
  }

  private def findClasses(paths: TraversableOnce[String], parentPathLength: Integer) = {
    for (
      path <- paths;
      name = path.substring(parentPathLength, path.length - 6).replace(File.separator, ".");
      packageLength = name.lastIndexOf('.')
    ) yield {
      (name.substring(0, packageLength), name.substring(packageLength + 1))
    }
  }

  private def listFiles(f: File) = Option(f.listFiles()).map(_.filter(f => selectClassFilename(f.getName)).map(_.getAbsolutePath).toList).getOrElse(List())

  private def selectClassFilename(s: String) = !s.contains("$") && s.endsWith(".class")

  private def selectClassZipEntries(ze: ZipEntry) = {
    val name = ze.getName
    name.startsWith(modelPackage + "/") && selectClassFilename(name.substring(name.lastIndexOf('/') + 1)) && !ze.isDirectory
  }

  private def getClassesFromClasspathEntry(cpFile: File) =
    if (! cpFile.exists) List()
    else {
      if (cpFile.isDirectory) findClasses(directoryFiles(cpFile), cpFile.getAbsolutePath.length + 1)
      else findClasses(new ZipFile(cpFile).entries.filter(selectClassZipEntries).map(_.getName.replace("/", ".")), 0)
    }
    
  private def isRestApiClass(symbol: ClassSymbol) = {
    val companionTraits = symbol.companionSymbol.typeSignature.baseClasses
    val isApiClass = symbol.isCaseClass && companionTraits.exists(isRestApiTrait)
    println("Checking class: "+symbol.name.decoded+" with traits "+companionTraits + " - "+isApiClass)
    isApiClass
  }

  private def modelClasses = {
    val classpath = c.compilerSettings(c.compilerSettings.indexOf("-classpath") + 1)
    val classpathClasses = classpath.split(File.pathSeparator).toList.flatten(s => getClassesFromClasspathEntry(new File(s))).toSet
    def findPackageDeclaration(t: Tree) = packageFinder(t, pkg => pkg.equals(modelPackage) || pkg.startsWith(modelPackage + "."))
    def nameFragments(unit: CompilationUnit) =
      (unit.body.find(packageFinder(_, _ => true)).get.asInstanceOf[PackageDef].name.decoded, unit.body.find(classFinder).get.asInstanceOf[ClassDef].name.decoded)
    val allClasses = classpathClasses ++ c.enclosingRun.units.filter(unit => unit.body.find(findPackageDeclaration).isDefined && unit.body.find(classFinder).isDefined).map(nameFragments)
    allClasses.filter(n => Exception.allCatch.opt(isRestApiClass(c.mirror.staticClass(n._1 + "." + n._2))).getOrElse(false))
  }

  private def namesForClass(nameFragments: (String, String)) = {
    val fqn = nameFragments._1 + "." + nameFragments._2
    ClassNames(nameFragments._2, nameFragments._1, fqn, fqn.replace(".", "$"))
  }

  private def directoryFiles(cpFile: java.io.File): List[String] = {
    Option(new File(cpFile, modelPackage)).map(listFiles).getOrElse(List())
  }

  private def isRestApiTrait (s : Symbol) : Boolean = {
    println("Checking "+s.fullName)
    if (s.fullName.startsWith("restapi.actions.")) true
    else false
  }

  def getImplementedTraits: String => List[Symbol] = c.mirror.staticModule(_).typeSignature.baseClasses.filter(isRestApiTrait)

  def foreachClass(fn: ClassNames => Unit): Unit = {
    modelClasses.foreach(nameFragments => {
      println("Found model class: " + nameFragments._1 + "." + nameFragments._2)
      val names = namesForClass(nameFragments)
      fn(names)
    })
  }

  def selectFullyQualified(fqn: String) = {
    if (fqn contains ".") {
      val components = fqn.splitAt(fqn.indexOf('.'))
      val l = if (components._2.contains(".")) components._2.substring(1).split('.').toList else List(components._2)
      l.foldLeft[Tree](Ident(newTermName(components._1)))((t: Tree, s: String) => Select(t, newTermName(s)))
    } else {
      Ident(newTermName(fqn))
    }
  }

}