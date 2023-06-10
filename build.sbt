ThisBuild / organization := "com.horothesun"
ThisBuild / organizationName := "horothesun"
ThisBuild / scalaVersion := "2.13.11"

scalacOptions ++= Seq("-deprecation", "-feature")

lazy val root = project
  .in(file("root"))
  .settings(name := "postgres-queue")
  .settings(Dependencies.core)

lazy val integration = project
  .in(file("integration"))
  .dependsOn(root)
  .settings(Dependencies.core)
  .settings(publish / skip := true)
  .settings(test / parallelExecution := false)
