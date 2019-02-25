import simplehtmlwriterinscala.Basic.{ TNode, NodeMaker, Attr, Node }

import java.io.{File, PrintWriter}
import java.nio.file.{ Path, Paths, Files, FileAlreadyExistsException,
  NoSuchFileException}
import java.util.{NoSuchElementException}

import scala.collection.mutable.{ Set => MutableSet }

package simplesiteinscalakiss {

  case class Context (
    val currentDir :Path,
    val config :Config
  ){

    def linkPage (strPathPage:String) :String =
      if (Paths.get(strPathPage).isAbsolute) {
        Paths.get(
          "./",
          currentDir.relativize(Paths.get("/")).toString,
          strPathPage).normalize.toString }
      else {
        val pathPage = currentDir.resolve(Paths.get(strPathPage)).normalize
        currentDir.relativize(pathPage).normalize.toString
      }

    def linkResource (strPathResource:String) :String =
      if (Paths.get(strPathResource).isAbsolute) {
        Paths.get(
          "./",
          currentDir.relativize(Paths.get("/")).toString,
          config.dirTargetResources,
          strPathResource).normalize.toString }
      else {
        val pathResource = Paths.get(strPathResource)
        Paths.get(
          currentDir.relativize(Paths.get("/")).toString,
          config.dirTargetResources,
          currentDir.toString,
          pathResource.toString).normalize.toString
      }
  }

  class Config (
    // where is the resource folder in sbt
    val dirResources :String = "src/main/resources/ssisk",
    // where to write the site
    val dirTarget :String = "target/ssisk/",
    // where to write the resources in the site
    val dirTargetResources :String = "resources/"
  )

  abstract class Page (val pagePathStr:String) (implicit config:Config){
    val pagePath = Paths.get(pagePathStr)

    if (! pagePath.isAbsolute) new Exception("page path must be absolute")
    if (pagePathStr.startsWith(config.dirTargetResources))
      new Exception("resources folder is restricted to resources")

    val ctxt = Context(pagePath.getParent, config)

    def linkPage (pagePathStr:String) = ctxt.linkPage(pagePathStr)
    def linkResource (resPathStr:String) = ctxt.linkResource(resPathStr)

    val node :TNode
  }

  object Site {

    def make (config:Config, pages :Set[Page]) {

      import FilesUtil._

      val pagesPaths = pages.map { page => page.pagePath.toString }

      mkdir(config.dirTarget)

      pages.foreach { page =>
        val path = Paths.get(config.dirTarget, page.pagePath.toString)
        val content = "<!DOCTYPE html>" + page.node.toString
        writeFile (path, content)
      }

      symbLink(
        Paths.get(config.dirResources),
        Paths.get(config.dirTarget, config.dirTargetResources))
    }
  }

  object FilesUtil {
    def fileExists (path:Path) :Boolean = path.toFile.exists
    def fileExists (strPath:String) :Boolean = (new File (strPath)).exists

    def fillDirs (file:File) :Unit = {
      var success = true
      try Files.createDirectories(file.toPath.getParent)
      catch {
        case e:FileAlreadyExistsException =>
          (new File (e.getFile)).delete
          success = false
      }
      if (! success) fillDirs(file)
    }

    def writeFile (file:File, content:String) :Unit = {
      fillDirs(file)
      if (file.exists) { file.delete ; file.createNewFile }
      val writer = new PrintWriter (file)
      writer.write(content)
      writer.close
    }
    def writeFile (path:Path, content:String) :Unit =
      writeFile(path.toFile, content)

    def mkdir (path:String) :Unit = {
      val file = new File (path)
      fillDirs(file)
      file.mkdirs
    }

    def symbLink (originP:Path, linkP:Path, originF:File, linkF:File) :Unit = {
      fillDirs(linkF)
      if (! originF.exists)
        println("origin for symblink does not exist: " + originP)
      try linkF.delete
      catch { case e:NoSuchFileException => () }
      Files.createSymbolicLink(
        linkP.toAbsolutePath,
        originP.toAbsolutePath)
    }
    def symbLink (originP:Path, linkP:Path) :Unit =
      symbLink(originP, linkP, originP.toFile, linkP.toFile)

  }
}
