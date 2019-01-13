import simplehtmlwriterinscala.Basic.AbsNode
import simplehtmlwriterinscala.Html5._
import simplehtmlwriterinscala.Html5.Attr._
import simplehtmlwriterinscala.Implicits._

import SimpleSiteInScalaKISS.{ ssiskpage, ssiskresource, makeSite }

object Example {

  def main (args:Array[String]) :Unit = {

    println("Running Example.");

    makeSite(Map(
      "/index.html" -> seq(
        a(
          ssiskpage("href") := "/sous-dossier/page.html"
        )("page dans un sous dossier"),
        br(),
        a(
          ssiskpage("href") := "sous-dossier/sous-dossier/page.html"
        )("page dans un sous-dossier dans un sous-dossier"),
        br(),
        a(ssiskresource("href") := "pdf/rien.pdf")("resource pdf vide de contenu"),
        br(),
        img(
          ssiskresource("src") := "jpg/spider-cat.jpg",
          alt := "kitty image found at https://kittybloger.files.wordpress.com")
      ),
      "/sous-dossier/page.html" -> seq(
        h1("page dans un sous-dossier"),
        a(ssiskpage("href") := "../index.html")("niveau au dessus"),
        br(),
        a(ssiskpage("href") := "sous-dossier/page.html")("niveau en dessous"),
        br(),
        a(ssiskresource("href") := "../pdf/rien.pdf")("resource pdf vide de contenu")
      ),
      "/sous-dossier/sous-dossier/page.html" -> seq(
        span("ça commence à faire beaucoup de niveaux.."),
        br(),
        a(ssiskpage("href") := "/index.html")("retour à l'accueil"),
        br(),
        a(ssiskpage("href") := "../page.html")("retour au niveau - 1"),
        br(),
        a(ssiskresource("href") := "/pdf/rien.pdf")("resource pdf vide de contenu")
      )
    ))

  }
}
