import sbt.*
import sbt.Keys.libraryDependencies

object Dependencies {

  object Version {

    val catsEffect = "3.5.4"

    val betterMonadicFor = "0.3.1"

    val fs2 = "3.10.2"

    val circe = "0.14.7"

    val skunk = "0.6.4"

    val logbackClassic = "1.5.6"

    val munit = "0.7.29"

    val munitCatsEffect3 = "2.0.0"

  }

  lazy val project: Seq[ModuleID] = Seq(
    "org.typelevel" %% "cats-effect" % Version.catsEffect,
    compilerPlugin("com.olegpy" %% "better-monadic-for" % Version.betterMonadicFor),
    "co.fs2" %% "fs2-core" % Version.fs2,
    "org.tpolecat" %% "skunk-core" % Version.skunk,
    "io.circe" %% "circe-generic" % Version.circe
  )

  lazy val logs: Seq[ModuleID] = Seq(
    "ch.qos.logback" % "logback-classic" % Version.logbackClassic
  )

  lazy val test: Seq[ModuleID] = List(
    "io.circe" %% "circe-parser" % Version.circe,
    "org.scalameta" %% "munit-scalacheck" % Version.munit,
    "org.typelevel" %% "munit-cats-effect" % Version.munitCatsEffect3
  ).map(_ % Test)

  lazy val core = libraryDependencies ++= (project ++ logs ++ test)

}
