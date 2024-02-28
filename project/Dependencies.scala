import sbt.*
import sbt.Keys.libraryDependencies

object Dependencies {

  object Version {

    val catsEffect = "3.5.3"

    val betterMonadicFor = "0.3.1"

    val fs2 = "3.9.4"

    val circe = "0.14.6"

    val skunk = "0.6.3"

    val logbackClassic = "1.5.1"

    val munit = "0.7.29"

    val munitCatsEffect3 = "1.0.7"

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
    "org.typelevel" %% "munit-cats-effect-3" % Version.munitCatsEffect3
  ).map(_ % Test)

  lazy val core = libraryDependencies ++= (project ++ logs ++ test)

}
