package com.gu.identity.testing.usernames

import java.io.FileInputStream
import java.time.Duration
import java.util.Properties

object Main extends App {
  val propertyName="identity.test.users.secret"

  val file: String = args.headOption.getOrElse("/etc/gu/membership-keys.conf")

  println(s"Using property file $file - you can specify a different one at the command line")
  val prop = new Properties()
  prop.load(new FileInputStream(file))

  val secret = prop.getProperty(propertyName).replace("\"","")

  println(s"Loaded secret ${secret.take(3)}...")

  private val usernames = TestUsernames(Encoder.withSecret(secret), Duration.ofMinutes(30))

  println(s"Generated username : ${usernames.generateEmail(None)}")
}
