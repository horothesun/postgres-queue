import sbt.*
import sbt.Keys.libraryDependencies

object Dependencies {

  object Version {

    val cats = "2.12.0"

    val catsEffect = "3.5.7"

    val betterMonadicFor = "0.3.1"

    val fs2 = "3.11.0"

    val circe = "0.14.10"

    val skunk = "0.6.4"

    val logbackClassic = "1.5.13"

    val munit = "1.0.0"

    val munitCatsEffect = "2.0.0"

    val munitScalacheck = "1.0.0"

    val scalacheck = "1.18.1"

    val scalacheckEffectMunit = "1.0.4"

  }

  lazy val project: Seq[ModuleID] = Seq(
    "org.typelevel" %% "cats-core" % Version.cats,
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
    "org.typelevel" %% "munit-cats-effect" % Version.munitCatsEffect,
    "org.scalameta" %% "munit-scalacheck" % Version.munitScalacheck,
    "org.scalacheck" %% "scalacheck" % Version.scalacheck,
    "org.typelevel" %% "scalacheck-effect-munit" % Version.scalacheckEffectMunit,
    "org.typelevel" %% "cats-effect-testkit" % Version.catsEffect
  ).map(_ % Test)

  lazy val core = libraryDependencies ++= (project ++ logs ++ test)

}
