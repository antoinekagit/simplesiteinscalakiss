import simplehtmlwriterinscala.Basic.AbsNode
import simplehtmlwriterinscala.Html5._
import simplehtmlwriterinscala.Html5.Attr._
import simplehtmlwriterinscala.Implicits._

import simplesiteinscalakiss.{ Config, Site, Page }

object Example {

  def main (args:Array[String]) :Unit = {

    println("Running Example.")

    implicit val config = new Config // default config

    Site.make(Set(
      new Page("/index.html") { val node = seq(
        a(href := ssisk.linkpage("/sous-dossier/page.html"))(
          "page dans un sous dossier"),
        br(),
        a(href := ssisk.linkpage("sous-dossier/sous-dossier/page.html"))(
          "page dans un sous-dossier dans un sous-dossier"),
        br(),
        a(href := ssisk.linkresource("pdf/rien.pdf"))(
          "resource pdf vide de contenu"),
        br(),
        img(
          src := ssisk.linkresource("jpg/spider-cat.jpg"),
          alt := "kitty image found at https://kittybloger.files.wordpress.com")
      )},
      new Page("/sous-dossier/page.html") { val node = seq(
        h1("page dans un sous-dossier"),
        a(href := ssisk.linkpage("../index.html"))(
          "niveau au dessus"),
        br(),
        a(href := ssisk.linkpage("sous-dossier/page.html"))(
          "niveau en dessous"),
        br(),
        a(href := ssisk.linkresource("/pdf/rien.pdf"))(
          "resource pdf vide de contenu")
      )},
      new Page("/sous-dossier/sous-dossier/page.html") { val node = seq(
        span("ca commence a faire beaucoup de niveaux.."),
        br(),
        a(href := ssisk.linkpage("/index.html"))(
          "retour a l'accueil"),
        br(),
        a(href := ssisk.linkpage("../page.html"))(
          "retour au niveau - 1"),
        br(),
        a(href := ssisk.linkresource("texte.txt"))(
          "texte contenant du texte")
      )}
    ))

  }
}
