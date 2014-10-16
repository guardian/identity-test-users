package com.gu.identity.testing.usernames

import org.specs2.mutable.Specification

import scala.Array.fill

class TestUsernamesTest extends Specification {
   "Test usernames" should {
     "roundtrip" in {
       val testUsernames = TestUsernames(Encoder.withSecret("shared=-secret-thing"))

       val confData = fill(2)(6.toByte)
       val username = testUsernames.generate(confData)

       testUsernames.validate(username) must beSome(confData)
     }

   }
 }
