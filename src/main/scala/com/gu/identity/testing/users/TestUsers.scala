package com.gu.identity.testing.users

import java.nio.ByteBuffer

import com.github.nscala_time.time.Imports._

trait TestUsers {
  def generate(conf: Array[Byte]): String

  def validate(userString: String): Option[Array[Byte]]

  def generate(): String = generate(Array.ofDim[Byte](2))

  def isValid(userString: String): Boolean = validate(userString).isDefined
}

object TestUsers {

  val ConfPayloadByteSize = 2

  def apply(encoder: Encoder, recency: Duration = 30.minutes): TestUsers = new TestUsers {
    def generate(conf: Array[Byte]): String = {
      val bb = ByteBuffer.allocate(Encoder.PayloadByteLength)
      bb.putInt((DateTime.now.getMillis / 1000).toInt)
      bb.put(conf)
      encoder.encodeSigned(bb.array())
    }

    def validate(userString: String): Option[Array[Byte]] = {
      def extractRecentConfData(validlySignedData: Array[Byte]): Option[Array[Byte]] = {
        val bb = ByteBuffer.wrap(validlySignedData)
        val creationTime = new DateTime(bb.getInt() * 1000L)
        val confPayload: Array[Byte] = Array.ofDim[Byte](ConfPayloadByteSize)
        bb.get(confPayload)
        if ((creationTime to DateTime.now).duration < recency) Some(confPayload) else None
      }
      for {
        validlySignedData <- encoder.decodeSigned(userString.takeWhile(Encoder.BaseChars))
        recentlyCreatedConfData <- extractRecentConfData(validlySignedData)
      } yield recentlyCreatedConfData
    }
  }
}
