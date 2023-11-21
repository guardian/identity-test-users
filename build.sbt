import sbt.Keys.*
import sbtrelease.*
import ReleaseStateTransformations.*

name := "identity-test-users"

organization := "com.gu"

scalaVersion := "2.13.12"

crossScalaVersions := Seq(scalaVersion.value, "3.3.1")

releaseCrossBuild := true

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
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.5",
  "ch.qos.logback" % "logback-classic" % "1.4.11" % "test",
  "org.specs2" %% "specs2-core" % "4.20.3" % "test"
)

lazy val root = project in file(".")

releaseProcess := Seq[ReleaseStep](
  checkSnapshotDependencies,
  inquireVersions,
  runClean,
  runTest,
  setReleaseVersion,
  commitReleaseVersion,
  tagRelease,
  releaseStepCommandAndRemaining("+publishSigned"),
  releaseStepCommand("sonatypeBundleRelease"),
  setNextVersion,
  commitNextVersion,
  pushChanges
)
