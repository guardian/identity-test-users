package com.gu.identity.testing.users

import org.specs2.mutable.Specification

import scala.Array.fill

class TestUsernamesTest extends Specification {
   "Test usernames" should {
     "roundtrip" in {
       val testUsernames = TestUsers(Encoder.withSecret("shared-secret-thing"))

       val confData = fill(2)(6.toByte)
       val username = testUsernames.generate(confData)

       testUsernames.validate(username) must beSome(confData)
     }

     "tolerate trailing rubbish so that we can easily grab info from email addresses etc" in {
       val testUsernames = TestUsers(Encoder.withSecret("shared-secret-thing"))

       val confData = fill(2)(6.toByte)
       
       val username = testUsernames.generate(confData)

       testUsernames.validate(username+" Smith") must beSome(confData)
       testUsernames.validate(username+".12313123@gu.com") must beSome(confData)
       testUsernames.validate(username+"@gu.com") must beSome(confData)
     }
   }
 }
