package com.excella.lobstreamer.modules

import java.security.spec.{ PKCS8EncodedKeySpec, X509EncodedKeySpec }
import java.security._
import javax.crypto.spec.{ IvParameterSpec, SecretKeySpec }
import javax.crypto.{ Cipher, KeyGenerator }

import akka.NotUsed
import akka.stream.scaladsl.{ GraphDSL, RunnableGraph, Sink, Source }
import akka.stream._
import akka.stream.alpakka.s3.scaladsl.MultipartUploadResult
import akka.stream.stage.{ GraphStage, GraphStageLogic, InHandler, OutHandler }
import akka.util.ByteString

import scala.concurrent.Future

object crypto {


  class AesStage(cipher: Cipher) extends GraphStage[FlowShape[ByteString, ByteString]] {
    val in = Inlet[ByteString]("in")
    val out = Outlet[ByteString]("out")

    override val shape = FlowShape.of(in, out)

    override def createLogic(inheritedAttributes: Attributes): GraphStageLogic =
      new GraphStageLogic(shape) {
        setHandler(in, new InHandler {
          override def onPush(): Unit = {
            val bs = grab(in)
            if (bs.isEmpty) push(out, bs)
            else push(out, ByteString(cipher.update(bs.toArray)))
          }

          override def onUpstreamFinish(): Unit = {
            val bs = ByteString(cipher.doFinal())
            if (bs.nonEmpty) emit(out, bs)
            complete(out)
          }
        })

        setHandler(out, new OutHandler {
          override def onPull(): Unit = {
            pull(in)
          }
        })
      }
  }

  val aesKeySize = 128
  val rand = new SecureRandom()

  def generateIv() = rand.generateSeed(16)
  def generateAesKey() = {
    val gen = KeyGenerator.getInstance("AES")
    gen.init(aesKeySize)
    val key = gen.generateKey()
    val aesKey = key.getEncoded()
    aesKeySpec(aesKey)
  }

  private val aesKey = generateAesKey()
  private val iv = generateIv()




  def aesKeySpec(key: Array[Byte]) =
    new SecretKeySpec(key, "AES")

  private def aesCipher(mode: Int, keySpec: SecretKeySpec, ivBytes: Array[Byte]) = {
    val cipher = Cipher.getInstance("AES/CFB/NoPadding")
    val ivSpec = new IvParameterSpec(ivBytes)
    cipher.init(mode, keySpec, ivSpec)
    cipher
  }

  def encryptionCipher =  aesCipher(Cipher.ENCRYPT_MODE, aesKey, iv)
  def decryptionCipher = aesCipher(Cipher.DECRYPT_MODE, aesKey, iv)


  def getRsaKeyFactory() =
    KeyFactory.getInstance("RSA")

  def loadRsaPrivateKey(key: Array[Byte]) = {
    val spec = new PKCS8EncodedKeySpec(key)
    getRsaKeyFactory.generatePrivate(spec)
  }

  def loadRsaPublicKey(key: Array[Byte]) = {
    val spec = new X509EncodedKeySpec(key)
    getRsaKeyFactory.generatePublic(spec)
  }

  private def rsaCipher(mode: Int, key: Key) = {
    val cipher = Cipher.getInstance("RSA")
    cipher.init(mode, key)
    cipher
  }

  def encryptRsa(bytes: Array[Byte], key: PublicKey): Array[Byte] =
    rsaCipher(Cipher.ENCRYPT_MODE, key).doFinal(bytes)

  def decryptRsa(bytes: Array[Byte], key: PrivateKey): Array[Byte] =
    rsaCipher(Cipher.DECRYPT_MODE, key).doFinal(bytes)

}
