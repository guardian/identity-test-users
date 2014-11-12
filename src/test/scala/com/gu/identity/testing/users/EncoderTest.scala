package com.gu.identity.testing.users

import com.gu.identity.testing.users.Encoder._
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
      val usernameEncoder = Encoder.withSecret("shared-secret-thing")

      val username = usernameEncoder.encodeSigned(data)

      usernameEncoder.decodeSigned(username) must beSome(data)

      Encoder.withSecret("different-shared-secret").decodeSigned(username) mustEqual None
    }
  }
}
