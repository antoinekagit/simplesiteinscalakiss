import simplehtmlwriterinscala.Basic.{
  AbsNode, AbsAttr, AttrStr, Node, Leaf, SeqNode, StrData, EmptyNode }
import simplehtmlwriterinscala.LessStrict.AttrStrBuilder

import java.io.{File, PrintWriter}
import java.nio.file.{ Path, Paths, Files, FileAlreadyExistsException,
  NoSuchFileException}
import java.util.{NoSuchElementException}

import scala.collection.mutable.{ Set => MutableSet }

package simplesiteinscalakiss {

  class Config (
    val dirResources :String = "src/main/resources/ssisk",
    val dirTarget :String = "target/ssisk/",
    val dirTargetResources :String = "resources/"
  )

  class Ssisk (val pwd:Path) (implicit config:Config) {

    def linkpage (strPathPage:String) :String = {
      val pathPage = pwd.resolve(Paths.get(strPathPage)).normalize
      pwd.relativize(pathPage).toString
    }

    def linkresource (strPathResource:String) :String = {
      val pathResource = Paths.get(strPathResource)
      if (pathResource.isAbsolute) {
        Paths.get(
          pwd.relativize(Paths.get("/")).toString,
          config.dirTargetResources,
          pathResource.toString).toString
      }
      else {
        Paths.get(
          pwd.relativize(Paths.get("/")).toString,
          config.dirTargetResources,
          pwd.toString,
          pathResource.toString).toString
      }
    }
  }

  abstract class Page (val strPath:String) (implicit config:Config) {
    val path = Paths.get(strPath)
    if (! path.isAbsolute) new Exception("the page path must be absolute")
    if (strPath.startsWith(config.dirTargetResources))
      new Exception("resources folder is restricted to resources")

    implicit val ssisk = new Ssisk (path.getParent)
    val node:AbsNode
  }

  object Site {

    def make (pages :Set[Page]) (implicit config:Config) {

      import FilesUtil._

      val pagesPaths = pages.map { page => page.path.toString }

      mkdir(config.dirTarget)

      pages.foreach { page =>
        val path = Paths.get(config.dirTarget, page.path.toString)
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
