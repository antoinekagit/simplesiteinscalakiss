import scala.language.implicitConversions

package simplehtmlwriterinscala {

  object Basic {

    case class Attr (
      val name :String,
      val value :String
    ) {
      override def toString = "%s=\"%s\"" format (name, value)
    }

    trait TNode 

    case class Node (
      val tag :String,
      var attrs :Seq[Attr],
      var childs :Seq[TNode]
    ) extends TNode {

      override def toString = {
        val attStr =
          if (attrs.isEmpty) ""
          else attrs mkString (" ", " ", " ")
        s"<$tag$attStr>${childs.mkString}</$tag>"
      }
    }

    case class Leaf (
      val tag :String,
      val attrs :Seq[Attr]
    ) extends TNode {

      override def toString = {
        val attStr =
          if (attrs.isEmpty) ""
          else attrs mkString (" ", " ", " ")
        s"<$tag$attStr/>" }
    }

    case class StrDataNode (strData:String) extends TNode {
      override def toString = strData
    }

    case class VirtualSeqNode (childs:Seq[TNode]) extends TNode {
      override def toString = childs.mkString
    }

    case object EmptyNode extends TNode {
      override def toString = ""
    }


    case class AttrMaker (attrMaker:String=>Attr) {
      def apply (value:String) = attrMaker (value)
      def := (value:String) = attrMaker (value)
    }

    case class NodeMaker (nodeMaker :Seq[Attr]=>Seq[TNode]=>Node) {
      case class ChildsMaker (attrs:Seq[Attr]) {
        def apply (childs:Seq[TNode]) = nodeMaker (attrs) (childs)
        def apply (first:TNode, childs:TNode*) =
          nodeMaker (attrs) (first +: childs)
        def apply () = nodeMaker (attrs) (Seq.empty)
      }
      def apply (first:Attr, attrs:Attr*) = ChildsMaker (first +: attrs)
      def apply (attrs:Seq[Attr]) = ChildsMaker (attrs)
      def apply = ChildsMaker (Seq.empty)
    }

    case class LeafMaker (leafMaker :Seq[Attr]=>Leaf) {
      def apply (first:Attr, attrs:Attr*) = leafMaker (first +: attrs)
      def apply (attrs:Seq[Attr]) = leafMaker (attrs)
      def apply () = leafMaker (Seq.empty)
    }

    object VirtualSeqMaker {
      def apply (first:TNode, childs:TNode*) = VirtualSeqNode (first +: childs)
      def apply (childs:Seq[TNode]) = VirtualSeqNode (childs)
      def apply () = VirtualSeqNode (Seq.empty)
    }

    object AttrMakerSimple {
      def apply (name:String) = AttrMaker { value => Attr (name, value) }
    }

    object NodeMakerSimple {
      def apply (tag:String) = NodeMaker { attrs => childs =>
        Node (tag, attrs, childs)
      }
    }

    object LeafMakerSimple {
      def apply (tag:String) = LeafMaker { attrs => Leaf (tag, attrs) }
    }

    lazy val id = AttrMakerSimple("id")

    lazy val body = NodeMakerSimple("body")

    lazy val br = LeafMakerSimple("br")

    lazy val seq = VirtualSeqMaker
    lazy val empty = EmptyNode

    val n = br(Seq.empty)
    val a = Attr("id", "bob")

    val l = Seq(
      body(Seq(a, a))(Seq(n, n)),
      body(Seq(a, a))(n, n),
      body(a, a)(Seq(n, n)),
      body(a, a)(n, n),
      body(Seq(n, n)),
      body(n, n),
      body(),

      br(Seq(a, a)),
      br(a, a),
      br(),

      id("toto"),
      id := "toto"
    )

  }

  // Here are two implicits conversion methods
  // In a separate object to prevent 'surprise implicits'
  object Implicits {
    import Basic._
    // conversion from string to string data (see Basic above)
    implicit def string_to_StrDataNode (str:String) = StrDataNode (str)
    // conversion from sequence to seq node (see Basic above)
    implicit def seq_to_VirtualSeqNode (sn:Seq[TNode]) = VirtualSeqNode(sn)
    implicit def mutiter_to_AbsNode [X<:TNode]
      (in:scala.collection.mutable.Iterable[X]) :VirtualSeqNode =
      VirtualSeqNode(in.toSeq)
  }

  // Here are a collection of methods to build HTML5 nodes and attributes
  // You can update it or create your own, the model is simple
  object Html5 {

    import Basic.{
      AttrMaker, AttrMakerSimple, NodeMakerSimple, LeafMakerSimple }

    // attributes in a separate object because these names are common
    object Attrs {
      lazy val alt = AttrMakerSimple("alt")
      lazy val id = AttrMakerSimple("id")
      lazy val charset = AttrMakerSimple("charset")
      lazy val classes = AttrMakerSimple("class")
      lazy val href = AttrMakerSimple("href")
      lazy val rel = AttrMakerSimple("rel")
      lazy val src = AttrMakerSimple("src")
      lazy val titleAttr = AttrMakerSimple("title")
      def dataAttr (name:String) = AttrMaker { value =>
        Basic.Attr ("data-name", value) }
    }

    // 'block' nodes
    lazy val a = NodeMakerSimple("a")
    lazy val body = NodeMakerSimple("body")
    lazy val div = NodeMakerSimple("div")
    lazy val footer = NodeMakerSimple("footer")
    lazy val h1 = NodeMakerSimple("h1")
    lazy val h2 = NodeMakerSimple("h2")
    lazy val h3 = NodeMakerSimple("h3")
    lazy val h4 = NodeMakerSimple("h4")
    lazy val h5 = NodeMakerSimple("h5")
    lazy val h6 = NodeMakerSimple("h6")
    lazy val head = NodeMakerSimple("head")
    lazy val html = NodeMakerSimple("html")
    lazy val i = NodeMakerSimple("i")
    lazy val li = NodeMakerSimple("li")
    lazy val ol = NodeMakerSimple("ol")
    lazy val p = NodeMakerSimple("p")
    lazy val pre = NodeMakerSimple("pre")
    lazy val section = NodeMakerSimple("section")
    lazy val span = NodeMakerSimple("span")
    lazy val strong = NodeMakerSimple("strong")
    lazy val style = NodeMakerSimple("style")
    lazy val sup = NodeMakerSimple("sup")
    lazy val table = NodeMakerSimple("table")
    lazy val td = NodeMakerSimple("td")
    lazy val title = NodeMakerSimple("title")
    lazy val th = NodeMakerSimple("th")
    lazy val tr = NodeMakerSimple("tr")
    lazy val ul = NodeMakerSimple("ul")

    // 'auto-closing' nodes
    lazy val base = LeafMakerSimple("base")
    lazy val br = LeafMakerSimple("br")
    lazy val img = LeafMakerSimple("img")
    lazy val link = LeafMakerSimple("link")
    lazy val meta = LeafMakerSimple("meta")
  }
}
