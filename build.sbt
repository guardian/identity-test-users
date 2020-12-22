import sbt.Keys._
import sbtrelease._
import ReleaseStateTransformations._

releaseSettings

sonatypeSettings

name := "identity-test-users"

organization := "com.gu"

scalaVersion := "2.11.8"

crossScalaVersions := Seq(scalaVersion.value, "2.12.1", "2.13.4")

ReleaseKeys.crossBuild := true

scmInfo := Some(ScmInfo(
  url("https://github.com/guardian/identity-test-users"),
  "scm:git:git@github.com:guardian/identity-test-users.git"
))

description := "Test Users for Identity"

pomExtra := (
  <url>https://github.com/guardian/identity-test-users</url>
    <developers>
      <developer>
        <id>rtyley</id>
        <name>Roberto Tyley</name>
        <url>https://github.com/rtyley</url>
      </developer>
    </developers>
  )

licenses := Seq("Apache V2" -> url("http://www.apache.org/licenses/LICENSE-2.0.html"))

libraryDependencies ++= Seq(
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2",
  "ch.qos.logback" % "logback-classic" % "1.1.9" % "test",
  "org.specs2" %% "specs2-core" % "4.10.0" % "test"
)

lazy val root = project in file(".")

ReleaseKeys.releaseProcess := Seq[ReleaseStep](
  checkSnapshotDependencies,
  inquireVersions,
  runClean,
  runTest,
  setReleaseVersion,
  commitReleaseVersion,
  tagRelease,
  ReleaseStep(
    action = state => Project.extract(state).runTask(PgpKeys.publishSigned, state)._1,
    enableCrossBuild = true
  ),
  setNextVersion,
  commitNextVersion,
  ReleaseStep(state => Project.extract(state).runTask(SonatypeKeys.sonatypeReleaseAll, state)._1),
  pushChanges
)
