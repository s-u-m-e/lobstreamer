package com.excella.lobstreamer.modules

import java.nio.file.Paths

import akka.NotUsed
import akka.stream.alpakka.s3.scaladsl.MultipartUploadResult
import akka.stream.scaladsl.FileIO
import akka.stream.scaladsl.{ Broadcast, GraphDSL, RunnableGraph, Sink, Source }
import akka.stream.{ ClosedShape, SourceShape, UniformFanOutShape }
import akka.util.ByteString
import com.excella.lobstreamer.modules.crypto.decryptionFlow

import scala.concurrent.Future

/**
 * Created by mmekuria on 12/11/17.
 */
trait GraphModule {
  import GraphDSL.Implicits._

  def encryptAndStore(fromRequest: Source[ByteString, Any], sink: Sink[ByteString, Future[MultipartUploadResult]]) =
    RunnableGraph.fromGraph(GraphDSL.create(sink) { implicit b => s =>
      val splitter = b.add(Broadcast[ByteString](2))

      fromRequest ~> crypto.encryptionFlow ~> splitter ~> s
      splitter ~> FileIO.toPath(Paths.get("random.pdf"))
      ClosedShape
    });

  def decryptAndServe(source: Source[ByteString, NotUsed]) = Source.fromGraph(GraphDSL.create() {
    implicit b =>
      val s3source = b.add(source)
      val splitter = b.add(Broadcast[ByteString](2))

      val dfl = b.add(decryptionFlow)
      s3source ~> splitter ~> dfl
      splitter ~> FileIO.toPath(Paths.get("downloaded.pdf"))
      SourceShape(dfl.out)
  });

}
