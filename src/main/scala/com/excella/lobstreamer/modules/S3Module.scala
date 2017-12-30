package com.excella.lobstreamer.modules

import akka.stream.alpakka.s3.scaladsl.S3Client

/**
 * Created by mmekuria on 12/3/17.
 */

trait S3Module {
  self: ActorModule with ConfigModule =>

  lazy val bucketName = config.getString("lobstreamer.bucketname")
  val client = S3Client()
  def s3Sink(key: String) = client.multipartUpload(bucketName, key)
  def s3Source(key: String) = client.download(bucketName, key)

}
