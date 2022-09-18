import sbt._
import sbt.Keys._

object Resolvers {

  private val repos = Seq(
    "confluent" at "https://packages.confluent.io/maven/"
  )

  lazy val settings = Seq(
    resolvers ++= repos,
    retrieveManaged := true
  )

}
