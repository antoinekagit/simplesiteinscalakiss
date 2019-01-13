import simplehtmlwriterinscala.Basic.{
  AbsNode, AbsAttr, AttrStr, Node, Leaf, SeqNode, StrData, EmptyNode }
import simplehtmlwriterinscala.LessStrict.AttrStrBuilder

import java.io.{File, PrintWriter}
import java.nio.file.{ Path, Paths, Files, FileAlreadyExistsException,
  NoSuchFileException}
import java.util.{NoSuchElementException}

object SimpleSiteInScalaKISS {

  def ssiskpage (attributeName:String) :AttrStrBuilder =
    simplehtmlwriterinscala.Html5.Attr.data("ssiskpage-" + attributeName)

  def ssiskresource (attributeName:String) :AttrStrBuilder =
    simplehtmlwriterinscala.Html5.Attr.data("ssiskresource-" + attributeName)

  def makeSite (
    pages :Map[String,AbsNode],
    dirResources :String = "src/main/resources/",
    dirTarget :String = "target/simplesiteinscalakiss/") {

    mkdir(dirTarget)

    val resourcesAsked = scala.collection.mutable.Set.empty[String]

    def attrs_replaceSsiskAtt (dir:Path, attrs:Seq[AbsAttr]) :Seq[AbsAttr] = {
      attrs.map {

        case AttrStr(name, value) if name.startsWith("data-ssiskpage-") =>
          val pathPage = dir.resolve(Paths.get(value)).normalize
          val strPathPage = pathPage.toString
          if (pages.isDefinedAt(strPathPage)) {
            AttrStr(
              name.substring("data-ssiskpage-".size),
              dir.relativize(pathPage).toString)
          }
          else {
            System.err.println("[Warning] : page not found : " + strPathPage)
            AttrStr(name, value)
          }

        case AttrStr(name, value) if name.startsWith("data-ssiskresource-") =>
          val pathResource = dir.resolve(Paths.get(value)).normalize
          val strPathResource = pathResource.toString
          if (pages.isDefinedAt(strPathResource)) {
            System.err.println(
              "[Warning] : the path of this resource is already used by a page : "
                + strPathResource)
            AttrStr(name, value)
          }
          else {
            if (fileExists(Paths.get(dirResources, strPathResource))) {
              resourcesAsked += strPathResource
              AttrStr(
                name.substring("data-ssiskresource-".size),
                dir.relativize(pathResource).toString)
            }
            else {
              System.err.println(
                "[Warning] : resource not found : "
                  + Paths.get(dirResources, strPathResource))
              AttrStr(name, value)
            }
          }

        case other => other
      }
    }

    def node_replaceSsiskAtt (dir:Path, node:AbsNode) :AbsNode = node match {
      case Node(tag, attrs, childs) =>
        val attrs2 = attrs_replaceSsiskAtt(dir, attrs)
        val childs2 = childs.map(node_replaceSsiskAtt(dir, _))
        Node(tag, attrs2, childs2)
      case Leaf(tag, attrs) =>
        val attrs2 = attrs_replaceSsiskAtt(dir, attrs)
        Leaf(tag, attrs2)
      case SeqNode(childs) => SeqNode(childs.map(node_replaceSsiskAtt(dir, _)))
      case StrData(strData) => StrData(strData)
      case EmptyNode => EmptyNode
    }

    pages.foreach { case (strPath, node) =>
      val pathPage = Paths.get(strPath)
      if (pathPage.isAbsolute) {
        val node2 =
          node_replaceSsiskAtt(pathPage.getParent, node)
        val content = "<!DOCTYPE html>" + node2.toString
        writeFile(Paths.get(dirTarget, strPath), content)
      }
      else System.err.println(
        "[Warning] this page path is not absolute : "
          + strPath + " ; the page is then ignored.")
    }

    resourcesAsked.foreach { resourceName =>
      symbLink(
        Paths.get(dirResources, resourceName),
        Paths.get(dirTarget, resourceName))
    }

  }

  def fileExists (path:Path) :Boolean = path.toFile.exists
  def fileExists (strPath:String) :Boolean = (new File (strPath)).exists

  def fillDirs (file:File) :Unit =
    Files.createDirectories(file.toPath.getParent)

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
