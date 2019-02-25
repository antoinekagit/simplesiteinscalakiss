import simplehtmlwriterinscala.Basic.seq
import simplehtmlwriterinscala.Html5._
import simplehtmlwriterinscala.Html5.Attrs._
import simplehtmlwriterinscala.Implicits._

import simplesiteinscalakiss.{ Config, Site, Page }

object Example {

  def main (args:Array[String]) :Unit = {

    println("Running Example.")

    implicit val config = new Config // default config

    Site.make(config, Set(
      new Page("/index.html") { val node = seq(
        a(href := linkPage("/sous-dossier/page.html"))(
          "page dans un sous dossier"),
        br(),
        a(href := linkPage("sous-dossier/sous-dossier/page.html"))(
          "page dans un sous-dossier dans un sous-dossier"),
        br(),
        a(href := linkResource("pdf/rien.pdf"))(
          "resource pdf vide de contenu"),
        br(),
        img(
          src := linkResource("jpg/spider-cat.jpg"),
          alt := "kitty image found at https://kittybloger.files.wordpress.com")
      )},
      new Page("/sous-dossier/page.html") { val node = seq(
        h1("page dans un sous-dossier"),
        a(href := linkPage("../index.html"))(
          "niveau au dessus"),
        br(),
        a(href := linkPage("sous-dossier/page.html"))(
          "niveau en dessous"),
        br(),
        a(href := linkResource("/pdf/rien.pdf"))(
          "resource pdf vide de contenu")
      )},
      new Page("/sous-dossier/sous-dossier/page.html") { val node = seq(
        span("ca commence a faire beaucoup de niveaux.."),
        br(),
        a(href := linkPage("/index.html"))(
          "retour a l'accueil"),
        br(),
        a(href := linkPage("../page.html"))(
          "retour au niveau - 1"),
        br(),
        a(href := linkResource("texte.txt"))(
          "texte contenant du texte")
      )}
    ))

  }
}
