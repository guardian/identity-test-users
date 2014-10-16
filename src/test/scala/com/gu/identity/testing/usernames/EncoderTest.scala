package com.gu.identity.testing.usernames

import com.gu.identity.testing.usernames.Encoder._
import org.specs2.mutable.Specification

import scala.Array.fill
import scala.util.Random

class EncoderTest extends Specification {
  "Username encoder" should {
    "roundtrip" in {
      val r = new Random(0)
      for (arraySize <- 1 to 20) {
        val byteArray: Array[Byte] = new Array[Byte](arraySize)
        usernameDecode(usernameEncode(byteArray)) mustEqual byteArray

        for (sample <- 1 to 10) {
          r.nextBytes(byteArray)
          usernameDecode(usernameEncode(byteArray)) mustEqual byteArray
        }
      }
      usernameDecode(usernameEncode(Array(0.toByte))) mustEqual Array(0.toByte)
    }

    "generate verifiable username" in {
      val data = fill(6)(5.toByte)
      val sharedSecret: Array[Byte] = fill(20)(1.toByte)
      val usernameEncoder = Encoder.withSecret(sharedSecret)

      val username = usernameEncoder.encodeSigned(data)

      usernameEncoder.decodeSigned(username) must beSome(data)

      Encoder.withSecret(fill(20)(2.toByte)).decodeSigned(username) mustEqual None
    }
  }
}
