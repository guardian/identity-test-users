package com.gu.identity.testing.usernames

import java.nio.ByteBuffer

import com.github.nscala_time.time.Imports._

trait TestUsernames {
  def generate(conf: Array[Byte]): String

  def validate(username: String): Option[Array[Byte]]

  def generate(): String = generate(Array.ofDim[Byte](2))

  def isValid(username: String): Boolean = validate(username).isDefined
}

object TestUsernames {

  val ConfPayloadByteSize = 2

  def apply(usernameEncoder: Encoder, recency: Duration = 30.minutes): TestUsernames = new TestUsernames {
    def generate(conf: Array[Byte]): String = {
      val bb = ByteBuffer.allocate(Encoder.PayloadByteLength)
      bb.putInt((DateTime.now.getMillis / 1000).toInt)
      bb.put(conf)
      usernameEncoder.encodeSigned(bb.array())
    }

    def validate(username: String): Option[Array[Byte]] = {
      def extractRecentConfData(validlySignedData: Array[Byte]): Option[Array[Byte]] = {
        val bb = ByteBuffer.wrap(validlySignedData)
        val creationTime = new DateTime(bb.getInt() * 1000L)
        val confPayload: Array[Byte] = Array.ofDim[Byte](ConfPayloadByteSize)
        bb.get(confPayload)
        if ((creationTime to DateTime.now).duration < recency) Some(confPayload) else None
      }

      for {
        validlySignedData <- usernameEncoder.decodeSigned(username)
        recentlyCreatedConfData <- extractRecentConfData(validlySignedData)
      } yield recentlyCreatedConfData
    }
  }
}
