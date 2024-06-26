package com.gu.identity.testing.usernames

import com.gu.identity.testing.usernames.TestUsernames.{ConfPayloadByteSize, TestUser, logger, maybeTokenFromEmail, tokenToEmail}

import java.nio.ByteBuffer
import java.time.Clock.systemUTC
import java.time.{Clock, Duration, Instant}
import java.time.Duration.ofMinutes
import java.time.Instant.ofEpochSecond
import com.typesafe.scalalogging.LazyLogging



class TestUsernames(usernameEncoder: Encoder, recency: Duration)(implicit clock: Clock) extends LazyLogging {
  def generate(conf: Array[Byte]): String = {
    val bb = ByteBuffer.allocate(Encoder.PayloadByteLength)
    bb.putInt(Instant.now(clock).getEpochSecond.toInt)
    bb.put(conf)
    usernameEncoder.encodeSigned(bb.array())
  }

  def validate(username: String): Option[Array[Byte]] = {

    import scala.math.Ordering.Implicits._

    def extractRecentConfData(validlySignedData: Array[Byte]): Option[Array[Byte]] = {
      val bb = ByteBuffer.wrap(validlySignedData)
      val creationTime = ofEpochSecond(bb.getInt())
      val now = Instant.now(clock)
      if (creationTime > now) {
        logger.warn(s"TestUsername created on $creationTime, apparently AFTER this moment in time! now=$now")
      }

      val confPayload: Array[Byte] = Array.ofDim[Byte](ConfPayloadByteSize)
      bb.get(confPayload)

      val expirationTime = creationTime.plus(recency)
      if (expirationTime > now) Some(confPayload) else {
        logger.warn(s"TestUsername created on $creationTime EXPIRED on $expirationTime")
        None
      }
    }

    for {
      validlySignedData <- usernameEncoder.decodeSigned(username)
      recentlyCreatedConfData <- extractRecentConfData(validlySignedData)
    } yield recentlyCreatedConfData
  }

  def isValid(username: String): Boolean = validate(username).isDefined

  def generateEmail(baseEmail: String): TestUser = {
    val token = generate(Array.ofDim[Byte](2))
    val email: String = tokenToEmail(baseEmail, token)
    TestUser(email, token)
  }

  def isValidEmail(primaryEmailAddress: String): Boolean = {
    val maybeValidTestUser = maybeTokenFromEmail(primaryEmailAddress)
    maybeValidTestUser.exists(isValid)
  }

}

object TestUsernames extends LazyLogging {

  case class TestUser(email: String, token: String)

  def maybeTokenFromEmail(primaryEmailAddress: String): Option[String] =
    for {
      localPart <- primaryEmailAddress.split('@').toList match {
        case local :: _ :: Nil => Some(local)
        case _ => None
      }
      possibleTestUsername <- localPart.split('+').toList match {
        case _ :: subAddress :: _ => Some(subAddress)
        case _ => None
      }
    } yield possibleTestUsername

  def tokenToEmail(baseEmail: String, token: String) =
    baseEmail.replace("@", s"+$token@")

  val ConfPayloadByteSize = 2

  def apply(usernameEncoder: Encoder, recency: Duration = ofMinutes(30))(implicit clock: Clock = systemUTC): TestUsernames =
    new TestUsernames(usernameEncoder, recency)(clock)

}
