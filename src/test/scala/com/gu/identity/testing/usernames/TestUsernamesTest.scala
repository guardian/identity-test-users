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

     "tolerate being lowercased e.g. via identity email addresses" in {
       val testUsernames = TestUsernames(encoder)

       val confData = fill(2)(6.toByte)
       val username = testUsernames.generate(confData)
       val lower = username.toLowerCase

       testUsernames.validate(lower) must beSome(confData)
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

     "roundtrip as a generic address" in {
       val testUsernames = TestUsernames(encoder)

       val username = testUsernames.generateEmail(None)
       testUsernames.isValidEmail(username.email) must beTrue
     }

     "roundtrip as a known email address" in {
       val testUsernames = TestUsernames(encoder)

       val username = testUsernames.generateEmail(Some("test.user@thegulocal.com"))

       testUsernames.isValidEmail(username.email) must beTrue
     }

     "not think a normal email address is a test user" in {
       val testUsernames = TestUsernames(encoder)

       testUsernames.isValidEmail("dummy@thegulocal.com") must beFalse
     }

     "not think a normal email address with a plus is a test user" in {
       val testUsernames = TestUsernames(encoder)

       testUsernames.isValidEmail("dummy+1234@thegulocal.com") must beFalse
     }

     "extract from an email address" in {
       val extractedToken = TestUsernames.maybeTokenFromEmail("test.user+TOKEN@thegulocal.com")
       extractedToken must beSome("TOKEN")
     }

     "extract the whole local part from an email address without a subaddress" in {
       val extractedToken = TestUsernames.maybeTokenFromEmail("TOKEN@thegulocal.com")
       extractedToken must beSome("TOKEN")
     }

     "not extract from an email address with no domain separator" in {
       val extractedToken = TestUsernames.maybeTokenFromEmail("test.user.without.domain")
       extractedToken must beNone
     }

     "embed into an email address" in {
       val email = TestUsernames.tokenToEmail(Some("test.user@thegulocal.com"), "TOKEN")
       email must beEqualTo("test.user+TOKEN@thegulocal.com")
     }

     "embed into a dummy email address" in {
       val email = TestUsernames.tokenToEmail(None, "TOKEN")
       email must beEqualTo("TOKEN@thegulocal.com")
     }

   }
 }
