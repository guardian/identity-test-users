package com.gu.identity.testing.usernames

import java.nio.ByteBuffer
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

trait Encoder {
  def encodeSigned(data: Array[Byte]): String

  def decodeSigned(username: String):Option[Array[Byte]]

}

object Encoder {

  val UsernameMaxLength = 20

  val BaseString: String = ((0 to 9) ++ ('A' to 'Z') ++ ('a' to 'z')).mkString
  
  val Base = BaseString.length

  def toBigInt (li: List[Int], base: Int) : BigInt = li match {
    case Nil => BigInt(0)
    case x :: xs => BigInt (x % base) + (BigInt(base) * toBigInt (xs, base))
  }

  def fromBigInt (bi: BigInt, base: Int) : List[Int] =
    if (bi==0L) Nil else (bi % base).toInt :: fromBigInt (bi/base, base)

  def usernameEncode(data: Array[Byte]) =
    fromBigInt(BigInt.apply((1.toByte)+:data), Base).map(BaseString.charAt).mkString

  def usernameDecode(text: String) =
    toBigInt(text.map(BaseString.indexOf(_)).toList, Base).toByteArray.drop(1)

  val MaxByteLength = (1 to UsernameMaxLength).takeWhile(byteLen => usernameEncode(Array.fill[Byte](byteLen)(0)).length<=UsernameMaxLength).max

  val PayloadByteLength = 6

  val TruncatedSigByteLength = MaxByteLength - PayloadByteLength

  /* Adapted from:
   *
   * https://github.com/playframework/playframework/blob/65e098e/framework/src/play/src/main/scala/play/api/libs/Crypto.scala#L94-L110
   *
   * See also http://codahale.com/a-lesson-in-timing-attacks/
   */
  def constantTimeEquals(a : Array[Byte], b : Array[Byte]):Boolean = {
    if (a.length != b.length) false else {
      var equal = 0
      for (i <- 0 until a.length) {
        equal |= a(i) ^ b(i)
      }
      equal == 0
    }
  }

  def withSecret(sharedSecret: String): Encoder = new Encoder {
    
    val sharedSecretBytes = sharedSecret.getBytes("utf-8")
    
    def encodeSigned(data: Array[Byte]):String = {
      require(data.length == PayloadByteLength)
      val bb = ByteBuffer.allocate(MaxByteLength)
      bb.put(data)
      bb.put(truncatedSigFor(data))
      usernameEncode(bb.array())
    }

    def decodeSigned(username: String):Option[Array[Byte]] = {
      val (data, sig) = usernameDecode(username).splitAt(PayloadByteLength)
      if (constantTimeEquals(sig, truncatedSigFor(data))) Some(data) else None
    }

    def truncatedSigFor(data: Array[Byte]): Array[Byte] = {
      val mac = Mac.getInstance("HmacSHA1")
      mac.init(new SecretKeySpec(sharedSecretBytes, "HmacSHA1"))
      mac.doFinal(data).take(TruncatedSigByteLength)
    }
  }
}
