package com.gu.identity.testing.usernames

import java.time.Clock
import java.time.Clock.systemUTC
import java.time.Duration.{ofHours, ofMinutes}

import org.specs2.mutable.Specification

import scala.Array.fill

class TestUsernamesTest extends Specification {

  val clockThatIsCorrect: Clock = systemUTC()

   "Test usernames" should {
     val encoder = Encoder.withSecret("shared=-secret-thing")


     "roundtrip" in {
       val testUsernames = TestUsernames(encoder)

       val confData = fill(2)(6.toByte)
       val username = testUsernames.generate(confData)

       testUsernames.validate(username) must beSome(confData)
     }

     "tolerate clock drift where generator is ahead of verifer" in {
       val clockThatIsAhead = Clock.offset(clockThatIsCorrect, ofMinutes(5))
       val username = TestUsernames(encoder)(clockThatIsAhead).generate(fill(2)(0.toByte))

       TestUsernames(encoder)(clockThatIsCorrect).isValid(username) must beTrue
     }

     "not tolerate clock drift where generator is way behind verifer" in {
       val clockThatIsBehind = Clock.offset(clockThatIsCorrect, ofHours(-2))
       val username = TestUsernames(encoder, ofMinutes(30))(clockThatIsBehind).generate(fill(2)(0.toByte))

       TestUsernames(encoder)(clockThatIsCorrect).isValid(username) must beFalse
     }

   }
 }
