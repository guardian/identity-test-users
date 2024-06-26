package com.gu.identity.testing.usernames

import java.nio.ByteBuffer
import java.time.Clock.systemUTC
import java.time.{Clock, Duration, Instant}
import java.time.Duration.ofMinutes
import java.time.Instant.ofEpochSecond

import com.typesafe.scalalogging.LazyLogging



trait TestUsernames {
  def generate(conf: Array[Byte]): String

  def validate(username: String): Option[Array[Byte]]

  def generate(): String = generate(Array.ofDim[Byte](2))

  def isValid(username: String): Boolean = validate(username).isDefined
}

object TestUsernames extends LazyLogging {

  val ConfPayloadByteSize = 2

  def apply(usernameEncoder: Encoder, recency: Duration = ofMinutes(30))(implicit clock: Clock = systemUTC): TestUsernames = new TestUsernames {
    def generate(conf: Array[Byte]): String = {
      val bb = ByteBuffer.allocate(Encoder.PayloadByteLength)
      bb.putInt(Instant.now(clock).getEpochSecond.toInt)
      bb.put(conf)
      val baseUsername = usernameEncoder.encodeSigned(bb.array())
      // upper case half of the letters to make it usable as a password too
      baseUsername.zipWithIndex.map {
        case (c, idx) if idx % 2 == 0 => c.toUpper
        case (c, _) => c
      }.mkString
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
        validlySignedData <- usernameEncoder.decodeSigned(username.toLowerCase)
        recentlyCreatedConfData <- extractRecentConfData(validlySignedData)
      } yield recentlyCreatedConfData
    }
  }
}
