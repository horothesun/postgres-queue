ThisBuild / organization := "com.horothesun"
ThisBuild / organizationName := "horothesun"
ThisBuild / scalaVersion := "2.13.10"

lazy val root = project
  .in(file("."))
  .settings(name := "postgres-queue")
  .configs(IntegrationTest)
  .settings(Defaults.itSettings)
  .settings(Dependencies.core)
  .settings(Resolvers.settings)

IntegrationTest / parallelExecution := false
