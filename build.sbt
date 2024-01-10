import sbt.Keys.*
import sbtrelease.*
import ReleaseStateTransformations.*
import sbtversionpolicy.withsbtrelease.ReleaseVersion

name := "identity-test-users"

organization := "com.gu"

scalaVersion := "2.13.12"

crossScalaVersions := Seq(scalaVersion.value, "3.3.1")

scalacOptions := Seq("-release:11")

releaseCrossBuild := true

description := "Test Users for Identity"

licenses := Seq(License.Apache2)

libraryDependencies ++= Seq(
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.5",
  "ch.qos.logback" % "logback-classic" % "1.4.11" % "test",
  "org.specs2" %% "specs2-core" % "4.20.3" % "test"
)

lazy val root = project in file(".")

releaseVersion := ReleaseVersion.fromAggregatedAssessedCompatibilityWithLatestRelease().value

releaseProcess := Seq[ReleaseStep](
  checkSnapshotDependencies,
  inquireVersions,
  runClean,
  runTest,
  setReleaseVersion,
  commitReleaseVersion,
  tagRelease,
  setNextVersion,
  commitNextVersion,
)
