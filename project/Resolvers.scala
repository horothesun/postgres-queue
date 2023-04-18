import sbt.*
import sbt.Keys.*

object Resolvers {

  private val repos = Seq(
    "confluent" at "https://packages.confluent.io/maven/"
  )

  lazy val settings = Seq(
    resolvers ++= repos,
    retrieveManaged := true
  )

}
